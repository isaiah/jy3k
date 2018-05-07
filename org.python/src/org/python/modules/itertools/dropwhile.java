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

@ExposedType(name = "itertools.dropwhile", base = PyObject.class, doc = BuiltinDocs.itertools_dropwhile_doc)
public class dropwhile extends PyObject {
    public static final PyType TYPE = PyType.fromClass(dropwhile.class);
    private ItertoolsIterator iter;

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
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject dropwhile_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        return new dropwhile(subtype);
    }

    @ExposedMethod
    final void dropwhile___init__(PyObject[] args, String[] kwds) {
        ArgParser ap = new ArgParser("dropwhile", args, kwds, new String[]{"predicate", "iterable"}, 2);
        ap.noKeywords();
        PyObject predicate = ap.getPyObject(0);
        PyObject iterable = ap.getPyObject(1);
        dropwhile___init__(predicate, iterable);
    }

    private void dropwhile___init__(PyObject predicate, PyObject iterable) {
        iter = new itertools.WhileIterator(predicate, iterable, true);
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod(names = "__next__")
    public PyObject dropwhile__next__() {
        return iter.next();
    }
}
