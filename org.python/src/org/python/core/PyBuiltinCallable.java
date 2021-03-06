/* Copyright (c) Jython Developers */
package org.python.core;

import java.io.Serializable;

import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;

@Untraversable
@ExposedType(name = "builtin_function_or_method", isBaseType = false)
public abstract class PyBuiltinCallable extends PyObject {
    /* Bootstrap type, cannot initial type statically */
    // public static final PyType TYPE = PyType.fromClass(PyBuiltinCallable.class);

    public PyBuiltinMethodData info;

    protected PyBuiltinCallable(PyBuiltinMethodData info) {
        this.info = info;
    }

    @ExposedGet(name = "__name__")
    public PyObject fastGetName() {
        return new PyUnicode(this.info.getName());
    }

    @ExposedGet(name = "__qualname__")
    public PyObject getQualname() {
        PyObject self = getSelf();
        String qualname = info.getName();
        if (self != null && self != Py.None && !(self instanceof PyModule)) {
            if (!info.isStatic) {
                self = self.getType();
            }
            PyObject name = Abstract._PyObject_GetAttrId(self, "__name__");
            if (name != null) {
                qualname = name + "." + info.getName();
            }
        }
        return new PyUnicode(qualname);
    }

    @ExposedGet(name = "__doc__")
    public String getDoc() {
        return info.doc;
    }

    @ExposedGet(name = "__module__")
    public PyObject getModule() {
        return info.getModule() == null ? Py.None : new PyUnicode(info.getModule());
    }

    @ExposedGet(name = "__self__")
    public PyObject getSelf() {
        return Py.None;
    }

    @ExposedGet(name = "__text_signature__")
    public PyObject textSignature() {
        if (getSelf() != Py.None) {
            return PyType.getBuiltinDoc(getSelf().getType().fastGetName() + "_" + info.name + "_sig");
        }
        return PyType.getBuiltinDoc("builtins_" + info.name + "_sig");
    }

    @ExposedSlot(SlotFunc.CALL)
    public static PyObject call(PyObject meth, PyObject[] args, String[] keywords) {
        return meth.__call__(args, keywords);
    }

    /** pickle support **/
    @ExposedMethod
    public String __reduce_ex__(PyObject proto) {
        return info.name;
    }

    public void setInfo(PyBuiltinMethodData info) {
        this.info = info;
    }

    @Override
    public String toString() {
        if (getSelf() == null || getSelf() instanceof PyModule) {
            return String.format("<built-in function %s>", fastGetName());
        }
        return String.format("<built-in method %s of %s object at %s>", fastGetName(), getSelf().getType().fastGetName(), Py.idstr(getSelf()));
    }
}
