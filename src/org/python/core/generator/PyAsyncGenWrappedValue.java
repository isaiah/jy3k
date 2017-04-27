package org.python.core.generator;

import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.expose.ExposedGet;
import org.python.expose.ExposedType;

@ExposedType(name = "asyenc_generator_wrapped_value")
public class PyAsyncGenWrappedValue extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyAsyncGenWrappedValue.class);

    @ExposedGet
    public PyObject agw_val;

    public PyAsyncGenWrappedValue(PyObject val) {
        super(TYPE);
        this.agw_val = val;
    }
}
