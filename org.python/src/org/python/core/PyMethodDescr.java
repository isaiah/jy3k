package org.python.core;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.support.Guards;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;
import org.python.internal.lookup.MethodHandleFactory;
import org.python.internal.lookup.MethodHandleFunctionality;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * A Method descriptor represents a builtin instance method that's detached from instance, e.g. str.split
 */
@ExposedType(name = "method_descriptor", base = PyObject.class, isBaseType = false)
public class PyMethodDescr extends PyDescriptor implements DynLinkable, Traverseproc {
    public static final PyType TYPE = PyType.fromClass(PyMethodDescr.class);

    static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    static final MethodHandleFunctionality MH = MethodHandleFactory.getFunctionality();
    public static final MethodHandle CHECK_CALLER_TYPE = MH.findStatic(LOOKUP, PyMethodDescr.class, "checkCallerType", MethodType.methodType(PyObject.class, String.class, PyType.class, PyObject.class));
    public static final MethodHandle REMOVE_SELF = MH.findStatic(LOOKUP, PyMethodDescr.class, "removeSelf", MethodType.methodType(PyObject[].class, PyObject[].class));

    static MethodHandle SELF_GETTER = MethodHandles.arrayElementGetter(PyObject[].class);
    static {
        SELF_GETTER = MethodHandles.insertArguments(SELF_GETTER, 1, 0);
    }
    protected int minargs, maxargs;

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
    public PyObject getDoc() {
        return PyType.getBuiltinDoc(dtype.fastGetName() + "_" + name + "_doc");
//        return meth.getDoc();
    }

    @ExposedGet(name = "__text_signature__")
    public PyObject textSignature() {
        return PyType.getBuiltinDoc(dtype.fastGetName() + "_" + name + "_sig");
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
        return method_descriptor___call__(this, args, kwargs);
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

//    @ExposedMethod
//    final PyObject method_descriptor___call__(PyObject[] args, String[] kwargs) {
//        checkReceiver(args);
//        checkCallerType(dtype, args[0]);
//        PyObject[] actualArgs = new PyObject[args.length - 1];
//        System.arraycopy(args, 1, actualArgs, 0, actualArgs.length);
//        return meth.bind(args[0]).invoke(actualArgs, kwargs);
//    }
    @ExposedSlot(SlotFunc.CALL)
    public final static PyObject method_descriptor___call__(PyObject obj, PyObject[] args, String[] kwargs) {
        PyDescriptor self;
        if (obj instanceof PyDescriptor) {
            self = (PyDescriptor) obj;

            self.checkReceiver(args);
            checkCallerType(self.name, self.dtype, args[0]);
            PyObject[] actualArgs = new PyObject[args.length - 1];
            System.arraycopy(args, 1, actualArgs, 0, actualArgs.length);
            return self.meth.bind(args[0]).invoke(actualArgs, kwargs);
        } else {
            self = ((PyMethodWrapper) obj).getDescr();
            return self.meth.bind(self).invoke(args, kwargs);
        }
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
        boolean needThreadState  = methodType.parameterCount() > argOffset && methodType.parameterType(argOffset) == ThreadState.class;
        if (needThreadState) {
            argOffset++;
        }

        MethodHandle checker = MethodHandles.insertArguments(CHECK_CALLER_TYPE, 0, this.getClass().getCanonicalName(), dtype);
        checker = MethodHandles.explicitCastArguments(checker, MethodType.methodType(methodType.parameterType(0), PyObject.class));
        mh = MethodHandles.filterArguments(mh, 0, checker);
        int paramCount = methodType.parameterCount() - argOffset;
        Class<?>[] paramArray = methodType.parameterArray();
        int defaultLength = info.defaults.length;
        int missingArg = paramCount - argCount;
        int startIndex = defaultLength - missingArg;
        if (info.isWide) {
            if (BaseCode.isWideCall(argType)) {
                if (argType.parameterType(2) != PyObject.class) {
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
        } else if(BaseCode.isWideCall(argType)) {
            /** it's a wide call, but not a wide method */
            String[] keywords = (String[]) linkRequest.getArguments()[3];
            if (keywords.length != 0) {
                throw Py.TypeError(String.format("%s() takes no keyword arguments", info.getName()));
            }
            // it's a stararg call, spread the args
            PyObject[] args = (PyObject[]) linkRequest.getArguments()[2];
            paramCount++;
            argCount = args.length; // without self
            if (paramCount < argCount) {
                if (paramCount == 0) {
                    throw Py.TypeError(String.format("%s() takes no arguments, (%d given)", info.getName(), args.length));
                } else {
                    throw Py.TypeError(String.format("%s() takes at most %d arguments, (%d given)",
                            info.getName(), paramCount - defaultLength, args.length));
                }
            }

            for (int i = argOffset; i < argCount; i++) {
                mh = PyBuiltinMethod.convert(mh, i, paramArray[i]);
            }

            missingArg = paramCount - argCount;
            startIndex = defaultLength - missingArg;
            if (startIndex < 0) {
                throw Py.TypeError(String.format("%s() takes exactly %d arguments (%d given)", info.getName(), methodType.parameterCount(), argCount));
            }
            if (missingArg > 0) {
                for (int i = argCount; i < paramCount; i++) {
                    mh = MethodHandles.insertArguments(mh, argCount + argOffset, info.defaults[defaultLength + i - paramCount]);
                }
            }
            mh = mh.asSpreader(0, PyObject[].class, argCount);

            mh = MethodHandles.dropArguments(mh, 1, String[].class);
        } else {
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
            for (int i = argOffset; i < argCount + argOffset; i++) {
                mh = PyBuiltinMethod.convert(mh, i, paramArray[i]);
            }
        }


        /*
         * FIXME: it has difficulty linking instance native method that take ThreadState as first argument
         * e.g. object.__format__(object(), ""), since ts is always in the second position, the order is wrong with the receiver
         */
        mh = MethodHandles.dropArguments(mh, 0, PyObject.class);
        if (!needThreadState) {
            mh = MethodHandles.dropArguments(mh, 1, ThreadState.class);
        }
        mh = PyBuiltinMethod.asTypesafeReturn(mh, methodType);
        // the guard guards the instance of the method, relink when the dtype is different
        return new GuardedInvocation(mh, Guards.getIdentityGuard(linkRequest.getReceiver()));
    }

    static PyObject[] removeSelf(PyObject[] args) {
        int len = args.length;
        assert len > 0: "need at least one argument";
        PyObject[] newArgs = new PyObject[len - 1];
        System.arraycopy(args, 1, newArgs, 0, len - 1);
        return newArgs;
    }
}
