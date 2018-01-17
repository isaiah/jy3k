/* Copyright (c) Jython Developers */
package org.python.modules._functools;

import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;

/**
 * The Python _functools module.
 */
@ExposedModule(doc = BuiltinDocs._functools_doc)
public class _functools {

    @ModuleInit
    public static void init(PyObject dict) {
        dict.__setitem__("partial", PyPartial.TYPE);
    }

    @ExposedFunction(defaults = {"null"}, doc = BuiltinDocs._functools_reduce_doc)
    public static PyObject reduce(PyObject f, PyObject l, PyObject z) {
        PyObject result = z;
        PyObject iter = Py.iter(l, "reduce() arg 2 must support iteration");
        PyObject item;
        try {
            for (; ; ) {
                item = PyObject.iterNext(iter);
                if (result == null) {
                    result = item;
                } else {
                    result = f.__call__(result, item);
                }
            }
        } catch (PyException e) {
            if (!e.match(Py.StopIteration)) {
                throw e;
            }
        }
        if (result == null) {
            throw Py.TypeError("reduce of empty sequence with no initial value");
        }
        return result;
    }
}
