package org.python.core;

import java.lang.invoke.MethodHandle;

public class PyBuiltinExceptionMethod extends PyBuiltinMethod {
    public PyBuiltinExceptionMethod(PyObject self, PyBuiltinMethodData info) {
        super(self, info);
    }

    public PyBuiltinExceptionMethod(String name, String defaultVals, MethodHandle mh, String doc, boolean isStatic, boolean isWide) {
        super(name, defaultVals, mh, doc, isStatic, isWide, false);
    }

    @Override
    public PyObject invoke() {
        return invoke(Py.EmptyObjects, Py.NoKeywords);
    }

    @Override
    public PyObject invoke(PyObject[] args, String[] keywords) {
        if (self != null) {
            return info.invoke(self, args, keywords);
        }
        return info.invoke(args, keywords);
    }

    @Override
    public PyBuiltinExceptionMethod bind(PyObject bindTo) {
        return new PyBuiltinExceptionMethod(bindTo, info);
    }
}
