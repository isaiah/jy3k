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

@ExposedType(name = "itertools.dropwhile", base = PyObject.class, doc = BuiltinDocs.itertools_dropwhile_doc)
public class dropwhile extends PyIterator {
    public static final PyType TYPE = PyType.fromClass(dropwhile.class);
    private PyIterator iter;

    public dropwhile() {
        super(TYPE);
    }

    public dropwhile(PyType subType) {
        super(subType);
    }

    public dropwhile(PyObject predicate, PyObject iterable) {
        super(TYPE);
        dropwhile___init__(predicate, iterable);
    }

    /**
     * Create an iterator that drops items from the iterable while <code>predicate(item)</code>
     * equals true. After which every remaining item of the iterable is returned.
     */
    @ExposedNew
    @ExposedMethod
    final void dropwhile___init__(PyObject[] args, String[] kwds) {
        ArgParser ap = new ArgParser("dropwhile", args, kwds, new String[] {"predicate", "iterable"}, 2);
        ap.noKeywords();
        PyObject predicate = ap.getPyObject(0);
        PyObject iterable = ap.getPyObject(1);
        dropwhile___init__(predicate, iterable);
    }

    private void dropwhile___init__(PyObject predicate, PyObject iterable) {
        iter = new itertools.WhileIterator(predicate, iterable, true);
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
