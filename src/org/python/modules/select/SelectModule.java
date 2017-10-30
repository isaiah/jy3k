package org.python.modules.select;

import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;
import org.python.core.Py;
import org.python.core.PyObject;

@ExposedModule(name = "select")
public class SelectModule {

    @ExposedFunction(defaults={"null"})
    public static PyObject select(PyObject rlist, PyObject wlist, PyObject xlist, PyObject timeout) {
        return Py.None;
    }
}
