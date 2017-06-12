package org.python.modules._struct;

import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.expose.ExposedType;

@ExposedType(name = "unpack_iterator")
public class PyUnpackIterator extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyUnpackIterator.class);

    PyUnpackIterator() {
        super(TYPE);
    }
}
