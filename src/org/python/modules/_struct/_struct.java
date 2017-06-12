/*
 * Copyright 1999 Finn Bock.
 *
 * This program contains material copyrighted by:
 * Copyright 1991-1995 by Stichting Mathematisch Centrum, Amsterdam,
 * The Netherlands.
 *
 */
package org.python.modules._struct;

import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyArray;
import org.python.core.PyBytes;
import org.python.core.PyException;
import org.python.core.PyFloat;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.core.PyTuple;
import org.python.expose.ExposedFunction;
import org.python.expose.ExposedModule;
import org.python.expose.ModuleInit;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@ExposedModule(doc = BuiltinDocs._struct_doc)
public class _struct {

    /**
     * Exception raised on various occasions; argument is a
     * string describing what is wrong.
     */
    public static final PyObject error = Py.makeClass("error", exceptionNamespace(), Py.Exception);

    @ModuleInit
    public static void init(PyObject dict) {
        dict.__setitem__("error", error);
        dict.__setitem__("Struct", PyStruct.TYPE);
    }

    static class FormatDef {
        char name;
        int size;
        int alignment;

        FormatDef init(char name, int size, int alignment) {
            this.name = name;
            this.size = size;
            this.alignment = alignment;
            return this;
        }

        void pack(ByteStream buf, PyObject value)  {}

        Object unpack(ByteStream buf) {
            return null;
        }

        int doPack(ByteStream buf, int count, int pos, PyObject[] args) {
            if (pos + count > args.length)
                throw StructError("insufficient arguments to pack");

            int cnt = count;
            while (count-- > 0)
                pack(buf, args[pos++]);
            return cnt;
        }

        void doUnpack(ByteStream buf, int count, List list) {
            while (count-- > 0)
                list.add(Py.java2py(unpack(buf)));
        }


        int get_int(PyObject value) {
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

        long get_long(PyObject value) {
            if (value instanceof PyLong){
                try {
                    return ((PyLong) value).getValue().longValueExact();
                } catch (ArithmeticException e) {
                    throw StructError("argument out of range");
                }
            } else
                return get_int(value);
        }

        BigInteger get_ulong(PyObject value) {
            if (value instanceof PyLong){
                BigInteger v = ((PyLong) value).getValue();
                if (v.compareTo(PyLong.MAX_ULONG) > 0){
                    throw StructError("unsigned long int too long to convert");
                }
                return v;
            } else
                return BigInteger.valueOf(get_int(value));
        }

        double get_float(PyObject value) {
            return value.asDouble();
        }


        void BEwriteInt(ByteStream buf, int v) {
            buf.writeByte((v >>> 24) & 0xFF);
            buf.writeByte((v >>> 16) & 0xFF);
            buf.writeByte((v >>>  8) & 0xFF);
            buf.writeByte((v >>>  0) & 0xFF);
        }

        void LEwriteInt(ByteStream buf, int v) {
            buf.writeByte((v >>>  0) & 0xFF);
            buf.writeByte((v >>>  8) & 0xFF);
            buf.writeByte((v >>> 16) & 0xFF);
            buf.writeByte((v >>> 24) & 0xFF);
        }

        int BEreadInt(ByteStream buf) {
            int b1 = buf.readByte();
            int b2 = buf.readByte();
            int b3 = buf.readByte();
            int b4 = buf.readByte();
            return ((b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0));
        }

        int LEreadInt(ByteStream buf) {
            int b1 = buf.readByte();
            int b2 = buf.readByte();
            int b3 = buf.readByte();
            int b4 = buf.readByte();
            return ((b1 << 0) + (b2 << 8) + (b3 << 16) + (b4 << 24));
        }
    }


    static class ByteStream {
        byte[] data;
        int len;
        int pos;

        ByteStream() {
            data = new byte[10];
            len = 0;
            pos = 0;
        }

        ByteStream(byte[] bytes) {
            data = bytes;
            len = bytes.length;
            pos = 0;
        }

        ByteStream(String s) {
            this(s, 0);
        }
        
        ByteStream(String s, int offset) {
            int size = s.length() - offset;
            data = s.getBytes();
            len = size;
            pos = 0;
        }

        int readByte() {
            return data[pos++] & 0xFF;
        }

        void read(char[] buf, int pos, int len) {
            System.arraycopy(data, this.pos, buf, pos, len);
            this.pos += len;
        }


