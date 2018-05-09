package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;

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

    @ExposedSlot(SlotFunc.ITER)
    public static PyObject __iter__(PyObject self) {
        return self;
    }

    @ExposedSlot(SlotFunc.ITER_NEXT)
    public static PyObject callable_iterator___next__(PyObject obj) {
        PyCallIter self = (PyCallIter) obj;
        if (self.callable == null) {
            throw Py.StopIteration();
        }

        PyObject result;
        result = Abstract.PyObject_Call(Py.getThreadState(), self.callable, Py.EmptyObjects, Py.NoKeywords);
        if (result == null || self.sentinel.richCompare(result, CompareOp.EQ).isTrue()) {
            self.callable = null;
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
