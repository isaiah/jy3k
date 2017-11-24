package org.python.modules.array;

import org.python.annotations.ExposedClassMethod;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.core.ArgParser;
import org.python.core.BuiltinDocs;
import org.python.core.CompareOp;
import org.python.core.Py;
import org.python.core.PyBytes;
import org.python.core.PyLong;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.core.PyUnicode;

import java.nio.ByteBuffer;

@ExposedType(name = "array.array", doc = BuiltinDocs.array_array_doc)
public class PyArrayArray extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyArrayArray.class);
    private static final int INITIAL_CAPACITY = 128;
    protected MachineFormatCode formatCode;
    private ByteBuffer buf;

    public PyArrayArray(PyType subtype) {
        this(subtype, MachineFormatCode.SIGNED_INT8, INITIAL_CAPACITY);
    }

    public PyArrayArray(PyType type, MachineFormatCode code) {
        this(type, code, INITIAL_CAPACITY);
    }

    public PyArrayArray(PyType type, MachineFormatCode code, int size) {
        super(type);
        this.formatCode = code;
        this.buf = ByteBuffer.allocate(size);
    }

    @ExposedNew
    static PyObject _new(PyNewWrapper new_, boolean init, PyType subtype, PyObject[] args,
                              String[] keywords) {
        ArgParser ap = new ArgParser("array", args, keywords, "typecode", "initializer");
        char typecode = ap.getString(0).charAt(0);
        PyObject initial = ap.getPyObject(1, null);
        PyArrayArray ret = new PyArrayArray(subtype, MachineFormatCode.formatCode(typecode), INITIAL_CAPACITY);
        if (initial != null) {
            if (initial instanceof PyArrayArray) {
                PyArrayArray initArray = (PyArrayArray) initial;
                if (initArray.formatCode.typecode() == 'u' && typecode != 'u') {
                    throw Py.TypeError(String.format("cannot use a unicode array to initialize an array with typecode '%s'", typecode));
                }
                ByteBuffer readBuf = initArray.buf.asReadOnlyBuffer();
                readBuf.flip();
                ret.buf = ByteBuffer.allocate(readBuf.remaining());
                ret.buf.put(readBuf);
            } else {
                if (initial instanceof PyUnicode && typecode != 'u') {
                    throw Py.TypeError(String.format("cannot use a str to initialize an array with typecode '%s'", typecode));
                }
                ret.doExtend(initial);
            }

        }
        return ret;
    }

    @ExposedClassMethod
    public static PyArrayArray zeros(PyType subtype, int n, char typecode) {
        return new PyArrayArray(subtype, MachineFormatCode.formatCode(typecode), n);
    }

    @ExposedMethod
    public PyObject append(PyObject val) {
        checkCapacity(formatCode.getItemSize());
        formatCode.setitem(buf, buf.position(), val);
        buf.position(buf.position()+ formatCode.getItemSize());
        return this;
    }

    // insert one byte
    private void ins1(byte b) {
        checkCapacity(1);
        buf.put(b);
    }

    @ExposedMethod
    public PyObject extend(PyObject bb) {
        if (bb instanceof PyArrayArray) {
            char c = ((PyArrayArray) bb).typecode();
            if (c != typecode()) {
                if (c == 'u') {
                    throw Py.TypeError(String.format("cannot use a unicode array to initialize an array with typecode '%s'", typecode()));
                }
                throw Py.TypeError("can only extend with array of same kind");
            }
        } else if (bb instanceof PyUnicode && typecode() != 'u') {
            throw Py.TypeError(String.format("cannot use a str to initialize an array with typecode '%s'", typecode()));
        }
        doExtend(bb);
        return this;
    }

    private void doExtend(PyObject bb) {
        if (bb instanceof PyArrayArray) {
            if (((PyArrayArray) bb).byteLength() > buf.remaining()) {
                ByteBuffer newBuf = ByteBuffer.allocate(byteLength() + ((PyArrayArray) bb).byteLength() + INITIAL_CAPACITY);
                newBuf.put(readBuf());
                buf = newBuf;
            }
            buf.put(((PyArrayArray) bb).readBuf());
            return;
        }
        if (bb instanceof PyBytes) {
            byte[] buf = ((PyBytes) bb).toBytes();
            if (buf.length % itemsize() != 0) {
                throw Py.ValueError("bytes length not a multiple of item size");
            }
            for (byte b: buf) {
                ins1(b);
            }
        } else {
            for (PyObject val : bb.asIterable()) {
                append(val);
            }
        }
    }

    @ExposedMethod
    public PyObject buffer_info() {
        return new PyTuple(Py.Zero, new PyLong(buf.limit()));
    }

    @ExposedGet
    public int itemsize() {
        return formatCode.getItemSize();
    }

    @Override
    @ExposedMethod
    public int __len__() {
        return byteLength() / formatCode.getItemSize();
    }

    private int byteLength() {
        return readBuf().remaining();
    }

    @ExposedMethod
    public PyObject __iter__() {
        return new PyArrayIter(this);
    }

    @ExposedMethod
    public PyObject __add__(PyObject other) {
        if (!(other instanceof PyArrayArray)) {
            throw Py.TypeError(String.format("can only append an array (not \"%s\") to array", other.getType().fastGetName()));
        }
        checkFormatCode((PyArrayArray) other);
        PyArrayArray ret = new PyArrayArray(TYPE, formatCode, byteLength() + ((PyArrayArray) other).byteLength());
        ByteBuffer newBuf = ret.buf;
        newBuf.put(readBuf());
        newBuf.put(((PyArrayArray) other).readBuf());
        return ret;
    }

    @Override
    public String toString() {
        ByteBuffer readonly = readBuf();
        int len = __len__();
        if (len == 0) {
            return String.format("array('%s')", typecode());
        }
        StringBuilder bufStr = new StringBuilder(len * 3);
        for (int i = 0; i < len; i++) {
            if (i != 0) {
                bufStr.append(", ");
            }
            bufStr.append(formatCode.getitem(readonly, i * itemsize()));
        }
        return String.format("array('%s', [%s])", typecode(), bufStr);
    }

    @Override
    public PyObject do_richCompare(PyObject other, CompareOp op) {
        if (other instanceof PyArrayArray) {
            PyArrayArray o = (PyArrayArray) other;
            int ret = 0;
            ByteBuffer readThis = readBuf();
            ByteBuffer readThat = o.readBuf();
            int len = readThis.remaining();
            if (readThis.remaining() == readThat.remaining()) {
                for(int i = 0; i < len; i++) {
                    byte a = readThis.get();
                    byte b = readThat.get();
                    if (a != b) {
                        ret = a > b ? 1 : -1;
                        break;
                    }
                }
            }
            return op.bool(ret);
        }
        if (op == CompareOp.NE || op == CompareOp.EQ) {
            return op.bool(1);
        }
        return Py.NotImplemented;
    }

    protected ByteBuffer readBuf() {
        ByteBuffer readonly = buf.asReadOnlyBuffer();
        readonly.flip();
        return readonly;
    }

    protected ByteBuffer bufferInternal() {
        return buf;
    }

    private char typecode() {
        return formatCode.typecode();
    }

    private void checkFormatCode(PyArrayArray other) {
        if (formatCode != other.formatCode) {
            throw Py.TypeError("requires array of the same kind");
        }
    }

    private void checkCapacity(int size) {
        if (buf.position() + size >= buf.limit()) {
            ByteBuffer newBuf = ByteBuffer.allocate(buf.limit() * 2 + INITIAL_CAPACITY);
            ByteBuffer readonly = buf.asReadOnlyBuffer();
            readonly.flip();
            newBuf.put(readonly);
            buf = newBuf;
        }
    }
}
