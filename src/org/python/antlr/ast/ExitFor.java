// Autogenerated AST node
package org.python.antlr.ast;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.python.antlr.AST;
import org.python.antlr.PythonTree;
import org.python.antlr.adapter.AstAdapters;
import org.python.antlr.base.excepthandler;
import org.python.antlr.base.expr;
import org.python.antlr.base.mod;
import org.python.antlr.base.slice;
import org.python.antlr.base.stmt;
import org.python.core.ArgParser;
import org.python.core.AstList;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyUnicode;
import org.python.core.PyStringMap;
import org.python.core.PyType;
import org.python.core.Visitproc;
import org.python.expose.ExposedGet;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedNew;
import org.python.expose.ExposedSet;
import org.python.expose.ExposedType;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

@ExposedType(name = "_ast.ExitFor", base = stmt.class)
public class ExitFor extends stmt {
public static final PyType TYPE = PyType.fromClass(ExitFor.class);

    private final static PyUnicode[] fields = new PyUnicode[0];
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    @ExposedNew
    @ExposedMethod
    public void ExitFor___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("ExitFor", args, keywords, new String[]
            {"lineno", "col_offset"}, 0, true);
        int lin = ap.getInt(0, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(1, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public ExitFor() {
        super(TYPE);
    }

    // called from derived class
    public ExitFor(PyType subtype) {
        super(subtype);
    }

    public ExitFor(Token token) {
        super(TYPE, token);
    }

    public ExitFor(PythonTree tree) {
        super(TYPE, tree);
    }

    public ExitFor copy() {
        return new ExitFor(this.getToken());
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "ExitFor";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("ExitFor(");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitExitFor(this);
    }

    public void traverse(VisitorIF<?> visitor) throws Exception {
    }

    public void replaceField(expr value, expr newValue) {
    }

    public PyObject __dict__;

    @Override
    public PyObject fastGetDict() {
        ensureDict();
        return __dict__;
    }

    @ExposedGet(name = "__dict__")
    public PyObject getDict() {
        return fastGetDict();
    }

    private void ensureDict() {
        if (__dict__ == null) {
            __dict__ = new PyStringMap();
        }
    }

    private int lineno = -1;
    @ExposedGet(name = "lineno")
    public int getLineno() {
        if (lineno != -1) {
            return lineno;
        }
        return getLine();
    }

    @ExposedSet(name = "lineno")
    public void setLineno(int num) {
        lineno = num;
    }

    private int col_offset = -1;
    @ExposedGet(name = "col_offset")
    public int getCol_offset() {
        if (col_offset != -1) {
            return col_offset;
        }
        return getCharPositionInLine();
    }

    @ExposedSet(name = "col_offset")
    public void setCol_offset(int num) {
        col_offset = num;
    }

}
