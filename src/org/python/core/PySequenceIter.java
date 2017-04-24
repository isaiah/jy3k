/* Copyright (c) Jython Developers */
package org.python.core;

import org.python.expose.ExposedMethod;
import org.python.expose.ExposedType;

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
    public PyObject sequence_iter___next__() {
        return super.next();
    }

    public PyObject __next__() {
        if (seq == null) {
            return null;
        }

        PyObject result;
        try {
            result = seq.__finditem__(index++);
        } catch (PyException exc) {
            if (exc.match(Py.StopIteration)) {
                seq = null;
                return null;
            }
            throw exc;
        }
        if (result == null) {
            seq = null;
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
