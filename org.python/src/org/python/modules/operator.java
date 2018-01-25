// Copyright (c) Corporation for National Research Initiatives
package org.python.modules;

import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedModule;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.annotations.ModuleInit;
import org.python.core.Abstract;
import org.python.core.ArgParser;
import org.python.core.BuiltinModule;
import org.python.core.CompareOp;
import org.python.core.Py;
import org.python.core.PyBytes;
import org.python.core.PyException;
import org.python.core.PyLong;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.core.ThreadState;
import org.python.core.Traverseproc;
import org.python.core.Untraversable;
import org.python.core.Visitproc;

@Untraversable
@ExposedModule(name = "_operator")
public class operator {
    @ExposedFunction(names = {"add", "concat"})
    public static PyObject add(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._add(ts, arg2);
    }

    @ExposedFunction
    public static PyObject and_(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._and(ts, arg2);
    }

    @ExposedFunction
    public static PyObject lshift(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._lshift(ts, arg2);
    }

    @ExposedFunction
    public static PyObject mod(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._mod(ts, arg2);
    }

    @ExposedFunction(names = {"mul", "repeat"})
    public static PyObject mul(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._mul(ts, arg2);
    }

    @ExposedFunction
    public static PyObject or(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._or(ts, arg2);
    }

    @ExposedFunction
    public static PyObject rshift(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._rshift(ts, arg2);
    }

    @ExposedFunction
    public static PyObject sub(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._sub(ts, arg2);
    }

    @ExposedFunction
    public static PyObject xor(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._xor(ts, arg2);
    }

    @ExposedFunction
    public static PyObject abs(ThreadState ts, PyObject arg) {
        return BuiltinModule.abs(arg);
    }

    @ExposedFunction(names = {"inv", "invert"})
    public static PyObject inv(ThreadState ts, PyObject arg) {
        return Abstract.PyNumber_Invert(ts, arg);
    }

    @ExposedFunction
    public static PyObject neg(ThreadState ts, PyObject arg) {
        return Abstract.PyNumber_Negative(ts, arg);
    }

    @ExposedFunction
    public static PyObject not_(ThreadState ts, PyObject arg) {
        return Abstract.PyObject_Not(ts, arg);
    }

    @ExposedFunction
    public static PyObject pos(ThreadState ts, PyObject arg) {
        return Abstract.PyNumber_Positive(ts, arg);
    }

    @ExposedFunction
    public static boolean truth(ThreadState ts, PyObject arg) {
        return Abstract.PyObject_IsTrue(ts, arg);
    }

    @ExposedFunction
    public static boolean contains(ThreadState ts, PyObject arg1, PyObject arg2) {
        return Abstract.PySequence_Contains(arg2, arg1, ts);
    }

    @ExposedFunction
    public static void delitem(ThreadState ts, PyObject arg1, PyObject arg2) {
        arg1.__delitem__(arg2);
    }

    @ExposedFunction
    public static PyObject getitem(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1.__getitem__(arg2);
    }

    @ExposedFunction
    public static void setitem(ThreadState ts, PyObject arg1, PyObject arg2, PyObject arg3) {
        arg1.__setitem__(arg2, arg3);
    }

    @ExposedFunction
    public static PyObject ge(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1.richCompare(arg2, CompareOp.GE);
    }

    @ExposedFunction
    public static PyObject gt(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1.richCompare(arg2, CompareOp.GT);
    }

    @ExposedFunction
    public static PyObject le(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1.richCompare(arg2, CompareOp.LE);
    }

    @ExposedFunction
    public static PyObject lt(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1.richCompare(arg2, CompareOp.LT);
    }

    @ExposedFunction
    public static PyObject eq(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1.richCompare(arg2, CompareOp.EQ);
    }

    @ExposedFunction
    public static PyObject ne(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1.richCompare(arg2, CompareOp.NE);
    }

    @ExposedFunction
    public static PyObject floordiv(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._floordiv(ts, arg2);
    }

    @ExposedFunction
    public static PyObject truediv(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._truediv(ts, arg2);
    }

    @ExposedFunction
    public static PyObject pow(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._pow(ts, arg2);
    }

    @ExposedFunction
    public static PyObject is_(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._is(arg2);
    }

    @ExposedFunction
    public static PyObject is_not(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._isnot(arg2);
    }

    @ExposedFunction(names = {"iadd", "iconcat"})
    public static PyObject iadd(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._iadd(ts, arg2);
    }

    @ExposedFunction
    public static PyObject iand(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._iand(ts, arg2);
    }

    @ExposedFunction
    public static PyObject ifloordiv(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._ifloordiv(ts, arg2);
    }

    @ExposedFunction
    public static PyObject ilshift(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._ilshift(ts, arg2);
    }

    @ExposedFunction
    public static PyObject rlshift(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._ilshift(ts, arg2);
    }

    @ExposedFunction
    public static PyObject imod(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._imod(ts, arg2);
    }

