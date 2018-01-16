package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;
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

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod
    public PyObject tuple_iterator___next__() {
        if (tuple == null) {
            throw Py.StopIteration();
        }
        if (index >= tuple.__len__()) {
            tuple = null;
            throw Py.StopIteration();
        }
        PyObject ret = tuple.pyget(index++);
        if (ret == null) {
            tuple = null;
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
