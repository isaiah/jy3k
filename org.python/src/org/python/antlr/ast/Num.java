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

@ExposedType(name = "_ast.Num", base = expr.class)
public class Num extends expr {
public static final PyType TYPE = PyType.fromClass(Num.class);
    private PyObject n;
    public PyObject getInternalN() {
        return n;
    }
    public void setInternalN(PyObject n) {
        this.n = n;
    }
    @ExposedGet(name = "n")
    public PyObject getN() {
        return n;
    }
    @ExposedSet(name = "n")
    public void setN(PyObject n) {
        this.n = n;
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("n")};
    @ExposedGet(name = "_fields")
    public PyObject get_fields() { return new PyTuple(fields); }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyObject get_attributes() { return new PyTuple(attributes); }

    public Num() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject Num_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[]
    args, String[] keywords) {
        return new Num(subtype);
    }
    @ExposedMethod(names={"__init__"})
    public void Num___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("Num", args, keywords, new String[]
            {"n", "lineno", "col_offset"}, 1, true);
        setN(ap.getPyObject(0, Py.None));
        PyObject lin = ap.getOptionalArg(1);
        if (lin != null) {
            lineno = lin;
        }

        PyObject col = ap.getOptionalArg(2);
        if (col != null) {
            col_offset = col;
        }

    }

    public Num(PyObject n) {
        super(TYPE);
        setN(n);
    }

    // called from derived class
    public Num(PyType subtype) {
        super(subtype);
    }

    public Num(Token token, PyObject n) {
        super(TYPE, token);
        this.n = n;
    }

    public Num(PythonTree tree, PyObject n) {
        super(TYPE, tree);
        this.n = n;
    }

    public Num copy() {
        return new Num(this.getToken(), this.n);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "Num";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("Num(");
        sb.append("n=");
        sb.append(dumpThis(n));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterNum(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveNum(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitNum(this);
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
