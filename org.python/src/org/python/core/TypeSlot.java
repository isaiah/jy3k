package org.python.core;

import org.python.annotations.SlotFunc;

import java.lang.invoke.MethodHandle;

public class TypeSlot {
    private SlotFunc type;
    private MethodHandle mh;

    public TypeSlot(SlotFunc type, MethodHandle mh) {
        this.type = type;
        this.mh = mh;
    }

    public void assign(PyType pyType) {
        type.assign(pyType, mh);
    }

    public void assign(PyType pyType, PyObject dict) {
        type.assign(pyType, mh);
        if (type.hasName()) {
            PyBuiltinMethod method = new PyBuiltinMethod(type.getName(), mh, type.isWide());
            dict.__setitem__(type.getName(), new PyWrapperDescr(pyType, method));
        }
    }
}
