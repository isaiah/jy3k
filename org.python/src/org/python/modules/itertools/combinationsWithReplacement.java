/* Copyright (c) 2012 Jython Developers */
package org.python.modules.itertools;

import org.python.annotations.ExposedSlot;
import org.python.annotations.SlotFunc;
import org.python.core.ArgParser;
import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.core.Visitproc;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;

@ExposedType(name = "itertools.combinations_with_replacement", base = PyObject.class,
    doc = BuiltinDocs.itertools_combinations_with_replacement_doc)
public class combinationsWithReplacement extends PyObject {
    public static final PyType TYPE = PyType.fromClass(combinationsWithReplacement.class);
    private ItertoolsIterator iter;

    public combinationsWithReplacement() {
        super(TYPE);
    }

    public combinationsWithReplacement(PyType subType) {
        super(subType);
    }

    public combinationsWithReplacement(PyObject iterable, int r) {
        super(TYPE);
        combinationsWithReplacement___init__(iterable, r);
    }

    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject combinations_with_replacement_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        return new combinationsWithReplacement(subtype);
    }

    @ExposedMethod
    final void combinationsWithReplacement___init__(PyObject[] args, String[] kwds) {
        if (args.length > 2) {
            throw Py.TypeError("combinations_with_replacement() takes at most 2 arguments (3 given)");
        }
        ArgParser ap = new ArgParser("combinations_with_replacement", args, kwds, "iterable", "r");
        PyObject iterable = ap.getPyObject(0);
        int r = ap.getInt(1);
        if (r < 0) {
            throw Py.ValueError("r must be non-negative");
        }
        combinationsWithReplacement___init__(iterable, r);
    }

    private void combinationsWithReplacement___init__(PyObject iterable, final int r) {
        final PyTuple pool = PyTuple.fromIterable(iterable);
        final int n = pool.__len__();
        final int indices[] = new int[r];
        for (int i = 0; i < r; i++) {
            indices[i] = 0;
        }

        iter = new ItertoolsIterator() {
            boolean firstthru = true;

            @Override
            public PyObject next() {
                if (firstthru) {
                    firstthru = false;
                    if (n == 0 && r > 0) {
                        throw Py.StopIteration();
                    }
                    return itertools.makeIndexedTuple(pool, indices);
                }
                int i;
                for (i = r - 1 ; i >= 0 && indices[i] == n - 1; i--);
                if (i < 0) {
                    throw Py.StopIteration();
                }
                indices[i]++;
                for (int j = i + 1; j < r; j++) {
                    indices[j] = indices[j-1];
                }
                return itertools.makeIndexedTuple(pool, indices);
            }
        };
    }

    @ExposedMethod(names = "__next__")
    public PyObject combination_with_replacement___next__() {
        return iter.next();
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }
}
