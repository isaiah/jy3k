/* Copyright (c) Jython Developers */
package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;

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

    @Override
    @ExposedMethod(names = "__iter__")
    public PyObject __iter__() {
        return this;
    }

    @Override
    @ExposedMethod(doc = BuiltinDocs.range_iterator___next___doc)
    public PyObject __next__() {
        if (index < len) {
            return new PyLong(start + index++ * step);
        }

        throw Py.StopIteration();
    }

    @Override
    @ExposedMethod(names = "__length_hint__")
    public int __len__() {
        return (int) len;
    }
}
