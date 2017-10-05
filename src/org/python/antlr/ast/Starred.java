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

@ExposedType(name = "_ast.Starred", base = expr.class)
public class Starred extends expr implements Context {
public static final PyType TYPE = PyType.fromClass(Starred.class);
    private expr value;
    public expr getInternalValue() {
        return value;
    }
    public void setInternalValue(expr value) {
        this.value = value;
    }
    @ExposedGet(name = "value")
    public PyObject getValue() {
        return value;
    }
    @ExposedSet(name = "value")
    public void setValue(PyObject value) {
        this.value = AstAdapters.py2expr(value);
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
    new PyUnicode[] {new PyUnicode("value"), new PyUnicode("ctx")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public Starred() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void Starred___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("Starred", args, keywords, new String[]
            {"value", "ctx", "lineno", "col_offset"}, 2, true);
        setValue(ap.getPyObject(0, Py.None));
        setCtx(ap.getPyObject(1, Py.None));
        int lin = ap.getInt(2, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(3, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public Starred(PyObject value, PyObject ctx) {
        super(TYPE);
        setValue(value);
        setCtx(ctx);
    }

    // called from derived class
    public Starred(PyType subtype) {
        super(subtype);
    }

    public Starred(Token token, expr value, expr_contextType ctx) {
        super(TYPE, token);
        this.value = value;
        if (this.value != null)
            this.value.setParent(this);
        this.ctx = ctx;
    }

    public Starred(PythonTree tree, expr value, expr_contextType ctx) {
        super(TYPE, tree);
        this.value = value;
        if (this.value != null)
            this.value.setParent(this);
        this.ctx = ctx;
    }

    public Starred copy() {
        return new Starred(this.getToken(), this.value, this.ctx);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "Starred";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("Starred(");
        sb.append("value=");
        sb.append(dumpThis(value));
        sb.append(",");
        sb.append("ctx=");
        sb.append(dumpThis(ctx));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterStarred(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveStarred(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitStarred(this);
    }

    public void traverse(VisitorIF<?> visitor) {
        if (value != null)
            value.accept(visitor);
    }

    public void replaceField(expr value, expr newValue) {
        if (value == value) this.value = newValue;
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