        String readString(int l) {
            char[] data = new char[l];
            read(data, 0, l);
            return new String(data);
        }


        private void ensureCapacity(int l) {
            if (pos + l >= data.length) {
                byte[] b = new byte[(pos + l) * 2];
                System.arraycopy(data, 0, b, 0, pos);
                data = b;
            }
        }


        void writeByte(int b) {
            ensureCapacity(1);
            data[pos++] = (byte)(b & 0xFF);
        }


        void write(char[] buf, int pos, int len) {
            ensureCapacity(len);
            System.arraycopy(buf, pos, data, this.pos, len);
            this.pos += len;
        }

        void writeString(String s, int pos, int len) {
            char[] data = new char[len];
            s.getChars(pos, len, data, 0);
            write(data, 0, len);
        }


        int skip(int l) {
            pos += l;
            return pos;
        }

        int size() {
            return pos;
        }

        public String toString() {
            return new String(data, 0, pos);
        }
    }


    static class PadFormatDef extends FormatDef {
        int doPack(ByteStream buf, int count, int pos, PyObject[] args) {
            while (count-- > 0)
                buf.writeByte(0);
            return 0;
        }

        void doUnpack(ByteStream buf, int count, PyList list) {
            while (count-- > 0)
                buf.readByte();
        }
    }


    static class StringFormatDef extends FormatDef {
        int doPack(ByteStream buf, int count, int pos, PyObject[] args) {
            PyObject value = args[pos];

            if (!(value instanceof PyBytes))
                throw StructError("argument for 's' must be a string");

            String s = value.toString();
            int len = s.length();
            buf.writeString(s, 0, Math.min(count, len));
            if (len < count) {
                count -= len;
                for (int i = 0; i < count; i++)
                    buf.writeByte(0);
            }
            return 1;
        }

        void doUnpack(ByteStream buf, int count, PyList list) {
            list.append(Py.newString(buf.readString(count)));
        }
    }


    static class PascalStringFormatDef extends StringFormatDef {
        int doPack(ByteStream buf, int count, int pos, PyObject[] args) {
            PyObject value = args[pos];

            if (!(value instanceof PyBytes))
                throw StructError("argument for 'p' must be a string");

            buf.writeByte(Math.min(0xFF, Math.min(value.toString().length(), count-1)));
            return super.doPack(buf, count-1, pos, args);
        }

        void doUnpack(ByteStream buf, int count, PyList list) {
            int n = buf.readByte();
            if (n >= count)
                n = count-1;
            super.doUnpack(buf, n, list);
            buf.skip(Math.max(count-n-1, 0));
        }
    }


    static class CharFormatDef extends FormatDef {
        void pack(ByteStream buf, PyObject value) {
            if (!(value instanceof PyBytes) || value.__len__() != 1)
                throw StructError("char format require string of length 1");
            buf.writeByte(value.toString().charAt(0));
        }

        Object unpack(ByteStream buf) {
            return Py.newString((char)buf.readByte());
        }
    }


    static class ByteFormatDef extends FormatDef {
        void pack(ByteStream buf, PyObject value) {
            int val = get_int(value);
            if (val > Byte.MAX_VALUE || val < Byte.MIN_VALUE) {
                throw StructError("byte format requires -128 <= number <= 127");
            }
            buf.writeByte(val);
        }

        Object unpack(ByteStream buf) {
            int b = buf.readByte();
            if (b > Byte.MAX_VALUE)
                b -= 0x100;
            return Py.newInteger(b);
        }
    }

    static class UnsignedByteFormatDef extends ByteFormatDef {
        void pack(ByteStream buf, PyObject value) {
            int val = get_int(value);
            if (val < 0 || val > 0xFF) {
                throw StructError("ubyte format requires 0 <= number <= 255");
            }

            buf.writeByte(val);
        }

        Object unpack(ByteStream buf) {
            return Py.newInteger(buf.readByte());
        }
    }
    
    static class PointerFormatDef extends FormatDef {
        FormatDef init(char name) {
            String dataModel = System.getProperty("sun.arch.data.model");
            if (dataModel == null)
                throw Py.NotImplementedError("Can't determine if JVM is 32- or 64-bit");
            int length = dataModel.equals("64") ? 8 : 4;
            super.init(name, length, length);
            return this;
        }
        
