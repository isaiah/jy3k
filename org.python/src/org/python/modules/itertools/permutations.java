/* Copyright (c) Jython Developers */
package org.python.modules.itertools;

import org.python.annotations.ExposedSlot;
import org.python.annotations.SlotFunc;
import org.python.core.ArgParser;
import org.python.core.Py;
import org.python.core.PyIterator;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.core.Visitproc;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;

@ExposedType(name = "itertools.permutations", base = PyObject.class)
public class permutations extends PyObject {
    public static final PyType TYPE = PyType.fromClass(permutations.class);
    private ItertoolsIterator iter;

    public permutations() {
        super();
    }

    public permutations(PyType subType) {
        super(subType);
    }

    public permutations(PyObject iterable, int r) {
        super();
        permutations___init__(iterable, r);
    }

    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject permutations_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        return new permutations(subtype);
    }

    @ExposedMethod
    final void permutations___init__(PyObject[] args, String[] kwds) {
        if (args.length > 2) {
            throw Py.TypeError("permutations() takes at most 2 arguments (3 given)");
        }
        ArgParser ap = new ArgParser("permutations", args, kwds, "iterable", "r");
        PyObject iterable = ap.getPyObject(0);
        PyObject r = ap.getPyObject(1, Py.None);

        int perm_length;
        if (r == Py.None) {
            perm_length = iterable.__len__();
        } else {
            perm_length = r.asInt();
            if (perm_length < 0) {
                throw Py.ValueError("r must be non-negative");
            }
        }

        permutations___init__(iterable, perm_length);
    }

    private void permutations___init__(final PyObject iterable, final int r) {
        final PyTuple pool = PyTuple.fromIterable(iterable);
        final int n = pool.__len__();
        final int indices[] = new int[n];
        for (int i = 0; i < n; i++) {
            indices[i] = i;
        }
        final int cycles[] = new int[r];
        for (int i = 0; i < r; i++) {
            cycles[i] = n - i;
        }
        iter = new ItertoolsIterator() {
            boolean firstthru = true;

            @Override
            public PyObject next() {
                if (r > n) {
                    throw Py.StopIteration();
                }
                if (firstthru) {
                    firstthru = false;
                    return itertools.makeIndexedTuple(pool, indices, r);
                }
                for (int i = r - 1; i >= 0; i--) {
                    cycles[i] -= 1;
                    if (cycles[i] == 0) {
                        // rotate indices at the ith position
                        int first = indices[i];
                        for (int j = i; j < n - 1; j++) {
                            indices[j] = indices[j + 1];
                        }
                        indices[n - 1] = first;
                        cycles[i] = n - i;
                    } else {
                        int j = cycles[i];
                        int index = indices[i];
                        indices[i] = indices[n - j];
                        indices[n - j] = index;
                        return itertools.makeIndexedTuple(pool, indices, r);
                    }
                }
                throw Py.StopIteration();
            }
        };
    }

    @ExposedMethod(names = "__next__")
    public PyObject permutations___next__() {
        return iter.next();
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }
}
