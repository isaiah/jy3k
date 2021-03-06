// Copyright (c) Corporation for National Research Initiatives
// Copyright (c) Jython Developers
package org.python.core;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.python.core.stringlib.FloatFormatter;
import org.python.core.stringlib.InternalFormat;
import org.python.core.stringlib.InternalFormat.Formatter;
import org.python.core.stringlib.InternalFormat.Spec;
import org.python.core.util.ExtraMath;
import org.python.annotations.ExposedClassMethod;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.expose.MethodType;
import org.python.modules.math;

/**
 * A builtin python float.
 */
@Untraversable
@ExposedType(name = "float", doc = BuiltinDocs.float_doc)
public class PyFloat extends PyObject {

    public static final PyType TYPE = PyType.fromClass(PyFloat.class);

    /** Format specification used by repr(). */
    static final Spec SPEC_REPR = InternalFormat.fromText(" >r");
    /** Format specification used by str(). */
    static final Spec SPEC_STR = Spec.NUMERIC;
    /** Constant float(0). */
    static final PyFloat ZERO = new PyFloat(0.0);
    /** Constant float(1). */
    static final PyFloat ONE = new PyFloat(1.0);
    /** Constant float("nan"). */
    static final PyFloat NAN = new PyFloat(Double.NaN);

    private final double value;

    public double getValue() {
        return value;
    }

    public PyFloat(PyType subtype, double v) {
        super(subtype);
        value = v;
    }

    public PyFloat(double v) {
        this(TYPE, v);
    }

    public PyFloat(float v) {
        this((double)v);
    }

    @ExposedNew
    public static PyObject float_new(PyNewWrapper new_, boolean init, PyType subtype,
            PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("float", args, keywords, new String[] {"x"}, 0);
        PyObject x = ap.getPyObject(0, null);
        if (x == null) {
            if (new_.for_type == subtype) {
                return ZERO;
            }
            return new PyFloat(subtype, 0.0);
        }
        PyObject floatObject = Abstract.PyNumber_Float(Py.getThreadState(), x);
        if (new_.for_type == subtype) {
            return floatObject;
        }
        return new PyFloat(subtype, ((PyFloat) floatObject).getValue());
    }

    @ExposedGet(name = "real")
    public PyObject getReal() {
        return float___float__();
    }

    @ExposedGet(name = "imag")
    public PyObject getImag() {
        return ZERO;
    }

    @ExposedClassMethod(doc = BuiltinDocs.float_fromhex_doc)
    public static PyObject float_fromhex(PyType type, PyObject o) {
        // XXX: I'm sure this could be shortened/simplified, but Double.parseDouble() takes
        // non-hex strings and requires the form 0xNUMBERpNUMBER for hex input which
        // causes extra complexity here.

        String message = "invalid hexadecimal floating-point string";
        boolean negative = false;

        PyObject s = Abstract.PyObject_Str(Py.getThreadState(), o);
        String value = s.toString().trim().toLowerCase();

        if (value.length() == 0) {
            throw Py.ValueError(message);
        } else if (value.equals("nan") || value.equals("-nan") || value.equals("+nan")) {
            return NAN;
        } else if (value.equals("inf") || value.equals("infinity") || value.equals("+inf")
                || value.equals("+infinity")) {
            return new PyFloat(Double.POSITIVE_INFINITY);
        } else if (value.equals("-inf") || value.equals("-infinity")) {
            return new PyFloat(Double.NEGATIVE_INFINITY);
        }

        // Strip and record + or -
        if (value.charAt(0) == '-') {
            value = value.substring(1);
            negative = true;
        } else if (value.charAt(0) == '+') {
            value = value.substring(1);
        }
        if (value.length() == 0) {
            throw Py.ValueError(message);
        }

        // Append 0x if not present.
        if (!value.startsWith("0x") && !value.startsWith("0X")) {
            value = "0x" + value;
        }

        // reattach - if needed.
        if (negative) {
            value = "-" + value;
        }

        // Append p if not present.
        if (value.indexOf('p') == -1) {
            value = value + "p0";
        }

        try {
            double d = Double.parseDouble(value);
            if (Double.isInfinite(d)) {
                throw Py.OverflowError("hexadecimal value too large to represent as a float");
            }
            return new PyFloat(d);
        } catch (NumberFormatException n) {
            throw Py.ValueError(message);
        }
    }

