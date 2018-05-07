// Autogenerated AST node
package org.python.antlr.op;

import org.python.antlr.AST;
import org.python.antlr.base.cmpop;
import org.python.antlr.PythonTree;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyUnicode;
import org.python.core.PyType;
import org.python.core.PyNewWrapper;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSet;
import org.python.annotations.ExposedType;
import org.python.annotations.ExposedSlot;
import org.python.annotations.SlotFunc;

@ExposedType(name = "_ast.Is", base = cmpop.class)
public class Is extends PythonTree {
    public static final PyType TYPE = PyType.fromClass(Is.class);

    public Is() {
        super(TYPE);
    }
    public Is(PyType subType) {
        super(subType);
    }
    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject Is_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args,
    String[] keywords) {
        return new Is(subtype);
    }
    @ExposedMethod
    public void Is___init__(PyObject[] args, String[] keywords) {}

    private final static PyUnicode[] fields = new PyUnicode[0];
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes = new PyUnicode[0];
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    @ExposedMethod
    public final PyObject Is___int__() {
        return Py.newInteger(7);
    }

    @Override
    public String toStringTree() {
        return Is.class.toString();
    }
}
