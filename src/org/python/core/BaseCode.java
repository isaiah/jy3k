/*
 * Copyright (c) Corporation for National Research Initiatives
 * Copyright (c) Jython Developers
 */
package org.python.core;

import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;

public class BaseCode {
    public static boolean isWideMethod(MethodType argType) {
        // ThreadState;PyFrame;PyObject[];String[];
        return argType.parameterCount() == 4 && argType.parameterType(2) == PyObject[].class && argType.parameterType(3) == String[].class;
    }

    public static PyFrame createFrame(PyObject funcObj, ThreadState ts) {
        return createFrame(funcObj, ts, Py.EmptyObjects, Py.NoKeywords);
    }
    // create a frame with arguments and without ThreadState
    public static PyFrame createFrame(PyObject funcObj, PyObject[] args) {
        PyFunction function = (PyFunction) funcObj;
        PyFrame frame = createFrame((PyTableCode) function.__code__, args, Py.NoKeywords, function.__globals__,
                function.__defaults__, function.__kwdefaults__);
        frame.setupEnv((PyTuple) function.__closure__);
        return frame;
    }

    // create a frame with arguments
    public static PyFrame createFrame(PyObject funcObj, ThreadState ts, PyObject[] args, String[] keywords) {
        PyFunction function = (PyFunction) funcObj;
        PyFrame frame = createFrame((PyTableCode) function.__code__, args, keywords, function.__globals__,
                function.__defaults__, function.__kwdefaults__);
        frame.f_back = ts.frame;
        frame.fBackExecSize = ts.exceptions.size();
        ts.frame = frame;
        frame.setupEnv((PyTuple) function.__closure__);
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
                                      PyObject[] defs, PyDictionary kwDefaults) {
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

    private static String positionalArgErrorMessage(String name, int min, int max, int argLength) {
        return String.format("%.200s() takes from %d to %d positional arguments but %d were given",
                name, min, max, argLength);
    }

    private static String positionalArgErrorMessage(String name, int paramLength, int argLength) {
        return String.format("%.200s() takes %d positional argument but %d were given", name, paramLength, argLength);
    }
}
