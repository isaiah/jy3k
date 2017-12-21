package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;

/**
 * The type behind builtins.map
 */
@ExposedType(name = "map", doc = BuiltinDocs.builtins_map_doc)
public class PyMap extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyMap.class);

    private PyObject f;
    private int n;
    private PyObject[] iters;

    public PyMap() {
        super(TYPE);
    }

    @ExposedNew
    public static PyObject map___new__(PyNewWrapper new_, boolean init, PyType subtype,
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

    @Override
    public PyObject __next__() {
        return map___next__();
    }

    @ExposedMethod
    public PyObject map___next__() {
        PyObject[] args = new PyObject[n];
        for (int j = 0; j < n; j++) {
            args[j] = iters[j].__next__();
            if (args[j] == null) {
                throw Py.StopIteration();
            }
        }
        return f.__call__(args);
    }

    @Override
    public PyObject __iter__() {
        return map___iter__();
    }

    @ExposedMethod
    public PyObject map___iter__() {
        return this;
    }
}