    // @ExposedClassMethod(doc = BuiltinDocs.float_hex_doc)
    // public static PyObject float_hex(PyType type, double value) {
    // return new PyBytes(Double.toHexString(value));
    // }

    private String pyHexString(Double f) {
        // Simply rewrite Java hex repr to expected Python values; not
        // the most efficient, but we don't expect this to be a hot
        // spot in our code either
        String java_hex = Double.toHexString(getValue());
        if (java_hex.equals("Infinity")) {
            return "inf";
        } else if (java_hex.equals("-Infinity")) {
            return "-inf";
        } else if (java_hex.equals("NaN")) {
            return "nan";
        } else if (java_hex.equals("0x0.0p0")) {
            return "0x0.0p+0";
        } else if (java_hex.equals("-0x0.0p0")) {
            return "-0x0.0p+0";
        }

        // replace hex rep of MpE to conform with Python such that
        // 1. M is padded to 16 digits (ignoring a leading -)
        // 2. Mp+E if E>=0
        // example: result of 42.0.hex() is translated from
        // 0x1.5p5 to 0x1.5000000000000p+5
        int len = java_hex.length();
        boolean start_exponent = false;
        StringBuilder py_hex = new StringBuilder(len + 1);
        int padding = f > 0 ? 17 : 18;
        for (int i = 0; i < len; i++) {
            char c = java_hex.charAt(i);
            if (c == 'p') {
                for (int pad = i; pad < padding; pad++) {
                    py_hex.append('0');
                }
                start_exponent = true;
            } else if (start_exponent) {
                if (c != '-') {
                    py_hex.append('+');
                }
                start_exponent = false;
            }
            py_hex.append(c);
        }
        return py_hex.toString();
    }

    @ExposedMethod(doc = BuiltinDocs.float_hex_doc)
    public PyObject float_hex() {
        return new PyUnicode(pyHexString(getValue()));
    }

    /**
     * Determine if this float is not infinity, nor NaN.
     */
    public boolean isFinite() {
        return !Double.isInfinite(getValue()) && !Double.isNaN(getValue());
    }

    @Override
    public String toString() {
        return formatDouble(SPEC_STR);
    }

    @ExposedMethod(doc = BuiltinDocs.float___str___doc)
    final PyObject float___str__() {
        return Py.newUnicode(toString());
    }

    @Override
    public PyObject richCompare(PyObject other, CompareOp op) {
        double i = getValue();
        double j;

        int result;
        if (other instanceof PyFloat) {
            j = ((PyFloat)other).getValue();
        } else if (!isFinite()) {
            // we're infinity: our magnitude exceeds any finite
            // integer, so it doesn't matter which int we compare i
            // with. If NaN, similarly.
            if (other instanceof PyLong) {
                j = 0.0;
            } else {
                return Py.NotImplemented;
            }
        } else if (other instanceof PyLong) {
            BigDecimal v = new BigDecimal(getValue());
            BigDecimal w = new BigDecimal(((PyLong) other).getValue());
            return op.bool(v.compareTo(w));
        } else {
            return Py.NotImplemented;
        }

        if (i < j) {
            result = -1;
        } else if (i > j) {
            result = 1;
        } else if (i == j) {
            result = 0;
        } else {
          // at least one side is NaN
          result = Double.isNaN(i) ? (Double.isNaN(j) ? 1 : -1) : 1;
        }
        return op.bool(result);
    }

    @ExposedMethod(doc = BuiltinDocs.float___lt___doc)
    final PyObject float___lt__(PyObject other) {
        return richCompare(other, CompareOp.LT);
    }

