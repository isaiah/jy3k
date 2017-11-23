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
import org.python.core.PyLong;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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
                ByteBuffer readBuf = ((PyArrayArray) initial).buf.asReadOnlyBuffer();
                readBuf.flip();
                ret.buf = ByteBuffer.allocate(readBuf.remaining());
                ret.buf.put(readBuf);
            } else {
                ret.extend(initial);
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
        formatCode.setitem(buf, buf.position(), val);
        if (buf.position() == buf.limit()) {
            ByteBuffer newBuf = ByteBuffer.allocate(buf.limit() * 2);
            ByteBuffer readonly = buf.asReadOnlyBuffer();
            readonly.flip();
            newBuf.put(readonly);
            buf = newBuf;
        }
        buf.position(buf.position()+1);
        return this;
    }

    @ExposedMethod
    public PyObject extend(PyObject bb) {
        for (PyObject val: bb.asIterable()) {
            append(val);
        }
        return this;
    }

    @ExposedMethod
    public PyObject buffer_info() {
        return new PyTuple(Py.Zero, new PyLong(buf.limit()));
    }

    @ExposedGet
    public int itemsize() {
        return formatCode.getItemSize();
    }

    @ExposedMethod(names = {"__len__"})
    public int length() {
        return readBuf().remaining();
    }

    @ExposedMethod
    public PyObject __iter__() {
        return new PyArrayIter(this);
    }

    @Override
    public String toString() {
        ByteBuffer readonly = readBuf();
        int len = readonly.remaining();
        if (len == 0) {
            return String.format("array('%s')", formatCode.typecode());
        }
        StringBuilder bufStr = new StringBuilder(len * 3);
        for (int i = 0; i < len; i++) {
            if (i != 0) {
                bufStr.append(", ");
            }
            bufStr.append(readonly.get());
        }
        return String.format("array('%s', [%s])", formatCode.typecode(), bufStr);
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
}