        void pack(ByteStream buf, PyObject value) {
//            throw Py.NotImplementedError("Pointer packing/unpacking not implemented in Jython");
            int v = get_int(value);
            buf.writeByte(v & 0xFF);
            buf.writeByte((v >> 8) & 0xFF);
        }

        Object unpack(ByteStream buf) {
            throw Py.NotImplementedError("Pointer packing/unpacking not implemented in Jython");
        }
    }
    
    static class LEShortFormatDef extends FormatDef {
        void pack(ByteStream buf, PyObject value) {
            int v = get_int(value);
            if (v > Short.MAX_VALUE || v < Short.MIN_VALUE) {
                throw StructError("short format requires (-0x7fff - 1) <= number <= 0x7fff");
            }
            buf.writeByte(v & 0xFF);
            buf.writeByte((v >> 8) & 0xFF);
        }

        Object unpack(ByteStream buf) {
            int v = buf.readByte() |
                   (buf.readByte() << 8);
            if (v > Short.MAX_VALUE)
                v -= 0x10000 ;
            return Py.newInteger(v);
        }
    }

    static class LEUnsignedShortFormatDef extends FormatDef {
        void pack(ByteStream buf, PyObject value) {
            int v = get_int(value);
            if (v > 0xFFFF || v < 0) {
                throw StructError("ushort format requires 0 <= number <= (0x7fff * 2 + 1)");
            }
            buf.writeByte(v & 0xFF);
            buf.writeByte((v >> 8) & 0xFF);
        }

        Object unpack(ByteStream buf) {
            int v = buf.readByte() |
                   (buf.readByte() << 8);
            return Py.newInteger(v);
        }
    }


    static class BEShortFormatDef extends FormatDef {
        void pack(ByteStream buf, PyObject value) {
            int v = get_int(value);
            if (v > Short.MAX_VALUE || v < Short.MIN_VALUE) {
                throw StructError("short format requires (-0x7fff - 1) <= number <= 0x7fff");
            }
            buf.writeByte((v >> 8) & 0xFF);
            buf.writeByte(v & 0xFF);
        }

        Object unpack(ByteStream buf) {
            int v = (buf.readByte() << 8) |
                     buf.readByte();
            if (v > Short.MAX_VALUE)
                v -= 0x10000;
            return Py.newInteger(v);
        }
    }


    static class BEUnsignedShortFormatDef extends FormatDef {
        void pack(ByteStream buf, PyObject value) {
            int v = get_int(value);
            if (v > 0xFFFF || v < 0) {
                throw StructError("ushort format requires 0 <= number <= (0x7fff * 2 + 1)");
            }
            buf.writeByte((v >> 8) & 0xFF);
            buf.writeByte(v & 0xFF);
        }

        Object unpack(ByteStream buf) {
            int v = (buf.readByte() << 8) |
                     buf.readByte();
            return Py.newInteger(v);
        }
    }


    static class LEIntFormatDef extends FormatDef {
        void pack(ByteStream buf, PyObject value) {
            LEwriteInt(buf, get_int(value));
        }

        Object unpack(ByteStream buf) {
            int v = LEreadInt(buf);
            return Py.newInteger(v);
        }
    }


    static class LEUnsignedIntFormatDef extends FormatDef {
        void pack(ByteStream buf, PyObject value) {
            long v = get_long(value);
            if (v < 0 || v > 0xFFFFFFFFL) {
                throw StructError("argument out of range");
            }
            LEwriteInt(buf, (int)(v & 0xFFFFFFFF));
        }

        Object unpack(ByteStream buf) {
            long v = LEreadInt(buf);
            if (v < 0)
                v += 0x100000000L;
            return new PyLong(v);
        }
    }


    static class BEIntFormatDef extends FormatDef {
        void pack(ByteStream buf, PyObject value) {
            BEwriteInt(buf, get_int(value));
        }

        Object unpack(ByteStream buf) {
            return Py.newInteger(BEreadInt(buf));
        }
    }


    static class BEUnsignedIntFormatDef extends FormatDef {
        void pack(ByteStream buf, PyObject value) {
            long v = get_long(value);
            if (v < 0 || v > 0xFFFFFFFFL) {
                throw StructError("argument out of range");
            }
            BEwriteInt(buf, (int)(v & 0xFFFFFFFF));
        }

        Object unpack(ByteStream buf) {
            long v = BEreadInt(buf);
            if (v < 0)
                v += 0x100000000L;
            return new PyLong(v);
        }
    }

