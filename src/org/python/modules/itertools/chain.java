/* Copyright (c) 2012 Jython Developers */
package org.python.modules.itertools;

import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyIter;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.expose.ExposedClassMethod;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedNew;
import org.python.expose.ExposedType;

import java.util.Arrays;

@ExposedType(name = "itertools.chain", base = PyObject.class, doc = BuiltinDocs.chain_doc)
public class chain extends PyObject {

    public static final PyType TYPE = PyType.fromClass(chain.class);
    private PyObject source;
    private PyObject active;

    public chain() {
        super(TYPE);
    }

    public chain(PyType subType) {
        super(subType);
    }

    public chain(PyObject iterable) {
        super(TYPE);
        source = iterable;
    }

    @ExposedClassMethod
    public static final PyObject from_iterable(PyType type, PyObject iterable) {
        return new chain(iterable);
    }

    /**
     * Creates an iterator that iterates over a <i>chain</i> of iterables.
     */
    @ExposedNew
    @ExposedMethod
    final void chain___init__(final PyObject[] args, String[] kwds) {
        source = new PyIter(Arrays.asList(args));
    }

    @Override
    @ExposedMethod(names = "__iter__", doc = BuiltinDocs.chain___iter___doc)
    public PyObject __iter__() {
        return this;
    }

    @Override
    public PyObject __next__() {
        return chain___next__();
    }

    @ExposedMethod(doc = BuiltinDocs.chain___next___doc)
    final PyObject chain___next__() {
        if (source == null) {
            throw Py.StopIteration();
        }
        if (active == null) {
            PyObject iterable = source.__next__();
            if (iterable == null) {
                source = null;
                throw Py.StopIteration();
            }
            active = iterable.__iter__();
            if (active == null) {
                source = null;
                throw Py.StopIteration();
            }
        }
        try {
            PyObject item = active.__next__();
            if (item != null) {
                return item;
            }
        } catch (PyException e) {
            if (!e.match(Py.StopIteration)) {
                throw e;
            }
        }
        active = null;
        return chain___next__();
    }
}
