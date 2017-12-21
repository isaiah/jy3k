// Copyright (c) Corporation for National Research Initiatives
package org.python.modules;

import org.python.core.ArgParser;
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
import org.python.core.Traverseproc;
import org.python.core.Untraversable;
import org.python.core.Visitproc;
import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedModule;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.annotations.ModuleInit;

@Untraversable
@ExposedModule(name = "_operator")
public class operator
{
    @ExposedFunction(names = {"add", "concat"})
    public static PyObject add(PyObject arg1, PyObject arg2) {
        return arg1._add(arg2);
    }

    @ExposedFunction
    public static PyObject and_(PyObject arg1, PyObject arg2) {
        return arg1._and(arg2);
    }

    @ExposedFunction
    public static PyObject div(PyObject arg1, PyObject arg2) {
        return arg1._div(arg2);
    }

    @ExposedFunction
    public static PyObject lshift(PyObject arg1, PyObject arg2) {
        return arg1._lshift(arg2);
    }

    @ExposedFunction
    public static PyObject mod(PyObject arg1, PyObject arg2) {
        return arg1._mod(arg2);
    }

    @ExposedFunction(names = {"mul", "repeat"})
    public static PyObject mul(PyObject arg1, PyObject arg2) {
        return arg1._mul(arg2);
    }

    @ExposedFunction
    public static PyObject or(PyObject arg1, PyObject arg2) {
        return arg1._or(arg2);
    }

    @ExposedFunction
    public static PyObject rshift(PyObject arg1, PyObject arg2) {
        return arg1._rshift(arg2);
    }

    @ExposedFunction
    public static PyObject sub(PyObject arg1, PyObject arg2) {
        return arg1._sub(arg2);
    }

    @ExposedFunction
    public static PyObject xor(PyObject arg1, PyObject arg2) {
        return arg1._xor(arg2);
    }

    @ExposedFunction
    public static PyObject abs(PyObject arg) {
        return arg.__abs__();
    }

    @ExposedFunction(names = {"inv", "invert"})
    public static PyObject inv(PyObject arg) {
        return arg.__invert__();
    }

    @ExposedFunction
    public static PyObject neg(PyObject arg) {
        return arg.__neg__();
    }

    @ExposedFunction
    public static PyObject not_(PyObject arg) {
        return arg.__not__();
    }

    @ExposedFunction
    public static PyObject pos(PyObject arg) {
        return arg.__pos__();
    }

    @ExposedFunction
    public static boolean truth(PyObject arg) {
        return arg.__bool__();
    }

    @ExposedFunction
    public static boolean contains(PyObject arg1, PyObject arg2) {
        return arg1.__contains__(arg2);
    }

    @ExposedFunction
    public static void delitem(PyObject arg1, PyObject arg2) {
        arg1.__delitem__(arg2);
    }

    @ExposedFunction
    public static PyObject getitem(PyObject arg1, PyObject arg2) {
        return arg1.__getitem__(arg2);
    }

    @ExposedFunction
    public static void setitem(PyObject arg1, PyObject arg2, PyObject arg3) {
        arg1.__setitem__(arg2, arg3);
    }

    @ExposedFunction
    public static PyObject ge(PyObject arg1, PyObject arg2) {
        return arg1.richCompare(arg2, CompareOp.GE);
    }

    @ExposedFunction
    public static PyObject gt(PyObject arg1, PyObject arg2) {
        return arg1.richCompare(arg2, CompareOp.GT);
    }

    @ExposedFunction
    public static PyObject le(PyObject arg1, PyObject arg2) {
        return arg1.richCompare(arg2, CompareOp.LE);
    }

    @ExposedFunction
    public static PyObject lt(PyObject arg1, PyObject arg2) {
        return arg1.richCompare(arg2, CompareOp.LT);
    }

    @ExposedFunction
    public static PyObject eq(PyObject arg1, PyObject arg2) {
        return arg1.richCompare(arg2, CompareOp.EQ);
    }

    @ExposedFunction
    public static PyObject ne(PyObject arg1, PyObject arg2) {
        return arg1.richCompare(arg2, CompareOp.NE);
    }

    @ExposedFunction
    public static PyObject floordiv(PyObject arg1, PyObject arg2) {
        return arg1._floordiv(arg2);
    }

    @ExposedFunction
    public static PyObject truediv(PyObject arg1, PyObject arg2) {
        return arg1._truediv(arg2);
    }

    @ExposedFunction
    public static PyObject pow(PyObject arg1, PyObject arg2) {
        return arg1._pow(arg2);
    }

    @ExposedFunction
    public static PyObject is_(PyObject arg1, PyObject arg2) {
        return arg1._is(arg2);
    }

