/* Copyright (c) Jython Developers */
package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;
import org.python.bootstrap.Import;

/**
 * Specially optimized xrange iterator.
 */
@ExposedType(name = "range_iterator", base = PyObject.class, isBaseType = false)
public class PyXRangeIter extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyXRangeIter.class);

    private long index;
    private long start;
    private long step;
    private long len;

    public PyXRangeIter(long index, long start, long step, long len) {
        super(TYPE);
        this.index = index;
        this.start = start;
        this.step = step;
        this.len = len;
    }

    @ExposedSlot(SlotFunc.ITER_NEXT)
    public static PyObject range_iterator___next__(PyObject iter) {
        PyXRangeIter self = (PyXRangeIter) iter;
        if (self.index < self.len) {
            return new PyLong(self.start + self.index++ * self.step);
        }

        throw Py.StopIteration();
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @Override
    @ExposedMethod(names = "__length_hint__")
    public int __len__() {
        return (int) len;
    }

    @ExposedMethod
    public PyObject __reduce__() {
        PyObject builtins = Import.importModule("builtins");
        PyObject range = new PyRange(start, start + len, (int) step);
        return new PyTuple(builtins.__findattr__("iter"), new PyTuple(range), new PyLong(index));
    }

    @ExposedMethod
    public void __setstate__(int index) {
        this.index = index;
    }
}
