package org.python.core.linker;

import java.lang.invoke.MethodHandle;

public final class InvokeByName {
    private MethodHandle getter;
    private MethodHandle invoker;
    private String name;

    public InvokeByName(String name, Class<?> targetClass) {
        this(name, targetClass, Object.class);
    }

    public InvokeByName(String name, Class<?> targetClass, Class<?> rtype, Class<?>... ptypes) {
        this.name = name;
        this.getter = Bootstrap.createDynamicInvoker(name, Bootstrap.GET_PROPERTY, Object.class, targetClass);
        Class<?>[] finalPtypes;
        int plen = ptypes.length;
        if (plen == 0) {
            finalPtypes = new Class<?>[]{Object.class};
        } else {
            finalPtypes = new Class<?>[plen + 1];
            finalPtypes[0] = Object.class;
            System.arraycopy(ptypes, 0, finalPtypes, 1, plen);
        }
        this.invoker = Bootstrap.createDynamicCallInvoker(rtype, finalPtypes);
    }

    public String getName() {
        return name;
    }

    public MethodHandle getGetter() {
        return getter;
    }

    public MethodHandle getInvoker() {
        return invoker;
    }
}
