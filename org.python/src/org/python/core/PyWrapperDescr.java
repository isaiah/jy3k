package org.python.core;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.MethodHandleTransformer;
import jdk.dynalink.linker.support.Guards;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@ExposedType(name = "wrapper_descriptor")
public class PyWrapperDescr extends PyDescriptor implements DynLinkable {
    public static final PyType TYPE = PyType.fromClass(PyWrapperDescr.class);

    public PyWrapperDescr(PyType dtype, PyBuiltinMethod method) {
        this.name = method.info.getName();
        this.dtype = dtype;
        this.meth = method;
    }

    @Override
    @ExposedMethod(defaults = "null")
    public PyObject __get__(PyObject obj, PyObject type) {
        if (obj != Py.None && obj != null) {
            checkGetterType(obj.getType());
            return new PyMethodWrapper(this, obj);
        }
        return this;
    }

    @ExposedSlot(SlotFunc.CALL)
    public static PyObject call(PyObject obj, PyObject[] args, String[] keywords) {
        PyWrapperDescr self = (PyWrapperDescr) obj;
        return self.__call__(args, keywords);
    }

    @Override
    public String toString() {
        return String.format("<slot wrapper '%s' of '%s' objects>", name, dtype.fastGetName());
    }

    @Override
    public GuardedInvocation findCallMethod(CallSiteDescriptor desc, LinkRequest linkRequest) {
        PyBuiltinMethodData info = meth.info;
        MethodHandle mh = info.target;
        MethodType argType = desc.getMethodType();
        // When a method descriptor is called, it means it definitely need a self
        int argOffset = 1;
        int argCount = argType.parameterCount() - 3;
        MethodType methodType = info.target.type();
        boolean needThreadState = methodType.parameterCount() > argOffset && methodType.parameterType(argOffset) == ThreadState.class;
        Class<?>[] paramArray = methodType.parameterArray();
        if (needThreadState) {
            argOffset++;
        }
        if (info.isWide) {
            if (BaseCode.isWideCall(argType)) {
                /* if both are wide, this is a __call__ method, insert the receiver */
                mh = MethodHandles.insertArguments(mh, 0, dtype);
            }
        } else {
            for (int i = argOffset; i < argCount + argOffset; i++) {
                mh = PyBuiltinMethod.convert(mh, i, paramArray[i]);
            }
        }
        mh = MethodHandles.dropArguments(mh, 0, Object.class, ThreadState.class);
        mh = PyBuiltinMethod.asTypesafeReturn(mh, methodType);
        return new GuardedInvocation(mh, Guards.getIdentityGuard(linkRequest.getReceiver()));
    }
}
