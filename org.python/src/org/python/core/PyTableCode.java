// Copyright (c) Corporation for National Research Initiatives
package org.python.core;

/**
 * An implementation of PyCode where the actual executable content
 * is stored as a PyFunctionTable instance and an integer index.
 */

import org.python.core.generator.PyAsyncGenerator;
import org.python.core.generator.PyCoroutine;
import org.python.core.generator.PyGenerator;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedType;
import org.python.modules._systemrestart;

import java.util.Arrays;

@Untraversable
@ExposedType(name = "code", base = PyObject.class, doc = BuiltinDocs.code_doc)
public class PyTableCode extends PyCode {

    public static final int CO_CELL_NOT_AN_ARG = 255;

    @ExposedGet
    public int co_argcount;
    @ExposedGet
    public int co_kwonlyargcount;
    @ExposedGet
    public int co_firstlineno = -1;
    /** All names */
    public String[] co_names;
    /** local var names */
    public String[] co_varnames;
    public String[] co_cellvars;
    public int[] co_cell2arg; // maps cellvars which are arguments
    public String[] co_freevars;
    public PyObject[] co_consts;
    @ExposedGet
    public String co_filename;
    public CompilerFlags co_flags = new CompilerFlags();
    @ExposedGet
    public int co_nlocals;
    public boolean varargs, varkwargs;
    public PyFunctionTable funcs;
    int func_id;
    public String co_code = ""; // only used by inspect
    public Class<?> klazz;
    public String funcname;
    int nargs;

    public PyTableCode(int argcount, String names[],
                       String filename, String name,
                       int firstlineno,
                       boolean varargs, boolean varkwargs,
                       PyFunctionTable funcs, int func_id)
    {
        this(argcount, names, filename, name, firstlineno, varargs,
             varkwargs, funcs, func_id, null, null, null, null,
                0, 0, null);
    }

    public PyTableCode(int argcount, String[] names,
                       String filename, String name,
                       int firstlineno,
                       boolean varargs, boolean varkwargs,
                       PyFunctionTable funcs, int func_id,
                       String[] cellvars, String[] freevars, String[] varnames,
                       PyObject[] consts, int kwonlyargcount, int moreflags, String funcname) // may change
    {
        co_argcount = nargs = argcount;
        co_varnames = varnames;
        co_names = names;
        co_consts = consts;
        co_nlocals = varnames == null ? 0 : varnames.length;
        co_filename = filename;
        co_firstlineno = firstlineno;
        co_cellvars = cellvars;
        co_freevars = freevars;
        this.varargs = varargs;
        co_name = name;
        if (varargs) {
            co_flags.setFlag(CodeFlag.CO_VARARGS);
        }
        this.varkwargs = varkwargs;
        if (varkwargs) {
            co_flags.setFlag(CodeFlag.CO_VARKEYWORDS);
        }
        this.co_flags = new CompilerFlags(co_flags.toBits() | moreflags);
        this.funcs = funcs;
        this.func_id = func_id;
        this.funcname = funcname;
        this.co_kwonlyargcount = kwonlyargcount;
        if (cellvars.length > 0) {
            this.co_cell2arg = mapCellToArg(cellvars, varnames);
        }
    }

    private int[] mapCellToArg(String[] cellvars, String[] varnames) {
        boolean usedCellToArg = false;
        int[] mapping = new int[cellvars.length];
        Arrays.fill(mapping, CO_CELL_NOT_AN_ARG);
        for (int i = 0; i < cellvars.length; i++) {
            for (int j = 0; j < varnames.length; j++) {
                if (cellvars[i].equals(varnames[j])) {
                    usedCellToArg = true;
                    mapping[i] = j;
                }
            }
        }
        if (usedCellToArg) {
            return mapping;
        }
        return null;
    }

