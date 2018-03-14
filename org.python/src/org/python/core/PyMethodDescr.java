package org.python.core;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.support.Guards;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;
import org.python.internal.lookup.MethodHandleFactory;
import org.python.internal.lookup.MethodHandleFunctionality;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@ExposedType(name = "method_descriptor", base = PyObject.class, isBaseType = false)
public class PyMethodDescr extends PyDescriptor implements DynLinkable, Traverseproc {
    static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    static final MethodHandleFunctionality MH = MethodHandleFactory.getFunctionality();
    public static final MethodHandle CHECK_CALLER_TYPE = MH.findStatic(LOOKUP, PyMethodDescr.class, "checkCallerType", MethodType.methodType(PyObject.class, PyType.class, PyObject.class));
    protected int minargs, maxargs;
    protected PyBuiltinMethod meth;

    public PyMethodDescr(PyType t, PyBuiltinMethod func) {
        name = func.info.getName();
        dtype = t;
        minargs = func.info.getMinargs();
        maxargs = func.info.getMaxargs();
        meth = func;
//        meth.setInfo(this);
    }

    public PyBuiltinMethod getMeth() {
        return meth;
    }

    @ExposedGet(name = "__doc__")
    public String getDoc() {
        return meth.getDoc();
    }

    public int getMaxargs() {
        return maxargs;
    }

    public void setMaxargs(int maxArgs) {
        maxargs = maxArgs;
    }

    public int getMinargs() {
        return minargs;
    }

    public void setMinargs(int minArgs) {
        minargs = minArgs;
    }

    @Override
    public String toString() {
        return String.format("<method '%s' of '%s' objects>", name, dtype.fastGetName());
    }

    @Override
    public PyObject __call__(PyObject[] args, String[] kwargs) {
        return method_descriptor___call__(args, kwargs);
    }

    @Override
    @ExposedMethod(defaults = "null")
    public PyObject __get__(PyObject obj, PyObject type) {
        if (obj != Py.None && obj != null) {
            checkGetterType(obj.getType());
            return meth.bind(obj);
        }
        return this;
    }

    @ExposedMethod
    final PyObject method_descriptor___call__(PyObject[] args, String[] kwargs) {
        checkReceiver(args);
        checkCallerType(dtype, args[0]);
        PyObject[] actualArgs = new PyObject[args.length - 1];
        System.arraycopy(args, 1, actualArgs, 0, actualArgs.length);
        return meth.bind(args[0]).invoke(actualArgs, kwargs);
    }

    /**
     * Return the name this descriptor is exposed as.
     *
     * @return a name String
     */
    @ExposedGet(name = "__name__")
    public String getName() {
        return name;
    }

    /**
     * Return the owner class of this descriptor.
     *
     * @return this descriptor's owner
     */
    @ExposedGet(name = "__objclass__")
    public PyObject getObjClass() {
        return dtype;
    }


    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        return meth == null ? 0 : visit.visit(meth, arg);
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        return ob != null && ob == meth;
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
        if (info.isWide) {
            if (argCount == 0) {
                mh = MethodHandles.insertArguments(mh, argOffset, Py.EmptyObjects, Py.NoKeywords);
            } else if (!BaseCode.isWideCall(argType)) {
                mh = MethodHandles.insertArguments(mh, 1 + argOffset, (Object) Py.NoKeywords);
                mh = mh.asCollector(argOffset, PyObject[].class, argCount);
            }
        } else {
            int paramCount = methodType.parameterCount() - argOffset;
            Class<?>[] paramArray = methodType.parameterArray();
            int defaultLength = info.defaults.length;
            int missingArg = paramCount - argCount;
            int startIndex = defaultLength - missingArg;
            if (missingArg > 0) {
                if (startIndex < 0) {
                    throw Py.TypeError(String.format("%s() takes exactly %d arguments (%d given)", info.getName(), methodType.parameterCount(), argCount));
                }

                for (int i = argCount; i < paramCount; i++) {
                    mh = MethodHandles.insertArguments(mh, argCount + argOffset, info.defaults[defaultLength + i - paramCount]);
                }
            }
            if (argCount > paramCount) {
                throw Py.TypeError(String.format("%s() takes at most %d argument%s (%d given)", info.getName(), paramCount, paramCount > 1 ? "s" : "", argCount));
            }
        }

//        MethodHandle filter = MethodHandles.insertArguments(CHECK_CALLER_TYPE, 0, dtype);
//        filter = MethodHandles.explicitCastArguments(filter, MethodType.methodType(methodType.parameterType(0), PyObject.class));
//        mh = MethodHandles.filterArguments(mh, 0, filter);
        mh = MethodHandles.dropArguments(mh, 0, Object.class);
        mh = MethodHandles.dropArguments(mh, 1, ThreadState.class);
        mh = PyBuiltinMethod.asTypesafeReturn(mh, methodType);
        // the guard guards the instance of the method, relink when the dtype is different
        return new GuardedInvocation(mh, Guards.getIdentityGuard(linkRequest.getReceiver()));
    }
}
