/* Copyright (c) Jython Developers */
package org.python.modules.itertools;

import org.python.core.ArgParser;
import org.python.core.BuiltinDocs;
import org.python.core.PyIterator;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.Visitproc;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;

@ExposedType(name = "itertools.filterfalse", base = PyObject.class,
    doc = BuiltinDocs.itertools_filterfalse_doc)
public class filterfalse extends PyIterator {
    public static final PyType TYPE = PyType.fromClass(filterfalse.class);
    private PyIterator iter;

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

    /**
     * Creates an iterator that returns the items of the iterable for which
     * <code>predicate(item)</code> is <code>false</code>. If <code>predicate</code> is null
     * (None) return the items that are false.
     */
    @ExposedNew
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

    @Override
    @ExposedMethod(names = "__iter__")
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod(names = "__next__")
    @Override
    public PyObject __next__() {
        return doNext(iter.__next__());
    }

    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        int retVal = super.traverse(visit, arg);
        if (retVal != 0) {
            return retVal;
        }
        return iter != null ? visit.visit(iter, arg) : 0;
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        return ob != null && (iter == ob || super.refersDirectlyTo(ob));
    }
}
