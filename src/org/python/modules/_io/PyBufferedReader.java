package org.python.modules._io;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;
import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyBytes;
import org.python.core.PyObject;
import org.python.core.PyType;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

@ExposedType(name = "_io.BufferedReader")
public class PyBufferedReader extends PyBufferedIOBase {
    public static final PyType TYPE = PyType.fromClass(PyBufferedReader.class);

    private BufferedInputStream input;
    private FileChannel fileChannel;

    public PyBufferedReader(InputStream in, int bufferSize) {
        super(TYPE);
        this.input = new BufferedInputStream(in, bufferSize);
        this.blksize = bufferSize;
    }

    public PyBufferedReader(File file) {
        super(TYPE);
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            input = new BufferedInputStream(fileInputStream);
            fileChannel = fileInputStream.getChannel();
        } catch (FileNotFoundException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedMethod(names = {"read", "read1"}, defaults = {"-1"}, doc = BuiltinDocs.BufferedReader_read_doc)
    public final PyObject read(PyObject sizeObj) {
        int size = sizeObj.asInt();
        if (size < -1) {
            throw Py.ValueError("invalid number of bytes to read");
        }
        byte[] buf;
        if (size > 0) {
            buf = new byte[size];
            try {
                int n = input.read(buf);
                return new PyBytes(buf, 0, n);
            } catch (IOException e) {
                throw Py.IOError(e);
            }
        }
        try {
            buf = input.readAllBytes();
            return new PyBytes(buf);
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedMethod(defaults = {"0"}, doc = BuiltinDocs.BufferedReader_peek_doc)
    public final PyObject peek(PyObject sizeObj) {
        int size = sizeObj.asInt();
        byte[] buf = new byte[size];
        try {
            input.mark(size);
            int n = input.read(buf);
            input.reset();
            return new PyBytes(buf, 0, n);
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedMethod
    public final PyObject BufferedReader_tell() {
        throw unsupported("tell");
    }

    @ExposedMethod(defaults = {"0"}, doc = BuiltinDocs.BufferedReader_seek_doc)
    public final PyObject seek(PyObject pos, PyObject whence) {
        throw unsupported("seek");
    }

    @ExposedMethod
    public final boolean readable() {
        return true;
    }

    @ExposedMethod
    public final boolean writable() {
        return false;
    }

    @ExposedMethod
    public final boolean seekable() {
        return false;
    }

    @ExposedMethod
    public final boolean markable() {
        return input.markSupported();
    }

    @ExposedMethod
    public void mark(int readLimit) {
        input.mark(readLimit);
    }

    @ExposedMethod
    public void reset() {
        try {
            input.reset();
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }
}
