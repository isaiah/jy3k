/* Copyright (c) 2012 Jython Developers */
package org.python.modules.itertools;

import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyIterator;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;

@ExposedType(name = "itertools.product", base = PyObject.class, doc = BuiltinDocs.itertools_product_doc)
public class product extends PyIterator {

    public static final PyType TYPE = PyType.fromClass(product.class);
    private int[] indices;
    private boolean firstthru = true;
    private int numPools;
    private PyTuple[] pools;

    public product() {
        super();
    }

    public product(PyType subType) {
        super(subType);
    }

    public product(PyTuple[] tuples, int repeat) {
        super();
        product___init__(tuples, repeat);
    }

    @ExposedNew
    @ExposedMethod
    final void product___init__(PyObject[] args, String[] kws) {
        final int repeat;
        final int num_iterables;
        if (kws.length == 1 && kws[0] == "repeat") {
            repeat = args[args.length - 1].asInt();
            if (repeat < 0) {
                throw Py.ValueError("repeat argument cannot be negative");
            }
            num_iterables = args.length - 1;
        } else {
            repeat = 1;
            num_iterables = args.length;
        }
        final PyTuple tuples[] = new PyTuple[num_iterables];
        for (int i = 0; i < num_iterables; i++) {
            tuples[i] = PyTuple.fromIterable(args[i]);
        }
        product___init__(tuples, repeat);
    }

    private void product___init__(PyTuple[] tuples, int repeat) {
        // Make repeat duplicates, in order
        numPools = tuples.length * repeat;
        pools = new PyTuple[numPools];
        for (int r = 0; r < repeat; r++) {
            System.arraycopy(tuples, 0, pools, r * tuples.length, tuples.length);
        }
        indices = new int[numPools];
    }

    @ExposedMethod(names = "__next__")
    public PyObject product___next__() {
        if (firstthru) {
            for (PyTuple pool : pools) {
                if (pool.__len__() == 0) {
                    throw Py.StopIteration();
                }
            }
            firstthru = false;
            return makeTuple();
        }
        for (int i = numPools - 1; i >= 0; i--) {
            indices[i]++;

            if (indices[i] == pools[i].__len__()) {
                indices[i] = 0;
            } else {
                return makeTuple();
            }
        }
        throw Py.StopIteration();
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod(names = {"__reduce__"})
    public PyObject reduce() {
        return new PyTuple(TYPE, new PyTuple(pools));
    }

    @ExposedMethod(names = {"__setstate__"})
    public void setstate(PyObject state) {
    }

    private PyTuple makeTuple() {
        PyObject items[] = new PyObject[numPools];
        for (int i = 0; i < numPools; i++) {
            items[i] = pools[i].__getitem__(indices[i]);
        }
        return new PyTuple(items);
    }
}

