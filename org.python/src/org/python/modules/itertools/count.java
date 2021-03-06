/* Copyright (c) 2012 Jython Developers */
package org.python.modules.itertools;

import org.python.annotations.ExposedSlot;
import org.python.annotations.SlotFunc;
import org.python.core.ArgParser;
import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyIterator;
import org.python.core.PyLong;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;

@ExposedType(name = "itertools.count", base = PyObject.class, doc = BuiltinDocs.itertools_count_doc)
public class count extends PyObject {
    public static final PyType TYPE = PyType.fromClass(count.class);

    private ItertoolsIterator iter;
    private PyObject counter;
    private PyObject stepper;

    public count(PyType subType) {
        super(subType);
    }

    /**
     * Creates an iterator that returns consecutive numbers starting at 0.
     */
    public count() {
        super(TYPE);
        count___init__(Py.Zero, Py.One);
    }

    /**
     * Creates an iterator that returns consecutive numbers starting at <code>start</code>.
     */
    public count(final PyObject start) {
        super(TYPE);
        count___init__(start, Py.One);
    }

    /**
     * Creates an iterator that returns consecutive numbers starting at <code>start</code> with <code>step</code> step.
     */
    public count(final PyObject start, final PyObject step) {
        super(TYPE);
        count___init__(start, step);
    }

    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject count_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        return new count(subtype);
    }

    @ExposedMethod
    final void count___init__(final PyObject[] args, String[] kwds) {
        ArgParser ap = new ArgParser("count", args, kwds, new String[] {"start", "step"}, 0);
        PyObject start = ap.getPyObject(0, Py.Zero);
        PyObject step = ap.getPyObject(1, Py.One);
        if (!start.isNumberType() || !step.isNumberType()) {
            throw Py.TypeError("a number is required");
        }
        count___init__(start, step);
    }

    private void count___init__(final PyObject start, final PyObject step) {
        counter = start;
        stepper = step;

        iter = new ItertoolsIterator() {
            public PyObject next() {
                PyObject result = counter;
                counter = counter._add(Py.getThreadState(), stepper);
                return result;
            }
        };
    }

    @ExposedMethod
    public PyObject count___copy__() {
        return new count(counter, stepper);
    }

    @ExposedMethod
    final PyObject count___reduce_ex__(PyObject protocol) {
        return __reduce_ex__(protocol);
    }

    @ExposedMethod
    final PyObject count___reduce__() {
        return __reduce_ex__(Py.Zero);
    }


    public PyObject __reduce_ex__(PyObject protocol) {
        if (stepper == Py.One) {
            return new PyTuple(getType(), new PyTuple(counter));
        } else {
            return new PyTuple(getType(), new PyTuple(counter, stepper));
        }
    }

    @ExposedMethod
    public PyUnicode __repr__() {
        if (stepper instanceof PyLong && stepper._cmp(Py.One) == 0) {
            return Py.newUnicode(String.format("count(%s)", counter));
        }
        return Py.newUnicode(String.format("count(%s, %s)", counter, stepper));
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod(names = "__next__")
    public PyObject count___next__() {
        return iter.next();
    }

}
