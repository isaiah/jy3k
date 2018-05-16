// Autogenerated AST node
package org.python.antlr.op;

import org.python.antlr.AST;
import org.python.antlr.base.expr_context;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.expr_contextType;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyUnicode;
import org.python.core.PyType;
import org.python.core.PyNewWrapper;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSet;
import org.python.annotations.ExposedType;
import org.python.annotations.ExposedSlot;
import org.python.annotations.SlotFunc;

@ExposedType(name = "_ast.AugStore", base = expr_context.class)
public class AugStore extends PythonTree {
    public static final PyType TYPE = PyType.fromClass(AugStore.class);

    public AugStore() {
        super(TYPE);
    }
    public AugStore(PyType subType) {
        super(subType);
    }
    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject AugStore_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[]
    args, String[] keywords) {
        return expr_contextType.AugStore.getImpl();
    }
    @ExposedMethod
    public void AugStore___init__(PyObject[] args, String[] keywords) {}

    private final static PyUnicode[] fields = new PyUnicode[0];
    @ExposedGet(name = "_fields")
    public PyObject get_fields() { return Py.EmptyTuple; }

    private final static PyUnicode[] attributes = new PyUnicode[0];
    @ExposedGet(name = "_attributes")
    public PyObject get_attributes() { return Py.EmptyTuple; }

    @Override
    public String toStringTree() {
        return "AugStore";
    }
}
