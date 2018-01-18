/* Copyright (c) Jython Developers */
package org.python.modules.itertools;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;

import java.util.Arrays;

@ExposedType(name = "itertools.zip_longest", base = PyObject.class,
    doc = BuiltinDocs.itertools_zip_longest_doc)
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
        this.iterators = new PyObject[iterables.length];
        exhausted = new boolean[iterables.length];
        Arrays.fill(exhausted, false);
        for (int i = 0; i < iterables.length; i++) {
            iterators[i] = getIter(iterables[i]);
        }
        this.fillvalue = fillvalue;
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod(names = "__next__")
    public PyObject zip_longest___next__() {
        if (unexhausted == 0) {
            throw Py.StopIteration();
        }
        PyObject item[] = new PyObject[iterators.length];
        for (int i = 0; i < iterators.length; i++) {
            if (exhausted[i]) {
                item[i] = fillvalue;
            } else {
                try {
                    item[i] = iterNext(iterators[i]);
                } catch (PyException e) {
                    if (e.match(Py.StopIteration)) {
                        unexhausted--;
                        exhausted[i] = true;
                        item[i] = fillvalue;
                    } else {
                        throw e;
                    }
                }
            }
        }
        if (unexhausted == 0) {
            throw Py.StopIteration();
        }
        return new PyTuple(item);
    }

    @ExposedMethod(names = {"__reduce__"})
    public PyObject reduce() {
        return new PyTuple(TYPE, new PyTuple(fillvalue, new PyList(iterators)));
    }
}
