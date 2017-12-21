package org.python.modules;

import org.python.core.BytecodeNotification;
import org.python.core.PyObject;
import org.python.core.Py;
import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;

/**
 * BytecodeTools provides tools for generated JVM bytecode.
 * <p/>
 * This module supports registering a python callback function
 * to be notified when new bytecode is loaded.
 * see also core/BytecodeNotification.java
 */
@ExposedModule
public class _bytecodetools {
    static class _Callback implements BytecodeNotification.Callback {
        PyObject callback;

        public _Callback(PyObject callback) {
            this.callback = callback;
        }

        public void notify(String name, byte[] bytes, Class c) {
            callback.__call__(Py.java2py(name), Py.java2py(bytes), Py.java2py(c));
        }

        public int hashCode() {
            return callback.hashCode();
        }

        public boolean equals(Object other) {
            if (!(other instanceof _Callback)) return false;
            _Callback that = (_Callback) other;
            return callback.equals(that.callback);
        }
    }

    /**
     * Registers a python callback function that will be notified on bytecode loading.
     *
     * @param callback a Python callback function
     */
    @ExposedFunction
    public static void register(final PyObject callback) {
        BytecodeNotification.register(new _Callback(callback));
    }

    /**
     * Unregisters a python callback function.
     *
     * @param callback a Python callback function
     */
    @ExposedFunction
    public static boolean unregister(final PyObject callback) {
        return BytecodeNotification.unregister(new _Callback(callback));
    }

    /**
     * Clears all the registered callbacks.
     */
    @ExposedFunction
    public static void clear() {
        BytecodeNotification.clear();
    }
}
