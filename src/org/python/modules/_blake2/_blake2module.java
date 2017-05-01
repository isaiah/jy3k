package org.python.modules._blake2;

import org.python.core.PyObject;
import org.python.expose.ExposedModule;
import org.python.expose.ModuleInit;

@ExposedModule(name = "_blake2")
public class _blake2module {

    @ModuleInit
    public static void init(PyObject dict) {
        dict.__setitem__("blake2b", PyBlake2b.TYPE);
        dict.__setitem__("blake2s", PyBlake2s.TYPE);
    }
}
