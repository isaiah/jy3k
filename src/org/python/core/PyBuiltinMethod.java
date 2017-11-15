package org.python.core;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import org.python.expose.ExposeAsSuperclass;
import org.python.internal.lookup.MethodHandleFactory;
import org.python.internal.lookup.MethodHandleFunctionality;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SwitchPoint;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * PyBuiltinMethod is native implemented methods
 * i.e. str.split
 */
public class PyBuiltinMethod extends PyBuiltinCallable implements ExposeAsSuperclass, Cloneable, Traverseproc {
    static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();
    static final MethodHandleFunctionality MH = MethodHandleFactory.getFunctionality();
    static final MethodHandle W_INTEGER = MH.findStatic(LOOKUP, Py.class, "newInteger", MethodType.methodType(PyLong.class, int.class));
    static final MethodHandle W_LONG = MH.findStatic(LOOKUP, Py.class, "newLong", MethodType.methodType(PyLong.class, long.class));
    static final MethodHandle W_UNICODE = MH.findStatic(LOOKUP, Py.class, "newUnicode", MethodType.methodType(PyUnicode.class, String.class));
    static final MethodHandle W_DOUBLE = MH.findStatic(LOOKUP, Py.class, "newFloat", MethodType.methodType(PyFloat.class, double.class));
    static final MethodHandle W_FLOAT = MH.findStatic(LOOKUP, Py.class, "newFloat", MethodType.methodType(PyFloat.class, float.class));
    static final MethodHandle W_BOOLEAN = MH.findStatic(LOOKUP, Py.class, "newBoolean", MethodType.methodType(PyBoolean.class, boolean.class));
    static final MethodHandle W_VOID = MethodHandles.constant(PyObject.class, Py.None);

    static final MethodHandle U_INTEGER = MH.findVirtual(LOOKUP, PyObject.class, "asInt", MethodType.methodType(int.class));
    static final MethodHandle U_BOOLEAN = MH.findVirtual(LOOKUP, PyObject.class, "__bool__", MethodType.methodType(boolean.class));
    static final MethodHandle U_STRING = MH.findStatic(LOOKUP, Py.class, "getString", MethodType.methodType(String.class, PyObject.class));
    static final MethodHandle U_DOUBLE = MH.findVirtual(LOOKUP, PyObject.class, "asDouble", MethodType.methodType(double.class));
    static final MethodHandle U_LONG = MH.findVirtual(LOOKUP, PyObject.class, "asLong", MethodType.methodType(long.class));

    static final MethodHandle GET_REAL_SELF = findOwnMH("getRealSelf", PyObject.class, PyObject.class);
    static final MethodHandle IS_BUILTIN_METHOD_MH = findOwnMH("isBuiltinMethodMH", boolean.class, Object.class);
    static final MethodHandle VARARG_LEN = findOwnMH("varargLen", boolean.class, PyObject.class, ThreadState.class,
            PyObject[].class, String[].class, int.class, PyObject.class);

    protected PyObject self;

    public PyBuiltinMethod(PyObject self, PyBuiltinMethodData info) {
        super(info);
        this.self = self;
    }

    public PyBuiltinMethod(String name, String defaultVals, MethodHandle mh, String doc, boolean isStatic, boolean isWide) {
        super(new PyBuiltinMethodData(name, defaultVals, mh, doc, isStatic, isWide));
    }

    @Override
    public PyObject _doget(PyObject container) {
        return bind(container);
    }

    public PyBuiltinMethod bind(PyObject bindTo) {
        return new PyBuiltinMethod(bindTo, info);
    }

    @Override
    public PyObject getSelf(){
        return self;
    }

    public PyMethodDescr makeDescriptor(PyType t) {
        return new PyMethodDescr(t, this);
    }

    @Override
    public int hashCode() {
        int hashCode = self == null ? 0 : self.hashCode();
        return hashCode ^ getClass().hashCode();
    }

    @SuppressWarnings("unused")
    private static boolean varargLen(PyObject obj, ThreadState ts, PyObject[] args, String[] keywords, int len, PyObject self) {
        return obj == self && args.length == len;
    }

    @SuppressWarnings("unused")
    private static boolean isBuiltinMethodMH(final Object self) {
        return self instanceof PyBuiltinMethod;
    }

