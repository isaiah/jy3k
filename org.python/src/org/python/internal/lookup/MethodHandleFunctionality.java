package org.python.internal.lookup;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public interface MethodHandleFunctionality {
    public MethodHandle findStatic(MethodHandles.Lookup explicitLookup, Class<?> clazz, String name, MethodType type);
    public MethodHandle findVirtual(MethodHandles.Lookup explicitLookup, Class<?> clazz, String name, MethodType type);
    public MethodHandle findConstructor(MethodHandles.Lookup explicitLookup, Class<?> clazz, MethodType type);
}
