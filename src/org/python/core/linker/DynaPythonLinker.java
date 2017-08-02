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
import org.python.core.PyMethod;
import org.python.core.PyNewWrapper;
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
                if (self instanceof PyBuiltinMethod && !(self instanceof PyNewWrapper)) {
                    return ((PyBuiltinMethod) self).findCallMethod(desc, linkRequest);
                } else if (self instanceof PyFunction) {
                    return ((PyFunction) self).findCallMethod(desc, linkRequest);
                } else if (self instanceof PyMethod) {
                    return ((PyMethod) self).findCallMethod(desc, linkRequest);
                }
                /** PyBuiltinFuction from builtins module */
                mh = MH.findVirtual(LOOKUP, self.getClass(), "__call__", desc.getMethodType().dropParameterTypes(0, 1));
                break;
            default:
                mh = MH.findVirtual(LOOKUP, self.getClass(), "__call__", MethodType.methodType(PyObject.class, PyObject[].class, String[].class));
        }

        return new GuardedInvocation(mh, null, new SwitchPoint[0], ClassCastException.class);
    }

    @Override
    public boolean canLinkType(Class<?> type) {
        return PyBuiltinCallable.class.isAssignableFrom(type);
    }
}
