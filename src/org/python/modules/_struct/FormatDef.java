package org.python.modules._struct;

import org.python.core.Py;
import org.python.core.PyBoolean;
import org.python.core.PyBytes;
import org.python.core.PyException;
import org.python.core.PyFloat;
import org.python.core.PyLong;
import org.python.core.PyObject;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.python.modules._struct._struct.StructError;

/** The translation function for each format character is table driven */
//    typedef struct _formatdef {
//        char format;
//        Py_ssize_t size;
//        Py_ssize_t alignment;
//        PyObject* (*unpack)(const char *,
//                        const struct _formatdef *);
//    int (*pack)(char *, PyObject *,
//                const struct _formatdef *);
//    } formatdef;


public class FormatDef implements Packer, Unpacker {
    char name;
    int size;
    int alignment;
    private Packer packer;
    private Unpacker unpacker;

    FormatDef(int size, Packer packer, Unpacker unpacker) {
        this.size = size;
        this.packer = packer;
        this.unpacker = unpacker;
    }

    @Override
    public void pack(ByteBuffer buf, PyObject value) {
        packer.pack(buf, value);
    }

    @Override
    public PyObject unpack(ByteBuffer buf) {
        return unpacker.unpack(buf);
    }

    static int get_int(PyObject value) {
        if (value instanceof PyLong) {
            return value.asInt();
        } else if (value.isIndex()) {
            try {
                return value.asIndex();
            } catch (PyException ex) {
                throw StructError("required argument is not an integer");
            }
        }
        throw StructError("required argument is not an integer");
    }

    static long get_long(PyObject value) {
        if (value instanceof PyLong){
            try {
                return ((PyLong) value).getValue().longValueExact();
            } catch (ArithmeticException e) {
                throw StructError("argument out of range");
            }
        } else
            return get_int(value);
    }

    static BigInteger get_ulong(PyObject value) {
        if (value instanceof PyLong){
            BigInteger v = ((PyLong) value).getValue();
            if (v.compareTo(PyLong.MAX_ULONG) > 0){
                throw StructError("unsigned long int too long to convert");
            }
            return v;
        } else
            return BigInteger.valueOf(get_int(value));
    }

    static double get_double(PyObject value) {
        return value.asDouble();
    }

    static FormatDef getentry(char c, char tableIndicator) {
        switch (c) {
            case 'b':
                return f_byte;
            case 'B':
                return f_ubyte;
            case '?':
                return f_bool;
            case 'c':
                return f_char;
            case 's':
            case 'p':
            case 'x':
                return f_empty;
            case 'e':
                return f_halffloat;
            case 'h':
                return f_short;
            case 'H':
                return f_ushort;
            case 'i':
            case 'l':
                return f_int;
            case 'I':
            case 'L':
                return f_uint;
            case 'q':
                return f_long;
            case 'Q':
                return f_ulong;
            case 'n':
                if (tableIndicator == '@') {
                    return f_size_t;
                }
                break;
            case 'N':
                if (tableIndicator == '@') {
                    return f_ssize_t;
                }
                break;
            case 'f':
                return f_float;
            case 'd':
                return f_double;
            case 'P':
                return f_pointer;
        }
        throw StructError("bad char in struct format");
    }


    static ByteOrder byteOrder(String pfmt) {
        if (pfmt.isEmpty()) {
            return ByteOrder.BIG_ENDIAN;
        }
        char c = pfmt.charAt(0);
        return c == '<' ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
    }

   /* A large number of small routines follow, with names of the form

       [bln][up]_TYPE

       [bln] distiguishes among big-endian, little-endian and native.
       [pu] distiguishes between pack (to struct) and unpack (from struct).
       TYPE is one of char, byte, ubyte, etc.
    */

    /* Native mode routines. ****************************************************/
    /* NOTE:
       In all n[up]_<type> routines handling types larger than 1 byte, there is
       *no* guarantee that the p pointer is properly aligned for each type,
       therefore memcpy is called.  An intermediate variable is used to
       compensate for big-endian architectures.
       Normally both the intermediate variable and the memcpy call will be
       skipped by C optimisation in little-endian architectures (gcc >= 2.91
       does this). */
    static PyObject nu_char(ByteBuffer buf) {
        return new PyBytes((char) buf.get());
    }

