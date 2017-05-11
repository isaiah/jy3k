package org.python.modules._sha3;

import org.python.core.PyObject;
import org.python.core.PyUnicode;
import org.python.expose.ExposedModule;
import org.python.expose.ModuleInit;

/**
 * Created by isaiah on 5/1/17.
 */
@ExposedModule(name = "_sha3")
public class _sha3module {

    @ModuleInit
    public static void init(PyObject dict) {
        dict.__setitem__("implementation", new PyUnicode("java based implementation"));
        dict.__setitem__("sha3_224", PySHA224.TYPE);
        dict.__setitem__("sha3_256", PySHA256.TYPE);
        dict.__setitem__("sha3_384", PySHA384.TYPE);
        dict.__setitem__("sha3_512", PySHA512.TYPE);
        dict.__setitem__("shake_128", PyShake128.TYPE);
        dict.__setitem__("shake_256", PyShake256.TYPE);
    }
}
