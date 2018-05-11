package org.python.core;

import java.util.function.Supplier;

public class Call {

    public static PyObject _PyOBject_CallMethodIdObjArgs(PyObject self, String methodId, Supplier<PyObject> noAttrHandler, PyObject... args) {
        try {
            PyObject func = Abstract._PyObject_GetAttrId(self, methodId);
            return Abstract.PyObject_Call(Py.getThreadState(), func, args, Py.NoKeywords);
        } catch (PyException e) {
            if (e.match(Py.AttributeError)) {
                return noAttrHandler.get();
            }
            throw e;
        }
    }

    public static PyObject PyObject_CallMethod(PyObject self, String methodId) {
        PyObject func = Abstract._PyObject_GetAttrId(self, methodId);
        return Abstract.PyObject_Call(Py.getThreadState(), func, Py.EmptyObjects, Py.NoKeywords);
    }
}
