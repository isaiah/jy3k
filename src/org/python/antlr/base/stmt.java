// Autogenerated AST node
package org.python.antlr.base;
import org.antlr.v4.runtime.Token;
import org.python.antlr.AST;
import org.python.antlr.PythonTree;
import org.python.core.PyUnicode;
import org.python.core.PyType;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedType;

@ExposedType(name = "_ast.stmt", base = AST.class)
public abstract class stmt extends PythonTree {

    public static final PyType TYPE = PyType.fromClass(stmt.class);
    private final static PyUnicode[] fields = new PyUnicode[0];
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public abstract stmt copy();
    public stmt(PyType subtype) {
        super(subtype);
    }

    public stmt(PyType subtype, Token token) {
        super(subtype, token);
    }

    public stmt(PyType subtype, PythonTree node) {
        super(subtype, node);
    }

}
