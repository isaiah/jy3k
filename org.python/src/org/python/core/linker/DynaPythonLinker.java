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
import org.python.core.Abstract;
import org.python.core.DynLinkable;
import org.python.core.Py;
import org.python.core.PyBuiltinCallable;
import org.python.core.PyObject;
import org.python.core.ThreadState;
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
    static final MethodHandle GETATTR = MH.findStatic(LOOKUP, Abstract.class, "_PyObject_GetAttrId",
                    MethodType.methodType(PyObject.class, PyObject.class, String.class));
    static final MethodHandle GETITEM = MH.findStatic(LOOKUP, Abstract.class, "PyObject_GetItem",
                            MethodType.methodType(PyObject.class, ThreadState.class, PyObject.class, PyObject.class));
    static final MethodHandle SETATTR = MH.findVirtual(LOOKUP, PyObject.class, "__setattr__",
            MethodType.methodType(void.class, String.class, PyObject.class));
    static final MethodHandle SETITEM = MH.findStatic(LOOKUP, Abstract.class, "PyObject_SetItem",
            MethodType.methodType(void.class, ThreadState.class, PyObject.class, PyObject.class, PyObject.class));

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
                    mh = MethodHandles.insertArguments(GETITEM, 0, Py.getThreadState());
                }
                guard = Guards.getIdentityGuard(self);
                break;
            case SET:
                if (namespace == StandardNamespace.PROPERTY) {
                    mh = SETATTR;
                    mh = MethodHandles.insertArguments(mh, 1, name);
                } else if (namespace == StandardNamespace.ELEMENT) {
                    mh = MethodHandles.insertArguments(SETITEM, 0, Py.getThreadState());
                }
                guard = Guards.getIdentityGuard(self);
                break;
            case CALL:
                if (self instanceof DynLinkable) {
                    return ((DynLinkable) self).findCallMethod(desc, linkRequest);
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
