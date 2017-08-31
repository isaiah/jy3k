package org.python.core;

import java.util.Map;

/**
 * A common interface for {@link PyStringMap} and {@link PyDictionary}
 */
public interface PyDict {
    /**
     * Merge another dictionary into this one
     * @param o
     */
    void update(PyObject o);

    Map<? extends Object, PyObject> getMap();
}
