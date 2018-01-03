package org.python.core;

import org.python.Version;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;
import org.python.bootstrap.Import;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This is a python importer (finder & loader) that locates python modules from java classpath
 * The resource have to be in /Lib/ and if compiles it have to be in __pycache__/
 */
@ExposedType(name = "builtins.classpath_importer")
public class PyClasspathImporter extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyClasspathImporter.class);
    public static final String IMPORT_PATH_ENTRY = "/__resources__";

    private ClassLoader cl;

    public PyClasspathImporter() {
        super(TYPE);
        cl = PyClasspathImporter.class.getClassLoader();
    }

    @ExposedMethod
    public boolean is_package(String fullname) {
        String filename = get_filename(fullname);
        return filename.endsWith("__init__" + Version.PY_CACHE_TAG + ".class") || filename.endsWith("__init__.py");
    }

    @ExposedMethod
    public String get_filename(String fullname) {
        String sep = "/";
        String pkg = fullname.replaceAll("\\.", sep);
        String classFile = pkg + sep + Import.CACHEDIR + sep + "__init__." + Version.PY_CACHE_TAG + ".class";
        URL initClassPath = getResource(classFile);
        if (initClassPath != null) {
            return initClassPath.getPath();
        }

        String pyFile = pkg + sep + "__init__.py";
        URL initPath = getResource(pyFile);
        if (initPath != null) {
            return pyFile;
        }
        int base = pkg.lastIndexOf(sep);
        if (base > -1) {
            classFile = pkg.substring(0, base) + Import.CACHEDIR + sep + pkg.substring(base + 1) + "." + Version.PY_CACHE_TAG + ".class";
        } else {
            classFile = Import.CACHEDIR + sep + pkg + "." + Version.PY_CACHE_TAG + ".class";
        }
        URL classPath = getResource(classFile);
        if (classPath != null) {
            return classFile;
        }
        pyFile = pkg + ".py";
        URL pyPath = getResource(pyFile);
        if (pyPath != null) {
            return pyFile;
        }
        throw Py.ImportError(fullname + " not found");
    }

    @ExposedMethod(defaults = {"null"})
    public PyObject find_spec(PyObject fullname, PyObject target) {
        PySystemState interp = Py.getSystemState();
        return interp.importlib.invoke("spec_from_loader", fullname, this);
    }

    @ExposedMethod
    public void create_module(PyObject module) {}

    @ExposedMethod
    public PyObject exec_module(PyObject moduleObj) {
        PyModule module = (PyModule) moduleObj;
        PyObject spec = module.__findattr__("__spec__");
        PyObject origin = spec.__findattr__("origin");
        String filename = origin.toString();
        String name = spec.__findattr__("name").toString();
        byte[] bytes;
        InputStream data = cl.getResourceAsStream(filename);
        if (data == null) {
            throw Py.ImportError(name + " resource not found");
        }
        if (filename.endsWith(".class")) {
            try {
                bytes = data.readAllBytes();
            } catch (IOException e) {
                throw Py.IOError(e);
            }
        } else {
            bytes = Import.compileSource(name, data, filename);
        }
        PyCode code = BytecodeLoader.makeCode(name + Version.PY_CACHE_TAG, bytes, filename);
        return Py.runCode(code, module.__dict__, module.__dict__);
    }

    private URL getResource(String name) {
        return cl.getResource("Lib/" + name);
    }
}
