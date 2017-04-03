package org.python.modules.sre;

import org.python.core.ArgParser;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.expose.ExposedConst;
import org.python.expose.ExposedFunction;
import org.python.expose.ExposedModule;

/**
 * Created by isaiah on 3/24/17.
 */
@ExposedModule(name="_sre_compile", doc="Native implementation of sre_compile")
public class _sre_compile {

    @ExposedFunction
    public static PyObject compile(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("compile", args, keywords, "pattern", "flags");
        PyObject flags = ap.getPyObject(1, Py.Zero);
        PyObject s = ap.getPyObject(0);
        return new PySRE_Pattern(s, flags);
    }

    @ExposedConst
    public static final int MAGIC = 20140917;

    @ExposedConst
    public static final int MAXREPEAT = Character.MAX_VALUE;

    @ExposedConst
    public static final int MAXGROUPS = Integer.MAX_VALUE;

    @ExposedConst
    public static final int CODESIZE = 4;

    @ExposedFunction
    public static int getcodesize() {
        return CODESIZE;
    }

    @ExposedFunction
    public static int getlower(PyObject ch, PyObject flags) {
        return SRE_STATE.getlower(ch.asInt(), flags.asInt());
    }
}