    static PyObject nu_byte(ByteBuffer buf) {
        return new PyLong(buf.get());
    }

    static PyObject nu_ubyte(ByteBuffer buf) {
        return new PyLong(buf.get() & 0xFF);
    }

    static PyObject nu_short(ByteBuffer buf) {
        return new PyLong(buf.getShort());
    }

    static PyObject nu_ushort(ByteBuffer buf) {
        return new PyLong(buf.getShort() & 0xFFFF);
    }

    static PyObject nu_halffloat(ByteBuffer buf) {
        boolean sign;
        int e;
        int f, firstbyte, secondbyte;
        double x;
        int s = 1;
        if (buf.order() == ByteOrder.LITTLE_ENDIAN) {
            secondbyte = buf.get() & 0xFF;
            firstbyte = buf.get() & 0xFF;
        } else {
            firstbyte = buf.get() & 0xFF;
            secondbyte = buf.get() & 0xFF;
        }

        /* First byte */
        sign = ((firstbyte >> 7) & 1) == 1;
        e = (firstbyte & 0x7C) >> 2;
        f = (firstbyte & 0x03) << 8;

        /* Second byte */
        f |= secondbyte;

        if (e == 0x1F) {
            if (f == 0) {
                /* Infinity */
                return new PyFloat(sign ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY);
            }
            /* Nan */
            return new PyFloat(sign ? -Float.NaN : Float.NaN);
        }
        x = f / 1024.0;
        if (e == 0) {
            e = -14;
        } else {
            x += 1.0;
            e -= 15;
        }
        x = Math.scalb(x, e);
        if (sign) {
            return new PyFloat(-x);
        }
        return new PyFloat(x);
    }

    static PyObject nu_int(ByteBuffer buf) {
        return new PyLong(buf.getInt());
    }

    static PyObject nu_uint(ByteBuffer buf) {
        long v = buf.getInt();
        return new PyLong(v & 0xFFFFFFFFL);
    }

    static PyObject nu_long(ByteBuffer buf) {
        return new PyLong(buf.getLong());
    }

    /** the constant 2^64 */
    private static final BigInteger TWO_64 = BigInteger.ONE.shiftLeft(64);
    static PyObject nu_ulong(ByteBuffer buf) {
        BigInteger b = BigInteger.valueOf(buf.getLong());
        if (b.signum() < 0) {
            b = b.add(TWO_64);
        }
        return new PyLong(b);
    }

    static PyObject nu_size_t(ByteBuffer buf) {
        return new PyLong(buf.getLong());
    }

    static PyObject nu_bool(ByteBuffer buf) {
        return new PyBoolean(buf.get() != 0);
    }

    static PyObject nu_float(ByteBuffer buf) {
        return new PyFloat(buf.getFloat());
    }

    static PyObject nu_double(ByteBuffer buf) {
        return new PyFloat(buf.getDouble());
    }

    private static void np_byte(ByteBuffer buf, PyObject v) {
        int val = get_int(v);
        if (val > Byte.MAX_VALUE || val < Byte.MIN_VALUE) {
            throw StructError("byte format requires -128 <= number <= 127");
        }
        buf.put((byte) val);
    }

    private static void np_ubyte(ByteBuffer buf, PyObject v) {
        int val = get_int(v);
        if (val < 0 || val > 0xFF) {
            throw StructError("ubyte format requires 0 <= number <= 255");
        }

        buf.put((byte) val);
    }

    private static void np_char(ByteBuffer buf, PyObject v) {
        String s = ((PyBytes) v).getString();
        s.chars().forEach(c -> buf.put((byte) c));
    }

    private static void np_short(ByteBuffer buf, PyObject v) {
        int val = get_int(v);
        if (val > Short.MAX_VALUE || val < Short.MIN_VALUE) {
            throw StructError("short format requires (-0x7fff - 1) <= number <= 0x7fff");
        }
        buf.putShort((short) val);
    }

