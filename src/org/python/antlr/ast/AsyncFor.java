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

@ExposedType(name = "_ast.AsyncFor", base = stmt.class)
public class AsyncFor extends stmt {
public static final PyType TYPE = PyType.fromClass(AsyncFor.class);
    private expr target;
    public expr getInternalTarget() {
        return target;
    }
    public void setInternalTarget(expr target) {
        this.target = target;
    }
    @ExposedGet(name = "target")
    public PyObject getTarget() {
        return target;
    }
    @ExposedSet(name = "target")
    public void setTarget(PyObject target) {
        this.target = AstAdapters.py2expr(target);
    }

    private expr iter;
    public expr getInternalIter() {
        return iter;
    }
    public void setInternalIter(expr iter) {
        this.iter = iter;
    }
    @ExposedGet(name = "iter")
    public PyObject getIter() {
        return iter;
    }
    @ExposedSet(name = "iter")
    public void setIter(PyObject iter) {
        this.iter = AstAdapters.py2expr(iter);
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
    new PyUnicode[] {new PyUnicode("target"), new PyUnicode("iter"), new PyUnicode("body"), new
                      PyUnicode("orelse")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public AsyncFor() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void AsyncFor___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("AsyncFor", args, keywords, new String[]
            {"target", "iter", "body", "orelse", "lineno", "col_offset"}, 4, true);
        setTarget(ap.getPyObject(0, Py.None));
        setIter(ap.getPyObject(1, Py.None));
        setBody(ap.getPyObject(2, Py.None));
        setOrelse(ap.getPyObject(3, Py.None));
        int lin = ap.getInt(4, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(5, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public AsyncFor(PyObject target, PyObject iter, PyObject body, PyObject orelse) {
        super(TYPE);
        setTarget(target);
        setIter(iter);
        setBody(body);
        setOrelse(orelse);
    }

    // called from derived class
    public AsyncFor(PyType subtype) {
        super(subtype);
    }

    public AsyncFor(Token token, expr target, expr iter, java.util.List<stmt> body,
    java.util.List<stmt> orelse) {
        super(TYPE, token);
        this.target = target;
        if (this.target != null)
            this.target.setParent(this);
        this.iter = iter;
        if (this.iter != null)
            this.iter.setParent(this);
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

    public AsyncFor(PythonTree tree, expr target, expr iter, java.util.List<stmt> body,
    java.util.List<stmt> orelse) {
        super(TYPE, tree);
        this.target = target;
        if (this.target != null)
            this.target.setParent(this);
        this.iter = iter;
        if (this.iter != null)
            this.iter.setParent(this);
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

    public AsyncFor copy() {
        return new AsyncFor(this.getToken(), this.target, this.iter, this.body, this.orelse);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "AsyncFor";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("AsyncFor(");
        sb.append("target=");
        sb.append(dumpThis(target));
        sb.append(",");
        sb.append("iter=");
        sb.append(dumpThis(iter));
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
        return visitor.enterAsyncFor(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveAsyncFor(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitAsyncFor(this);
    }

    public void traverse(VisitorIF<?> visitor) {
        if (target != null)
            target.accept(visitor);
        if (iter != null)
            iter.accept(visitor);
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

    public void replaceField(expr value, expr newValue) {
        if (value == target) this.target = newValue;
        if (value == iter) this.iter = newValue;
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
