// Copyright (c) Corporation for National Research Initiatives
// Copyright (c) Jython Developers

package org.python.core;

import org.python.core.stringlib.Encoding;
import org.python.core.stringlib.FloatFormatter;
import org.python.core.stringlib.IntegerFormatter;
import org.python.core.stringlib.InternalFormat;
import org.python.core.stringlib.InternalFormat.Formatter;
import org.python.core.stringlib.InternalFormat.Spec;
import org.python.annotations.ExposedClassMethod;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.expose.MethodType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * A builtin python int. This is implemented as a java.math.BigInteger.
 */
@Untraversable
@ExposedType(name = "int", doc = BuiltinDocs.int_doc)
public class PyLong extends PyObject {

    public static final PyType TYPE = PyType.fromClass(PyLong.class);

    public static final BigInteger MIN_INT = BigInteger.valueOf(Long.MIN_VALUE);
    public static final BigInteger MAX_INT = BigInteger.valueOf(Long.MAX_VALUE);
    public static final BigInteger MAX_ULONG = BigInteger.valueOf(1).shiftLeft(64)
            .subtract(BigInteger.valueOf(1));

    private final BigInteger value;

    public PyLong(PyType subType, int v) {
        this(subType, toBigInteger(v));
    }

    public PyLong(PyType subType, BigInteger v) {
        super(subType);
        value = v;
    }

    public PyLong(BigInteger v) {
        this(TYPE, v);
    }

    public PyLong(double v) {
        this(toBigInteger(v));
    }

    public PyLong(int v) {
        this(toBigInteger(v));
    }

    public PyLong(long v) {
        this(BigInteger.valueOf(v));
    }

    public PyLong(String s) {
        this(new BigInteger(s));
    }

    @ExposedNew
    public static PyObject int___new__(PyNewWrapper new_, boolean init, PyType subtype,
            PyObject[] args, String[] keywords) {
        if (new_.for_type != subtype) {
            return longSubtypeNew(new_, init, subtype, args, keywords);
        }

        ArgParser ap = new ArgParser("long", args, keywords, new String[] {"x", "base"}, 0);
        PyObject x = ap.getPyObject(0, null);
        if (x != null && x.getJavaProxy() instanceof BigInteger) {
            return new PyLong((BigInteger)x.getJavaProxy());
        }

        PyObject baseObj = ap.getPyObject(1, null);

        if (baseObj == null) {
            return asPyLong(x);
        }
        if (x == null) {
            throw Py.TypeError("int() missing string argument");
        }
        if (!(baseObj instanceof PyLong)) {
            throw Py.TypeError(String.format("'%s' object cannot be interpreted as an integer",
                    baseObj.getType().fastGetName()));
        }
        int base = baseObj.asInt();
        if (x instanceof PyUnicode) {
            return ((PyUnicode)x).atol(base);
        } else if (x instanceof PyByteArray || x instanceof PyBytes) {
            return Encoding.atol(new String(Py.unwrapBuffer(x)), base);
        }

        throw Py.TypeError("int: can't convert non-string with explicit base");
    }

    /**
     * @return convert to a long.
     * @throws TypeError and AttributeError.
     */
    private static PyObject asPyLong(PyObject x) {
        if (x == null) {
            return Py.Zero;
        }
        try {
            return x.__int__();
        } catch (PyException pye) {
            if (!pye.match(Py.AttributeError)) {
                throw pye;
            }
            try {
                PyObject integral = x.invoke("__trunc__");
                return convertIntegralToLong(integral);
            } catch (PyException pye2) {
                if (!pye2.match(Py.AttributeError)) {
                    throw pye2;
                }
                throw Py.TypeError(String.format(
                        "long() argument must be a string a bytes-like object or a number, not '%.200s'", x.getType()
                                .fastGetName()));
            }
        }
    }

    /**
     * @return convert to an int.
     * @throws TypeError and AttributeError.
     */
    private static PyObject convertIntegralToLong(PyObject integral) {
        if (!(integral instanceof PyLong)) {
            try {
                PyObject i = integral.invoke("__int__");
                if (!(i instanceof PyLong)) {
                    throw Py.TypeError(String.format("__trunc__ returned non-Integral (type %.200s)",
                            integral.getType().fastGetName()));
                }
                return i;
            } catch (PyException e) {
                throw Py.TypeError(String.format("__trunc__ returned non-Integral (type %.200s)",
                        integral.getType().fastGetName()));
            }
        }
        return integral;
    }

