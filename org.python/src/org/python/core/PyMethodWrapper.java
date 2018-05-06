package org.python.core;

import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;

import java.util.Objects;

@ExposedType(name = "method-wrapper")
public class PyMethodWrapper extends PyDescriptor {
    public static final PyType TYPE = PyType.fromClass(PyMethodWrapper.class);

    @ExposedGet(name = "__self__")
    private PyObject self;

    public PyMethodWrapper(PyType subtype, String name, PyType dtype) {
        super(subtype, name, dtype);
    }

    @ExposedSlot(SlotFunc.CALL)
    public static PyObject call(PyObject descr, PyObject[] args, String[] keywords) {
        return Py.None;
    }

    public String toString() {
        return String.format("<method-wrapper '%s' of %s object at %d>", name, self.getType().fastGetName(), Objects.hash(self));
    }
}
