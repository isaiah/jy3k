package org.python.modules._io;

import org.python.annotations.ExposedType;
import org.python.core.PyType;

import java.io.InputStream;
import java.io.OutputStream;

@ExposedType(name = "_io.BufferedRandom")
public class PyBufferedRandom extends PyBufferedIOBase {
    public static final PyType TYPE = PyType.fromClass(PyBufferedRandom.class);

    public PyBufferedRandom(PyType subtype) {
        super(subtype);
    }

    public PyBufferedRandom(InputStream input, OutputStream output, int buffering) {
        super(TYPE);
    }
}
