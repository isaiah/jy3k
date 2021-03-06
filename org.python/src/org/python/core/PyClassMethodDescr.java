/* Copyright (c) Jython Developers */
package org.python.core;

import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;

import java.util.Arrays;

@Untraversable
@ExposedType(name = "classmethod_descriptor", isBaseType = false)
public class PyClassMethodDescr extends PyMethodDescr {

    public static final PyType TYPE = PyType.fromClass(PyClassMethodDescr.class);

    PyClassMethodDescr(PyType type, PyBuiltinMethod meth) {
        super(type, meth);
    }

    @Override
    public PyObject __get__(PyObject obj, PyObject type) {
        return classmethod_descriptor___get__(obj, type);
    }

    @ExposedMethod(defaults = "null")
    public PyObject classmethod_descriptor___get__(PyObject obj, PyObject type) {
        if (type == null || type == Py.None) {
            if (obj != null) {
                type = obj.getType();
            } else {
                throw Py.TypeError(String.format("descriptor '%s' for type '%s' needs either an "
                                                 + " object or a type", name, dtype.fastGetName()));
            }
        } else if (!(type instanceof PyType)) {
            throw Py.TypeError(String.format("descriptor '%s' for type '%s' needs a type, not a"
                                             + " '%s' as arg 2", name, dtype.fastGetName(),
                                             type.getType().fastGetName()));
        }
        checkGetterType((PyType)type);
        return meth.bind(type);
    }

    @ExposedSlot(SlotFunc.CALL)
    public static PyObject call(PyObject obj, PyObject[] args, String[] keywords) {
        PyClassMethodDescr self = (PyClassMethodDescr) obj;
        return Abstract.PyObject_Call(Py.getThreadState(), self.meth.bind(args[0]), Arrays.copyOfRange(args, 1, args.length), keywords);
    }

    @Override
    @ExposedGet(name = "__doc__")
    public PyObject getDoc() {
        return super.getDoc();
    }
}
