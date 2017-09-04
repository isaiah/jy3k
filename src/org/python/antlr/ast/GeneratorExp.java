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

@ExposedType(name = "_ast.GeneratorExp", base = expr.class)
public class GeneratorExp extends expr {
public static final PyType TYPE = PyType.fromClass(GeneratorExp.class);
    private expr elt;
    public expr getInternalElt() {
        return elt;
    }
    public void setInternalElt(expr elt) {
        this.elt = elt;
    }
    @ExposedGet(name = "elt")
    public PyObject getElt() {
        return elt;
    }
    @ExposedSet(name = "elt")
    public void setElt(PyObject elt) {
        this.elt = AstAdapters.py2expr(elt);
    }

    private java.util.List<comprehension> generators;
    public java.util.List<comprehension> getInternalGenerators() {
        return generators;
    }
    public void setInternalGenerators(java.util.List<comprehension> generators) {
        this.generators = generators;
    }
    @ExposedGet(name = "generators")
    public PyObject getGenerators() {
        return new AstList(generators, AstAdapters.comprehensionAdapter);
    }
    @ExposedSet(name = "generators")
    public void setGenerators(PyObject generators) {
        this.generators = AstAdapters.py2comprehensionList(generators);
    }


    private final static PyUnicode[] fields =
    new PyUnicode[] {new PyUnicode("elt"), new PyUnicode("generators")};
    @ExposedGet(name = "_fields")
    public PyUnicode[] get_fields() { return fields; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyUnicode[] get_attributes() { return attributes; }

    public GeneratorExp() {
        super(TYPE);
    }
    @ExposedNew
    @ExposedMethod
    public void GeneratorExp___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("GeneratorExp", args, keywords, new String[]
            {"elt", "generators", "lineno", "col_offset"}, 2, true);
        setElt(ap.getPyObject(0, Py.None));
        setGenerators(ap.getPyObject(1, Py.None));
        int lin = ap.getInt(2, -1);
        if (lin != -1) {
            setLineno(lin);
        }

        int col = ap.getInt(3, -1);
        if (col != -1) {
            setLineno(col);
        }

    }

    public GeneratorExp(PyObject elt, PyObject generators) {
        super(TYPE);
        setElt(elt);
        setGenerators(generators);
    }

    // called from derived class
    public GeneratorExp(PyType subtype) {
        super(subtype);
    }

    public GeneratorExp(Token token, expr elt, java.util.List<comprehension> generators) {
        super(TYPE, token);
        this.elt = elt;
        if (this.elt != null)
            this.elt.setParent(this);
        this.generators = generators;
        if (generators == null) {
            this.generators = new ArrayList<>(0);
        }
        for(int i = 0; i < this.generators.size(); i++) {
            PythonTree t = this.generators.get(i);
            if (t != null)
                t.setParent(this);
        }
    }

    public GeneratorExp(PythonTree tree, expr elt, java.util.List<comprehension> generators) {
        super(TYPE, tree);
        this.elt = elt;
        if (this.elt != null)
            this.elt.setParent(this);
        this.generators = generators;
        if (generators == null) {
            this.generators = new ArrayList<>(0);
        }
        for(int i = 0; i < this.generators.size(); i++) {
            PythonTree t = this.generators.get(i);
            if (t != null)
                t.setParent(this);
        }
    }

    public GeneratorExp copy() {
        return new GeneratorExp(this.getToken(), this.elt, this.generators);
    }

    @ExposedGet(name = "repr")
    public String toString() {
        return "GeneratorExp";
    }

    @Override
    public String toStringTree() {
        StringBuffer sb = new StringBuffer("GeneratorExp(");
        sb.append("elt=");
        sb.append(dumpThis(elt));
        sb.append(",");
        sb.append("generators=");
        sb.append(dumpThis(generators));
        sb.append(",");
        sb.append(")");
        return sb.toString();
    }

    public <R> R accept(VisitorIF<R> visitor) throws Exception {
        return visitor.visitGeneratorExp(this);
    }

    public void traverse(VisitorIF<?> visitor) throws Exception {
        if (elt != null)
            elt.accept(visitor);
        if (generators != null) {
            for (PythonTree t : generators) {
                if (t != null)
                    t.accept(visitor);
            }
        }
    }

    public void replaceField(expr value, expr newValue) {
        if (value == elt) this.elt = newValue;
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
