// Hand copied from stmt.
// XXX: autogenerate this.
package org.python.antlr.base;
import org.antlr.v4.runtime.Token;
import org.python.antlr.AST;
import org.python.antlr.PythonTree;
import org.python.core.PyBytes;
import org.python.core.PyType;
import org.python.expose.ExposedGet;
import org.python.expose.ExposedType;

@ExposedType(name = "_ast.boolop", base = AST.class)
public abstract class boolop extends PythonTree {

    public static final PyType TYPE = PyType.fromClass(boolop.class);
    private final static PyBytes[] fields = new PyBytes[0];
    @ExposedGet(name = "_fields")
    public PyBytes[] get_fields() { return fields; }

    private final static PyBytes[] attributes =
    new PyBytes[] {new PyBytes("lineno"), new PyBytes("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyBytes[] get_attributes() { return attributes; }

    public boolop(Token token) {
        super(TYPE, token);
    }
}