    /**
     * Wimpy, slow approach to new calls for subtypes of long.
     *
     * First creates a regular long from whatever arguments we got, then allocates a subtype
     * instance and initializes it from the regular long. The regular long is then thrown away.
     */
    private static PyObject longSubtypeNew(PyNewWrapper new_, boolean init, PyType subtype,
            PyObject[] args, String[] keywords) {
        PyObject tmp = int___new__(new_, init, TYPE, args, keywords);
        return new PyLongDerived(subtype, ((PyLong)tmp).getValue());
    }

    /**
     * Convert a double to BigInteger, raising an OverflowError if infinite.
     */
    private static BigInteger toBigInteger(double value) {
        if (Double.isInfinite(value)) {
            throw Py.OverflowError("cannot convert float infinity to long");
        }
        if (Double.isNaN(value)) {
            throw Py.ValueError("cannot convert float NaN to integer");
        }
        return new BigDecimal(value).toBigInteger();
    }

    @ExposedClassMethod(doc = BuiltinDocs.int_from_bytes_doc)
    public final static PyObject int_from_bytes(PyType type, PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("from_bytes", args, keywords, "bytes", "byteorder", "*", "signed");
        PyObject bytes = ap.getPyObject(0);
        String byteorder = ap.getString(1);
        boolean signed = ap.getBoolean(2, false);
        ByteOrder order = getByteOrder(byteorder);

        if (bytes instanceof BufferProtocol) {
            try (PyBuffer view = ((BufferProtocol) bytes).getBuffer(PyBUF.FULL_RO)) {
                int length = view.getLen();
                byte[] buf = new byte[length];
                view.copyTo(buf, 0);
                if (order == ByteOrder.LITTLE_ENDIAN) {
                    reverseByteArray(buf);
                }
                return new PyLong(new BigInteger(buf));
            }
        }
        PyObject iter;
        try {
            iter = bytes.__iter__();
        } catch(PyException e) {
            throw Py.TypeError(String.format("cannot convert '%s' object to bytes", bytes.getType().fastGetName()));
        }
        PyObject next;
        long ret = 0L;
        int i = 0;
        try {
            while ((next = iter.__next__()) != null) {
                int v = next.asIndex() & 0xFF;
                if (order == ByteOrder.BIG_ENDIAN) {
                    ret = (ret << 8) | v;
                } else {
                    ret = ((v << 8 * i++) | ret);
                }
            }
        } catch (PyException e) {
            if (!e.match(Py.StopIteration)) {
                throw e;
            }
        }
        return new PyLong(BigInteger.valueOf(ret));
    }

    private static void reverseByteArray(byte[] buf) {
        int length = buf.length;
        byte b;
        for (int i = 0, j = length - 1; i < j; i++, j--) {
            b = buf[i];
            buf[i] = buf[j];
            buf[j] = b;
        }
    }

    private static ByteOrder getByteOrder(String byteorder) {
        switch(byteorder) {
            case "little":
                return ByteOrder.LITTLE_ENDIAN;
            case "big":
                return ByteOrder.BIG_ENDIAN;
            default:
                throw Py.TypeError("byteorder must be either 'little' or 'big'");
        }
    }

    private static final double scaledDoubleValue(BigInteger val, int[] exp) {
        double x = 0;
        int signum = val.signum();
        byte[] digits;

        if (signum >= 0) {
            digits = val.toByteArray();
        } else {
            digits = val.negate().toByteArray();
        }

        int count = 8;
        int i = 0;

        if (digits[0] == 0) {
            i++;
            count++;
        }
        count = count <= digits.length ? count : digits.length;

        while (i < count) {
            x = x * 256 + (digits[i] & 0xff);
            i++;
        }
        exp[0] = digits.length - i;
        return signum * x;
    }

    private static final boolean canCoerce(PyObject other) {
        return other instanceof PyLong;
    }

    private static final BigInteger coerce(PyObject other) {
        if (other instanceof PyLong) {
            return ((PyLong) other).getValue();
        } else {
            throw Py.TypeError("xxx");
        }
    }

