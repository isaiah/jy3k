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
    final private PyObject self;

    public PyMethodWrapper(String name, PyObject self) {
        this.name = name;
        this.self = self;
        setType(TYPE);
    }

    @ExposedSlot(SlotFunc.CALL)
    public static PyObject call(PyObject descr, PyObject[] args, String[] keywords) {
        PyMethodWrapper self = (PyMethodWrapper) descr;
        return Py.None;
    }

    public String toString() {
        return String.format("<method-wrapper '%s' of %s object at %d>", name, self.getType().fastGetName(), Objects.hash(self));
    }
}
