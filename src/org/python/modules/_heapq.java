package org.python.modules;

import org.python.core.PyObject;
import org.python.expose.ExposedModule;
import org.python.expose.ModuleInit;

/**
 * Native implementation of heapq
 */
@ExposedModule
public class _heapq {
    @ModuleInit
    public static void init(PyObject dict) {
    }
}
