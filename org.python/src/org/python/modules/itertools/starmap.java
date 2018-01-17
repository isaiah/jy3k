/* Copyright (c) 2012 Jython Developers */
package org.python.modules.itertools;

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

@ExposedType(name = "itertools.starmap", base = PyObject.class, doc = BuiltinDocs.itertools_starmap_doc)
public class starmap extends PyIterator {
    public static final PyType TYPE = PyType.fromClass(starmap.class);
    private ItertoolsIterator iter;

    public starmap() {
        super(TYPE);
    }

    public starmap(PyType subType) {
        super(subType);
    }

    public starmap(PyObject callable, PyObject iterator) {
        super(TYPE);
        starmap___init__(callable, iterator);
    }

    /**
     * Create an iterator whose <code>next()</code> method returns the result
     * of calling the function (first argument) with a tuple of arguments
     * returned from the iterable (second argument).
     *
     * @param starargs
     *            [0] = callable function, [1] = iterable with argument tuples
     */
    @ExposedNew
    @ExposedMethod
    final void starmap___init__(PyObject[] starargs, String[] kwds) {
        if (starargs.length != 2) {
            throw Py.TypeError("starmap requires 2 arguments, got "
                    + starargs.length);
        }
        final PyObject callable = starargs[0];
        final PyObject iterator = getIter(starargs[1]);

        starmap___init__(callable, iterator);
    }

    private void starmap___init__(final PyObject callable, final PyObject iterator) {
        iter = new ItertoolsIterator() {

            public PyObject next() {
                PyObject args = nextElement(iterator);
                PyObject result = null;

                if (args != null) {
                    PyTuple argTuple = PyTuple.fromIterable(args);
                    // convert to array of PyObjects in call to function
                    result = callable.__call__(argTuple.getArray());
                }
                return result;
            }

        };
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod(names = "__next__")
    public PyObject starmap___next__() {
        return iter.next();
    }
}