    @ExposedMethod(doc = BuiltinDocs.float___eq___doc)
    final PyObject float___eq__(PyObject other) {
        return richCompare(other, CompareOp.EQ);
    }

    public PyUnicode __repr__() {
        return float___repr__();
    }

    @ExposedMethod(doc = BuiltinDocs.float___repr___doc)
    public final PyUnicode float___repr__() {
        return Py.newUnicode(formatDouble(SPEC_REPR));
    }

    /**
     * Format this float according to the specification passed in. Supports <code>__str__</code> and
     * <code>__repr__</code>.
     *
     * @param spec parsed format specification string
     * @return formatted value
     */
    private String formatDouble(Spec spec) {
        FloatFormatter f = new FloatFormatter(spec);
        return f.format(value).getResult();
    }

    @Override
    public int hashCode() {
        return float___hash__();
    }

    @ExposedMethod(doc = BuiltinDocs.float___hash___doc)
    final int float___hash__() {
        double value = getValue();
        if (Double.isInfinite(value)) {
            return value < 0 ? -271828 : 314159;
        } else if (Double.isNaN(value)) {
            return 0;
        }

        int intPart = (int) Math.floor(value);
        double fractPart = value - intPart;

        if (fractPart == 0) {
            if (intPart <= Integer.MAX_VALUE && intPart >= Integer.MIN_VALUE) {
                return intPart;
            } else {
                return new PyLong(intPart).hashCode();
            }
        } else {
            long v = Double.doubleToLongBits(getValue());
            return (int)v ^ (int)(v >> 32);
        }
    }

    @Override
    public boolean isTrue() {
        return float___bool__();
    }

    @ExposedMethod(doc = BuiltinDocs.float___bool___doc)
    final boolean float___bool__() {
        return getValue() != 0;
    }

    @Override
    public Object __tojava__(Class<?> c) {
        if (c == Double.TYPE || c == Number.class || c == Double.class || c == Object.class
                || c == Serializable.class) {
            return Double.valueOf(getValue());
        } else if (c == Float.TYPE || c == Float.class) {
            return Float.valueOf((float) getValue());
        }
        return super.__tojava__(c);
    }

    @Override
    public PyObject __eq__(PyObject other) {
        return float___eq__(other);
    }

    @Override
    public PyObject __ne__(PyObject other) {
        if (Double.isNaN(getValue())) {
            return Py.True;
        }
        return null;
    }

    @Override
    public PyObject __gt__(PyObject other) {
        // NaN > anything is always false.
        if (Double.isNaN(getValue())) {
            return Py.False;
        }
        return null;
    }

    @Override
    public PyObject __ge__(PyObject other) {
        // NaN >= anything is always false.
        if (Double.isNaN(getValue())) {
            return Py.False;
        }
        return null;
    }

    @Override
    public PyObject __lt__(PyObject other) {
        return float___lt__(other);
    }

    @Override
    public PyObject __le__(PyObject other) {
        // NaN >= anything is always false.
        if (Double.isNaN(getValue())) {
            return Py.False;
        }
        return null;
    }

    @Override
    public Object __coerce_ex__(PyObject other) {
        return float___coerce_ex__(other);
    }
//
//    @ExposedMethod(doc = BuiltinDocs.float___coerce___doc)
//    final PyObject float___coerce__(PyObject other) {
//        return adaptToCoerceTuple(float___coerce_ex__(other));
//    }

    /**
     * Coercion logic for float. Implemented as a final method to avoid invocation of virtual
     * methods from the exposed coerce.
     */
    final Object float___coerce_ex__(PyObject other) {
        if (other instanceof PyFloat) {
            return other;
        } else if (other instanceof PyLong) {
            return new PyFloat(((PyLong)other).doubleValue());
        } else {
            return Py.None;
        }
    }

    private static boolean canCoerce(PyObject other) {
        return other instanceof PyFloat || other instanceof PyLong;
    }

