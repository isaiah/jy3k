/*
 * Copyright (c) Corporation for National Research Initiatives
 * Copyright (c) Jython Developers
 */
package org.python.core;

import org.python.core.generator.PyAsyncGenerator;
import org.python.core.generator.PyCoroutine;
import org.python.core.generator.PyGenerator;
import org.python.modules._systemrestart;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class PyBaseCode extends PyCode {

    public int co_argcount;
    public int co_kwonlyargcount;
    int nargs;
    public int co_firstlineno = -1;
    public String[] co_varnames;
    public String[] co_names;
    public String[] co_cellvars;
    public String[] co_freevars;
    public PyObject[] co_consts;
    public String co_filename;
    public int jy_npurecell; // internal: jython specific
    public CompilerFlags co_flags = new CompilerFlags();
    public int co_nlocals;
    public boolean varargs,  varkwargs;


    public boolean hasFreevars() {
        return co_freevars != null && co_freevars.length > 0;
    }

    @Override
    public PyObject call(ThreadState ts, PyFrame frame, PyObject closure) {
        if (ts.systemState == null) {
            ts.systemState = Py.defaultSystemState;
        }

        // Push frame
        frame.f_back = ts.frame;
        // nested scopes: setup env with closure
        // this should only be done once, so let the frame take care of it
        frame.setupEnv((PyTuple) closure);

        ts.frame = frame;

        // Handle trace function for debugging
        if (ts.tracefunc != null) {
            frame.f_lineno = co_firstlineno;
            frame.tracefunc = ts.tracefunc.traceCall(frame);
        }

        // Handle trace function for profiling
        if (ts.profilefunc != null) {
            ts.profilefunc.traceCall(frame);
        }

        PyObject ret;
        ThreadStateMapping.enterCall(ts);
        try {
            ret = interpret(frame, ts);
        } catch (Throwable t) {
            // Convert Exceptions that occurred in Java code to PyExceptions
            PyException pye = Py.JavaError(t);
            pye.normalize();
            pye.tracebackHere(frame);

            frame.f_lasti = -1;

            if (frame.tracefunc != null) {
                frame.tracefunc.traceException(frame, pye);
            }
            if (ts.profilefunc != null) {
                ts.profilefunc.traceException(frame, pye);
            }

            ts.frame = ts.frame.f_back;
            throw pye;
        } finally {
            ThreadStateMapping.exitCall(ts);
        }

        if (frame.tracefunc != null) {
            frame.tracefunc.traceReturn(frame, ret);
        }
        // Handle trace function for profiling
        if (ts.profilefunc != null) {
            ts.profilefunc.traceReturn(frame, ret);
        }

        ts.frame = ts.frame.f_back;

        // Check for interruption, which is used for restarting the interpreter
        // on Jython
        if (ts.systemState._systemRestart && Thread.currentThread().isInterrupted()) {
            throw new PyException(_systemrestart.SystemRestart);
        }
        return ret;
    }

    private boolean extractArg(int arg) {
        return co_argcount == arg && co_kwonlyargcount == 0 && !varargs && !varkwargs;
    }

    @Override
    public PyObject call(ThreadState state, PyObject globals, PyObject[] defaults,
                         PyDictionary kw_defaults, PyObject closure)
    {
        if (!extractArg(0))
            return call(state, Py.EmptyObjects, Py.NoKeywords, globals, defaults,
                        kw_defaults, closure);
        PyFrame frame = new PyFrame(this, globals);
        if (co_flags.isFlagSet(CodeFlag.CO_COROUTINE)) {
            return new PyCoroutine(frame, closure);
        } else if (co_flags.isFlagSet(CodeFlag.CO_ASYNC_GENERATOR)) {
            return new PyAsyncGenerator(frame, closure);
        } else if (co_flags.isFlagSet(CodeFlag.CO_GENERATOR)) {
            return new PyGenerator(frame, closure);
        }
        return call(state, frame, closure);
    }

    @Override
    public PyObject call(ThreadState state, PyObject arg1, PyObject globals, PyObject[] defaults,
                         PyDictionary kw_defaults, PyObject closure)
    {
        if (!extractArg(1))
            return call(state, new PyObject[] {arg1},
                        Py.NoKeywords, globals, defaults, kw_defaults, closure);
        PyFrame frame = new PyFrame(this, globals);
        frame.f_fastlocals[0] = arg1;
        if (co_flags.isFlagSet(CodeFlag.CO_COROUTINE)) {
            return new PyCoroutine(frame, closure);
        } else if (co_flags.isFlagSet(CodeFlag.CO_ASYNC_GENERATOR)) {
            return new PyAsyncGenerator(frame, closure);
        } else if (co_flags.isFlagSet(CodeFlag.CO_GENERATOR)) {
            return new PyGenerator(frame, closure);
        }
        return call(state, frame, closure);
    }

    @Override
    public PyObject call(ThreadState state, PyObject arg1, PyObject arg2, PyObject globals,
                         PyObject[] defaults, PyDictionary kw_defaults, PyObject closure)
    {
        if (!extractArg(2))
            return call(state, new PyObject[] {arg1, arg2},
                        Py.NoKeywords, globals, defaults, kw_defaults, closure);
        PyFrame frame = new PyFrame(this, globals);
        frame.f_fastlocals[0] = arg1;
        frame.f_fastlocals[1] = arg2;
        if (co_flags.isFlagSet(CodeFlag.CO_GENERATOR)) {
            return new PyGenerator(frame, closure);
        } else if (co_flags.isFlagSet(CodeFlag.CO_COROUTINE)) {
            return new PyCoroutine(frame, closure);
        } else if (co_flags.isFlagSet(CodeFlag.CO_ASYNC_GENERATOR)) {
            return new PyAsyncGenerator(frame, closure);
        }
        return call(state, frame, closure);
    }

    @Override
    public PyObject call(ThreadState state, PyObject arg1, PyObject arg2, PyObject arg3,
                         PyObject globals, PyObject[] defaults, PyDictionary kw_defaults,
                         PyObject closure)
    {
        if (!extractArg(3))
            return call(state, new PyObject[] {arg1, arg2, arg3},
                        Py.NoKeywords, globals, defaults, kw_defaults, closure);
        PyFrame frame = new PyFrame(this, globals);
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
        return call(state, frame, closure);
    }
    
    @Override
    public PyObject call(ThreadState state, PyObject arg1, PyObject arg2,
                         PyObject arg3, PyObject arg4, PyObject globals,
                         PyObject[] defaults, PyDictionary kw_defaults, PyObject closure) {
        if (!extractArg(4))
            return call(state, new PyObject[]{arg1, arg2, arg3, arg4},
                        Py.NoKeywords, globals, defaults, kw_defaults, closure);
        PyFrame frame = new PyFrame(this, globals);
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
        return call(state, frame, closure);
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

    /**
     * Create a frame from arguments and closure
     * @param args
     * @param kws
     * @param globals
     * @param defs
     * @param kwDefaults
     * @param closure
     * @return
     */
    public static PyFrame createFrame(PyBaseCode code, PyObject[] args, String kws[], PyObject globals, PyObject[] defs,
                                      PyDictionary kwDefaults, PyObject closure) {
        final PyFrame frame = new PyFrame(code, globals);
        final int argcount = args.length - kws.length;
        int paramCount = code.paramCount();
        if ((paramCount > 0) || code.varargs || code.varkwargs) {
            // n is the position of the last positional parameter
            int n = argcount;
            PyObject kwdict = null;
            final PyObject[] fastlocals = frame.f_fastlocals;

            if (code.varkwargs) {
                kwdict = new PyDictionary();
                fastlocals[paramCount - 1] = kwdict;
            }

            if (argcount > code.co_argcount) {
                // extra positional arguments
                if (!code.varargs) {
                    int defcount = defs != null ? defs.length : 0;
                    String msg;
                    if (defcount > 0) {
                        msg = positionalArgErrorMessage(code.co_name, code.co_argcount - defcount,
                                code.co_argcount, argcount);
                    } else {
                        msg = positionalArgErrorMessage(code.co_name, code.co_argcount, argcount);
                    }
                    throw Py.TypeError(msg);
                }
                n = code.co_argcount;
            }

            if (args.length > 0) {
                System.arraycopy(args, 0, fastlocals, 0, n);
            }

            // insert the extra parameter to the vararg slot
            if (code.varargs) {
                PyObject[] u = new PyObject[argcount - n];
                if (u.length > 0)
                    System.arraycopy(args, n, u, 0, u.length);
                fastlocals[n] = new PyTuple(u);
            }
            for (int i = 0; i < kws.length; i++) {
                String keyword = kws[i];
                PyObject value = args[i + argcount];
                int j;
                // look for the position of the keyword argument
                for (j = 0; j < paramCount; j++) {
                    // skip the vararg parameter, it cannot be assigned as a keyword
                    if (code.varargs && j == code.co_argcount)
                        continue;
                    if (code.co_varnames[j].equals(keyword)) {
                        break;
                    }
                }
                // not in varnames
                if (j == paramCount) {
                    if (code.varkwargs) {
                        if (keyword.chars().allMatch(c -> c < 127)) {
                            kwdict.__setitem__(keyword, value);
                        } else {
                            kwdict.__setitem__(Py.newUnicode(keyword), value);
                        }
                    } else {
                        // unexpected keyword argument
                        throw Py.TypeError(String.format(
                                "%.200s() got an unexpected keyword argument '%.400s'",
                                code.co_name,
                                Py.newUnicode(keyword).encode("ascii", "replace")));
                    }
                } else {
                    if (fastlocals[j] != null) {
                        // keyword param got multiple assignment
                        throw Py.TypeError(String.format("%.200s() got multiple values for keyword argument '%.400s'",
                                code.co_name, keyword));
                    }
                    fastlocals[j] = value;
                }
            }

            // check for missing keyword only parameter
            boolean missingArg = Stream.of(fastlocals).anyMatch(Objects::isNull);
            if (missingArg && code.co_kwonlyargcount > 0) {
                java.util.List<String> missingKwArg = new ArrayList<>();
                int kwonlyargZeroIndex = code.co_argcount;
                if (code.varargs) kwonlyargZeroIndex++;
                for (int j = 0; j < code.co_kwonlyargcount; j++) {
                    int kwonlyargIdx = kwonlyargZeroIndex + j;
                    String name = code.co_varnames[kwonlyargIdx];
                    PyUnicode key = Py.newUnicode(name);
                    if (fastlocals[kwonlyargIdx] == null) {
                        if (kwDefaults.__contains__(key)) {
                            fastlocals[kwonlyargIdx] = kwDefaults.__getitem__(key);
                        } else {
                            missingKwArg.add(name);
                        }
                    }
                }
                if (!missingKwArg.isEmpty()) {
                    throw Py.TypeError(String.format("%.200s() missing %d keyword-only %s: '%s'", code.co_name, missingKwArg.size(),
                            missingKwArg.size() > 1 ? "arguments" : "argument", String.join(",", missingKwArg)));
                }
            }

            // filling missing parameter with default value
            if (missingArg && argcount < code.co_argcount) {
                final int defcount = defs != null ? defs.length : 0;
                final int m = code.co_argcount - defcount;
                for (int i = argcount; i < m; i++) {
                    if (fastlocals[i] == null) {
                        String msg =
                                String.format("%.200s() takes %s %d %sargument%s (%d given)",
                                              code.co_name,
                                              (code.varargs || defcount > 0) ? "at least" : "exactly",
                                              m,
                                              kws.length > 0 ? "" : "",
                                              m == 1 ? "" : "s",
                                              argcount);
                        throw Py.TypeError(msg);
                    }
                }
                int i = 0;
                if (n > m) {
                    i = n - m;
                }
                for (; i < defcount; i++) {
                    if (fastlocals[m + i] == null) {
                        fastlocals[m + i] = defs[i];
                    }
                }
            }
        } else if ((argcount > 0) || (args.length > 0 && (paramCount == 0 && !code.varargs && !code.varkwargs))) {
            throw Py.TypeError(String.format("%.200s() takes no arguments (%d given)",
                                             code.co_name, args.length));
        }
        return frame;
    }

    @Override
    public PyObject call(ThreadState state, PyObject args[], String kws[], PyObject globals,
                         PyObject[] defs, PyDictionary kw_defaults, PyObject closure) {
        PyFrame frame = createFrame(this, args, kws, globals, defs, kw_defaults, closure);
        if (co_flags.isFlagSet(CodeFlag.CO_GENERATOR)) {
            return new PyGenerator(frame, closure);
        } else if (co_flags.isFlagSet(CodeFlag.CO_COROUTINE)) {
            return new PyCoroutine(frame, closure);
        } else if (co_flags.isFlagSet(CodeFlag.CO_ASYNC_GENERATOR)) {
            return new PyAsyncGenerator(frame, closure);
        }
        return call(state, frame, closure);
    }

    public String toString() {
        return String.format("<code object %.100s at %s, file \"%.300s\", line %d>",
                             co_name, Py.idstr(this), co_filename, co_firstlineno);
    }

    protected abstract PyObject interpret(PyFrame f, ThreadState ts);

    protected int getline(PyFrame f) {
         return f.f_lineno;
    }

    // returns the augmented version of CompilerFlags (instead of just as a bit vector int)
    public CompilerFlags getCompilerFlags() {
        return co_flags;
    }

    private static String positionalArgErrorMessage(String name, int min, int max, int argLength) {
        return String.format("%.200s() takes from %d to %d positional arguments but %d were given",
                name, min, max, argLength);
    }

    private static String positionalArgErrorMessage(String name, int paramLength, int argLength) {
        return String.format("%.200s() takes %d positional argument but %d were given", name, paramLength, argLength);
    }
}
