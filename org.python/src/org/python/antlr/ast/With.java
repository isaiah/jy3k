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
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyUnicode;
import org.python.core.PyStringMap;
import org.python.core.PyType;
import org.python.core.PyList;
import org.python.core.Visitproc;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSet;
import org.python.annotations.ExposedType;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

@ExposedType(name = "_ast.With", base = stmt.class)
public class With extends stmt {
public static final PyType TYPE = PyType.fromClass(With.class);
    private java.util.List<withitem> items;
    public java.util.List<withitem> getInternalItems() {
        return items;
    }
    public void setInternalItems(java.util.List<withitem> items) {
        this.items = items;
    }
    @ExposedGet(name = "items")
    public PyObject getItems() {
        return new PyList(items);
    }
    @ExposedSet(name = "items")
    public void setItems(PyObject items) {
        this.items = AstAdapters.py2withitemList(items);
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
        return new PyList(body);
    }
    @ExposedSet(name = "body")
    public void setBody(PyObject body) {
        this.body = AstAdapters.py2stmtList(body);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("items"), new PyUnicode("body")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public With() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void With___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("With", args, keywords, new String[]
            {"items", "body", "lineno", "col_offset"}, 2, true);
        setItems(ap.getPyObject(0, Py.None));
        setBody(ap.getPyObject(1, Py.None));
        int lin = ap.getInt(2, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(3, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public With(PyObject items, PyObject body) {
        super(TYPE);
        setItems(items);
        setBody(body);
    }

    // called from derived class
    public With(PyType subtype) {
        super(subtype);
    }

    public With(Token token, java.util.List<withitem> items, java.util.List<stmt> body) {
        super(TYPE, token);
        this.items = items;
        if (items == null) {
            this.items = new ArrayList<>(0);
        }
        for(int i = 0; i < this.items.size(); i++) {
            PythonTree t = this.items.get(i);
            if (t != null)
                t.setParent(this);
        }
        this.body = body;
        if (body == null) {
            this.body = new ArrayList<>(0);
        }
        for(int i = 0; i < this.body.size(); i++) {
            PythonTree t = this.body.get(i);
            addChild(t, i, this.body);
        }
    }

    public With(PythonTree tree, java.util.List<withitem> items, java.util.List<stmt> body) {
        super(TYPE, tree);
        this.items = items;
        if (items == null) {
            this.items = new ArrayList<>(0);
        }
        for(int i = 0; i < this.items.size(); i++) {
            PythonTree t = this.items.get(i);
            if (t != null)
                t.setParent(this);
        }
        this.body = body;
        if (body == null) {
            this.body = new ArrayList<>(0);
        }
        for(int i = 0; i < this.body.size(); i++) {
            PythonTree t = this.body.get(i);
            addChild(t, i, this.body);
        }
    }

    public With copy() {
        return new With(this.getToken(), this.items, this.body);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "With";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("With(");
        sb.append("items=");
        sb.append(dumpThis(items));
        sb.append(",");
        sb.append("body=");
        sb.append(dumpThis(body));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterWith(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveWith(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitWith(this);
    }

    public <R> void traverse(VisitorIF<R> visitor) {
        if (items != null) {
            for (PythonTree t : items) {
                if (t != null)
                    t.accept(visitor);
            }
        }
        if (body != null) {
            for (PythonTree t : body) {
                if (t != null)
                    t.accept(visitor);
            }
        }
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
