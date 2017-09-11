package org.python.compiler;

import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedType;
import org.python.antlr.PythonTree;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@ExposedType
public class PySTEntryObject extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PySTEntryObject.class);
//        PyObject *ste_id;        /* int: key in ste_table->st_blocks */
//    PyObject *ste_symbols;   /* dict: variable names to flags */
//    PyObject *ste_name;      /* string: name of current block */
//    PyObject *ste_varnames;  /* list of function parameters */
//    PyObject *ste_children;  /* list of child blocks */
//    PyObject *ste_directives;/* locations of global and nonlocal statements */
//    _Py_block_ty ste_type;   /* module, class, or function */
//    int ste_nested;      /* true if block is nested */
//    unsigned ste_free : 1;        /* true if block has free variables */
//    unsigned ste_child_free : 1;  /* true if a child block has free vars,
//                                     including free refs to globals */
//    unsigned ste_generator : 1;   /* true if namespace is a generator */
//    unsigned ste_coroutine : 1;   /* true if namespace is a coroutine */
//    unsigned ste_varargs : 1;     /* true if block has varargs */
//    unsigned ste_varkeywords : 1; /* true if block has varkeywords */
//    unsigned ste_returns_value : 1;  /* true if namespace uses return with
//                                        an argument */
//    unsigned ste_needs_class_closure : 1; /* for class scopes, true if a
//                                             closure over __class__
//                                             should be created */
//    int ste_lineno;          /* first line of block */
//    int ste_col_offset;      /* offset of first line of block */
//    int ste_opt_lineno;      /* lineno of last exec or import * */
//    int ste_opt_col_offset;  /* offset of last exec or import * */
//    int ste_tmpname;         /* counter for listcomp temp vars */

//    struct symtable *ste_table;

    private PyObject id;
    Map<String, EnumSet<Symtable.Defs>> symbols;
    String name;
    List<String> varnames;
    List<PySTEntryObject> children;
    List<PyTuple> directives;
    BlockType type;
    boolean nested;
    boolean free, childFree, generator, coroutine, varargs, varkeywords, returnsValue, needsClassClosure;
    Symtable table;

    int lineno;
    int colOffset;
    int optLineno;
    int optColOffset;
    int tmpName;

    public enum BlockType {
        FunctionBlock(0), ClassBlock(1), ModuleBlock(2);

        private int value;

        private BlockType(int v) {
            value = v;
        }
    }

    PySTEntryObject(Symtable st, String name, PySTEntryObject.BlockType block, PythonTree key, int lineno, int colOffset) {
        super(TYPE);
        this.table = st;
        this.name = name;
        this.symbols = null;
        this.varnames = null;
        this.children = new ArrayList<>();
        this.directives = null;
        this.type = block;
        this.lineno = lineno;
        this.colOffset = colOffset;

    }
    public PySTEntryObject() {
        super(TYPE);
        type = BlockType.FunctionBlock;
    }

    @ExposedGet(name = "type")
    public int getBlockType() {
        return type.value;
    }
}