    private static final PyFloat true_divide(BigInteger a, BigInteger b) {
        int[] ae = new int[1];
        int[] be = new int[1];
        double ad, bd;

        ad = scaledDoubleValue(a, ae);
        bd = scaledDoubleValue(b, be);

        if (bd == 0) {
            throw Py.ZeroDivisionError("division by zero");
        }

        ad /= bd;
        int aexp = ae[0] - be[0];

        if (aexp > Integer.MAX_VALUE / 8) {
            throw Py.OverflowError("long/long too large for a float");
        } else if (aexp < -(Integer.MAX_VALUE / 8)) {
            return PyFloat.ZERO;
        }

        ad = ad * Math.pow(2.0, aexp * 8);

        if (Double.isInfinite(ad)) {
            throw Py.OverflowError("long/long too large for a float");
        }

        return new PyFloat(ad);
    }

    public static PyObject _pow(BigInteger value, BigInteger y, PyObject modulo, PyObject left,
                                PyObject right) {
        if (y.compareTo(BigInteger.ZERO) < 0) {
            if (value.compareTo(BigInteger.ZERO) != 0) {
                return left.__float__().__pow__(right, modulo);
            } else {
                throw Py.ZeroDivisionError("zero to a negative power");
            }
        }
        if (modulo == null) {
            return Py.newLong(value.pow(y.intValue()));
        } else {
            // This whole thing can be trivially rewritten after bugs
            // in modPow are fixed by SUN

            BigInteger z = coerce(modulo);
            // Clear up some special cases right away
            if (z.equals(BigInteger.ZERO)) {
                throw Py.ValueError("pow(x, y, z) with z == 0");
            }
            if (z.abs().equals(BigInteger.ONE)) {
                return Py.newLong(0);
            }

            if (z.compareTo(BigInteger.valueOf(0)) <= 0) {
                // Handle negative modulo specially
                // if (z.compareTo(BigInteger.valueOf(0)) == 0) {
                // throw Py.ValueError("pow(x, y, z) with z == 0");
                // }
                y = value.modPow(y, z.negate());
                if (y.compareTo(BigInteger.valueOf(0)) > 0) {
                    return Py.newLong(z.add(y));
                } else {
                    return Py.newLong(y);
                }
                // return __pow__(right).__mod__(modulo);
            } else {
                // XXX: 1.1 no longer supported so review this.
                // This is buggy in SUN's jdk1.1.5
                // Extra __mod__ improves things slightly
                return Py.newLong(value.modPow(y, z));
                // return __pow__(right).__mod__(modulo);
            }
        }
    }

    private static final int coerceInt(PyObject other) {
        if (other instanceof PyLong) {
            return (other).asInt();
        } else {
            throw Py.TypeError("xxx");
        }
    }

    /**
     * Prepare an IntegerFormatter. This object has an
     * overloaded format method {@link IntegerFormatter#format(int)} and
     * {@link IntegerFormatter#format(BigInteger)} to support the two types.
     *
     * @param spec a parsed PEP-3101 format specification.
     * @return a formatter ready to use, or null if the type is not an integer format type.
     * @throws PyException(ValueError) if the specification is faulty.
     */
    @SuppressWarnings("fallthrough")
    static IntegerFormatter prepareFormatter(Spec spec) throws PyException {

        // Slight differences between format types
        switch (spec.type) {
            case 'c':
                // Character data: specific prohibitions.
                if (Spec.specified(spec.sign)) {
                    throw IntegerFormatter.signNotAllowed("integer", spec.type);
                } else if (spec.alternate) {
                    throw IntegerFormatter.alternateFormNotAllowed("integer", spec.type);
                }
                // Fall through

            case 'x':
            case 'X':
            case 'o':
            case 'b':
            case 'n':
                if (spec.grouping) {
                    throw IntegerFormatter.notAllowed("Grouping", "integer", spec.type);
                }
                // Fall through

            case Spec.NONE:
            case 'd':
                // Check for disallowed parts of the specification
                if (Spec.specified(spec.precision)) {
                    throw IntegerFormatter.precisionNotAllowed("integer");
                }
                // spec may be incomplete. The defaults are those commonly used for numeric formats.
                spec = spec.withDefaults(Spec.NUMERIC);
                // Get a formatter for the spec.
                return new IntegerFormatter(spec);

            default:
                return null;
        }
    }

