package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;

@ExposedType(name = "callable_iterator")
public class PyCallIter extends PyIterator {
    //note: Already implements Traverseproc, inheriting it from PyIterator

    private PyObject callable;

    private PyObject sentinel;

    public PyCallIter(PyObject callable, PyObject sentinel) {
        if (!callable.isCallable()) {
            throw Py.TypeError("iter(v, w): v must be callable");
        }
        this.callable = callable;
        this.sentinel = sentinel;
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod
    public PyObject callable_iterator___next__() {
        if (callable == null) {
            throw Py.StopIteration();
        }

        PyObject result;
        result = callable.__call__();
        if (result == null || sentinel.richCompare(result, CompareOp.EQ).isTrue()) {
            callable = null;
            throw Py.StopIteration();
        }
        return result;
    }


    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        int retValue = super.traverse(visit, arg);
        if (retValue != 0) {
            return retValue;
        }
        if (callable != null) {
            retValue = visit.visit(callable, arg);
            if (retValue != 0) {
                return retValue;
            }
        }
        return sentinel != null ? visit.visit(sentinel, arg) : 0;
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        return ob != null && (ob == callable || ob == sentinel || super.refersDirectlyTo(ob));
    }
}
