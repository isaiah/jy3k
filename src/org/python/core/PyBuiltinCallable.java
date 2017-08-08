/* Copyright (c) Jython Developers */
package org.python.core;

import java.io.Serializable;

import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedType;

@Untraversable
@ExposedType(name = "builtin_function_or_method", isBaseType = false)
public abstract class PyBuiltinCallable extends PyObject {

    public PyBuiltinMethodData info;

    protected PyBuiltinCallable(PyType type, PyBuiltinMethodData info) {
        super(type);
        this.info = info;
    }

    protected PyBuiltinCallable(PyBuiltinMethodData info) {
        this.info = info;
    }

    /**
     * Returns a new instance of this type of PyBuiltinFunction bound to self
     */
    abstract public PyBuiltinCallable bind(PyObject self);

    @ExposedGet(name = "__name__")
    public PyObject fastGetName() {
        return new PyUnicode(this.info.getName());
    }

    @ExposedGet(name = "__qualname__")
    public PyObject getQualname() {
        PyObject self = getSelf();
        String qualname = info.getName();
        if (self != null && self != Py.None) {
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
        return Py.None;
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
        return String.format("<built-in method %s of %s object at %s>", fastGetName(), getSelf().getType().fastGetName(), Py.idstr(getSelf()));
    }

    public interface Info extends Serializable {

        String getName();

        int getMaxargs();

        void setMaxargs(int maxArgs);

        int getMinargs();

        void setMinargs(int minArgs);

        PyException unexpectedCall(int nargs, boolean keywords);
    }
}
