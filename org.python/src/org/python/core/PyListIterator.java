package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;

@ExposedType(name = "list_iterator")
public class PyListIterator extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyListIterator.class);

    private PyList list;
    private int index;
    private long length;
    public PyListIterator(PyList list) {
        super(TYPE);
        this.list = list;
        this.index = 0;
        this.length = list.__len__();
    }

    @ExposedSlot(SlotFunc.ITER_NEXT)
    public static PyObject next(PyObject iter) {
        PyListIterator self = (PyListIterator) iter;
        if (self.index >= self.length) {
            throw Py.StopIteration();
        }
        return PyList.getitem(self.list, self.index++);
    }

    @ExposedMethod(names = {"__length_hint__"})
    public long length() {
        return length;
    }
}
