package org.python.modules._socket;

import jnr.constants.platform.AddressFamily;
import jnr.constants.platform.Sock;
import jnr.constants.platform.SocketOption;
import org.python.annotations.ExposedConst;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyUnicode;
import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;

import java.util.Arrays;

@ExposedModule(name = "_socket")
public class SocketModule {

    @ModuleInit
    public static void classDictInit(final PyObject dict) {
        dict.__setitem__("socket", PySocket.TYPE);
        for (AddressFamily value : AddressFamily.values()) {
            dict.__setitem__(value.description(), new PyLong(value.intValue()));
        }
        for (Sock value : Sock.values()) {
            dict.__setitem__(value.description(), new PyLong(value.intValue()));
        };
        for (SocketOption value : SocketOption.values()) {
            dict.__setitem__(value.description(), new PyLong(value.intValue()));
        };
    }
}