    private static final String[] __members__ = {
        "co_name", "co_argcount",
        "co_varnames", "co_filename", "co_firstlineno",
        "co_flags","co_cellvars","co_freevars", "co_nlocals",
        "co_kwonlyargcount"
        // not supported: co_code, co_consts, co_names,
        // co_lnotab, co_stacksize
    };

//    public PyObject __dir__() {
//        PyUnicode members[] = new PyUnicode[__members__.length];
//        for (int i = 0; i < __members__.length; i++)
//            members[i] = new PyUnicode(__members__[i]);
//        return new PyList(members);
//    }

    private void throwReadonly(String name) {
        for (int i = 0; i < __members__.length; i++)
            if (__members__[i] == name)
                throw Py.TypeError("readonly attribute");
        throw Py.AttributeError(name);
    }

    public void __setattr__(String name, PyObject value) {
        // no writable attributes
        throwReadonly(name);
    }

    public void __delattr__(String name) {
        throwReadonly(name);
    }

    private static PyTuple toPyStringTuple(String[] ar) {
        if (ar == null) return Py.EmptyTuple;
        int sz = ar.length;
        PyUnicode[] pystr = new PyUnicode[sz];
        for (int i = 0; i < sz; i++) {
            pystr[i] = new PyUnicode(ar[i]);
        }
        return new PyTuple(pystr);
    }

    @ExposedGet
    final PyObject co_varnames() {
        return toPyStringTuple(co_varnames);
    }

    @ExposedGet
    final PyObject co_names() {
        return toPyStringTuple(co_names);
    }

    @ExposedGet
    final PyObject co_cellvars() {
        return toPyStringTuple(co_cellvars);
    }

    @ExposedGet
    final PyObject co_freevars() {
        return toPyStringTuple(co_freevars);
    }

    @ExposedGet
    final PyObject co_consts() {
        return new PyTuple(co_consts);
    }

    @ExposedGet
    public final PyObject co_name() {
        return new PyUnicode(co_name);
    }

    @ExposedGet
    public final int co_flags() {
        return co_flags.toBits();
    }

//    @Override
//    public PyObject call(ThreadState ts, PyFrame frame, PyObject closure) {
//        if (ts.systemState == null) {
//            ts.systemState = Py.defaultSystemState;
//        }
//
//        // Cache previously defined exception
////        PyException previous_exception = ts.exceptions.peekFirst();
//        int exceptionsLength = ts.exceptions.size();
//
//        // Push frame
//        frame.f_back = ts.frame;
//        // nested scopes: setup env with closure
//        // this should only be done once, so let the frame take care of it
//        frame.setupEnv((PyTuple)closure);
//
//        ts.frame = frame;
//
//        // Handle trace function for debugging
//        if (ts.tracefunc != null) {
//            frame.f_lineno = co_firstlineno;
//            frame.tracefunc = ts.tracefunc.traceCall(frame);
//        }
//
//        // Handle trace function for profiling
//        if (ts.profilefunc != null) {
//            ts.profilefunc.traceCall(frame);
//        }
//
//        PyObject ret;
//        ThreadStateMapping.enterCall(ts);
//        try {
//            ret = funcs.call_function(func_id, ts, frame);
//        } catch (Throwable t) {
//            // Convert Exceptions that occurred in Java code to PyExceptions
//            PyException pye = Py.JavaError(t);
//            pye.normalize();
//            pye.tracebackHere(frame);
//
//            frame.f_lasti = -1;
//
//            if (frame.tracefunc != null) {
//                frame.tracefunc.traceException(frame, pye);
//            }
//            if (ts.profilefunc != null) {
//                ts.profilefunc.traceException(frame, pye);
//            }
//
//            // Rethrow the exception to the next stack frame
////            ts.exceptions.addFirst(previous_exception);
//            while(ts.exceptions.size() > exceptionsLength) {
//                ts.exceptions.pop();
//            }
//
//            ts.frame = frame.f_back;
//            throw pye;
//        } finally {
//            ThreadStateMapping.exitCall(ts);
//        }
//
//        if (frame.tracefunc != null) {
//            frame.tracefunc.traceReturn(frame, ret);
//        }
//        // Handle trace function for profiling
//        if (ts.profilefunc != null) {
//            ts.profilefunc.traceReturn(frame, ret);
//        }
//
//        // Restore previously defined exception
////        ts.exceptions.poll();
////        ts.exceptions.addFirst(previous_exception);
//        if (exceptionsLength == 0) {
//            ts.exceptions.clear();
//        } else {
//            while (ts.exceptions.size() > exceptionsLength) {
//                ts.exceptions.pop();
//            }
//        }
//
//
//        ts.frame = ts.frame.f_back;
//
//        // Check for interruption, which is used for restarting the interpreter
//        // on Jython
//        if (ts.systemState._systemRestart && Thread.currentThread().isInterrupted()) {
//            throw new PyException(_systemrestart.SystemRestart);
//        }
//        return ret;
//    }

