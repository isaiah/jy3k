package org.python.modules._io;

import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.core.Py;
import org.python.core.PyBytes;
import org.python.core.PyIter;
import org.python.core.PyIterator;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@ExposedType(name = "_io.BytesIO")
public class PyBytesIO extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyBytesIO.class);

    private ByteBuffer buf;
    private int pos;
    private int stringSize;
    private PyObject dict;
    private int exports;


    public PyBytesIO() {
        this(TYPE);
    }

    public PyBytesIO(PyType type) {
        super(type);
        buf = ByteBuffer.allocate(100);
    }

    public PyBytesIO(PyType subtype, PyObject initvalue) {
        this(subtype);
    }

    @ExposedNew
    @ExposedMethod
    public void __init__(PyObject[] args, String[] keywords) {
    }

    @ExposedMethod
    public boolean isatty() {
        return false;
    }

    @ExposedMethod
    public int tell() {
        return buf.position();
    }

    @ExposedMethod
    public PyObject getvalue() {
        byte[] bytes = new byte[buf.position()];
        buf.flip();
        buf.get(bytes);
        return new PyBytes(bytes);
    }

    @ExposedMethod(names = {"read", "read1"})
    public PyObject read(int size) {
        size = Math.max(size, buf.remaining());
        byte[] bytes = readBytes(size);
        return new PyBytes(bytes);
    }

    @ExposedMethod(defaults = {"-1"})
    public PyObject readline(int size) {
        int n = scaneol(size);
        return new PyBytes(readBytes(n));
    }

    @ExposedMethod(defaults = {"-1"})
    public PyObject readlines(int maxsize) {
        List<PyObject> lines = new ArrayList<>();
        int n = 0;
        int size = 0;
        while((n = scaneol(-1)) != 0) {
            PyObject line = new PyBytes(readBytes(n));
            lines.add(line);
            size += n;

            if (maxsize > 0 && size >= maxsize) {
                break;
            }
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
    public PyObject writelines(PyObject lines) {
        if (lines == Py.None) {
            return lines;
        }
        Iterable<PyObject> iter = lines.asIterable();
        for (PyObject item: iter) {
            write(item);
        }
        return Py.None;
    }

    @ExposedGet
    public boolean closed() {
        return buf == null;
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
    public PyObject flush() {
        return Py.None;
    }

    @ExposedMethod(names = {"__getstate__"})
    public PyObject getstate() {
        PyObject initvalue = getvalue();
        PyObject dict = Py.None;
        PyObject[] state = new PyObject[] { initvalue, new PyLong(buf.position()), dict};
        return new PyTuple(state);
    }

    /**
     * Get a line from the buffer of a BytesIO object.
     * Returns the length between the current position to the next newline character.
     */
    private int scaneol(int len) {
        buf.mark();
        int start = buf.position();
        int maxlen = buf.remaining();
        if (len < 0 || len > maxlen) {
            len = maxlen;
        }
        try {
            while(true) {
                byte c = buf.get();
                if (c == '\n') {
                    len = buf.position() - start + 1;
                    break;
                }
            }
        } catch (BufferUnderflowException e) {
        }
        buf.reset();
        return len;
    }

    private byte[] readBytes(int size) {
        byte[] output = new byte[size];
        buf.get(output);
        return output;
    }

    private int writeBytes(byte[] bytes) {
        try {
            buf.put(bytes);
        } catch (BufferOverflowException e) {
            resize(buf.position() + bytes.length);
             buf.put(bytes);
        }
        return bytes.length;
    }

    private void resize(int size) {
        int alloc = buf.capacity();
        if (size > Integer.MAX_VALUE) {
            throw Py.OverflowError("byteio size overflow");
        }
        if (size < alloc / 2) {
            // Major downsize; resize down to exact size.
            alloc = size + 1;
        } else if (size < alloc) {
            // Within allocated size; quick exit
            return;
        } else if (size < alloc * 1.125) {
            // Moderate upsize
            alloc = size + (size >> 3) + (size < 9 ? 3 : 6);
        } else {
            // Major upsize; resize up to exact size
            alloc = size + 1;
        }

        ByteBuffer newBuf = ByteBuffer.allocate(alloc);
        newBuf.put(buf);
        buf.clear();
        buf = newBuf;
    }
}
