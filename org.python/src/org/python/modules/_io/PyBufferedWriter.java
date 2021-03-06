package org.python.modules._io;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.core.ArgParser;
import org.python.core.BufferProtocol;
import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyBUF;
import org.python.core.PyLong;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyType;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

@ExposedType(name = "_io.BufferedWriter")
public class PyBufferedWriter extends PyBufferedIOBase {
    public static final PyType TYPE = PyType.fromClass(PyBufferedWriter.class);

    private final BufferedOutputStream output;
    private FileChannel fileChannel;

    public PyBufferedWriter(PyType type) {
        super(type);
        output = null;
    }

    public PyBufferedWriter(OutputStream out, int bufferSize) {
        super(TYPE);
        output = new BufferedOutputStream(out, bufferSize);
        blksize = bufferSize;
    }

    public PyBufferedWriter(File file) {
        super(TYPE);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            output = new BufferedOutputStream(fileOutputStream);
            fileChannel = fileOutputStream.getChannel();
        } catch (FileNotFoundException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedNew
    public static PyObject _new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("__init__", args, keywords, "raw");
        PyObject raw = ap.getPyObject(0);
        if (raw instanceof PyRawIOBase) {
            return new PyBufferedWriter(((PyRawIOBase) raw).outputStream(), 1024);
        }
        return new PyBufferedWriter(((PyBytesIO) raw).outputStream(), 1024);
    }

    @ExposedMethod(doc = BuiltinDocs.BufferedWriter_write_doc)
    public final PyObject write(PyObject b) {
        if (!(b instanceof BufferProtocol)) {
            throw Py.TypeError("bytes-like object expected");
        }
        try {
            output.write(Py.unwrapBuffer(b));
            return new PyLong(b.__len__());
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedMethod(doc = BuiltinDocs.BufferedWriter_flush_doc)
    public final void flush() {
        try {
            output.flush();
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    public void close() {
        try {
            output.close();
            __closed = true;
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedMethod(doc = BuiltinDocs.BufferedWriter_tell_doc)
    public final int tell() {
        throw unsupported("tell");
    }

    public OutputStream outputStream() {
        return output;
    }
}
