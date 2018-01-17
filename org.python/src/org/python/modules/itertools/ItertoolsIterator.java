package org.python.modules.itertools;

import org.python.core.PyObject;

/**
 * Iterator base class used by most methods.
 */
abstract class ItertoolsIterator {

    abstract PyObject next();

    /**
     * Returns the next element from an iterator. If it raises/throws StopIteration just store
     * the Exception and return null according to PyIterator practice.
     */
    protected PyObject nextElement(PyObject pyIter) {
        return PyObject.iterNext(pyIter);
    }
}
