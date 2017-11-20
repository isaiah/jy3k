/* Copyright (c) Jython Developers */
package org.python.core;

import java.io.Serializable;

import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedType;

@Untraversable
@ExposedType(name = "builtin_function_or_method", isBaseType = false)
public abstract class PyBuiltinCallable extends PyObject {

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
            PyObject name = self.__getattr__("__name__");
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

    @ExposedGet(name = "__call__")
    public PyObject makeCall() {
        return this;
    }

    @ExposedGet(name = "__self__")
    public PyObject getSelf() {
        return Py.None;
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
