package org.python.importlib;

import org.python.annotations.ExposedClassMethod;
import org.python.annotations.ExposedType;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.core.PyType;

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
        if (pn.startsWith(JavaImporter.JAVA_IMPORT_PATH_ENTRY)) {
            return interp.importlib.invoke("spec_from_loader", fullname, new JavaImporter());
        }
        return Py.None;
    }


}
