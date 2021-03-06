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
import org.python.parser.Node;
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

@ExposedType(name = "_ast.UnaryOp", base = expr.class)
public class UnaryOp extends expr {
public static final PyType TYPE = PyType.fromClass(UnaryOp.class);
    private unaryopType op;
    public unaryopType getInternalOp() {
        return op;
    }
    public void setInternalOp(unaryopType op) {
        this.op = op;
    }
    @ExposedGet(name = "op")
    public PyObject getOp() {
        return AstAdapters.unaryop2py(op);
    }
    @ExposedSet(name = "op")
    public void setOp(PyObject op) {
        this.op = AstAdapters.py2unaryop(op);
    }

    private expr operand;
    public expr getInternalOperand() {
        return operand;
    }
    public void setInternalOperand(expr operand) {
        this.operand = operand;
    }
    @ExposedGet(name = "operand")
    public PyObject getOperand() {
        return operand;
    }
    @ExposedSet(name = "operand")
    public void setOperand(PyObject operand) {
        this.operand = AstAdapters.py2expr(operand);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("op"), new PyUnicode("operand")};
    @ExposedGet(name = "_fields")
    public PyObject get_fields() { return new PyTuple(fields); }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyObject get_attributes() { return new PyTuple(attributes); }

    public UnaryOp() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject UnaryOp_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[]
    args, String[] keywords) {
        return new UnaryOp(subtype);
    }
    @ExposedMethod(names={"__init__"})
    public void UnaryOp___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("UnaryOp", args, keywords, new String[]
            {"op", "operand", "lineno", "col_offset"}, 2, true);
        setOp(ap.getPyObject(0, Py.None));
        setOperand(ap.getPyObject(1, Py.None));
        PyObject lin = ap.getOptionalArg(2);
        if (lin != null) {
            lineno = lin;
        }

        PyObject col = ap.getOptionalArg(3);
        if (col != null) {
            col_offset = col;
        }

    }

    public UnaryOp(PyObject op, PyObject operand) {
        super(TYPE);
        setOp(op);
        setOperand(operand);
    }

    // called from derived class
    public UnaryOp(PyType subtype) {
        super(subtype);
    }

    public UnaryOp(Node token, unaryopType op, expr operand) {
        super(TYPE, token);
        this.op = op;
        this.operand = operand;
        if (this.operand != null)
            this.operand.setParent(this);
    }

    public UnaryOp(Token token, unaryopType op, expr operand) {
        super(TYPE, token);
        this.op = op;
        this.operand = operand;
        if (this.operand != null)
            this.operand.setParent(this);
    }

    public UnaryOp(PythonTree tree, unaryopType op, expr operand) {
        super(TYPE, tree);
        this.op = op;
        this.operand = operand;
        if (this.operand != null)
            this.operand.setParent(this);
    }

    public UnaryOp copy() {
        return new UnaryOp(this.getToken(), this.op, this.operand);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "UnaryOp";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("UnaryOp(");
        sb.append("op=");
        sb.append(dumpThis(op));
        sb.append(",");
        sb.append("operand=");
        sb.append(dumpThis(operand));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterUnaryOp(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveUnaryOp(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitUnaryOp(this);
    }

    public <R> void traverse(VisitorIF<R> visitor) {
        if (operand != null)
            operand.accept(visitor);
    }

    public void replaceField(expr value, expr newValue) {
        if (value == operand) this.operand = newValue;
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
