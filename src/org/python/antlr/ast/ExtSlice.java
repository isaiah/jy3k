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

@ExposedType(name = "_ast.ExtSlice", base = slice.class)
public class ExtSlice extends slice {
public static final PyType TYPE = PyType.fromClass(ExtSlice.class);
    private java.util.List<slice> dims;
    public java.util.List<slice> getInternalDims() {
        return dims;
    }
    public void setInternalDims(java.util.List<slice> dims) {
        this.dims = dims;
    }
    @ExposedGet(name = "dims")
    public PyObject getDims() {
        return new AstList(dims, AstAdapters.sliceAdapter);
    }
    @ExposedSet(name = "dims")
    public void setDims(PyObject dims) {
        this.dims = AstAdapters.py2sliceList(dims);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("dims")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes = new PyUnicode[0];
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public ExtSlice(PyType subType) {
        super(subType);
    }
    public ExtSlice() {
        this(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void ExtSlice___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("ExtSlice", args, keywords, new String[]
            {"dims"}, 1, true);
        setDims(ap.getPyObject(0, Py.None));
    }

    public ExtSlice(PyObject dims) {
        setDims(dims);
    }

    public ExtSlice(Token token, java.util.List<slice> dims) {
        super(token);
        this.dims = dims;
        if (dims == null) {
            this.dims = new ArrayList<slice>();
        }
        for(PythonTree t : this.dims) {
            addChild(t);
        }
    }

    public ExtSlice(Integer ttype, Token token, java.util.List<slice> dims) {
        super(ttype, token);
        this.dims = dims;
        if (dims == null) {
            this.dims = new ArrayList<slice>();
        }
        for(PythonTree t : this.dims) {
            addChild(t);
        }
    }

    public ExtSlice(TerminalNode node, java.util.List<slice> dims) {
        super(node);
        this.dims = dims;
        if (dims == null) {
            this.dims = new ArrayList<slice>();
        }
        for(PythonTree t : this.dims) {
            addChild(t);
        }
    }

    public ExtSlice(PythonTree tree, java.util.List<slice> dims) {
        super(tree);
        this.dims = dims;
        if (dims == null) {
            this.dims = new ArrayList<slice>();
        }
        for(PythonTree t : this.dims) {
            addChild(t);
        }
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "ExtSlice";
    }

    public String toStringTree() {
        StringBuffer sb = new StringBuffer("ExtSlice(");
        sb.append("dims=");
        sb.append(dumpThis(dims));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitExtSlice(this);
    }

    public void traverse(VisitorIF<?> visitor) throws Exception {
        if (dims != null) {
            for (PythonTree t : dims) {
                if (t != null)
                    t.accept(visitor);
            }
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