    public GuardedInvocation findCallMethod(final CallSiteDescriptor desc, LinkRequest request) {
        MethodHandle mh = info.target;
        MethodType argType = desc.getMethodType();
        int argCount = argType.parameterCount() - 2;
        MethodType methodType = info.target.type();
        int argOffset = 0;
        Class<?> selfType = null;
        if (this instanceof PyBuiltinClassMethod || !info.isStatic) {
            selfType = methodType.parameterType(0);
            argOffset = 1;
        }
        boolean needThreadState  = methodType.parameterCount() > argOffset && methodType.parameterType(argOffset) == ThreadState.class;
        if (needThreadState) {
            argOffset++;
        }
        int paramCount = methodType.parameterCount() - argOffset;
        Class<?>[] paramArray = methodType.parameterArray();
        int defaultLength = info.defaults.length;
        int missingArg = paramCount - argCount;
        int startIndex = defaultLength - missingArg;
        MethodHandle guard = IS_BUILTIN_METHOD_MH;
        // wide call
        if (defaultLength == 0 && info.isWide) {
            if (argCount == 0) {
                mh = MethodHandles.insertArguments(mh, argOffset, Py.EmptyObjects, Py.NoKeywords);
            } else if (!BaseCode.isWideCall(argType)) {
                mh = MethodHandles.insertArguments(mh, 1 + argOffset, (Object) Py.NoKeywords);
                mh = mh.asCollector(argOffset, PyObject[].class, argCount);
            }
        } else if(BaseCode.isWideCall(argType)) {
            /** it's a wide call, but not a wide method */
            String[] keywords = (String[]) request.getArguments()[3];
            // if there is no keywords, means it's a stararg call, spread the args
            if (keywords.length == 0) {
                PyObject[] args = (PyObject[]) request.getArguments()[2];
                argCount = args.length;
                if (paramCount < argCount) {
                    if (paramCount == 0) {
                        throw Py.TypeError(String.format("%s() takes no arguments, (%d given)", info.getName(), args.length));
                    } else {
                        throw Py.TypeError(String.format("%s() takes at most %d arguments, (%d given)",
                                info.getName(), paramCount - defaultLength, args.length));
                    }
                }

                for (int i = 0; i < argCount; i++) {
                    mh = convert(mh, argOffset + i, paramArray[i]);
                }

                missingArg = paramCount - argCount;
                startIndex = defaultLength - missingArg;
                if (startIndex < 0) {
                    throw Py.TypeError(String.format("%s() takes exactly %d arguments (%d given)", info.getName(), methodType.parameterCount(), argCount));
                }
                if (missingArg > 0) {
                    for (int i = argCount; i < paramCount; i++) {
                        mh = MethodHandles.insertArguments(mh, argCount + argOffset, info.defaults[defaultLength + i - paramCount]);
                    }
                }
                mh = mh.asSpreader(argOffset, PyObject[].class, argCount);

                mh = MethodHandles.dropArguments(mh, argOffset + 1, String[].class);
                guard = MethodHandles.insertArguments(VARARG_LEN, 4, argCount, self);
            } else {
                throw Py.TypeError(String.format("%s() takes no keyword arguments", info.getName()));
            }
        } else {
            if (missingArg > 0) {
                if (startIndex < 0) {
                    throw Py.TypeError(String.format("%s() takes exactly %d arguments (%d given)", info.getName(), methodType.parameterCount(), argCount));
                }

                for (int i = argCount; i < paramCount; i++) {
                    mh = MethodHandles.insertArguments(mh, argCount + argOffset, info.defaults[defaultLength + i - paramCount]);
                }
            }

            if (argCount > paramCount) {
                throw Py.TypeError(String.format("%s() takes at most %d argument%s (%d given)", info.getName(), paramCount, paramCount > 1 ? "s" : "", argCount));
            }
            for (int i = argOffset; i < argCount + argOffset; i++) {
                mh = convert(mh, i, paramArray[i]);
            }
        }

        if (selfType != null) {
            MethodHandle filter = MethodHandles.explicitCastArguments(GET_REAL_SELF, MethodType.methodType(selfType, PyObject.class));
            mh = MethodHandles.filterArguments(mh, 0, filter);
        } else {
            /** Drop receiver for static method */
            mh = MethodHandles.dropArguments(mh, 0, PyObject.class);
        }

        if (!needThreadState) {
            mh = MethodHandles.dropArguments(mh, 1, ThreadState.class);
        }
        mh = asTypesafeReturn(mh, methodType);
        return new GuardedInvocation(mh, guard, new SwitchPoint[0], ClassCastException.class);
    }

