package org.python.modules;

import org.python.annotations.ExposedSlot;
import org.python.annotations.SlotFunc;
import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.core.PyType;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;

import java.util.HashMap;
import java.util.Map;

/**
 * namespace object implementation
 */
@ExposedType(name = "SimpleNamespace", doc = BuiltinDocs.SimpleNamespace_doc)
public class PyNamespace extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyNamespace.class);

    public Map<String, PyObject> dict;

    public PyNamespace(PyType subtype) {
        super(subtype);
        this.dict = new HashMap<>();
    }

    public PyNamespace(Map<String, PyObject> dict) {
        super(TYPE);
        this.dict = dict;
    }

    @Override
    @ExposedGet(name = "__dict__")
    public PyObject fastGetDict() {
        return new PyStringMap(dict);
    }

    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject SimpleNamespace_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        return new PyNamespace(subtype);
    }

    @ExposedMethod(doc = BuiltinDocs.SimpleNamespace___init___doc)
    final void SimpleNamespace___init__(PyObject[] args, String[] kwds) {
        if (args.length > kwds.length) {
            throw Py.TypeError("no positional arguments expected");
        }
        for (int i = 0; i < args.length; i++) {
            dict.put(kwds[i], args[i]);
        }
    }

    @Override
    public String toString() {
        StringBuilder items = new StringBuilder("namespace(");
        boolean first = true;
        for (String key : dict.keySet()) {
            if (!first) {
                items.append(", ");
            } else {
                first = false;
            }
            items.append(key).append("=").append(dict.get(key));
        }
        return items.append(")").toString();
    }

    @ExposedMethod
    final PyObject SimpleNamespace___eq__(PyObject other) {
        return Py.newBoolean(dict.equals(other.__getattr__("__dict__")));
    }

    @ExposedMethod(doc = BuiltinDocs.SimpleNamespace___setattr___doc)
    final PyObject SimpleNamespace___setattr__(PyObject name, PyObject value) {
        dict.put(name.asString(), value);
        return Py.None;
    }

    @Override
    public void __setattr__(String name, PyObject value) {
        dict.put(name, value);
    }
}
