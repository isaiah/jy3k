package org.python.modules.itertools;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyType;

@ExposedType(name = "itertools._grouper")
public class PyGrouper extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyGrouper.class);

    private PyObject currentValue, keyfunc, targetKey, iterator;
    private boolean completed;

    public PyGrouper(PyObject currentValue, PyObject targetKey, PyObject keyfunc, PyObject iterator) {
        super(TYPE);
        this.currentValue = currentValue;
        this.keyfunc = keyfunc;
        this.targetKey = targetKey;
        this.iterator = iterator;
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod
    public PyObject grouper___next__() {
        PyObject currentKey;
        if (completed) {
            throw Py.StopIteration();
        }
        final PyObject item = currentValue;

        currentValue = iterNext(iterator);

        if (keyfunc == Py.None) {
            currentKey = currentValue;
        } else {
            currentKey = keyfunc.__call__(currentValue);
        }
        if (!currentKey.equals(targetKey)) {
            completed = true;
        }
        return item;
    }
}
