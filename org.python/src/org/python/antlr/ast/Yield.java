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
import org.python.core.PyNewWrapper;
import org.python.core.Visitproc;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSet;
import org.python.annotations.ExposedType;
import org.python.annotations.ExposedSlot;
import org.python.annotations.SlotFunc;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

@ExposedType(name = "_ast.Yield", base = expr.class)
public class Yield extends expr {
public static final PyType TYPE = PyType.fromClass(Yield.class);
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


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("value")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public Yield() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject Yield_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[]
    args, String[] keywords) {
        return new Yield(subtype);
    }
    @ExposedMethod(names={"__init__"})
    public void Yield___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("Yield", args, keywords, new String[]
            {"value", "lineno", "col_offset"}, 1, true);
        setValue(ap.getPyObject(0, Py.None));
        int lin = ap.getInt(1, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(2, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public Yield(PyObject value) {
        super(TYPE);
        setValue(value);
    }

    // called from derived class
    public Yield(PyType subtype) {
        super(subtype);
    }

    public Yield(Token token, expr value) {
        super(TYPE, token);
        this.value = value;
        if (this.value != null)
            this.value.setParent(this);
    }

    public Yield(PythonTree tree, expr value) {
        super(TYPE, tree);
        this.value = value;
        if (this.value != null)
            this.value.setParent(this);
    }

    public Yield copy() {
        return new Yield(this.getToken(), this.value);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "Yield";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("Yield(");
        sb.append("value=");
        sb.append(dumpThis(value));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterYield(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveYield(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitYield(this);
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
