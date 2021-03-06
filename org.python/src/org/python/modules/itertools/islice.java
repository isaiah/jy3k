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
import org.python.core.PyLong;
import org.python.core.PyNewWrapper;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PyType;

@ExposedType(name = "itertools.islice", base = PyObject.class, doc = BuiltinDocs.itertools_islice_doc)
public class islice extends PyIterator {
    public static final PyType TYPE = PyType.fromClass(islice.class);

    private ItertoolsIterator iter;
    public islice() {
        super(TYPE);
    }

    public islice(PyType subType) {
        super(subType);
    }

    /**
     * @see #islice___init__(PyObject, PyObject, PyObject, PyObject) startObj defaults to 0 and stepObj to 1
     */
    public islice(PyObject iterable, PyObject stopObj) {
        super(TYPE);
        islice___init__(iterable, Py.Zero, stopObj, Py.One);
    }

    /**
     * @see #islice___init__(PyObject, PyObject, PyObject, PyObject) stepObj defaults to 1
     */
    public islice(PyObject iterable, PyObject start,
                                    PyObject stopObj) {
        super();
        islice___init__(iterable, start, stopObj, Py.One);
    }

    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject islice_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        return new islice(subtype);
    }

    @ExposedMethod
    final void islice___init__(PyObject[] args, String[] kwds) {
        ArgParser ap = new ArgParser("islice", args, kwds, new String[] {
                "iterable", "start", "stop", "step"}, 2);

        PyObject iterable = ap.getPyObject(0);
        if (args.length == 2) {
            PyObject stopObj = ap.getPyObject(1);
            islice___init__(iterable, new PyLong(0), stopObj, new PyLong(1));
        }
        else {
            PyObject startObj = ap.getPyObject(1);
            PyObject stopObj = ap.getPyObject(2);
            if (args.length == 3) {
                islice___init__(iterable, startObj, stopObj, new PyLong(1));
            }
            else {
                PyObject stepObj = ap.getPyObject(3);
                islice___init__(iterable, startObj, stopObj, stepObj);
            }
        }
    }

    /**
     * Creates an iterator that returns selected values from an iterable.
     *
     * @param startObj
     *            the index of where in the iterable to start returning values
     * @param stopObj
     *            the index of where in the iterable to stop returning values
     * @param stepObj
     *            the number of steps to take between each call to <code>next()</code>
     */
    private void islice___init__(final PyObject iterable, PyObject startObj,
                                       PyObject stopObj, PyObject stepObj) {
        final int start = itertools.py2int(startObj, 0, "Start argument must be a non-negative integer or None");
        final int step = itertools.py2int(stepObj, 1, "Step argument must be a non-negative integer or None");
        final int stopArg = itertools.py2int(stopObj, 0, "Stop argument must be a non-negative integer or None");
        final int stop = stopObj instanceof PyNone ? Integer.MAX_VALUE : stopArg;

        if (start < 0 || step < 0 || stop < 0) {
            throw Py.ValueError("Indices for islice() must be non-negative integers");
        }

        if (step == 0) {
            throw Py.ValueError("Step must be one or larger for islice()");
        }

        iter = new ItertoolsIterator() {
            int counter = start;

            int lastCount = 0;

            PyObject iter = getIter(iterable);

            public PyObject next() {
                PyObject result = null;

                // ensure we never move the underlying iterator past 'stop'
                while (lastCount < Math.min(counter + 1, stop)) {
                    result = nextElement(iter);
                    lastCount++;
                }

                if (lastCount - 1 == counter) {
                    counter += step;
                    return result;
                }

                throw Py.StopIteration();
            }

        };
    }

    @ExposedMethod(names = {"__next__"})
    public PyObject islice___next__() {
        return iter.next();
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }
}
