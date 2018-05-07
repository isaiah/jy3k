package org.python.core;

import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;

@ExposedType(name = "wrapper_descriptor")
public class PyWrapperDescr extends PyDescriptor {
    public static final PyType TYPE = PyType.fromClass(PyWrapperDescr.class);

    private SlotFunc wrapped;

    public PyWrapperDescr(PyType subtype, String name, PyType dtype) {
        setType(subtype);
        this.name = name;
        this.dtype = dtype;
    }

    @ExposedSlot(SlotFunc.CALL)
    public static PyObject call(PyObject descr, PyObject[] args, String[] keywords) {
        PyWrapperDescr self = (PyWrapperDescr) descr;
        return Py.None;
    }

    @Override
    public String toString() {
        return String.format("<slot wrapper '%s' of '%s' objects>", name, dtype.fastGetName());
    }
}
