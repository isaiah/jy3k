package org.python.modules._socket;

import org.python.core.PyObject;
import org.python.core.PyUnicode;
import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;

/**
 * Created by isaiah on 6/18/16.
 */
@ExposedModule
public class _socket {
    @ModuleInit
    public static void classDictInit(PyObject dict) {
        dict.__setitem__("__name__", new PyUnicode("_socket"));
        dict.__setitem__("socket", PySocket.TYPE);
    }


}
