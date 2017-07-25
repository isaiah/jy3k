package org.python.core;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import org.python.core.linker.DynaPythonLinker;
import org.python.expose.ExposeAsSuperclass;
import org.python.internal.lookup.MethodHandleFactory;
import org.python.internal.lookup.MethodHandleFunctionality;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SwitchPoint;

public abstract class PyBuiltinMethod extends PyBuiltinCallable implements ExposeAsSuperclass, Cloneable, Traverseproc {
    static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();
    static final MethodHandleFunctionality MH = MethodHandleFactory.getFunctionality();
    static final MethodHandle W_INTEGER = MH.findStatic(LOOKUP, Py.class, "newInteger", MethodType.methodType(PyLong.class, int.class));
    static final MethodHandle W_LONG = MH.findStatic(LOOKUP, Py.class, "newLong", MethodType.methodType(PyLong.class, long.class));
    static final MethodHandle W_UNICODE = MH.findStatic(LOOKUP, Py.class, "newUnicode", MethodType.methodType(PyUnicode.class, String.class));
    static final MethodHandle W_DOUBLE = MH.findStatic(LOOKUP, Py.class, "newFloat", MethodType.methodType(PyFloat.class, double.class));
    static final MethodHandle W_FLOAT = MH.findStatic(LOOKUP, Py.class, "newFloat", MethodType.methodType(PyFloat.class, float.class));
    static final MethodHandle W_BOOLEAN = MH.findStatic(LOOKUP, Py.class, "newBoolean", MethodType.methodType(PyBoolean.class, boolean.class));
    static final MethodHandle W_VOID = MethodHandles.constant(PyObject.class, Py.None);
    static final MethodHandle GET_REAL_SELF = findOwnMH("getRealSelf", PyObject.class, PyObject.class);
    static final MethodHandle IS_BUILTIN_METHOD_MH = findOwnMH("isBuiltinMethodMH", boolean.class, Object.class);

    protected PyObject self;
    public String methodName;
    public String defaultVals;

    protected PyBuiltinMethod(PyType type, PyObject self, Info info) {
        super(type, info);
        this.self = self;
    }

    protected PyBuiltinMethod(PyObject self, Info info) {
        super(info);
        this.self = self;
    }

    protected PyBuiltinMethod(String name) {
        this(null, new DefaultInfo(name));
    }

    @Override
    public PyBuiltinCallable bind(PyObject bindTo) {
        if(self == null) {
            PyBuiltinMethod bindable;
            try {
                bindable = (PyBuiltinMethod)clone();
            } catch(CloneNotSupportedException e) {
                throw new RuntimeException("Didn't expect PyBuiltinMethod to throw " +
                                           "CloneNotSupported since it implements Cloneable", e);
            }
            bindable.self = bindTo;
            return bindable;
        }
        return this;
    }

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

    public GuardedInvocation findCallMethod(final CallSiteDescriptor desc, LinkRequest request) {
        MethodHandle mh;
        Object receiver = request.getReceiver();
        int argCount = desc.getMethodType().parameterCount() - 2;
        String funcname = ((PyBuiltinMethod) receiver).methodName;
        Class<?> klazz = ((PyBuiltinMethod) receiver).klazz;
        String descriptor = ((PyBuiltinMethod) receiver).methodDescriptor;
        MethodType methodType = MethodType.fromMethodDescriptorString(descriptor, null);
        String defaultVals = ((PyBuiltinMethod) receiver).defaultVals;
        String[] defaults = defaultVals.split(",");
        Class<?>[] paramArray = methodType.parameterArray();
        int defaultLength = defaults.length;
        int missingArg = methodType.parameterCount() - argCount;
        int startIndex = defaultLength - missingArg;
        if (((PyBuiltinMethod) receiver).isStatic) {
            mh = MH.findStatic(LOOKUP, klazz, funcname, methodType);
            if (missingArg > 0) {
                for (int i = argCount; i < methodType.parameterCount(); i++) {
                    mh = MethodHandles.insertArguments(mh, argCount + 1, getDefaultValue(defaults[startIndex++], paramArray[i]));
                }
            }
            /** Drop receiver for static method */
            mh = MethodHandles.dropArguments(mh, 0, PyObject.class, ThreadState.class);
            mh = asTypesafeReturn(mh, methodType);
        } else {
            mh = MH.findVirtual(LOOKUP, klazz, funcname, methodType);
            if (methodType.parameterCount() > 0) {
                if (defaultVals.equals("")) {
                    // wide call
                    if (methodType.parameterCount() == 2
                            && methodType.parameterType(0) == PyObject[].class
                            && methodType.parameterType(1) == String[].class) {
                        mh = MethodHandles.insertArguments(mh, 1, Py.EmptyObjects, Py.NoKeywords);
                    }
                } else {
                    if (missingArg > 0) {
                        for (int i = argCount; i < methodType.parameterCount(); i++) {
                            mh = MethodHandles.insertArguments(mh, argCount + 1, getDefaultValue(defaults[startIndex++], paramArray[i]));
                        }
                    }
                }
            }
            MethodHandle filter = MethodHandles.explicitCastArguments(GET_REAL_SELF, MethodType.methodType(klazz, PyObject.class));
            mh = MethodHandles.filterArguments(mh, 0, filter);

            mh = asTypesafeReturn(mh, methodType);
            mh = MethodHandles.dropArguments(mh, 1, ThreadState.class);
        }
        return new GuardedInvocation(mh, IS_BUILTIN_METHOD_MH, new SwitchPoint[0], ClassCastException.class);
    }

    @SuppressWarnings("unused")
    private static boolean isBuiltinMethodMH(final Object self) {
        return self instanceof PyBuiltinMethod;
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
}
