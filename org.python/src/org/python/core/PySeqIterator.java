package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;
import org.python.bootstrap.Import;

@ExposedType(name = "iterator")
public class PySeqIterator extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PySeqIterator.class);

    private int index;
    private Object getitemFunc;
    private PyObject seq;

    public PySeqIterator(PyObject seq, Object getitemFunc) {
        super(TYPE);
        index = 0;
        this.seq = seq;
        this.getitemFunc = getitemFunc;
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod
    public PyObject iterator___next__(ThreadState ts) {
        if (seq == null) {
            throw Py.StopIteration();
        }
        try {
            if (getitemFunc == null) {
                getitemFunc = getitem.getGetter().invokeExact(seq);
            }
            return (PyObject) getitem.getInvoker().invokeExact(getitemFunc, ts, (PyObject) new PyLong(index++));
        } catch (Throwable throwable) {
            seq = null;
            throw Py.StopIteration();
        }
    }

    @ExposedMethod
    public PyObject __reduce__() {
        PyObject builtins = Import.importModule("builtins");
        return new PyTuple(builtins.__findattr__("iter"), new PyTuple(seq), new PyLong(index));
    }

    @ExposedMethod
    public void __setstate__(int index) {
        this.index = index < 0 ? 0 : index;
    }
}