    private static double coerce(PyObject other) {
        if (other instanceof PyFloat) {
            return ((PyFloat)other).getValue();
        } else if (other instanceof PyLong) {
            return ((PyLong)other).doubleValue();
        } else {
            throw Py.TypeError("xxx");
        }
    }

    @ExposedMethod(names = {"__add__", "__radd__"}, type = MethodType.BINARY, doc = BuiltinDocs.float___add___doc)
    public final PyObject __add__(PyObject right) {
        if (!canCoerce(right)) {
            return Py.NotImplemented;
        }
        double rightv = coerce(right);
        return new PyFloat(getValue() + rightv);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.float___sub___doc)
    final PyObject float___sub__(PyObject right) {
        if (!canCoerce(right)) {
            return null;
        }
        double rightv = coerce(right);
        return new PyFloat(getValue() - rightv);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.float___rsub___doc)
    final PyObject float___rsub__(PyObject left) {
        if (!canCoerce(left)) {
            return null;
        }
        double leftv = coerce(left);
        return new PyFloat(leftv - getValue());
    }

    public PyObject __mul__(PyObject right) {
        return float___mul__(right);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.float___mul___doc)
    final PyObject float___mul__(PyObject right) {
        if (!canCoerce(right)) {
            return null;
        }
        double rightv = coerce(right);
        return new PyFloat(getValue() * rightv);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.float___rmul___doc)
    final PyObject float___rmul__(PyObject left) {
        return __mul__(left);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.float___floordiv___doc)
    final PyObject float___floordiv__(PyObject right) {
        if (!canCoerce(right)) {
            return null;
        }
        double rightv = coerce(right);
        if (rightv == 0) {
            throw Py.ZeroDivisionError("float division by zero");
        }
        return new PyFloat(Math.floor(getValue() / rightv));
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.float___rfloordiv___doc)
    final PyObject float___rfloordiv__(PyObject left) {
        if (!canCoerce(left)) {
            return null;
        }
        double leftv = coerce(left);
        if (getValue() == 0) {
            throw Py.ZeroDivisionError("float division by zero");
        }
        return new PyFloat(Math.floor(leftv / getValue()));
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.float___truediv___doc)
    final PyObject float___truediv__(PyObject right) {
        if (!canCoerce(right)) {
            return null;
        }
        double rightv = coerce(right);
        if (rightv == 0) {
            throw Py.ZeroDivisionError("float division by zero");
        }
        return new PyFloat(getValue() / rightv);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.float___rtruediv___doc)
    final PyObject float___rtruediv__(PyObject left) {
        if (!canCoerce(left)) {
            return null;
        }
        double leftv = coerce(left);
        if (getValue() == 0) {
            throw Py.ZeroDivisionError("float division by zero");
        }
        return new PyFloat(leftv / getValue());
    }

    /**
     * Python % operator: y = n*x + z. The modulo operator always yields a result with the same sign
     * as its second operand (or zero). (Compare <code>java.Math.IEEEremainder</code>)
     *
     * @param x dividend
     * @param y divisor
     * @return <code>x % y</code>
     */
    private static double modulo(double x, double y) {
        if (y == 0.0) {
            throw Py.ZeroDivisionError("float modulo");
        } else {
            double z = x % y;
            if (z == 0.0) {
                // Has to be same sign as y (even when zero).
                return Math.copySign(z, y);
            } else if ((z > 0.0) == (y > 0.0)) {
                // z has same sign as y, as it must.
                return z;
            } else {
                // Note abs(z) < abs(y) and opposite sign.
                return z + y;
            }
        }
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.float___mod___doc)
    final PyObject float___mod__(PyObject right) {
        if (!canCoerce(right)) {
            return null;
        }
        double rightv = coerce(right);
        return new PyFloat(modulo(getValue(), rightv));
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.float___rmod___doc)
    final PyObject float___rmod__(PyObject left) {
        if (!canCoerce(left)) {
            return null;
        }
        double leftv = coerce(left);
        return new PyFloat(modulo(leftv, getValue()));
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.float___divmod___doc)
    final PyObject float___divmod__(PyObject right) {
        if (!canCoerce(right)) {
            return null;
        }
        double rightv = coerce(right);

        if (rightv == 0) {
            throw Py.ZeroDivisionError("float division by zero");
        }
        double z = Math.floor(getValue() / rightv);

        return new PyTuple(new PyFloat(z), new PyFloat(getValue() - z * rightv));
    }

