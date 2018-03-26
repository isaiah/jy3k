package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;

import java.math.BigInteger;

/**
 * range iterator for number big than Long.MAX_VALUE
 */
@ExposedType(name = "longrange_iterator", base = PyObject.class, isBaseType = false)
public class PyLongRangeIter extends PyObject {
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

    @ExposedSlot(SlotFunc.ITER)
    public static PyObject iter(PyObject self) {
        return self;
    }

    @ExposedSlot(SlotFunc.ITER_NEXT)
    public static PyObject range_iterator___next__(PyObject obj) {
        PyLongRangeIter self = (PyLongRangeIter) obj;
        if (self.index.compareTo(self.len) < 0) {
            self.curr = self.curr.add(self.step);
            self.index.add(BigInteger.ONE);
            return new PyLong(self.curr);
        }
        throw Py.StopIteration();
    }

    @ExposedMethod
    public int __len__() {
        return len.intValue();
    }
}
