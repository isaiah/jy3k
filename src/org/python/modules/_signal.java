package org.python.modules;

import jnr.constants.platform.Signal;
import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;
import org.python.core.Py;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.modules.posix.PosixModule;

import java.util.HashMap;
import java.util.Map;

@ExposedModule(name = "_signal")
public class _signal {
    private static Map<Signal, PyObject> mapping;
    private static PyObject SIG_IGN = Py.Zero;
    private static PyObject SIG_DFL = Py.One;

    @ModuleInit
    public static final void init(PyObject dict) {
        for (Signal sig : Signal.values()) {
            dict.__setitem__(sig.name(), new PyLong(sig.intValue()));
        }
        dict.__setitem__("SIG_IGN", SIG_IGN);
        dict.__setitem__("SIG_DFL", SIG_DFL);
        mapping = new HashMap<>();
    }

    @ExposedFunction
    public static void signal(int num, PyObject handler) {
        Signal sig = Signal.valueOf(num);
        mapping.put(sig, handler);
        PosixModule.signal(sig, handler);
    }

    @ExposedFunction
    public static PyObject getsignal(int num) {
        Signal sig = Signal.valueOf(num);
        if (mapping.containsKey(sig)) {
            return mapping.get(sig);
        }
        return SIG_DFL;
    }
}
