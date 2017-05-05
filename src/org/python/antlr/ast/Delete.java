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

@ExposedType(name = "_ast.Delete", base = stmt.class)
public class Delete extends stmt {
public static final PyType TYPE = PyType.fromClass(Delete.class);
    private java.util.List<expr> targets;
    public java.util.List<expr> getInternalTargets() {
        return targets;
    }
    public void setInternalTargets(java.util.List<expr> targets) {
        this.targets = targets;
    }
    @ExposedGet(name = "targets")
    public PyObject getTargets() {
        return new AstList(targets, AstAdapters.exprAdapter);
    }
    @ExposedSet(name = "targets")
    public void setTargets(PyObject targets) {
        this.targets = AstAdapters.py2exprList(targets);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("targets")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public Delete() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void Delete___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("Delete", args, keywords, new String[]
            {"targets", "lineno", "col_offset"}, 1, true);
        setTargets(ap.getPyObject(0, Py.None));
        int lin = ap.getInt(1, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(2, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public Delete(PyObject targets) {
        super(TYPE);
        setTargets(targets);
    }

    // called from derived class
    public Delete(PyType subtype) {
        super(subtype);
    }

    public Delete(Token token, java.util.List<expr> targets) {
        super(TYPE, token);
        this.targets = targets;
        if (targets == null) {
            this.targets = new ArrayList<>(0);
        }
    }

    public Delete(PythonTree tree, java.util.List<expr> targets) {
        super(TYPE, tree);
        this.targets = targets;
        if (targets == null) {
            this.targets = new ArrayList<>(0);
        }
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "Delete";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("Delete(");
        sb.append("targets=");
        sb.append(dumpThis(targets));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitDelete(this);
    }

    public void traverse(VisitorIF<?> visitor) throws Exception {
        if (targets != null) {
            for (PythonTree t : targets) {
                if (t != null)
                    t.accept(visitor);
            }
        }
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
