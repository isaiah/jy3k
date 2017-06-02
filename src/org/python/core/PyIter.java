package org.python.core;

import org.python.expose.ExposedMethod;
import org.python.expose.ExposedType;

import java.util.Iterator;

/**
 * A Python iterator that wraps a Java iterator
 *
 * This class should be able to replace PyIterator and most of its subclasses
 */
@ExposedType(name = "iterator")
public class PyIter extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyIter.class);

    Iterator<PyObject> iter;

    public PyIter(Iterator<PyObject> iter) {
        super(TYPE);
        this.iter = iter;
    }

    @Override
    public PyObject __next__() {
        try {
            return iterator___next__();
        } catch (PyException e) {
            if (e.match(Py.StopIteration)) {
                return null;
            }
            throw e;
        }
    }

    @ExposedMethod
    public PyObject iterator___iter__() {
        return this;
    }

    @ExposedMethod
    public PyObject iterator___next__() {
        if (iter.hasNext()) {
            return iter.next();
        }
        throw Py.StopIteration();
    }
}
