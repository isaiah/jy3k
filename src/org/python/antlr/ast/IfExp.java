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
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSet;
import org.python.annotations.ExposedType;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

@ExposedType(name = "_ast.IfExp", base = expr.class)
public class IfExp extends expr {
public static final PyType TYPE = PyType.fromClass(IfExp.class);
    private expr test;
    public expr getInternalTest() {
        return test;
    }
    public void setInternalTest(expr test) {
        this.test = test;
    }
    @ExposedGet(name = "test")
    public PyObject getTest() {
        return test;
    }
    @ExposedSet(name = "test")
    public void setTest(PyObject test) {
        this.test = AstAdapters.py2expr(test);
    }

    private expr body;
    public expr getInternalBody() {
        return body;
    }
    public void setInternalBody(expr body) {
        this.body = body;
    }
    @ExposedGet(name = "body")
    public PyObject getBody() {
        return body;
    }
    @ExposedSet(name = "body")
    public void setBody(PyObject body) {
        this.body = AstAdapters.py2expr(body);
    }

    private expr orelse;
    public expr getInternalOrelse() {
        return orelse;
    }
    public void setInternalOrelse(expr orelse) {
        this.orelse = orelse;
    }
    @ExposedGet(name = "orelse")
    public PyObject getOrelse() {
        return orelse;
    }
    @ExposedSet(name = "orelse")
    public void setOrelse(PyObject orelse) {
        this.orelse = AstAdapters.py2expr(orelse);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("test"), new PyUnicode("body"), new PyUnicode("orelse")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public IfExp() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void IfExp___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("IfExp", args, keywords, new String[]
            {"test", "body", "orelse", "lineno", "col_offset"}, 3, true);
        setTest(ap.getPyObject(0, Py.None));
        setBody(ap.getPyObject(1, Py.None));
        setOrelse(ap.getPyObject(2, Py.None));
        int lin = ap.getInt(3, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(4, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public IfExp(PyObject test, PyObject body, PyObject orelse) {
        super(TYPE);
        setTest(test);
        setBody(body);
        setOrelse(orelse);
    }

    // called from derived class
    public IfExp(PyType subtype) {
        super(subtype);
    }

    public IfExp(Token token, expr test, expr body, expr orelse) {
        super(TYPE, token);
        this.test = test;
        if (this.test != null)
            this.test.setParent(this);
        this.body = body;
        if (this.body != null)
            this.body.setParent(this);
        this.orelse = orelse;
        if (this.orelse != null)
            this.orelse.setParent(this);
    }

    public IfExp(PythonTree tree, expr test, expr body, expr orelse) {
        super(TYPE, tree);
        this.test = test;
        if (this.test != null)
            this.test.setParent(this);
        this.body = body;
        if (this.body != null)
            this.body.setParent(this);
        this.orelse = orelse;
        if (this.orelse != null)
            this.orelse.setParent(this);
    }

    public IfExp copy() {
        return new IfExp(this.getToken(), this.test, this.body, this.orelse);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "IfExp";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("IfExp(");
        sb.append("test=");
        sb.append(dumpThis(test));
        sb.append(",");
        sb.append("body=");
        sb.append(dumpThis(body));
        sb.append(",");
        sb.append("orelse=");
        sb.append(dumpThis(orelse));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterIfExp(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveIfExp(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitIfExp(this);
    }

    public void traverse(VisitorIF<?> visitor) {
        if (test != null)
            test.accept(visitor);
        if (body != null)
            body.accept(visitor);
        if (orelse != null)
            orelse.accept(visitor);
    }

    public void replaceField(expr value, expr newValue) {
        if (value == test) this.test = newValue;
        if (value == body) this.body = newValue;
        if (value == orelse) this.orelse = newValue;
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
