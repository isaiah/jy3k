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

@ExposedType(name = "_ast.AnonymousFunction", base = expr.class)
public class AnonymousFunction extends expr {
public static final PyType TYPE = PyType.fromClass(AnonymousFunction.class);
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

    private java.util.List<stmt> body;
    public java.util.List<stmt> getInternalBody() {
        return body;
    }
    public void setInternalBody(java.util.List<stmt> body) {
        this.body = body;
    }
    @ExposedGet(name = "body")
    public PyObject getBody() {
        return new AstList(body, AstAdapters.stmtAdapter);
    }
    @ExposedSet(name = "body")
    public void setBody(PyObject body) {
        this.body = AstAdapters.py2stmtList(body);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("args"), new PyUnicode("body")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public AnonymousFunction() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void AnonymousFunction___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("AnonymousFunction", args, keywords, new String[]
            {"args", "body", "lineno", "col_offset"}, 2, true);
        setArgs(ap.getPyObject(0, Py.None));
        setBody(ap.getPyObject(1, Py.None));
        int lin = ap.getInt(2, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(3, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public AnonymousFunction(PyObject args, PyObject body) {
        super(TYPE);
        setArgs(args);
        setBody(body);
    }

    // called from derived class
    public AnonymousFunction(PyType subtype) {
        super(subtype);
    }

    public AnonymousFunction(Token token, arguments args, java.util.List<stmt> body) {
        super(TYPE, token);
        this.args = args;
        if (this.args != null)
            this.args.setParent(this);
        this.body = body;
        if (body == null) {
            this.body = new ArrayList<>(0);
        }
        for(int i = 0; i < this.body.size(); i++) {
            PythonTree t = this.body.get(i);
            addChild(t, i, this.body);
        }
    }

    public AnonymousFunction(PythonTree tree, arguments args, java.util.List<stmt> body) {
        super(TYPE, tree);
        this.args = args;
        if (this.args != null)
            this.args.setParent(this);
        this.body = body;
        if (body == null) {
            this.body = new ArrayList<>(0);
        }
        for(int i = 0; i < this.body.size(); i++) {
            PythonTree t = this.body.get(i);
            addChild(t, i, this.body);
        }
    }

    public AnonymousFunction copy() {
        return new AnonymousFunction(this.getToken(), this.args, this.body);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "AnonymousFunction";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("AnonymousFunction(");
        sb.append("args=");
        sb.append(dumpThis(args));
        sb.append(",");
        sb.append("body=");
        sb.append(dumpThis(body));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitAnonymousFunction(this);
    }

    public void traverse(VisitorIF<?> visitor) {
        if (args != null)
            args.accept(visitor);
        if (body != null) {
            for (PythonTree t : body) {
                if (t != null)
                    t.accept(visitor);
            }
        }
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
