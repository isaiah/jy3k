package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;

import java.math.BigInteger;

/**
 * range iterator for number big than Long.MAX_VALUE
 */
@ExposedType(name = "longrange_iterator", base = PyObject.class, isBaseType = false)
public class PyLongRangeIter extends PyIterator {
    public static final PyType TYPE = PyType.fromClass(PyLongRangeIter.class);

    private BigInteger index;
    private BigInteger start;
    private BigInteger step;
    private BigInteger len;
    private BigInteger curr;

    public PyLongRangeIter(BigInteger index, BigInteger start, BigInteger step, BigInteger len) {
        super(TYPE);
        this.index = index;
        this.start = start;
        this.step = step;
        this.len = len;
        this.curr = start;
    }

    @ExposedMethod(doc = BuiltinDocs.range_iterator___next___doc)
    public final PyObject range_iterator___next__() {
        if (index.compareTo(len) < 0) {
            curr = curr.add(step);
            index.add(BigInteger.ONE);
            return new PyLong(curr);
        }
        throw Py.StopIteration();
    }

    @Override
    public int __len__() {
        return len.intValue();
    }
}
