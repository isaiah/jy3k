// Autogenerated AST node
package org.python.antlr.ast;
import org.antlr.v4.runtime.Token;
import org.python.antlr.PythonTree;
import org.python.antlr.adapter.AstAdapters;
import org.python.antlr.base.expr;
import org.python.core.ArgParser;
import org.python.core.AstList;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyUnicode;
import org.python.core.PyStringMap;
import org.python.core.PyType;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSet;
import org.python.annotations.ExposedType;

import java.util.ArrayList;

@ExposedType(name = "_ast.Dict", base = expr.class)
public class Dict extends expr {
public static final PyType TYPE = PyType.fromClass(Dict.class);
    private java.util.List<expr> keys;
    public java.util.List<expr> getInternalKeys() {
        return keys;
    }
    public void setInternalKeys(java.util.List<expr> keys) {
        this.keys = keys;
    }
    @ExposedGet(name = "keys")
    public PyObject getKeys() {
        return new AstList(keys, AstAdapters.exprAdapter);
    }
    @ExposedSet(name = "keys")
    public void setKeys(PyObject keys) {
        this.keys = AstAdapters.py2exprList(keys);
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
    new PyUnicode[] {new PyUnicode("keys"), new PyUnicode("values")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public Dict() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void Dict___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("Dict", args, keywords, new String[]
            {"keys", "values", "lineno", "col_offset"}, 2, true);
        setKeys(ap.getPyObject(0, Py.None));
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

    public Dict(PyObject keys, PyObject values) {
        super(TYPE);
        setKeys(keys);
        setValues(values);
    }

    // called from derived class
    public Dict(PyType subtype) {
        super(subtype);
    }

    public Dict(Token token, java.util.List<expr> keys, java.util.List<expr> values) {
        super(TYPE, token);
        this.keys = keys;
        if (keys == null) {
            this.keys = new ArrayList<>(0);
        }
        for(int i = 0; i < this.keys.size(); i++) {
            PythonTree t = this.keys.get(i);
            if (t != null)
                t.setParent(this);
        }
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

    public Dict(PythonTree tree, java.util.List<expr> keys, java.util.List<expr> values) {
        super(TYPE, tree);
        this.keys = keys;
        if (keys == null) {
            this.keys = new ArrayList<>(0);
        }
        for(int i = 0; i < this.keys.size(); i++) {
            PythonTree t = this.keys.get(i);
            if (t != null)
                t.setParent(this);
        }
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

    public Dict copy() {
        return new Dict(this.getToken(), this.keys, this.values);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "Dict";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("Dict(");
        sb.append("keys=");
        sb.append(dumpThis(keys));
        sb.append(",");
        sb.append("values=");
        sb.append(dumpThis(values));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitDict(this);
    }

    public void traverse(VisitorIF<?> visitor) throws Exception {
        if (keys != null) {
            for (PythonTree t : keys) {
                if (t != null)
                    t.accept(visitor);
            }
        }
        if (values != null) {
            for (PythonTree t : values) {
                if (t != null)
                    t.accept(visitor);
            }
        }
    }

    public void replaceField(expr value, expr newValue) {
        for (int i=0;i<this.keys.size();i++){
            expr thisVal = this.keys.get(i);
            if (value == thisVal) this.keys.set(i,newValue);
        }
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
