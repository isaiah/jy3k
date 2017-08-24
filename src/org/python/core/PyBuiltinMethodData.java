package org.python.core;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public class PyBuiltinMethodData {

    String name, defaultVals, doc;
    Object[] defaults;
    MethodHandle target;
    boolean isStatic;
    boolean isWide;

    private int maxargs, minargs;

    public PyBuiltinMethodData(String name, int minargs, int maxargs) {
        this.name = name;
        this.minargs = minargs;
        this.maxargs = maxargs;
    }

    /**
     * Create from wide static methods for Exceptions.class
     * @param name
     * @param mh
     */
    public PyBuiltinMethodData(String name, MethodHandle mh) {
        this.name = name;
        this.target = mh;
        this.doc = "";
        this.isWide = true;
        this.isStatic = true;
    }

    /**
     * Information for native implemented methods
     * @param name
     * @param defaultVals
     * @param mh
     * @param doc
     * @param isStatic
     * @param isWide
     */
    public PyBuiltinMethodData(String name, String defaultVals, MethodHandle mh, String doc, boolean isStatic, boolean isWide) {
        this.name = name;
        this.defaultVals = defaultVals;
        this.target = mh;
        this.doc = doc;
        this.isStatic = isStatic;
        this.isWide = isWide;
        getDefaults();
    }

    public String getName() {
        return name;
    }

    public int getMaxargs() {
        return maxargs;
    }

    public void setMaxargs(int maxArgs) {
        this.maxargs = maxArgs;
    }

    public int getMinargs() {
        return minargs;
    }

    public void setMinargs(int minArgs) {
        this.minargs = minArgs;
    }

    public static boolean check(int nargs, int minargs, int maxargs) {
        if (nargs < minargs) {
            return false;
        }
        if (maxargs != -1 && nargs > maxargs) {
            return false;
        }
        return true;
    }

    public PyObject invoke(PyObject[] args, String[] keywords) {
        try {
            if (isWide) {
                return (PyObject) target.invokeExact(args, keywords);
            } else {
                if(keywords.length != 0) {
                    throw unexpectedCall(args.length, true);
                }
                return invoke(args);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    public PyObject invoke(PyObject arg) {
        try {
            int i = 0, j = 0;
            MethodType type = target.type();
            int paramCount = type.parameterCount();
            if (paramCount > 1) {
                if (paramCount == 2) {
                    return wrap(target.invokeExact(unwrap(arg, type.parameterType(i)), defaults[defaults.length - 1]));
                } else {
                    return wrap(target.invokeExact(unwrap(arg, type.parameterType(i)), defaults[defaults.length - 1], defaults[defaults.length - 2]));
                }
            } else {
                return wrap(target.invokeExact(unwrap(arg, type.parameterType(i))));
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    public PyObject invoke(PyObject... args) {
        try {
            if (isWide) {
                return wrap(target.invokeExact(args, Py.NoKeywords));
            }
            MethodType type = target.type();
            int paramCount = type.parameterCount();
            Object[] callArgs = new Object[paramCount];
            if (defaults != null) {
                System.arraycopy(defaults, 0, callArgs, paramCount - defaults.length, defaults.length);
            }
            for (int i = 0; i < args.length; i++) {
                callArgs[i] = unwrap(args[i], type.parameterType(i));
            }
            return wrap(target.asSpreader(Object[].class, paramCount).invoke(callArgs));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    private static Object unwrap(PyObject pyObj, Class<?> argType) {
        if (argType == int.class) {
            return pyObj.__int__();
        } else if (argType == boolean.class) {
            return pyObj.__bool__();
        } else if (argType == String.class) {
            return Py.getString(pyObj);
        } else if (argType == double.class) {
            return pyObj.asDouble();
        } else if (argType == long.class) {
            return pyObj.asLong();
        }
        return pyObj;
    }

    private PyObject wrap(Object ret) {
        Class<?> returnType = target.type().returnType();
        if (returnType == int.class) {
            return new PyLong((int) ret);
        } else if (returnType == long.class) {
            return new PyLong((long) ret);
        } else if (returnType == String.class) {
            return new PyUnicode((String) ret);
        } else if (returnType == double.class) {
            return new PyFloat((double) ret);
        } else if (returnType == float.class) {
            return new PyFloat((float) ret);
        } else if (returnType == boolean.class) {
            return new PyBoolean((boolean) ret);
        } else if (returnType == void.class) {
            return Py.None;
        }
        return (PyObject) ret;
    }

    private void getDefaults() {
        if ("".equals(defaultVals)) {
            defaults = new Object[0];
            return;
        }
        MethodType type = target.type();
        Class<?>[] paramTypes = type.parameterArray();
        String[] values = defaultVals.split(",");
        int startIndex = type.parameterCount() - values.length;
        defaults = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            defaults[i] = getDefaultValue(values[i], paramTypes[startIndex + i]);
        }
    }

    private static Object getDefaultValue(String def, Class<?> arg) {
        if (def.equals("null")) {
            return Py.None;
        } else if (arg == int.class) {
            return Integer.valueOf(def);
        } else if (arg == long.class) {
            return Long.valueOf(def);
        } else if (arg == String.class) {
            return def;
        } else if (arg == double.class) {
            return Double.valueOf(def);
        } else if (arg == float.class) {
            return Float.valueOf(def);
        } else if (arg == PyUnicode.class || arg == PyObject.class) {
            return new PyUnicode(def);
        } else if (arg == boolean.class) {
            return Boolean.valueOf(def);
        }
        return def;
    }


    public static PyException unexpectedCall(int nargs, boolean keywords, String name,
                                             int minargs, int maxargs) {
        if (keywords) {
            return Py.TypeError(name + "() takes no keyword arguments");
        }

        String argsblurb;
        if (minargs == maxargs) {
            if (minargs == 0) {
                argsblurb = "no arguments";
            } else if (minargs == 1) {
                argsblurb = "exactly one argument";
            } else {
                argsblurb = minargs + " arguments";
            }
        } else if (maxargs == -1) {
            return Py.TypeError(String.format("%s() requires at least %d arguments (%d) given",
                    name, minargs, nargs));
        } else if (minargs <= 0) {
            argsblurb = "at most " + maxargs + " arguments";
        } else {
            argsblurb = minargs + "-" + maxargs + " arguments";
        }
        return Py.TypeError(String.format("%s() takes %s (%d given)", name, argsblurb, nargs));
    }

    public PyException unexpectedCall(int nargs, boolean keywords) {
        return unexpectedCall(nargs, keywords, name, minargs, maxargs);
    }
}
