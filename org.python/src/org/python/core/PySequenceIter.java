/* Copyright (c) Jython Developers */
package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;

/**
 * General sequence iterator.
 */
@ExposedType(name = "sequence_iter")
public class PySequenceIter extends PyIterator {

    private PyObject seq;

    private int index = 0;

    public PySequenceIter(PyObject seq) {
        this.seq = seq;
    }

    @ExposedMethod
    public PyObject __iter__(){
        return this;
    }

    @ExposedMethod
    public PyObject sequence_iter___next__() {
        if (seq == null) {
            throw Py.StopIteration();
        }

        PyObject result;
        result = seq.__finditem__(index++);
        if (result == null) {
            seq = null;
            throw Py.StopIteration();
        }
        return result;
    }


    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        int retVal = super.traverse(visit, arg);
        if (retVal != 0) {
            return retVal;
        }
        return seq == null ? 0 : visit.visit(seq, arg);
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        return ob != null && (ob == seq || super.refersDirectlyTo(ob));
    }
}
