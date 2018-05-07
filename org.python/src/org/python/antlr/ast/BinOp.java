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

@ExposedType(name = "_ast.BinOp", base = expr.class)
public class BinOp extends expr {
public static final PyType TYPE = PyType.fromClass(BinOp.class);
    private expr left;
    public expr getInternalLeft() {
        return left;
    }
    public void setInternalLeft(expr left) {
        this.left = left;
    }
    @ExposedGet(name = "left")
    public PyObject getLeft() {
        return left;
    }
    @ExposedSet(name = "left")
    public void setLeft(PyObject left) {
        this.left = AstAdapters.py2expr(left);
    }

    private operatorType op;
    public operatorType getInternalOp() {
        return op;
    }
    public void setInternalOp(operatorType op) {
        this.op = op;
    }
    @ExposedGet(name = "op")
    public PyObject getOp() {
        return AstAdapters.operator2py(op);
    }
    @ExposedSet(name = "op")
    public void setOp(PyObject op) {
        this.op = AstAdapters.py2operator(op);
    }

    private expr right;
    public expr getInternalRight() {
        return right;
    }
    public void setInternalRight(expr right) {
        this.right = right;
    }
    @ExposedGet(name = "right")
    public PyObject getRight() {
        return right;
    }
    @ExposedSet(name = "right")
    public void setRight(PyObject right) {
        this.right = AstAdapters.py2expr(right);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("left"), new PyUnicode("op"), new PyUnicode("right")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public BinOp() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject BinOp_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[]
    args, String[] keywords) {
        return new BinOp(subtype);
    }
    @ExposedMethod(names={"__init__"})
    public void BinOp___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("BinOp", args, keywords, new String[]
            {"left", "op", "right", "lineno", "col_offset"}, 3, true);
        setLeft(ap.getPyObject(0, Py.None));
        setOp(ap.getPyObject(1, Py.None));
        setRight(ap.getPyObject(2, Py.None));
        int lin = ap.getInt(3, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(4, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public BinOp(PyObject left, PyObject op, PyObject right) {
        super(TYPE);
        setLeft(left);
        setOp(op);
        setRight(right);
    }

    // called from derived class
    public BinOp(PyType subtype) {
        super(subtype);
    }

    public BinOp(Token token, expr left, operatorType op, expr right) {
        super(TYPE, token);
        this.left = left;
        if (this.left != null)
            this.left.setParent(this);
        this.op = op;
        this.right = right;
        if (this.right != null)
            this.right.setParent(this);
    }

    public BinOp(PythonTree tree, expr left, operatorType op, expr right) {
        super(TYPE, tree);
        this.left = left;
        if (this.left != null)
            this.left.setParent(this);
        this.op = op;
        this.right = right;
        if (this.right != null)
            this.right.setParent(this);
    }

    public BinOp copy() {
        return new BinOp(this.getToken(), this.left, this.op, this.right);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "BinOp";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("BinOp(");
        sb.append("left=");
        sb.append(dumpThis(left));
        sb.append(",");
        sb.append("op=");
        sb.append(dumpThis(op));
        sb.append(",");
        sb.append("right=");
        sb.append(dumpThis(right));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterBinOp(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveBinOp(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitBinOp(this);
    }

    public <R> void traverse(VisitorIF<R> visitor) {
        if (left != null)
            left.accept(visitor);
        if (right != null)
            right.accept(visitor);
    }

    public void replaceField(expr value, expr newValue) {
        if (value == left) this.left = newValue;
        if (value == right) this.right = newValue;
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


    private boolean Inplace;

    public boolean isInplace() {
        return Inplace;
    }

    public void setInplace(boolean Inplace) {
        this.Inplace = Inplace;
    }
}
