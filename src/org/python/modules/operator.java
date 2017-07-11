// Copyright (c) Corporation for National Research Initiatives
package org.python.modules;

import org.python.core.ArgParser;
import org.python.core.CompareOp;
import org.python.core.Py;
import org.python.core.PyBuiltinFunctionSet;
import org.python.core.PyBytes;
import org.python.core.PyException;
import org.python.core.PyLong;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PySlice;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.core.Traverseproc;
import org.python.core.Untraversable;
import org.python.core.Visitproc;
import org.python.expose.ExposedFunction;
import org.python.expose.ExposedGet;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedModule;
import org.python.expose.ExposedNew;
import org.python.expose.ExposedType;
import org.python.expose.ModuleInit;

@Untraversable
@ExposedModule(name = "_operator")
public class operator
{
    @ModuleInit
    public static void init(PyObject dict) {
        dict.__setitem__("add", new OperatorFunctions("add", 0, 2));
        dict.__setitem__("concat", new OperatorFunctions("concat", 0, 2));
        dict.__setitem__("and_", new OperatorFunctions("and_", 1, 2));
        dict.__setitem__("div", new OperatorFunctions("div", 2, 2));
        dict.__setitem__("lshift", new OperatorFunctions("lshift", 3, 2));
        dict.__setitem__("mod", new OperatorFunctions("mod", 4, 2));
        dict.__setitem__("mul", new OperatorFunctions("mul", 5, 2));
        dict.__setitem__("repeat", new OperatorFunctions("repeat", 5, 2));
        dict.__setitem__("or_", new OperatorFunctions("or_", 6, 2));
        dict.__setitem__("rshift", new OperatorFunctions("rshift", 7, 2));
        dict.__setitem__("sub", new OperatorFunctions("sub", 8, 2));
        dict.__setitem__("xor", new OperatorFunctions("xor", 9, 2));
        dict.__setitem__("abs", new OperatorFunctions("abs", 10, 1));
        dict.__setitem__("inv", new OperatorFunctions("inv", 11, 1));
        dict.__setitem__("neg", new OperatorFunctions("neg", 12, 1));
        dict.__setitem__("not_", new OperatorFunctions("not_", 13, 1));
        dict.__setitem__("pos", new OperatorFunctions("pos", 14, 1));
        dict.__setitem__("truth", new OperatorFunctions("truth", 15, 1));
        dict.__setitem__("isCallable", new OperatorFunctions("isCallable", 16, 1));
        dict.__setitem__("isMappingType", new OperatorFunctions("isMappingType", 17, 1));
        dict.__setitem__("isNumberType", new OperatorFunctions("isNumberType", 18, 1));
        dict.__setitem__("isSequenceType", new OperatorFunctions("isSequenceType", 19, 1));
        dict.__setitem__("contains", new OperatorFunctions("contains", 20, 2));
        dict.__setitem__("sequenceIncludes", new OperatorFunctions("sequenceIncludes", 20, 2));
        dict.__setitem__("delitem", new OperatorFunctions("delitem", 21, 2));
        dict.__setitem__("delslice", new OperatorFunctions("delslice", 22, 3));
        dict.__setitem__("getitem", new OperatorFunctions("getitem", 23, 2));
        dict.__setitem__("getslice", new OperatorFunctions("getslice", 24, 3));
        dict.__setitem__("setitem", new OperatorFunctions("setitem", 25, 3));
        dict.__setitem__("setslice", new OperatorFunctions("setslice", 26, 4));
        dict.__setitem__("ge", new OperatorFunctions("ge", 27, 2));
        dict.__setitem__("le", new OperatorFunctions("le", 28, 2));
        dict.__setitem__("eq", new OperatorFunctions("eq", 29, 2));
        dict.__setitem__("floordiv", new OperatorFunctions("floordiv", 30, 2));
        dict.__setitem__("gt", new OperatorFunctions("gt", 31, 2));
        dict.__setitem__("invert", new OperatorFunctions("invert", 32, 1));
        dict.__setitem__("lt", new OperatorFunctions("lt", 33, 2));
        dict.__setitem__("ne", new OperatorFunctions("ne", 34, 2));
        dict.__setitem__("truediv", new OperatorFunctions("truediv", 35, 2));
        dict.__setitem__("pow", new OperatorFunctions("pow", 36, 2));
        dict.__setitem__("is_", new OperatorFunctions("is_", 37, 2));
        dict.__setitem__("is_not", new OperatorFunctions("is_not", 38, 2));

        dict.__setitem__("iadd", new OperatorFunctions("iadd", 39, 2));
        dict.__setitem__("iconcat", new OperatorFunctions("iconcat", 39, 2));
        dict.__setitem__("iand", new OperatorFunctions("iand", 40, 2));
        dict.__setitem__("idiv", new OperatorFunctions("idiv", 41, 2));
        dict.__setitem__("ifloordiv", new OperatorFunctions("ifloordiv", 42, 2));
        dict.__setitem__("ilshift", new OperatorFunctions("ilshift", 43, 2));
        dict.__setitem__("imod", new OperatorFunctions("imod", 44, 2));
        dict.__setitem__("imul", new OperatorFunctions("imul", 45, 2));
        dict.__setitem__("irepeat", new OperatorFunctions("irepeat", 45, 2));
        dict.__setitem__("ior", new OperatorFunctions("ior", 46, 2));
        dict.__setitem__("ipow", new OperatorFunctions("ipow", 47, 2));
        dict.__setitem__("irshift", new OperatorFunctions("irshift", 48, 2));
        dict.__setitem__("isub", new OperatorFunctions("isub", 49, 2));
        dict.__setitem__("itruediv", new OperatorFunctions("itruediv", 50, 2));
        dict.__setitem__("ixor", new OperatorFunctions("ixor", 51, 2));
        dict.__setitem__("index", new OperatorFunctions("index", 52, 1));

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
    static class PyAttrGetter extends PyObject implements Traverseproc {

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
    static class PyItemGetter extends PyObject implements Traverseproc {

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
    static class PyMethodCaller extends PyObject implements Traverseproc {

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
