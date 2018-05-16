package org.python.antlr;

import org.antlr.v4.runtime.Token;
import org.python.antlr.ast.Block;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.VisitorIF;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;
import org.python.core.Py;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.Traverseproc;
import org.python.core.Visitproc;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class PythonTree extends AST implements Traverseproc {
    private Token node;

    private List<stmt> block;

    private int index;

    /** Who is the parent node of this node; if null, implies node is root */
    private PythonTree parent;

    public PythonTree(PyType subtype) {
        super(subtype);
        node = null;
    }
    
    public PythonTree(PyType subtype, Token t) {
        super(subtype);
        node = t;
    }

    public PythonTree(PyType subtype, PythonTree tree) {
        super(subtype);
        node = tree.getNode();
    }
    
    public Token getNode() {
        return node;
    }

    public Token getToken() {
        return node;
    }

    public boolean isNil() {
        return node instanceof CommonTree;
    }

    public int getAntlrType() {
        return getToken().getType();
    }

    public String getText() {
        return node.getText();
    }

    public int getLine() {
        return node.getLine();
    }

    public int getCharPositionInLine() {
        return node.getCharPositionInLine();
    }

    public PythonTree getParent() {
        return parent;
    }

    public void setParent(PythonTree t) {
        this.parent = t;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setBlock(List<stmt> block) {
        this.block = block;
    }

    public void addChild(PythonTree t, int index, List<stmt> body) {
        t.setParent(this);
        t.setIndex(index);
        t.setBlock(body);
    }

//    public void addChild(expr t, int index, List<expr> body) {
//        t.setParent(this);
//        t.setIndex(index);
//        t.setBlock(body);
//    }

    /**
     * Converts a list of Name to a dotted-name string.
     * Because leading dots are indexable identifiers (referring
     * to parent directories in relative imports), a Name list
     * may include leading dots, but not dots between names.
     */
    public static String dottedNameListToString(List<Name> names) {
        if (names == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean leadingDot = true;
        for (int i = 0, len = names.size(); i < len; i++) {
            Name name = names.get(i);
            String id = name.getInternalId();
            if (id == null) {
                continue;
            }
            if (!".".equals(id)) {
                leadingDot = false;
            }
            sb.append(id);
            if (i < len - 1 && !leadingDot) {
                sb.append(".");
            }
        }
        return sb.toString();
    }

    public void replaceField(expr value, expr newValue) {}

    public abstract String toStringTree();

    protected String dumpThis(String s) {
        return s;
    }

    protected String dumpThis(Object o) {
        if (o instanceof PythonTree) {
            return ((PythonTree)o).toStringTree();
        }
        if (o instanceof Collection) {
            return (String) ((Collection) o).stream().map(el -> {
                if (el instanceof PythonTree) {
                    return ((PythonTree) el).toStringTree();
                }
                return el.toString();
            }).collect(Collectors.joining(",", "[", "]"));
        }
        return String.valueOf(o);
    }

    protected String dumpThis(Object[] s) {
        StringBuffer sb = new StringBuffer();
        if (s == null) {
            sb.append("null");
        } else {
            sb.append("(");
            for (int i = 0; i < s.length; i++) {
                if (i > 0)
                    sb.append(", ");
                sb.append(dumpThis(s[i]));
            }
            sb.append(")");
        }
        
        return sb.toString();
    }

    public PythonTree replaceSelf(expr other) {
        if (this instanceof expr) {
            getParent().replaceField((expr) this, other);
            return this;
        }
        throw new RuntimeException("only support expression");
    }

    public PythonTree replaceSelf(stmt... others) {
        return replaceSelf(Arrays.asList(others));
    }

    public PythonTree replaceSelf(List<stmt> others) {
        return replaceSelf(new Block(getToken(), others));
    }

    public PythonTree replaceSelf(stmt other) {
        if (this instanceof stmt) {
            other.setIndex(this.index);
            other.setParent(parent);
            other.setBlock(block);
            block.set(index, other);
            return other;
        }
        throw new RuntimeException("Only support statement: " + this);
    }

    protected PyObject lineno, col_offset;
    public int getLineno() {
        if (lineno == null) {
            if (getToken() == null) {
                throw Py.TypeErrorFmt("required field \"lineno\" missing from %s", this);
            }
            return getLine();
        }
        if (!(lineno instanceof PyLong)) {
            throw Py.ValueError("invalid integer value: " + lineno);
        }
        return lineno.asInt();
    }

    public int getCol_offset() {
        if (col_offset == null) {
            if (getToken() == null) {
                throw Py.TypeErrorFmt("required field \"coloffset\" missing from %s", this);
            }
            return getCharPositionInLine();
        }
        if (!(col_offset instanceof PyLong)) {
            throw Py.ValueError("invalid integer value: " + col_offset);
        }
        return col_offset.asInt();
    }

    public <R> boolean enter(VisitorIF<R> visitor) {
        throw new RuntimeException("not a node" + this);
    }

    public <R> void leave(VisitorIF<R> visitor) {
        throw new RuntimeException("not a node" + this);
    }

    public <R> R accept(VisitorIF<R> visitor) {
        throw new RuntimeException("not a node" + this);
    }

    public <R> void traverse(VisitorIF<R> visitor) {
        throw new RuntimeException("not a node");
    }

    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        return parent != null ? visit.visit(parent, arg) : 0;
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        return ob != null && ob == parent;
    }
}