    @ExposedFunction
    public static PyObject imul(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._imul(ts, arg2);
    }

    @ExposedFunction
    public static PyObject ior(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._ior(ts, arg2);
    }

    @ExposedFunction
    public static PyObject ipow(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._ipow(ts, arg2);
    }

    @ExposedFunction
    public static PyObject isub(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._isub(ts, arg2);
    }

    @ExposedFunction
    public static PyObject itruediv(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._itruediv(ts, arg2);
    }

    @ExposedFunction
    public static PyObject ixor(ThreadState ts, PyObject arg1, PyObject arg2) {
        return arg1._ixor(ts, arg2);
    }

    @ExposedFunction
    public static PyObject index(ThreadState ts, PyObject arg1) {
        return Abstract.PyNumber_Index(ts, arg1);
    }

    @ModuleInit
    public static void init(PyObject dict) {
        dict.__setitem__("attrgetter", PyAttrGetter.TYPE);
        dict.__setitem__("itemgetter", PyItemGetter.TYPE);
        dict.__setitem__("methodcaller", PyMethodCaller.TYPE);
    }

    @ExposedFunction(defaults = {"0"})
    public static PyObject length_hint(PyObject o, int defaultVal) {
        PyObject res;
        try {
            return new PyLong(o.__len__());
        } catch (PyException e) {
            if (!e.match(Py.TypeError)) {
                throw e;
            }
        }
        PyObject hint = o.__findattr__("__length_hint__");
        if (hint == null) {
            return new PyLong(defaultVal);
        }
        try {
            res = hint.__call__();
        } catch (PyException e) {
            if (e.match(Py.TypeError)) {
                return new PyLong(defaultVal);
            }
            throw e;
        }
        if (res == Py.NotImplemented) {
            return new PyLong(defaultVal);
        }
        if (!(res instanceof PyLong)) {
            throw Py.TypeError(String.format("__length_hint__ must be an integer, not %s", res.getType().fastGetName()));
        }
        return res;

    }

    @ExposedFunction
    public static int countOf(PyObject seq, PyObject item) {
        int count = 0;

        for (PyObject tmp : seq.asIterable()) {
            if (item.richCompare(tmp, CompareOp.EQ).isTrue()) {
                count++;
            }
        }
        return count;
    }

    @ExposedFunction
    public static int indexOf(PyObject seq, PyObject item) {
        PyObject iter = PyObject.getIter(seq);
        int i = 0;
        for (;;) {
            try {
                if (item.richCompare(PyObject.iterNext(iter), CompareOp.EQ).isTrue()) {
                    return i;
                }
            } catch (PyException e) {
                if (e.match(Py.StopIteration)) {
                    throw Py.ValueError("sequence.index(x): x not in list");
                }
                throw e;
            }
            i++;
        }
    }

    private static String ensureStringAttribute(PyObject name) {
        String nameStr;
        if (name instanceof PyUnicode) {
            nameStr = ((PyUnicode) name).encode();
        } else if (name instanceof PyBytes) {
            nameStr = name.asString();
        } else {
            throw Py.TypeError(String.format("attribute name must be string, not '%.200s'",
                    name.getType().fastGetName()));
        }
        return nameStr;
    }

    /**
     * The attrgetter type.
     */
    @ExposedType(name = "operator.attrgetter", isBaseType = false)
    public static class PyAttrGetter extends PyObject implements Traverseproc {

        public static final PyType TYPE = PyType.fromClass(PyAttrGetter.class);

        public PyObject[] attrs;

        public PyAttrGetter(PyObject[] attrs) {
            this.attrs = attrs;
        }

        @ExposedNew
        final static PyObject attrgetter___new__(PyNewWrapper new_, boolean init, PyType subtype,
                                                 PyObject[] args, String[] keywords) {
            ArgParser ap = new ArgParser("attrgetter", args, keywords, "attr");
            ap.noKeywords();
            ap.getPyObject(0);
            return new PyAttrGetter(args);
        }

        @Override
        public PyObject __call__(PyObject[] args, String[] keywords) {
            return attrgetter___call__(args, keywords);
        }

        @ExposedMethod
        final PyObject attrgetter___call__(PyObject[] args, String[] keywords) {
            ArgParser ap = new ArgParser("attrgetter", args, Py.NoKeywords, "obj");
            PyObject obj = ap.getPyObject(0);

            if (attrs.length == 1) {
                return getattr(obj, attrs[0]);
            }

            PyObject[] result = new PyObject[attrs.length];
            int i = 0;
            for (PyObject attr : attrs) {
                result[i++] = getattr(obj, attr);
            }
            return new PyTuple(result);
        }

        private PyObject getattr(PyObject obj, PyObject name) {
            // XXX: We should probably have a PyObject.__getattr__(PyObject) that does
            // this. This is different than __builtin__.getattr (in how it handles
            // Exceptions)
            String nameStr = ensureStringAttribute(name);
            String[] components = nameStr.split("\\.");
            for (String component : components) {
                obj = obj.__getattr__(component.intern());
            }
            return obj;
        }


