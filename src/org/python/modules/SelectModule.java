package org.python.modules;

import org.python.core.PyObject;
import org.python.core.PyUnicode;
import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;

/**
 * Created by isaiah on 6/19/16.
 */
@ExposedModule
public class SelectModule {
    @ModuleInit
    public static void classDictInit(PyObject dict) {
        dict.__setitem__("__name__", new PyUnicode("select"));
    }
}
