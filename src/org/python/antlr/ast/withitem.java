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
import org.python.expose.ExposedGet;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedNew;
import org.python.expose.ExposedSet;
import org.python.expose.ExposedType;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

@ExposedType(name = "_ast.withitem", base = AST.class)
public class withitem extends PythonTree {
    public static final PyType TYPE = PyType.fromClass(withitem.class);
    private expr context_expr;
    public expr getInternalContext_expr() {
        return context_expr;
    }
    @ExposedGet(name = "context_expr")
    public PyObject getContext_expr() {
        return context_expr;
    }
    @ExposedSet(name = "context_expr")
    public void setContext_expr(PyObject context_expr) {
        this.context_expr = AstAdapters.py2expr(context_expr);
    }

    private expr optional_vars;
    public expr getInternalOptional_vars() {
        return optional_vars;
    }
    @ExposedGet(name = "optional_vars")
    public PyObject getOptional_vars() {
        return optional_vars;
    }
    @ExposedSet(name = "optional_vars")
    public void setOptional_vars(PyObject optional_vars) {
        this.optional_vars = AstAdapters.py2expr(optional_vars);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("context_expr"), new PyUnicode("optional_vars")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes = new PyUnicode[0];
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public withitem(PyType subType) {
        super(subType);
    }
    public withitem() {
        this(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void withitem___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("withitem", args, keywords, new String[]
            {"context_expr", "optional_vars"}, 2, true);
        setContext_expr(ap.getPyObject(0, Py.None));
        setOptional_vars(ap.getPyObject(1, Py.None));
    }

    public withitem(PyObject context_expr, PyObject optional_vars) {
        setContext_expr(context_expr);
        setOptional_vars(optional_vars);
    }

    public withitem(Token token, expr context_expr, expr optional_vars) {
        super(token);
        this.context_expr = context_expr;
        addChild(context_expr);
        this.optional_vars = optional_vars;
        addChild(optional_vars);
    }

    public withitem(Integer ttype, Token token, expr context_expr, expr optional_vars) {
        super(ttype, token);
        this.context_expr = context_expr;
        addChild(context_expr);
        this.optional_vars = optional_vars;
        addChild(optional_vars);
    }

    public withitem(TerminalNode node, expr context_expr, expr optional_vars) {
        super(node);
        this.context_expr = context_expr;
        addChild(context_expr);
        this.optional_vars = optional_vars;
        addChild(optional_vars);
    }

    public withitem(PythonTree tree, expr context_expr, expr optional_vars) {
        super(tree);
        this.context_expr = context_expr;
        addChild(context_expr);
        this.optional_vars = optional_vars;
        addChild(optional_vars);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "withitem";
    }

    public String toStringTree() {
        StringBuffer sb = new StringBuffer("withitem(");
        sb.append("context_expr=");
        sb.append(dumpThis(context_expr));
        sb.append(",");
        sb.append("optional_vars=");
        sb.append(dumpThis(optional_vars));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        traverse(visitor);
        return null;
    }

    public void traverse(VisitorIF<?> visitor) throws Exception {
        if (context_expr != null)
            context_expr.accept(visitor);
        if (optional_vars != null)
            optional_vars.accept(visitor);
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
