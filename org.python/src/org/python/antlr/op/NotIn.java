// Autogenerated AST node
package org.python.antlr.op;

import org.python.antlr.AST;
import org.python.antlr.base.cmpop;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.cmpopType;
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

@ExposedType(name = "_ast.NotIn", base = cmpop.class)
public class NotIn extends PythonTree {
    public static final PyType TYPE = PyType.fromClass(NotIn.class);

    public NotIn() {
        super(TYPE);
    }
    public NotIn(PyType subType) {
        super(subType);
    }
    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject NotIn_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[]
    args, String[] keywords) {
        return cmpopType.NotIn.getImpl();
    }
    @ExposedMethod
    public void NotIn___init__(PyObject[] args, String[] keywords) {}

    private final static PyUnicode[] fields = new PyUnicode[0];
    @ExposedGet(name = "_fields")
    public PyObject get_fields() { return Py.EmptyTuple; }

    private final static PyUnicode[] attributes = new PyUnicode[0];
    @ExposedGet(name = "_attributes")
    public PyObject get_attributes() { return Py.EmptyTuple; }

    @Override
    public String toStringTree() {
        return "NotIn";
    }
}