    static class LEUnsignedLongFormatDef extends FormatDef {
        void pack(ByteStream buf, PyObject value) {
            BigInteger bi = get_ulong(value);
            if (bi.compareTo(BigInteger.valueOf(0)) < 0) {
                throw StructError("can't convert negative long to unsigned");
            }
            long lvalue = bi.longValue(); // underflow is OK -- the bits are correct
            int high    = (int) ( (lvalue & 0xFFFFFFFF00000000L)>>32 );
            int low     = (int) ( lvalue & 0x00000000FFFFFFFFL );
            LEwriteInt( buf, low );
            LEwriteInt( buf, high );
        }

        Object unpack(ByteStream buf) {
            long low       = ( LEreadInt( buf ) & 0X00000000FFFFFFFFL );
            long high      = ( LEreadInt( buf ) & 0X00000000FFFFFFFFL );
                java.math.BigInteger result=java.math.BigInteger.valueOf(high);
            result=result.multiply(java.math.BigInteger.valueOf(0x100000000L));
            result=result.add(java.math.BigInteger.valueOf(low));
            return new PyLong(result);
        }
    }


    static class BEUnsignedLongFormatDef extends FormatDef {
        void pack(ByteStream buf, PyObject value) {
            BigInteger bi = get_ulong(value);
            if (bi.compareTo(BigInteger.ZERO) < 0) {
                throw StructError("can't convert negative long to unsigned");
            }
            long lvalue = bi.longValue(); // underflow is OK -- the bits are correct
            int high    = (int) ( (lvalue & 0xFFFFFFFF00000000L)>>32 );
            int low     = (int) ( lvalue & 0x00000000FFFFFFFFL );
            BEwriteInt( buf, high );
            BEwriteInt( buf, low );
        }

        Object unpack(ByteStream buf) {
            long high      = ( BEreadInt( buf ) & 0X00000000FFFFFFFFL );
            long low       = ( BEreadInt( buf ) & 0X00000000FFFFFFFFL );
            java.math.BigInteger result=java.math.BigInteger.valueOf(high);
            result=result.multiply(java.math.BigInteger.valueOf(0x100000000L));
            result=result.add(java.math.BigInteger.valueOf(low));
            return new PyLong(result);
        }
    }


    static class LELongFormatDef extends FormatDef {
        void pack(ByteStream buf, PyObject value) {
            long lvalue  = get_long( value );
            int high    = (int) ( (lvalue & 0xFFFFFFFF00000000L)>>32 );
            int low     = (int) ( lvalue & 0x00000000FFFFFFFFL );
            LEwriteInt( buf, low );
            LEwriteInt( buf, high );
        }

        Object unpack(ByteStream buf) {
            long low = LEreadInt(buf) & 0x00000000FFFFFFFFL;
            long high = ((long)(LEreadInt(buf))<<32) & 0xFFFFFFFF00000000L;
            long result=(high|low);
            return new PyLong(result);
        }
    }


    static class BELongFormatDef extends FormatDef {
        void pack(ByteStream buf, PyObject value) {
            long lvalue  = get_long( value );
            int high    = (int) ( (lvalue & 0xFFFFFFFF00000000L)>>32 );
            int low     = (int) ( lvalue & 0x00000000FFFFFFFFL );
            BEwriteInt( buf, high );
            BEwriteInt( buf, low );
        }

        Object unpack(ByteStream buf) {
            long high = ((long)(BEreadInt(buf))<<32) & 0xFFFFFFFF00000000L;
            long low = BEreadInt(buf) & 0x00000000FFFFFFFFL;
            long result=(high|low);
            return new PyLong(result);
        }
    }


    static class LEFloatFormatDef extends FormatDef {
        void pack(ByteStream buf, PyObject value) {
            int bits = Float.floatToIntBits((float)get_float(value));
            LEwriteInt(buf, bits);
        }

        Object unpack(ByteStream buf) {
            int bits = LEreadInt(buf);
            float v = Float.intBitsToFloat(bits);
            if (PyFloat.float_format == PyFloat.Format.UNKNOWN && (
                    Float.isInfinite(v) || Float.isNaN(v))) {
                throw Py.ValueError("can't unpack IEEE 754 special value on non-IEEE platform");
            }
            return Py.newFloat(v);
        }
    }

    static class LEDoubleFormatDef extends FormatDef {
        void pack(ByteStream buf, PyObject value) {
            long bits = Double.doubleToLongBits(get_float(value));
            LEwriteInt(buf, (int)(bits & 0xFFFFFFFF));
            LEwriteInt(buf, (int)(bits >>> 32));
        }

