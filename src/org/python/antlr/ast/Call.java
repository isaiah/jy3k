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
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSet;
import org.python.annotations.ExposedType;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

@ExposedType(name = "_ast.Call", base = expr.class)
public class Call extends expr {
public static final PyType TYPE = PyType.fromClass(Call.class);
    private expr func;
    public expr getInternalFunc() {
        return func;
    }
    public void setInternalFunc(expr func) {
        this.func = func;
    }
    @ExposedGet(name = "func")
    public PyObject getFunc() {
        return func;
    }
    @ExposedSet(name = "func")
    public void setFunc(PyObject func) {
        this.func = AstAdapters.py2expr(func);
    }

    private java.util.List<expr> args;
    public java.util.List<expr> getInternalArgs() {
        return args;
    }
    public void setInternalArgs(java.util.List<expr> args) {
        this.args = args;
    }
    @ExposedGet(name = "args")
    public PyObject getArgs() {
        return new AstList(args, AstAdapters.exprAdapter);
    }
    @ExposedSet(name = "args")
    public void setArgs(PyObject args) {
        this.args = AstAdapters.py2exprList(args);
    }

    private java.util.List<keyword> keywords;
    public java.util.List<keyword> getInternalKeywords() {
        return keywords;
    }
    public void setInternalKeywords(java.util.List<keyword> keywords) {
        this.keywords = keywords;
    }
    @ExposedGet(name = "keywords")
    public PyObject getKeywords() {
        return new AstList(keywords, AstAdapters.keywordAdapter);
    }
    @ExposedSet(name = "keywords")
    public void setKeywords(PyObject keywords) {
        this.keywords = AstAdapters.py2keywordList(keywords);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("func"), new PyUnicode("args"), new PyUnicode("keywords")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public Call() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void Call___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("Call", args, keywords, new String[]
            {"func", "args", "keywords", "lineno", "col_offset"}, 3, true);
        setFunc(ap.getPyObject(0, Py.None));
        setArgs(ap.getPyObject(1, Py.None));
        setKeywords(ap.getPyObject(2, Py.None));
        int lin = ap.getInt(3, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(4, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public Call(PyObject func, PyObject args, PyObject keywords) {
        super(TYPE);
        setFunc(func);
        setArgs(args);
        setKeywords(keywords);
    }

    // called from derived class
    public Call(PyType subtype) {
        super(subtype);
    }

    public Call(Token token, expr func, java.util.List<expr> args, java.util.List<keyword>
    keywords) {
        super(TYPE, token);
        this.func = func;
        if (this.func != null)
            this.func.setParent(this);
        this.args = args;
        if (args == null) {
            this.args = new ArrayList<>(0);
        }
        for(int i = 0; i < this.args.size(); i++) {
            PythonTree t = this.args.get(i);
            if (t != null)
                t.setParent(this);
        }
        this.keywords = keywords;
        if (keywords == null) {
            this.keywords = new ArrayList<>(0);
        }
        for(int i = 0; i < this.keywords.size(); i++) {
            PythonTree t = this.keywords.get(i);
            if (t != null)
                t.setParent(this);
        }
    }

    public Call(PythonTree tree, expr func, java.util.List<expr> args, java.util.List<keyword>
    keywords) {
        super(TYPE, tree);
        this.func = func;
        if (this.func != null)
            this.func.setParent(this);
        this.args = args;
        if (args == null) {
            this.args = new ArrayList<>(0);
        }
        for(int i = 0; i < this.args.size(); i++) {
            PythonTree t = this.args.get(i);
            if (t != null)
                t.setParent(this);
        }
        this.keywords = keywords;
        if (keywords == null) {
            this.keywords = new ArrayList<>(0);
        }
        for(int i = 0; i < this.keywords.size(); i++) {
            PythonTree t = this.keywords.get(i);
            if (t != null)
                t.setParent(this);
        }
    }

    public Call copy() {
        return new Call(this.getToken(), this.func, this.args, this.keywords);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "Call";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("Call(");
        sb.append("func=");
        sb.append(dumpThis(func));
        sb.append(",");
        sb.append("args=");
        sb.append(dumpThis(args));
        sb.append(",");
        sb.append("keywords=");
        sb.append(dumpThis(keywords));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        return visitor.enterCall(this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        visitor.leaveCall(this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        return visitor.visitCall(this);
    }

    public <R> void traverse(VisitorIF<R> visitor) {
        if (func != null)
            func.accept(visitor);
        if (args != null) {
            for (PythonTree t : args) {
                if (t != null)
                    t.accept(visitor);
            }
        }
        if (keywords != null) {
            for (PythonTree t : keywords) {
                if (t != null)
                    t.accept(visitor);
            }
        }
    }

    public void replaceField(expr value, expr newValue) {
        if (value == func) this.func = newValue;
        for (int i=0;i<this.args.size();i++){
            expr thisVal = this.args.get(i);
            if (value == thisVal) this.args.set(i,newValue);
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
