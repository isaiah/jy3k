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

@ExposedType(name = "_ast.arg", base = AST.class)
public class arg extends PythonTree {
    public static final PyType TYPE = PyType.fromClass(arg.class);
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

    private expr annotation;
    public expr getInternalAnnotation() {
        return annotation;
    }
    public void setInternalAnnotation(expr annotation) {
        this.annotation = annotation;
    }
    @ExposedGet(name = "annotation")
    public PyObject getAnnotation() {
        return annotation;
    }
    @ExposedSet(name = "annotation")
    public void setAnnotation(PyObject annotation) {
        this.annotation = AstAdapters.py2expr(annotation);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("arg"), new PyUnicode("annotation")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes = new PyUnicode[0];
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public arg() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void arg___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("arg", args, keywords, new String[]
            {"arg", "annotation"}, 2, true);
        setArg(ap.getPyObject(0, Py.None));
        setAnnotation(ap.getPyObject(1, Py.None));
    }

    public arg(PyObject arg, PyObject annotation) {
        super(TYPE);
        setArg(arg);
        setAnnotation(annotation);
    }

    // called from derived class
    public arg(PyType subtype) {
        super(subtype);
    }

    public arg(Token token, String arg, expr annotation) {
        super(TYPE, token);
        this.arg = arg;
        this.annotation = annotation;
        if (this.annotation != null)
            this.annotation.setParent(this);
    }

    public arg(PythonTree tree, String arg, expr annotation) {
        super(TYPE, tree);
        this.arg = arg;
        this.annotation = annotation;
        if (this.annotation != null)
            this.annotation.setParent(this);
    }

    public arg copy() {
        return new arg(this.getToken(), this.arg, this.annotation);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "arg";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("arg(");
        sb.append("arg=");
        sb.append(dumpThis(arg));
        sb.append(",");
        sb.append("annotation=");
        sb.append(dumpThis(annotation));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        traverse(visitor);
        return null;
    }

    public void traverse(VisitorIF<?> visitor) throws Exception {
        if (annotation != null)
            annotation.accept(visitor);
    }

    public void replaceField(expr value, expr newValue) {
        if (value == annotation) this.annotation = newValue;
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
