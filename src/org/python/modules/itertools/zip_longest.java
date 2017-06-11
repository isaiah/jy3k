/* Copyright (c) Jython Developers */
package org.python.modules.itertools;

import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyIterator;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.core.Visitproc;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedNew;
import org.python.expose.ExposedType;

@ExposedType(name = "itertools.zip_longest", base = PyObject.class,
    doc = BuiltinDocs.itertools_zip_longest_doc)
public class zip_longest extends PyIterator {

    public static final PyType TYPE = PyType.fromClass(zip_longest.class);
    private PyIterator iter;

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
        //XXX error checking on args

        zip_longest___init__(iterables, fillvalue);
    }

    private void zip_longest___init__(final PyObject[] iterables, final PyObject fillvalue) {
        final PyObject iterators[] = new PyObject[iterables.length];
        final boolean exhausted[] = new boolean[iterables.length];
        for (int i = 0; i < iterables.length; i++) {
            iterators[i] = iterables[i].__iter__();
            exhausted[i] = false;
        }

        iter = new itertools.ItertoolsIterator() {
            int unexhausted = iterables.length;

            @Override
            public PyObject __next__() {
                PyObject item[] = new PyObject[iterables.length];
                for (int i = 0; i < iterables.length; i++) {
                    if (exhausted[i]) {
                        item[i] = fillvalue;
                    } else {
                        PyObject elem = iterators[i].__next__();
                        if (elem == null) {
                            unexhausted--;
                            exhausted[i] = true;
                            item[i] = fillvalue;
                        } else {
                            item[i] = elem;
                        }
                    }
                }
                if (unexhausted == 0) {
                    return null;
                } else {
                    return new PyTuple(item);
                }
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
