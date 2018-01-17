package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;

import java.util.Collection;
import java.util.ConcurrentModificationException;
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
    int length;

    public PyIter(Collection<PyObject> coll) {
        super(TYPE);
        this.iter = coll.iterator();
        this.length = coll.size();
    }

    public PyIter(Iterator<PyObject> iter) {
        super(TYPE);
        this.iter = iter;
        this.length = -1;
    }

    @ExposedMethod
    public PyObject iterator___iter__() {
        return this;
    }

    @ExposedMethod
    public PyObject iterator___next__() {
        try {
            if (iter.hasNext()) {
                return iter.next();
            }
        } catch (ConcurrentModificationException e) {
            throw Py.RuntimeError("set changed duration iteration");
        }
        throw Py.StopIteration();
    }

    @ExposedMethod
    public PyObject iterator___length_hint__() {
        return new PyLong(length);
    }
}
