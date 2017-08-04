/* Copyright (c) Jython Developers */
package org.python.modules._weakref;

import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;

/**
 * ProxyType with __call__.
 */
@ExposedType(name = "weakcallableproxy", isBaseType = false)
public class PyWeakCallableProxy extends PyWeakProxy {

    public static final PyType TYPE = PyType.fromClass(PyWeakCallableProxy.class);

    public PyWeakCallableProxy(GlobalRef ref, PyObject callback) {
        super(TYPE, ref, callback);
    }

    public PyObject __call__(PyObject[] args, String[] kws) {
        return weakcallableproxy___call__(args, kws);
    }

    @ExposedMethod
    final PyObject weakcallableproxy___call__(PyObject[] args, String[] kws) {
        return py().__call__(args, kws);
    }
}
