package org.python.modules.itertools;

import org.python.core.ArgParser;
import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyIterator;
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
    private PyIterator iter;

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

        iter = new itertools.ItertoolsIterator() {
            boolean firstthru = true;

            @Override
            public PyObject __next__() {
                if (r > n) { return null; }
                if (firstthru) {
                    firstthru = false;
                    return itertools.makeIndexedTuple(pool, indices);
                }
                int i;
                for (i = r-1; i >= 0 && indices[i] == i+n-r ; i--);
                if (i < 0) return null;
                indices[i]++;
                for (int j = i+1; j < r; j++) {
                    indices[j] = indices[j-1] + 1;
                }
                return itertools.makeIndexedTuple(pool, indices);
            }
        };
    }

    @ExposedMethod(names = "__next__")
    @Override
    public PyObject __next__() {
        return doNext(iter.__next__());
    }

    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        int retVal = super.traverse(visit, arg);
        if (retVal != 0) {
            return retVal;
        }
        return iter != null ? visit.visit(iter, arg) : 0;
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        return ob != null && (iter == ob || super.refersDirectlyTo(ob));
    }
}
