package org.python.core.generator;

import org.python.core.Py;
import org.python.core.PyFrame;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;

/**
 * PEP-525 Asynchronous Generator
 */
@ExposedType(name = "async_generator")
public class PyAsyncGenerator extends PyGenerator {
    public static final PyType TYPE = PyType.fromClass(PyAsyncGenerator.class);

    @ExposedGet
    public boolean ag_closed = false;

    public PyAsyncGenerator(PyFrame frame, PyObject closure) {
        super(TYPE, frame, closure);
    }

    @ExposedMethod
    public PyObject async_generator___aiter__() {
        return this;
    }

    @ExposedMethod
    public PyObject async_generator___anext__() {
        return async_generator_asend(Py.None);
    }

    @ExposedMethod
    public PyObject async_generator_asend(PyObject val) {
        return new PyAsyncGenASend(this, val);
    }

    @ExposedGet
    public PyObject ag_running() {
        return Py.newBoolean(gi_running);
    }

    @ExposedGet
    public PyObject ag_frame() {
        return gi_frame;
    }
}
