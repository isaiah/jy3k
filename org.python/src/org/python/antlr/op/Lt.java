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

@ExposedType(name = "_ast.Lt", base = cmpop.class)
public class Lt extends PythonTree {
    public static final PyType TYPE = PyType.fromClass(Lt.class);

    public Lt() {
        super(TYPE);
    }
    public Lt(PyType subType) {
        super(subType);
    }
    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject Lt_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args,
    String[] keywords) {
        return new Lt(subtype);
    }
    @ExposedMethod
    public void Lt___init__(PyObject[] args, String[] keywords) {}

    private final static PyUnicode[] fields = new PyUnicode[0];
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes = new PyUnicode[0];
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    @ExposedMethod
    public final PyObject Lt___int__() {
        return Py.newInteger(3);
    }

    @Override
    public String toStringTree() {
        return Lt.class.toString();
    }
}
