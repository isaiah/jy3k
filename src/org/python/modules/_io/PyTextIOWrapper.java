package org.python.modules._io;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.core.ArgParser;
import org.python.core.Py;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyType;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

@ExposedType(name = "_io.TextIOWrapper")
public class PyTextIOWrapper extends PyTextIOBase {
    public static final PyType TYPE = PyType.fromClass(PyTextIOWrapper.class);
    private InputStreamReader reader;
    private OutputStreamWriter writer;

    public PyTextIOWrapper(PyType type) {
        super(type);
    }

    public PyTextIOWrapper(OutputStreamWriter outputStreamWriter, int bufferSize, int fileno) {
        super(TYPE);
        this.writer = outputStreamWriter;
        this.fileno = fileno;
    }

    public PyTextIOWrapper(InputStreamReader inputStreamReader, int bufferSize, int fileno) {
        super(TYPE);
        this.reader = inputStreamReader;
        this.fileno = fileno;
    }

    @ExposedNew
    public static PyObject _new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("__init__", args, keywords);
        return new PyTextIOWrapper(TYPE);
    }

    @ExposedMethod
    public void writelines(PyObject lines) {
        try {
            for (PyObject line: lines.asIterable()) {
                writer.write(line.asString());
                writer.write('\n');
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
