package org.python.modules;

import jnr.constants.platform.Signal;
import org.python.annotations.ExposedConst;
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
    private static final Map<Signal, PyObject> mapping = new HashMap<>();

    @ExposedConst
    public static final int SIG_IGN = 0;
    @ExposedConst
    public static final int SIG_DFL = 1;

    @ModuleInit
    public static final void init(PyObject dict) {
        for (Signal sig : Signal.values()) {
            dict.__setitem__(sig.name(), new PyLong(sig.intValue()));
        }
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
        return new PyLong(SIG_DFL);
    }

    @ExposedFunction
    public static PyObject default_int_handler() {
        throw Py.KeyboardInterrupt("");
    }
}
