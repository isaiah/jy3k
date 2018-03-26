package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;

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

    @ExposedSlot(SlotFunc.ITER)
    public static PyObject iterator___iter__(PyObject self) {
        return self;
    }

    @ExposedSlot(SlotFunc.ITER_NEXT)
    public static PyObject iterator___next__(PyObject obj) {
        PyIter self = (PyIter) obj;
        try {
            if (self.iter.hasNext()) {
                return self.iter.next();
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
