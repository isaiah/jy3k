package org.python.modules.itertools;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.core.ArgParser;
import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;

import java.util.ArrayList;
import java.util.List;

@ExposedType(name = "itertools.cycle", base = PyObject.class, doc = BuiltinDocs.itertools_cycle_doc)
public class cycle extends PyObject {

    public static final PyType TYPE = PyType.fromClass(cycle.class);
    private List<PyObject> saved = new ArrayList<>();
    private int index = 0;
    PyObject iter;


    public cycle() {
        super(TYPE);
    }

    public cycle(PyType subType) {
        super(subType);
    }

    /**
     * Creates an iterator that iterates over an iterable, saving the values for each iteration.
     * When the iterable is exhausted continues to iterate over the saved values indefinitely.
     */
    public cycle(PyObject sequence) {
        super(TYPE);
        cycle___init__(sequence);
    }

    @ExposedNew
    @ExposedMethod
    final void cycle___init__(final PyObject[] args, String[] kwds) {
        ArgParser ap = new ArgParser("cycle", args, kwds, new String[]{"iterable"}, 1);
        ap.noKeywords();
        cycle___init__(ap.getPyObject(0));
    }

    private void cycle___init__(final PyObject sequence) {
        if (sequence != Py.None) {
            iter = getIter(sequence);
        }
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod(names = "__next__")
    public PyObject cycle___next__() {
        if (iter != null) {
            try {
                PyObject obj = iterNext(iter);
                saved.add(obj);
                return obj;
            } catch (PyException e) {
                if (!e.match(Py.StopIteration)) {
                    throw e;
                }
                iter = null;
            }
        }
        if (saved.size() == 0) {
            throw Py.StopIteration();
        }

        // pick element from saved List
        if (index >= saved.size()) {
            // start over again
            index = 0;
        }
        return saved.get(index++);
    }

    @ExposedMethod(names = {"__reduce__"})
    public PyObject reduce() {
        if (iter == null) {
            return new PyTuple(TYPE, Py.None, new PyTuple(new PyList(saved), new PyLong(index)));
        }
        return new PyTuple(TYPE, new PyTuple(iter), new PyTuple(new PyList(saved), new PyLong(index)));
    }

    @ExposedMethod(names = {"__setstate__"})
    public void setstate(PyObject state) {
        PyTuple data = (PyTuple) state;
        for (PyObject el: data.pyget(0).asIterable()) {
            saved.add(el);
        }
        index = data.pyget(1).asInt();
    }
}