    private static void np_ushort(ByteBuffer buf, PyObject v) {
        int val = get_int(v);
        if (val > 0xFFFF || val < 0) {
            throw StructError("ushort format requires 0 <= number <= (0x7fff * 2 + 1)");
        }
        buf.putShort((short) val);
    }

    private static void np_halffloat(ByteBuffer buf, PyObject v) {
        double x = v.asDouble();
        boolean sign;
        int e;
        double f;
        short bits;
        if (x == 0.0) {
            sign = (Math.copySign(1.0, x) == -1.0);
            e = 0;
            bits = 0;
        } else if (Double.isInfinite(x)) {
            sign = x < 0.0;
            e = 0x1f;
            bits = 0;
        } else if (Double.isNaN(x)) {
            sign = Math.copySign(1.0, x) == -1.0;
            e = 0x1f;
            bits = 512;
        } else {
            sign = (x < 0.0);
            if (sign) {
                x = -x;
            }
            FRexpResult r = frexp(x);
            f = r.mantissa;
            e = r.exponent;
            /* Normalize f to be in range [1.0, 2.0) */
            f *= 2.0;
            e--;
            if (e >= 16) {
                throw Py.OverflowError("float to large to pack with e format");
            } else if (e < -25) {
                /* |x| < 2**-25. Underflow to zero. */
                f = 0.0;
                e = 0;
            } else if (e < -14) {
                /* |x| < 2**-14. Gradual underflow */
                f = Math.scalb(f, 14 + e);
                e = 0;
            } else /* if (!(e == 0 && f == 0.0)) */ {
                e += 15;
                f -= 1.0; /* Get rid of leading 1 */
            }

            f *= 1024.0; /* 2**10 */
            /* Round to even */
            bits = (short) f;
            assert(bits < 1024);
            assert(e < 31);
            if ((f - bits > 0.5) || ((f - bits == 0.5) && (bits % 2 == 1))) {
                ++bits;
                if (bits == 1024) {
                    /* The carry propagated out of a string of 10 1 bits. */
                    bits = 0;
                    ++e;
                    if (e == 31) {
                        throw Py.OverflowError("float to large to pack with e format");
                    }
                }
            }
        }
        bits |= (e << 10) | ((sign ? 1 : 0) << 15);
        /* Write out result. */
        int firstbyte = (bits >> 8) & 0xFF;
        int secondbyte = bits & 0xFF;
        if (buf.order() == ByteOrder.LITTLE_ENDIAN) {
            buf.put((byte) secondbyte);
            buf.put((byte) firstbyte);
        } else {
            buf.put((byte) firstbyte);
            buf.put((byte) secondbyte);
        }
    }

    static class FRexpResult {
        int exponent = 0;
        double mantissa = 0.;
    }

    private static FRexpResult frexp(double value) {
        final FRexpResult result = new FRexpResult();
        long bits = Double.doubleToLongBits(value);
        double realMant = 1.;

        // Test for NaN, infinity, and zero.
        if (Double.isNaN(value) ||
                value + value == value ||
                Double.isInfinite(value))
        {
            result.exponent = 0;
            result.mantissa = value;
        }
        else
        {

            boolean neg = (bits < 0);
            int exponent = (int)((bits >> 52) & 0x7ffL);
            long mantissa = bits & 0xfffffffffffffL;

            if(exponent == 0)
            {
                exponent++;
            }
            else
            {
                mantissa = mantissa | (1L<<52);
            }

            // bias the exponent - actually biased by 1023.
            // we are treating the mantissa as m.0 instead of 0.m
            //  so subtract another 52.
            exponent -= 1075;
            realMant = mantissa;

            // normalize
            while(realMant >= 1.0)
            {
                mantissa >>= 1;
                realMant /= 2.;
                exponent++;
            }

            if(neg)
            {
                realMant = realMant * -1;
            }

            result.exponent = exponent;
            result.mantissa = realMant;
        }
        return result;
    }

    private static void np_int(ByteBuffer buf, PyObject v) {
        long val = get_long(v);
        if (val < Integer.MIN_VALUE || val > Integer.MAX_VALUE) {
            throw StructError("argument out of range");
        }
        buf.putInt((int) val);
    }