        Object unpack(ByteStream buf) {
            long bits = (LEreadInt(buf) & 0xFFFFFFFFL) +
                        (((long)LEreadInt(buf)) << 32);
            double v = Double.longBitsToDouble(bits);
            if (PyFloat.double_format == PyFloat.Format.UNKNOWN &&
                    (Double.isInfinite(v) || Double.isNaN(v))) {
                throw Py.ValueError("can't unpack IEEE 754 special value on non-IEEE platform");
            }
            return Py.newFloat(v);
        }
    }


    static class BEFloatFormatDef extends FormatDef {
        void pack(ByteStream buf, PyObject value) {
            int bits = Float.floatToIntBits((float)get_float(value));
            BEwriteInt(buf, bits);
        }

        Object unpack(ByteStream buf) {
            int bits = BEreadInt(buf);
            float v = Float.intBitsToFloat(bits);
            if (PyFloat.float_format == PyFloat.Format.UNKNOWN && (
                    Float.isInfinite(v) || Float.isNaN(v))) {
                throw Py.ValueError("can't unpack IEEE 754 special value on non-IEEE platform");
            }
            return Py.newFloat(v);
        }
    }

    static class BEDoubleFormatDef extends FormatDef {
        void pack(ByteStream buf, PyObject value) {
            long bits = Double.doubleToLongBits(get_float(value));
            BEwriteInt(buf, (int)(bits >>> 32));
            BEwriteInt(buf, (int)(bits & 0xFFFFFFFF));
        }

        Object unpack(ByteStream buf) {
            long bits = (((long) BEreadInt(buf)) << 32) +
                    (BEreadInt(buf) & 0xFFFFFFFFL);
            double v = Double.longBitsToDouble(bits);
            if (PyFloat.double_format == PyFloat.Format.UNKNOWN &&
                    (Double.isInfinite(v) || Double.isNaN(v))) {
                throw Py.ValueError("can't unpack IEEE 754 special value on non-IEEE platform");
            }
            return Py.newFloat(v);
        }
    }


    private static FormatDef[] lilendian_table = {
        new PadFormatDef()              .init('x', 1, 0),
        new ByteFormatDef()             .init('b', 1, 0),
        new UnsignedByteFormatDef()     .init('B', 1, 0),
        new CharFormatDef()             .init('c', 1, 0),
        new StringFormatDef()           .init('s', 1, 0),
        new PascalStringFormatDef()     .init('p', 1, 0),
        new LEShortFormatDef()          .init('h', 2, 0),
        new LEUnsignedShortFormatDef()  .init('H', 2, 0),
        new LEIntFormatDef()            .init('i', 4, 0),
        new LEUnsignedIntFormatDef()    .init('I', 4, 0),
        new LEIntFormatDef()            .init('l', 4, 0),
        new LEUnsignedIntFormatDef()    .init('L', 4, 0),
        new LELongFormatDef()           .init('q', 8, 0),
        new LEUnsignedLongFormatDef()   .init('Q', 8, 0),
        new LEFloatFormatDef()          .init('f', 4, 0),
        new LEDoubleFormatDef()         .init('d', 8, 0),
    };

    private static FormatDef[] bigendian_table = {
        new PadFormatDef()              .init('x', 1, 0),
        new ByteFormatDef()             .init('b', 1, 0),
        new UnsignedByteFormatDef()     .init('B', 1, 0),
        new CharFormatDef()             .init('c', 1, 0),
        new StringFormatDef()           .init('s', 1, 0),
        new PascalStringFormatDef()     .init('p', 1, 0),
        new BEShortFormatDef()          .init('h', 2, 0),
        new BEUnsignedShortFormatDef()  .init('H', 2, 0),
        new BEIntFormatDef()            .init('i', 4, 0),
        new BEUnsignedIntFormatDef()    .init('I', 4, 0),
        new BEIntFormatDef()            .init('l', 4, 0),
        new BEUnsignedIntFormatDef()    .init('L', 4, 0),
        new BELongFormatDef()           .init('q', 8, 0),
        new BEUnsignedLongFormatDef()   .init('Q', 8, 0),
        new BEFloatFormatDef()          .init('f', 4, 0),
        new BEDoubleFormatDef()         .init('d', 8, 0),
    };

