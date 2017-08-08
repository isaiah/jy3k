package org.python.core;


/**
 * A builtin classmethod with a restricted number of arguments.
 */
public abstract class PyBuiltinClassMethod extends PyBuiltinMethod {
    protected PyBuiltinClassMethod(PyObject self, PyBuiltinMethodData info) {
        super(self, info);
    }

    protected PyBuiltinClassMethod(PyType type, PyObject self, PyBuiltinMethodData info) {
        super(type, self, info);
    }
    
    public PyMethodDescr makeDescriptor(PyType t) {
        return new PyClassMethodDescr(t, this);
    }
}
