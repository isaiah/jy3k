package org.python.core;

import org.python.Version;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;
import org.python.bootstrap.Import;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * This is a python importer (finder & loader) that locates python modules from java classpath
 * The resource have to be in /Lib/ and if compiles it have to be in __pycache__/
 */
@ExposedType(name = "builtins.classpath_loader")
public class PyClasspathLoader extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyClasspathLoader.class);
    public static final String IMPORT_PATH_ENTRY = "/__resources__";
    public static final String PREFIX = "Lib/";

    private ClassLoader cl;
    private String prefix;

    public PyClasspathLoader() {
        super(TYPE);
        this.prefix = PREFIX;
        this.cl = PyClasspathLoader.class.getClassLoader();
    }

    public PyClasspathLoader(String prefix) {
        super(TYPE);
        this.prefix = PREFIX + prefix + "/";
        this.cl = PyClasspathLoader.class.getClassLoader();
    }

    @ExposedMethod
    public boolean is_package(String fullname) {
        String filename = get_filename(fullname);
        return filename.endsWith("__init__.py");
    }
    @ExposedMethod
    public String get_filename(String fullname) {
        ModuleSpec spec = getSpec(fullname);
        if (spec == null) {
            throw Py.ImportError("not found", fullname);
        }
        return spec.origin;
    }

    private ModuleSpec getSpec(String fullname) {
        String sep = "/";
        String pkg = fullname.replaceAll("\\.", sep);
        String classFile = pkg + sep + Import.CACHEDIR + sep + "__init__." + Version.PY_CACHE_TAG + ".class";
        String pyFile = pkg + sep + "__init__.py";
        boolean cached = getResource(classFile) != null;
        if (cached || getResource(pyFile) != null) {
            return new ModuleSpec(pyFile, classFile, pkg,true, cached);
        }

        int base = pkg.lastIndexOf(sep);
        if (base > -1) {
            classFile = pkg.substring(0, base) + sep + Import.CACHEDIR + sep + pkg.substring(base + 1) + "." + Version.PY_CACHE_TAG + ".class";
        } else {
            classFile = Import.CACHEDIR + sep + pkg + "." + Version.PY_CACHE_TAG + ".class";
        }

        pyFile = pkg + ".py";
        cached = getResource(classFile) != null;
        if (cached || getResource(pyFile) != null) {
            return new ModuleSpec(pyFile, classFile, false, cached);
        }
        return null;
    }

    @ExposedMethod
    public PyObject get_code(String fullname) {
        ModuleSpec spec = getSpec(fullname);
        return getCode(fullname, spec);
    }

    private PyCode getCode(String fullname, ModuleSpec spec) {
        byte[] bytes;
        InputStream data;
        try {
            if (spec.isCached) {
                data = getResourceAsStream(spec.cached);
                bytes = data.readAllBytes();
            } else {
                data = getResourceAsStream(spec.origin);
                bytes = Import.compileSource(fullname, data, spec.origin);
            }
        } catch (IOException e) {
            throw Py.IOError(e);
        }
        return BytecodeLoader.makeCode(fullname + Version.PY_CACHE_TAG, bytes, spec.origin);
    }

    @ExposedMethod
    public PyObject find_module(String fullname) {
        ModuleSpec spec = getSpec(fullname);
        if (spec == null) {
            return Py.None;
        }
        return this;
    }

    @ExposedMethod
    public PyObject load_module(String fullname) {
        ModuleSpec spec = getSpec(fullname);
        PyCode code = getCode(fullname, spec);
        PyModule module = Import.addModule(fullname);
        module.__setattr__("__file__", new PyUnicode(spec.origin));
        if (spec.isCached) {
            module.__setattr__("__cached__", new PyUnicode(spec.cached));
        }
        if (spec.isPackage) {
            // This is the hint for the import machinery to use this loader for submodules
            module.__setattr__("__path__", new PyUnicode(IMPORT_PATH_ENTRY));
        }
        Py.runCode(code, module.__dict__, module.__dict__);
        return module;
    }

    private URL getResource(String name) {
        return cl.getResource(prefix + name);
    }

    private InputStream getResourceAsStream(String pn) {
        return cl.getResourceAsStream(prefix + pn);
    }

    class ModuleSpec {
        final String origin;
        final String cached;
        final boolean isPackage;
        final boolean isCached;
        final String path;

        ModuleSpec(String origin, String cached, String path, boolean isPackage, boolean isCached) {
            this.origin = origin;
            this.cached = cached;
            this.isPackage = isPackage;
            this.isCached = isCached;
            this.path = path;
        }

        ModuleSpec(String origin, String cached, boolean isPackage, boolean isCached) {
            this(origin, cached, null, isPackage, isCached);
        }
    }
}
