package org.python.modules.itertools;

import org.python.core.ArgParser;
import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyIterator;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.Visitproc;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;

import java.util.ArrayList;
import java.util.List;

@ExposedType(name = "itertools.cycle", base = PyObject.class, doc = BuiltinDocs.itertools_cycle_doc)
public class cycle extends PyIterator {

    public static final PyType TYPE = PyType.fromClass(cycle.class);
    private PyIterator iter;

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
        ArgParser ap = new ArgParser("cycle", args, kwds, new String[] {"iterable"}, 1);
        ap.noKeywords();
        cycle___init__(ap.getPyObject(0));
    }

    private void cycle___init__(final PyObject sequence) {
        iter = new itertools.ItertoolsIterator() {
            List<PyObject> saved = new ArrayList<PyObject>();
            int counter = 0;
            PyObject iterator = sequence.__iter__();

            boolean save = true;

            public PyObject __next__() {
                if (save) {
                    try {
                    PyObject obj = iterator.__next__();
                        if (obj != null) {
                            saved.add(obj);
                            return obj;
                        }
                        save = false;
                    } catch (PyException e) {
                        if (!e.match(Py.StopIteration)) {
                            throw e;
                        }
                        save = false;
                    }
                }
                if (saved.size() == 0) {
                    return null;
                }

                // pick element from saved List
                if (counter >= saved.size()) {
                    // start over again
                    counter = 0;
                }
                return saved.get(counter++);
            }

        };
    }

    @Override
    @ExposedMethod(names = "__iter__")
    public PyObject __iter__() {
        return this;
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

