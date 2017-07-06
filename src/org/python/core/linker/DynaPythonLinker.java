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
                    if (((PyBuiltinMethod) self).isStatic) {
                        String funcname = ((PyBuiltinCallable) self).info.getName();
                        Class<?> klazz = ((PyBuiltinMethod) self).klazz;
                        String descriptor = ((PyBuiltinMethod) self).methodDescriptor;
                        MethodType methodType = MethodType.fromMethodDescriptorString(descriptor, null);
                        mh = LOOKUP.findStatic(klazz, funcname, methodType);
                        if (methodType.parameterCount() > 0) {
                            int i = 0;
                            String[] defaults = ((PyBuiltinMethod) self).defaultVals.split(",");
                            for (Class<?> paramType : methodType.parameterArray()) {
                                mh = MethodHandles.insertArguments(mh, i, getDefaultValue(defaults[i++], paramType));
                            }
                        }
                        /** Drop self for static method */
                        mh = MethodHandles.dropArguments(mh, 0, PyObject.class);
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
                        }
//                    } else {
//                        PyObject realSelf = ((PyBuiltinCallable) self).getSelf();
//                        mh = LOOKUP.findVirtual(realSelf.getClass(), name, MethodType.methodType(PyObject.class));
                        break;
                    }
                }
                mh = LOOKUP.findVirtual(self.getClass(), "__call__", MethodType.methodType(PyObject.class));
                break;
            default:
                mh = LOOKUP.findVirtual(self.getClass(), "__call__", MethodType.methodType(PyObject.class, PyObject[].class, String[].class));
        }

//        return new GuardedInvocation(mh);
        return new GuardedInvocation(mh, null, new SwitchPoint[0], ClassCastException.class);
    }

    private Object getDefaultValue(String def, Class<?> arg) {
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
        }
        return def;
    }
}
