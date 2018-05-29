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

@ExposedType(name = "_ast.Delete", base = stmt.class)
public class Delete extends stmt {
public static final PyType TYPE = PyType.fromClass(Delete.class);
    private java.util.List<expr> targets;
    public java.util.List<expr> getInternalTargets() {
        return targets;
    }
    public void setInternalTargets(java.util.List<expr> targets) {
        this.targets = targets;
    }
    @ExposedGet(name = "targets")
    public PyObject getTargets() {
        return new PyList(targets);
    }
    @ExposedSet(name = "targets")
    public void setTargets(PyObject targets) {
        this.targets = AstAdapters.py2exprList(targets);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("targets")};
    @ExposedGet(name = "_fields")
    public PyObject get_fields() { return new PyTuple(fields); }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyObject get_attributes() { return new PyTuple(attributes); }

    public Delete() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject Delete_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[]
    args, String[] keywords) {
        return new Delete(subtype);
    }
    @ExposedMethod(names={"__init__"})
    public void Delete___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("Delete", args, keywords, new String[]
            {"targets", "lineno", "col_offset"}, 1, true);
        setTargets(ap.getPyObject(0, Py.None));
        PyObject lin = ap.getOptionalArg(1);
        if (lin != null) {
            lineno = lin;
        }

        PyObject col = ap.getOptionalArg(2);
        if (col != null) {
            col_offset = col;
        }

    }

    public Delete(PyObject targets) {
        super(TYPE);
        setTargets(targets);
    }

    // called from derived class
    public Delete(PyType subtype) {
        super(subtype);
    }

    public Delete(Node token, java.util.List<expr> targets) {
        super(TYPE, token);
        this.targets = targets;
        if (targets == null) {
            this.targets = new ArrayList<>(0);
        }
        for(int i = 0; i < this.targets.size(); i++) {
            PythonTree t = this.targets.get(i);
            if (t != null)
                t.setParent(this);
        }
    }

    public Delete(Token token, java.util.List<expr> targets) {
        super(TYPE, token);
        this.targets = targets;
        if (targets == null) {
            this.targets = new ArrayList<>(0);
        }
        for(int i = 0; i < this.targets.size(); i++) {
            PythonTree t = this.targets.get(i);
            if (t != null)
                t.setParent(this);
        }
    }

    public Delete(PythonTree tree, java.util.List<expr> targets) {
        super(TYPE, tree);
        this.targets = targets;
        if (targets == null) {
            this.targets = new ArrayList<>(0);
        }
        for(int i = 0; i < this.targets.size(); i++) {
            PythonTree t = this.targets.get(i);
            if (t != null)
                t.setParent(this);
        }
    }

    public Delete copy() {
        return new Delete(this.getToken(), this.targets);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "Delete";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("Delete(");
        sb.append("targets=");
        sb.append(dumpThis(targets));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterDelete(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveDelete(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitDelete(this);
    }

    public <R> void traverse(VisitorIF<R> visitor) {
        if (targets != null) {
            for (PythonTree t : targets) {
                if (t != null)
                    t.accept(visitor);
            }
        }
    }

    public void replaceField(expr value, expr newValue) {
        for (int i=0;i<this.targets.size();i++){
            expr thisVal = this.targets.get(i);
            if (value == thisVal) this.targets.set(i,newValue);
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
