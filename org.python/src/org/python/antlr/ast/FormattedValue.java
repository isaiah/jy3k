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
import org.python.core.PyLong;
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
import java.util.Objects;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

@ExposedType(name = "_ast.FormattedValue", base = expr.class)
public class FormattedValue extends expr {
public static final PyType TYPE = PyType.fromClass(FormattedValue.class);
    private expr value;
    public expr getInternalValue() {
        return value;
    }
    public void setInternalValue(expr value) {
        this.value = value;
    }
    @ExposedGet(name = "value")
    public PyObject getValue() {
        return value;
    }
    @ExposedSet(name = "value")
    public void setValue(PyObject value) {
        this.value = AstAdapters.py2expr(value);
    }

    private Integer conversion;
    public Integer getInternalConversion() {
        return conversion;
    }
    public void setInternalConversion(Integer conversion) {
        this.conversion = conversion;
    }
    @ExposedGet(name = "conversion")
    public PyObject getConversion() {
        return Py.newInteger(conversion);
    }
    @ExposedSet(name = "conversion")
    public void setConversion(PyObject conversion) {
        this.conversion = AstAdapters.py2int(conversion);
    }

    private expr format_spec;
    public expr getInternalFormat_spec() {
        return format_spec;
    }
    public void setInternalFormat_spec(expr format_spec) {
        this.format_spec = format_spec;
    }
    @ExposedGet(name = "format_spec")
    public PyObject getFormat_spec() {
        return format_spec;
    }
    @ExposedSet(name = "format_spec")
    public void setFormat_spec(PyObject format_spec) {
        this.format_spec = AstAdapters.py2expr(format_spec);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("value"), new PyUnicode("conversion"), new
                      PyUnicode("format_spec")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public FormattedValue() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject FormattedValue_new(PyNewWrapper _new, boolean init, PyType subtype,
    PyObject[] args, String[] keywords) {
        return new FormattedValue(subtype);
    }
    @ExposedMethod(names={"__init__"})
    public void FormattedValue___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("FormattedValue", args, keywords, new String[]
            {"value", "conversion", "format_spec", "lineno", "col_offset"}, 3, true);
        setValue(ap.getPyObject(0, Py.None));
        setConversion(ap.getPyObject(1, Py.None));
        setFormat_spec(ap.getPyObject(2, Py.None));
        PyObject lin = ap.getOptionalArg(3);
        if (lin != null) {
            lineno = lin;
        }

        PyObject col = ap.getOptionalArg(4);
        if (col != null) {
            col_offset = col;
        }

    }

    public FormattedValue(PyObject value, PyObject conversion, PyObject format_spec) {
        super(TYPE);
        setValue(value);
        setConversion(conversion);
        setFormat_spec(format_spec);
    }

    // called from derived class
    public FormattedValue(PyType subtype) {
        super(subtype);
    }

    public FormattedValue(Token token, expr value, Integer conversion, expr format_spec) {
        super(TYPE, token);
        this.value = value;
        if (this.value != null)
            this.value.setParent(this);
        this.conversion = conversion;
        this.format_spec = format_spec;
        if (this.format_spec != null)
            this.format_spec.setParent(this);
    }

    public FormattedValue(PythonTree tree, expr value, Integer conversion, expr format_spec) {
        super(TYPE, tree);
        this.value = value;
        if (this.value != null)
            this.value.setParent(this);
        this.conversion = conversion;
        this.format_spec = format_spec;
        if (this.format_spec != null)
            this.format_spec.setParent(this);
    }

    public FormattedValue copy() {
        return new FormattedValue(this.getToken(), this.value, this.conversion, this.format_spec);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "FormattedValue";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("FormattedValue(");
        sb.append("value=");
        sb.append(dumpThis(value));
        sb.append(",");
        sb.append("conversion=");
        sb.append(dumpThis(conversion));
        sb.append(",");
        sb.append("format_spec=");
        sb.append(dumpThis(format_spec));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterFormattedValue(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveFormattedValue(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitFormattedValue(this);
    }

    public <R> void traverse(VisitorIF<R> visitor) {
        if (value != null)
            value.accept(visitor);
        if (format_spec != null)
            format_spec.accept(visitor);
    }

    public void replaceField(expr value, expr newValue) {
        if (value == value) this.value = newValue;
        if (value == format_spec) this.format_spec = newValue;
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

    @ExposedGet(name = "lineno")
    public int getLineno() {
        return super.getLineno();
    }

    @ExposedSet(name = "lineno")
    public void setLineno(int num) {
        lineno = new PyLong(num);
    }

    @ExposedGet(name = "col_offset")
    public int getCol_offset() {
        return super.getCol_offset();
    }

    @ExposedSet(name = "col_offset")
    public void setCol_offset(int num) {
        col_offset = new PyLong(num);
    }

}
