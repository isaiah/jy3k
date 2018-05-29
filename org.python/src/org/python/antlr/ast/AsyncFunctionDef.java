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

@ExposedType(name = "_ast.AsyncFunctionDef", base = stmt.class)
public class AsyncFunctionDef extends stmt {
public static final PyType TYPE = PyType.fromClass(AsyncFunctionDef.class);
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
    new PyUnicode[] {new PyUnicode("name"), new PyUnicode("args"), new PyUnicode("body"), new
                      PyUnicode("decorator_list"), new PyUnicode("returns"), new
                      PyUnicode("docstring")};
    @ExposedGet(name = "_fields")
    public PyObject get_fields() { return new PyTuple(fields); }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyObject get_attributes() { return new PyTuple(attributes); }

    public AsyncFunctionDef() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject AsyncFunctionDef_new(PyNewWrapper _new, boolean init, PyType subtype,
    PyObject[] args, String[] keywords) {
        return new AsyncFunctionDef(subtype);
    }
    @ExposedMethod(names={"__init__"})
    public void AsyncFunctionDef___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("AsyncFunctionDef", args, keywords, new String[]
            {"name", "args", "body", "decorator_list", "returns", "docstring", "lineno",
              "col_offset"}, 6, true);
        setName(ap.getPyObject(0, Py.None));
        setArgs(ap.getPyObject(1, Py.None));
        setBody(ap.getPyObject(2, Py.None));
        setDecorator_list(ap.getPyObject(3, Py.None));
        setReturns(ap.getPyObject(4, Py.None));
        setDocstring(ap.getPyObject(5, Py.None));
        PyObject lin = ap.getOptionalArg(6);
        if (lin != null) {
            lineno = lin;
        }

        PyObject col = ap.getOptionalArg(7);
        if (col != null) {
            col_offset = col;
        }

    }

    public AsyncFunctionDef(PyObject name, PyObject args, PyObject body, PyObject decorator_list,
    PyObject returns, PyObject docstring) {
        super(TYPE);
        setName(name);
        setArgs(args);
        setBody(body);
        setDecorator_list(decorator_list);
        setReturns(returns);
        setDocstring(docstring);
    }

    // called from derived class
    public AsyncFunctionDef(PyType subtype) {
        super(subtype);
    }

    public AsyncFunctionDef(Node token, String name, arguments args, java.util.List<stmt> body,
    java.util.List<expr> decorator_list, expr returns, String docstring) {
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
        this.docstring = docstring;
    }

    public AsyncFunctionDef(Token token, String name, arguments args, java.util.List<stmt> body,
    java.util.List<expr> decorator_list, expr returns, String docstring) {
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
        this.docstring = docstring;
    }

    public AsyncFunctionDef(PythonTree tree, String name, arguments args, java.util.List<stmt>
    body, java.util.List<expr> decorator_list, expr returns, String docstring) {
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
        this.docstring = docstring;
    }

    public AsyncFunctionDef copy() {
        return new AsyncFunctionDef(this.getToken(), this.name, this.args, this.body,
        this.decorator_list, this.returns, this.docstring);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "AsyncFunctionDef";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("AsyncFunctionDef(");
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
        sb.append("docstring=");
        sb.append(dumpThis(docstring));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterAsyncFunctionDef(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveAsyncFunctionDef(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitAsyncFunctionDef(this);
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
