// Copyright (c) Corporation for National Research Initiatives
package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;

import java.math.BigInteger;

/**
 * The builtin range type.
 */
@Untraversable
@ExposedType(name = "range", base = PyObject.class, isBaseType = false,
             doc = BuiltinDocs.range_doc)
public class PyRange extends PySequence {

    public static final PyType TYPE = PyType.fromClass(PyRange.class);

    private final BigInteger start;
    private final BigInteger step;
    private final BigInteger stop;
    private final BigInteger len;
    private final boolean extraLong;

    public PyRange(long ihigh) {
        this(0, ihigh, 1);
    }

    public PyRange(long ilow, long ihigh) {
        this(ilow, ihigh, 1);
    }

    public PyRange(long ilow, long ihigh, int istep) {
        super(TYPE);

        if (istep == 0) {
            throw Py.ValueError("range() arg 3 must not be zero");
        }

        int n;
        long listep = istep;
        if (listep > 0) {
            n = getLenOfRange(ilow, ihigh, listep);
        } else {
            n = getLenOfRange(ihigh, ilow, -listep);
        }
        if (n < 0) {
            throw Py.OverflowError("range() result has too many items");
        }
        start = BigInteger.valueOf(ilow);
        len = BigInteger.valueOf(n);
        step = BigInteger.valueOf(istep);
        stop = BigInteger.valueOf(ihigh);
        extraLong = false;
    }

    public PyRange(PyType type, BigInteger ilow, BigInteger ihigh, BigInteger istep) {
        super(type);
        BigInteger n;
        BigInteger listep = istep;
        if (listep.compareTo(BigInteger.ZERO) > 0) {
            n = ihigh.subtract(ilow).divide(istep);
        } else {
            n = ilow.subtract(ihigh).divide(istep);
        }
        start = ilow;
        len = n.add(BigInteger.ONE);
        step = istep;
        stop = ihigh;
        extraLong = true;
    }

    @ExposedNew
    static final PyObject range___new__(PyNewWrapper new_, boolean init, PyType subtype,
                                         PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("range", args, keywords,
                                     new String[] {"ilow", "ihigh", "istep"}, 1);
        ap.noKeywords();

        long ilow = 0;
        long ihigh;
        int istep = 1;
        if (args.length == 1) {
            PyObject highObj = args[0];
            if (highObj instanceof PyLong && ((PyLong) highObj).isOverflow()) {
                return new PyRange(subtype, BigInteger.ZERO, ((PyLong) highObj).getValue(), BigInteger.ONE);
            }
            ihigh = ap.getLong(0);
        } else {
            try {
                ilow = ap.getLong(0);
                ihigh = ap.getLong(1);
                istep = ap.getInt(2, 1);
                if (istep == 0) {
                    throw Py.ValueError("range() arg 3 must not be zero");
                }
            } catch (PyException e) {
                if (e.match(Py.OverflowError)) {
                    return new PyRange(subtype, ((PyLong) args[0]).getValue(), ((PyLong) args[1]).getValue(), ((PyLong) args[2]).getValue());
                }
                throw e;
            }
        }
        return new PyRange(ilow, ihigh, istep);
    }

    /**
     * Return number of items in range (lo, hi, step).  step > 0 required.  Return
     * a value < 0 if & only if the true value is too large to fit in a Java int.
     *
     * @param lo int value
     * @param hi int value
     * @param step int value (> 0)
     * @return int length of range
     */
    static int getLenOfRange(long lo, long hi, long step) {
        if (lo < hi) {
            // the base difference may be > Integer.MAX_VALUE
            long diff = hi - lo - 1;
            // any long > Integer.MAX_VALUE or < Integer.MIN_VALUE gets cast to a
            // negative number
            return (int)((diff / step) + 1);
        } else {
            return 0;
        }
    }

    @Override
    public int __len__() {
        return range___len__();
    }

    @ExposedMethod(doc = BuiltinDocs.range___len___doc)
    final int range___len__() {
        return len.intValue();
    }

    @Override
    public PyObject __getitem__(PyObject index) {
        return range___getitem__(index);
    }

