package org.python.modules.sre;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.expose.ExposedFunction;
import org.python.expose.ExposedModule;
import org.python.internal.joni.Regex;

/**
 * Created by isaiah on 3/24/17.
 */
@ExposedModule(name="_sre_compile", doc="Native implementation of sre_compile")
public class _sre_compile {

    @ExposedFunction
    public static PyObject compile(String s) {
        return new PySRE_Pattern(s);
    }
}
