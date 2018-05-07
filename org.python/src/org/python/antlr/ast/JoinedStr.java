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
import org.python.core.PyNewWrapper;
import org.python.core.Visitproc;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSet;
import org.python.annotations.ExposedType;
import org.python.annotations.ExposedSlot;
import org.python.annotations.SlotFunc;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

@ExposedType(name = "_ast.JoinedStr", base = expr.class)
public class JoinedStr extends expr {
public static final PyType TYPE = PyType.fromClass(JoinedStr.class);
    private java.util.List<expr> values;
    public java.util.List<expr> getInternalValues() {
        return values;
    }
    public void setInternalValues(java.util.List<expr> values) {
        this.values = values;
    }
    @ExposedGet(name = "values")
    public PyObject getValues() {
        return new PyList(values);
    }
    @ExposedSet(name = "values")
    public void setValues(PyObject values) {
        this.values = AstAdapters.py2exprList(values);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("values")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public JoinedStr() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject JoinedStr_new(PyNewWrapper _new, boolean init, PyType subtype,
    PyObject[] args, String[] keywords) {
        return new JoinedStr(subtype);
    }
    @ExposedMethod(names={"__init__"})
    public void JoinedStr___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("JoinedStr", args, keywords, new String[]
            {"values", "lineno", "col_offset"}, 1, true);
        setValues(ap.getPyObject(0, Py.None));
        int lin = ap.getInt(1, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(2, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public JoinedStr(PyObject values) {
        super(TYPE);
        setValues(values);
    }

    // called from derived class
    public JoinedStr(PyType subtype) {
        super(subtype);
    }

    public JoinedStr(Token token, java.util.List<expr> values) {
        super(TYPE, token);
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

    public JoinedStr(PythonTree tree, java.util.List<expr> values) {
        super(TYPE, tree);
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

    public JoinedStr copy() {
        return new JoinedStr(this.getToken(), this.values);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "JoinedStr";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("JoinedStr(");
        sb.append("values=");
        sb.append(dumpThis(values));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterJoinedStr(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveJoinedStr(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitJoinedStr(this);
    }

    public <R> void traverse(VisitorIF<R> visitor) {
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