    public BigInteger getValue() {
        return value;
    }

    public PyObject to_bytes(int length, String byteorder) {
        return int_to_bytes(length, byteorder, false);
    }

    public PyObject to_bytes(int length, String byteorder, boolean signed) {
        return int_to_bytes(length, byteorder, signed);
    }

    @ExposedMethod(defaults={"false"}, doc = BuiltinDocs.int_to_bytes_doc)
    public final PyObject int_to_bytes(int length, String byteorder, boolean signed) {
        byte[] origin = value.toByteArray();
        if (origin[0] == 0) {
            byte[] tmp = origin;
            origin = new byte[tmp.length - 1];
            System.arraycopy(tmp, 1, origin, 0, origin.length);
        }
        if (length < origin.length) {
            throw Py.OverflowError("int too big to convert");
        }
        byte[] result = new byte[length];
        ByteOrder order = getByteOrder(byteorder);
        if (order == ByteOrder.LITTLE_ENDIAN) {
            for (int i = 0, j = origin.length - 1; j >= 0; i++, j--) {
                result[i] = origin[j];
            }
        }

        return new PyBytes(result);
    }

    @ExposedGet(name = "real", doc = BuiltinDocs.int_real_doc)
    public PyObject getReal() {
        return int___int__();
    }

    @ExposedGet(name = "imag", doc = BuiltinDocs.int_imag_doc)
    public PyObject getImag() {
        return Py.newLong(0);
    }

    @ExposedGet(name = "numerator", doc = BuiltinDocs.int_numerator_doc)
    public PyObject getNumerator() {
        return int___int__();
    }

    @ExposedGet(name = "denominator", doc = BuiltinDocs.int_denominator_doc)
    public PyObject getDenominator() {
        return Py.newLong(1);
    }

    @Override
    public String toString() {
        return int_toString();
    }

    @ExposedMethod(names = "__repr__", doc = BuiltinDocs.int___repr___doc)
    public final String int_toString() {
        return getValue().toString();
    }

    @Override
    public int hashCode() {
        return int___hash__();
    }

    @ExposedMethod(doc = BuiltinDocs.int___hash___doc)
    public final int int___hash__() {
        return getValue().hashCode();
    }

    @Override
    public boolean __bool__() {
        return int___bool__();
    }

    @ExposedMethod(doc = BuiltinDocs.int___bool___doc)
    public boolean int___bool__() {
        return !getValue().equals(BigInteger.ZERO);
    }

    public double doubleValue() {
        double v = getValue().doubleValue();
        if (Double.isInfinite(v)) {
            throw Py.OverflowError("long int too large to convert to float");
        }
        return v;
    }

    public double scaledDoubleValue(int[] exp) {
        return scaledDoubleValue(getValue(), exp);
    }

    public long getLong(long min, long max) {
        return getLong(min, max, "long int too large to convert");
    }

    public long getLong(long min, long max, String overflowMsg) {
        if (!isOverflow()) {
            long v = getValue().longValue();
            if (v >= min && v <= max) {
                return v;
            }
        }
        throw Py.OverflowError(overflowMsg);
    }

    public boolean isOverflow() {
        return getValue().compareTo(MAX_INT) >= 0 && getValue().compareTo(MIN_INT) <= 0;
    }

    @Override
    public long asLong(int index) {
        return asLong();
    }

    @Override
    public int asInt(int index) {
        return (int)getLong(Integer.MIN_VALUE, Integer.MAX_VALUE,
                "long int too large to convert to int");
    }

    @Override
    public int asInt() {
        return (int)getLong(Integer.MIN_VALUE, Integer.MAX_VALUE,
                "long int too large to convert to int");
    }

    @Override
    public long asLong() {
        return getLong(Long.MIN_VALUE, Long.MAX_VALUE, "long too big to convert");
    }

