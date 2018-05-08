package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;

@ExposedType(name = "wrapper_descriptor")
public class PyWrapperDescr extends PyMethodDescr {
    public static final PyType TYPE = PyType.fromClass(PyWrapperDescr.class);

    public PyWrapperDescr(PyType dtype, PyBuiltinMethod method) {
        super(dtype, method);
    }

    @Override
    @ExposedMethod(defaults = "null")
    public PyObject __get__(PyObject obj, PyObject type) {
        if (obj != Py.None && obj != null) {
            checkGetterType(obj.getType());
            return new PyMethodWrapper(this, obj);
        }
        return this;
    }

    @ExposedSlot(SlotFunc.CALL)
    public static PyObject call(PyObject obj, PyObject[] args, String[] keywords) {
        PyWrapperDescr self = (PyWrapperDescr) obj;
        return self.__call__(args, keywords);
    }

    @Override
    public String toString() {
        return String.format("<slot wrapper '%s' of '%s' objects>", name, dtype.fastGetName());
    }
}