    public boolean hasFreevars() {
        return co_freevars != null && co_freevars.length > 0;
    }

    private boolean extractArg(int arg) {
        return co_argcount == arg && co_kwonlyargcount == 0 && !varargs && !varkwargs && co_cell2arg == null;
    }

    @Override
    public PyObject call(ThreadState state, PyObject globals, PyObject[] defaults,
                         PyDictionary kw_defaults, PyObject closure)
    {
        if (!extractArg(0))
            return call(state, Py.EmptyObjects, Py.NoKeywords, globals, defaults,
                        kw_defaults, closure);
        PyFrame frame = new PyFrame(this, globals, null, closure);
        frame.setupEnv((PyTuple) closure);
        if (co_flags.isFlagSet(CodeFlag.CO_COROUTINE)) {
            return new PyCoroutine(frame, closure);
        } else if (co_flags.isFlagSet(CodeFlag.CO_ASYNC_GENERATOR)) {
            return new PyAsyncGenerator(frame, closure);
        } else if (co_flags.isFlagSet(CodeFlag.CO_GENERATOR)) {
            return new PyGenerator(frame, closure);
        }
        return Py.runCode(state, this, frame);
//        return call(state, frame, closure);
    }

    @Override
    public PyObject call(ThreadState state, PyObject arg1, PyObject globals, PyObject[] defaults,
                         PyDictionary kw_defaults, PyObject closure)
    {
        if (!extractArg(1))
            return call(state, new PyObject[] {arg1},
                        Py.NoKeywords, globals, defaults, kw_defaults, closure);
        PyFrame frame = new PyFrame(this, globals, null, closure);
        frame.f_fastlocals[0] = arg1;
        if (co_flags.isFlagSet(CodeFlag.CO_COROUTINE)) {
            return new PyCoroutine(frame, closure);
        } else if (co_flags.isFlagSet(CodeFlag.CO_ASYNC_GENERATOR)) {
            return new PyAsyncGenerator(frame, closure);
        } else if (co_flags.isFlagSet(CodeFlag.CO_GENERATOR)) {
            return new PyGenerator(frame, closure);
        }
        return Py.runCode(state, this, frame);
//        return call(state, frame, closure);
    }

    @Override
    public PyObject call(ThreadState state, PyObject arg1, PyObject arg2, PyObject globals,
                         PyObject[] defaults, PyDictionary kw_defaults, PyObject closure)
    {
        if (!extractArg(2))
            return call(state, new PyObject[] {arg1, arg2},
                        Py.NoKeywords, globals, defaults, kw_defaults, closure);
        PyFrame frame = new PyFrame(this, globals, null, closure);
        frame.f_fastlocals[0] = arg1;
        frame.f_fastlocals[1] = arg2;
        if (co_flags.isFlagSet(CodeFlag.CO_GENERATOR)) {
            return new PyGenerator(frame, closure);
        } else if (co_flags.isFlagSet(CodeFlag.CO_COROUTINE)) {
            return new PyCoroutine(frame, closure);
        } else if (co_flags.isFlagSet(CodeFlag.CO_ASYNC_GENERATOR)) {
            return new PyAsyncGenerator(frame, closure);
        }
        return Py.runCode(state, this, frame);
//        return call(state, frame, closure);
    }

