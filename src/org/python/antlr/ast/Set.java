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

@ExposedType(name = "_ast.Set", base = expr.class)
public class Set extends expr {
public static final PyType TYPE = PyType.fromClass(Set.class);
    private java.util.List<expr> elts;
    public java.util.List<expr> getInternalElts() {
        return elts;
    }
    public void setInternalElts(java.util.List<expr> elts) {
        this.elts = elts;
    }
    @ExposedGet(name = "elts")
    public PyObject getElts() {
        return new AstList(elts, AstAdapters.exprAdapter);
    }
    @ExposedSet(name = "elts")
    public void setElts(PyObject elts) {
        this.elts = AstAdapters.py2exprList(elts);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("elts")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public Set() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void Set___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("Set", args, keywords, new String[]
            {"elts", "lineno", "col_offset"}, 1, true);
        setElts(ap.getPyObject(0, Py.None));
        int lin = ap.getInt(1, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(2, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public Set(PyObject elts) {
        super(TYPE);
        setElts(elts);
    }

    // called from derived class
    public Set(PyType subtype) {
        super(subtype);
    }

    public Set(Token token, java.util.List<expr> elts) {
        super(TYPE, token);
        this.elts = elts;
        if (elts == null) {
            this.elts = new ArrayList<>(0);
        }
        for(int i = 0; i < this.elts.size(); i++) {
            PythonTree t = this.elts.get(i);
            if (t != null)
                t.setParent(this);
        }
    }

    public Set(PythonTree tree, java.util.List<expr> elts) {
        super(TYPE, tree);
        this.elts = elts;
        if (elts == null) {
            this.elts = new ArrayList<>(0);
        }
        for(int i = 0; i < this.elts.size(); i++) {
            PythonTree t = this.elts.get(i);
            if (t != null)
                t.setParent(this);
        }
    }

    public Set copy() {
        return new Set(this.getToken(), this.elts);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "Set";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("Set(");
        sb.append("elts=");
        sb.append(dumpThis(elts));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterSet(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveSet(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitSet(this);
    }

    public <R> void traverse(VisitorIF<R> visitor) {
        if (elts != null) {
            for (PythonTree t : elts) {
                if (t != null)
                    t.accept(visitor);
            }
        }
    }

    public void replaceField(expr value, expr newValue) {
        for (int i=0;i<this.elts.size();i++){
            expr thisVal = this.elts.get(i);
            if (value == thisVal) this.elts.set(i,newValue);
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
