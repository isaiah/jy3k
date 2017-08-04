package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;

/**
 * An iterator that yields the objects from a sequence-like object in reverse
 * order. 
 */
@ExposedType(name = "reversed_iterator")
public class PyReversedIterator extends PyIterator {

    /**
     * Creates an iterator that first yields the item at __len__ - 1 on seq and
     * returns the objects in descending order from there down to 0.
     * 
     * @param seq -
     *            an object that supports __getitem__ and __len__
     */
    public PyReversedIterator(PyObject seq) {
        this.seq = seq;
        idx = seq.__len__() - 1;
    }

    @ExposedMethod
    public PyObject reversed_iterator___next__() {
        if(idx >= 0) {
            return seq.__finditem__(idx--);
        }
        throw Py.StopIteration();
    }

    public PyObject __next__() {
        return reversed_iterator___next__();
    }

    private PyObject seq;

    private int idx;


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
