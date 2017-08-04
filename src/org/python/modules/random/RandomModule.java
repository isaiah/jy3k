package org.python.modules.random;

import org.python.core.PyObject;
import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;

@ExposedModule(name = "_random")
public class RandomModule {

    private RandomModule() {}

    @ModuleInit
    public static void init(PyObject dict) {
        dict.__setitem__("Random", PyRandom.TYPE);
    }
}