        /* Traverseproc implementation */
        @Override
        public int traverse(Visitproc visit, Object arg) {
            if (attrs != null) {
                int retVal;
                for (PyObject ob : attrs) {
                    if (ob != null) {
                        retVal = visit.visit(ob, arg);
                        if (retVal != 0) {
                            return retVal;
                        }
                    }
                }
            }
            return 0;
        }

        @Override
        public boolean refersDirectlyTo(PyObject ob) {
            if (ob == null || attrs == null) {
                return false;
            }
            for (PyObject obj : attrs) {
                if (obj == ob) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * The itemgetter type.
     */
    @ExposedType(name = "operator.itemgetter", isBaseType = false)
    public static class PyItemGetter extends PyObject implements Traverseproc {

        public static final PyType TYPE = PyType.fromClass(PyItemGetter.class);

        public PyObject[] items;

        public PyItemGetter(PyObject[] items) {
            this.items = items;
        }

        @ExposedNew
        final static PyObject itemgetter___new__(PyNewWrapper new_, boolean init, PyType subtype,
                                                 PyObject[] args, String[] keywords) {
            ArgParser ap = new ArgParser("itemgetter", args, keywords, "attr");
            ap.noKeywords();
            ap.getPyObject(0);
            return new PyItemGetter(args);
        }

        @ExposedMethod
        final PyObject itemgetter___call__(PyObject[] args, String[] keywords) {
            ArgParser ap = new ArgParser("itemgetter", args, Py.NoKeywords, "obj");
            PyObject obj = ap.getPyObject(0);

            if (items.length == 1) {
                return obj.__getitem__(items[0]);
            }

            PyObject[] result = new PyObject[items.length];
            int i = 0;
            for (PyObject item : items) {
                result[i++] = obj.__getitem__(item);
            }
            return new PyTuple(result);
        }        @Override
        public PyObject __call__(PyObject[] args, String[] keywords) {
            return itemgetter___call__(args, keywords);
        }




        /* Traverseproc implementation */
        @Override
        public int traverse(Visitproc visit, Object arg) {
            if (items != null) {
                int retVal;
                for (PyObject ob : items) {
                    if (ob != null) {
                        retVal = visit.visit(ob, arg);
                        if (retVal != 0) {
                            return retVal;
                        }
                    }
                }
            }
            return 0;
        }

        @Override
        public boolean refersDirectlyTo(PyObject ob) {
            if (ob == null || items == null) {
                return false;
            }
            for (PyObject obj : items) {
                if (obj == ob) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * The methodcaller type.
     */
    @ExposedType(name = "operator.methodcaller", isBaseType = false)
    public static class PyMethodCaller extends PyObject implements Traverseproc {

        public static final PyType TYPE = PyType.fromClass(PyMethodCaller.class);

        public String name;
        public PyObject[] args;
        public String[] keywords;

        public PyMethodCaller(String name, PyObject[] args, String[] keywords) {
            this.name = name;
            this.args = args;
            this.keywords = keywords;
        }

        @ExposedNew
        final static PyObject methodcaller___new__(PyNewWrapper new_, boolean init,
                                                   PyType subtype, PyObject[] args,
                                                   String[] keywords) {

            if (args.length == 0) {
                throw Py.TypeError("methodcaller needs at least one argument, the method name");
            }
            String nameStr = ensureStringAttribute(args[0]);
            PyObject[] newArgs = new PyObject[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, args.length - 1);
            return new PyMethodCaller(nameStr, newArgs, keywords);
        }

        @ExposedMethod
        final PyObject methodcaller___call__(PyObject[] args, String[] keywords) {
            if (args.length > 1) {
                throw Py.TypeError("methodcaller expected 1 arguments, got " + args.length);
            }
            ArgParser ap = new ArgParser("methodcaller", args, Py.NoKeywords, "obj");
            PyObject obj = ap.getPyObject(0);
            return obj.invoke(name, this.args, this.keywords);
        }        @Override
        public PyObject __call__(PyObject[] args, String[] keywords) {
            return methodcaller___call__(args, keywords);
        }




        /* Traverseproc implementation */
        @Override
        public int traverse(Visitproc visit, Object arg) {
            if (args != null) {
                int retVal;
                for (PyObject ob : args) {
                    if (ob != null) {
                        retVal = visit.visit(ob, arg);
                        if (retVal != 0) {
                            return retVal;
                        }
                    }
                }
            }
            return 0;
        }

        @Override
        public boolean refersDirectlyTo(PyObject ob) {
            if (ob == null || args == null) {
                return false;
            }
            for (PyObject obj : args) {
                if (obj == ob) {
                    return true;
                }
            }
            return false;
        }
    }
}
