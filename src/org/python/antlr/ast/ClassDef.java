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

@ExposedType(name = "_ast.ClassDef", base = stmt.class)
public class ClassDef extends stmt {
public static final PyType TYPE = PyType.fromClass(ClassDef.class);
    private String name;
    public String getInternalName() {
        return name;
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

    private java.util.List<expr> bases;
    public java.util.List<expr> getInternalBases() {
        return bases;
    }
    public void setInternalBases(java.util.List<expr> bases) {
        this.bases = bases;
    }
    @ExposedGet(name = "bases")
    public PyObject getBases() {
        return new AstList(bases, AstAdapters.exprAdapter);
    }
    @ExposedSet(name = "bases")
    public void setBases(PyObject bases) {
        this.bases = AstAdapters.py2exprList(bases);
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

    private java.util.List<expr> decorator_list;
    public java.util.List<expr> getInternalDecorator_list() {
        return decorator_list;
    }
    public void setInternalDecorator_list(java.util.List<expr> decorator_list) {
        this.decorator_list = decorator_list;
    }
    @ExposedGet(name = "decorator_list")
    public PyObject getDecorator_list() {
        return new AstList(decorator_list, AstAdapters.exprAdapter);
    }
    @ExposedSet(name = "decorator_list")
    public void setDecorator_list(PyObject decorator_list) {
        this.decorator_list = AstAdapters.py2exprList(decorator_list);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("name"), new PyUnicode("bases"), new PyUnicode("keywords"), new
                      PyUnicode("body"), new PyUnicode("decorator_list")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public ClassDef() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void ClassDef___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("ClassDef", args, keywords, new String[]
            {"name", "bases", "keywords", "body", "decorator_list", "lineno", "col_offset"}, 5,
              true);
        setName(ap.getPyObject(0, Py.None));
        setBases(ap.getPyObject(1, Py.None));
        setKeywords(ap.getPyObject(2, Py.None));
        setBody(ap.getPyObject(3, Py.None));
        setDecorator_list(ap.getPyObject(4, Py.None));
        int lin = ap.getInt(5, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(6, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public ClassDef(PyObject name, PyObject bases, PyObject keywords, PyObject body, PyObject
    decorator_list) {
        super(TYPE);
        setName(name);
        setBases(bases);
        setKeywords(keywords);
        setBody(body);
        setDecorator_list(decorator_list);
    }

    // called from derived class
    public ClassDef(PyType subtype) {
        super(subtype);
    }

    public ClassDef(Token token, String name, java.util.List<expr> bases, java.util.List<keyword>
    keywords, java.util.List<stmt> body, java.util.List<expr> decorator_list) {
        super(TYPE, token);
        this.name = name;
        this.bases = bases;
        if (bases == null) {
            this.bases = new ArrayList<>(0);
        }
        this.keywords = keywords;
        if (keywords == null) {
            this.keywords = new ArrayList<>(0);
        }
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
    }

    public ClassDef(PythonTree tree, String name, java.util.List<expr> bases,
    java.util.List<keyword> keywords, java.util.List<stmt> body, java.util.List<expr>
    decorator_list) {
        super(TYPE, tree);
        this.name = name;
        this.bases = bases;
        if (bases == null) {
            this.bases = new ArrayList<>(0);
        }
        this.keywords = keywords;
        if (keywords == null) {
            this.keywords = new ArrayList<>(0);
        }
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
    }

    public ClassDef copy() {
        return new ClassDef(this.getToken(), this.name, this.bases, this.keywords, this.body,
        this.decorator_list);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "ClassDef";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("ClassDef(");
        sb.append("name=");
        sb.append(dumpThis(name));
        sb.append(",");
        sb.append("bases=");
        sb.append(dumpThis(bases));
        sb.append(",");
        sb.append("keywords=");
        sb.append(dumpThis(keywords));
        sb.append(",");
        sb.append("body=");
        sb.append(dumpThis(body));
        sb.append(",");
        sb.append("decorator_list=");
        sb.append(dumpThis(decorator_list));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitClassDef(this);
    }

    public void traverse(VisitorIF<?> visitor) throws Exception {
        if (bases != null) {
            for (PythonTree t : bases) {
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
