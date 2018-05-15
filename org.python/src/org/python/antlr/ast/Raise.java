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

@ExposedType(name = "_ast.Raise", base = stmt.class)
public class Raise extends stmt {
public static final PyType TYPE = PyType.fromClass(Raise.class);
    private expr exc;
    public expr getInternalExc() {
        return exc;
    }
    public void setInternalExc(expr exc) {
        this.exc = exc;
    }
    @ExposedGet(name = "exc")
    public PyObject getExc() {
        return exc;
    }
    @ExposedSet(name = "exc")
    public void setExc(PyObject exc) {
        this.exc = AstAdapters.py2expr(exc);
    }

    private expr cause;
    public expr getInternalCause() {
        return cause;
    }
    public void setInternalCause(expr cause) {
        this.cause = cause;
    }
    @ExposedGet(name = "cause")
    public PyObject getCause() {
        return cause;
    }
    @ExposedSet(name = "cause")
    public void setCause(PyObject cause) {
        this.cause = AstAdapters.py2expr(cause);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("exc"), new PyUnicode("cause")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public Raise() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject Raise_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[]
    args, String[] keywords) {
        return new Raise(subtype);
    }
    @ExposedMethod(names={"__init__"})
    public void Raise___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("Raise", args, keywords, new String[]
            {"exc", "cause", "lineno", "col_offset"}, 2, true);
        setExc(ap.getPyObject(0, Py.None));
        setCause(ap.getPyObject(1, Py.None));
        PyObject lin = ap.getOptionalArg(2);
        if (lin != null) {
            lineno = lin;
        }

        PyObject col = ap.getOptionalArg(3);
        if (col != null) {
            col_offset = col;
        }

    }

    public Raise(PyObject exc, PyObject cause) {
        super(TYPE);
        setExc(exc);
        setCause(cause);
    }

    // called from derived class
    public Raise(PyType subtype) {
        super(subtype);
    }

    public Raise(Token token, expr exc, expr cause) {
        super(TYPE, token);
        this.exc = exc;
        if (this.exc != null)
            this.exc.setParent(this);
        this.cause = cause;
        if (this.cause != null)
            this.cause.setParent(this);
    }

    public Raise(PythonTree tree, expr exc, expr cause) {
        super(TYPE, tree);
        this.exc = exc;
        if (this.exc != null)
            this.exc.setParent(this);
        this.cause = cause;
        if (this.cause != null)
            this.cause.setParent(this);
    }

    public Raise copy() {
        return new Raise(this.getToken(), this.exc, this.cause);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "Raise";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("Raise(");
        sb.append("exc=");
        sb.append(dumpThis(exc));
        sb.append(",");
        sb.append("cause=");
        sb.append(dumpThis(cause));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterRaise(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveRaise(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitRaise(this);
    }

    public <R> void traverse(VisitorIF<R> visitor) {
        if (exc != null)
            exc.accept(visitor);
        if (cause != null)
            cause.accept(visitor);
    }

    public void replaceField(expr value, expr newValue) {
        if (value == exc) this.exc = newValue;
        if (value == cause) this.cause = newValue;
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
