// Autogenerated AST node
package org.python.antlr.op;

import org.python.antlr.AST;
import org.python.antlr.base.boolop;
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

@ExposedType(name = "_ast.Or", base = boolop.class)
public class Or extends PythonTree {
    public static final PyType TYPE = PyType.fromClass(Or.class);

    public Or() {
        super(TYPE);
    }
    public Or(PyType subType) {
        super(subType);
    }
    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject Or_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args,
    String[] keywords) {
        return new Or(subtype);
    }
    @ExposedMethod
    public void Or___init__(PyObject[] args, String[] keywords) {}

    private final static PyUnicode[] fields = new PyUnicode[0];
    @ExposedGet(name = "_fields")
    public PyObject get_fields() { return Py.EmptyTuple; }

    private final static PyUnicode[] attributes = new PyUnicode[0];
    @ExposedGet(name = "_attributes")
    public PyObject get_attributes() { return Py.EmptyTuple; }

    @ExposedMethod
    public final PyObject Or___int__() {
        return Py.newInteger(2);
    }

    @Override
    public String toStringTree() {
        return Or.class.toString();
    }
}
