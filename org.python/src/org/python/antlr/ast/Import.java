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

@ExposedType(name = "_ast.Import", base = stmt.class)
public class Import extends stmt {
public static final PyType TYPE = PyType.fromClass(Import.class);
    private java.util.List<alias> names;
    public java.util.List<alias> getInternalNames() {
        return names;
    }
    public void setInternalNames(java.util.List<alias> names) {
        this.names = names;
    }
    @ExposedGet(name = "names")
    public PyObject getNames() {
        return new PyList(names);
    }
    @ExposedSet(name = "names")
    public void setNames(PyObject names) {
        this.names = AstAdapters.py2aliasList(names);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("names")};
    @ExposedGet(name = "_fields")
    public PyObject get_fields() { return new PyTuple(fields); }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyObject get_attributes() { return new PyTuple(attributes); }

    public Import() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject Import_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[]
    args, String[] keywords) {
        return new Import(subtype);
    }
    @ExposedMethod(names={"__init__"})
    public void Import___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("Import", args, keywords, new String[]
            {"names", "lineno", "col_offset"}, 1, true);
        setNames(ap.getPyObject(0, Py.None));
        PyObject lin = ap.getOptionalArg(1);
        if (lin != null) {
            lineno = lin;
        }

        PyObject col = ap.getOptionalArg(2);
        if (col != null) {
            col_offset = col;
        }

    }

    public Import(PyObject names) {
        super(TYPE);
        setNames(names);
    }

    // called from derived class
    public Import(PyType subtype) {
        super(subtype);
    }

    public Import(Node token, java.util.List<alias> names) {
        super(TYPE, token);
        this.names = names;
        if (names == null) {
            this.names = new ArrayList<>(0);
        }
        for(int i = 0; i < this.names.size(); i++) {
            PythonTree t = this.names.get(i);
            if (t != null)
                t.setParent(this);
        }
    }

    public Import(Token token, java.util.List<alias> names) {
        super(TYPE, token);
        this.names = names;
        if (names == null) {
            this.names = new ArrayList<>(0);
        }
        for(int i = 0; i < this.names.size(); i++) {
            PythonTree t = this.names.get(i);
            if (t != null)
                t.setParent(this);
        }
    }

    public Import(PythonTree tree, java.util.List<alias> names) {
        super(TYPE, tree);
        this.names = names;
        if (names == null) {
            this.names = new ArrayList<>(0);
        }
        for(int i = 0; i < this.names.size(); i++) {
            PythonTree t = this.names.get(i);
            if (t != null)
                t.setParent(this);
        }
    }

    public Import copy() {
        return new Import(this.getToken(), this.names);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "Import";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("Import(");
        sb.append("names=");
        sb.append(dumpThis(names));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterImport(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveImport(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitImport(this);
    }

    public <R> void traverse(VisitorIF<R> visitor) {
        if (names != null) {
            for (PythonTree t : names) {
                if (t != null)
                    t.accept(visitor);
            }
        }
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
