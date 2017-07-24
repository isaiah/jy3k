package org.python.core.linker;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.NamedOperation;
import jdk.dynalink.NamespaceOperation;
import jdk.dynalink.StandardNamespace;
import jdk.dynalink.StandardOperation;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.TypeBasedGuardingDynamicLinker;
import org.python.core.CodeFlag;
import org.python.core.Py;
import org.python.core.BaseCode;
import org.python.core.PyBoolean;
import org.python.core.PyBuiltinCallable;
import org.python.core.PyBuiltinMethod;
import org.python.core.PyFloat;
import org.python.core.PyFrame;
import org.python.core.PyFunction;
import org.python.core.PyFunctionTable;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyTableCode;
import org.python.core.PyUnicode;
import org.python.core.ThreadState;
import org.python.core.generator.PyAsyncGenerator;
import org.python.core.generator.PyCoroutine;
import org.python.core.generator.PyGenerator;
import org.python.internal.lookup.MethodHandleFactory;
import org.python.internal.lookup.MethodHandleFunctionality;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SwitchPoint;
import java.lang.reflect.Method;

/**
 * The dynamic linker implementation for Python objects
 */
public class DynaPythonLinker implements TypeBasedGuardingDynamicLinker {
    static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();
    static final MethodHandleFunctionality MH = MethodHandleFactory.getFunctionality();
    static final MethodHandle CREATE_FRAME = MH.findStatic(LOOKUP, BaseCode.class, "createFrame",
            MethodType.methodType(PyFrame.class, PyObject.class, ThreadState.class));

    static final MethodHandle W_INTEGER = MH.findStatic(LOOKUP, Py.class, "newInteger", MethodType.methodType(PyLong.class, int.class));
    static final MethodHandle W_LONG = MH.findStatic(LOOKUP, Py.class, "newLong", MethodType.methodType(PyLong.class, long.class));
    static final MethodHandle W_UNICODE = MH.findStatic(LOOKUP, Py.class, "newUnicode", MethodType.methodType(PyUnicode.class, String.class));
    static final MethodHandle W_DOUBLE = MH.findStatic(LOOKUP, Py.class, "newFloat", MethodType.methodType(PyFloat.class, double.class));
    static final MethodHandle W_FLOAT = MH.findStatic(LOOKUP, Py.class, "newFloat", MethodType.methodType(PyFloat.class, float.class));
    static final MethodHandle W_BOOLEAN = MH.findStatic(LOOKUP, Py.class, "newBoolean", MethodType.methodType(PyBoolean.class, boolean.class));
    static final MethodHandle W_VOID = MethodHandles.constant(PyObject.class, Py.None);
    static final MethodHandle GET_REAL_SELF = findOwnMH("getRealSelf", PyObject.class, PyObject.class);
    static final MethodHandle GET_FUNC_TBL = findOwnMH("getFuncTable", PyFunctionTable.class, PyObject.class);
    static final MethodHandle GET_CLOSURE = findOwnMH("getClosure", PyObject.class, PyObject.class);
    static final MethodHandle RESTORE_FRAME = findOwnMH("restoreFrame", PyObject.class, Throwable.class,
            PyObject.class, PyObject.class, ThreadState.class);

