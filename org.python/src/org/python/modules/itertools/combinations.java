package org.python.modules.itertools;

import org.python.annotations.ExposedSlot;
import org.python.annotations.SlotFunc;
import org.python.core.ArgParser;
import org.python.core.BuiltinDocs;
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

@ExposedType(name = "itertools.combinations", base = PyObject.class, doc = BuiltinDocs.itertools_combinations_doc)
public class combinations extends PyIterator {
    public static final PyType TYPE = PyType.fromClass(combinations.class);
    private ItertoolsIterator iter;

    public combinations() {
        super(TYPE);
    }

    public combinations(PyType subType) {
        super(subType);
    }

    public combinations(PyObject iterable, int r) {
        super(TYPE);
        combinations___init__(iterable, r);
    }

    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject combinations_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        return new combinations(subtype);
    }

    @ExposedMethod
    final void combinations___init__(PyObject[] args, String[] kwds) {
        if (args.length > 2) {
            throw Py.TypeError(String.format(
                "combinations_with_replacement() takes at most 2 arguments (%d given)", args.length));
        }
        ArgParser ap = new ArgParser("combinations_with_replacement", args, kwds, "iterable", "r");
        PyObject iterable = ap.getPyObject(0);
        int r = ap.getInt(1);
        if (r < 0) {
            throw Py.ValueError("r must be non-negative");
        }
        combinations___init__(iterable, r);
    }

    private void combinations___init__(PyObject iterable, final int r) {
        if (r < 0) throw Py.ValueError("r must be non-negative");
        final PyTuple pool = PyTuple.fromIterable(iterable);
        final int n = pool.__len__();
        final int indices[] = new int[r];
        for (int i = 0; i < r; i++) {
            indices[i] = i;
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
                    return itertools.makeIndexedTuple(pool, indices);
                }
                int i;
                for (i = r-1; i >= 0 && indices[i] == i+n-r ; i--);
                if (i < 0) {
                    throw Py.StopIteration();
                }
                indices[i]++;
                for (int j = i+1; j < r; j++) {
                    indices[j] = indices[j-1] + 1;
                }
                return itertools.makeIndexedTuple(pool, indices);
            }
        };
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod(names = "__next__")
    public PyObject combinations___next__() {
        return iter.next();
    }
}
