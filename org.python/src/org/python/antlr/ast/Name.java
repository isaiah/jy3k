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

@ExposedType(name = "_ast.Name", base = expr.class)
public class Name extends expr implements Context {
public static final PyType TYPE = PyType.fromClass(Name.class);
    private String id;
    public String getInternalId() {
        return id;
    }
    public void setInternalId(String id) {
        this.id = id;
    }
    @ExposedGet(name = "id")
    public PyObject getId() {
        if (id == null) return Py.None;
        return new PyUnicode(id);
    }
    @ExposedSet(name = "id")
    public void setId(PyObject id) {
        this.id = AstAdapters.py2identifier(id);
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
    new PyUnicode[] {new PyUnicode("id"), new PyUnicode("ctx")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public Name() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject Name_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[]
    args, String[] keywords) {
        return new Name(subtype);
    }
    @ExposedMethod(names={"__init__"})
    public void Name___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("Name", args, keywords, new String[]
            {"id", "ctx", "lineno", "col_offset"}, 2, true);
        setId(ap.getPyObject(0, Py.None));
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

    public Name(PyObject id, PyObject ctx) {
        super(TYPE);
        setId(id);
        setCtx(ctx);
    }

    // called from derived class
    public Name(PyType subtype) {
        super(subtype);
    }

    public Name(Token token, String id, expr_contextType ctx) {
        super(TYPE, token);
        this.id = id;
        this.ctx = ctx;
    }

    public Name(PythonTree tree, String id, expr_contextType ctx) {
        super(TYPE, tree);
        this.id = id;
        this.ctx = ctx;
    }

    public Name copy() {
        return new Name(this.getToken(), this.id, this.ctx);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "Name";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("Name(");
        sb.append("id=");
        sb.append(dumpThis(id));
        sb.append(",");
        sb.append("ctx=");
        sb.append(dumpThis(ctx));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterName(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveName(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitName(this);
    }

    public <R> void traverse(VisitorIF<R> visitor) {
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


    private boolean Expr;

    public boolean isExpr() {
        return Expr;
    }

    public void setExpr(boolean Expr) {
        this.Expr = Expr;
    }
}
