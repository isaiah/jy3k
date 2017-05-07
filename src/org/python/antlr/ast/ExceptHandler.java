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

@ExposedType(name = "_ast.ExceptHandler", base = excepthandler.class)
public class ExceptHandler extends excepthandler {
public static final PyType TYPE = PyType.fromClass(ExceptHandler.class);
    private expr type;
    public expr getInternalType() {
        return type;
    }
    public void setInternalType(expr type) {
        this.type = type;
    }
    @ExposedGet(name = "type")
    public PyObject getExceptType() {
        return type;
    }
    @ExposedSet(name = "type")
    public void setExceptType(PyObject type) {
        this.type = AstAdapters.py2expr(type);
    }

    private String name;
    public String getInternalName() {
        return name;
    }
    public void setInternalName(String name) {
        this.name = name;
    }
    @ExposedGet(name = "name")
    public PyObject getName() {
        if (name == null) return Py.None;
        return new PyUnicode(name);
    }
    @ExposedSet(name = "name")
    public void setName(PyObject name) {
        this.name = AstAdapters.py2identifier(name);
    }

    private java.util.List<stmt> body;
    public java.util.List<stmt> getInternalBody() {
        return body;
    }
    public void setInternalBody(java.util.List<stmt> body) {
        this.body = body;
    }
    @ExposedGet(name = "body")
    public PyObject getBody() {
        return new AstList(body, AstAdapters.stmtAdapter);
    }
    @ExposedSet(name = "body")
    public void setBody(PyObject body) {
        this.body = AstAdapters.py2stmtList(body);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("type"), new PyUnicode("name"), new PyUnicode("body")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public ExceptHandler() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void ExceptHandler___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("ExceptHandler", args, keywords, new String[]
            {"type", "name", "body", "lineno", "col_offset"}, 3, true);
        setExceptType(ap.getPyObject(0, Py.None));
        setName(ap.getPyObject(1, Py.None));
        setBody(ap.getPyObject(2, Py.None));
        int lin = ap.getInt(3, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(4, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public ExceptHandler(PyObject type, PyObject name, PyObject body) {
        super(TYPE);
        setExceptType(type);
        setName(name);
        setBody(body);
    }

    // called from derived class
    public ExceptHandler(PyType subtype) {
        super(subtype);
    }

    public ExceptHandler(Token token, expr type, String name, java.util.List<stmt> body) {
        super(TYPE, token);
        this.type = type;
        if (this.type != null)
            this.type.setParent(this);
        this.name = name;
        this.body = body;
        if (body == null) {
            this.body = new ArrayList<>(0);
        }
        for(int i = 0; i < this.body.size(); i++) {
            PythonTree t = this.body.get(i);
            addChild(t, i, this.body);
        }
    }

    public ExceptHandler(PythonTree tree, expr type, String name, java.util.List<stmt> body) {
        super(TYPE, tree);
        this.type = type;
        if (this.type != null)
            this.type.setParent(this);
        this.name = name;
        this.body = body;
        if (body == null) {
            this.body = new ArrayList<>(0);
        }
        for(int i = 0; i < this.body.size(); i++) {
            PythonTree t = this.body.get(i);
            addChild(t, i, this.body);
        }
    }

    public ExceptHandler copy() {
        return new ExceptHandler(this.getToken(), this.type, this.name, this.body);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "ExceptHandler";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("ExceptHandler(");
        sb.append("type=");
        sb.append(dumpThis(type));
        sb.append(",");
        sb.append("name=");
        sb.append(dumpThis(name));
        sb.append(",");
        sb.append("body=");
        sb.append(dumpThis(body));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitExceptHandler(this);
    }

    public void traverse(VisitorIF<?> visitor) throws Exception {
        if (type != null)
            type.accept(visitor);
        if (body != null) {
            for (PythonTree t : body) {
                if (t != null)
                    t.accept(visitor);
            }
        }
    }

    public void replaceField(expr value, expr newValue) {
        if (value == type) this.type = newValue;
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
