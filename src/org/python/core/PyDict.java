package org.python.core;

/**
 * A common interface for {@link PyStringMap} and {@link PyDictionary}
 */
public interface PyDict {
    /**
     * Merge another dictionary into this one
     * @param o
     */
    void update(PyObject o);
}
