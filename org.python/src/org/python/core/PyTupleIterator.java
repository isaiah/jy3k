package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;

@ExposedType(name = "tuple_iterator")
public class PyTupleIterator extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyTupleIterator.class);
    private int index;
    private PyObject[] array;

    public PyTupleIterator(PyTuple tuple) {
        super(TYPE);
        index = 0;
        array = tuple.getArray();
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @Override
    public PyObject __next__() {
        if (index >= array.length) {
            return null;
        }
        return array[index++];
    }

    @ExposedMethod
    public PyObject tuple_iterator___next__() {
        if (index >= array.length) {
            throw Py.StopIteration();
        }
        return array[index++];
    }

    @ExposedMethod
    public int __length_hint__() {
        return index;
    }
}
