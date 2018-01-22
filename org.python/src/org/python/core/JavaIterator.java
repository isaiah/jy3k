package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;

import java.util.Iterator;

@ExposedType
public class JavaIterator extends PyIterator {

    final private Iterator<Object> proxy;

    public JavaIterator(Iterable<Object> proxy) {
        this(proxy.iterator());
    }

    public JavaIterator(Iterator<Object> proxy) {
        this.proxy = proxy;
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod
    public PyObject __next__() {
        if (proxy.hasNext()) {
            return Py.java2py(proxy.next());
        }
        throw Py.StopIteration();
    }
}
