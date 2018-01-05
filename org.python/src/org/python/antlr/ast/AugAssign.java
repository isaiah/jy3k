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

@ExposedType(name = "_ast.AugAssign", base = stmt.class)
public class AugAssign extends stmt {
public static final PyType TYPE = PyType.fromClass(AugAssign.class);
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
    new PyUnicode[] {new PyUnicode("target"), new PyUnicode("op"), new PyUnicode("value")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public AugAssign() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void AugAssign___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("AugAssign", args, keywords, new String[]
            {"target", "op", "value", "lineno", "col_offset"}, 3, true);
        setTarget(ap.getPyObject(0, Py.None));
        setOp(ap.getPyObject(1, Py.None));
        setValue(ap.getPyObject(2, Py.None));
        int lin = ap.getInt(3, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(4, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public AugAssign(PyObject target, PyObject op, PyObject value) {
        super(TYPE);
        setTarget(target);
        setOp(op);
        setValue(value);
    }

    // called from derived class
    public AugAssign(PyType subtype) {
        super(subtype);
    }

    public AugAssign(Token token, expr target, operatorType op, expr value) {
        super(TYPE, token);
        this.target = target;
        if (this.target != null)
            this.target.setParent(this);
        this.op = op;
        this.value = value;
        if (this.value != null)
            this.value.setParent(this);
    }

    public AugAssign(PythonTree tree, expr target, operatorType op, expr value) {
        super(TYPE, tree);
        this.target = target;
        if (this.target != null)
            this.target.setParent(this);
        this.op = op;
        this.value = value;
        if (this.value != null)
            this.value.setParent(this);
    }

    public AugAssign copy() {
        return new AugAssign(this.getToken(), this.target, this.op, this.value);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "AugAssign";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("AugAssign(");
        sb.append("target=");
        sb.append(dumpThis(target));
        sb.append(",");
        sb.append("op=");
        sb.append(dumpThis(op));
        sb.append(",");
        sb.append("value=");
        sb.append(dumpThis(value));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterAugAssign(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveAugAssign(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitAugAssign(this);
    }

    public <R> void traverse(VisitorIF<R> visitor) {
        if (target != null)
            target.accept(visitor);
        if (value != null)
            value.accept(visitor);
    }

    public void replaceField(expr value, expr newValue) {
        if (value == target) this.target = newValue;
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