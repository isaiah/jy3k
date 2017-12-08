package org.python.modules._io;

import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedType;
import org.python.core.PyObject;
import org.python.core.PyType;

@ExposedType(name = "_io._BufferedIOBase")
public class PyBufferedIOBase extends PyIOBase {
    public static final PyType TYPE = PyType.fromClass(PyBufferedIOBase.class);

    protected int blksize;

    public PyBufferedIOBase(PyType type) {
        super(type);
    }

    @ExposedGet(name = "_blksize")
    public int getBlksize() {
        return blksize;
    }
}
