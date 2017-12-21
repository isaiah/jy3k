package org.python.modules._io;

import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.core.ArgParser;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyType;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.python.modules._io._io.DEFAULT_BUFFER_SIZE;

@ExposedType(name = "_io.BufferedRWPair")
public class PyBufferedRWPair extends PyBufferedIOBase {
    public static final PyType TYPE = PyType.fromClass(PyBufferedRWPair.class);

    private BufferedInputStream input;
    private BufferedOutputStream output;

    public PyBufferedRWPair(PyType type) {
        super(type);
    }

    public PyBufferedRWPair(InputStream reader, OutputStream writer, int bufferSize) {
        super(TYPE);
        input = new BufferedInputStream(reader, bufferSize);
        output = new BufferedOutputStream(writer, bufferSize);
    }

    @ExposedNew
    public static PyObject _new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("__init__", args, keywords, "reader", "writer", "buffere_size");
        PyObject reader = ap.getPyObject(0);
        PyObject writer = ap.getPyObject(1);
        int bufferSize = ap.getInt(2, DEFAULT_BUFFER_SIZE);

        InputStream input = null;
        OutputStream output = null;
        if (reader instanceof PyRawIOBase) {
            input = ((PyRawIOBase) reader).inputStream();
        }
        if (writer instanceof PyRawIOBase) {
            output = ((PyRawIOBase) writer).outputStream();
        }
        return new PyBufferedRWPair(input, output, bufferSize);
    }
}
