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
}
