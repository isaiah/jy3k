package org.python.modules.array;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyType;

import java.nio.ByteBuffer;

@ExposedType(name = "arrayiterator")
public class PyArrayIter extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyArrayIter.class);
    private MachineFormatCode formatCode;
    private ByteBuffer readBuf;

    PyArrayIter(PyArrayArray array) {
        this.formatCode = array.formatCode;
        this.readBuf = array.readBuf();
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod
    public PyObject __next__() {
        if (readBuf.remaining() == 0) {
            throw Py.StopIteration();
        }
        PyObject ret = formatCode.getitem(readBuf, readBuf.position());
        readBuf.position(readBuf.position() + 1);
        return ret;
    }
}
