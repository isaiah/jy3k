package org.python.modules.itertools;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyLong;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;

@ExposedType(name = "itertools._tee", base = PyObject.class,
    isBaseType = false, doc = BuiltinDocs.itertools_tee_doc)
public class PyTeeIterator extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyTeeIterator.class);
    public PyException stopException;

    private int position;
    private PyTeeData teeData;

    public PyTeeIterator(PyType subType) {
        super(subType);
    }

    public PyTeeIterator(PyTeeData teeData) {
        super(TYPE);
        this.teeData = teeData;
    }
    
    @ExposedNew
    final static PyObject tee___new__ (PyNewWrapper new_, boolean init,
            PyType subtype, PyObject[] args, String[] keywords) {
        final int nargs = args.length;
        // CPython tee ignores keywords, so we do too!
        if (nargs < 1 || nargs > 1) {
            throw Py.TypeError("tee expected 1 arguments, got " + nargs);
        }
        return fromIterable(args[0]);
    }

    public static PyObject[] makeTees(PyObject iterable, int n) {
        if (n < 0) {
            throw Py.ValueError("n must be >= 0");
        }

        PyObject[] tees = new PyObject[n];

        if (n == 0) {
            return tees;
        }

        PyTeeIterator iter;
        if (iterable instanceof PyTeeIterator) {
            iter = (PyTeeIterator) iterable;
        } else {
            iter = fromIterable(iterable);
        }
        tees[0] = iter;
        for (int i = 1; i < n; i++) {
            tees[i] = iter.tee___copy__();
        }
        return tees;
    }

    private static PyTeeIterator fromIterable(PyObject iterable) {
        if (iterable instanceof PyTeeIterator) {
            return ((PyTeeIterator) iterable).tee___copy__();
        }
        PyObject iterator = getIter(iterable);
        PyTeeData teeData = new PyTeeData(iterator);
        return new PyTeeIterator(teeData);
    }

    @ExposedMethod(names = "__next__")
    public PyObject tee___next__() {
        return teeData.getItem(position++);
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod(names = {"__copy__"})
    public final PyTeeIterator tee___copy__() {
        return new PyTeeIterator(teeData);
    }

    @ExposedMethod(names = {"__reduce__"})
    public PyObject reduce() {
        return new PyTuple(TYPE, new PyTuple(teeData), new PyTuple(new PyLong(position)));
    }

    @ExposedMethod(names = {"__setstate__"})
    public void setstate(PyObject pos) {
        position = pos.asInt();
    }
}