    public PyObject __rdivmod__(PyObject left) {
        if (!canCoerce(left)) {
            return null;
        }
        double leftv = coerce(left);

        if (getValue() == 0) {
            throw Py.ZeroDivisionError("float division by zero");
        }
        double z = Math.floor(leftv / getValue());

        return new PyTuple(new PyFloat(z), new PyFloat(leftv - z * getValue()));
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.float___rdivmod___doc)
    final PyObject float___rdivmod__(PyObject left) {
        return __rdivmod__(left);
    }

    @Override
    public PyObject __pow__(PyObject right, PyObject modulo) {
        return float___pow__(right, modulo);
    }

    @ExposedMethod(type = MethodType.BINARY, defaults = "null", //
            doc = BuiltinDocs.float___pow___doc)
    final PyObject float___pow__(PyObject right, PyObject modulo) {
        if (!canCoerce(right)) {
            return null;
        }

        if (modulo != null && modulo != Py.None) {
            throw Py.TypeError("pow() 3rd argument not allowed unless all arguments are integers");
        }
        return _pow(getValue(), coerce(right));
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.float___rpow___doc)
    final PyObject float___rpow__(PyObject left) {
        return __rpow__(left);
    }

    public PyObject __rpow__(PyObject left) {
        if (!canCoerce(left)) {
            return null;
        }
        return _pow(coerce(left), getValue());
    }

    private static PyFloat _pow(double v, double w) {
        /*
         * This code was translated from the CPython implementation at v2.7.8 by progressively
         * removing cases that could be delegated to Java. Jython differs from CPython in that where
         * C pow() overflows, Java pow() returns inf (observed on Windows). This is not subject to
         * regression tests, so we take it as an allowable platform dependency. All other
         * differences in Java Math.pow() are trapped below and Python behaviour is enforced.
         */
        if (w == 0) {
            // v**0 is 1, even 0**0
            return ONE;

        } else if (Double.isNaN(v)) {
            // nan**w = nan, unless w == 0
            return NAN;

        } else if (Double.isNaN(w)) {
            // v**nan = nan, unless v == 1; 1**nan = 1
            if (v == 1.0) {
                return ONE;
            } else {
                return NAN;
            }

        } else if (Double.isInfinite(w)) {
            /*
             * In Java Math pow(1,inf) = pow(-1,inf) = pow(1,-inf) = pow(-1,-inf) = nan, but in
             * Python they are all 1.
             */
            if (v == 1.0 || v == -1.0) {
                return ONE;
            }

        } else if (v == 0.0) {
            // 0**w is an error if w is negative.
            if (w < 0.0) {
                throw Py.ZeroDivisionError("0.0 cannot be raised to a negative power");
            }

        } else if (!Double.isInfinite(v) && v < 0.0) {
            if (w != Math.floor(w)) {
                throw Py.ValueError("negative number cannot be raised to a fractional power");
            }

        }

        // In all cases not caught above we can entrust the calculation to Java
        return new PyFloat(Math.pow(v, w));

    }

    @ExposedMethod(doc = BuiltinDocs.float___neg___doc)
    final PyObject float___neg__() {
        return new PyFloat(-getValue());
    }

    @ExposedMethod(doc = BuiltinDocs.float___pos___doc)
    final PyObject float___pos__() {
        return float___float__();
    }

    @ExposedMethod(doc = BuiltinDocs.float___abs___doc)
    final PyObject float___abs__() {
        return new PyFloat(Math.abs(getValue()));
    }

