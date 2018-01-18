/* Copyright (c) 2012 Jython Developers */
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

@ExposedType(name = "itertools.compress", base = PyObject.class, doc = BuiltinDocs.itertools_compress_doc)
public class compress extends PyObject {
    public static final PyType TYPE = PyType.fromClass(compress.class);

    private PyObject data;
    private PyObject selectors;

    public compress() {
        super(TYPE);
    }

    public compress(PyType subType) {
        super(subType);
    }

    public compress(PyObject dataIterable, PyObject selectorsIterable) {
        super(TYPE);
        data = getIter(dataIterable);
        selectors = getIter(selectorsIterable);
    }

    @ExposedNew
    @ExposedMethod
    final void compress___init__(final PyObject[] args, String[] kwds) {
        ArgParser ap = new ArgParser("compress", args, kwds, "data", "selectors");
        if (args.length > 2) {
            throw Py.TypeError(String.format("compress() takes at most 2 arguments (%s given)", args.length));
        }
        data = getIter(ap.getPyObject(0));
        selectors = getIter(ap.getPyObject(1));
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod(names = "__next__")
    public PyObject compress___next__() {
        while (true) {
            PyObject datum = iterNext(data);
            PyObject selector = iterNext(selectors);
            if (selector.isTrue()) {
                return datum;
            }
        }
    }

    @ExposedMethod(names = {"__reduce__"})
    public PyObject reduce() {
        return new PyTuple(TYPE, new PyTuple(data, selectors));
    }
}
