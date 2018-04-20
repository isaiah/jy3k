package org.python.core;

// Objects/abstract.c

import org.python.core.linker.InvokeByName;
import org.python.core.stringlib.Encoding;

import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Abstract Object Interface
 */
public class Abstract {
    private static final InvokeByName bool = new InvokeByName("__bool__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName call = new InvokeByName("__call__", PyObject.class, PyObject.class, ThreadState.class, PyObject[].class, String[].class);
    private static final InvokeByName divmod = new InvokeByName("__divmod__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class, PyObject.class);
    private static final InvokeByName rdivmod = new InvokeByName("__rdivmod__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class, PyObject.class);
    private static final InvokeByName float$ = new InvokeByName("__float__", PyObject.class, PyObject.class, ThreadState.class);
    private static final InvokeByName format = new InvokeByName("__format__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class, PyObject.class);
    private static final InvokeByName int$ = new InvokeByName("__int__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName index = new InvokeByName("__index__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName len = new InvokeByName("__len__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName lenHint = new InvokeByName("__length_hint__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName repr = new InvokeByName("__repr__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName str = new InvokeByName("__str__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName trunc = new InvokeByName("__trunc__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName contains = new InvokeByName("__contains__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class, PyObject.class);
    private final static InvokeByName pos = new InvokeByName("__pos__", PyObject.class, PyObject.class, ThreadState.class);
    private final static InvokeByName neg = new InvokeByName("__neg__", PyObject.class, PyObject.class, ThreadState.class);
    private final static InvokeByName invert = new InvokeByName("__invert__", PyObject.class, PyObject.class, ThreadState.class);
    private static final InvokeByName getitem = new InvokeByName("__getitem__", PyType.class, PyObject.class, ThreadState.class, PyObject.class, PyObject.class);
    private static final InvokeByName setitem = new InvokeByName("__setitem__", PyType.class, PyObject.class, ThreadState.class, PyObject.class, PyObject.class, PyObject.class);
//    private static final InvokeByName getattribute = new InvokeByName("__getattribute__", PyType.class, PyObject.class, ThreadState.class, PyObject.class, PyObject.class);
//    private static final InvokeByName getattr = new InvokeByName("__getattr__", PyType.class, PyObject.class, ThreadState.class, PyObject.class, PyObject.class);
    /**
     * Check whether ob is in sequence seq
     * @param seq
     * @param ob
     * @return
     */
    public static boolean PySequence_Contains(PyObject seq, PyObject ob) {
        return PySequence_Contains(ob, seq, Py.getThreadState());
    }

    public static PyObject PySequence_SetItem(PyObject o, int key, PyObject value) {
        PyType tp = o.getType();
        try {
            if (key < 0) {
                int len = (int) tp.sqLen.invokeExact(o);
                key += len;
            }
            tp.sqAssItem.invokeExact(o, key, value);
        } catch (Throwable e) {
            throw Py.JavaError(e);
        }
        return Py.None;
    }

    public static PyObject PySequence_GetItem(PyObject o, int keyValue) {
        PyType tp = o.getType();
        try {
            if (keyValue < 0) {
                int len = (int) tp.sqLen.invokeExact(o);
                keyValue += len;
            }
            return (PyObject) tp.sqItem.invokeExact(o, keyValue);
        } catch (Throwable e) {
            throw Py.JavaError(e);
        }
    }

    public static PyObject _PyObject_GetAttrId(PyObject o, String attr) {
        PyType tp = o.getType();
        if (tp.getattro != null) {
            try {
                return (PyObject) tp.getattro.invokeExact(o, attr);
            } catch (Throwable throwable) {
                throw Py.JavaError(throwable);
            }
        }
        return PyObject.PyObject_GenericGetAttr(o, attr);
//        throw Py.AttributeError(String.format("%s has no attribute %s", tp.fastGetName(), attr));
    }

    /**
     * Wether ob is in sequence seq, used by bytecode
     * @param ob
     * @param seq
     * @param ts
     * @return
     */
    public static boolean PySequence_Contains(PyObject ob, PyObject seq, ThreadState ts) {
        PyType tp = seq.getType();
        if (tp.sqContains != null) {
            try {
                return (boolean) tp.sqContains.invokeExact(seq, ob);
            } catch (Throwable throwable) {
                throw Py.JavaError(throwable);
            }
        }
        try {
            Object func = contains.getGetter().invokeExact((PyObject) tp);
            return PyObject_IsTrue((PyObject) contains.getInvoker().invoke(func, ts, seq, ob), ts);
        } catch (PyException e) {
            if (e.match(Py.AttributeError)) {
                return _PySequence_Stream(seq).anyMatch(el -> Objects.equals(el, ob));
            }
            throw e;
        } catch (Throwable throwable) {
            throw Py.JavaError(throwable);
        }
    }

    public static int PyObject_Size(PyObject self, ThreadState ts) {
        PyType tp = self.getType();
        if (tp.sqLen != null) {
            try {
                return (int) tp.sqLen.invokeExact(self);
            } catch (Throwable throwable) {
                throw Py.JavaError(throwable);
            }
        }
        try {
            Object lenFunc = len.getGetter().invokeExact((PyObject) self.getType());
            PyObject ret = (PyObject) len.getInvoker().invokeExact(lenFunc, ts, self);
            return ret.asInt();
        } catch (Throwable throwable) {
            throw Py.JavaError(throwable);
        }
    }

    // called from generated code, is easier to have the threadstate in the end
    public static boolean PyObject_IsTrue(PyObject self, ThreadState ts) {
        if (self == Py.True) {
            return true;
        } else if (self == Py.False || self == Py.None) {
            return false;
        }
        PyType tp = self.getType();
        try {
            if (tp.nbBool != null) {
                return (boolean) tp.nbBool.invokeExact(self);
            } else if (tp.sqLen != null) {
                return ((int) tp.sqLen.invokeExact(self)) > 0;
            }
        } catch (Throwable e) {
            throw Py.JavaError(e);
        }

        try {
            Object boolFunc = bool.getGetter().invokeExact((PyObject) tp);
            PyObject ret = (PyObject) bool.getInvoker().invokeExact(boolFunc, ts, self);
            return ret.isTrue();
        } catch (PyException e) {
            if (e.match(Py.AttributeError)) {
                try {
                    Object lenFunc = len.getGetter().invokeExact((PyObject) self.getType());
                    PyObject ret = (PyObject) len.getInvoker().invokeExact(lenFunc, ts, self);
                    return ret.do_richCompareBool(Py.Zero, CompareOp.GT);
                } catch (PyException e1) {
                    if (e1.match(Py.AttributeError)) {
                        return self.isTrue();
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

    public static boolean PyObject_IsTrue(ThreadState ts, PyObject self) {
        if (self == Py.True) {
            return true;
        } else if (self == Py.False || self == Py.None) {
            return false;
        }
        return PyObject_IsTrue(self, ts);
    }

    public static PyObject PyNumber_Positive(ThreadState ts, PyObject obj) {
        return unaryOp(ts, pos, "+", obj);
    }

    public static PyObject PyNumber_Negative(ThreadState ts, PyObject obj) {
        return unaryOp(ts, neg, "-", obj);
    }

    public static PyObject PyNumber_Invert(ThreadState ts, PyObject obj) {
        return unaryOp(ts, invert, "~", obj);
    }

    /**
     * Return a Python int from the object item.
     * Raise TypeError if the result is not an int
     * or if the object cannot be interpreted as an index.
     * @param ts
     * @param item
     * @return
     */
    public static PyObject PyNumber_Index(ThreadState ts, PyObject item) {
        PyObject ret = PyObject.unaryOp(ts, index, item, self -> {
            throw Py.TypeErrorFmt("'%s' object cannot be interpreted as an integer", self);
        });

        if (ret instanceof PyLong) {
            return ret;
        }
        throw Py.TypeErrorFmt("__index__ returned non-int (type %s)", ret);
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
            Object intFunc = int$.getGetter().invokeExact((PyObject) obj.getType());
            return (PyObject) int$.getInvoker().invokeExact(intFunc, ts, obj);
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

    public static PyObject PyNumber_Divmod(ThreadState ts, PyObject x, PyObject y) {
        PyType tpx = x.getType();
        if (tpx.nbDivmod != null) {
            try {
                return (PyObject) tpx.nbDivmod.invokeExact(x, y);
            } catch (Throwable throwable) {
                throw Py.JavaError(throwable);
            }
        }
        return PyObject.binOp(ts, divmod, rdivmod, x, y, n -> n);
    }

    public static Stream<PyObject> _PySequence_Stream(PyObject seq) {
        Spliterator<PyObject> iterator = new PyObjectSpliterator(seq);
        return StreamSupport.stream(iterator, false);
    }

    public static boolean _PyObject_IsAbstract(PyObject callable) {
        return PyObject_IsTrue(_PyObject_GetAttrId(callable, "__isabstractmethod__"), Py.getThreadState());
    }

    public static class PyObjectSpliterator implements Spliterator<PyObject> {
        private PyObject iter;

        public PyObjectSpliterator(PyObject iter) {
            this.iter = PyObject.getIter(iter);
        }

        @Override
        public boolean tryAdvance(Consumer<? super PyObject> action) {
            try {
                PyObject next = PyObject.iterNext(iter);
                action.accept(next);
                return true;
            } catch (PyException e) {
                if (e.match(Py.StopIteration)) {
                    return false;
                }
                throw e;
            }
        }

        @Override
        public Spliterator<PyObject> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            try {
                Object func = lenHint.getGetter().invokeExact(iter);
                PyObject length = (PyObject) lenHint.getInvoker().invokeExact(func, Py.getThreadState());
                return length.asLong();
            } catch (Throwable e) {
                return 0;
            }
        }

        @Override
        public int characteristics() {
            return NONNULL;
        }
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

    public static PyObject PyObject_Repr(ThreadState ts, PyObject o) {
        try {
            Object reprFunc = repr.getGetter().invokeExact((PyObject) o.getType());
            return (PyObject) repr.getInvoker().invokeExact(reprFunc, ts, o);
        } catch (Throwable e) {
            throw Py.JavaError(e);
        }
    }

    public static PyObject PyObject_Str(ThreadState ts, PyObject v) {
        if (v instanceof PyUnicode) {
            return v;
        }

        PyType tp = v.getType();
        if (tp.str != null) {
            try {
                return (PyObject) tp.str.invokeExact(v);
            } catch (Throwable throwable) {
                throw Py.JavaError(throwable);
            }
        }
        PyObject res = PyObject.unaryOpType(ts, str, v, self ->  PyObject_Repr(ts, v));
        if (res instanceof PyUnicode) {
            return res;
        }
        throw Py.TypeErrorFmt("__str__ returned non-string (type %s)", res);
    }

    public static PyObject PyObject_Format(ThreadState ts, PyObject obj, PyObject formatSpec) {
        try {
            Object func = format.getGetter().invokeExact((PyObject) obj.getType());
            PyObject formatted = (PyObject) format.getInvoker().invokeExact(func, Py.getThreadState(), obj, formatSpec);
            if (!Py.isInstance(formatted, PyBytes.TYPE) && !Py.isInstance(formatted, PyUnicode.TYPE)) {
                throw Py.TypeError("instance.__format__ must return string or unicode, not " + formatted.getType().fastGetName());
            }
            return formatted;
        } catch (PyException e) {
            throw e;
        } catch (Throwable t) {
            throw Py.JavaError(t);
        }
    }

    public static void PyMapping_SetItemString(PyObject o, final String key, final PyObject value) {
        if (o instanceof PyStringMap) {
            o.__setitem__(key, value);
        } else {
            PyObject_SetItem(Py.getThreadState(), o, new PyUnicode(key), value);
        }
    }

    public static void PyObject_SetItem(ThreadState ts, PyObject o, PyObject key, PyObject value) {
        PyType tp = o.getType();
        if (tp.mqAssSubscript != null) {
            try {
                tp.mqAssSubscript.invokeExact(o, key, value);
            } catch (Throwable t) {
                throw Py.JavaError(t);
            }
            return;
        }
        if (tp.sqAssItem != null) {
            if (key.isIndex()) {
                PySequence_SetItem(o, key.asIndex(), value);
                return;
            }
            throw Py.TypeErrorFmt("Sequence index must be integer, not '%s'", key);
        }
        try {
            Object func = setitem.getGetter().invokeExact(tp);
            PyObject _ignore = (PyObject) setitem.getInvoker().invokeExact(func, ts, o, key, value);
            return;
        } catch (Throwable throwable) {
            throw Py.JavaError(throwable);
        }
    }

    public static PyObject PyMapping_GetItemString(PyObject o, final String key) {
        if (o instanceof PyStringMap) {
            return o.__finditem__(key);
        }
        return PyObject_GetItem(Py.getThreadState(), o, new PyUnicode(key));

    }

    public static PyObject PyObject_GetItem(ThreadState ts, PyObject o, PyObject key) {
        PyType tp = o.getType();
        if (tp.mqSubscript != null) {
            try {
                return (PyObject) tp.mqSubscript.invokeExact(o, key);
            } catch (Throwable throwable) {
                throw Py.JavaError(throwable);
            }
        }
        if (tp.sqItem != null) {
            int keyval = key.asIndex();
            return PySequence_GetItem(o, keyval);
        }
        try {
            Object func = getitem.getGetter().invokeExact(tp);
            return (PyObject) getitem.getInvoker().invokeExact(func, ts, o, key);
        } catch (Throwable throwable) {
            throw Py.JavaError(throwable);
        }
    }

    public static PyObject PyObject_Call(ThreadState ts, PyObject callable, PyObject... args) {
        PyType tp = callable.getType();
        if (tp.call != null) {
            try {
                return (PyObject) tp.call.invokeExact(callable, args, Py.NoKeywords);
            } catch (Throwable throwable) {
                throw Py.JavaError(throwable);
            }
        }
        try {
            Object func = call.getGetter().invokeExact(callable);
            return (PyObject) call.getInvoker().invokeExact(func, ts, args, Py.NoKeywords);
        } catch (Throwable throwable) {
            throw Py.JavaError(throwable);
        }
    }

    public static PyObject unaryOp(ThreadState ts, InvokeByName op, String sign, PyObject arg) {
        Object func = null;
        try {
            func = op.getGetter().invokeExact(arg);
            return (PyObject) op.getInvoker().invokeExact(func, ts);
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
