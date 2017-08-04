// Autogenerated AST node
package org.python.antlr.ast;
import org.antlr.v4.runtime.Token;
import org.python.antlr.PythonTree;
import org.python.antlr.adapter.AstAdapters;
import org.python.antlr.base.expr;
import org.python.core.ArgParser;
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

@ExposedType(name = "_ast.NameConstant", base = expr.class)
public class NameConstant extends expr {
public static final PyType TYPE = PyType.fromClass(NameConstant.class);
    private String value;
    public String getInternalValue() {
        return value;
    }
    public void setInternalValue(String value) {
        this.value = value;
    }
    @ExposedGet(name = "value")
    public PyObject getValue() {
        return AstAdapters.singleton2py(value);
    }
    @ExposedSet(name = "value")
    public void setValue(PyObject value) {
        this.value = AstAdapters.py2singleton(value);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("value")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public NameConstant() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void NameConstant___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("NameConstant", args, keywords, new String[]
            {"value", "lineno", "col_offset"}, 1, true);
        setValue(ap.getPyObject(0, Py.None));
        int lin = ap.getInt(1, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(2, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public NameConstant(PyObject value) {
        super(TYPE);
        setValue(value);
    }

    // called from derived class
    public NameConstant(PyType subtype) {
        super(subtype);
    }

    public NameConstant(Token token, String value) {
        super(TYPE, token);
        this.value = value;
    }

    public NameConstant(PythonTree tree, String value) {
        super(TYPE, tree);
        this.value = value;
    }

    public NameConstant copy() {
        return new NameConstant(this.getToken(), this.value);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "NameConstant";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("NameConstant(");
        sb.append("value=");
        sb.append(dumpThis(value));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitNameConstant(this);
    }

    public void traverse(VisitorIF<?> visitor) throws Exception {
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
