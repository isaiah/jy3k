/*
 * Copyright (c) Corporation for National Research Initiatives
 * Copyright (c) Jython Developers
 */
package org.python.core;

import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class BaseCode {
    public static boolean isWideCall(MethodType argType) {
        // ThreadState;PyFrame;PyObject[];String[];
        return argType.parameterCount() == 4 && argType.parameterType(2) == PyObject[].class
                && argType.parameterType(3) == String[].class;
    }

    public static PyFrame createFrame(PyObject funcObj, ThreadState ts) {
        return createFrame(funcObj, ts, Py.EmptyObjects, Py.NoKeywords);
    }

    /**
     *  Destruct vararg and kwarg
     *  @returns a (PyObject[], String[]) pair
     */
    public static Object[] destructArguments(PyObject funcObj, List<PyObject> arglist, String[] keywords,
                                      PyObject[] kwargsArray) {

        PyObject[] args = arglist.toArray(new PyObject[0]);
        int argslen = args.length;

        for (PyObject kwargs : kwargsArray) {
            argslen += kwargs.__len__();
        }

        PyObject[] newargs = new PyObject[argslen];
        int argidx = args.length;
        if (argslen > args.length) {
            System.arraycopy(args, 0, newargs, 0, argidx);
        }

        for (PyObject kwargs : kwargsArray) {
            String[] newkeywords = new String[keywords.length + kwargs.__len__()];
            System.arraycopy(keywords, 0, newkeywords, 0, keywords.length);

            if (kwargs.__len__() == 0) {
                continue;
            }
            PyObject keys = kwargs.invoke("keys");
            int i = 0;
            Iterator<PyObject> keysIter = keys.asIterable().iterator();
            for (PyObject key; keysIter.hasNext();) {
                key = keysIter.next();
                if (!(key instanceof PyUnicode))
                    throw Py.TypeError(getFuncName(funcObj) + "keywords must be strings");
                newkeywords[keywords.length + i++] =
                    ((PyUnicode) key).internedString();
                newargs[argidx++] = kwargs.__finditem__(key);
            }
            keywords = newkeywords;
        }
        if (newargs.length > args.length)
            args = newargs;
        return new Object[]{args, keywords};
    }

    /**
     * Create a frame with bound receiver and arguments
     */
    public static PyFrame createFrame(PyObject funcObj, PyObject self, PyObject[] args) {
        PyObject[] newArgs = new PyObject[args.length + 1];
        newArgs[0] = self;
        System.arraycopy(args, 0, newArgs, 1, args.length);
        return createFrame(funcObj, newArgs);
    }
    /**
     * Create a frame with arguments only
     */
    public static PyFrame createFrame(PyObject funcObj, PyObject[] args) {
        PyFunction function;
        if (funcObj instanceof PyFunction) {
            function = (PyFunction) funcObj;
        } else if (funcObj instanceof PyMethod){
            function = (PyFunction) ((PyMethod) funcObj).__func__;
        } else {
            function = null;
        }
        PyFrame frame = createFrame((PyTableCode) function.__code__, args, Py.NoKeywords, function.__globals__,
                function.__defaults__, function.__kwdefaults__, function.__closure__);
        return frame;
    }

    /**
     * Create a frame with arguments, keywords and bound receiver, but without threadstate, use by generator functions
     * @param funcObj
     * @param args
     * @param keywords
     * @return
     */
    public static PyFrame createFrame(PyObject funcObj, PyObject self, PyObject[] args, String[] keywords) {
        PyObject[] newArgs = new PyObject[args.length + 1];
        newArgs[0] = self;
        System.arraycopy(args, 0, newArgs, 1, args.length);
        return createFrame(funcObj, newArgs, keywords);
    }

    /**
     * Create a frame with arguments and keywords, but without threadstate, use by generator functions
     * @param funcObj
     * @param args
     * @param keywords
     * @return
     */
    public static PyFrame createFrame(PyObject funcObj, PyObject[] args, String[] keywords) {
        PyFunction function;
        if (funcObj instanceof PyFunction) {
            function = (PyFunction) funcObj;
        } else {
            function = (PyFunction) ((PyMethod) funcObj).__func__;
        }
        PyFrame frame = createFrame((PyTableCode) function.__code__, args, keywords, function.__globals__,
                function.__defaults__, function.__kwdefaults__, function.__closure__);
        return frame;
    }

    public static PyFrame createFrameWithSelf(PyObject funcObj, ThreadState ts, PyObject self, PyObject[] args, String[] keywords) {
        PyObject[] newArgs = new PyObject[args.length + 1];
        newArgs[0] = self;
        System.arraycopy(args, 0, newArgs, 1, args.length);
        return createFrame(funcObj, ts, newArgs, keywords);
    }

    /**
     * create a frame with arguments
     */
    public static PyFrame createFrame(PyObject funcObj, ThreadState ts, PyObject[] args, String[] keywords) {
        PyFunction function;
        if (funcObj instanceof PyFunction) {
            function = (PyFunction) funcObj;
        } else {
            function = (PyFunction) ((PyMethod) funcObj).__func__;
        }
        PyFrame frame = createFrame((PyTableCode) function.__code__, args, keywords, function.__globals__,
                function.__defaults__, function.__kwdefaults__, function.__closure__);
        frame.f_back = ts.frame;
        frame.fBackExecSize = ts.exceptions.size();
        ts.frame = frame;
        return frame;
    }

    /**
     * Create a frame from arguments and closure
     * @param args
     * @param kws
     * @param globals
     * @param defs
     * @param kwDefaults
     * @return
     */
    public static PyFrame createFrame(PyTableCode code, PyObject[] args, String kws[], PyObject globals,
                                      PyObject[] defs, PyDictionary kwDefaults, PyObject closure) {
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
            // insert the extra parameter to the vararg slot
            if (code.varargs) {
                n = argcount - code.co_argcount;
                if (n > 0) {
                    PyObject[] u = new PyObject[n];
                    System.arraycopy(args, code.co_argcount, u, 0, n);
                    fastlocals[code.co_argcount] = new PyTuple(u);
                } else {
                    fastlocals[code.co_argcount] = Py.EmptyTuple;
                }
            }
        } else if ((argcount > 0) || (args.length > 0 && (paramCount == 0 && !code.varargs && !code.varkwargs))) {
            throw Py.TypeError(String.format("%.200s() takes no arguments (%d given)",
                                             code.co_name, args.length));
        }
        // check possible arguements which are also cellvar
        frame.setupEnv((PyTuple) closure);
        if (code.co_cell2arg != null) {
            for (int i = 0; i < code.co_cell2arg.length; i++) {
                int j = code.co_cell2arg[i];
                if (i != PyTableCode.CO_CELL_NOT_AN_ARG) {
                    PyObject arg = frame.f_fastlocals[j];
                    frame.f_fastlocals[j] = null;
                    frame.setderef(i, arg);
                }
            }
        }
        return frame;
    }

    private static String positionalArgErrorMessage(String name, int min, int max, int argLength) {
        return String.format("%.200s() takes from %d to %d positional arguments but %d were given",
                name, min, max, argLength);
    }

    private static String positionalArgErrorMessage(String name, int paramLength, int argLength) {
        return String.format("%.200s() takes %d positional argument but %d were given", name, paramLength, argLength);
    }

    private static String getFuncName(PyObject funcObj) {
        String name;
        if (funcObj instanceof PyFunction) {
            name = ((PyFunction) funcObj).__name__ + "() ";
        } else if (funcObj instanceof PyBuiltinCallable) {
            name = ((PyBuiltinCallable)funcObj).fastGetName().toString() + "() ";
        } else {
            name = funcObj.getType().fastGetName() + " ";
        }
        return name;
    }
}
