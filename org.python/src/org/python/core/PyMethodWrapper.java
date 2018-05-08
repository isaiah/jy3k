package org.python.core;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;

import java.util.Objects;

@ExposedType(name = "method-wrapper")
public class PyMethodWrapper extends PyObject implements DynLinkable {
    public static final PyType TYPE = PyType.fromClass(PyMethodWrapper.class);

    @ExposedGet(name = "__self__")
    final private PyObject self;

    private PyDescriptor descr;

    public PyMethodWrapper(PyDescriptor descr, PyObject self) {
        super(TYPE);
        this.descr = descr;
        this.self = self;
    }

    @ExposedSlot(SlotFunc.CALL)
    public static PyObject call(PyObject obj, PyObject[] args, String[] keywords) {
        PyMethodWrapper meth = (PyMethodWrapper) obj;
        PyObject[] newArgs = new PyObject[args.length + 1];
        newArgs[0] = meth.self;
        System.arraycopy(args, 0, newArgs, 1, args.length);
        // FIXME broken
        return meth.descr.__call__(newArgs, keywords);
    }

    @Override
    public String toString() {
        return String.format("<method-wrapper '%s' of %s object at 0x%X>", descr.name, self.getType().fastGetName(), Objects.hash(self));
    }

    @Override
    public GuardedInvocation findCallMethod(CallSiteDescriptor desc, LinkRequest linkRequest) {
        return descr.meth.findCallMethod(desc, linkRequest);
    }

    public PyDescriptor getDescr() {
        return descr;
    }
}
