package org.python.core.linker;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.NamedOperation;
import jdk.dynalink.NamespaceOperation;
import jdk.dynalink.StandardNamespace;
import jdk.dynalink.StandardOperation;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import org.python.core.Py;
import org.python.core.PyBoolean;
import org.python.core.PyBuiltinCallable;
import org.python.core.PyBuiltinMethod;
import org.python.core.PyFloat;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyUnicode;
import org.python.internal.lookup.MethodHandleFactory;
import org.python.internal.lookup.MethodHandleFunctionality;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SwitchPoint;

/**
 * The dynamic linker implementation for Python objects
 */
public class DynaPythonLinker implements GuardingDynamicLinker {
    static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();
    static final MethodHandleFunctionality MH = MethodHandleFactory.getFunctionality();
    static final MethodHandle W_INTEGER = MH.findStatic(LOOKUP, Py.class, "newInteger", MethodType.methodType(PyLong.class, int.class));
    static final MethodHandle W_LONG = MH.findStatic(LOOKUP, Py.class, "newLong", MethodType.methodType(PyLong.class, long.class));
    static final MethodHandle W_UNICODE = MH.findStatic(LOOKUP, Py.class, "newUnicode", MethodType.methodType(PyUnicode.class, String.class));
    static final MethodHandle W_DOUBLE = MH.findStatic(LOOKUP, Py.class, "newFloat", MethodType.methodType(PyFloat.class, double.class));
    static final MethodHandle W_FLOAT = MH.findStatic(LOOKUP, Py.class, "newFloat", MethodType.methodType(PyFloat.class, float.class));
    static final MethodHandle W_BOOLEAN = MH.findStatic(LOOKUP, Py.class, "newBoolean", MethodType.methodType(PyBoolean.class, boolean.class));
    static final MethodHandle W_VOID = MethodHandles.constant(PyObject.class, Py.None);

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
                if (self instanceof PyBuiltinMethod) {
                    String funcname = ((PyBuiltinMethod) self).methodName;
                    Class<?> klazz = ((PyBuiltinMethod) self).klazz;
                    String descriptor = ((PyBuiltinMethod) self).methodDescriptor;
                    MethodType methodType = MethodType.fromMethodDescriptorString(descriptor, null);
                    Class<?> returnType = methodType.returnType();
                    if (((PyBuiltinMethod) self).isStatic) {
                        mh = LOOKUP.findStatic(klazz, funcname, methodType);
                        if (methodType.parameterCount() > 0) {
                            int i = 0;
                            String[] defaults = ((PyBuiltinMethod) self).defaultVals.split(",");
                            for (Class<?> paramType : methodType.parameterArray()) {
                                mh = MethodHandles.insertArguments(mh, i, getDefaultValue(defaults[i++], paramType));
                            }
                        }
                        /** Drop receiver for static method */
                        mh = MethodHandles.dropArguments(mh, 0, PyObject.class);
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
                        }
                    } else {
                        PyObject realSelf = ((PyBuiltinCallable) self).getSelf();
                        mh = LOOKUP.findVirtual(klazz, funcname, methodType);
                        if (methodType.parameterCount() > 0) {
                            String defaultVals = ((PyBuiltinMethod) self).defaultVals;
                            if (defaultVals.equals("")) {
                                if (descriptor.equals("([Lorg/python/core/PyObject;[Ljava/lang/String;)V")) {
                                    mh = MethodHandles.insertArguments(mh, 1, Py.EmptyObjects, Py.NoKeywords);
                                }
                            } else {
                                int i = 1;
                                String[] defaults = defaultVals.split(",");
                                for (Class<?> paramType : methodType.parameterArray()) {
                                    mh = MethodHandles.insertArguments(mh, i, getDefaultValue(defaults[i++], paramType));
                                }
                            }
                        }
                        mh = MethodHandles.insertArguments(mh, 0, realSelf);
                        mh = MethodHandles.dropArguments(mh, 0, PyObject.class);
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
                    }
                    break;
                }
                mh = LOOKUP.findVirtual(self.getClass(), "__call__", MethodType.methodType(PyObject.class));
                break;
            default:
                mh = LOOKUP.findVirtual(self.getClass(), "__call__", MethodType.methodType(PyObject.class, PyObject[].class, String[].class));
        }

//        return new GuardedInvocation(mh);
        return new GuardedInvocation(mh, null, new SwitchPoint[0], ClassCastException.class);
    }

    private static Object getDefaultValue(String def, Class<?> arg) {
        if (def == "null") {
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
        }
        return def;
    }
}