    private static FormatDef[] native_table = {
        new PadFormatDef()              .init('x', 1, 0),
        new ByteFormatDef()             .init('b', 1, 0),
        new UnsignedByteFormatDef()     .init('B', 1, 0),
        new CharFormatDef()             .init('c', 1, 0),
        new StringFormatDef()           .init('s', 1, 0),
        new PascalStringFormatDef()     .init('p', 1, 0),
        new BEShortFormatDef()          .init('h', 2, 2),
        new BEUnsignedShortFormatDef()  .init('H', 2, 2),
        new BEIntFormatDef()            .init('i', 4, 4),
        new BEUnsignedIntFormatDef()    .init('I', 4, 4),
        new BEIntFormatDef()            .init('l', 4, 4),
        new BEUnsignedIntFormatDef()    .init('L', 4, 4),
        new BELongFormatDef()           .init('q', 8, 8),
        new BEUnsignedLongFormatDef()   .init('Q', 8, 8),
        new BELongFormatDef()           .init('n', 8, 8),
        new BEUnsignedLongFormatDef()   .init('N', 8, 8),
        new BEFloatFormatDef()          .init('f', 4, 4),
        new BEDoubleFormatDef()         .init('d', 8, 8),
        new PointerFormatDef()          .init('P')
    };



    static FormatDef[] whichtable(String pfmt) {
        char c = pfmt.charAt(0);
        switch (c) {
        case '<' :
            return lilendian_table;
        case '>':
        case '!':
            // Network byte order is big-endian
            return bigendian_table;
        case '=':
            return bigendian_table;
        case '@':
        default:
            return native_table;
        }
    }


    private static FormatDef getentry(char c, FormatDef[] f) {
        for (int i = 0; i < f.length; i++) {
            if (f[i].name == c)
                return f[i];
        }
        throw StructError("bad char in struct format");
    }



    private static int align(int size, FormatDef e) {
        if (e.alignment != 0) {
            size = ((size + e.alignment - 1)
                                / e.alignment)
                                * e.alignment;
        }
        return size;
    }



    static int calcsize(String format, FormatDef[] f) {
        int size = 0;

        int len = format.length();
        for (int j = 0; j < len; j++) {
            char c = format.charAt(j);
            if (j == 0 && (c=='@' || c=='<' || c=='>' || c=='=' || c=='!'))
                continue;
            if (Character.isWhitespace(c))
                continue;
            int num = 1;
            if (Character.isDigit(c)) {
                num = Character.digit(c, 10);
                while (++j < len &&
                          Character.isDigit((c = format.charAt(j)))) {
                    int x = num*10 + Character.digit(c, 10);
                    if (x/10 != num)
                        throw StructError("overflow in item count");
                    num = x;
                }
                if (j >= len)
                    break;
            }

            FormatDef e = getentry(c, f);

            int itemsize = e.size;
            size = align(size, e);
            int x = num * itemsize;
            size += x;
            if (x/itemsize != num || size < 0)
                throw StructError("total struct size too long");
        }
        return size;
    }


    /**
     * Return the size of the struct (and hence of the string)
     * corresponding to the given format.
     */
    @ExposedFunction
    public static int calcsize(String format) {
        FormatDef[] f = whichtable(format);
        return calcsize(format, f);
    }

    /**
     * Return a string containing the values v1, v2, ... packed according
     * to the given format. The arguments must match the
     * values required by the format exactly.
     */
    @ExposedFunction
    public static PyBytes pack(PyObject[] args, String[] kws) {
        if (args.length < 1)
            Py.TypeError("illegal argument type for built-in operation");

        String format = args[0].toString();

        FormatDef[] f = whichtable(format);
        int size = calcsize(format, f);

        ByteStream bytes = pack(format, f, size, 1, args);
        return new PyBytes(bytes.data, 0, bytes.size());
    }
    
    // xxx - may need to consider doing a generic arg parser here
    @ExposedFunction
    static public void pack_into(PyObject[] args, String[] kws) {
        if (args.length < 3)
            Py.TypeError("illegal argument type for built-in operation");
        String format = args[0].toString();
        FormatDef[] f = whichtable(format);
        int size = calcsize(format, f);
        pack_into(format, f, size, 1, args);
    }
    
