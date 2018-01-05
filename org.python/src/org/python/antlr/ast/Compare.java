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

@ExposedType(name = "_ast.Compare", base = expr.class)
public class Compare extends expr {
public static final PyType TYPE = PyType.fromClass(Compare.class);
    private expr left;
    public expr getInternalLeft() {
        return left;
    }
    public void setInternalLeft(expr left) {
        this.left = left;
    }
    @ExposedGet(name = "left")
    public PyObject getLeft() {
        return left;
    }
    @ExposedSet(name = "left")
    public void setLeft(PyObject left) {
        this.left = AstAdapters.py2expr(left);
    }

    private java.util.List<cmpopType> ops;
    public java.util.List<cmpopType> getInternalOps() {
        return ops;
    }
    public void setInternalOps(java.util.List<cmpopType> ops) {
        this.ops = ops;
    }
    @ExposedGet(name = "ops")
    public PyObject getOps() {
        return new AstList(ops, AstAdapters.cmpopAdapter);
    }
    @ExposedSet(name = "ops")
    public void setOps(PyObject ops) {
        this.ops = AstAdapters.py2cmpopList(ops);
    }

    private java.util.List<expr> comparators;
    public java.util.List<expr> getInternalComparators() {
        return comparators;
    }
    public void setInternalComparators(java.util.List<expr> comparators) {
        this.comparators = comparators;
    }
    @ExposedGet(name = "comparators")
    public PyObject getComparators() {
        return new AstList(comparators, AstAdapters.exprAdapter);
    }
    @ExposedSet(name = "comparators")
    public void setComparators(PyObject comparators) {
        this.comparators = AstAdapters.py2exprList(comparators);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("left"), new PyUnicode("ops"), new PyUnicode("comparators")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public Compare() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void Compare___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("Compare", args, keywords, new String[]
            {"left", "ops", "comparators", "lineno", "col_offset"}, 3, true);
        setLeft(ap.getPyObject(0, Py.None));
        setOps(ap.getPyObject(1, Py.None));
        setComparators(ap.getPyObject(2, Py.None));
        int lin = ap.getInt(3, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(4, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public Compare(PyObject left, PyObject ops, PyObject comparators) {
        super(TYPE);
        setLeft(left);
        setOps(ops);
        setComparators(comparators);
    }

    // called from derived class
    public Compare(PyType subtype) {
        super(subtype);
    }

    public Compare(Token token, expr left, java.util.List<cmpopType> ops, java.util.List<expr>
    comparators) {
        super(TYPE, token);
        this.left = left;
        if (this.left != null)
            this.left.setParent(this);
        this.ops = ops;
        this.comparators = comparators;
        if (comparators == null) {
            this.comparators = new ArrayList<>(0);
        }
        for(int i = 0; i < this.comparators.size(); i++) {
            PythonTree t = this.comparators.get(i);
            if (t != null)
                t.setParent(this);
        }
    }

    public Compare(PythonTree tree, expr left, java.util.List<cmpopType> ops, java.util.List<expr>
    comparators) {
        super(TYPE, tree);
        this.left = left;
        if (this.left != null)
            this.left.setParent(this);
        this.ops = ops;
        this.comparators = comparators;
        if (comparators == null) {
            this.comparators = new ArrayList<>(0);
        }
        for(int i = 0; i < this.comparators.size(); i++) {
            PythonTree t = this.comparators.get(i);
            if (t != null)
                t.setParent(this);
        }
    }

    public Compare copy() {
        return new Compare(this.getToken(), this.left, this.ops, this.comparators);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "Compare";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("Compare(");
        sb.append("left=");
        sb.append(dumpThis(left));
        sb.append(",");
        sb.append("ops=");
        sb.append(dumpThis(ops));
        sb.append(",");
        sb.append("comparators=");
        sb.append(dumpThis(comparators));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterCompare(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveCompare(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitCompare(this);
    }

    public <R> void traverse(VisitorIF<R> visitor) {
        if (left != null)
            left.accept(visitor);
        if (comparators != null) {
            for (PythonTree t : comparators) {
                if (t != null)
                    t.accept(visitor);
            }
        }
    }

    public void replaceField(expr value, expr newValue) {
        if (value == left) this.left = newValue;
        for (int i=0;i<this.comparators.size();i++){
            expr thisVal = this.comparators.get(i);
            if (value == thisVal) this.comparators.set(i,newValue);
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