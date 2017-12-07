package org.python.modules._io;

import org.python.annotations.ExposedType;
import org.python.core.PyType;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

@ExposedType(name = "_io.TextIOWrapper")
public class PyTextIOWrapper extends PyTextIOBase {
    public static final PyType TYPE = PyType.fromClass(PyTextIOWrapper.class);
    private InputStreamReader reader;
    private OutputStreamWriter writer;

    public PyTextIOWrapper(OutputStreamWriter outputStreamWriter) {
        super(TYPE);
        this.writer = outputStreamWriter;
    }

    public PyTextIOWrapper(InputStreamReader inputStreamReader) {
        super(TYPE);
        this.reader = inputStreamReader;
    }
}
