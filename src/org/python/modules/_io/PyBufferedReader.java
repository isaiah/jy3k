package org.python.modules._io;

import org.apache.tools.ant.taskdefs.Input;
import org.python.annotations.ExposedGet;
import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyBytes;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;
import org.python.core.PyType;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    public final PyObject BufferedReader_read(PyObject sizeObj) {
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
    public final PyObject BufferedReader_peek(PyObject sizeObj) {
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
        try {
            return new PyLong(fileChannel.position());
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedMethod(defaults = {"0"}, doc = BuiltinDocs.BufferedReader_seek_doc)
    public final PyObject BufferedReader_seek(PyObject pos, PyObject whence) {
        try {
            return new PyLong(fileChannel.position());
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedMethod
    public final PyObject BufferedReader_readable() {
        return Py.True;
    }

    @ExposedMethod
    public final PyObject BufferedReader_writable() {
        return Py.False;
    }

    @ExposedMethod
    public final PyObject BufferedReader_seekable() {
        return Py.newBoolean(fileChannel != null);
    }
}
