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
    private PyArrayArray array;
    private int pos;
    private boolean exhausted;

    PyArrayIter(PyArrayArray array) {
        this.formatCode = array.formatCode;
        this.array = array;
        this.pos = 0;
        this.exhausted = false;
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod
    public PyObject __next__() {
        if (exhausted || pos >= array.bufferInternal().position()) {
            exhausted = true;
            throw Py.StopIteration();
        }
        PyObject ret = formatCode.getitem(array.bufferInternal(), pos++);
        return ret;
    }
}
