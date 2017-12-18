package org.python.modules._io;

import org.jruby.util.ByteList;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.core.Py;
import org.python.core.PyBytes;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;

import java.util.ArrayList;
import java.util.List;

@ExposedType(name = "_io.BytesIO")
public class PyBytesIO extends PyBufferedIOBase {
    public static final PyType TYPE = PyType.fromClass(PyBytesIO.class);

    private final ByteList buf;
    private int pos;
    private int stringSize;
    private PyObject dict;
    private int exports;


    public PyBytesIO() {
        super(TYPE);
        buf = new ByteList();
    }

    public PyBytesIO(PyType type) {
        super(type);
        buf = new ByteList();
    }

    public PyBytesIO(PyType subtype, PyObject initvalue) {
        super(subtype);
        buf = new ByteList(Py.unwrapBuffer(initvalue));
    }

    @ExposedNew
    public static PyObject _new(PyNewWrapper _new, boolean init, PyType subType, PyObject[] args, String[] keywords) {
        if (args.length > 0) {
            return new PyBytesIO(subType, args[0]);
        } else {
            return new PyBytesIO(subType);
        }
    }

    @ExposedMethod
    public boolean isatty() {
        return false;
    }

    @ExposedMethod
    public int tell() {
        return buf.begin();
    }

    @ExposedMethod
    public PyObject getvalue() {
        return new PyBytes(buf.bytes());
    }

    @ExposedMethod(names = {"read", "read1"})
    public PyObject read(int size) {
        size = Math.min(size, buf.length());
        byte[] bytes = readBytes(size);
        return new PyBytes(bytes);
    }

    @ExposedMethod(defaults = {"-1"})
    public PyObject readline(int size) {
        int n = scaneol(size);
        int len;
        if (n > 0) {
            len = n - pos;
            return new PyBytes(readBytes(len));
        }
        return Py.EmptyByte;
    }

    @ExposedMethod(defaults = {"-1"})
    public PyObject readlines(int maxsize) {
        List<PyObject> lines = new ArrayList<>();
        for (int n = scaneol(-1), size = 0; n > 0; size += n) {
            PyObject line = new PyBytes(readBytes(n - pos));
            lines.add(line);
            size += n;

            if (maxsize > 0 && size >= maxsize) {
                break;
            }
            n = scaneol(-1);
        }
        return new PyList(lines);
    }

    @ExposedMethod
    public PyObject write(PyObject b) {
        byte[] bytes = Py.unwrapBuffer(b);
        int n = 0;
        if (bytes.length > 0) {
            n = writeBytes(bytes);
        }
        return new PyLong(n);
    }

    @ExposedMethod
    public void writelines(PyObject lines) {
        if (lines == Py.None) {
            return;
        }
        Iterable<PyObject> iter = lines.asIterable();
        for (PyObject item: iter) {
            write(item);
        }
    }

    @ExposedMethod
    public boolean writable() {
        return true;
    }

    @ExposedMethod
    public boolean readable() {
        return true;
    }

    @ExposedMethod
    public boolean seekable() {
        return true;
    }

    @ExposedMethod
    public void flush() {
    }

    @ExposedMethod(names = {"__getstate__"})
    public PyObject getstate() {
        PyObject initvalue = getvalue();
        PyObject dict = Py.None;
        PyObject[] state = new PyObject[] { initvalue, new PyLong(buf.begin()), dict};
        return new PyTuple(state);
    }

    /**
     * Get a line from the buffer of a BytesIO object.
     * Returns the length between the current position to the next newline character.
     */
    private int scaneol(int len) {
        int index = buf.indexOf('\n', pos + 1);
        if (len > 0 && len < index) {
            return len;
        }
        if (index < 0 && pos < buf.length()) {
            return buf.length();
        }
        return index + 1;
    }

    private byte[] readBytes(int size) {
        byte[] data = buf.getUnsafeBytes();
        size = Math.min(data.length - pos, size);
        byte[] output = new byte[size];
        System.arraycopy(data, pos, output, 0, size);
        pos += size;
        return output;
    }

    private int writeBytes(byte[] bytes) {
        buf.append(bytes);
        return bytes.length;
    }

//    private void resize(int size) {
//        int alloc = buf.capacity();
//        if (size > Integer.MAX_VALUE) {
//            throw Py.OverflowError("byteio size overflow");
//        }
//        if (size < alloc / 2) {
//            // Major downsize; resize down to exact size.
//            alloc = size + 1;
//        } else if (size < alloc) {
//            // Within allocated size; quick exit
//            return;
//        } else if (size < alloc * 1.125) {
//            // Moderate upsize
//            alloc = size + (size >> 3) + (size < 9 ? 3 : 6);
//        } else {
//            // Major upsize; resize up to exact size
//            alloc = size + 1;
//        }
//
//        ByteBuffer newBuf = ByteBuffer.allocate(alloc);
//        newBuf.put(buf);
//        buf.clear();
//        buf = newBuf;
//    }
}
