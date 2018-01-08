/* Copyright (c) Jython Developers */
package org.python.modules._weakref;

import org.python.core.PyComplex;
import org.python.core.PyFloat;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.annotations.ExposedType;

/**
 * A weak reference proxy object.
 */
@ExposedType(name = "weakproxy", isBaseType = false)
public class PyWeakProxy extends AbstractReference {

    public static final PyType TYPE = PyType.fromClass(PyWeakProxy.class);

    public PyWeakProxy(PyType subType, GlobalRef ref, PyObject callback) {
        super(subType, ref, callback);
    }

    public PyWeakProxy(GlobalRef ref, PyObject callback) {
        this(TYPE, ref, callback);
    }

    public boolean isTrue() { return py().isTrue(); }
    public int __len__() { return py().__len__(); }

    public PyObject __finditem__(PyObject key) { return py().__finditem__(key); }
    public void __setitem__(PyObject key, PyObject value) { py().__setitem__(key, value); }
    public void __delitem__(PyObject key) { py().__delitem__(key); }

    public PyObject __findattr_ex__(String name) { return py().__findattr_ex__(name); }
    public void __setattr__(String name, PyObject value) { py().__setattr__(name, value); }
    public void __delattr__(String name) { py().__delattr__(name); }

    public PyObject __iter__() { return py().__iter__(); }
    public PyUnicode __str__() { return py().__str__(); }
    public PyFloat __float__() { return py().__float__(); }
    public PyObject __int__() { return py().__int__(); }
    public PyComplex __complex__() { return py().__complex__(); }

    public PyObject __abs__() { return py().__abs__(); }


    public boolean __contains__(PyObject o) { return py().__contains__(o); }
    public PyObject __index__() { return py().__index__(); }
}
