// Autogenerated AST node
package org.python.antlr.op;

import org.python.antlr.base.expr_context;
import org.python.antlr.PythonTree;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyUnicode;
import org.python.core.PyType;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;

@ExposedType(name = "_ast.Param", base = expr_context.class)
public class Param extends PythonTree {
    public static final PyType TYPE = PyType.fromClass(Param.class);

    public Param() {
        super(TYPE);
    }
    public Param(PyType subType) {
        super(subType);
    }
    @ExposedNew
    @ExposedMethod
    public void Param___init__(PyObject[] args, String[] keywords) {}

    private final static PyUnicode[] fields = new PyUnicode[0];
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes = new PyUnicode[0];
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    @ExposedMethod
    public PyObject __int__() {
        return Param___int__();
    }

    final PyObject Param___int__() {
        return Py.newInteger(6);
    }

    @Override
    public String toStringTree() {
        return Param.class.toString();
    }
}
