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

@ExposedType(name = "_ast.excepthandler", base = AST.class)
public abstract class excepthandler extends PythonTree {

    public static final PyType TYPE = PyType.fromClass(excepthandler.class);
    private final static PyUnicode[] fields = new PyUnicode[0];
    @ExposedGet(name = "_fields")
    public PyObject get_fields() { return Py.EmptyTuple; }

    private final static PyUnicode[] attributes =
    new PyUnicode[] {new PyUnicode("lineno"), new PyUnicode("col_offset")};
    @ExposedGet(name = "_attributes")
    public PyObject get_attributes() { return new PyTuple(attributes); }

    public excepthandler(PyType subtype) {
        super(subtype);
    }

    public excepthandler(PyType subtype, Token token) {
        super(subtype, token);
    }

    public excepthandler(PyType subtype, PythonTree node) {
        super(subtype, node);
    }

    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject excepthandler_new(PyNewWrapper _new, boolean init, PyType subtype,
    PyObject[] args, String[] keywords) {
        return new excepthandler(subtype) {
            public String toString() {
                return String.format("<_ast.excepthandler object at 0x%X>", Objects.hashCode(this));
            }
            public String toStringTree() {
                return toString();
            }
            public excepthandler copy() {
                return null;
            }
        };
    }
    public <R> R accept(VisitorIF<R> visitor) {
        throw Py.TypeError(String.format("expected some sort of excepthandler, but got %s", this));
    }
}