    @Override
    public PyObject call(ThreadState state, PyObject arg1, PyObject arg2, PyObject arg3,
                         PyObject globals, PyObject[] defaults, PyDictionary kw_defaults,
                         PyObject closure)
    {
        if (!extractArg(3))
            return call(state, new PyObject[] {arg1, arg2, arg3},
                        Py.NoKeywords, globals, defaults, kw_defaults, closure);
        PyFrame frame = new PyFrame(this, globals, null, closure);
        frame.f_fastlocals[0] = arg1;
        frame.f_fastlocals[1] = arg2;
        frame.f_fastlocals[2] = arg3;
        if (co_flags.isFlagSet(CodeFlag.CO_GENERATOR)) {
            return new PyGenerator(frame, closure);
        } else if (co_flags.isFlagSet(CodeFlag.CO_COROUTINE)) {
            return new PyCoroutine(frame, closure);
        } else if (co_flags.isFlagSet(CodeFlag.CO_ASYNC_GENERATOR)) {
            return new PyAsyncGenerator(frame, closure);
        }
        return Py.runCode(state, this, frame);
//        return call(state, frame, closure);
    }

    @Override
    public PyObject call(ThreadState state, PyObject arg1, PyObject arg2,
                         PyObject arg3, PyObject arg4, PyObject globals,
                         PyObject[] defaults, PyDictionary kw_defaults, PyObject closure) {
        if (!extractArg(4))
            return call(state, new PyObject[]{arg1, arg2, arg3, arg4},
                        Py.NoKeywords, globals, defaults, kw_defaults, closure);
        PyFrame frame = new PyFrame(this, globals, null, closure);
        frame.f_fastlocals[0] = arg1;
        frame.f_fastlocals[1] = arg2;
        frame.f_fastlocals[2] = arg3;
        frame.f_fastlocals[3] = arg4;
        if (co_flags.isFlagSet(CodeFlag.CO_GENERATOR)) {
            return new PyGenerator(frame, closure);
        } else if (co_flags.isFlagSet(CodeFlag.CO_COROUTINE)) {
            return new PyCoroutine(frame, closure);

        } else if (co_flags.isFlagSet(CodeFlag.CO_ASYNC_GENERATOR)) {
            return new PyAsyncGenerator(frame, closure);
        }
        return Py.runCode(state, this, frame);
//        return call(state, frame, closure);
    }

    @Override
    public PyObject call(ThreadState state, PyObject self, PyObject args[],
                         String keywords[], PyObject globals,
                         PyObject[] defaults, PyDictionary kw_defaults, PyObject closure)
    {
        PyObject[] os = new PyObject[args.length+1];
        os[0] = self;
        System.arraycopy(args, 0, os, 1, args.length);
        return call(state, os, keywords, globals, defaults, kw_defaults, closure);
    }

    public int paramCount() {
        int paramCount = co_argcount + co_kwonlyargcount;
        if (varargs) paramCount++;
        if (varkwargs) paramCount++;
        return paramCount;
    }

    @Override
    public PyObject call(ThreadState state, PyObject args[], String kws[], PyObject globals,
                         PyObject[] defs, PyDictionary kw_defaults, PyObject closure) {
        PyFrame frame = BaseCode.createFrame(this, args, kws, globals, defs, kw_defaults, closure);
        if (co_flags.isFlagSet(CodeFlag.CO_GENERATOR)) {
            return new PyGenerator(frame, closure);
        } else if (co_flags.isFlagSet(CodeFlag.CO_COROUTINE)) {
            return new PyCoroutine(frame, closure);
        } else if (co_flags.isFlagSet(CodeFlag.CO_ASYNC_GENERATOR)) {
            return new PyAsyncGenerator(frame, closure);
        }
//        return call(state, frame, closure);
        return Py.runCode(state, this, frame);
    }

    public String toString() {
        return String.format("<code object %.100s at %s, file \"%.300s\", line %d>",
                             co_name, Py.idstr(this), co_filename, co_firstlineno);
    }

    protected int getline(PyFrame f) {
         return f.f_lineno;
    }

    // returns the augmented version of CompilerFlags (instead of just as a bit vector int)
    public CompilerFlags getCompilerFlags() {
        return co_flags;
    }
}
