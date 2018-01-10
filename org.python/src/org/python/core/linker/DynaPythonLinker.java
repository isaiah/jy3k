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
import jdk.dynalink.linker.support.Guards;
import org.python.core.PyBuiltinCallable;
import org.python.core.PyBuiltinMethod;
import org.python.core.PyFunction;
import org.python.core.PyMethod;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.internal.lookup.MethodHandleFactory;
import org.python.internal.lookup.MethodHandleFunctionality;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SwitchPoint;

/**
 * The dynamic linker implementation for Python objects
 */
public class DynaPythonLinker implements TypeBasedGuardingDynamicLinker {
    static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    static final MethodHandleFunctionality MH = MethodHandleFactory.getFunctionality();
    static final MethodHandle GETATTR = MH.findVirtual(LOOKUP, PyObject.class, "__getattr__",
                    MethodType.methodType(PyObject.class, String.class));
    static final MethodHandle GETITEM = MH.findVirtual(LOOKUP, PyObject.class, "__getitem__",
                            MethodType.methodType(PyObject.class, PyObject.class));
    static final MethodHandle SETATTR = MH.findVirtual(LOOKUP, PyObject.class, "__setattr__",
            MethodType.methodType(void.class, String.class, PyObject.class));
    static final MethodHandle SETITEM = MH.findVirtual(LOOKUP, PyObject.class, "__setitem__",
            MethodType.methodType(void.class, PyObject.class, PyObject.class));

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) throws Exception {
        final Object self = linkRequest.getReceiver();
        final CallSiteDescriptor desc = linkRequest.getCallSiteDescriptor();
        String name = (String) NamedOperation.getName(desc.getOperation());
        StandardOperation baseOperation = (StandardOperation) NamespaceOperation.getBaseOperation(NamedOperation.getBaseOperation(desc.getOperation()));
        StandardNamespace namespace = StandardNamespace.findFirst(desc.getOperation());
        MethodHandle mh = null;
        MethodHandle guard = null;
        switch (baseOperation) {
            case GET:
                if (namespace == StandardNamespace.PROPERTY) {
                    mh = GETATTR;
                    mh = MethodHandles.insertArguments(mh, 1, name);
                } else if (namespace == StandardNamespace.ELEMENT) {
                    mh = GETITEM;
                }
                guard = Guards.getIdentityGuard(self);
                break;
            case SET:
                if (namespace == StandardNamespace.PROPERTY) {
                    mh = SETATTR;
                    mh = MethodHandles.insertArguments(mh, 1, name);
                } else if (namespace == StandardNamespace.ELEMENT) {
                     mh = SETITEM;
                }
                guard = Guards.getIdentityGuard(self);
                break;
            case CALL:
                if (self instanceof PyNewWrapper) {
                    return ((PyNewWrapper) self).findCallMethod(desc, linkRequest);
                } else if (self instanceof PyBuiltinMethod) {
                    return ((PyBuiltinMethod) self).findCallMethod(desc, linkRequest);
                } else if (self instanceof PyFunction) {
                    return ((PyFunction) self).findCallMethod(desc, linkRequest);
                } else if (self instanceof PyMethod) {
                    return ((PyMethod) self).findCallMethod(desc, linkRequest);
//                } else if (self instanceof PyType) {
//                    return ((PyType) self).findCallMethod(desc, linkRequest);
                }
                /** PyBuiltinFuction from builtins module */
                mh = MH.findVirtual(LOOKUP, self.getClass(), "__call__", desc.getMethodType().dropParameterTypes(0, 1));
                break;
            default:
                mh = MH.findVirtual(LOOKUP, self.getClass(), "__call__", MethodType.methodType(PyObject.class, PyObject[].class, String[].class));
        }

        return new GuardedInvocation(mh, guard, new SwitchPoint[0], ClassCastException.class);
    }

    @Override
    public boolean canLinkType(Class<?> type) {
        return PyBuiltinCallable.class.isAssignableFrom(type);
    }
}
