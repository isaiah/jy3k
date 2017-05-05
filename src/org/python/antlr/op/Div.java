// Autogenerated AST node
package org.python.antlr.op;

import org.python.antlr.AST;
import org.python.antlr.base.operator;
import org.python.antlr.PythonTree;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyUnicode;
import org.python.core.PyType;
import org.python.expose.ExposedGet;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedNew;
import org.python.expose.ExposedSet;
import org.python.expose.ExposedType;

@ExposedType(name = "_ast.Div", base = operator.class)
public class Div extends PythonTree {
    public static final PyType TYPE = PyType.fromClass(Div.class);

    public Div() {
        super(TYPE);
    }
    public Div(PyType subType) {
        super(subType);
    }
    @ExposedNew
    @ExposedMethod
    public void Div___init__(PyObject[] args, String[] keywords) {}

    private final static PyUnicode[] fields = new PyUnicode[0];
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes = new PyUnicode[0];
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    @ExposedMethod
    public PyObject __int__() {
        return Div___int__();
    }

    final PyObject Div___int__() {
        return Py.newInteger(5);
    }

    @Override
    public String toStringTree() {
        return Div.class.toString();
    }
}
