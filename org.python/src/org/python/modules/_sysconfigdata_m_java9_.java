package org.python.modules;

import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;
import org.python.core.PyDictionary;
import org.python.core.PyObject;

@ExposedModule
public class _sysconfigdata_m_java9_ {

    @ModuleInit
    public static final void init(PyObject dict) {
        dict.__setitem__("build_time_vars", new PyDictionary());
    }
}
