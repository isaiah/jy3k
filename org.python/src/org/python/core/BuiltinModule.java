/*
 * Copyright (c) Corporation for National Research Initiatives
 * Copyright (c) Jython Developers
 */
package org.python.core;

import org.python.antlr.base.mod;
import org.python.bootstrap.Import;
import org.python.core.linker.InvokeByName;
import org.python.core.stringlib.Encoding;
import org.python.core.stringlib.IntegerFormatter;
import org.python.modules._io._io;
import org.python.modules.sys.SysModule;

/**
 * The builtin module. All builtin functions are defined here
 */
public class BuiltinModule {
    private static final InvokeByName abs = new InvokeByName("__abs__", PyObject.class, PyObject.class, ThreadState.class);
    private static final InvokeByName len = new InvokeByName("__len__", PyObject.class, PyObject.class, ThreadState.class);
    private static final InvokeByName dir = new InvokeByName("__dir__", PyObject.class, PyObject.class, ThreadState.class);
    private static final InvokeByName get = new InvokeByName("__get__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class, PyObject.class);

    public static void fillWithBuiltins(PyObject dict) {
        /* newstyle */
        dict.__setitem__("object", PyObject.TYPE);
        dict.__setitem__("type", PyType.TYPE);
        dict.__setitem__("bool", PyBoolean.TYPE);
        dict.__setitem__("int", PyLong.TYPE);
        dict.__setitem__("enumerate", PyEnumerate.TYPE);
        dict.__setitem__("float", PyFloat.TYPE);
        dict.__setitem__("complex", PyComplex.TYPE);
        dict.__setitem__("dict", PyDictionary.TYPE);
        dict.__setitem__("list", PyList.TYPE);
        dict.__setitem__("map", PyMap.TYPE);
        dict.__setitem__("filter", PyFilter.TYPE);
        dict.__setitem__("zip", PyZip.TYPE);
        dict.__setitem__("tuple", PyTuple.TYPE);
        dict.__setitem__("set", PySet.TYPE);
        dict.__setitem__("frozenset", PyFrozenSet.TYPE);
        dict.__setitem__("property", PyProperty.TYPE);
        dict.__setitem__("staticmethod", PyStaticMethod.TYPE);
        dict.__setitem__("classmethod", PyClassMethod.TYPE);
        dict.__setitem__("super", PySuper.TYPE);
        dict.__setitem__("str", PyUnicode.TYPE);
        dict.__setitem__("slice", PySlice.TYPE);
        dict.__setitem__("range", PyRange.TYPE);

        dict.__setitem__("None", Py.None);
        dict.__setitem__("NotImplemented", Py.NotImplemented);
        dict.__setitem__("Ellipsis", Py.Ellipsis);
        dict.__setitem__("True", Py.True);
        dict.__setitem__("False", Py.False);
        dict.__setitem__("bytes", PyBytes.TYPE);
        dict.__setitem__("bytearray", PyByteArray.TYPE);
        dict.__setitem__("memoryview", PyMemoryView.TYPE);

        // Work in debug mode by default
        // Hopefully add -O option in the future to change this
        dict.__setitem__("__debug__", Py.One);

        // TODO: redo the builtin function stuff to possibly use enum
        dict.__setitem__("abs", PyBuiltinFunction.named("abs", BuiltinDocs.builtins_abs_doc).with(BuiltinModule::abs));
        dict.__setitem__("ascii", PyBuiltinFunction.named("ascii", BuiltinDocs.builtins_ascii_doc).with(BuiltinModule::ascii));
        dict.__setitem__("callable", PyBuiltinFunction.named("callable", BuiltinDocs.builtins_callable_doc).with(BuiltinModule::callable));
        dict.__setitem__("chr", PyBuiltinFunction.named("chr", BuiltinDocs.builtins_chr_doc).with(BuiltinModule::chr));
        dict.__setitem__("globals", PyBuiltinFunction.named("globals", BuiltinDocs.builtins_globals_doc).with(BuiltinModule::globals));
        dict.__setitem__("hash", PyBuiltinFunction.named("hash", BuiltinDocs.builtins_hash_doc).with(BuiltinModule::hash));
        dict.__setitem__("id", PyBuiltinFunction.named("id", BuiltinDocs.builtins_id_doc).with(BuiltinModule::id));
        dict.__setitem__("isinstance", PyBuiltinFunction.named("isinstance", BuiltinDocs.builtins_isinstance_doc).with(BuiltinModule::isinstance));
        dict.__setitem__("len", PyBuiltinFunction.named("len", BuiltinDocs.builtins_len_doc).with(BuiltinModule::len));
        dict.__setitem__("ord", PyBuiltinFunction.named("ord", BuiltinDocs.builtins_ord_doc).with(BuiltinModule::ord));
        dict.__setitem__("sum", PyBuiltinFunction.named("sum", BuiltinDocs.builtins_sum_doc)
                .with(BuiltinModule::sum1)
                .with(BuiltinModule::sum2));
        dict.__setitem__("delattr", PyBuiltinFunction.named("delattr", BuiltinDocs.builtins_delattr_doc).with(BuiltinModule::delattr));
        dict.__setitem__("dir", PyBuiltinFunction.named("dir", BuiltinDocs.builtins_dir_doc)
                .with(BuiltinModule::dir0)
                .with(BuiltinModule::dir1));
        dict.__setitem__("divmod", PyBuiltinFunction.named("divmod", BuiltinDocs.builtins_divmod_doc).with(BuiltinModule::divmod));
        dict.__setitem__("eval", PyBuiltinFunction.named("eval", BuiltinDocs.builtins_eval_doc)
                .with(BuiltinModule::eval1)
                .with(BuiltinModule::eval2)
                .with(BuiltinModule::eval3));
        dict.__setitem__("getattr", PyBuiltinFunction.named("getattr", BuiltinDocs.builtins_getattr_doc)
                .with(BuiltinModule::getattr2)
                .with(BuiltinModule::getattr3));
        dict.__setitem__("hasattr", PyBuiltinFunction.named("hasattr", BuiltinDocs.builtins_hasattr_doc).with(BuiltinModule::hasattr));
        dict.__setitem__("hex", PyBuiltinFunction.named("hex", BuiltinDocs.builtins_hex_doc).with(BuiltinModule::hex));
        dict.__setitem__("issubclass", PyBuiltinFunction.named(
                "issubclass", BuiltinDocs.builtins_issubclass_doc).with(BuiltinModule::issubclass));
        dict.__setitem__("iter", PyBuiltinFunction.named("iter", BuiltinDocs.builtins_iter_doc)
                .with(BuiltinModule::iter1)
                .with(BuiltinModule::iter2));
        dict.__setitem__("locals", PyBuiltinFunction.named("locals", BuiltinDocs.builtins_locals_doc).with(BuiltinModule::locals));
        dict.__setitem__("max", PyBuiltinFunction.named("max", BuiltinDocs.builtins_max_doc).wide(BuiltinModule::max));
        dict.__setitem__("min", PyBuiltinFunction.named("min", BuiltinDocs.builtins_min_doc).wide(BuiltinModule::min));
        dict.__setitem__("oct", PyBuiltinFunction.named("oct", BuiltinDocs.builtins_oct_doc).with(BuiltinModule::oct));
        dict.__setitem__("pow", PyBuiltinFunction.named("pow", BuiltinDocs.builtins_pow_doc)
                .with(BuiltinModule::pow2)
                .with(BuiltinModule::pow3));
        dict.__setitem__("input", PyBuiltinFunction.named("input", BuiltinDocs.builtins_input_doc)
                .with(BuiltinModule::raw_input0)
                .with(BuiltinModule::raw_input1)
                .with(BuiltinModule::raw_input2));
        dict.__setitem__("round", PyBuiltinFunction.named("round", BuiltinDocs.builtins_round_doc).wide(BuiltinModule::round));
        dict.__setitem__("repr", PyBuiltinFunction.named("repr", BuiltinDocs.builtins_repr_doc).with(BuiltinModule::repr));
        dict.__setitem__("setattr", PyBuiltinFunction.named("setattr", BuiltinDocs.builtins_setattr_doc).with(BuiltinModule::setattr));
        dict.__setitem__("vars", PyBuiltinFunction.named("vars", BuiltinDocs.builtins_vars_doc)
                .with(BuiltinModule::vars0)
                .with(BuiltinModule::vars1));
        dict.__setitem__("compile", PyBuiltinFunction.named("compile", BuiltinDocs.builtins_compile_doc).wide(BuiltinModule::compile));
        dict.__setitem__("open", PyBuiltinFunction.named("open", BuiltinDocs.builtins_open_doc).wide(_io::open));
        dict.__setitem__("reversed", PyBuiltinFunction.named("reversed", BuiltinDocs.builtins_reversed_doc)
                .with(BuiltinModule::reversed));
        dict.__setitem__("exec", PyBuiltinFunction.named("exec", BuiltinDocs.builtins_exec_doc)
                .with(BuiltinModule::exec1)
                .with(BuiltinModule::exec2)
                .with(BuiltinModule::exec3));
        dict.__setitem__("__import__", PyBuiltinFunction.named("__import__", BuiltinDocs.builtins___import___doc).wide(BuiltinModule::import$));
        dict.__setitem__("sorted", PyBuiltinFunction.named("sorted", BuiltinDocs.builtins_sorted_doc).wide(BuiltinModule::sorted));
        dict.__setitem__("all", PyBuiltinFunction.named("all", BuiltinDocs.builtins_all_doc).with(BuiltinModule::all));
        dict.__setitem__("any", PyBuiltinFunction.named("any", BuiltinDocs.builtins_any_doc).with(BuiltinModule::any));
        dict.__setitem__("format", PyBuiltinFunction.named("format", BuiltinDocs.builtins_format_doc)
                .with(BuiltinModule::format1)
                .with(BuiltinModule::format2));
        dict.__setitem__("print", PyBuiltinFunction.named("print", BuiltinDocs.builtins_print_doc).wide(BuiltinModule::print));
        dict.__setitem__("next", PyBuiltinFunction.named("next", BuiltinDocs.builtins_next_doc).wide(BuiltinModule::next));
        dict.__setitem__("bin", PyBuiltinFunction.named("bin", BuiltinDocs.builtins_bin_doc).wide(BuiltinModule::bin));
        dict.__setitem__("__build_class__", PyBuiltinFunction.named("__build_class__", BuiltinDocs.builtins___build_class___doc).wide(BuiltinModule::buildClass));
    }

    public static void fillWithBuiltinExceptions(PyObject dict) {
        Exceptions.init(dict);
    }

    public static PyObject abs(PyObject o) {
        return PyObject.unaryOp(Py.getThreadState(), abs, o, self -> {
            throw Py.TypeError(String.format("bad operand type for abs(): '%s'", self.getType().fastGetName()));
        });
    }

    public static PyObject all(PyObject arg) {
        PyObject iter = PyObject.getIter(arg);
        try {
            for (;;) {
                PyObject item = PyObject.iterNext(iter);
                if (!item.isTrue()) {
                    return Py.False;
                }
            }
        } catch (PyException e) {
            if (!e.match(Py.StopIteration)) {
                throw e;
            }
        }
        return Py.True;
    }

    public static PyObject any(PyObject arg) {
        PyObject iter = PyObject.getIter(arg);
        try {
            for (; ; ) {
                PyObject item = PyObject.iterNext(iter);
                if (item.isTrue()) {
                    return Py.True;
                }
            }
        } catch (PyException e) {
            if (!e.match(Py.StopIteration)) {
                throw e;
            }
        }
        return Py.False;
    }

    public static PyObject ascii(PyObject obj) {
        boolean quoted = obj instanceof PyUnicode || obj instanceof PyBytes;
        return new PyUnicode(Encoding.encode_UnicodeEscapeAsASCII(obj.toString(), quoted));
    }

    public static PyObject bin(PyObject args[], String kwds[]) {
        ArgParser ap = new ArgParser("bin", args, kwds, new String[]{"number"}, 1);
        ap.noKeywords();
        return IntegerFormatter.bin(ap.getPyObject(0));
    }

    public static PyObject callable(PyObject obj) {
        return Py.newBoolean(obj.isCallable());
    }

    public static PyObject chr(PyObject num) {
        long i = num.asLong();
        if (i < 0 || i > SysModule.MAXUNICODE) {
            throw Py.ValueError("chr() arg not in range(0x110000)");
        }
        return new PyUnicode(String.valueOf(Character.toChars((int) i)));
    }

    public static PyObject compile(PyObject args[], String kwds[]) {
        ArgParser ap = new ArgParser("compile", args, kwds,
                new String[]{"source", "filename", "mode", "flags",
                        "dont_inherit", "optimize"},
                3);
        PyObject source = ap.getPyObject(0);
        String filename = ap.getString(1);
        String mode = ap.getString(2);
        int flags = ap.getInt(3, 0);
        boolean dont_inherit = ap.getPyObject(4, Py.False).isTrue();
        int optimize = ap.getInt(5, -1);
        return compile(source, filename, mode, flags, dont_inherit);
    }

    public static PyObject compile(PyObject source, String filename, String mode) {
        return compile(source, filename, mode, 0, false);
    }

    public static PyObject compile(PyObject source, String filename, String mode, int flags,
                                   boolean dont_inherit) {
        CompilerFlags cflags = Py.getCompilerFlags(flags, dont_inherit);
        CompileMode kind = CompileMode.getMode(mode);

        return compile(source, filename, kind, cflags, dont_inherit);
    }

    public static PyObject compile(PyObject source, String filename, CompileMode kind,
                                   CompilerFlags cflags, boolean dont_inherit) {
        cflags = Py.getCompilerFlags(cflags, dont_inherit);

        mod ast = py2node(source);
        if (ast == null) {
            if (!(source instanceof PyBytes) && !(source instanceof PyUnicode)) {
                throw Py.TypeError("expected a readable buffer object");
            }
            cflags.source_is_utf8 = source instanceof PyUnicode;

            String data = source.toString();

            if (data.contains("\0")) {
                throw Py.TypeError("compile() expected string without null bytes");
            }
            if (cflags != null && cflags.dont_imply_dedent) {
                data += "\n";
            } else {
                data += "\n\n";
            }
            ast = ParserFacade.parse(data, kind, filename, cflags);
        }

        if (cflags.only_ast) {
            return ast;
        } else {
            return Py.compile_flags(ast, filename, kind, cflags);
        }
    }

    /**
     * @returns mod if obj is a wrapper around an AST mod else returns
     * null
     * <p>
     * XXX: Reaches into implementation details -- needs to be reviewed if our
     * java integration changes.
     */
    private static mod py2node(PyObject obj) {
        Object node = obj.__tojava__(mod.class);
        if (node == Py.NoConversion) {
            return null;
        }
        return (mod) node;
    }

    public static PyObject delattr(PyObject obj, PyObject name) {
        obj.__delattr__(asName(name, "delattr"));
        return Py.None;
    }

    public static PyObject dir1(PyObject o) {
        PyObject func = PyType.lookupSpecial(o, "__dir__");
        PyObject ret = func.__call__();
        if (!Py.isInstance(ret, PyList.TYPE)) {
            ret = new PyList(ret);
        }
        ((PyList) ret).sort();
        return ret;
    }

    public static PyObject dir0() {
        PyObject l = locals();
        PyList ret;
        PyObject retObj = l.invoke("keys");
        if (retObj instanceof PyList) {
            ret = (PyList) retObj;
        } else {
            ret = new PyList(retObj);
        }
        ret.sort();
        return ret;
    }

    public static PyObject divmod(PyObject x, PyObject y) {
        return x._divmod(Py.getThreadState(), y);
    }

    private static boolean isMappingType(PyObject o) {
        return o == null || o == Py.None || o.isMappingType();
    }

    private static void verify_mappings(PyObject globals, PyObject locals) {
        if (!isMappingType(globals)) {
            throw Py.TypeError("globals must be a mapping");
        }
        if (!isMappingType(locals)) {
            throw Py.TypeError("locals must be a mapping");
        }
    }

    public static PyObject exec3(PyObject o, PyObject globals, PyObject locals) {
        if (o instanceof PyTableCode) {
            Py.runCode((PyTableCode) o, globals, locals);
        } else {
            Py.exec(o, globals, locals);
        }
        return Py.None;
    }

    public static PyObject exec1(PyObject o) {
        return exec3(o, null, null);
    }

    public static PyObject exec2(PyObject o, PyObject globals) {
        return exec3(o, globals, null);
    }

    public static PyObject eval3(PyObject o, PyObject globals, PyObject locals) {
        verify_mappings(globals, locals);
        PyCode code;
        if (o instanceof PyCode) {
            code = (PyCode) o;
        } else {
            if (o instanceof PyUnicode) {
                code = (PyCode) compile(o, "<string>", "eval");
            } else {
                throw Py.TypeError("eval: argument 1 must be string or code object");
            }
        }
        return Py.runCode(code, globals, locals);
    }

    public static PyObject eval2(PyObject o, PyObject globals) {
        return eval3(o, globals, globals);
    }

    public static PyObject eval1(PyObject o) {
        if (o instanceof PyTableCode && ((PyTableCode) o).hasFreevars()) {
            throw Py.TypeError("code object passed to eval() may not contain free variables");
        }
        return eval3(o, null, null);
    }

    public static PyObject filter(PyObject func, PyObject seq) {
        if (seq instanceof PyBytes) {
            return filterBaseString(func, (PyBytes) seq,
                    seq instanceof PyUnicode ? PyUnicode.TYPE : PyBytes.TYPE);
        }
        if (seq instanceof PyTuple) {
            return filterTuple(func, (PyTuple) seq);
        }

        PyList list = new PyList();
        for (PyObject item : seq.asIterable()) {
            if (func == PyBoolean.TYPE || func == Py.None) {
                if (!item.isTrue()) {
                    continue;
                }
            } else if (!func.__call__(item).isTrue()) {
                continue;
            }
            list.append(item);
        }
        return list;
    }

    public static PyObject filterBaseString(PyObject func, PyBytes seq, PyType stringType) {
        if (func == Py.None && seq.getType() == stringType) {
            // If it's a real string we can return the original, as no character is ever
            // false and __getitem__ does return this character. If it's a subclass we
            // must go through the __getitem__ loop
            return seq;
        }

        StringBuilder builder = new StringBuilder();
        for (PyObject item : seq.asIterable()) {
            if (func == Py.None) {
                if (!item.isTrue()) {
                    continue;
                }
            } else if (!func.__call__(item).isTrue()) {
                continue;
            }
            if (!Py.isInstance(item, stringType)) {
                String name = stringType.fastGetName();
                throw Py.TypeError(String.format("can't filter %s to %s: __getitem__ returned "
                        + "different type", name, name));
            }
            builder.append(item.toString());
        }

        String result = builder.toString();
        return stringType == PyBytes.TYPE ? new PyBytes(result) : new PyUnicode(result);
    }

    public static PyObject filterTuple(PyObject func, PyTuple seq) {
        int len = seq.size();
        if (len == 0) {
            if (seq.getType() != PyTuple.TYPE) {
                return Py.EmptyTuple;
            }
            return seq;
        }

        PyList list = new PyList();
        PyObject item;
        for (int i = 0; i < len; i++) {
            item = seq.__finditem__(i);
            if (func == Py.None) {
                if (!item.isTrue()) {
                    continue;
                }
            } else if (!func.__call__(item).isTrue()) {
                continue;
            }
            list.append(item);
        }
        return PyTuple.fromIterable(list);
    }

    public static PyObject format1(PyObject arg1) {
        return format2(arg1, Py.EmptyUnicode);
    }

    public static PyObject format2(PyObject value, PyObject formatSpec) {
        return Abstract.PyObject_Format(Py.getThreadState(), value, formatSpec);
    }

    public static PyObject getattr2(PyObject obj, PyObject name) {
        return getattr3(obj, name, null);
    }

    public static PyObject getattr3(PyObject obj, PyObject nameObj, PyObject def) {
        String name = asName(nameObj, "getattr");
        PyObject result = null;
        PyException attributeError = null;

        try {
            result = obj.__findattr_ex__(name);
        } catch (PyException pye) {
            if (!pye.match(Py.AttributeError)) {
                throw pye;
            }
            attributeError = pye;
        }
        if (result != null) {
            return result;
        }
        if (def != null) {
            return def;
        }

        if (attributeError == null) {
            // throws AttributeError
            obj.noAttributeError(name);
        }
        throw attributeError;
    }

    public static PyObject globals() {
        return Py.getFrame().f_globals;
    }

    public static PyObject hasattr(PyObject obj, PyObject nameObj) {
        String name = asName(nameObj, "hasattr");
        try {
            return Py.newBoolean(obj.__findattr_ex__(name) != null);
        } catch (PyException pye) {
            if (pye.match(Py.AttributeError)) {
                return Py.False;
            }
            throw pye;
        }
    }

    public static PyLong hash(PyObject o) {
        return o.__hash__();
    }

    public static PyUnicode hex(PyObject o) {
        return IntegerFormatter.hex(o);
    }

    public static PyUnicode oct(PyObject o) {
        return IntegerFormatter.oct(o);
    }

    public static PyObject id(PyObject o) {
        return new PyLong(Py.id(o));
    }

    public static PyObject import$(PyObject args[], String keywords[]) {
        ArgParser ap = new ArgParser("__import__", args, keywords,
                new String[]{"name", "globals", "locals", "fromlist",
                        "level"},
                1);
        PyObject name = ap.getPyObject(0);
        PyObject globals = ap.getPyObject(1, null);
        PyObject fromlist = ap.getPyObject(3, Py.EmptyTuple);
        int level = ap.getInt(4, Import.DEFAULT_LEVEL);
        return Import.importModuleLevelObject(name, globals, fromlist, level);
    }

    // xxx find where used, modify with more appropriate if necessary
    public static PyObject isinstance(PyObject obj, PyObject cls) {
        return Py.newBoolean(Py.isInstance(obj, cls));
    }

    // xxx find where used, modify with more appropriate if necessary
    public static PyObject issubclass(PyObject derived, PyObject cls) {
        return Py.newBoolean(Py.isSubClass(derived, cls));
    }

    public static PyObject iter1(PyObject obj) {
        return PyObject.getIter(obj);
    }

    public static PyObject iter2(PyObject callable, PyObject sentinel) {
        return new PyCallIter(callable, sentinel);
    }

    public static PyObject len(PyObject obj) {
        return new PyLong(obj.__len__());
    }

    public static PyObject locals() {
        return Py.getFrame().getLocals();
    }

    public static PyObject max(PyObject args[], String kwds[]) {
        ArgParser ap = new ArgParser("max", args, kwds, 1, "iterable", "*", "default", "key");
        int argslen = args.length - kwds.length;
        PyObject key = ap.getPyObject("key", null);
        PyObject defaultValue = ap.getPyObject("default", null);

        if (argslen == 0) {
            throw Py.TypeError("max() expected at least 1 argument, got 0");
        } else if (argslen > 1) {
            if (defaultValue != null) {
                throw Py.TypeError("Cannot specify a default for max() with multiple positional arguments");
            }
            return max_min(new PyTuple(PyTuple.TYPE, args, argslen), key, defaultValue, CompareOp.GT);
        } else {
            return max_min(args[0], key, defaultValue, CompareOp.GT);
        }
    }

    public static PyObject min(PyObject args[], String kwds[]) {
        ArgParser ap = new ArgParser("min", args, kwds, 1, "iterable", "*", "default", "key");
        int argslen = args.length - kwds.length;
        PyObject key = ap.getPyObject("key", null);
        PyObject defaultValue = ap.getPyObject("default", null);

        if (argslen == 0) {
            throw Py.TypeError("min() expected at least 1 argument, got 0");
        } else if (argslen > 1) {
            if (defaultValue != null) {
                throw Py.TypeError("Cannot specify a default for min() with multiple positional arguments");
            }
            return max_min(new PyTuple(PyTuple.TYPE, args, argslen), key, defaultValue, CompareOp.LT);
        } else {
            return max_min(args[0], key, defaultValue, CompareOp.LT);
        }
    }

    public static PyObject next(PyObject args[], String kwds[]) {
        ArgParser ap = new ArgParser("next", args, kwds, new String[]{"iterator", "default"}, 1);
        ap.noKeywords();
        PyObject it = ap.getPyObject(0);
        PyObject def = ap.getPyObject(1, null);

        try {
            return PyObject.iterNext(it);
        } catch (PyException e) {
            if (e.match(Py.StopIteration) && def != null) {
                return def;
            }
            throw e;
        }
    }

    /**
     * Built-in Python function ord() applicable to the string-like types <code>str</code>,
     * <code>bytearray</code>, <code>unicode</code>.
     *
     * @param c string-like object of length 1
     * @return ordinal value of character or byte value in
     * @throws PyException (TypeError) if not a string-like type
     */
    public static final PyObject ord(PyObject c) throws PyException {
        final int length;

        if (c instanceof PyUnicode) {
            String cu = ((PyUnicode) c).getString();
            length = cu.codePointCount(0, cu.length());
            if (length == 1) {
                return new PyLong(cu.codePointAt(0));
            }

        } else if (c instanceof PyBytes) {
            String cs = ((PyBytes) c).getString();
            length = cs.length();
            if (length == 1) {
                return new PyLong(cs.charAt(0));
            }

        } else if (c instanceof BaseBytes) {
            BaseBytes cb = (BaseBytes) c;
            length = cb.__len__();
            if (length == 1) {
                return new PyLong(cb.intAt(0));
            }

        } else {
            // Not any of the acceptable types
            throw Py.TypeError("ord() expected string of length 1, but "
                    + c.getType().fastGetName() + " found");
        }
        /*
         * It was a qualifying string-like object, but if we didn't return or throw by now, the
         * problem was the length.
         */
        throw Py.TypeError("ord() expected a character, but string of length " + length + " found");
    }

    public static PyObject pow2(PyObject x, PyObject y) {
        return x._pow(Py.getThreadState(), y);
    }

    private static boolean coerce(PyObject[] objs) {
        PyObject x = objs[0];
        PyObject y = objs[1];
        PyObject[] result;
        result = x._coerce(y);
        if (result != null) {
            objs[0] = result[0];
            objs[1] = result[1];
            return true;
        }
        result = y._coerce(x);
        if (result != null) {
            objs[0] = result[1];
            objs[1] = result[0];
            return true;
        }
        return false;
    }

    public static PyObject pow3(PyObject x, PyObject y, PyObject z) {
        if (z == Py.None) {
            return pow2(x, y);
        }

        PyObject[] tmp = new PyObject[2];
        tmp[0] = x;
        tmp[1] = y;
        if (coerce(tmp)) {
            x = tmp[0];
            y = tmp[1];
            tmp[1] = z;
            if (coerce(tmp)) {
                x = tmp[0];
                z = tmp[1];
                tmp[0] = y;
                if (coerce(tmp)) {
                    z = tmp[1];
                    y = tmp[0];
                }
            }
        } else {
            tmp[1] = z;
            if (coerce(tmp)) {
                x = tmp[0];
                z = tmp[1];
                tmp[0] = y;
                if (coerce(tmp)) {
                    y = tmp[0];
                    z = tmp[1];
                    tmp[1] = x;
                    if (coerce(tmp)) {
                        x = tmp[1];
                        y = tmp[0];
                    }
                }
            }
        }

        PyObject result = x.__pow__(y, z);
        if (result != null) {
            return result;
        }

        throw Py.TypeError(String.format("unsupported operand type(s) for pow(): '%.100s', "
                        + "'%.100s', '%.100s'", x.getType().fastGetName(),
                y.getType().fastGetName(), z.getType().fastGetName()));
    }

    public static PyObject print(PyObject args[], String kwds[]) {
        int kwlen = kwds.length;
        int argslen = args.length;
        boolean useUnicode = false;
        PyObject values[] = new PyObject[argslen - kwlen];
        System.arraycopy(args, 0, values, 0, argslen - kwlen);
        PyObject keyValues[] = new PyObject[kwlen];
        System.arraycopy(args, argslen - kwlen, keyValues, 0, kwlen);
        ArgParser ap = new ArgParser("print", keyValues, kwds, new String[]{"sep", "end", "file", "flush"});
        for (PyObject keyValue : keyValues) {
            if (keyValue instanceof PyUnicode) {
                //If "file" is passed in as PyUnicode, that's OK as it will error later.
                useUnicode = true;
            }
        }
        String sep = ap.getString(0, null);
        String end = ap.getString(1, null);
        PyObject file = ap.getPyObject(2, null);
        boolean flush = ap.getBoolean(3, false);
        return print(values, sep, end, file, useUnicode, flush);
    }

    private static PyObject print(PyObject values[], String sep, String end,
                                  PyObject file, boolean useUnicode, boolean flush) {
        StdoutWrapper out;
        if (file != null && file != Py.None) {
            out = new FixedFileWrapper(file);
        } else {
            out = Py.stdout;
        }
        if (values.length == 0) {
            out.println(useUnicode);
        } else {
            if (!useUnicode) {
                for (PyObject value : values) {
                    if (value instanceof PyUnicode) {
                        useUnicode = true;
                        break;
                    }
                }
            }

            PyObject sepObject;
            if (sep == null) {
                sepObject = useUnicode ? Py.UnicodeSpace : Py.Space;
            } else {
                sepObject = useUnicode ? Py.newUnicode(sep) : Py.newString(sep);
            }

            PyObject endObject;
            if (end == null) {
                endObject = useUnicode ? Py.UnicodeNewline : Py.Newline;
            } else {
                endObject = useUnicode ? Py.newUnicode(end) : Py.newString(end);
            }

            out.print(values, sepObject, endObject);
            if (flush) {
                out.flush();
            }
        }
        return Py.None;
    }

    private static PyUnicode readline(PyObject file) {
        PyObject ret = file.invoke("readline");
        if (!(ret instanceof PyUnicode)) {
            throw Py.TypeError("object.readline() returned non-string");
        }
        if (ret.equals(Py.EmptyUnicode)) {
            throw Py.EOFError("EOF when reading a line");
        }
        return (PyUnicode) ret;
    }

    /**
     * Companion to <code>raw_input</code> built-in function used when the interactive interpreter
     * is directed to a file.
     *
     * @param prompt to issue at console before read
     * @param file   a file-like object to read from
     * @return line of text from the file (encoded as bytes values compatible with PyBytes)
     */
    public static PyObject raw_input2(PyObject prompt, PyObject file) {
        if (prompt != null && prompt != Py.None) {
            PyObject stdout = Py.getSystemState().getStdout();
            if (stdout == null) {
                throw Py.RuntimeError("input(): lost sys.stdout");
            } else {
                Py.print(stdout, prompt);
            }
        }
        PyUnicode data = readline(file);
        return data.str_rstrip(null);
    }

    /**
     * Implementation of <code>raw_input(prompt)</code> built-in function using the console
     * indirectly via <code>sys.stdin</code> and <code>sys.stdin</code>.
     *
     * @param prompt to issue at console before read
     * @return line of text from console (encoded as bytes values compatible with PyBytes)
     */
    public static PyObject raw_input1(PyObject prompt) {
        PyObject stdin = Py.getSystemState().getStdin();
        if (stdin == null) {
            throw Py.RuntimeError("input: lost sys.stdin");
        }
        return raw_input2(prompt, stdin);
    }

    /**
     * Implementation of <code>raw_input()</code> built-in function using the console directly.
     *
     * @return line of text from console (encoded as bytes values compatible with PyBytes)
     */
    public static PyObject raw_input0() {
        return raw_input1(null);
    }

    public static PyObject repr(PyObject o) {
        return Abstract.PyObject_Repr(Py.getThreadState(), o);
    }

    public static PyObject round(PyObject args[], String kwds[]) {
        ArgParser ap = new ArgParser("round", args, kwds, new String[]{"number", "ndigits"}, 0);
        PyObject number = ap.getPyObject(0);
        PyObject ndigits = ap.getPyObject(1, null);
        PyObject round = number.getType().__findattr__("__round__");
        if (round == null) {
            throw Py.TypeError(String.format("type %s doesn't define __round__ method", number.getType()));
        }
        if (ndigits == null) {
            return round.__get__(number, number.getType()).__call__();
        }
        return round.__get__(number, number.getType()).__call__(ndigits);
    }

    public static PyObject setattr(PyObject obj, PyObject name, PyObject value) {
        obj.__setattr__(asName(name, "setattr"), value);
        return Py.None;
    }

    public static PyObject sorted(PyObject args[], String kwds[]) {
        if (args.length == 0) {
            throw Py.TypeError("sorted() takes at least 1 argument (0 given)");
        } else if (args.length > 3) {
            throw Py.TypeError(String.format("sorted() takes at most 3 arguments (%d given)",
                    args.length));
        } else {
            PyObject iter = PyObject.getIter(args[0]);
            if (iter == null) {
                throw Py.TypeError(String.format("'%s' object is not iterable",
                        args[0].getType().fastGetName()));
            }
        }

        PyList seq = new PyList(args[0]);
        ArgParser ap = new ArgParser("sorted", args, kwds,
                new String[]{"iterable", "key", "reverse"}, 0);
        PyObject key = ap.getPyObject(1, Py.None);
        PyObject reverse = ap.getPyObject(2, Py.Zero);
        seq.sort(key, reverse);
        return seq;
    }

    public static PyObject sum2(PyObject seq, PyObject result) {
        if (result instanceof PyUnicode) {
            throw Py.TypeError("sum() can't sum strings [use ''.join(seq) instead]");
        }
        if (result instanceof PyBytes) {
            throw Py.TypeError("sum() can't sum bytes [use b''.join(seq) instead]");
        }
        if (result instanceof PyByteArray) {
            throw Py.TypeError("sum() can't sum bytearray [use b''.join(seq) instead]");
        }
        for (PyObject item : seq.asIterable()) {
            result = result._add(Py.getThreadState(), item);
        }
        return result;
    }

    public static PyObject reversed(PyObject seq) {
        PyObject reversed = seq.__findattr__("__reversed__");
        if (reversed != null) {
            return reversed.__call__();
        } else if (seq.__findattr__("__getitem__") != null && seq.__findattr__("__len__") != null
                && seq.__findattr__("keys") == null) {
            reversed = new PyReversedIterator(seq);
        } else {
            throw Py.TypeError("argument to reversed() must be a sequence");
        }
        return reversed;
    }

    public static PyObject sum1(PyObject seq) {
        return sum2(seq, Py.Zero);
    }

    public static PyType type(PyObject o) {
        return o.getType();
    }

    public static PyObject vars0() {
        return locals();
    }

    public static PyObject vars1(PyObject o) {
        try {
            return o.__getattr__("__dict__");
        } catch (PyException e) {
            if (e.match(Py.AttributeError)) {
                throw Py.TypeError("vars() argument must have __dict__ attribute");
            }
            throw e;
        }
    }

    public static PyObject buildClass(PyObject[] args, String[] keywords) {
        PyFunction func = (PyFunction) args[0];
        PyObject className = args[1];
        PyObject[] bases = new PyObject[args.length - keywords.length - 2];
        System.arraycopy(args, 2, bases, 0, bases.length);
        PyObject metaclass = null;
        int index = -1;
        for (int i = 0; i < keywords.length; i++) {
            if ("metaclass".equals(keywords[i])) {
                index = i;
                break;
            }
        }
        PyObject[] newArgs;
        if (index >= 0) {
            metaclass = args[bases.length + index + 2];
            // fast path
            if (keywords.length == 1) {
                keywords = new String[0];
            } else {
                String[] newKeywords = new String[keywords.length - 1];
                for (int i = 0; i < keywords.length; i++) {
                    if (i == index) {
                        continue;
                    }
                    if (i < index) {
                        newKeywords[i] = keywords[i];
                    } else {
                        newKeywords[i - 1] = keywords[i];
                    }
                }
                keywords = newKeywords;
            }
            newArgs = new PyObject[keywords.length + 2];
            if (keywords.length > 0) {
                System.arraycopy(args, bases.length + 3, newArgs, 2, index);
                System.arraycopy(args, bases.length + index + 3, newArgs, index + 2, keywords.length - index);
            }
        } else {
            metaclass = Py.findMetaclass(bases);
            newArgs = new PyObject[keywords.length + 2];
            System.arraycopy(args, bases.length, newArgs, 2, keywords.length);
        }
        PyObject prepare = metaclass.__findattr__("__prepare__");
        PyObject basesArray = new PyTuple(bases);
        newArgs[0] = className;
        newArgs[1] = basesArray;
        PyObject ns = prepare.__call__(newArgs, keywords);
        ThreadState state = Py.getThreadState();
        PyObject cell = Py.runCode(state, func.__code__, func.__globals__, ns, (PyTuple) func.__closure__);
        PyObject cls;
        boolean isWide = keywords.length > 0;
        try {
            if (isWide) {
                PyObject[] newArgs2 = new PyObject[newArgs.length + 1];
                System.arraycopy(newArgs, 0, newArgs2, 0, 2);
                newArgs2[2] = ns;
                System.arraycopy(newArgs, 2, newArgs2, 3, newArgs.length - 2);
                cls = metaclass.__call__(newArgs2, keywords);
            } else {
                cls = metaclass.__call__(className, basesArray, ns);
            }
        } catch (PyException pye) {
            if (!pye.match(Py.TypeError)) {
                throw pye;
            }
            pye.value = Py.newUnicode(String.format("Error when calling the metaclass bases\n    "
                    + "%s", BuiltinModule.repr(pye.value)));
            throw pye;
        }
        if (cls instanceof PyType && cell instanceof PyCell) {
            PyObject cell_cls = ((PyCell) cell).ob_ref;
            if (cell_cls != cls) {
                if (cell_cls == null) {
                    String msg = "__class__ not set defining %.200s as %.200s. Was __classcell__ propagated to type.__new__?";
                    Py.warning(Py.DeprecationWarning, String.format(msg, className, cls));
                    ((PyCell) cell).ob_ref = cls;
                } else {
                    throw Py.TypeError(String.format("__class__ set to %.200s defining %.200s as %.200s", cell_cls, className, cls));
                }
            }
        }
        return cls;
    }

    /**
     * Return an interned String from name, raising a TypeError when conversion fails.
     *
     * @param name     a PyObject
     * @param function name of the python function caller
     * @return an interned String
     */
    private static String asName(PyObject name, String function) {
        if (name instanceof PyUnicode) {
            return ((PyUnicode) name).getString().intern();
        }
        throw Py.TypeError(function + "(): attribute name must be string");
    }

    static PyObject max_min(PyObject o, PyObject key, PyObject defaultVal, CompareOp op) {
        PyObject max = null;
        PyObject maxKey = null;
        for (PyObject item : o.asIterable()) {
            PyObject itemKey;
            if (key == null) {
                itemKey = item;
            } else {
                itemKey = key.__call__(item);
            }
            if (maxKey == null || itemKey.richCompare(maxKey, op).isTrue()) {
                maxKey = itemKey;
                max = item;
            }
        }
        if (max == null) {
            if (defaultVal != null) {
                return defaultVal;
            }
            throw Py.ValueError("max_min of empty sequence");
        }
        return max;
    }
}

