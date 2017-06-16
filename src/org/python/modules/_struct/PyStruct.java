package org.python.modules._struct;

import org.python.core.ArgParser;
import org.python.core.Py;
import org.python.core.PyArray;
import org.python.core.PyByteArray;
import org.python.core.PyBytes;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.core.Untraversable;
import org.python.expose.ExposedGet;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedNew;
import org.python.expose.ExposedType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.python.modules._struct._struct.StructError;

@Untraversable
@ExposedType(name = "struct.Struct", base = PyObject.class)
public class PyStruct extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyStruct.class);
    
    @ExposedGet
    public String format;
    
    @ExposedGet
    public int size;
    public int len;
    FormatCode[] codes;
    private ByteOrder byteOrder;

    private PyObject weakreflist;

    @ExposedGet(name = "__class__")
    @Override
    public PyType getType() {
        return TYPE;
    }

    public PyStruct(String format) {
        super(TYPE);
        Struct___init__(format);
    }

    @ExposedNew
    final static PyObject Struct___new__(PyNewWrapper new_, boolean init,
            PyType subtype, PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("Struct", args, keywords, new String[] {"format"}, 1);

        PyObject format = ap.getPyObject(0);
        if ((format instanceof PyUnicode)) {
            return new PyStruct(((PyUnicode) format).getString());
        }
        throw Py.TypeError("coercing to Unicode: need string, '"
                + format.getType().fastGetName() + "' type found");
    }

    private static int align(int size, FormatDef e) {
        if (e.size != 0) {
            size = ((size + e.size - 1)
                                / e.size)
                                * e.size;
        }
        return size;
    }

    private final PyObject Struct___init__(String fmt) {
        this.format = fmt;
        int size = 0;
        int len = 0;

        int f_len = fmt.length();
        List<FormatCode> codes = new ArrayList<>();

        for (int j = 0; j < f_len; j++) {
            char c = fmt.charAt(j);
            if (j == 0 && (c=='@' || c=='<' || c=='>' || c=='=' || c=='!'))
                continue;
            if (Character.isWhitespace(c))
                continue;
            int num = 1;
            if (Character.isDigit(c)) {
                num = Character.digit(c, 10);
                while (++j < f_len &&
                          Character.isDigit((c = fmt.charAt(j)))) {
                    int x = num*10 + Character.digit(c, 10);
                    if (x/10 != num)
                        throw StructError("overflow in item count");
                    num = x;
                }
                if (j >= f_len) {
                    throw StructError("repeat count given without format specifier");
                }
            }

            FormatDef e = FormatDef.getentry(c);
            codes.add(new FormatCode(e, num, c));
            switch(c) {
                case 's':
                case 'p':
                    len++;
                    break;
                case 'x':
                    break;
                default:
                    len += num;
                    break;
            }

            int itemsize = e.size;
            size = align(size, e);
            int x = num * itemsize;
            size += x;
            if (x/itemsize != num || size < 0)
                throw StructError("total struct size too long");
        }
        this.byteOrder = FormatDef.byteOrder(fmt);
        this.size = size;
        this.len = len;
        this.codes = codes.toArray(new FormatCode[0]);

        return this;
    }

    @ExposedMethod(names = "iter_unpack")
    public PyObject iter_unpack(PyObject buffer) {
        return new PyUnpackIterator(this, buffer);
    }

    @ExposedMethod(names = "pack")
    public PyObject pack(PyObject[] args, String[] kwds) {
        if (len != args.length) {
            throw StructError(String.format("pack expected %d items for packing (got %d)", len, args.length));
        }
        return new PyBytes(pack(args));
    }

    private ByteBuffer pack(PyObject[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.order(byteOrder);
        int i = 0;
        for (FormatCode code: codes) {
            if (code.format == 'x') {
                for (int j = 0; j < code.repeat; j++) {
                    buffer.put((byte) 0);
                }
                continue;
            }
            PyObject v = args[i++];
            if (code.format == 's') {
                if (!(v instanceof PyBytes || v instanceof PyByteArray)) {
                    throw StructError("argument for 's' must be a bytes object");
                }
                byte[] bytes = v.asString().getBytes();
                buffer.put(Arrays.copyOfRange(bytes, 0, Math.min(bytes.length, code.repeat)));
            } else if (code.format == 'p') {
                if (!(v instanceof PyBytes || v instanceof PyByteArray)) {
                    throw StructError("argument for 's' must be a bytes object");
                }
                byte[] bytes = v.asString().getBytes();
                int n = Math.min(0xFF, bytes.length);
                buffer.put((byte) n);
                buffer.put(Arrays.copyOfRange(bytes, 0, Math.min(n, code.repeat - 1)));
            } else {
                for (int j = 0; j < code.repeat; j++) {
                    code.fmtdef.pack(buffer, v);
                }
            }
        }
        return buffer;
    }
    
    @ExposedMethod
    final void pack_into(PyObject[] args, String[] kwds) {
        PyArray buffer = (PyArray)args[0];
        int n = buffer.__len__();
        if (n < size) {
            throw StructError(String.format("pack_into requires a buffer of at least %d bytes", size));
        }
        int offset = args[1].asInt();
        ByteBuffer buf = pack(Arrays.copyOfRange(args, 2, args.length));
        int i = 0;
        for (byte b: buf.array()) {
            buffer.set(offset + i++, b);
        }
//        if (args.length - argstart < 2)
//            Py.TypeError("illegal argument type for built-in operation");
//        if (!(args[argstart] instanceof PyArray)) {
//            throw Py.TypeError("pack_into takes an array arg"); // as well as a buffer, what else?
//        }
//        PyArray buffer = (PyArray)args[argstart];
//        int offset = args[argstart + 1].asInt();
//
//        ByteStream res = pack(format, f, size, argstart + 2, args);
//        if (res.pos > buffer.__len__()) {
//            throw StructError("pack_into requires a buffer of at least " + res.pos + " bytes, got " + buffer.__len__());
//        }
//        for (int i = 0; i < res.pos; i++, offset++) {
//            byte val = res.data[i];
//            buffer.set(offset, val);
//        }
    }
  
    @ExposedMethod
    public PyTuple unpack(PyObject source) {
        byte[] bytes = Py.unwrapBuffer(source);
        if (size != bytes.length)
            throw StructError("unpack str size does not match format");
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        return unpack(buf);
    }

    private PyTuple unpack(ByteBuffer buf) {
        buf.order(byteOrder);
        PyObject[] results = new PyObject[len];
        int i = 0;
        for (FormatCode code: codes) {
            if (code.repeat == 0) {
                results[i++] = Py.EmptyByte;
                continue;
            }
            if (code.format == 'x') {
                buf.get(new byte[code.repeat]);
            } else if (code.format == 's') {
                byte[] bytes = new byte[code.repeat];
                buf.get(bytes);
                results[i++] = new PyBytes(bytes);
            } else {
                for (int j = 0; j < code.repeat; j++) {
                    results[i++] = code.fmtdef.unpack(buf);
                }
            }
        }
        return new PyTuple(results);
    }
    
    @ExposedMethod(defaults = {"0"})
    public PyTuple unpack_from(PyObject string, int offset) {
        byte[] bytes = Py.unwrapBuffer(string);
        if (size <= (bytes.length - offset + 1))
            throw _struct.StructError("unpack_from str size does not match format");
        ByteBuffer buf = ByteBuffer.wrap(bytes, offset, size);
        return unpack(buf);
    }
}
