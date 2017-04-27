package org.python.core.generator;

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedType;

/**
 * Created by isaiah on 4/26/17.
 */
@ExposedType(name = "async_generator_asend")
public class PyAsyncGenASend extends PyGenerator {
    public static final PyType TYPE = PyType.fromClass(PyAsyncGenASend.class);

    private PyAsyncGenerator gen;
    private PyObject sendval;
    protected AwaitableState state;

    public PyAsyncGenASend(PyAsyncGenerator gen, PyObject sendval) {
        super(TYPE, gen.gi_frame, gen.closure);
        this.gen = gen;
        this.sendval = sendval;
    }

    @ExposedMethod(names = {"__iter__", "__await__"})
    public PyObject async_generator_asend___await__() {
        return this;
    }

    @ExposedMethod
    public PyObject async_generator_asend_send(PyObject val) {
        if (state == AwaitableState.CLOSED) {
            throw Py.StopIteration();
        }
        if (state == AwaitableState.INIT) {
            if (val == null || val == Py.None) {
                val = sendval;
            }
            state = AwaitableState.INIT;
        }
        PyObject result;
        try {
            result = gen.send(val);
        } catch (PyException e) {
            state = AwaitableState.CLOSED;
            if (e.match(Py.StopAsyncIteration) || e.match(Py.GeneratorExit)) {
                gen.ag_closed = true;
            }
            throw e;
        }
        if (result == null) {
            throw Py.StopAsyncIteration();
        }
        if (result instanceof PyAsyncGenWrappedValue) {
            state = PyAsyncGenASend.AwaitableState.CLOSED;
            throw Py.StopIteration(((PyAsyncGenWrappedValue) result).agw_val);
        }
        return result;
    }

    public enum AwaitableState {
        INIT, ITER, CLOSED
    }

    @ExposedMethod
    public PyObject async_generator_asend_close() {
        state = AwaitableState.CLOSED;
        return Py.None;
    }

    @ExposedMethod
    public PyObject async_generator_asend___next__() {
        return async_generator_asend_send(null);
    }
}
