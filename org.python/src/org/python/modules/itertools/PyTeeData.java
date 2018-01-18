package org.python.modules.itertools;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;

import java.util.ArrayList;
import java.util.List;

@ExposedType(name = "itertools._tee_dataobject")
public class PyTeeData extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyTeeData.class);
    private PyObject iterator;
    private int total;
    private List<PyObject> buffer;

    public PyTeeData(PyObject iterator) {
        this.iterator = iterator;
        buffer = new ArrayList<>();
    }

    public PyObject getItem(int pos) {
        if (pos == buffer.size()) {
            PyObject obj = PyObject.iterNext(iterator);
            buffer.add(obj);
        }
        return buffer.get(pos);
    }

    @ExposedMethod(names = {"__reduce__"})
    public PyObject reduce() {
        return new PyTuple(TYPE, new PyTuple(iterator), new PyTuple(new PyList(buffer)));
    }

    @ExposedMethod(names = {"__setstate__"})
    public void setstate(PyObject buffer) {
        this.buffer = ((PyList) buffer).subList(0, buffer.__len__());
    }
}
