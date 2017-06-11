package org.python.modules.itertools;

import org.python.core.ArgParser;
import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedModule;
import org.python.expose.ExposedNew;
import org.python.expose.ExposedType;

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
        this.iter = iter;
        this.binop = binop;
        this.total = null;
    }

    @ExposedNew
    final static PyObject accumulate_new(PyNewWrapper new_, boolean init, PyType subtype,
                                  PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("accumulate", args, keywords, "iterable", "func");
        PyObject iterable = ap.getPyObject(0);
        PyObject func = ap.getPyObject(1, null);
        return new accumulate(iterable.__iter__(), func);
    }

    @Override
    @ExposedMethod(names = {"__next__"})
    public PyObject __next__() {
        PyObject val, newtotal;
        val = iter.__next__();
        if (total == null) {
            total = val;
            return total;
        }
        if (binop == null) {
            newtotal = total._add(val);
        } else {
            newtotal = binop.__call__(total, val);
        }
        if (newtotal == null) {
            throw Py.StopIteration();
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
