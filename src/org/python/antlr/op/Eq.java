// Autogenerated AST node
package org.python.antlr.op;

import org.python.antlr.base.cmpop;
import org.python.antlr.PythonTree;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyUnicode;
import org.python.core.PyType;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;

@ExposedType(name = "_ast.Eq", base = cmpop.class)
public class Eq extends PythonTree {
    public static final PyType TYPE = PyType.fromClass(Eq.class);

    public Eq() {
        super(TYPE);
    }
    public Eq(PyType subType) {
        super(subType);
    }
    @ExposedNew
    @ExposedMethod
    public void Eq___init__(PyObject[] args, String[] keywords) {}

    private final static PyUnicode[] fields = new PyUnicode[0];
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes = new PyUnicode[0];
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    @ExposedMethod
    public PyObject __int__() {
        return Eq___int__();
    }

    final PyObject Eq___int__() {
        return Py.newInteger(1);
    }

    @Override
    public String toStringTree() {
        return Eq.class.toString();
    }
}
