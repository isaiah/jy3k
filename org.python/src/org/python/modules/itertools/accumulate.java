package org.python.modules.itertools;

import org.python.core.ArgParser;
import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;

/**
 * Created by isaiah
 */
@ExposedType(doc = BuiltinDocs.itertools_accumulate_doc)
public class accumulate extends PyObject {
    public static final PyType TYPE = PyType.fromClass(accumulate.class);

    private PyObject total;
    private PyObject iter;
    private PyObject binop;

    public accumulate(PyObject iter, PyObject binop) {
        super(TYPE);
        this.iter = PyObject.getIter(iter);
        this.binop = binop;
        this.total = null;
    }

    @ExposedNew
    final static PyObject accumulate_new(PyNewWrapper new_, boolean init, PyType subtype,
                                  PyObject[] args, String[] keywords) {
        if (args.length > 2) {
            throw Py.TypeError(String.format("accumulate() takes at most 2 arguments (%d given)", args.length));
        }
        ArgParser ap = new ArgParser("accumulate", args, keywords, "iterable", "func");
        PyObject iterable = ap.getPyObject(0);
        PyObject func = ap.getPyObject(1, null);
        return new accumulate(iterable, func);
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod(names = {"__next__"})
    public PyObject accumulate___next__() {
        PyObject val, newtotal;
        val = PyObject.iterNext(iter);

        if (total == null) {
            total = val;
            return total;
        }
        if (binop == null) {
            newtotal = total._add(Py.getThreadState(), val);
        } else {
            newtotal = binop.__call__(total, val);
        }

        total = newtotal;
        return newtotal;
    }

    @ExposedMethod
    public PyObject accumulate___set_state__(PyObject state) {
        total = state;
        return Py.None;
    }
}