    static final MethodType GEN_SIG = MethodType.methodType(void.class, PyFrame.class, PyObject.class);
    static final MethodHandle NEW_GENERATOR = MH.findConstructor(LOOKUP, PyGenerator.class, GEN_SIG);
    static final MethodHandle NEW_COROUTINE = MH.findConstructor(LOOKUP, PyCoroutine.class, GEN_SIG);
    static final MethodHandle NEW_ASYNC_GENERATOR = MH.findConstructor(LOOKUP, PyAsyncGenerator.class, GEN_SIG);

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) throws Exception {
        final Object self = linkRequest.getReceiver();
        final CallSiteDescriptor desc = linkRequest.getCallSiteDescriptor();
        String name = (String) NamedOperation.getName(desc.getOperation());
        StandardOperation baseOperation = (StandardOperation) NamespaceOperation.getBaseOperation(NamedOperation.getBaseOperation(desc.getOperation()));
        StandardNamespace namespace = StandardNamespace.findFirst(desc.getOperation());
        MethodHandle mh = null;
        switch (baseOperation) {
            case GET:
                if (namespace == StandardNamespace.PROPERTY) {
                    Class<?> receiverClass = self.getClass();
                    mh = LOOKUP.findVirtual(receiverClass, "__getattr__",
                            MethodType.methodType(PyObject.class, String.class));
                    mh = MethodHandles.insertArguments(mh, 1, name);
                } else if (namespace == StandardNamespace.ELEMENT) {
                    mh = LOOKUP.findVirtual(self.getClass(), "__getitem__",
                            MethodType.methodType(PyObject.class, PyObject.class));
                }
                break;
            case SET:
                if (namespace == StandardNamespace.PROPERTY) {
                    mh = LOOKUP.findVirtual(self.getClass(), "__setattr__",
                            MethodType.methodType(void.class, String.class, PyObject.class));
                    mh = MethodHandles.insertArguments(mh, 1, name);
                } else if (namespace == StandardNamespace.ELEMENT) {
                     mh = LOOKUP.findVirtual(self.getClass(), "__setitem__",
                            MethodType.methodType(void.class, PyObject.class, PyObject.class));
                }
                break;
            case CALL:
                int argCount = linkRequest.getCallSiteDescriptor().getMethodType().parameterCount() - 2;
                if (self instanceof PyBuiltinMethod) {
                    String funcname = ((PyBuiltinMethod) self).methodName;
                    Class<?> klazz = ((PyBuiltinMethod) self).klazz;
                    String descriptor = ((PyBuiltinMethod) self).methodDescriptor;
                    MethodType methodType = MethodType.fromMethodDescriptorString(descriptor, null);
                    String defaultVals = ((PyBuiltinMethod) self).defaultVals;
                    String[] defaults = defaultVals.split(",");
                    Class<?>[] paramArray = methodType.parameterArray();
                    int defaultLength = defaults.length;
                    int missingArg = methodType.parameterCount() - argCount;
                    int startIndex = defaultLength - missingArg;
                    if (((PyBuiltinMethod) self).isStatic) {
                        mh = LOOKUP.findStatic(klazz, funcname, methodType);
                        if (missingArg > 0) {
                            for (int i = argCount; i < methodType.parameterCount(); i++) {
                                mh = MethodHandles.insertArguments(mh, argCount + 1, getDefaultValue(defaults[startIndex++], paramArray[i]));
                            }
                        }
                        /** Drop receiver for static method */
                        mh = MethodHandles.dropArguments(mh, 0, PyObject.class, ThreadState.class);
                        mh = asTypesafeReturn(mh, methodType);
                    } else {
                        mh = LOOKUP.findVirtual(klazz, funcname, methodType);
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
                        return new GuardedInvocation(mh, null,
                                new SwitchPoint[0], ClassCastException.class);
                    }
                } else if (self instanceof PyFunction) {
                    PyFunction func = (PyFunction) self;
                    PyTableCode code = (PyTableCode) func.__code__;
                    Class<?> klazz = code.funcs.getClass();
                    String funcName = code.funcname;

                    Class<?> genCls;
                    if (code.co_flags.isFlagSet(CodeFlag.CO_GENERATOR)) {
                        mh = NEW_GENERATOR;
                        genCls = PyGenerator.class;
                    } else if (code.co_flags.isFlagSet(CodeFlag.CO_COROUTINE)) {
                        mh = NEW_COROUTINE;
                        genCls = PyCoroutine.class;
                    } else if (code.co_flags.isFlagSet(CodeFlag.CO_ASYNC_GENERATOR)) {
                        mh = NEW_ASYNC_GENERATOR;
                        genCls = PyAsyncGenerator.class;
                    } else {
                        mh = LOOKUP.findVirtual(klazz, funcName, MethodType.methodType(PyObject.class, ThreadState.class, PyFrame.class));
                        mh = MethodHandles.dropArguments(mh, 3, PyObject.class, ThreadState.class);
                        mh = MethodHandles.foldArguments(mh, 2, CREATE_FRAME);
                        mh = MethodHandles.permuteArguments(mh,
                                MethodType.methodType(PyObject.class, klazz, PyObject.class, ThreadState.class), 0, 2, 1, 2);
//                        mh = mh.asCollector(PyObject[].class, argCount);
                        mh = MethodHandles.filterArguments(mh, 0, MethodHandles.explicitCastArguments(GET_FUNC_TBL, MethodType.methodType(klazz, PyObject.class)));
                        mh = MethodHandles.permuteArguments(mh, MethodType.methodType(PyObject.class, PyObject.class, ThreadState.class), 0, 0, 1);
                        mh = MethodHandles.tryFinally(mh, RESTORE_FRAME);
                        break;
                    }
                    mh = MethodHandles.filterArguments(mh, 1, GET_CLOSURE);
                    mh = MethodHandles.dropArguments(mh, 1, PyObject.class, ThreadState.class);
                    mh = MethodHandles.foldArguments(mh, 0, CREATE_FRAME);
                    mh = MethodHandles.permuteArguments(mh,
                            MethodType.methodType(genCls, PyObject.class, ThreadState.class), 0, 1, 0);
                } else {
                    mh = LOOKUP.findVirtual(self.getClass(), "__call__", desc.getMethodType().dropParameterTypes(0, 1));
                }
                break;
            default:
                mh = LOOKUP.findVirtual(self.getClass(), "__call__", MethodType.methodType(PyObject.class, PyObject[].class, String[].class));
        }

        return new GuardedInvocation(mh, null, new SwitchPoint[0], ClassCastException.class);
    }

    /**
     * Get the real java object that hosts the linked method handle from the function object
     *
     * it's func.__code__.funcs
     */
    private static PyFunctionTable getFuncTable(PyObject funcObj) {
        return ((PyTableCode) ((PyFunction) funcObj).__code__).funcs;
    }

    private static PyObject getClosure(PyObject funcObj) {
        return ((PyFunction) funcObj).__closure__;
    }

    private static PyObject restoreFrame(Throwable t, PyObject v, PyObject arg, ThreadState ts) {
        ts.frame = ts.frame.f_back;
        return v;
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

    @Override
    public boolean canLinkType(Class<?> type) {
        return PyBuiltinCallable.class.isAssignableFrom(type);
    }

    @SuppressWarnings("unused")
    private static PyObject getRealSelf(PyObject self) {
        if (self instanceof PyBuiltinCallable) {
            return ((PyBuiltinCallable) self).getSelf();
        }
        return self;
    }

    private static MethodHandle findOwnMH(final String name, final Class<?> rtype, final Class<?>... types) {
        return MH.findStatic(MethodHandles.lookup(), DynaPythonLinker.class, name, MethodType.methodType(rtype, types));
    }
}
