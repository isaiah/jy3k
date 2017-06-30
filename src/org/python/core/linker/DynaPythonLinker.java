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
import org.python.core.PyObject;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * The dynamic linker implementation for Python objects
 */
public class DynaPythonLinker implements GuardingDynamicLinker {
    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) throws Exception {
        final Object self = linkRequest.getReceiver();
        final CallSiteDescriptor desc = linkRequest.getCallSiteDescriptor();
        String name = (String) NamedOperation.getName(desc.getOperation());
        StandardOperation baseOperation = (StandardOperation) NamespaceOperation.getBaseOperation(NamedOperation.getBaseOperation(desc.getOperation()));
        StandardNamespace namespace = StandardNamespace.findFirst(desc.getOperation());

        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        MethodHandle mh = null;
        switch (baseOperation) {
            case GET:
                if (namespace == StandardNamespace.PROPERTY) {
                    mh = lookup.findVirtual(self.getClass(), "__getattr__",
                            MethodType.methodType(PyObject.class, String.class));
                    mh = MethodHandles.insertArguments(mh, 1, name);
                } else if (namespace == StandardNamespace.ELEMENT) {
                    mh = lookup.findVirtual(self.getClass(), "__getitem__",
                            MethodType.methodType(PyObject.class, PyObject.class));
                }
                break;
            case SET:
                if (namespace == StandardNamespace.PROPERTY) {
                    mh = lookup.findVirtual(self.getClass(), "__setattr__",
                            MethodType.methodType(void.class, String.class, PyObject.class));
                    mh = MethodHandles.insertArguments(mh, 1, name);
                } else if (namespace == StandardNamespace.ELEMENT) {
                     mh = lookup.findVirtual(self.getClass(), "__setitem__",
                            MethodType.methodType(void.class, PyObject.class, PyObject.class));
                }
                break;
            case CALL:
                break;
            default:
                mh = lookup.findVirtual(self.getClass(), "__call__", MethodType.methodType(PyObject.class, PyObject[].class, String[].class));
        }

        return new GuardedInvocation(mh);
    }
}
