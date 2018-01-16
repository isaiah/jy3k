/* Copyright (c) Jython Developers */
package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;
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

    @ExposedMethod(doc = BuiltinDocs.range_iterator___next___doc)
    public PyObject range_iterator___next__() {
        if (index < len) {
            return new PyLong(start + index++ * step);
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
