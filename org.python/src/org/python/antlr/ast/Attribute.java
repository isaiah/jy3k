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

@ExposedType(name = "_ast.Attribute", base = expr.class)
public class Attribute extends expr implements Context {
public static final PyType TYPE = PyType.fromClass(Attribute.class);
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

    private String attr;
    public String getInternalAttr() {
        return attr;
    }
    public void setInternalAttr(String attr) {
        this.attr = attr;
    }
    @ExposedGet(name = "attr")
    public PyObject getAttr() {
        if (attr == null) return Py.None;
        return new PyUnicode(attr);
    }
    @ExposedSet(name = "attr")
    public void setAttr(PyObject attr) {
        this.attr = AstAdapters.py2identifier(attr);
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
    new PyUnicode[] {new PyUnicode("value"), new PyUnicode("attr"), new PyUnicode("ctx")};
    @ExposedGet(name = "_fields")
    public PyObject get_fields() { return new PyTuple(fields); }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyObject get_attributes() { return new PyTuple(attributes); }

    public Attribute() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject Attribute_new(PyNewWrapper _new, boolean init, PyType subtype,
    PyObject[] args, String[] keywords) {
        return new Attribute(subtype);
    }
    @ExposedMethod(names={"__init__"})
    public void Attribute___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("Attribute", args, keywords, new String[]
            {"value", "attr", "ctx", "lineno", "col_offset"}, 3, true);
        setValue(ap.getPyObject(0, Py.None));
        setAttr(ap.getPyObject(1, Py.None));
        setCtx(ap.getPyObject(2, Py.None));
        PyObject lin = ap.getOptionalArg(3);
        if (lin != null) {
            lineno = lin;
        }

        PyObject col = ap.getOptionalArg(4);
        if (col != null) {
            col_offset = col;
        }

    }

    public Attribute(PyObject value, PyObject attr, PyObject ctx) {
        super(TYPE);
        setValue(value);
        setAttr(attr);
        setCtx(ctx);
    }

    // called from derived class
    public Attribute(PyType subtype) {
        super(subtype);
    }

    public Attribute(Token token, expr value, String attr, expr_contextType ctx) {
        super(TYPE, token);
        this.value = value;
        if (this.value != null)
            this.value.setParent(this);
        this.attr = attr;
        this.ctx = ctx;
    }

    public Attribute(PythonTree tree, expr value, String attr, expr_contextType ctx) {
        super(TYPE, tree);
        this.value = value;
        if (this.value != null)
            this.value.setParent(this);
        this.attr = attr;
        this.ctx = ctx;
    }

    public Attribute copy() {
        return new Attribute(this.getToken(), this.value, this.attr, this.ctx);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "Attribute";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("Attribute(");
        sb.append("value=");
        sb.append(dumpThis(value));
        sb.append(",");
        sb.append("attr=");
        sb.append(dumpThis(attr));
        sb.append(",");
        sb.append("ctx=");
        sb.append(dumpThis(ctx));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterAttribute(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveAttribute(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitAttribute(this);
    }

    public <R> void traverse(VisitorIF<R> visitor) {
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

    public expr_contextType getContext() {
        return this.ctx;
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
