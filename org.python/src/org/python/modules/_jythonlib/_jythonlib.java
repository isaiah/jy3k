/* Copyright (c) Jython Developers */
package org.python.modules._jythonlib;

import org.python.core.PyBytes;
import org.python.core.PyObject;
import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;


@ExposedModule
public class _jythonlib {

    public static final PyBytes __doc__ = new PyBytes("jythonlib module");

    @ModuleInit
    public static void classDictInit(PyObject dict) {
        dict.__setitem__("__name__", new PyBytes("_jythonlib"));
        dict.__setitem__("__doc__", __doc__);
        dict.__setitem__("__module__", new PyBytes("_jythonlib"));
        dict.__setitem__("dict_builder", dict_builder.TYPE);
        dict.__setitem__("set_builder", set_builder.TYPE);
    }

}
