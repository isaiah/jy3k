package org.python.modules._queue;

import org.python.annotations.ExposedType;
import org.python.core.PyObject;
import org.python.core.PyType;

@ExposedType(name = "_queue.SimpleQueue")
public class PySimpleQueue extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PySimpleQueue.class);

    public PySimpleQueue(PyType subtype) {
        super(subtype);
    }

    public PySimpleQueue() {
        super(TYPE);
    }
}
