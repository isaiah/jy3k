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
import org.python.core.Visitproc;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSet;
import org.python.annotations.ExposedType;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

@ExposedType(name = "_ast.Assert", base = stmt.class)
public class Assert extends stmt {
public static final PyType TYPE = PyType.fromClass(Assert.class);
    private expr test;
    public expr getInternalTest() {
        return test;
    }
    public void setInternalTest(expr test) {
        this.test = test;
    }
    @ExposedGet(name = "test")
    public PyObject getTest() {
        return test;
    }
    @ExposedSet(name = "test")
    public void setTest(PyObject test) {
        this.test = AstAdapters.py2expr(test);
    }

    private expr msg;
    public expr getInternalMsg() {
        return msg;
    }
    public void setInternalMsg(expr msg) {
        this.msg = msg;
    }
    @ExposedGet(name = "msg")
    public PyObject getMsg() {
        return msg;
    }
    @ExposedSet(name = "msg")
    public void setMsg(PyObject msg) {
        this.msg = AstAdapters.py2expr(msg);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("test"), new PyUnicode("msg")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public Assert() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void Assert___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("Assert", args, keywords, new String[]
            {"test", "msg", "lineno", "col_offset"}, 2, true);
        setTest(ap.getPyObject(0, Py.None));
        setMsg(ap.getPyObject(1, Py.None));
        int lin = ap.getInt(2, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(3, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public Assert(PyObject test, PyObject msg) {
        super(TYPE);
        setTest(test);
        setMsg(msg);
    }

    // called from derived class
    public Assert(PyType subtype) {
        super(subtype);
    }

    public Assert(Token token, expr test, expr msg) {
        super(TYPE, token);
        this.test = test;
        if (this.test != null)
            this.test.setParent(this);
        this.msg = msg;
        if (this.msg != null)
            this.msg.setParent(this);
    }

    public Assert(PythonTree tree, expr test, expr msg) {
        super(TYPE, tree);
        this.test = test;
        if (this.test != null)
            this.test.setParent(this);
        this.msg = msg;
        if (this.msg != null)
            this.msg.setParent(this);
    }

    public Assert copy() {
        return new Assert(this.getToken(), this.test, this.msg);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "Assert";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("Assert(");
        sb.append("test=");
        sb.append(dumpThis(test));
        sb.append(",");
        sb.append("msg=");
        sb.append(dumpThis(msg));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterAssert(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveAssert(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitAssert(this);
    }

    public <R> void traverse(VisitorIF<R> visitor) {
        if (test != null)
            test.accept(visitor);
        if (msg != null)
            msg.accept(visitor);
    }

    public void replaceField(expr value, expr newValue) {
        if (value == test) this.test = newValue;
        if (value == msg) this.msg = newValue;
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
