package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;
import org.python.antlr.ast.Tuple;
import org.python.bootstrap.Import;

@ExposedType(name = "tuple_iterator")
public class PyTupleIterator extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyTupleIterator.class);
    private int index;
    private PyTuple tuple;

    public PyTupleIterator(PyTuple tuple) {
        super(TYPE);
        index = 0;
        this.tuple = tuple;
    }

    @ExposedSlot(SlotFunc.ITER)
    public static PyObject __iter__(PyObject self) {
        return self;
    }

    @ExposedSlot(SlotFunc.ITER_NEXT)
    public static PyObject tuple_iterator___next__(PyObject iter) {
        PyTupleIterator self = (PyTupleIterator) iter;
        if (self.tuple == null) {
            throw Py.StopIteration();
        }
        if (self.index >= self.tuple.__len__()) {
            self.tuple = null;
            throw Py.StopIteration();
        }
        PyObject ret = self.tuple.pyget(self.index++);
        if (ret == null) {
            self.tuple = null;
            throw Py.StopIteration();
        }
        return ret;
    }

    @ExposedMethod
    public int __length_hint__() {
        return index;
    }

    @ExposedMethod
    public PyObject __reduce__() {
        PyObject builtins = Import.importModule("builtins");
        return new PyTuple(builtins.__findattr__("iter"), tuple, new PyLong(index));
    }
}
