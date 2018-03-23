package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;
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

    @ExposedSlot(SlotFunc.ITER)
    public static PyObject __iter__(PyObject self) {
        return self;
    }

    @ExposedSlot(SlotFunc.ITER_NEXT)
    public static PyObject iterator___next__(PyObject obj, ThreadState ts) {
        PySeqIterator self = (PySeqIterator) obj;
        if (self.seq == null) {
            throw Py.StopIteration();
        }
        try {
            if (self.getitemFunc == null) {
                self.getitemFunc = getitem.getGetter().invokeExact(self.seq);
            }
            return (PyObject) getitem.getInvoker().invokeExact(self.getitemFunc, ts, (PyObject) new PyLong(self.index++));
        } catch (Throwable throwable) {
            self.seq = null;
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
