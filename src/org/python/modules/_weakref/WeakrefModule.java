/* Copyright (c) Jython Developers */
package org.python.modules._weakref;

import org.python.core.ArgParser;
import org.python.core.Py;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.expose.ExposedFunction;
import org.python.expose.ExposedModule;
import org.python.expose.ModuleInit;

/**
 * The _weakref module.
 */
@ExposedModule(name = "_weakref", doc = "Weak-reference support module.")
public class WeakrefModule {
    @ModuleInit
    public static void classDictInit(PyObject dict) {
        dict.__setitem__("ref", ReferenceType.TYPE);
        dict.__setitem__("ReferenceType", ReferenceType.TYPE);
        dict.__setitem__("ProxyType", PyWeakProxy.TYPE);
        dict.__setitem__("CallableProxyType", PyWeakCallableProxy.TYPE);
    }

    @ExposedFunction
    public static PyWeakProxy proxy(PyObject[] args, String[] keywords)  {
        ArgParser ap = new ArgParser("proxy", args, keywords, "object", "callback");
        PyObject object = ap.getPyObject(0);
        PyObject callback = ap.getPyObject(1, Py.None);
        if (callback != Py.None) {
            if (object.isCallable()) {
                return new PyWeakCallableProxy(GlobalRef.newInstance(object), callback);
            }
            return new PyWeakProxy(GlobalRef.newInstance(object), callback);
        }
        GlobalRef gref = GlobalRef.newInstance(object);
        boolean callable = object.isCallable();
        PyWeakProxy ret = (PyWeakProxy)gref.find(callable ? PyWeakCallableProxy.class : PyWeakProxy.class);
        if (ret != null) {
            return ret;
        }
        if (callable) {
            return new PyWeakCallableProxy(GlobalRef.newInstance(object), null);
        }
        return new PyWeakProxy(GlobalRef.newInstance(object), null);
    }

    @ExposedFunction
    public static int getweakrefcount(PyObject object) {
        return GlobalRef.getCount(object);
    }

    @ExposedFunction
    public static PyList getweakrefs(PyObject object) {
        return GlobalRef.getRefs(object);
    }
}
