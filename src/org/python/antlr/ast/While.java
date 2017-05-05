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

@ExposedType(name = "_ast.While", base = stmt.class)
public class While extends stmt {
public static final PyType TYPE = PyType.fromClass(While.class);
    private expr test;
    public expr getInternalTest() {
        return test;
    }
    @ExposedGet(name = "test")
    public PyObject getTest() {
        return test;
    }
    @ExposedSet(name = "test")
    public void setTest(PyObject test) {
        this.test = AstAdapters.py2expr(test);
    }

    private java.util.List<stmt> body;
    public java.util.List<stmt> getInternalBody() {
        return body;
    }
    public void setInternalBody(java.util.List<stmt> body) {
        this.body = body;
    }
    @ExposedGet(name = "body")
    public PyObject getBody() {
        return new AstList(body, AstAdapters.stmtAdapter);
    }
    @ExposedSet(name = "body")
    public void setBody(PyObject body) {
        this.body = AstAdapters.py2stmtList(body);
    }

    private java.util.List<stmt> orelse;
    public java.util.List<stmt> getInternalOrelse() {
        return orelse;
    }
    public void setInternalOrelse(java.util.List<stmt> orelse) {
        this.orelse = orelse;
    }
    @ExposedGet(name = "orelse")
    public PyObject getOrelse() {
        return new AstList(orelse, AstAdapters.stmtAdapter);
    }
    @ExposedSet(name = "orelse")
    public void setOrelse(PyObject orelse) {
        this.orelse = AstAdapters.py2stmtList(orelse);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("test"), new PyUnicode("body"), new PyUnicode("orelse")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public While() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void While___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("While", args, keywords, new String[]
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

    public While(PyObject test, PyObject body, PyObject orelse) {
        super(TYPE);
        setTest(test);
        setBody(body);
        setOrelse(orelse);
    }

    // called from derived class
    public While(PyType subtype) {
        super(subtype);
    }

    public While(Token token, expr test, java.util.List<stmt> body, java.util.List<stmt> orelse) {
        super(TYPE, token);
        this.test = test;
        this.body = body;
        if (body == null) {
            this.body = new ArrayList<>(0);
        }
        for(int i = 0; i < this.body.size(); i++) {
            PythonTree t = this.body.get(i);
            addChild(t, i, this.body);
        }
        this.orelse = orelse;
        if (orelse == null) {
            this.orelse = new ArrayList<>(0);
        }
        for(int i = 0; i < this.orelse.size(); i++) {
            PythonTree t = this.orelse.get(i);
            addChild(t, i, this.orelse);
        }
    }

    public While(PythonTree tree, expr test, java.util.List<stmt> body, java.util.List<stmt>
    orelse) {
        super(TYPE, tree);
        this.test = test;
        this.body = body;
        if (body == null) {
            this.body = new ArrayList<>(0);
        }
        for(int i = 0; i < this.body.size(); i++) {
            PythonTree t = this.body.get(i);
            addChild(t, i, this.body);
        }
        this.orelse = orelse;
        if (orelse == null) {
            this.orelse = new ArrayList<>(0);
        }
        for(int i = 0; i < this.orelse.size(); i++) {
            PythonTree t = this.orelse.get(i);
            addChild(t, i, this.orelse);
        }
    }

    public While copy() {
        return new While(this.getToken(), this.test, this.body, this.orelse);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "While";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("While(");
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

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitWhile(this);
    }

    public void traverse(VisitorIF<?> visitor) throws Exception {
        if (test != null)
            test.accept(visitor);
        if (body != null) {
            for (PythonTree t : body) {
                if (t != null)
                    t.accept(visitor);
            }
        }
        if (orelse != null) {
            for (PythonTree t : orelse) {
                if (t != null)
                    t.accept(visitor);
            }
        }
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