    @ExposedMethod(defaults = {"null"}, doc = BuiltinDocs.float___round___doc)
    final PyObject float___round__(PyObject ndigitsObj) {
        int ndigits = 0;
        if (ndigitsObj == Py.None) {
            ndigitsObj = null;
        }
        if (ndigitsObj != null) {
            ndigits = ndigitsObj.asIndex();
        }
        double x = asDouble();
        double r = ExtraMath.round(x, ndigits);
        if (Double.isInfinite(r) && !Double.isInfinite(x)) {
            // Rounding caused magnitude to increase beyond representable range
            throw Py.OverflowError("rounded value too large to represent");
        }
        if (!Double.isInfinite(r) && ndigitsObj != null)
            return new PyFloat(r);
        return new PyLong(r);
    }

    @ExposedMethod(doc = BuiltinDocs.float___int___doc)
    final PyObject float___int__() {
        return new PyLong(getValue());
    }

    @ExposedMethod(doc = BuiltinDocs.float___float___doc)
    final PyFloat float___float__() {
        return getType() == TYPE ? this : Py.newFloat(getValue());
    }

    @ExposedMethod(doc = BuiltinDocs.float___trunc___doc)
    final PyObject float___trunc__() {
        if (Double.isNaN(value)) {
            throw Py.ValueError("cannot convert float NaN to integer");
        }
        if (Double.isInfinite(value)) {
            throw Py.OverflowError("cannot convert float infinity to integer");
        }
        if (value < Integer.MAX_VALUE) {
            return new PyLong((long)value);
        }
        BigDecimal d = new BigDecimal(value);
        return new PyLong(d.toBigInteger());
    }

    @Override
    public PyObject conjugate() {
        return float_conjugate();
    }

    @ExposedMethod(doc = BuiltinDocs.float_conjugate_doc)
    final PyObject float_conjugate() {
        return this;
    }

    public boolean is_integer() {
        return float_is_integer();
    }

    @ExposedMethod(doc = BuiltinDocs.float_is_integer_doc)
    final boolean float_is_integer() {
        if (Double.isInfinite(value)) {
            return false;
        }
        return Math.floor(value) == value;
    }

    @Override
    public PyComplex __complex__() {
        return new PyComplex(getValue(), 0.);
    }

    @ExposedMethod(doc = BuiltinDocs.float___getnewargs___doc)
    final PyTuple float___getnewargs__() {
        return new PyTuple(new PyObject[] {new PyFloat(getValue())});
    }

    @Override
    public PyTuple __getnewargs__() {
        return float___getnewargs__();
    }

    @ExposedMethod(doc = BuiltinDocs.float___format___doc)
    final PyObject float___format__(PyObject formatSpec) {

        // Parse the specification
        Spec spec = InternalFormat.fromText(formatSpec, "__format__");

        // Get a formatter for the specification
        FloatFormatter f = prepareFormatter(spec);

        if (f != null) {
            // Bytes mode if formatSpec argument is not unicode.
            f.setBytes(!(formatSpec instanceof PyUnicode));
            // Convert as per specification.
            f.format(value);
            // Return a result that has the same type (str or unicode) as the formatSpec argument.
            return f.pad().getPyResult();

        } else {
            // The type code was not recognised in prepareFormatter
            throw Formatter.unknownFormat(spec.type, "float");
        }
    }

    /**
     * Common code for PyFloat and {@link PyLong} to prepare a
     * {@link FloatFormatter} from a parsed specification. The object returned has format method
     * {@link FloatFormatter#format(double)}.
     *
     * @param spec a parsed PEP-3101 format specification.
     * @return a formatter ready to use, or null if the type is not a floating point format type.
     * @throws PyException(ValueError) if the specification is faulty.
     */
    @SuppressWarnings("fallthrough")
    static FloatFormatter prepareFormatter(Spec spec) {

        // Slight differences between format types
        switch (spec.type) {

            case 'n':
                if (spec.grouping) {
                    throw Formatter.notAllowed("Grouping", "float", spec.type);
                }
                // Fall through

            case Spec.NONE:
            case 'e':
            case 'f':
            case 'g':
            case 'E':
            case 'F':
            case 'G':
            case '%':
                // Check for disallowed parts of the specification
                if (spec.alternate) {
                    throw FloatFormatter.alternateFormNotAllowed("float");
                }
                // spec may be incomplete. The defaults are those commonly used for numeric formats.
                spec = spec.withDefaults(Spec.NUMERIC);
                return new FloatFormatter(spec);

            default:
                return null;
        }
    }

