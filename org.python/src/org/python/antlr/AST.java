package org.python.antlr;

import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSet;
import org.python.annotations.ExposedSlot;
import org.python.annotations.SlotFunc;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.Untraversable;
import org.python.annotations.ExposedType;

@Untraversable
@ExposedType(name = "_ast.AST", base = PyObject.class)
public class AST extends PyObject {
    public static final PyType TYPE = PyType.fromClass(AST.class);

    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject AST_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        return new AST();
    }
    
    public AST() {
        this(TYPE);
    }

    public AST(PyType objtype) {
        super(objtype);
    }

    public static boolean check(int nargs, int expected, boolean takesZeroArgs) {
        if (nargs == expected) {
            return true;
        }
        if (takesZeroArgs && nargs == 0) {
            return true;
        }
        return false;
    }

    public static PyException unexpectedCall(int expected, String name) {
        String message = " constructor takes 0 positional arguments";
        if (expected != 0) {
            message = " constructor takes either 0 or " + expected + " arguments";
        }
        return Py.TypeError(name + message);
    }
}
