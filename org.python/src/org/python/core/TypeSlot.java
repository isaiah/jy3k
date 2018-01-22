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
        SlotFunc a = SlotFunc.ITER;
        switch (type) {
            case ITER_NEXT:
                pyType.iterNext = mh;
                break;
            default:
                break;
        }
    }
}
