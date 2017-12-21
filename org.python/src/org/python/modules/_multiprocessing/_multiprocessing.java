package org.python.modules._multiprocessing;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;

@ExposedModule
public class _multiprocessing {
    @ModuleInit
    public static void classDictInit(PyObject dict) {
        dict.__setitem__("SemLock", PySemLock.TYPE);
        dict.__setitem__("sem_unlock", Py.NotImplemented);
    }

}
