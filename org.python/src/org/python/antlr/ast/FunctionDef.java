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
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

@ExposedType(name = "_ast.FunctionDef", base = stmt.class)
public class FunctionDef extends stmt {
public static final PyType TYPE = PyType.fromClass(FunctionDef.class);
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

    private arguments args;
    public arguments getInternalArgs() {
        return args;
    }
    public void setInternalArgs(arguments args) {
        this.args = args;
    }
    @ExposedGet(name = "args")
    public PyObject getArgs() {
        return args;
    }
    @ExposedSet(name = "args")
    public void setArgs(PyObject args) {
        this.args = AstAdapters.py2arguments(args);
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
        return new PyList(body);
    }
    @ExposedSet(name = "body")
    public void setBody(PyObject body) {
        this.body = AstAdapters.py2stmtList(body);
    }

    private java.util.List<expr> decorator_list;
    public java.util.List<expr> getInternalDecorator_list() {
        return decorator_list;
    }
    public void setInternalDecorator_list(java.util.List<expr> decorator_list) {
        this.decorator_list = decorator_list;
    }
    @ExposedGet(name = "decorator_list")
    public PyObject getDecorator_list() {
        return new PyList(decorator_list);
    }
    @ExposedSet(name = "decorator_list")
    public void setDecorator_list(PyObject decorator_list) {
        this.decorator_list = AstAdapters.py2exprList(decorator_list);
    }

    private expr returns;
    public expr getInternalReturns() {
        return returns;
    }
    public void setInternalReturns(expr returns) {
        this.returns = returns;
    }
    @ExposedGet(name = "returns")
    public PyObject getReturns() {
        return returns;
    }
    @ExposedSet(name = "returns")
    public void setReturns(PyObject returns) {
        this.returns = AstAdapters.py2expr(returns);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("name"), new PyUnicode("args"), new PyUnicode("body"), new
                      PyUnicode("decorator_list"), new PyUnicode("returns")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public FunctionDef() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject FunctionDef_new(PyNewWrapper _new, boolean init, PyType subtype,
    PyObject[] args, String[] keywords) {
        return new FunctionDef(subtype);
    }
    @ExposedMethod(names={"__init__"})
    public void FunctionDef___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("FunctionDef", args, keywords, new String[]
            {"name", "args", "body", "decorator_list", "returns", "lineno", "col_offset"}, 5, true);
        setName(ap.getPyObject(0, Py.None));
        setArgs(ap.getPyObject(1, Py.None));
        setBody(ap.getPyObject(2, Py.None));
        setDecorator_list(ap.getPyObject(3, Py.None));
        setReturns(ap.getPyObject(4, Py.None));
        int lin = ap.getInt(5, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(6, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public FunctionDef(PyObject name, PyObject args, PyObject body, PyObject decorator_list,
    PyObject returns) {
        super(TYPE);
        setName(name);
        setArgs(args);
        setBody(body);
        setDecorator_list(decorator_list);
        setReturns(returns);
    }

    // called from derived class
    public FunctionDef(PyType subtype) {
        super(subtype);
    }

    public FunctionDef(Token token, String name, arguments args, java.util.List<stmt> body,
    java.util.List<expr> decorator_list, expr returns) {
        super(TYPE, token);
        this.name = name;
        this.args = args;
        if (this.args != null)
            this.args.setParent(this);
        this.body = body;
        if (body == null) {
            this.body = new ArrayList<>(0);
        }
        for(int i = 0; i < this.body.size(); i++) {
            PythonTree t = this.body.get(i);
            addChild(t, i, this.body);
        }
        this.decorator_list = decorator_list;
        if (decorator_list == null) {
            this.decorator_list = new ArrayList<>(0);
        }
        for(int i = 0; i < this.decorator_list.size(); i++) {
            PythonTree t = this.decorator_list.get(i);
            if (t != null)
                t.setParent(this);
        }
        this.returns = returns;
        if (this.returns != null)
            this.returns.setParent(this);
    }

    public FunctionDef(PythonTree tree, String name, arguments args, java.util.List<stmt> body,
    java.util.List<expr> decorator_list, expr returns) {
        super(TYPE, tree);
        this.name = name;
        this.args = args;
        if (this.args != null)
            this.args.setParent(this);
        this.body = body;
        if (body == null) {
            this.body = new ArrayList<>(0);
        }
        for(int i = 0; i < this.body.size(); i++) {
            PythonTree t = this.body.get(i);
            addChild(t, i, this.body);
        }
        this.decorator_list = decorator_list;
        if (decorator_list == null) {
            this.decorator_list = new ArrayList<>(0);
        }
        for(int i = 0; i < this.decorator_list.size(); i++) {
            PythonTree t = this.decorator_list.get(i);
            if (t != null)
                t.setParent(this);
        }
        this.returns = returns;
        if (this.returns != null)
            this.returns.setParent(this);
    }

    public FunctionDef copy() {
        return new FunctionDef(this.getToken(), this.name, this.args, this.body,
        this.decorator_list, this.returns);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "FunctionDef";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("FunctionDef(");
        sb.append("name=");
        sb.append(dumpThis(name));
        sb.append(",");
        sb.append("args=");
        sb.append(dumpThis(args));
        sb.append(",");
        sb.append("body=");
        sb.append(dumpThis(body));
        sb.append(",");
        sb.append("decorator_list=");
        sb.append(dumpThis(decorator_list));
        sb.append(",");
        sb.append("returns=");
        sb.append(dumpThis(returns));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterFunctionDef(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveFunctionDef(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitFunctionDef(this);
    }

    public <R> void traverse(VisitorIF<R> visitor) {
        if (args != null)
            args.accept(visitor);
        if (body != null) {
            for (PythonTree t : body) {
                if (t != null)
                    t.accept(visitor);
            }
        }
        if (decorator_list != null) {
            for (PythonTree t : decorator_list) {
                if (t != null)
                    t.accept(visitor);
            }
        }
        if (returns != null)
            returns.accept(visitor);
    }

    public void replaceField(expr value, expr newValue) {
        for (int i=0;i<this.decorator_list.size();i++){
            expr thisVal = this.decorator_list.get(i);
            if (value == thisVal) this.decorator_list.set(i,newValue);
        }
        if (value == returns) this.returns = newValue;
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


    private boolean Split;

    public boolean isSplit() {
        return Split;
    }

    public void setSplit(boolean Split) {
        this.Split = Split;
    }
}
