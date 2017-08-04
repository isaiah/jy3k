// Autogenerated AST node
package org.python.antlr.ast;
import org.antlr.v4.runtime.Token;
import org.python.antlr.PythonTree;
import org.python.antlr.adapter.AstAdapters;
import org.python.antlr.base.expr;
import org.python.antlr.base.slice;
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

@ExposedType(name = "_ast.Slice", base = slice.class)
public class Slice extends slice {
public static final PyType TYPE = PyType.fromClass(Slice.class);
    private expr lower;
    public expr getInternalLower() {
        return lower;
    }
    public void setInternalLower(expr lower) {
        this.lower = lower;
    }
    @ExposedGet(name = "lower")
    public PyObject getLower() {
        return lower;
    }
    @ExposedSet(name = "lower")
    public void setLower(PyObject lower) {
        this.lower = AstAdapters.py2expr(lower);
    }

    private expr upper;
    public expr getInternalUpper() {
        return upper;
    }
    public void setInternalUpper(expr upper) {
        this.upper = upper;
    }
    @ExposedGet(name = "upper")
    public PyObject getUpper() {
        return upper;
    }
    @ExposedSet(name = "upper")
    public void setUpper(PyObject upper) {
        this.upper = AstAdapters.py2expr(upper);
    }

    private expr step;
    public expr getInternalStep() {
        return step;
    }
    public void setInternalStep(expr step) {
        this.step = step;
    }
    @ExposedGet(name = "step")
    public PyObject getStep() {
        return step;
    }
    @ExposedSet(name = "step")
    public void setStep(PyObject step) {
        this.step = AstAdapters.py2expr(step);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("lower"), new PyUnicode("upper"), new PyUnicode("step")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes = new PyUnicode[0];
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public Slice() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void Slice___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("Slice", args, keywords, new String[]
            {"lower", "upper", "step"}, 3, true);
        setLower(ap.getPyObject(0, Py.None));
        setUpper(ap.getPyObject(1, Py.None));
        setStep(ap.getPyObject(2, Py.None));
    }

    public Slice(PyObject lower, PyObject upper, PyObject step) {
        super(TYPE);
        setLower(lower);
        setUpper(upper);
        setStep(step);
    }

    // called from derived class
    public Slice(PyType subtype) {
        super(subtype);
    }

    public Slice(Token token, expr lower, expr upper, expr step) {
        super(TYPE, token);
        this.lower = lower;
        if (this.lower != null)
            this.lower.setParent(this);
        this.upper = upper;
        if (this.upper != null)
            this.upper.setParent(this);
        this.step = step;
        if (this.step != null)
            this.step.setParent(this);
    }

    public Slice(PythonTree tree, expr lower, expr upper, expr step) {
        super(TYPE, tree);
        this.lower = lower;
        if (this.lower != null)
            this.lower.setParent(this);
        this.upper = upper;
        if (this.upper != null)
            this.upper.setParent(this);
        this.step = step;
        if (this.step != null)
            this.step.setParent(this);
    }

    public Slice copy() {
        return new Slice(this.getToken(), this.lower, this.upper, this.step);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "Slice";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("Slice(");
        sb.append("lower=");
        sb.append(dumpThis(lower));
        sb.append(",");
        sb.append("upper=");
        sb.append(dumpThis(upper));
        sb.append(",");
        sb.append("step=");
        sb.append(dumpThis(step));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitSlice(this);
    }

    public void traverse(VisitorIF<?> visitor) throws Exception {
        if (lower != null)
            lower.accept(visitor);
        if (upper != null)
            upper.accept(visitor);
        if (step != null)
            step.accept(visitor);
    }

    public void replaceField(expr value, expr newValue) {
        if (value == lower) this.lower = newValue;
        if (value == upper) this.upper = newValue;
        if (value == step) this.step = newValue;
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
