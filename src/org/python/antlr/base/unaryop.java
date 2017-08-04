// Hand copied from stmt.
// XXX: autogenerate this.
package org.python.antlr.base;
import org.antlr.v4.runtime.Token;
import org.python.antlr.AST;
import org.python.antlr.PythonTree;
import org.python.core.PyBytes;
import org.python.core.PyType;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedType;

@ExposedType(name = "_ast.unaryop", base = AST.class)
public abstract class unaryop extends PythonTree {

    public static final PyType TYPE = PyType.fromClass(unaryop.class);
    private final static PyBytes[] fields = new PyBytes[0];
    @ExposedGet(name = "_fields")
    public PyBytes[] get_fields() { return fields; }

    private final static PyBytes[] attributes =
    new PyBytes[] {new PyBytes("lineno"), new PyBytes("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyBytes[] get_attributes() { return attributes; }

    public unaryop(PyType subtype, Token token) {
        super(subtype, token);
    }

    public unaryop(PyType subtype, PythonTree node) {
        super(subtype, node);
    }

}
