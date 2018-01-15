package org.python.modules._io;

import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.core.ArgParser;
import org.python.core.Py;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.PyUnicode;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.CharBuffer;
import java.nio.channels.NonReadableChannelException;
import java.nio.charset.Charset;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@ExposedType(name = "_io.TextIOWrapper")
public class PyTextIOWrapper extends PyTextIOBase {
    public static final PyType TYPE = PyType.fromClass(PyTextIOWrapper.class);
    private BufferedReader reader;
    private BufferedWriter writer;

    @ExposedGet
    public String encoding;

    public PyTextIOWrapper(PyType type) {
        super(type);
    }

    public PyTextIOWrapper(InputStream inputStream) {
        super(TYPE);
        this.reader = new BufferedReader(new InputStreamReader(inputStream));
        fileno = -1;
    }
    public PyTextIOWrapper(OutputStream output) {
        super(TYPE);
        this.writer = new BufferedWriter(new OutputStreamWriter(output));
        fileno = -1;
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
        ArgParser ap = new ArgParser("__init__", args, keywords,
                "file", "mode", "buffering", "encoding", "errors", "newline",
                "closefd", "opener", "line_buffering");
        Charset charset;
        charset = Charset.forName(ap.getString(2, "UTF-8"));
        PyObject initValue = ap.getPyObject(0);
        PyTextIOWrapper ret =new PyTextIOWrapper(TYPE);
        ret.encoding = charset.displayName();
        if (initValue instanceof PyBytesIO) {
            ret.reader = new BufferedReader(new InputStreamReader(((PyBytesIO) initValue).inputStream(), charset));
            ret.writer = new BufferedWriter(new OutputStreamWriter(((PyBytesIO) initValue).outputStream(), charset));
        } else if (initValue instanceof PyRawIOBase) {
            ret.reader = new BufferedReader(new InputStreamReader(((PyRawIOBase) initValue).inputStream(), charset));
            ret.writer = new BufferedWriter(new OutputStreamWriter(((PyRawIOBase) initValue).outputStream(), charset));
        } else if (initValue instanceof PyBufferedReader) {
            ret.reader = new BufferedReader(new InputStreamReader(((PyBufferedReader) initValue).inputStream(), charset));
        } else if (initValue instanceof PyBufferedWriter) {
            ret.writer = new BufferedWriter(new OutputStreamWriter(((PyBufferedWriter) initValue).outputStream(), charset));
        } else {
            throw Py.TypeError("file should be a raw io class");
        }

        return ret;
    }

    @ExposedMethod
    public void writelines(PyObject lines) {
        try {
            for (PyObject line: lines.asIterable()) {
                writer.write(line.asString());
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

    @ExposedMethod(defaults = {"-1"})
    public PyObject read(int size) {
        if (size < 0) {
            try {
                String all = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                return new PyUnicode(all);
            } catch (NonReadableChannelException e) {
                throw unsupported("read");
            }
        }
        char[] cbuf = new char[size];
        try {
            int n = reader.read(cbuf);
            if (n < 0) {
                return Py.EmptyUnicode;
            }
            return new PyUnicode(CharBuffer.wrap(cbuf, 0, n));
        } catch (NonReadableChannelException e) {
            throw unsupported("read");
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod(names = "__next__")
    public PyObject TextIOWrapper___next__() {
        PyObject line = readline();
        if (line == Py.EmptyUnicode) {
            throw Py.StopIteration();
        }
        return line;
    }

    @ExposedMethod
    public PyObject readline() {
        try {
            String line = reader.readLine();
            if (line == null) {
                return Py.EmptyUnicode;
            }
//            if (reader.ready() || line.equals("")) { // empty string to differenciate from EOF (Ctrl+D)
                return new PyUnicode(line + System.lineSeparator());
//            }
//            return new PyUnicode(line);
        } catch (NonReadableChannelException e) {
            throw unsupported("read");
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedMethod
    public void flush() {
        try {
            if (writer != null) {
                writer.flush();
            }
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }
}
