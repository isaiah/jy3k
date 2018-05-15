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

@ExposedType(name = "_ast.ExcInfo", base = expr.class)
public class ExcInfo extends expr {
public static final PyType TYPE = PyType.fromClass(ExcInfo.class);

    private final static PyUnicode[] fields = new PyUnicode[0];
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject ExcInfo_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[]
    args, String[] keywords) {
        return new ExcInfo(subtype);
    }
    @ExposedMethod(names={"__init__"})
    public void ExcInfo___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("ExcInfo", args, keywords, new String[]
            {"lineno", "col_offset"}, 0, true);
        PyObject lin = ap.getOptionalArg(0);
        if (lin != null) {
            lineno = lin;
        }

        PyObject col = ap.getOptionalArg(1);
        if (col != null) {
            col_offset = col;
        }

    }

    public ExcInfo() {
        super(TYPE);
    }

    // called from derived class
    public ExcInfo(PyType subtype) {
        super(subtype);
    }

    public ExcInfo(Token token) {
        super(TYPE, token);
    }

    public ExcInfo(PythonTree tree) {
        super(TYPE, tree);
    }

    public ExcInfo copy() {
        return new ExcInfo(this.getToken());
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "ExcInfo";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("ExcInfo(");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterExcInfo(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveExcInfo(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitExcInfo(this);
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
