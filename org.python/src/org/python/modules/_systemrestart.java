package org.python.modules;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;

@ExposedModule
public class _systemrestart {
    /**
     * Jython-specific exception for restarting the interpreter. Currently
     * supported only by jython.java, when executing a file (i.e,
     * non-interactive mode).
     *
     * WARNING: This is highly *experimental* and subject to change.
     */
    public static PyObject SystemRestart;

    @ModuleInit
    public static void init(PyObject dict) {
        dict.__setitem__("SystemRestart", Py.makeClass(
                "_systemrestart.SystemRestart",
                new PyStringMap() {{
                    __setitem__("__doc__",
                            Py.newString("Request to restart the interpreter. " +
                                         "(Jython-specific)"));
                }}, Py.BaseException));
    }
}
