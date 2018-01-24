package org.python.core;

// Objects/abstract.c

import org.python.core.linker.InvokeByName;
import org.python.core.stringlib.Encoding;

/**
 * Abstract Object Interface
 */
public class Abstract {
    private static final InvokeByName bool = new InvokeByName("__bool__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName len = new InvokeByName("__len__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName float$ = new InvokeByName("__float__", PyObject.class, PyObject.class, ThreadState.class);
    private static final InvokeByName int$ = new InvokeByName("__int__", PyObject.class, PyObject.class, ThreadState.class);
    private static final InvokeByName trunc = new InvokeByName("__trunc__", PyObject.class, PyObject.class, ThreadState.class);

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

    public static PyObject PyNumber_Long(ThreadState ts, PyObject obj) {
        if (obj == null) {
            return Py.Zero;
        }
        if (obj instanceof PyUnicode) {
            return Encoding.atol(((PyUnicode) obj).getString(), 10);
        } else if (obj instanceof PyBytes) {
            return Encoding.atol(((PyBytes) obj).getString(), 10);
        } else if (obj instanceof BufferProtocol) {
            return Encoding.atol(new String(Py.unwrapBuffer(obj)), 10);
        }
        try {
            Object intFunc = int$.getGetter().invokeExact((obj));
            return (PyObject) int$.getInvoker().invokeExact(intFunc, ts);
        } catch (PyException pye) {
            if (!pye.match(Py.AttributeError)) {
                throw pye;
            }
            PyObject integral = PyObject.unaryOp(ts, trunc, obj, (self) -> {
                throw Py.TypeError(String.format(
                        "long() argument must be a string a bytes-like object or a number, not '%.200s'", obj.getType()
                                .fastGetName()));
            });
            return convertIntegralToLong(ts, integral);
        } catch (Throwable throwable) {
            throw Py.JavaError(throwable);
        }
    }

    public static PyObject PyNumber_Float(ThreadState ts, PyObject value) {
        PyObject floatObject;
        try {
            Object floatFunc = float$.getGetter().invokeExact(value);
            floatObject = (PyObject) float$.getInvoker().invokeExact(floatFunc, Py.getThreadState());
        } catch (PyException e) {
            if (!e.match(Py.AttributeError)) {
                throw e;
            }
            floatObject = floatFromString(value);
        } catch (Throwable throwable) {
            throw Py.JavaError(throwable);
        }
        return floatObject;
    }

    private static PyObject convertIntegralToLong(ThreadState ts, PyObject integral) {
        if (!(integral instanceof PyLong)) {
            return PyObject.unaryOp(ts, int$, integral, (self) -> {
                throw Py.TypeError(String.format("__trunc__ returned non-Integral (type %.200s)",
                        self.getType().fastGetName()));
            });
        }
        return integral;
    }

    public static PyFloat floatFromString(PyObject v) {
        String s = null;
        if (v instanceof PyUnicode) {
            s = ((PyUnicode) v).encodeDecimal();
        } else if (v instanceof PyBytes) {
            s = v.asString();
        } else if (v instanceof PyByteArray) {
            s = v.toString();
        } else if (v instanceof BufferProtocol) {
            s = ((BufferProtocol) v).getBuffer().asCharBuffer().toString();
        } else {
            throw Py.TypeError(String.format("float() argument must be a string or a number, not '%s'", v.getType().fastGetName()));
        }
        s = s.replaceAll("_", "");
        return new PyFloat(Encoding.atof(s, BuiltinModule.repr(v).toString()));
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
