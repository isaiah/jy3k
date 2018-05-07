/* Copyright (c) Jython Developers */
package org.python.modules.itertools;

import org.python.annotations.ExposedSlot;
import org.python.annotations.SlotFunc;
import org.python.core.ArgParser;
import org.python.core.BuiltinDocs;
import org.python.core.PyIterator;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.Visitproc;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;

@ExposedType(name = "itertools.filterfalse", base = PyObject.class,
    doc = BuiltinDocs.itertools_filterfalse_doc)
public class filterfalse extends PyObject {
    public static final PyType TYPE = PyType.fromClass(filterfalse.class);
    private ItertoolsIterator iter;

    public filterfalse() {
        super(TYPE);
    }

    public filterfalse(PyType subType) {
        super(subType);
    }

    public filterfalse(PyObject predicate, PyObject iterable) {
        super(TYPE);
        filterfalse___init__(predicate, iterable);
    }

    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject filterfalse_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        return new filterfalse(subtype);
    }

    /**
     * Creates an iterator that returns the items of the iterable for which
     * <code>predicate(item)</code> is <code>false</code>. If <code>predicate</code> is null
     * (None) return the items that are false.
     */
    @ExposedMethod
    final void filterfalse___init__(PyObject[] args, String[] kwds) {
        ArgParser ap = new ArgParser("filter", args, kwds, new String[] {"predicate", "iterable"}, 2);
        ap.noKeywords();
        PyObject predicate = ap.getPyObject(0);
        PyObject iterable = ap.getPyObject(1);
        filterfalse___init__(predicate, iterable);
    }

    public void filterfalse___init__(PyObject predicate, PyObject iterable) {
        iter = new itertools.FilterIterator(predicate, iterable, false);
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod(names = "__next__")
    public PyObject filterfalse___next__() {
        return iter.next();
    }
}
