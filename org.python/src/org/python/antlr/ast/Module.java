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

@ExposedType(name = "_ast.Module", base = mod.class)
public class Module extends mod {
public static final PyType TYPE = PyType.fromClass(Module.class);
    private java.util.List<stmt> body;
    public java.util.List<stmt> getInternalBody() {
        return body;
    }
    public void setInternalBody(java.util.List<stmt> body) {
        this.body = body;
    }
    @ExposedGet(name = "body")
    public PyObject getBody() {
        return new PyList(body);
    }
    @ExposedSet(name = "body")
    public void setBody(PyObject body) {
        this.body = AstAdapters.py2stmtList(body);
    }

    private String docstring;
    public String getInternalDocstring() {
        return docstring;
    }
    public void setInternalDocstring(String docstring) {
        this.docstring = docstring;
    }
    @ExposedGet(name = "docstring")
    public PyObject getDocstring() {
        return AstAdapters.string2py(docstring);
    }
    @ExposedSet(name = "docstring")
    public void setDocstring(PyObject docstring) {
        this.docstring = AstAdapters.py2string(docstring);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("body"), new PyUnicode("docstring")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes = new PyUnicode[0];
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public Module() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject Module_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[]
    args, String[] keywords) {
        return new Module(subtype);
    }
    @ExposedMethod(names={"__init__"})
    public void Module___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("Module", args, keywords, new String[]
            {"body", "docstring"}, 2, true);
        setBody(ap.getPyObject(0, Py.None));
        setDocstring(ap.getPyObject(1, Py.None));
    }

    public Module(PyObject body, PyObject docstring) {
        super(TYPE);
        setBody(body);
        setDocstring(docstring);
    }

    // called from derived class
    public Module(PyType subtype) {
        super(subtype);
    }

    public Module(Token token, java.util.List<stmt> body, String docstring) {
        super(TYPE, token);
        this.body = body;
        if (body == null) {
            this.body = new ArrayList<>(0);
        }
        for(int i = 0; i < this.body.size(); i++) {
            PythonTree t = this.body.get(i);
            addChild(t, i, this.body);
        }
        this.docstring = docstring;
    }

    public Module(PythonTree tree, java.util.List<stmt> body, String docstring) {
        super(TYPE, tree);
        this.body = body;
        if (body == null) {
            this.body = new ArrayList<>(0);
        }
        for(int i = 0; i < this.body.size(); i++) {
            PythonTree t = this.body.get(i);
            addChild(t, i, this.body);
        }
        this.docstring = docstring;
    }

    public Module copy() {
        return new Module(this.getToken(), this.body, this.docstring);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "Module";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("Module(");
        sb.append("body=");
        sb.append(dumpThis(body));
        sb.append(",");
        sb.append("docstring=");
        sb.append(dumpThis(docstring));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterModule(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveModule(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitModule(this);
    }

    public <R> void traverse(VisitorIF<R> visitor) {
        if (body != null) {
            for (PythonTree t : body) {
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

}
