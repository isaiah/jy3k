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

@ExposedType(name = "_ast.Lambda", base = expr.class)
public class Lambda extends expr {
public static final PyType TYPE = PyType.fromClass(Lambda.class);
    private arguments args;
    public arguments getInternalArgs() {
        return args;
    }
    public void setInternalArgs(arguments args) {
        this.args = args;
    }
    @ExposedGet(name = "args")
    public PyObject getArgs() {
        return args;
    }
    @ExposedSet(name = "args")
    public void setArgs(PyObject args) {
        this.args = AstAdapters.py2arguments(args);
    }

    private expr body;
    public expr getInternalBody() {
        return body;
    }
    public void setInternalBody(expr body) {
        this.body = body;
    }
    @ExposedGet(name = "body")
    public PyObject getBody() {
        return body;
    }
    @ExposedSet(name = "body")
    public void setBody(PyObject body) {
        this.body = AstAdapters.py2expr(body);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("args"), new PyUnicode("body")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public Lambda() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject Lambda_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[]
    args, String[] keywords) {
        return new Lambda(subtype);
    }
    @ExposedMethod(names={"__init__"})
    public void Lambda___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("Lambda", args, keywords, new String[]
            {"args", "body", "lineno", "col_offset"}, 2, true);
        setArgs(ap.getPyObject(0, Py.None));
        setBody(ap.getPyObject(1, Py.None));
        PyObject lin = ap.getOptionalArg(2);
        if (lin != null) {
            lineno = lin;
        }

        PyObject col = ap.getOptionalArg(3);
        if (col != null) {
            col_offset = col;
        }

    }

    public Lambda(PyObject args, PyObject body) {
        super(TYPE);
        setArgs(args);
        setBody(body);
    }

    // called from derived class
    public Lambda(PyType subtype) {
        super(subtype);
    }

    public Lambda(Token token, arguments args, expr body) {
        super(TYPE, token);
        this.args = args;
        if (this.args != null)
            this.args.setParent(this);
        this.body = body;
        if (this.body != null)
            this.body.setParent(this);
    }

    public Lambda(PythonTree tree, arguments args, expr body) {
        super(TYPE, tree);
        this.args = args;
        if (this.args != null)
            this.args.setParent(this);
        this.body = body;
        if (this.body != null)
            this.body.setParent(this);
    }

    public Lambda copy() {
        return new Lambda(this.getToken(), this.args, this.body);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "Lambda";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("Lambda(");
        sb.append("args=");
        sb.append(dumpThis(args));
        sb.append(",");
        sb.append("body=");
        sb.append(dumpThis(body));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterLambda(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveLambda(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitLambda(this);
    }

    public <R> void traverse(VisitorIF<R> visitor) {
        if (args != null)
            args.accept(visitor);
        if (body != null)
            body.accept(visitor);
    }

    public void replaceField(expr value, expr newValue) {
        if (value == body) this.body = newValue;
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
