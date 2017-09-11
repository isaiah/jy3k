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

@ExposedType(name = "_ast.AnnAssign", base = stmt.class)
public class AnnAssign extends stmt {
public static final PyType TYPE = PyType.fromClass(AnnAssign.class);
    private expr target;
    public expr getInternalTarget() {
        return target;
    }
    public void setInternalTarget(expr target) {
        this.target = target;
    }
    @ExposedGet(name = "target")
    public PyObject getTarget() {
        return target;
    }
    @ExposedSet(name = "target")
    public void setTarget(PyObject target) {
        this.target = AstAdapters.py2expr(target);
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

    private Integer simple;
    public Integer getInternalSimple() {
        return simple;
    }
    public void setInternalSimple(Integer simple) {
        this.simple = simple;
    }
    @ExposedGet(name = "simple")
    public PyObject getSimple() {
        return Py.newInteger(simple);
    }
    @ExposedSet(name = "simple")
    public void setSimple(PyObject simple) {
        this.simple = AstAdapters.py2int(simple);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("target"), new PyUnicode("annotation"), new PyUnicode("value"),
                      new PyUnicode("simple")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public AnnAssign() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void AnnAssign___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("AnnAssign", args, keywords, new String[]
            {"target", "annotation", "value", "simple", "lineno", "col_offset"}, 4, true);
        setTarget(ap.getPyObject(0, Py.None));
        setAnnotation(ap.getPyObject(1, Py.None));
        setValue(ap.getPyObject(2, Py.None));
        setSimple(ap.getPyObject(3, Py.None));
        int lin = ap.getInt(4, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(5, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public AnnAssign(PyObject target, PyObject annotation, PyObject value, PyObject simple) {
        super(TYPE);
        setTarget(target);
        setAnnotation(annotation);
        setValue(value);
        setSimple(simple);
    }

    // called from derived class
    public AnnAssign(PyType subtype) {
        super(subtype);
    }

    public AnnAssign(Token token, expr target, expr annotation, expr value, Integer simple) {
        super(TYPE, token);
        this.target = target;
        if (this.target != null)
            this.target.setParent(this);
        this.annotation = annotation;
        if (this.annotation != null)
            this.annotation.setParent(this);
        this.value = value;
        if (this.value != null)
            this.value.setParent(this);
        this.simple = simple;
    }

    public AnnAssign(PythonTree tree, expr target, expr annotation, expr value, Integer simple) {
        super(TYPE, tree);
        this.target = target;
        if (this.target != null)
            this.target.setParent(this);
        this.annotation = annotation;
        if (this.annotation != null)
            this.annotation.setParent(this);
        this.value = value;
        if (this.value != null)
            this.value.setParent(this);
        this.simple = simple;
    }

    public AnnAssign copy() {
        return new AnnAssign(this.getToken(), this.target, this.annotation, this.value,
        this.simple);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "AnnAssign";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("AnnAssign(");
        sb.append("target=");
        sb.append(dumpThis(target));
        sb.append(",");
        sb.append("annotation=");
        sb.append(dumpThis(annotation));
        sb.append(",");
        sb.append("value=");
        sb.append(dumpThis(value));
        sb.append(",");
        sb.append("simple=");
        sb.append(dumpThis(simple));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitAnnAssign(this);
    }

    public void traverse(VisitorIF<?> visitor) {
        if (target != null)
            target.accept(visitor);
        if (annotation != null)
            annotation.accept(visitor);
        if (value != null)
            value.accept(visitor);
    }

    public void replaceField(expr value, expr newValue) {
        if (value == target) this.target = newValue;
        if (value == annotation) this.annotation = newValue;
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
