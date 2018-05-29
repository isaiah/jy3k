// Autogenerated AST node
package org.python.antlr.base;
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

@ExposedType(name = "_ast.expr", base = AST.class)
public abstract class expr extends PythonTree {

    public static final PyType TYPE = PyType.fromClass(expr.class);
    private final static PyUnicode[] fields = new PyUnicode[0];
    @ExposedGet(name = "_fields")
    public PyObject get_fields() { return Py.EmptyTuple; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyObject get_attributes() { return new PyTuple(attributes); }

    public abstract expr copy();
    public expr(PyType subtype) {
        super(subtype);
    }

    public expr(PyType subtype, Token token) {
        super(subtype, token);
    }

    public expr(PyType subtype, Node node) {
        super(subtype, node);
    }
    public expr(PyType subtype, PythonTree node) {
        super(subtype, node);
    }

    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject expr_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[]
    args, String[] keywords) {
        return new expr(subtype) {
            public String toString() {
                return String.format("<_ast.expr object at 0x%X>", Objects.hashCode(this));
            }
            public String toStringTree() {
                return toString();
            }
            public expr copy() {
                return null;
            }
        };
    }
    public <R> R accept(VisitorIF<R> visitor) {
        throw Py.TypeError(String.format("expected some sort of expr, but got %s", this));
    }
}
