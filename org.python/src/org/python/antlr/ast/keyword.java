// Autogenerated AST node
package org.python.antlr.ast;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.python.antlr.AST;
import org.python.antlr.ast.VisitorIF;
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
import org.python.core.PyTuple;
import org.python.core.PyStringMap;
import org.python.core.PyLong;
import org.python.core.PyType;
import org.python.core.PyList;
import org.python.parser.Node;
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

@ExposedType(name = "_ast.keyword", base = AST.class)
public class keyword extends PythonTree {
    public static final PyType TYPE = PyType.fromClass(keyword.class);
    private String arg;
    public String getInternalArg() {
        return arg;
    }
    public void setInternalArg(String arg) {
        this.arg = arg;
    }
    @ExposedGet(name = "arg")
    public PyObject getArg() {
        if (arg == null) return Py.None;
        return new PyUnicode(arg);
    }
    @ExposedSet(name = "arg")
    public void setArg(PyObject arg) {
        this.arg = AstAdapters.py2identifier(arg);
    }

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


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("arg"), new PyUnicode("value")};
    @ExposedGet(name = "_fields")
    public PyObject get_fields() { return new PyTuple(fields); }

    private final static PyUnicode[] attributes = new PyUnicode[0];
    @ExposedGet(name = "_attributes")
    public PyObject get_attributes() { return Py.EmptyTuple; }

    public keyword() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject keyword_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[]
    args, String[] keywords) {
        return new keyword(subtype);
    }
    @ExposedMethod(names={"__init__"})
    public void keyword___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("keyword", args, keywords, new String[]
            {"arg", "value"}, 2, true);
        setArg(ap.getPyObject(0, Py.None));
        setValue(ap.getPyObject(1, Py.None));
    }

    public keyword(PyObject arg, PyObject value) {
        super(TYPE);
        setArg(arg);
        setValue(value);
    }

    // called from derived class
    public keyword(PyType subtype) {
        super(subtype);
    }

    public keyword(Node token, String arg, expr value) {
        super(TYPE, token);
        this.arg = arg;
        this.value = value;
        if (this.value != null)
            this.value.setParent(this);
    }

    public keyword(Token token, String arg, expr value) {
        super(TYPE, token);
        this.arg = arg;
        this.value = value;
        if (this.value != null)
            this.value.setParent(this);
    }

    public keyword(PythonTree tree, String arg, expr value) {
        super(TYPE, tree);
        this.arg = arg;
        this.value = value;
        if (this.value != null)
            this.value.setParent(this);
    }

    public keyword copy() {
        return new keyword(this.getToken(), this.arg, this.value);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "keyword";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("keyword(");
        sb.append("arg=");
        sb.append(dumpThis(arg));
        sb.append(",");
        sb.append("value=");
        sb.append(dumpThis(value));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return false;
    }

    public <R> void leave(VisitorIF<R> visitor) {
    }

    public <R> R accept(VisitorIF<R> visitor) {
        traverse(visitor);
        return null;
    }

    public <R> void traverse(VisitorIF<R> visitor) {
        if (value != null)
            value.accept(visitor);
    }

    public void replaceField(expr value, expr newValue) {
        if (value == value) this.value = newValue;
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
