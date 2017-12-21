package org.python.modules.array;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;
import org.python.bootstrap.Import;
import org.python.core.Py;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyTuple;
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
        PyObject ret = formatCode.getitem(array.bufferInternal(), pos);
        this.pos += formatCode.getItemSize();
        return ret;
    }

    @ExposedMethod
    public PyObject __reduce_ex__(PyObject proto) {
        PyObject builtins = Import.importModule("builtins");
        PyObject iter = builtins.__findattr__("iter");
        if (exhausted) {
            return new PyTuple(iter, new PyTuple(Py.EmptyTuple));
        }
        return new PyTuple(iter, new PyTuple(array), new PyLong(this.pos / formatCode.getItemSize()));
    }

    @ExposedMethod
    public PyObject __setstate__(PyObject state) {
        pos = state.asInt() * formatCode.getItemSize();
        return this;
    }
}
