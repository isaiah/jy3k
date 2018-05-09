package org.python.core;


/**
 * A builtin classmethod with a restricted number of arguments.
 */
public class PyBuiltinClassMethod extends PyBuiltinMethod {
    public PyBuiltinClassMethod(PyObject self, PyBuiltinMethodData info) {
        super(self, info);
    }

    @Override
    public PyMethodDescr makeDescriptor(PyType t) {
        return new PyClassMethodDescr(t, this);
    }

    @Override
    public PyBuiltinMethod bind(PyObject bindTo) {
        return new PyBuiltinClassMethod(bindTo, info);
    }
}
