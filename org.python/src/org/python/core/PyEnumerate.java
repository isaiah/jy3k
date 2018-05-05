/* Copyright (c) Jython Developers */
package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;

/**
 * The Python builtin enumerate type.
 */
@ExposedType(name = "enumerate", base = PyObject.class, doc = BuiltinDocs.enumerate_doc)
public class PyEnumerate extends PyIterator {
    //note: Already implements Traverseproc, inheriting it from PyIterator

    public static final PyType TYPE = PyType.fromClass(PyEnumerate.class);

    /** Current index of enumeration. */
    private PyObject index;     // using PyObject so we are not limited to sys.maxint or Integer.MAX_VALUE

    /** Secondary iterator of enumeration. */
    private PyObject sit;

    public PyEnumerate(PyType subType) {
        super(subType);
    }

    public PyEnumerate(PyType subType, PyObject seq, PyObject start) {
        super(subType);
        index = start;
        sit = PyObject.getIter(seq);
    }

    @ExposedSlot(SlotFunc.ITER)
    public static PyObject enumerate___iter__(PyObject self) {
        return self;
    }

    @ExposedNew
    public final static PyObject enumerate_new(PyNewWrapper new_, boolean init, PyType subtype,
                                               PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("enumerate", args, keywords, new String[] {"sequence", "start"});
        PyObject seq = ap.getPyObject(0);
        PyObject start = ap.getPyObject(1, Py.newInteger(0));
        if (!start.isIndex()) {
            throw Py.TypeError("an integer is required");
        }

        return new PyEnumerate(subtype, seq, start);

    }

    @ExposedSlot(SlotFunc.ITER_NEXT)
    public static PyObject enumerate___next__(PyObject enumerate) {
        PyEnumerate self = (PyEnumerate) enumerate;
        PyObject nextItem;

        nextItem = PyObject.iterNext(self.sit);
        if (nextItem == null) {
            if (self.sit instanceof PyIterator && ((PyIterator)self.sit).stopException != null) {
                throw ((PyIterator)self.sit).stopException;
            }
            throw Py.StopIteration();
        }

        PyObject next = new PyTuple(self.index, nextItem);
        self.index = self.index._add(Py.getThreadState(), Py.newInteger(1));

        return next;
    }


    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        int retValue = super.traverse(visit, arg);
        if (retValue != 0) {
            return retValue;
        }
        if (index != null) {
            retValue = visit.visit(index, arg);
            if (retValue != 0) {
                return retValue;
            }
        }
        return sit == null ? 0 : visit.visit(sit, arg);
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        return ob != null && (ob == index || ob == sit || super.refersDirectlyTo(ob));
    }
}
