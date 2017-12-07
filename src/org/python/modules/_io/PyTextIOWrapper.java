package org.python.modules._io;

import org.python.annotations.ExposedType;
import org.python.core.PyType;

@ExposedType(name = "_io.TextIOWrapper")
public class PyTextIOWrapper extends PyTextIOBase {
    public static final PyType TYPE = PyType.fromClass(PyTextIOWrapper.class);
}