    private MethodHandle convert(MethodHandle mh, int idx, Class<?> argType) {
        MethodHandle filter = null;
        if (argType == int.class) {
            filter = U_INTEGER;
        } else if (argType == boolean.class) {
            filter = U_BOOLEAN;
        } else if (argType == String.class) {
            filter = U_STRING;
        } else if (argType == double.class) {
            filter = U_DOUBLE;
        } else if (argType == long.class) {
            filter = U_LONG;
        }
        if (filter != null) {
            return MethodHandles.filterArguments(mh, idx, filter);
        }
        return mh;
    }

    private MethodHandle asTypesafeReturn(MethodHandle mh, MethodType methodType) {
        Class<?> returnType = methodType.returnType();
        if (returnType == int.class) {
            mh = MethodHandles.filterReturnValue(mh, W_INTEGER);
        } else if (returnType == long.class) {
            mh = MethodHandles.filterReturnValue(mh, W_LONG);
        } else if (returnType == String.class) {
            mh = MethodHandles.filterReturnValue(mh, W_UNICODE);
        } else if (returnType == double.class) {
            mh = MethodHandles.filterReturnValue(mh, W_DOUBLE);
        } else if (returnType == float.class) {
            mh = MethodHandles.filterReturnValue(mh, W_FLOAT);
        } else if (returnType == boolean.class) {
            mh = MethodHandles.filterReturnValue(mh, W_BOOLEAN);
        } else if (returnType == void.class) {
            mh = MethodHandles.filterReturnValue(mh, W_VOID);
        }
        return mh;
    }

    private static MethodHandle findOwnMH(final String name, final Class<?> rtype, final Class<?>... types) {
        return MH.findStatic(MethodHandles.lookup(), PyBuiltinMethod.class, name, MethodType.methodType(rtype, types));
    }

    @SuppressWarnings("unused")
    private static PyObject getRealSelf(PyObject self) {
        if (self instanceof PyBuiltinCallable) {
            return ((PyBuiltinCallable) self).getSelf();
        }
        return self;
    }

    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        return self != null ? visit.visit(self, arg) : 0;
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        return ob != null && ob == self;
    }

    public PyObject invoke(PyObject[] args, String[] keywords) {
        if (!isFunction() && self != null) {
            if (self instanceof PyType) {
                return info.invoke((PyType) self, args, keywords);
            }
            return info.invoke(self, args, keywords);
        }
        return info.invoke(args, keywords);
    }

    public PyObject invoke() {
        if (!isFunction() && self != null) {
            return info.invoke(self);
        }
        return info.invoke();
    }

    public PyObject invoke(PyObject arg) {
        if (!isFunction() && self != null) {
            return info.invoke(self, arg);
        }
        return info.invoke(arg);
    }

    public PyObject invoke(PyObject arg1, PyObject arg2) {
        if (!isFunction() && self != null) {
            return info.invoke(self, arg1, arg2);
        }
        return info.invoke(arg1, arg2);
    }

    public PyObject invoke(PyObject arg1, PyObject arg2, PyObject arg3) {
        if (!isFunction() && self != null) {
            return info.invoke(self, arg1, arg2, arg3);
        }
        return info.invoke(arg1, arg2, arg3);
    }

    public PyObject invoke(PyObject arg1, PyObject arg2, PyObject arg3, PyObject arg4) {
        if (!isFunction() && self != null) {
            return info.invoke(self, arg1, arg2, arg3, arg4);
        }
        return info.invoke(arg1, arg2, arg3, arg4);
    }

    public PyObject __call__() {
        return invoke();
    }

    public PyObject __call__(PyObject arg0) {
        return invoke(arg0);
    }

    public PyObject __call__(PyObject arg0, PyObject arg1) {
        return invoke(arg0, arg1);
    }

    public PyObject __call__(PyObject arg0, PyObject arg1, PyObject arg2) {
        return invoke(arg0, arg1, arg2);
    }

    public PyObject __call__(PyObject arg0, PyObject arg1, PyObject arg2, PyObject arg3) {
        return invoke(arg0, arg1, arg2, arg3);
    }

    public PyObject __call__(PyObject[] args, String[] keywords) {
        return invoke(args, keywords);
    }

    private boolean isFunction() {
        return info.isStatic && !(this instanceof PyBuiltinClassMethod);
    }
}