    @Override
    public Object __tojava__(Class<?> c) {
        try {
            if (c == Byte.TYPE || c == Byte.class) {
                return Byte.valueOf((byte)getLong(Byte.MIN_VALUE, 0xFF));
            }
            if (c == Short.TYPE || c == Short.class) {
                return Short.valueOf((short)getLong(Short.MIN_VALUE, Short.MAX_VALUE));
            }
            if (c == Integer.TYPE || c == Integer.class) {
                return Integer.valueOf((int)getLong(Integer.MIN_VALUE, Integer.MAX_VALUE));
            }
            if (c == Long.TYPE || c == Long.class) {
                return Long.valueOf(getLong(Long.MIN_VALUE, Long.MAX_VALUE));
            }
            if (c == Float.TYPE || c == Double.TYPE || c == Float.class || c == Double.class) {
                return __float__().__tojava__(c);
            }
            if (c == BigInteger.class || c == Number.class || c == Object.class
                    || c == Serializable.class) {
                return getValue();
            }
        } catch (PyException e) {
            return Py.NoConversion;
        }
        return super.__tojava__(c);
    }

    @Override
    public Object __coerce_ex__(PyObject other) {
        return int___coerce_ex__(other);
    }

    /**
     * Coercion logic for long. Implemented as a final method to avoid invocation of virtual methods
     * from the exposed coerce.
     */
    final Object int___coerce_ex__(PyObject other) {
        if (other instanceof PyLong) {
            return other;
        } else {
            return Py.None;
        }
    }

    @Override
    public PyObject __add__(PyObject right) {
        return int___add__(right);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___add___doc)
    public final PyObject int___add__(PyObject right) {
        if (!canCoerce(right)) {
            return Py.NotImplemented;
        }
        return Py.newLong(getValue().add(coerce(right)));
    }

    @Override
    public PyObject __radd__(PyObject left) {
        return int___radd__(left);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___radd___doc)
    public final PyObject int___radd__(PyObject left) {
        return __add__(left);
    }

    @Override
    public PyObject __sub__(PyObject right) {
        return int___sub__(right);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___sub___doc)
    public final PyObject int___sub__(PyObject right) {
        if (!canCoerce(right)) {
            return Py.NotImplemented;
        }
        return Py.newLong(getValue().subtract(coerce(right)));
    }

    @Override
    public PyObject __rsub__(PyObject left) {
        return int___rsub__(left);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rsub___doc)
    public final PyObject int___rsub__(PyObject left) {
        if (!canCoerce(left)) {
            throw Py.TypeError(String.format("unsupported operand type(s) for -: '%s' and '%s'",
                    left.getType().getName(), getType().getName()));
        }
        return Py.newLong(coerce(left).subtract(getValue()));
    }

    @Override
    public PyObject __mul__(PyObject right) {
        return int___mul__(right);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___mul___doc)
    public final PyObject int___mul__(PyObject right) {
        if (right instanceof PySequence) {
            return ((PySequence)right).repeat(coerceInt(this));
        }

        if (!canCoerce(right)) {
            return Py.NotImplemented;
        }
        return Py.newLong(getValue().multiply(coerce(right)));
    }

    @Override
    public PyObject __rmul__(PyObject left) {
        return int___rmul__(left);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rmul___doc)
    public final PyObject int___rmul__(PyObject left) {
        if (left instanceof PySequence) {
            return ((PySequence)left).repeat(coerceInt(this));
        }
        if (!canCoerce(left)) {
            return null;
        }
        return Py.newLong(coerce(left).multiply(getValue()));
    }

    // Getting signs correct for integer division
    // This convention makes sense when you consider it in tandem with modulo
    private BigInteger divide(BigInteger x, BigInteger y) {
        BigInteger zero = BigInteger.valueOf(0);
        if (y.equals(zero)) {
            throw Py.ZeroDivisionError("integer division or modulo by zero");
        }

        if (y.compareTo(zero) < 0) {
            if (x.compareTo(zero) > 0) {
                return (x.subtract(y).subtract(BigInteger.valueOf(1))).divide(y);
            }
        } else {
            if (x.compareTo(zero) < 0) {
                return (x.subtract(y).add(BigInteger.valueOf(1))).divide(y);
            }
        }
        return x.divide(y);
    }

    @Override
    public PyObject __floordiv__(PyObject right) {
        return int___floordiv__(right);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___floordiv___doc)
    public final PyObject int___floordiv__(PyObject right) {
        if (!canCoerce(right)) {
            return null;
        }
        return Py.newLong(divide(getValue(), coerce(right)));
    }

