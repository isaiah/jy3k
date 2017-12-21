package org.python.modules._io;

import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedType;
import org.python.core.PyObject;
import org.python.core.PyType;

@ExposedType(name = "_io._TextIOBase")
public class PyTextIOBase extends PyIOBase {
    public static final PyType TYPE = PyType.fromClass(PyTextIOBase.class);

    @ExposedGet
    public int _CHUNK_SIZE = 1024;

    public PyTextIOBase(PyType type) {
        super(type);
    }
}
