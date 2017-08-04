// Autogenerated AST node
package org.python.antlr.ast;
import org.antlr.v4.runtime.Token;
import org.python.antlr.AST;
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

@ExposedType(name = "_ast.comprehension", base = AST.class)
public class comprehension extends PythonTree {
    public static final PyType TYPE = PyType.fromClass(comprehension.class);
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

    private expr iter;
    public expr getInternalIter() {
        return iter;
    }
    public void setInternalIter(expr iter) {
        this.iter = iter;
    }
    @ExposedGet(name = "iter")
    public PyObject getIter() {
        return iter;
    }
    @ExposedSet(name = "iter")
    public void setIter(PyObject iter) {
        this.iter = AstAdapters.py2expr(iter);
    }

    private java.util.List<expr> ifs;
    public java.util.List<expr> getInternalIfs() {
        return ifs;
    }
    public void setInternalIfs(java.util.List<expr> ifs) {
        this.ifs = ifs;
    }
    @ExposedGet(name = "ifs")
    public PyObject getIfs() {
        return new AstList(ifs, AstAdapters.exprAdapter);
    }
    @ExposedSet(name = "ifs")
    public void setIfs(PyObject ifs) {
        this.ifs = AstAdapters.py2exprList(ifs);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("target"), new PyUnicode("iter"), new PyUnicode("ifs")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes = new PyUnicode[0];
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public comprehension() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void comprehension___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("comprehension", args, keywords, new String[]
            {"target", "iter", "ifs"}, 3, true);
        setTarget(ap.getPyObject(0, Py.None));
        setIter(ap.getPyObject(1, Py.None));
        setIfs(ap.getPyObject(2, Py.None));
    }

    public comprehension(PyObject target, PyObject iter, PyObject ifs) {
        super(TYPE);
        setTarget(target);
        setIter(iter);
        setIfs(ifs);
    }

    // called from derived class
    public comprehension(PyType subtype) {
        super(subtype);
    }

    public comprehension(Token token, expr target, expr iter, java.util.List<expr> ifs) {
        super(TYPE, token);
        this.target = target;
        if (this.target != null)
            this.target.setParent(this);
        this.iter = iter;
        if (this.iter != null)
            this.iter.setParent(this);
        this.ifs = ifs;
        if (ifs == null) {
            this.ifs = new ArrayList<>(0);
        }
        for(int i = 0; i < this.ifs.size(); i++) {
            PythonTree t = this.ifs.get(i);
            if (t != null)
                t.setParent(this);
        }
    }

    public comprehension(PythonTree tree, expr target, expr iter, java.util.List<expr> ifs) {
        super(TYPE, tree);
        this.target = target;
        if (this.target != null)
            this.target.setParent(this);
        this.iter = iter;
        if (this.iter != null)
            this.iter.setParent(this);
        this.ifs = ifs;
        if (ifs == null) {
            this.ifs = new ArrayList<>(0);
        }
        for(int i = 0; i < this.ifs.size(); i++) {
            PythonTree t = this.ifs.get(i);
            if (t != null)
                t.setParent(this);
        }
    }

    public comprehension copy() {
        return new comprehension(this.getToken(), this.target, this.iter, this.ifs);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "comprehension";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("comprehension(");
        sb.append("target=");
        sb.append(dumpThis(target));
        sb.append(",");
        sb.append("iter=");
        sb.append(dumpThis(iter));
        sb.append(",");
        sb.append("ifs=");
        sb.append(dumpThis(ifs));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        traverse(visitor);
        return null;
    }

    public void traverse(VisitorIF<?> visitor) throws Exception {
        if (target != null)
            target.accept(visitor);
        if (iter != null)
            iter.accept(visitor);
        if (ifs != null) {
            for (PythonTree t : ifs) {
                if (t != null)
                    t.accept(visitor);
            }
        }
    }

    public void replaceField(expr value, expr newValue) {
        if (value == target) this.target = newValue;
        if (value == iter) this.iter = newValue;
        for (int i=0;i<this.ifs.size();i++){
            expr thisVal = this.ifs.get(i);
            if (value == thisVal) this.ifs.set(i,newValue);
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

}
