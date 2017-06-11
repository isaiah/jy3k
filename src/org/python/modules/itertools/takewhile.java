/* Copyright (c) Jython Developers */
package org.python.modules.itertools;

import org.python.core.ArgParser;
import org.python.core.BuiltinDocs;
import org.python.core.PyIterator;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.Visitproc;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedNew;
import org.python.expose.ExposedType;

@ExposedType(name = "itertools.takewhile", base = PyObject.class, doc = BuiltinDocs.itertools_takewhile_doc)
public class takewhile extends PyIterator {
    public static final PyType TYPE = PyType.fromClass(takewhile.class);
    private PyIterator iter;

    public takewhile() {
        super(TYPE);
    }

    public takewhile(PyType subType) {
        super(subType);
    }

    public takewhile(PyObject predicate, PyObject iterable) {
        super(TYPE);
        takewhile___init__(predicate, iterable);
    }

    /**
     * Create an iterator that returns items from the iterable while <code>predicate(item)</code>
     * is true. After which iteration is stopped.
     */
    @ExposedNew
    @ExposedMethod
    final void takewhile___init__(PyObject[] args, String[] kwds) {
        ArgParser ap = new ArgParser("takewhile", args, kwds, new String[] {"predicate", "iterable"}, 2);
        ap.noKeywords();
        PyObject predicate = ap.getPyObject(0);
        PyObject iterable = ap.getPyObject(1);
        takewhile___init__(predicate, iterable);
    }

    private void takewhile___init__(PyObject predicate, PyObject iterable) {
        iter = new itertools.WhileIterator(predicate, iterable, false);
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
