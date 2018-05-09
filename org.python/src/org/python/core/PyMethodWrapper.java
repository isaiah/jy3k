package org.python.core;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.support.Guards;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;

import static org.python.core.PyMethodDescr.REMOVE_SELF;
import static org.python.core.PyMethodDescr.SELF_GETTER;

@ExposedType(name = "method-wrapper")
public class PyMethodWrapper extends PyObject implements DynLinkable {
    public static final PyType TYPE = PyType.fromClass(PyMethodWrapper.class);

    @ExposedGet(name = "__self__")
    protected PyObject self;

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
        PyBuiltinMethod meth = descr.meth;
        PyBuiltinMethodData info = meth.info;
        MethodHandle mh = info.target;
        MethodType argType = desc.getMethodType();
        // When a method descriptor is called, it means it definitely need a self
        int argOffset = 1;
        int argCount = argType.parameterCount() - 2;
        MethodType methodType = info.target.type();
        boolean needThreadState = methodType.parameterCount() > argOffset && methodType.parameterType(argOffset) == ThreadState.class;
        if (needThreadState) {
            argOffset++;
        }
        if (info.isWide) {
            if (BaseCode.isWideCall(argType)) {
                if (argType.parameterType(0) != PyObject.class) {
                    // if need a self, and self is in the vararg, take it out
                    mh = MethodHandles.filterArguments(mh, 0, SELF_GETTER);
                    mh = MethodHandles.filterArguments(mh, 1, REMOVE_SELF);
                    mh = MethodHandles.permuteArguments(mh, MethodType.methodType(methodType.returnType(), PyObject[].class, String[].class), 0, 0, 1);
                }
            } else {
                if (argCount == 0) {
                    mh = MethodHandles.insertArguments(mh, argOffset, Py.EmptyObjects, Py.NoKeywords);
                } else {
                    mh = MethodHandles.insertArguments(mh, 1 + argOffset, (Object) Py.NoKeywords);
                    mh = mh.asCollector(argOffset, PyObject[].class, argCount);
                }
            }
        }
        if (!needThreadState) {
            mh = MethodHandles.dropArguments(mh, 1, ThreadState.class);
        }
        /* replace the receiver with real self */
        mh = MethodHandles.insertArguments(mh, 0, self);
        mh = MethodHandles.dropArguments(mh, 0, PyObject.class);
        return new GuardedInvocation(mh, Guards.getIdentityGuard(linkRequest.getReceiver()));
    }

    public PyDescriptor getDescr() {
        return descr;
    }
}
