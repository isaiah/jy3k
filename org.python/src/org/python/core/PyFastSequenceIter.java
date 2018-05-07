/* Copyright (c) Jython Developers */
package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;
import org.python.bootstrap.Import;

/**
 * Sequence iterator specialized for accessing the underlying sequence directly.
 */
@ExposedType(name = "fastsequenceiterator", base = PyObject.class, isBaseType = false)
public class PyFastSequenceIter extends PyObject {
    //note: Already implements Traverseproc, inheriting it from PyIterator

    public static final PyType TYPE = PyType.fromClass(PyFastSequenceIter.class);

    private PySequence seq;
    public int index;

    public PyFastSequenceIter(PyType subtype, PyObject seq) {
        super(subtype);
        this.seq = (PySequence) seq;
        index = 0;
    }

    public PyFastSequenceIter(PyType subtype) {
        super(subtype);
    }

    public PyFastSequenceIter(PySequence seq) {
        super(TYPE);
        this.seq = seq;
        index = 0;
    }

    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject fastsequenceiterator_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        return new PyFastSequenceIter(subtype);
    }

    @ExposedMethod(names = {"__init__"})
    public void __init__(PyObject seq) {
        if (seq == Py.None) {
            this.seq = null;
        } else {
            this.seq = (PySequence) seq;
        }
    }

    @ExposedMethod(doc = BuiltinDocs.list_iterator___iter___doc)
    public final PyObject fastsequenceiterator___iter__() {
        return this;
    }

    @ExposedMethod(doc = BuiltinDocs.list_iterator___next___doc)
    public final PyObject fastsequenceiterator___next__() {
        if (seq == null) {
            throw Py.StopIteration();
        }

        PyObject result = seq.seq___finditem__(index++);
        if (result == null) {
            seq = null;
            throw Py.StopIteration();
        }
        return result;
    }

    @ExposedMethod
    public int __len__() {
        return seq.__len__();
    }

    @ExposedMethod
    public PyObject __reduce__() {
        PyObject builtins = Import.importModule("builtins");
        return new PyTuple(builtins.__findattr__("iter"), new PyTuple(seq), new PyLong(index));
    }

    @ExposedMethod
    public void __setstate__(PyObject index) {
        this.index = index.asInt();
    }
}
