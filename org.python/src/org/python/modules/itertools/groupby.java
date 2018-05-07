/* Copyright (c) Jython Developers */
package org.python.modules.itertools;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;
import org.python.core.ArgParser;
import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyIterator;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyRange;
import org.python.core.PyTuple;
import org.python.core.PyType;

@ExposedType(name = "itertools.groupby", base = PyObject.class, doc = BuiltinDocs.itertools_groupby_doc)
public class groupby extends PyIterator {
    public static final PyType TYPE = PyType.fromClass(groupby.class);
    private ItertoolsIterator iter;

    public groupby() {
        super(TYPE);
    }

    public groupby(PyType subType) {
        super(subType);
    }

    public groupby(PyObject iterable) {
        super(TYPE);
        groupby___init__(iterable, Py.None);
    }

    public groupby(PyObject iterable, PyObject keyfunc) {
        super(TYPE);
        groupby___init__(iterable, keyfunc);
    }

    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject groupby_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        return new groupby(subtype);
    }

    /**
     * Creates an iterator that returns the items of the iterable for which
     * <code>predicate(item)</code> is <code>true</code>. If <code>predicate</code> is null
     * (None) return the items that are true.
     */
    @ExposedMethod
    final void groupby___init__(PyObject[] args, String[] kwds) {
        ArgParser ap = new ArgParser("groupby", args, kwds, "iterable", "key");
        if(args.length > 2){
            throw Py.TypeError("groupby takes two arguments, iterable and key");
        }
        PyObject iterable = ap.getPyObject(0);
        PyObject keyfunc = ap.getPyObject(1, Py.None);

        groupby___init__(iterable, keyfunc);
    }

    private void groupby___init__(final PyObject iterable, final PyObject keyfunc) {
        iter = new ItertoolsIterator() {
            PyObject currentKey = new PyRange(0);
            PyObject currentValue = currentKey;
            PyObject targetKey = currentKey;
            PyObject iterator = getIter(iterable);

            public PyObject next() {
                while (currentKey.equals(targetKey)) {
                    currentValue = nextElement(iterator);
                    if (keyfunc == Py.None) {
                        currentKey = currentValue;
                    } else {
                        currentKey = keyfunc.__call__(currentValue);
                    }
                }
                targetKey = currentKey;
                return new PyTuple(currentKey, new PyGrouper(currentValue, currentKey, keyfunc, iterator));
            }
        };
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod(names = {"__next__"})
    public PyObject groupby___next__() {
        return iter.next();
    }
}