    @ExposedMethod(doc = BuiltinDocs.range___getitem___doc)
    final PyObject range___getitem__(PyObject index) {
        PyObject ret = seq___finditem__(index);
        if (ret == null) {
            throw Py.IndexError("range object index out of range");
        }
        return ret;
    }

    @ExposedMethod(doc = BuiltinDocs.range___iter___doc)
    public PyObject range___iter__() {
        return range_iter();
    }

    @ExposedMethod(doc = BuiltinDocs.range___reversed___doc)
    public PyObject range___reversed__() {
        return range_reverse();
    }

    private final PyObject range_iter() {
        if (extraLong) {
            return new PyLongRangeIter(BigInteger.ZERO, start, step, len);
        }
        return new PyXRangeIter(0, start.longValue(), step.longValue(), len.longValue());
    }

    private final PyObject range_reverse() {
        if (extraLong) {
            return new PyLongRangeIter(BigInteger.ZERO,
                    start.add(len.subtract(BigInteger.ONE)).multiply(step),   // start
                    step.negate(),                                            // step (negative value)
                    len);
        }

        long lstart = start.longValue();
        long lstep = step.longValue();
        long llen = len.longValue();
        return new PyXRangeIter(0,
                (lstart + (llen - 1) * lstep),   // start
                (0 - lstep),                   // step (negative value)
                llen);
    }

    @ExposedMethod
    public PyObject range___reduce__() {
        return new PyTuple(getType(),
                new PyTuple(new PyLong(start), new PyLong(stop), new PyLong(step)));
    }

    @ExposedSlot(SlotFunc.CONTAINS)
    public static boolean contains(PyObject self, PyObject other) {
        if (other instanceof PyLong) {
            return containsLong((PyRange) self, (PyLong) other);
        }
        return Abstract._PySequence_Stream(self).anyMatch(other::equals);
    }

    private static boolean containsLong(PyRange self, PyLong value) {
        BigInteger v = value.getValue();
        int cmp1 = self.step.compareTo(BigInteger.ZERO);
        int cmp2 = self.start.compareTo(v);
        int cmp3 = self.stop.compareTo(v);
        if (cmp1 >= 0) { /* positive steps: start <= value < stop */
            if (cmp2 > 0 || cmp3 <= 0) {
                return false;
            }
        } else {
            if (cmp2 <= 0 || cmp3 > 0) {
                return false;
            }
        }
        return v.subtract(self.start).remainder(self.step).compareTo(BigInteger.ZERO) == 0;
    }

    @Override
    public PyObject __reduce__() {
        return range___reduce__();
    }

    @Override
    protected PyObject pyget(int i) {
        return new PyLong(start.add(step.multiply(BigInteger.valueOf(i))));
    }

    @Override
    public PyObject getslice(int start, int stop, int step) {
        return new PyRange(TYPE, computeItem(start), computeItem(stop), this.step.multiply(BigInteger.valueOf(step)));
    }

    private BigInteger computeItem(int i) {
        BigInteger incr = step.multiply(BigInteger.valueOf(i));
        return incr.add(start);
    }

    @Override
    protected PyObject repeat(int howmany) {
        // not supported
        return null;
    }

    @Override
    protected String unsupportedopMessage(String op, PyObject o2) {
        // always return the default unsupported messages instead of PySequence's
        return null;
    }

    @Override
    public String toString() {
        if (start == BigInteger.ZERO && step == BigInteger.ONE) {
            return String.format("range(%d)", stop);
        } else if (step == BigInteger.ONE) {
            return String.format("range(%d, %d)", start, stop);
        } else {
            return String.format("range(%d, %d, %d)", start, stop, step);
        }
    }

//    @Override
//    public Object __tojava__(Class<?> c) {
//        if (c.isAssignableFrom(Iterable.class)) {
//            return new JavaIterator(range_iter());
//        }
//        if (c.isAssignableFrom(Iterator.class)) {
//            return (new JavaIterator(range_iter())).iterator();
//        }
//        if (c.isAssignableFrom(Collection.class)) {
//            List<Object> list = new ArrayList<>();
//            for (Object obj : new JavaIterator(range_iter())) {
//                list.add(obj);
//            }
//            return list;
//        }
//        return super.__tojava__(c);
//    }
}
