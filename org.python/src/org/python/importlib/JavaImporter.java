package org.python.importlib;

import jnr.posix.util.Platform;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.core.ArgParser;
import org.python.core.Py;
import org.python.core.PyJavaPackage;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.core.Untraversable;

/**
 * Load Java classes.
 */
@Untraversable
@ExposedType(name = "JavaImporter")
public class JavaImporter extends PyObject {
    public static final PyType TYPE = PyType.fromClass(JavaImporter.class);

    /**
     * Make sure the path an absolute path, because site.py will try to convert them, which will break the link between
     * the path and the {@link JavaImporter}
     */
    public static final String JAVA_IMPORT_PATH_ENTRY = (Platform.IS_WINDOWS ? "C:\\" : "/") + "__classpath__";

    public JavaImporter(PyType objtype) {
        super(objtype);
    }

    public JavaImporter() {
        super(TYPE);
    }

    @ExposedMethod
    public boolean is_package(String fullname) {
        PyObject ret = lookupName(fullname);
        return ret != null && ret instanceof PyJavaPackage;
    }

    @ExposedMethod
    public String get_filename(String fullname) {
        if (lookupName(fullname) != null) {
            return JAVA_IMPORT_PATH_ENTRY;
        }
        throw Py.ImportError("java package not found", fullname);
    }

    /**
     * Find the module for the fully qualified name.
     *
     * @param name the fully qualified name of the module
     * @param path if installed on the meta-path None or a module path
     * @return a loader instance if this importer can load the module, None
     *         otherwise
     */
    @ExposedMethod(defaults = {"null"})
    public PyObject JavaImporter_find_module(String name, PyObject path) {
        Py.writeDebug("import", "trying " + name
                + " in packagemanager for path " + path);
        PyObject ret = PySystemState.packageManager.lookupName(name.intern());
        if (ret != null) {
            Py.writeComment("import", "'" + name + "' as java package");
            return this;
        }
        return Py.None;
    }

    @ExposedMethod
    public PyObject JavaImporter_load_module(String name) {
        return lookupName(name);
    }

    public static final PyObject lookupName(String name) {
        PyObject ret = PySystemState.packageManager.lookupName(name.intern());
        if (ret == null && name.startsWith("java.")) {
            ret = PySystemState.packageManager.lookupName(name.replace("java.", "").intern());
        }
        if (ret != null && ret instanceof PyJavaPackage) {
            ret.__setattr__("__path__", new PyUnicode(JAVA_IMPORT_PATH_ENTRY));
        }
        if (ret != null) {
            Py.getSystemState().modules.__setitem__(name, ret);
        }
        return ret;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return String.format("<%s object>", getType().fastGetName());
    }
}
