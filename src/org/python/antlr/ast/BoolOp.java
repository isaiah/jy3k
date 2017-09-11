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

@ExposedType(name = "_ast.BoolOp", base = expr.class)
public class BoolOp extends expr {
public static final PyType TYPE = PyType.fromClass(BoolOp.class);
    private boolopType op;
    public boolopType getInternalOp() {
        return op;
    }
    public void setInternalOp(boolopType op) {
        this.op = op;
    }
    @ExposedGet(name = "op")
    public PyObject getOp() {
        return AstAdapters.boolop2py(op);
    }
    @ExposedSet(name = "op")
    public void setOp(PyObject op) {
        this.op = AstAdapters.py2boolop(op);
    }

    private java.util.List<expr> values;
    public java.util.List<expr> getInternalValues() {
        return values;
    }
    public void setInternalValues(java.util.List<expr> values) {
        this.values = values;
    }
    @ExposedGet(name = "values")
    public PyObject getValues() {
        return new AstList(values, AstAdapters.exprAdapter);
    }
    @ExposedSet(name = "values")
    public void setValues(PyObject values) {
        this.values = AstAdapters.py2exprList(values);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("op"), new PyUnicode("values")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public BoolOp() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void BoolOp___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("BoolOp", args, keywords, new String[]
            {"op", "values", "lineno", "col_offset"}, 2, true);
        setOp(ap.getPyObject(0, Py.None));
        setValues(ap.getPyObject(1, Py.None));
        int lin = ap.getInt(2, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(3, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public BoolOp(PyObject op, PyObject values) {
        super(TYPE);
        setOp(op);
        setValues(values);
    }

    // called from derived class
    public BoolOp(PyType subtype) {
        super(subtype);
    }

    public BoolOp(Token token, boolopType op, java.util.List<expr> values) {
        super(TYPE, token);
        this.op = op;
        this.values = values;
        if (values == null) {
            this.values = new ArrayList<>(0);
        }
        for(int i = 0; i < this.values.size(); i++) {
            PythonTree t = this.values.get(i);
            if (t != null)
                t.setParent(this);
        }
    }

    public BoolOp(PythonTree tree, boolopType op, java.util.List<expr> values) {
        super(TYPE, tree);
        this.op = op;
        this.values = values;
        if (values == null) {
            this.values = new ArrayList<>(0);
        }
        for(int i = 0; i < this.values.size(); i++) {
            PythonTree t = this.values.get(i);
            if (t != null)
                t.setParent(this);
        }
    }

    public BoolOp copy() {
        return new BoolOp(this.getToken(), this.op, this.values);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "BoolOp";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("BoolOp(");
        sb.append("op=");
        sb.append(dumpThis(op));
        sb.append(",");
        sb.append("values=");
        sb.append(dumpThis(values));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitBoolOp(this);
    }

    public void traverse(VisitorIF<?> visitor) {
        if (values != null) {
            for (PythonTree t : values) {
                if (t != null)
                    t.accept(visitor);
            }
        }
    }

    public void replaceField(expr value, expr newValue) {
        for (int i=0;i<this.values.size();i++){
            expr thisVal = this.values.get(i);
            if (value == thisVal) this.values.set(i,newValue);
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