    @ExposedMethod(doc = BuiltinDocs.float_as_integer_ratio_doc)
    public PyTuple as_integer_ratio() {
        if (Double.isInfinite(value)) {
            throw Py.OverflowError("Cannot pass infinity to float.as_integer_ratio.");
        }
        if (Double.isNaN(value)) {
            throw Py.ValueError("Cannot pass NaN to float.as_integer_ratio.");
        }
        PyTuple frexp = math.frexp(value);
        double float_part = ((Double)frexp.get(0)).doubleValue();
        int exponent = ((BigInteger)frexp.get(1)).intValue();
        for (int i = 0; i < 300 && float_part != Math.floor(float_part); i++) {
            float_part *= 2.0;
            exponent--;
        }
        /*
         * self == float_part * 2**exponent exactly and float_part is integral. If FLT_RADIX != 2,
         * the 300 steps may leave a tiny fractional part to be truncated by PyLong_FromDouble().
         */

        PyLong numerator = new PyLong(float_part);
        PyLong denominator = new PyLong(1);
        PyLong py_exponent = new PyLong(Math.abs(exponent));
        py_exponent = (PyLong)denominator.__lshift__(py_exponent);
        if (exponent > 0) {
            numerator = new PyLong(numerator.getValue().multiply(py_exponent.getValue()));
        } else {
            denominator = py_exponent;
        }
        return new PyTuple(numerator, denominator);
    }

    @Override
    public double asDouble() {
        return getValue();
    }

    @Override
    public boolean isNumberType() {
        return true;
    }

    // standard singleton issues apply here to __getformat__/__setformat__,
    // but this is what Python demands
    public enum Format {

        UNKNOWN("unknown"), BE("IEEE, big-endian"), LE("IEEE, little-endian");

        private final String format;

        Format(String format) {
            this.format = format;
        }

        public String format() {
            return format;
        }
    }

    // subset of IEEE-754, the JVM is big-endian
    public static volatile Format double_format = Format.BE;
    public static volatile Format float_format = Format.BE;

    @ExposedClassMethod(doc = BuiltinDocs.float___getformat___doc)
    public static String float___getformat__(PyType type, String typestr) {
        if ("double".equals(typestr)) {
            return double_format.format();
        } else if ("float".equals(typestr)) {
            return float_format.format();
        } else {
            throw Py.ValueError("__getformat__() argument 1 must be 'double' or 'float'");
        }
    }

    @ExposedClassMethod
    public static void float___setformat__(PyType type, String typestr, String format) {
        Format new_format = null;
        if (!"double".equals(typestr) && !"float".equals(typestr)) {
            throw Py.ValueError("__setformat__() argument 1 must be 'double' or 'float'");
        }
        if (Format.LE.format().equals(format)) {
            throw Py.ValueError(String.format("can only set %s format to 'unknown' or the "
                    + "detected platform value", typestr));
        } else if (Format.BE.format().equals(format)) {
            new_format = Format.BE;
        } else if (Format.UNKNOWN.format().equals(format)) {
            new_format = Format.UNKNOWN;
        } else {
            throw Py.ValueError("__setformat__() argument 2 must be 'unknown', "
                    + "'IEEE, little-endian' or 'IEEE, big-endian'");
        }
        if (new_format != null) {
            if ("double".equals(typestr)) {
                double_format = new_format;
            } else {
                float_format = new_format;
            }
        }
    }
}