    private static void np_uint(ByteBuffer buf, PyObject v) {
        long val = get_long(v);
        if (val < 0 || val > 0xFFFFFFFFL) {
            throw StructError("argument out of range");
        }
        buf.putInt((int) val);
    }

    private static void np_long(ByteBuffer buf, PyObject v) {
        long lvalue = get_long(v);
        buf.putLong(lvalue);
    }

    private static void np_ulong(ByteBuffer buf, PyObject v) {
        BigInteger bi = get_ulong(v);
        if (bi.signum() < 0) {
            throw StructError("can't convert negative long to unsigned");
        }
        long lvalue = bi.longValue(); // underflow is OK -- the bits are correct
        buf.putLong(lvalue);
    }

    private static void np_size_t(ByteBuffer buf, PyObject v) {
        if ((v instanceof PyLong)) {
            try {
                long val = ((PyLong) v).getValue().longValueExact();
                buf.putLong(val);
            } catch (ArithmeticException e) {
                throw Py.OverflowError("argument out of range");
            }
        } else if(v.isIndex()) {
            buf.putLong(v.asIndex());
        } else {
            throw StructError("required argument is not an integer");
        }
    }

    private static void np_ssize_t(ByteBuffer buf, PyObject v) {
        if ((v instanceof PyLong)) {
            try {
                BigInteger val = ((PyLong) v).getValue();
                if (val.signum() < 0 || val.bitLength() > 0x40) {
                    throw Py.OverflowError("argument out of range");
                }
                buf.putLong(val.longValue());
            } catch (ArithmeticException e) {
                throw Py.OverflowError("argument out of range");
            }
        } else if(v.isIndex()) {
            long val = v.asIndex();
            if (val < 0) {
                throw Py.OverflowError("argument out of range");
            }
            buf.putLong(val);
        } else {
            throw StructError("required argument is not an integer");
        }
    }

    private static void np_float(ByteBuffer buf, PyObject val) {
        float v = (float) ((PyFloat) val).getValue();
        if (Float.isInfinite(v)) {
            throw Py.OverflowError("float is too big to pack with f format");
        }
        buf.putFloat((float) v);
    }

    private static void np_double(ByteBuffer buf, PyObject v) {
        buf.putDouble(get_double(v));
    }

    private static void np_bool(ByteBuffer buf, PyObject v) {
        buf.put(v.__bool__() ? (byte) 1 : 0);
    }


    private static FormatDef f_byte  = new FormatDef(1, FormatDef::np_byte, FormatDef::nu_byte),
            f_empty = new FormatDef(1, null, null),
            f_bool = new FormatDef(1, FormatDef::np_bool, FormatDef::nu_bool),
            f_ubyte = new FormatDef(1, FormatDef::np_ubyte, FormatDef::nu_ubyte),
            f_char  = new FormatDef(1, FormatDef::np_char, FormatDef::nu_char),
            f_short  = new FormatDef(2, FormatDef::np_short, FormatDef::nu_short),
            f_ushort  = new FormatDef(2, FormatDef::np_ushort, FormatDef::nu_ushort),
            f_halffloat  = new FormatDef(2, FormatDef::np_halffloat, FormatDef::nu_halffloat),
            f_int  = new FormatDef(4, FormatDef::np_int, FormatDef::nu_int),
            f_uint  = new FormatDef(4, FormatDef::np_uint, FormatDef::nu_uint),
            f_long  = new FormatDef(8, FormatDef::np_long, FormatDef::nu_long),
            f_ulong  = new FormatDef(8, FormatDef::np_ulong, FormatDef::nu_ulong),
            f_size_t  = new FormatDef(8, FormatDef::np_size_t, FormatDef::nu_size_t),
            f_ssize_t  = new FormatDef(8, FormatDef::np_ssize_t, FormatDef::nu_ulong),
            f_float  = new FormatDef(4, FormatDef::np_float, FormatDef::nu_float),
            f_double  = new FormatDef(8, FormatDef::np_double, FormatDef::nu_double),
            f_pointer  = new FormatDef(8, FormatDef::np_long, FormatDef::nu_long);
}