    @Override
    public PyObject __rfloordiv__(PyObject left) {
        return int___rfloordiv__(left);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rfloordiv___doc)
    public final PyObject int___rfloordiv__(PyObject left) {
        if (!canCoerce(left)) {
            return null;
        }
        return Py.newLong(divide(coerce(left), getValue()));
    }

    @Override
    public PyObject __truediv__(PyObject right) {
        return int___truediv__(right);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___truediv___doc)
    public final PyObject int___truediv__(PyObject right) {
        if (!canCoerce(right)) {
            return null;
        }
        return true_divide(this.getValue(), coerce(right));
    }

    @Override
    public PyObject __rtruediv__(PyObject left) {
        return int___rtruediv__(left);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rtruediv___doc)
    public final PyObject int___rtruediv__(PyObject left) {
        if (!canCoerce(left)) {
            return null;
        }
        return true_divide(coerce(left), this.getValue());
    }

    private BigInteger modulo(BigInteger x, BigInteger y, BigInteger xdivy) {
        return x.subtract(xdivy.multiply(y));
    }

    @Override
    public PyObject __mod__(PyObject right) {
        return int___mod__(right);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___mod___doc)
    public final PyObject int___mod__(PyObject right) {
        if (!canCoerce(right)) {
            return null;
        }
        BigInteger rightv = coerce(right);
        return Py.newLong(modulo(getValue(), rightv, divide(getValue(), rightv)));
    }

    @Override
    public PyObject __rmod__(PyObject left) {
        return int___rmod__(left);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rmod___doc)
    public final PyObject int___rmod__(PyObject left) {
        if (!canCoerce(left)) {
            return null;
        }
        BigInteger leftv = coerce(left);
        return Py.newLong(modulo(leftv, getValue(), divide(leftv, getValue())));
    }

    @Override
    public PyObject __divmod__(PyObject right) {
        return int___divmod__(right);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___divmod___doc)
    public final PyObject int___divmod__(PyObject right) {
        if (!canCoerce(right)) {
            return null;
        }
        BigInteger rightv = coerce(right);

        BigInteger xdivy = divide(getValue(), rightv);
        return new PyTuple(Py.newLong(xdivy), Py.newLong(modulo(getValue(), rightv, xdivy)));
    }

    @Override
    public PyObject __rdivmod__(PyObject left) {
        return int___rdivmod__(left);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rdivmod___doc)
    public final PyObject int___rdivmod__(PyObject left) {
        if (!canCoerce(left)) {
            return null;
        }
        BigInteger leftv = coerce(left);

        BigInteger xdivy = divide(leftv, getValue());
        return new PyTuple(Py.newLong(xdivy), Py.newLong(modulo(leftv, getValue(), xdivy)));
    }

    @Override
    public PyObject __pow__(PyObject right, PyObject modulo) {
        return int___pow__(right, modulo);
    }

    @ExposedMethod(type = MethodType.BINARY, defaults = {"null"},
            doc = BuiltinDocs.int___pow___doc)
    public final PyObject int___pow__(PyObject right, PyObject modulo) {
        if (!canCoerce(right)) {
            return null;
        }

        modulo = (modulo == Py.None) ? null : modulo;
        if (modulo != null && !canCoerce(modulo)) {
            return null;
        }

        return _pow(getValue(), coerce(right), modulo, this, right);
    }

    @Override
    public PyObject __rpow__(PyObject left) {
        return int___rpow__(left);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rpow___doc)
    public final PyObject int___rpow__(PyObject left) {
        if (!canCoerce(left)) {
            return null;
        }

        return _pow(coerce(left), getValue(), null, left, this);
    }

    @Override
    public PyObject __lshift__(PyObject right) {
        return int___lshift__(right);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___lshift___doc)
    public final PyObject int___lshift__(PyObject right) {
        if (!canCoerce(right)) {
            return null;
        }
        int rightv = coerceInt(right);
        if (rightv < 0) {
            throw Py.ValueError("negative shift count");
        }
        return Py.newLong(getValue().shiftLeft(rightv));
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rlshift___doc)
    public final PyObject int___rlshift__(PyObject left) {
        if (!canCoerce(left)) {
            return null;
        }
        if (getValue().intValue() < 0) {
            throw Py.ValueError("negative shift count");
        }
        return Py.newLong(coerce(left).shiftLeft(coerceInt(this)));
    }

