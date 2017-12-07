package org.python.modules._io;

import org.python.annotations.ExposedType;
import org.python.core.PyObject;
import org.python.core.PyType;

@ExposedType(name = "_io.BufferedRWPair")
public class PyBufferedRWPair extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyBufferedRWPair.class);
}
