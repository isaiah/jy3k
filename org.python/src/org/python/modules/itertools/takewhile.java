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

@ExposedType(name = "itertools.takewhile", base = PyObject.class, doc = BuiltinDocs.itertools_takewhile_doc)
public class takewhile extends PyIterator {
    public static final PyType TYPE = PyType.fromClass(takewhile.class);
    private ItertoolsIterator iter;

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

    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject takewhile_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        return new takewhile(subtype);
    }

    /**
     * Create an iterator that returns items from the iterable while <code>predicate(item)</code>
     * is true. After which iteration is stopped.
     */
    @ExposedMethod
    final void takewhile___init__(PyObject[] args, String[] kwds) {
        ArgParser ap = new ArgParser("takewhile", args, kwds, new String[]{"predicate", "iterable"}, 2);
        ap.noKeywords();
        PyObject predicate = ap.getPyObject(0);
        PyObject iterable = ap.getPyObject(1);
        takewhile___init__(predicate, iterable);
    }

    private void takewhile___init__(PyObject predicate, PyObject iterable) {
        iter = new itertools.WhileIterator(predicate, iterable, false);
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod(names = "__next__")
    public PyObject takewhile___next__() {
        return iter.next();
    }
}