    @Override
    public PyObject __rshift__(PyObject right) {
        return int___rshift__(right);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rshift___doc)
    public final PyObject int___rshift__(PyObject right) {
        if (!canCoerce(right)) {
            return null;
        }
        int rightv = coerceInt(right);
        if (rightv < 0) {
            throw Py.ValueError("negative shift count");
        }
        return Py.newLong(getValue().shiftRight(rightv));
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rrshift___doc)
    public final PyObject int___rrshift__(PyObject left) {
        if (!canCoerce(left)) {
            return null;
        }
        if (getValue().intValue() < 0) {
            throw Py.ValueError("negative shift count");
        }
        return Py.newLong(coerce(left).shiftRight(coerceInt(this)));
    }

    @Override
    public PyObject __and__(PyObject right) {
        return int___and__(right);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___and___doc)
    public final PyObject int___and__(PyObject right) {
        if (!canCoerce(right)) {
            return null;
        }
        return Py.newLong(getValue().and(coerce(right)));
    }

    @Override
    public PyObject __rand__(PyObject left) {
        return int___rand__(left);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rand___doc)
    public final PyObject int___rand__(PyObject left) {
        if (!canCoerce(left)) {
            return null;
        }
        return Py.newLong(coerce(left).and(getValue()));
    }

    @Override
    public PyObject __xor__(PyObject right) {
        return int___xor__(right);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___xor___doc)
    public final PyObject int___xor__(PyObject right) {
        if (!canCoerce(right)) {
            return null;
        }
        return Py.newLong(getValue().xor(coerce(right)));
    }

    @Override
    public PyObject __rxor__(PyObject left) {
        return int___rxor__(left);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___rxor___doc)
    public final PyObject int___rxor__(PyObject left) {
        if (!canCoerce(left)) {
            return null;
        }
        return Py.newLong(coerce(left).xor(getValue()));
    }

    @Override
    public PyObject __or__(PyObject right) {
        return int___or__(right);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___or___doc)
    public final PyObject int___or__(PyObject right) {
        if (!canCoerce(right)) {
            return null;
        }
        return Py.newLong(getValue().or(coerce(right)));
    }

    @Override
    public PyObject __ror__(PyObject left) {
        return int___ror__(left);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.int___ror___doc)
    public final PyObject int___ror__(PyObject left) {
        if (!canCoerce(left)) {
            return null;
        }
        return Py.newLong(coerce(left).or(getValue()));
    }

    @Override
    public PyObject __neg__() {
        return int___neg__();
    }

    @ExposedMethod(doc = BuiltinDocs.int___neg___doc)
    public final PyObject int___neg__() {
        return Py.newLong(getValue().negate());
    }

    @Override
    public PyObject __pos__() {
        return int___pos__();
    }

    @ExposedMethod(doc = BuiltinDocs.int___pos___doc)
    public final PyObject int___pos__() {
        return int___int__();
    }

    @ExposedMethod(defaults = {"null"}, doc = BuiltinDocs.int___round___doc)
    public final PyObject int___round__(PyObject ndigits) {
        return this;
    }

    @Override
    public PyObject __abs__() {
        return int___abs__();
    }

    @ExposedMethod(doc = BuiltinDocs.int___abs___doc)
    public final PyObject int___abs__() {
        if (getValue().signum() == -1) {
            return int___neg__();
        }
        return int___int__();
    }

    @Override
    public PyObject __invert__() {
        return int___invert__();
    }

    @ExposedMethod(doc = BuiltinDocs.int___invert___doc)
    public final PyObject int___invert__() {
        return Py.newLong(getValue().not());
    }

    @Override
    public PyObject __int__() {
        return int___int__();
    }

    @ExposedMethod(doc = BuiltinDocs.int___int___doc)
    public final PyObject int___int__() {
        return getType() == TYPE ? this : Py.newLong(getValue());
    }

    @Override
    public PyObject richCompare(PyObject other, CompareOp op) {
        return op.bool(int_compare(other));
    }

    private int int_compare(PyObject other) {
        if (other instanceof PyLong) {
            return value.compareTo(((PyLong) other).getValue());
        }
        if (other instanceof PyFloat) {
            return new BigDecimal(value).compareTo(new BigDecimal(((PyFloat) other).getValue()));
        }
        if (other instanceof PyComplex) {
            PyComplex complex = (PyComplex) other;
            if (complex.imag != 0) {
                return -2;
            }
            return int_compare(complex.getReal());
        }
        return -2;
    }

