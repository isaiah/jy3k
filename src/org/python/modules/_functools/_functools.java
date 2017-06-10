/* Copyright (c) Jython Developers */
package org.python.modules._functools;

import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyBytes;
import org.python.core.PyObject;
import org.python.expose.ExposedFunction;
import org.python.expose.ExposedModule;
import org.python.expose.ModuleInit;

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

        for (PyObject item; (item = iter.__next__()) != null;) {
            if (result == null) {
                result = item;
            } else {
                result = f.__call__(result, item);
            }
        }
        if (result == null) {
            throw Py.TypeError("reduce of empty sequence with no initial value");
        }
        return result;
    }
}
