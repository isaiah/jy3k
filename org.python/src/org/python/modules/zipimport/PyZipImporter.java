package org.python.modules.zipimport;

import org.python.Version;
import org.python.annotations.ExposedClassMethod;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.bootstrap.Import;
import org.python.core.ArgParser;
import org.python.core.BytecodeLoader;
import org.python.core.Py;
import org.python.core.PyCode;
import org.python.core.PyException;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.core.PyType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@ExposedType(name = "zipimport.zipimporter")
public class PyZipImporter extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyZipImporter.class);
    @ExposedGet
    public String archive;

    @ExposedGet
    public String prefix;

    private final Path root;
    private final FileSystem zipfs;

    public PyZipImporter(PyType objtype, String archive, String prefix) {
        super(objtype);
        this.archive = archive;
        this.prefix = prefix;
        try {
            this.zipfs = FileSystems.newFileSystem(Paths.get(archive), this.getClass().getClassLoader());
            this.root = zipfs.getPath(prefix);
        } catch (IOException e) {
            throw new PyException(ZipImportModule.ZipImportError, e.getMessage());
        };
    }

    @ExposedNew
    public static PyObject _new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("__init__", args, keywords, "archivepath");
        String archivePath = ap.getString(0);
        String filename = archivePath;
        int len = filename.length();
        int flen = len;
        for (;;) {
            Path p = Paths.get(filename);
            if (Files.exists(p) && Files.isRegularFile(p)) {
                break;
            }
            filename = null;
            flen = archivePath.lastIndexOf(File.separatorChar, flen - 1);
            if (flen == -1) {
                break;
            }
            filename = archivePath.substring(0, flen);
        }
        if (filename == null) {
            throw new PyException(ZipImportModule.ZipImportError, "not a zip file");
        }
        String prefix;
        if (flen < len) {
            prefix = archivePath.substring(flen);
        } else {
            prefix = "";
        }

        return new PyZipImporter(subtype, filename, prefix);
    }

    @ExposedMethod
    public boolean is_package(String fullname) {
        String sep = zipfs.getSeparator();
        String name = fullname.replaceAll("\\.", sep);
        return isPackage(name);
    }

    private boolean isPackage(String pn) {
        Path path = root.resolve(pn);
        return Files.exists(path) && Files.isDirectory(path);
    }

    @ExposedMethod
    public String get_filename(String fullname) {
        String sep = zipfs.getSeparator();
        String pkg = fullname.replaceAll("\\.", sep);
        // try package
        if (isPackage(pkg)) {
            String classFile = pkg + sep + Import.CACHEDIR + sep + "__init__." + Version.PY_CACHE_TAG + ".class";
            Path initClassPath = root.resolve(classFile);
            if (Files.exists(initClassPath)) {
                return initClassPath.toAbsolutePath().toString();
            }
            Path initPath = root.resolve(pkg + sep + "__init__.py");
            if (Files.exists(initPath)) {
                return initPath.toAbsolutePath().toString();
            }
            throw Py.ImportError("found a package, but not __init__ file");
        }
        String classFile;
        int base = pkg.lastIndexOf(sep);
        if (base > -1) {
            classFile = pkg.substring(0, base) + Import.CACHEDIR + sep + pkg.substring(base + 1) + "." + Version.PY_CACHE_TAG + ".class";
        } else {
            classFile = Import.CACHEDIR + sep + pkg + "." + Version.PY_CACHE_TAG + ".class";
        }
        Path classPath = root.resolve(classFile);
        if (Files.exists(classPath)) {
            return classPath.toAbsolutePath().toString();
        }
        Path pyPath = root.resolve(pkg + ".py");
        if (Files.exists(pyPath)) {
            return pyPath.toAbsolutePath().toString();
        }
        throw Py.ImportError(pyPath.toString() + "not found");
    }

    @ExposedClassMethod(defaults = {"null"})
    public static PyObject find_spec(PyType subtype, PyObject fullname, PyObject path, PyObject target) {
        PySystemState interp = Py.getSystemState();
        // FIXME get the archive and prefix from the path, the origin of the spec have to include the whole url
        return interp.importlib.invoke("spec_from_loader", fullname, new PyZipImporter(subtype,"", ""));
    }

    @ExposedMethod
    public PyObject exec_module(PyObject module) {
        PyObject spec = module.__findattr__("__spec__");
        PyObject origin = spec.__findattr__("origin");
        String filename = origin.toString();
        String name = spec.__findattr__("name").toString();
        byte[] bytes;
        try {
            InputStream inputStream = Files.newInputStream(zipfs.getPath(filename));
            if (filename.endsWith(".class")) {
                bytes = inputStream.readAllBytes();
            } else {
                bytes = Import.compileSource(name, inputStream, filename);
            }
        } catch (IOException e) {
            throw Py.ImportError(e.getMessage());
        }
        PyCode code = BytecodeLoader.makeCode(name + Version.PY_CACHE_TAG, bytes, filename);
        return Py.runCode(code, null, null);
    }

    @ExposedMethod
    public PyObject create_module(PyObject spec) {
        /* use default module creation */
        return Py.None;
    }
}
