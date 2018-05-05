/* Copyright (c) Jython Developers */
package org.python.modules.itertools;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyList;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;

import java.util.Arrays;

@ExposedType(name = "itertools.zip_longest")
public class zip_longest extends PyObject {
    public static final PyType TYPE = PyType.fromClass(zip_longest.class);

    private int unexhausted;
    private PyObject[] iterators;
    private boolean[] exhausted;
    private PyObject fillvalue;


    public zip_longest() {
        super(TYPE);
    }

    public zip_longest(PyType subType) {
        super(subType);
    }

    public zip_longest(PyObject[] iterables, PyObject fillvalue) {
        super(TYPE);
        zip_longest___init__(iterables, fillvalue);
    }

    /**
     * Create an iterator that returns items from the iterable while <code>predicate(item)</code>
     * is true. After which iteration is stopped.
     */
    @ExposedNew
    public static PyObject zip_longest_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        return new zip_longest(subtype);
    }

    @ExposedMethod
    final void zip_longest___init__(PyObject[] args, String[] kwds) {

        PyObject[] iterables;
        PyObject fillvalue;

        if (kwds.length == 1 && kwds[0] == "fillvalue") {
            fillvalue = args[args.length - 1];
            iterables = new PyObject[args.length - 1];
            System.arraycopy(args, 0, iterables, 0, args.length - 1);
        } else {
            fillvalue = Py.None;
            iterables = args;
        }
        zip_longest___init__(iterables, fillvalue);
    }

    private void zip_longest___init__(final PyObject[] iterables, final PyObject fillvalue) {
        unexhausted = iterables.length;
        this.iterators = new PyObject[unexhausted];
        exhausted = new boolean[unexhausted];
        Arrays.fill(exhausted, false);
        for (int i = 0; i < iterables.length; i++) {
            iterators[i] = getIter(iterables[i]);
        }
        this.fillvalue = fillvalue;
    }

    @ExposedSlot(SlotFunc.ITER)
    public static PyObject __iter__(PyObject self) {
        return self;
    }

    @ExposedSlot(SlotFunc.ITER_NEXT)
    public static PyObject zip_longest___next__(PyObject obj) {
        zip_longest self = (zip_longest) obj;
        if (self.unexhausted == 0) {
            throw Py.StopIteration();
        }
        PyObject item[] = new PyObject[self.iterators.length];
        for (int i = 0; i < self.iterators.length; i++) {
            if (self.exhausted[i]) {
                item[i] = self.fillvalue;
            } else {
                try {
                    item[i] = iterNext(self.iterators[i]);
                } catch (PyException e) {
                    if (e.match(Py.StopIteration)) {
                        self.unexhausted--;
                        self.exhausted[i] = true;
                        item[i] = self.fillvalue;
                    } else {
                        throw e;
                    }
                }
            }
        }
        if (self.unexhausted == 0) {
            throw Py.StopIteration();
        }
        return new PyTuple(item);
    }

    @ExposedMethod(names = {"__reduce__"})
    public PyObject reduce() {
        return new PyTuple(TYPE, new PyTuple(fillvalue, new PyList(iterators)));
    }
}
