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

@ExposedType(name = "_ast.Expression", base = mod.class)
public class Expression extends mod {
public static final PyType TYPE = PyType.fromClass(Expression.class);
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
    new PyUnicode[] {new PyUnicode("body")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes = new PyUnicode[0];
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public Expression() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void Expression___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("Expression", args, keywords, new String[]
            {"body"}, 1, true);
        setBody(ap.getPyObject(0, Py.None));
    }

    public Expression(PyObject body) {
        super(TYPE);
        setBody(body);
    }

    // called from derived class
    public Expression(PyType subtype) {
        super(subtype);
    }

    public Expression(Token token, expr body) {
        super(TYPE, token);
        this.body = body;
        if (this.body != null)
            this.body.setParent(this);
    }

    public Expression(PythonTree tree, expr body) {
        super(TYPE, tree);
        this.body = body;
        if (this.body != null)
            this.body.setParent(this);
    }

    public Expression copy() {
        return new Expression(this.getToken(), this.body);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "Expression";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("Expression(");
        sb.append("body=");
        sb.append(dumpThis(body));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterExpression(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveExpression(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitExpression(this);
    }

    public void traverse(VisitorIF<?> visitor) {
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

}
