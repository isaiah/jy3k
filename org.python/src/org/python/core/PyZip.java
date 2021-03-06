package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;

/**
 * type for builtins.zip
 */
@ExposedType(name = "zip", doc = BuiltinDocs.builtins_zip_doc)
public class PyZip extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyZip.class);
    private PyObject[] iters;

    public PyZip() {
        super(TYPE);
    }

    @ExposedNew
    public static PyObject zip___new__(PyNewWrapper new_, boolean init, PyType subtype,
                                                PyObject[] args, String[] keywords) {
        int itemsize = args.length;
        PyZip zip = new PyZip();
        zip.iters = new PyObject[itemsize];

        for (int j = 0; j < itemsize; j++) {
            PyObject iter = PyObject.getIter(args[j]);
            if (iter == null) {
                throw Py.TypeError("zip argument #" + (j + 1) + " must support iteration");
            }
            zip.iters[j] = iter;
        }

        return zip;
    }

    @ExposedMethod
    public PyObject zip___next__() {
        if (iters.length == 0) {
            throw Py.StopIteration();
        }
        PyObject[] next = new PyObject[iters.length];
        int j = 0;
        for (PyObject iter: iters) {
            next[j] = PyObject.iterNext(iter);
            if (next[j++] == null) {
                throw Py.StopIteration();
            }
        }
        return new PyTuple(next);
    }

    @ExposedMethod
    public PyObject zip___iter__() {
        return this;
    }
}
