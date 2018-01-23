package org.python.core;

// Objects/abstract.c

import org.python.core.linker.InvokeByName;

/**
 * Abstract Object Interface
 */
public class Abstract {
    private static final InvokeByName bool = new InvokeByName("__bool__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName len = new InvokeByName("__len__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);

    // called from generated code, is easier without the threadstate argument
    public static boolean PyObject_IsTrue(PyObject obj) {
        if (obj == Py.True) {
            return true;
        } else if (obj == Py.False || obj == Py.None) {
            return false;
        }
        return PyObject_IsTrue(Py.getThreadState(), obj);
    }

    public static boolean PyObject_IsTrue(ThreadState ts, PyObject obj) {
        if (obj == Py.True) {
            return true;
        } else if (obj == Py.False || obj == Py.None) {
            return false;
        }
        try {
            Object boolFunc = bool.getGetter().invokeExact((PyObject) obj.getType());
            PyObject ret = (PyObject) bool.getInvoker().invokeExact(boolFunc, ts, obj);
            return ret.isTrue();
        } catch (PyException e) {
            if (e.match(Py.AttributeError)) {
                try {
                    Object lenFunc = len.getGetter().invokeExact((PyObject) obj.getType());
                    PyObject ret = (PyObject) len.getInvoker().invokeExact(lenFunc, ts, obj);
                    return ret.do_richCompareBool(Py.Zero, CompareOp.NE);
                } catch (PyException e1) {
                    if (e1.match(Py.AttributeError)) {
                        return obj.isTrue();
                    }
                    throw e1;
                } catch (Throwable t) {
                    throw Py.JavaError(t);
                }
            }
            throw e;
        } catch (Throwable t) {
            throw Py.JavaError(t);
        }
    }

    public static PyObject PyNumber_Positive(ThreadState ts, PyObject obj) {
        return unaryOp(ts, "__pos__", "+", obj);
    }

    public static PyObject PyNumber_Negative(ThreadState ts, PyObject obj) {
        return unaryOp(ts, "__neg__", "-", obj);
    }

    public static PyObject PyNumber_Invert(ThreadState ts, PyObject obj) {
        return unaryOp(ts, "__invert__", "~", obj);
    }

    public static PyObject PyObject_Not(ThreadState ts, PyObject obj) {
        return PyObject_IsTrue(ts, obj) ? Py.False : Py.True;
    }

    public static PyObject unaryOp(ThreadState ts, String name, String sign, PyObject arg) {
        InvokeByName pos = new InvokeByName(name, PyObject.class, PyObject.class, ThreadState.class);
        Object func = null;
        try {
            func = pos.getGetter().invokeExact(arg);
            return (PyObject) pos.getInvoker().invokeExact(func, ts);
        } catch (PyException e) {
            if (e.match(Py.AttributeError)) {
                throw Py.TypeError(String.format("bad operand type for unary %s: '%.200s'", sign,
                        arg.getType().fastGetName()));
            }
            throw e;
        } catch (Throwable t) {
            throw Py.JavaError(t);
        }
    }
}
