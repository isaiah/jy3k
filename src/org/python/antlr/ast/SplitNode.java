// Autogenerated AST node
package org.python.antlr.ast;
import org.antlr.v4.runtime.Token;
import org.python.antlr.PythonTree;
import org.python.antlr.adapter.AstAdapters;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;
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

@ExposedType(name = "_ast.SplitNode", base = stmt.class)
public class SplitNode extends stmt {
public static final PyType TYPE = PyType.fromClass(SplitNode.class);
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

    private stmt funcdef;
    public stmt getInternalFuncdef() {
        return funcdef;
    }
    public void setInternalFuncdef(stmt funcdef) {
        this.funcdef = funcdef;
    }
    @ExposedGet(name = "funcdef")
    public PyObject getFuncdef() {
        return funcdef;
    }
    @ExposedSet(name = "funcdef")
    public void setFuncdef(PyObject funcdef) {
        this.funcdef = AstAdapters.py2stmt(funcdef);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("name"), new PyUnicode("body"), new PyUnicode("funcdef")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public SplitNode() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void SplitNode___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("SplitNode", args, keywords, new String[]
            {"name", "body", "funcdef", "lineno", "col_offset"}, 3, true);
        setName(ap.getPyObject(0, Py.None));
        setBody(ap.getPyObject(1, Py.None));
        setFuncdef(ap.getPyObject(2, Py.None));
        int lin = ap.getInt(3, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(4, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public SplitNode(PyObject name, PyObject body, PyObject funcdef) {
        super(TYPE);
        setName(name);
        setBody(body);
        setFuncdef(funcdef);
    }

    // called from derived class
    public SplitNode(PyType subtype) {
        super(subtype);
    }

    public SplitNode(Token token, String name, java.util.List<stmt> body, stmt funcdef) {
        super(TYPE, token);
        this.name = name;
        this.body = body;
        if (body == null) {
            this.body = new ArrayList<>(0);
        }
        for(int i = 0; i < this.body.size(); i++) {
            PythonTree t = this.body.get(i);
            addChild(t, i, this.body);
        }
        this.funcdef = funcdef;
        if (this.funcdef != null)
            this.funcdef.setParent(this);
    }

    public SplitNode(PythonTree tree, String name, java.util.List<stmt> body, stmt funcdef) {
        super(TYPE, tree);
        this.name = name;
        this.body = body;
        if (body == null) {
            this.body = new ArrayList<>(0);
        }
        for(int i = 0; i < this.body.size(); i++) {
            PythonTree t = this.body.get(i);
            addChild(t, i, this.body);
        }
        this.funcdef = funcdef;
        if (this.funcdef != null)
            this.funcdef.setParent(this);
    }

    public SplitNode copy() {
        return new SplitNode(this.getToken(), this.name, this.body, this.funcdef);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "SplitNode";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("SplitNode(");
        sb.append("name=");
        sb.append(dumpThis(name));
        sb.append(",");
        sb.append("body=");
        sb.append(dumpThis(body));
        sb.append(",");
        sb.append("funcdef=");
        sb.append(dumpThis(funcdef));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitSplitNode(this);
    }

    public void traverse(VisitorIF<?> visitor) throws Exception {
        if (body != null) {
            for (PythonTree t : body) {
                if (t != null)
                    t.accept(visitor);
            }
        }
        if (funcdef != null)
            funcdef.accept(visitor);
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
