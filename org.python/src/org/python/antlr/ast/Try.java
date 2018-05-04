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

@ExposedType(name = "_ast.Try", base = stmt.class)
public class Try extends stmt {
public static final PyType TYPE = PyType.fromClass(Try.class);
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

    private java.util.List<excepthandler> handlers;
    public java.util.List<excepthandler> getInternalHandlers() {
        return handlers;
    }
    public void setInternalHandlers(java.util.List<excepthandler> handlers) {
        this.handlers = handlers;
    }
    @ExposedGet(name = "handlers")
    public PyObject getHandlers() {
        return new PyList(handlers);
    }
    @ExposedSet(name = "handlers")
    public void setHandlers(PyObject handlers) {
        this.handlers = AstAdapters.py2excepthandlerList(handlers);
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
        return new PyList(orelse);
    }
    @ExposedSet(name = "orelse")
    public void setOrelse(PyObject orelse) {
        this.orelse = AstAdapters.py2stmtList(orelse);
    }

    private java.util.List<stmt> finalbody;
    public java.util.List<stmt> getInternalFinalbody() {
        return finalbody;
    }
    public void setInternalFinalbody(java.util.List<stmt> finalbody) {
        this.finalbody = finalbody;
    }
    @ExposedGet(name = "finalbody")
    public PyObject getFinalbody() {
        return new PyList(finalbody);
    }
    @ExposedSet(name = "finalbody")
    public void setFinalbody(PyObject finalbody) {
        this.finalbody = AstAdapters.py2stmtList(finalbody);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("body"), new PyUnicode("handlers"), new PyUnicode("orelse"), new
                      PyUnicode("finalbody")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public Try() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void Try___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("Try", args, keywords, new String[]
            {"body", "handlers", "orelse", "finalbody", "lineno", "col_offset"}, 4, true);
        setBody(ap.getPyObject(0, Py.None));
        setHandlers(ap.getPyObject(1, Py.None));
        setOrelse(ap.getPyObject(2, Py.None));
        setFinalbody(ap.getPyObject(3, Py.None));
        int lin = ap.getInt(4, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(5, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public Try(PyObject body, PyObject handlers, PyObject orelse, PyObject finalbody) {
        super(TYPE);
        setBody(body);
        setHandlers(handlers);
        setOrelse(orelse);
        setFinalbody(finalbody);
    }

    // called from derived class
    public Try(PyType subtype) {
        super(subtype);
    }

    public Try(Token token, java.util.List<stmt> body, java.util.List<excepthandler> handlers,
    java.util.List<stmt> orelse, java.util.List<stmt> finalbody) {
        super(TYPE, token);
        this.body = body;
        if (body == null) {
            this.body = new ArrayList<>(0);
        }
        for(int i = 0; i < this.body.size(); i++) {
            PythonTree t = this.body.get(i);
            addChild(t, i, this.body);
        }
        this.handlers = handlers;
        if (handlers == null) {
            this.handlers = new ArrayList<>(0);
        }
        for(int i = 0; i < this.handlers.size(); i++) {
            PythonTree t = this.handlers.get(i);
            if (t != null)
                t.setParent(this);
        }
        this.orelse = orelse;
        if (orelse == null) {
            this.orelse = new ArrayList<>(0);
        }
        for(int i = 0; i < this.orelse.size(); i++) {
            PythonTree t = this.orelse.get(i);
            addChild(t, i, this.orelse);
        }
        this.finalbody = finalbody;
        if (finalbody == null) {
            this.finalbody = new ArrayList<>(0);
        }
        for(int i = 0; i < this.finalbody.size(); i++) {
            PythonTree t = this.finalbody.get(i);
            addChild(t, i, this.finalbody);
        }
    }

    public Try(PythonTree tree, java.util.List<stmt> body, java.util.List<excepthandler> handlers,
    java.util.List<stmt> orelse, java.util.List<stmt> finalbody) {
        super(TYPE, tree);
        this.body = body;
        if (body == null) {
            this.body = new ArrayList<>(0);
        }
        for(int i = 0; i < this.body.size(); i++) {
            PythonTree t = this.body.get(i);
            addChild(t, i, this.body);
        }
        this.handlers = handlers;
        if (handlers == null) {
            this.handlers = new ArrayList<>(0);
        }
        for(int i = 0; i < this.handlers.size(); i++) {
            PythonTree t = this.handlers.get(i);
            if (t != null)
                t.setParent(this);
        }
        this.orelse = orelse;
        if (orelse == null) {
            this.orelse = new ArrayList<>(0);
        }
        for(int i = 0; i < this.orelse.size(); i++) {
            PythonTree t = this.orelse.get(i);
            addChild(t, i, this.orelse);
        }
        this.finalbody = finalbody;
        if (finalbody == null) {
            this.finalbody = new ArrayList<>(0);
        }
        for(int i = 0; i < this.finalbody.size(); i++) {
            PythonTree t = this.finalbody.get(i);
            addChild(t, i, this.finalbody);
        }
    }

    public Try copy() {
        return new Try(this.getToken(), this.body, this.handlers, this.orelse, this.finalbody);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "Try";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("Try(");
        sb.append("body=");
        sb.append(dumpThis(body));
        sb.append(",");
        sb.append("handlers=");
        sb.append(dumpThis(handlers));
        sb.append(",");
        sb.append("orelse=");
        sb.append(dumpThis(orelse));
        sb.append(",");
        sb.append("finalbody=");
        sb.append(dumpThis(finalbody));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterTry(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveTry(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitTry(this);
    }

    public <R> void traverse(VisitorIF<R> visitor) {
        if (body != null) {
            for (PythonTree t : body) {
                if (t != null)
                    t.accept(visitor);
            }
        }
        if (handlers != null) {
            for (PythonTree t : handlers) {
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
        if (finalbody != null) {
            for (PythonTree t : finalbody) {
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
