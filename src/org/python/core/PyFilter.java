package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;

import java.util.Iterator;

/**
 * The type behind builtins.filter
 */
@ExposedType(name = "filter", doc = BuiltinDocs.builtins_filter_doc)
public class PyFilter extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyFilter.class);

    private PyObject func;
    private Iterator<PyObject> seq;

    public PyFilter() {
        super(TYPE);
    }

    @ExposedNew
    public static PyObject filter___new__(PyNewWrapper new_, boolean init, PyType subtype,
                                                PyObject[] args, String[] keywords) {
        if (args.length != 2) {
            throw Py.TypeError(String.format("filter expected 2 arguments, got %d", args.length));
        }
        PyFilter filter = new PyFilter();
        filter.func = args[0];
        filter.seq = args[1].asIterable().iterator();
        return filter;
    }

    @Override
    public PyObject __next__() {
        return filter___next__();
    }

    @ExposedMethod
    public PyObject filter___next__() {
        while(seq.hasNext()) {
            PyObject item = seq.next();
            if (func == PyBoolean.TYPE || func == Py.None) {
                if (!item.__bool__()) {
                    continue;
                }
            } else if (!func.__call__(item).__bool__()) {
                continue;
            }
            return item;
        }
        throw Py.StopIteration();
    }

    @Override
    public PyObject __iter__() {
        return filter___iter__();
    }

    @ExposedMethod
    public PyObject filter___iter__() {
        return this;
    }
}
