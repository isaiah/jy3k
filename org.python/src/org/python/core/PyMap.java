package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;

/**
 * The type behind builtins.map
 */
@ExposedType(name = "map")
public class PyMap extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyMap.class);

    private PyObject f;
    private int n;
    private PyObject[] iters;

    public PyMap() {
        super(TYPE);
    }

    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject map_new(PyNewWrapper new_, boolean init, PyType subtype,
                                                PyObject[] argstar, String[] keywords) {
        if (argstar.length < 2) {
            throw Py.TypeError("map requires at least two arguments");
        }
        PyMap map = new PyMap();
        map.n = argstar.length - 1;
        map.f = argstar[0];
        map.iters = new PyObject[map.n];

        for (int j = 0; j < map.n; j++) {
            map.iters[j] = Py.iter(argstar[j + 1], "argument " + (j + 1)
                               + " to map() must support iteration");
        }

        return map;
    }

    @ExposedSlot(SlotFunc.ITER_NEXT)
    public static PyObject next(PyObject map) {
        PyMap self = (PyMap) map;
        PyObject[] args = new PyObject[self.n];
        for (int j = 0; j < self.n; j++) {
            args[j] = PyObject.iterNext(self.iters[j]);
        }
//        return Abstract.PyObject_Call(Py.getThreadState(), self.f, args);
        return self.f.__call__(Py.getThreadState(), args);
    }

    @ExposedMethod
    public PyObject map___iter__() {
        return this;
    }
}
