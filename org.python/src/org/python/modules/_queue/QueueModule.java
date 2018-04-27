package org.python.modules._queue;

import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;
import org.python.core.PyObject;

@ExposedModule(name = "_queue")
public class QueueModule {

    @ModuleInit
    public static void init(PyObject dict) {
        dict.__setitem__("SimpleQueue", PySimpleQueue.TYPE);
        dict.__setitem__("Empty", new PySimpleQueue());
    }
}