    @Override
    public PyFloat __float__() {
        return int___float__();
    }

    @ExposedMethod(doc = BuiltinDocs.int___float___doc)
    final PyFloat int___float__() {
        return new PyFloat(doubleValue());
    }

    @Override
    public PyComplex __complex__() {
        return int___complex__();
    }

    final PyComplex int___complex__() {
        return new PyComplex(doubleValue(), 0.);
    }

    @Override
    public PyObject __trunc__() {
        return int___trunc__();
    }

    @ExposedMethod(doc = BuiltinDocs.int___trunc___doc)
    public final PyObject int___trunc__() {
        return this;
    }

    @Override
    public PyObject conjugate() {
        return int_conjugate();
    }

    @ExposedMethod(doc = BuiltinDocs.int_conjugate_doc)
    public final PyObject int_conjugate() {
        return this;
    }

    @ExposedMethod(doc = BuiltinDocs.int___str___doc)
    public PyUnicode int___str__() {
        return new PyUnicode(getValue().toString());
    }

    @Override
    public PyUnicode __str__() {
        return int___str__();
    }

    @ExposedMethod(doc = BuiltinDocs.int___getnewargs___doc)
    public final PyTuple int___getnewargs__() {
        return new PyTuple(new PyLong(this.getValue()));
    }

    @Override
    public PyTuple __getnewargs__() {
        return int___getnewargs__();
    }

    @Override
    public PyObject __index__() {
        return int___index__();
    }

    @ExposedMethod(doc = BuiltinDocs.int___index___doc)
    public final PyObject int___index__() {
        return this;
    }

    @Override
    public int bit_length() {
        return int_bit_length();
    }

    @ExposedMethod(doc = BuiltinDocs.int_bit_length_doc)
    public final int int_bit_length() {
        BigInteger v = value;
        if (v.compareTo(BigInteger.ZERO) == -1) {
            v = v.negate();
        }
        return v.bitLength();
    }

    @Override
    public PyObject __format__(PyObject formatSpec) {
        return int___format__(formatSpec);
    }

    @ExposedMethod(doc = BuiltinDocs.int___format___doc)
    public final PyObject int___format__(PyObject formatSpec) {

        // Parse the specification
        Spec spec = InternalFormat.fromText(formatSpec, "__format__");
        InternalFormat.Formatter f;

        // Try to make an integer formatter from the specification
        IntegerFormatter fi = PyLong.prepareFormatter(spec);
        if (fi != null) {
            // Bytes mode if formatSpec argument is not unicode.
            fi.setBytes(!(formatSpec instanceof PyUnicode));
            // Convert as per specification.
            fi.format(value);
            f = fi;

        } else {
            // Try to make a float formatter from the specification
            FloatFormatter ff = PyFloat.prepareFormatter(spec);
            if (ff != null) {
                // Bytes mode if formatSpec argument is not unicode.
                ff.setBytes(!(formatSpec instanceof PyUnicode));
                // Convert as per specification.
                ff.format(value.doubleValue());
                f = ff;

            } else {
                // The type code was not recognised in either prepareFormatter
                throw Formatter.unknownFormat(spec.type, "integer");
            }
        }

        // Return a result that has the same type (str or unicode) as the formatSpec argument.
        return f.pad().getPyResult();
    }

    @Override
    public boolean isIndex() {
        return true;
    }

    @Override
    public int asIndex(PyObject err) {
        boolean tooLow = getValue().compareTo(PyLong.MIN_INT) < 0;
        boolean tooHigh = getValue().compareTo(PyLong.MAX_INT) > 0;
        if (tooLow || tooHigh) {
            if (err != null) {
                throw new PyException(err, "cannot fit 'long' into an index-sized integer");
            }
            return tooLow ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        }
        return (int)getValue().longValue();
    }

    @Override
    public boolean isMappingType() {
        return false;
    }

    @Override
    public boolean isNumberType() {
        return true;
    }

    @Override
    public boolean isSequenceType() {
        return false;
    }

    public PyLong gcd(PyObject other) {
        return new PyLong(getValue().gcd(((PyLong) other.__index__()).getValue()));
    }
}
