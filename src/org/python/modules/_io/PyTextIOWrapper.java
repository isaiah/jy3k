package org.python.modules._io;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.core.ArgParser;
import org.python.core.Py;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyType;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

@ExposedType(name = "_io.TextIOWrapper")
public class PyTextIOWrapper extends PyTextIOBase {
    public static final PyType TYPE = PyType.fromClass(PyTextIOWrapper.class);
    private BufferedReader reader;
    private BufferedWriter writer;

    public PyTextIOWrapper(PyType type) {
        super(type);
    }

    public PyTextIOWrapper(InputStream input, OutputStream output, int bufferSize, int fileno) {
        super(TYPE);
        if (input != null) {
            this.reader = new BufferedReader(new InputStreamReader(input), bufferSize);
        }
        if (output != null) {
            this.writer = new BufferedWriter(new OutputStreamWriter(output), bufferSize);
        }
        this.fileno = fileno;
    }

    @ExposedNew
    public static PyObject _new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("__init__", args, keywords, "init_value", "line_buffering");
        PyObject initValue = ap.getPyObject(0);
        PyTextIOWrapper ret =new PyTextIOWrapper(TYPE);
        if (initValue instanceof PyBytesIO) {
            ret.reader = new BufferedReader(((PyBytesIO) initValue).getReader());
            ret.writer = new BufferedWriter(((PyBytesIO) initValue).getWriter());
        }
        return ret;
    }

    @ExposedMethod
    public void writelines(PyObject lines) {
        try {
            for (PyObject line: lines.asIterable()) {
                writer.write(line.asString());
                writer.newLine();
            }
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedMethod
    public int write(String line) {
        try {
            writer.write(line);
            return line.length();
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }
}
