package org.python.core;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.support.Guards;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedSlot;
import org.python.annotations.SlotFunc;
import org.python.internal.lookup.MethodHandleFactory;
import org.python.internal.lookup.MethodHandleFunctionality;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SwitchPoint;

public abstract class PyNewWrapper extends PyBuiltinMethod implements Traverseproc {
    MethodHandleFunctionality MH = MethodHandleFactory.getFunctionality();
//    MethodHandle NEW_IMPL = MH.findVirtual(MethodHandles.lookup(), PyNewWrapper.class, "new_impl",
//            MethodType.methodType(PyObject.class, boolean.class, PyType.class, PyObject[].class, String[].class));

    MethodHandle WIDE_CALL = MH.findVirtual(MethodHandles.lookup(), PyNewWrapper.class, "__call__",
            MethodType.methodType(PyObject.class, PyObject[].class, String[].class));


    public PyType for_type;

    /**
     * Creates a wrapper without binding it to a type. setWrappedType must be called
     * before this wrapper can be used.
     */
    public PyNewWrapper() {
        this((PyType)null, "__new__", -1, -1);
    }

    public PyNewWrapper(Class c, String name, int minargs, int maxargs) {
        this(PyType.fromClass(c), name, minargs, maxargs);
    }

    public PyNewWrapper(PyType type, String name, int minargs, int maxargs) {
        super(type, new PyBuiltinMethodData(name, "", null, BuiltinDocs.type___new___doc, true, true, false));
        for_type = (PyType)getSelf();
    }


    // init true => invoke subtype.__init__(...) unless it is known to be
    // unnecessary
    public abstract PyObject new_impl(boolean init,
                                      PyType subtype,
                                      PyObject[] args,
                                      String[] keywords);

    public PyType getWrappedType() {
        return for_type;
    }

    public PyNewWrapper bind(PyObject bindTo) {
        return this;
    }

    @Override
    public GuardedInvocation findCallMethod(CallSiteDescriptor desc, LinkRequest request) {
        MethodType argType = desc.getMethodType();
        if (BaseCode.isWideCall(argType)) {
            return new GuardedInvocation(MethodHandles.dropArguments(WIDE_CALL, 1, ThreadState.class), Guards.getClassGuard(PyNewWrapper.class));
        }
        MethodHandle mh = MethodHandles.insertArguments(WIDE_CALL, 2, (Object) Py.NoKeywords);
        mh = mh.asCollector(1, PyObject[].class, argType.parameterCount() - 2);
        mh = MethodHandles.dropArguments(mh,1, ThreadState.class);
        return new GuardedInvocation(mh, Guards.getClassGuard(PyNewWrapper.class), new SwitchPoint[0], ClassCastException.class);
    }

    public void setWrappedType(PyType type) {
        self = type;
        for_type = type;
    }

    public PyObject __call__(PyObject[] args) {
        return __call__(args, Py.NoKeywords);
    }

    @ExposedSlot(SlotFunc.CALL)
    public static PyObject call(PyObject obj, PyObject[] args, String[] keywords) {
        PyNewWrapper self = (PyNewWrapper) obj;
        return self.__call__(args, keywords);
    }

    public PyObject __call__(PyObject[] args, String[] keywords) {
        int nargs = args.length;
        if (nargs < 1 || nargs == keywords.length) {
            throw Py.TypeError(for_type.fastGetName() + ".__new__(): not enough arguments");
        }
        PyObject arg0 = args[0];
        if (!(arg0 instanceof PyType)) {
            throw Py.TypeError(for_type.fastGetName() + ".__new__(X): X is not a type object ("
                    + arg0.getType().fastGetName() + ")");
        }
        PyType subtype = (PyType)arg0;
        if (!subtype.isSubType(for_type)) {
            throw Py.TypeError(for_type.fastGetName() + ".__new__(" + subtype.fastGetName() + "): "
                    + subtype.fastGetName() + " is not a subtype of " + for_type.fastGetName());
        }
        if (subtype.getStatic() != for_type) {
            throw Py.TypeError(for_type.fastGetName() + ".__new__(" + subtype.fastGetName()
                    + ") is not safe, use " + subtype.fastGetName() + ".__new__()");
        }
        PyObject[] rest = new PyObject[nargs - 1];
        System.arraycopy(args, 1, rest, 0, nargs - 1);
        return new_impl(false, subtype, rest, keywords);
    }

    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        return for_type == null ? 0 : visit.visit(for_type, arg);
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        return ob != null && ob == for_type;
    }
}
