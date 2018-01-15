/* Copyright (c) 2012 Jython Developers */
package org.python.modules.itertools;

import org.python.core.ArgParser;
import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyIterator;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.core.Visitproc;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;

@ExposedType(name = "itertools.repeat", base = PyObject.class, doc = BuiltinDocs.itertools_repeat_doc)
public class repeat extends PyIterator {

    public static final PyType TYPE = PyType.fromClass(repeat.class);
    private PyIterator iter;
    private PyObject object;
    private int counter;

    public repeat() {
        super(TYPE);
    }

    public repeat(PyType subType) {
        super(subType);
    }

    public repeat(PyObject object) {
        super(TYPE);
        repeat___init__(object);
    }

    public repeat(PyObject object, int times) {
        super(TYPE);
        repeat___init__(object, times);
    }

    @ExposedNew
    @ExposedMethod
    final void repeat___init__(final PyObject[] args, String[] kwds) {
        ArgParser ap = new ArgParser("repeat", args, kwds, new String[] {"object", "times"}, 1);

        PyObject object = ap.getPyObject(0);
        if (args.length == 1) {
            repeat___init__(object);
        }
        else {
            int times = ap.getInt(1);
            repeat___init__(object, times);
        }
    }

    /**
     * Creates an iterator that returns the same object the number of times given by
     * <code>times</code>.
     */
    private void repeat___init__(final PyObject object, final int times) {
        this.object = object;
        if (times < 0) {
            counter = 0;
        } else {
            counter = times;
        }
        iter = new PyIterator() {

            public PyObject __next__() {
                if (counter > 0) {
                    counter--;
                    return object;
                }
                throw Py.StopIteration();
            }

        };
    }

    /**
     * Creates an iterator that returns the same object over and over again.
     */
    private void repeat___init__(final PyObject object) {
        this.object = object;
        counter = -1;
        iter = new PyIterator() {
            public PyObject __next__() {
                return object;
            }
        };
    }

    @ExposedMethod
    final PyObject __copy__() {
        return new repeat(object, counter);
    }

    @ExposedMethod
    public PyUnicode __repr__() {
        if (counter >= 0) {
            return (PyUnicode) (Py.newUnicode("repeat(%r, %d)").
                    __mod__(new PyTuple(object, Py.newInteger(counter))));
        }
        else {
            return (PyUnicode)(Py.newUnicode("repeat(%r)").
                    __mod__(new PyTuple(object)));
        }
    }

    @ExposedMethod
    public PyObject repeat___length_hint__() {
        if (counter >= 0) {
            return new PyLong(counter);
        }
        throw Py.TypeError("len() of unsized object");
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
        if (object != null) {
	        retVal = visit.visit(object, arg);
	        if (retVal != 0) {
	            return retVal;
	        }
        }
        return iter != null ? visit.visit(iter, arg) : 0;
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        return ob != null && (iter == ob || object == ob || super.refersDirectlyTo(ob));
    }
}