    static void pack_into(String format, FormatDef[] f, int size, int argstart, PyObject[] args) {
        if (args.length - argstart < 2)
            Py.TypeError("illegal argument type for built-in operation");
        if (!(args[argstart] instanceof PyArray)) {
            throw Py.TypeError("pack_into takes an array arg"); // as well as a buffer, what else?
        }
        PyArray buffer = (PyArray)args[argstart];
        int offset = args[argstart + 1].asInt();

        ByteStream res = pack(format, f, size, argstart + 2, args);
        if (res.pos > buffer.__len__()) {
            throw StructError("pack_into requires a buffer of at least " + res.pos + " bytes, got " + buffer.__len__());
        }
        for (int i = 0; i < res.pos; i++, offset++) {
            byte val = res.data[i];
            buffer.set(offset, val);
        }
    }
    
    static ByteStream pack(String format, FormatDef[] f, int size, int start, PyObject[] args) {
        ByteStream res = new ByteStream();

        int i = start;
        int len = format.length();
        for (int j = 0; j < len; j++) {
            char c = format.charAt(j);
            if (j == 0 && (c=='@' || c=='<' || c=='>' || c=='=' || c=='!'))
                continue;
            if (Character.isWhitespace(c))
                continue;
            int num = 1;
            if (Character.isDigit(c)) {
                num = Character.digit(c, 10);
                while (++j < len && Character.isDigit((c = format.charAt(j))))
                    num = num*10 + Character.digit(c, 10);
                if (j >= len)
                    break;
            }

            FormatDef e = getentry(c, f);

            // Fill pad bytes with zeros
            int nres = align(res.size(), e) - res.size();
            while (nres-- > 0)
                res.writeByte(0);
            i += e.doPack(res, num, i, args);
        }

        if (i < args.length)
            throw StructError("too many arguments for pack format");

        return res;
    }



    /**
     * Unpack the string (presumably packed by pack(fmt, ...)) according
     * to the given format. The result is a tuple even if it contains
     * exactly one item.
     * The string must contain exactly the amount of data required by
     * the format (i.e. len(string) must equal calcsize(fmt)).
     */

    public static PyTuple unpack(String format, String string) {
        FormatDef[] f = whichtable(format);
        int size = calcsize(format, f);
        int len = string.length();
        if (size != len) 
            throw StructError("unpack str size does not match format");
         return unpack(f, size, format, new ByteStream(string));
    }

    @ExposedFunction
    public static PyObject iter_unpack(String format, PyObject buffer) {
        return new PyUnpackIterator();
    }

    @ExposedFunction
    public static PyTuple unpack(String format, PyObject buffer) {
        byte[] string = Py.unwrapBuffer(buffer);
        FormatDef[] f = whichtable(format);
        int size = calcsize(format, f);
        int len = string.length;
        if (size != len) 
            throw StructError("unpack str size does not match format");
         return unpack(f, size, format, new ByteStream(string));
    }
    
    @ExposedFunction(defaults = {"0"})
    public static PyTuple unpack_from(String format, String string, int offset) {
        FormatDef[] f = whichtable(format);
        int size = calcsize(format, f);
        int len = string.length();
        if (size >= (len - offset + 1))
            throw StructError("unpack_from str size does not match format");
        return unpack(f, size, format, new ByteStream(string, offset));
    }

    @ExposedFunction
    public static PyObject _clearcache() {
        // noop
        return Py.None;
    }
    
    static PyTuple unpack(FormatDef[] f, int size, String format, ByteStream str) {
        List<PyObject> res = new ArrayList<>();
        int flen = format.length();
        for (int j = 0; j < flen; j++) {
            char c = format.charAt(j);
            if (j == 0 && (c=='@' || c=='<' || c=='>' || c=='=' || c=='!'))
                continue;
            if (Character.isWhitespace(c))
                continue;
            int num = 1;
            if (Character.isDigit(c)) {
                num = Character.digit(c, 10);
                while (++j < flen &&
                           Character.isDigit((c = format.charAt(j))))
                    num = num*10 + Character.digit(c, 10);
                if (j > flen)
                    break;
            }

            FormatDef e = getentry(c, f);

            str.skip(align(str.size(), e) - str.size());

            e.doUnpack(str, num, res);
        }
        return new PyTuple(res);
    }


    static PyException StructError(String explanation) {
        return new PyException(error, explanation);
    }

    private static PyObject exceptionNamespace() {
        PyObject dict = new PyStringMap();
        dict.__setitem__("__module__", new PyBytes("struct"));
        return dict;
    }
}

