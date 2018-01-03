package org.python.core;

import org.python.annotations.ExposedClassMethod;
import org.python.annotations.ExposedType;

@ExposedType(name = "builtins.classpath_finder")
public class PyClasspathFinder extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyClasspathFinder.class);

    @ExposedClassMethod(defaults = {"null"})
    public static PyObject find_spec(PyType subtype, PyObject fullname, PyObject path, PyObject target) {
        PySystemState interp = Py.getSystemState();
        if (path == null || path == Py.None) {
            return Py.None;
        }
        String pn = path.toString();
        if (pn.startsWith(PyClasspathLoader.IMPORT_PATH_ENTRY)) {
            return interp.importlib.invoke("spec_from_loader", fullname, new PyClasspathLoader());
        }
        return Py.None;
    }


}