    @ExposedFunction
    public static PyObject is_not(PyObject arg1, PyObject arg2) {
        return arg1._isnot(arg2);
    }

    @ExposedFunction(names = {"iadd", "iconcat"})
    public static PyObject iadd(PyObject arg1, PyObject arg2) {
        return arg1._iadd(arg2);
    }

    @ExposedFunction
    public static PyObject iand(PyObject arg1, PyObject arg2) {
        return arg1._iand(arg2);
    }

    @ExposedFunction
    public static PyObject idiv(PyObject arg1, PyObject arg2) {
        return arg1._idiv(arg2);
    }

    @ExposedFunction
    public static PyObject ifloordiv(PyObject arg1, PyObject arg2) {
        return arg1._ifloordiv(arg2);
    }

    @ExposedFunction
    public static PyObject ilshift(PyObject arg1, PyObject arg2) {
        return arg1._ilshift(arg2);
    }

    @ExposedFunction
    public static PyObject rlshift(PyObject arg1, PyObject arg2) {
        return arg1._ilshift(arg2);
    }

    @ExposedFunction
    public static PyObject imod(PyObject arg1, PyObject arg2) {
        return arg1._imod(arg2);
    }

    @ExposedFunction
    public static PyObject imul(PyObject arg1, PyObject arg2) {
        return arg1._imul(arg2);
    }

    @ExposedFunction
    public static PyObject ior(PyObject arg1, PyObject arg2) {
        return arg1._ior(arg2);
    }

    @ExposedFunction
    public static PyObject ipow(PyObject arg1, PyObject arg2) {
        return arg1._ipow(arg2);
    }

    @ExposedFunction
    public static PyObject isub(PyObject arg1, PyObject arg2) {
        return arg1._isub(arg2);
    }

    @ExposedFunction
    public static PyObject itruediv(PyObject arg1, PyObject arg2) {
        return arg1._itruediv(arg2);
    }

    @ExposedFunction
    public static PyObject ixor(PyObject arg1, PyObject arg2) {
        return arg1._ixor(arg2);
    }

    @ExposedFunction
    public static PyObject index(PyObject arg1) {
        return arg1.__index__();
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
            if (item.richCompare(tmp, CompareOp.EQ).__bool__()) {
                count++;
            }
        }
        return count;
    }

    @ExposedFunction
    public static int indexOf(PyObject seq, PyObject item) {
        int i = 0;
        PyObject iter = seq.__iter__();
        for (PyObject tmp = null; (tmp = iter.__next__()) != null; i++) {
            if (item.richCompare(tmp, CompareOp.EQ).__bool__()) {
                return i;
            }
        }
        throw Py.ValueError("sequence.index(x): x not in list");
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
                for (PyObject ob: attrs) {
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
            for (PyObject obj: attrs) {
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

        @Override
        public PyObject __call__(PyObject[] args, String[] keywords) {
            return itemgetter___call__(args, keywords);
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
        }


        /* Traverseproc implementation */
        @Override
        public int traverse(Visitproc visit, Object arg) {
            if (items != null) {
                int retVal;
                for (PyObject ob: items) {
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
            for (PyObject obj: items) {
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
            PyObject[] newArgs = new PyObject[args.length-1];
            System.arraycopy(args, 1, newArgs, 0, args.length-1);
            return new PyMethodCaller(nameStr, newArgs, keywords);
        }

        @Override
        public PyObject __call__(PyObject[] args, String[] keywords) {
            return methodcaller___call__(args, keywords);
        }

        @ExposedMethod
        final PyObject methodcaller___call__(PyObject[] args, String[] keywords) {
            if (args.length > 1) {
                throw Py.TypeError("methodcaller expected 1 arguments, got " + args.length);
            }
            ArgParser ap = new ArgParser("methodcaller", args, Py.NoKeywords, "obj");
            PyObject obj = ap.getPyObject(0);
            return obj.invoke(name, this.args, this.keywords);
        }


        /* Traverseproc implementation */
        @Override
        public int traverse(Visitproc visit, Object arg) {
            if (args != null) {
                int retVal;
                for (PyObject ob: args) {
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
            for (PyObject obj: args) {
                if (obj == ob) {
                    return true;
                }
            }
            return false;
        }
    }

    private static String ensureStringAttribute(PyObject name) {
        String nameStr;
        if (name instanceof PyUnicode) {
            nameStr = ((PyUnicode)name).encode();
        } else if (name instanceof PyBytes) {
            nameStr = name.asString();
        } else {
            throw Py.TypeError(String.format("attribute name must be string, not '%.200s'",
                    name.getType().fastGetName()));
        }
        return nameStr;
    }
}
