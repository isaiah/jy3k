// Autogenerated AST node
package org.python.antlr.ast;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.python.antlr.AST;
import org.python.antlr.ast.VisitorIF;
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
import org.python.core.PyTuple;
import org.python.core.PyStringMap;
import org.python.core.PyLong;
import org.python.core.PyType;
import org.python.core.PyList;
import org.python.core.PyNewWrapper;
import org.python.core.Visitproc;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSet;
import org.python.annotations.ExposedType;
import org.python.annotations.ExposedSlot;
import org.python.annotations.SlotFunc;
import java.util.Objects;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

@ExposedType(name = "_ast.List", base = expr.class)
public class List extends expr implements Context {
public static final PyType TYPE = PyType.fromClass(List.class);
    private java.util.List<expr> elts;
    public java.util.List<expr> getInternalElts() {
        return elts;
    }
    public void setInternalElts(java.util.List<expr> elts) {
        this.elts = elts;
    }
    @ExposedGet(name = "elts")
    public PyObject getElts() {
        return new PyList(elts);
    }
    @ExposedSet(name = "elts")
    public void setElts(PyObject elts) {
        this.elts = AstAdapters.py2exprList(elts);
    }

    private expr_contextType ctx;
    public expr_contextType getInternalCtx() {
        return ctx;
    }
    public void setInternalCtx(expr_contextType ctx) {
        this.ctx = ctx;
    }
    @ExposedGet(name = "ctx")
    public PyObject getCtx() {
        return AstAdapters.expr_context2py(ctx);
    }
    @ExposedSet(name = "ctx")
    public void setCtx(PyObject ctx) {
        this.ctx = AstAdapters.py2expr_context(ctx);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("elts"), new PyUnicode("ctx")};
    @ExposedGet(name = "_fields")
    public PyObject get_fields() { return new PyTuple(fields); }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyObject get_attributes() { return new PyTuple(attributes); }

    public List() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject List_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[]
    args, String[] keywords) {
        return new List(subtype);
    }
    @ExposedMethod(names={"__init__"})
    public void List___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("List", args, keywords, new String[]
            {"elts", "ctx", "lineno", "col_offset"}, 2, true);
        setElts(ap.getPyObject(0, Py.None));
        setCtx(ap.getPyObject(1, Py.None));
        PyObject lin = ap.getOptionalArg(2);
        if (lin != null) {
            lineno = lin;
        }

        PyObject col = ap.getOptionalArg(3);
        if (col != null) {
            col_offset = col;
        }

    }

    public List(PyObject elts, PyObject ctx) {
        super(TYPE);
        setElts(elts);
        setCtx(ctx);
    }

    // called from derived class
    public List(PyType subtype) {
        super(subtype);
    }

    public List(Token token, java.util.List<expr> elts, expr_contextType ctx) {
        super(TYPE, token);
        this.elts = elts;
        if (elts == null) {
            this.elts = new ArrayList<>(0);
        }
        for(int i = 0; i < this.elts.size(); i++) {
            PythonTree t = this.elts.get(i);
            if (t != null)
                t.setParent(this);
        }
        this.ctx = ctx;
    }

    public List(PythonTree tree, java.util.List<expr> elts, expr_contextType ctx) {
        super(TYPE, tree);
        this.elts = elts;
        if (elts == null) {
            this.elts = new ArrayList<>(0);
        }
        for(int i = 0; i < this.elts.size(); i++) {
            PythonTree t = this.elts.get(i);
            if (t != null)
                t.setParent(this);
        }
        this.ctx = ctx;
    }

    public List copy() {
        return new List(this.getToken(), this.elts, this.ctx);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "List";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("List(");
        sb.append("elts=");
        sb.append(dumpThis(elts));
        sb.append(",");
        sb.append("ctx=");
        sb.append(dumpThis(ctx));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterList(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveList(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitList(this);
    }

    public <R> void traverse(VisitorIF<R> visitor) {
        if (elts != null) {
            for (PythonTree t : elts) {
                if (t != null)
                    t.accept(visitor);
            }
        }
    }

    public void replaceField(expr value, expr newValue) {
        for (int i=0;i<this.elts.size();i++){
            expr thisVal = this.elts.get(i);
            if (value == thisVal) this.elts.set(i,newValue);
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

    public void setContext(expr_contextType c) {
        this.ctx = c;
    }

    @ExposedGet(name = "lineno")
    public int getLineno() {
        return super.getLineno();
    }

    @ExposedSet(name = "lineno")
    public void setLineno(int num) {
        lineno = new PyLong(num);
    }

    @ExposedGet(name = "col_offset")
    public int getCol_offset() {
        return super.getCol_offset();
    }

    @ExposedSet(name = "col_offset")
    public void setCol_offset(int num) {
        col_offset = new PyLong(num);
    }

}
