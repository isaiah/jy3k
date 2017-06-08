package org.python.modules.random;

import org.python.core.PyObject;
import org.python.expose.ExposedModule;
import org.python.expose.ModuleInit;

@ExposedModule(name = "_random")
public class RandomModule {

    private RandomModule() {}

    @ModuleInit
    public static void init(PyObject dict) {
        dict.__setitem__("Random", PyRandom.TYPE);
    }
}
