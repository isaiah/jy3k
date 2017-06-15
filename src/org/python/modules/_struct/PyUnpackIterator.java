package org.python.modules._struct;

import org.python.core.Py;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.expose.ExposedFunction;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedType;

import java.nio.ByteBuffer;

import static org.python.modules._struct._struct.StructError;

@ExposedType(name = "unpack_iterator")
public class PyUnpackIterator extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyUnpackIterator.class);

    private ByteBuffer buffer;
    private PyStruct struct;

    public PyUnpackIterator(PyStruct struct, PyObject buffer) {
        super(TYPE);
        byte[] bytes = Py.unwrapBuffer(buffer);
        if (bytes.length % struct.size != 0) {
            throw StructError(String.format("iterative unpacking requires a bytes length multiple of %d", struct.size));
        }
        this.buffer = ByteBuffer.wrap(bytes);
        this.struct = struct;
    }

    @Override
    @ExposedFunction(names = {"__next__"})
    public PyObject __next__() {
        if (!buffer.hasRemaining()) {
            throw Py.StopIteration();
        }
        PyObject[] res = new PyObject[struct.len];
        int k = 0;
        for (FormatCode code: struct.codes) {
            for (int i = 0; i < code.repeat; i++) {
                res[k++] = code.fmtdef.unpack(buffer);
            }
        }
        return new PyTuple(res);
    }

    @Override
    @ExposedMethod(names = "__iter__")
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod(names = "__length_hint__")
    public PyObject __length_hint__() {
        return new PyLong(buffer.remaining() / struct.size);
    }
}
