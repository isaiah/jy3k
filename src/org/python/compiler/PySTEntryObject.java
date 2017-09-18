package org.python.compiler;

import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedType;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.arguments;
import org.python.compiler.Symtable.Flag;
import org.python.core.PyDictionary;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PySyntaxError;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.core.PyUnicode;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.python.compiler.Symtable.Flag.*;

@ExposedType
public class PySTEntryObject extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PySTEntryObject.class);
//    PyObject *ste_id;        /* int: key in ste_table->st_blocks */
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
    Map<String, EnumSet<Flag>> symbols;
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
    private int id;

    /** XXX Old compiler stuff, it could be merged with CompileUnit */
    ArgListCompiler ac;
    PySTEntryObject(Symtable st, String name, PySTEntryObject.BlockType block, PythonTree key, int lineno, int colOffset, arguments args) {
        super(TYPE);
        this.table = st;
        this.id = System.identityHashCode(key);
        this.name = name;
        if (st.cur != null && (st.cur.nested || st.cur.type == BlockType.FunctionBlock)) {
            nested = true;
        }
        this.symbols = new HashMap<>();
        this.varnames = new ArrayList<>();
        this.children = new ArrayList<>();
        this.directives = new ArrayList<>();
        this.type = block;
        this.lineno = lineno;
        this.colOffset = colOffset;
        this.ac = new ArgListCompiler();
        ac.visitArgs(args);
        st.blocks.put(id, this);
    }

    public PySTEntryObject() {
        super(TYPE);
        type = BlockType.FunctionBlock;
    }

    /* Enter the final scope information into the ste_symbols dict.
     *
     * All arguments are dicts.  Modifies symbols, others are read-only.
    */
    private static void updateSymbols(Map<String, EnumSet<Flag>> symbols, Map<String, Flag> scopes,
                                      Set<String> bound, Set<String> free, boolean isClass) {
        for (String name : symbols.keySet()) {
            EnumSet<Flag> flags = symbols.get(name);
            Flag vScope = scopes.get(name);
            flags.add(vScope);
            symbols.put(name, flags);
        }

        /* Record not yet resolved free variables from children (if any) */
        for (String name : free) {
            /* Handle symbol that already exists in this scope */
            if (symbols.containsKey(name)) {
                EnumSet<Flag> flags = symbols.get(name);
                if (isClass && (flags.contains(DEF_GLOBAL) || flags.stream().anyMatch(DEF_BOUND::contains))) {
                    flags.add(DEF_FREE_CLASS);
                }
                /* It's a cell, or already free in this scope */
                continue;
            }
            /* Handle global symbol */
            if (!bound.contains(name)) {
                continue; /* it's a global */
            }
            /* Propagate new free symbol up the lexical stack */
            symbols.put(name, EnumSet.of(FREE));
        }
    }

    private static void analyzeCells(Map<String, Flag> scopes, Set<String> free) {
        for (String name : scopes.keySet()) {
            if (scopes.get(name) == LOCAL) {
                continue;
            }
            if (!free.contains(name)) {
                continue;
            }
            /* Replace DEF_LOCAL with CELL for this name, and remove
               from free. It is safe to replace the value of name
               in the dict, because it will not cause a resize.
             */
            scopes.put(name, Flag.CELL);
            free.remove(name);
        }
    }

    @ExposedGet(name = "lineno")
    public int getLineno() {
        return lineno;
    }

    @ExposedGet(name = "symbols")
    public Map<String, EnumSet<Flag>> getSymbols() {
        Map<PyObject, PyObject> ret = new HashMap<>(symbols.size());
        for (String key : symbols.keySet()) {
            ret.put(new PyUnicode(key), new PyLong(getFlags(symbols.get(key))));
        }
        return new PyDictionary(ret);
    }

    private long getFlags(EnumSet<Flag> flags) {
        long ret = 0;
        for (Flag flag : flags) {
            ret |= flag.value;
        }
        return ret;
    }

    @ExposedGet(name = "nested")
    public boolean isNested() {
        return nested;
    }

    @ExposedGet(name = "name")
    public String getName() {
        return name;
    }

    @ExposedGet(name = "type")
    public int getBlockType() {
        return type.value;
    }

    @ExposedGet(name = "children")
    public PyObject getChildren() {
        if (children == null) {
            return new PyList();
        }
        return new PyList(children);
    }

    public void analyzeBlock(Set<String> bound, Set<String> free, Set<String> global) {
        Set<String> local = new HashSet<>();
        Map<String, Flag> scopes = new HashMap<>();
        Set<String> newGlobal = new HashSet<>();
        Set<String> newFree = new HashSet<>();
        Set<String> newBound = new HashSet<>();
        if (type == BlockType.ClassBlock) {
            newGlobal.addAll(global);
            newBound.addAll(bound);
        }
        for (String name : symbols.keySet()) {
            EnumSet<Flag> flags = symbols.get(name);
            analyzeName(scopes, name, flags, bound, local, free, global);
        }
        /* Populate global and bound sets to be passed to children. */
        if (type != BlockType.ClassBlock) {
            /* Add function locals to bound set */
            if (type == BlockType.FunctionBlock) {
                newBound.addAll(local);
            }
            if (!bound.isEmpty()) {
                newBound.addAll(bound);
            }
            newGlobal.addAll(global);
        } else {
            /** Special case __class__ */
            newBound.add("__class__");
        }

        /* Recursively call analyze_child_block() on each child block.

           newbound, newglobal now contain the names visible in
           nested blocks.  The free variables in the children will
           be collected in allfree.
        */
        Set<String> allFree = new HashSet<>();
        for (PySTEntryObject entry : children) {
            entry.analyzeChildBlock(newBound, newFree, newGlobal, allFree);
            if (entry.free || entry.childFree) {
                childFree = true;
            }
        }
        newFree.addAll(allFree);
        /* Check if any local variables must be converted to cell variables */
        if (type == BlockType.FunctionBlock) {
            analyzeCells(scopes, newFree);
        } else if (type == BlockType.ClassBlock) {
            dropClassFree(newFree);
        }
        updateSymbols(symbols, scopes, bound, newFree, type == BlockType.ClassBlock);
        free.addAll(newFree);
    }

    private void dropClassFree(Set<String> free) {
        free.remove("__class__");
        needsClassClosure = true;
    }

    private void analyzeChildBlock(Set<String> bound, Set<String> free, Set<String> global, Set<String> childFree) {
        Set<String> tempBound = new HashSet<>(bound);
        Set<String> tempFree = new HashSet<>(free);
        Set<String> tempGlobal = new HashSet<>(global);

        analyzeBlock(tempBound, tempFree, tempGlobal);
        childFree.addAll(tempFree);
    }

    /* Decide on scope of name, given flags.

       The namespace dictionaries may be modified to record information
       about the new name.  For example, a new global will add an entry to
       global.  A name that was global can be changed to local.
    */
    private void analyzeName(Map<String, Flag> scopes, String name, EnumSet<Flag> flags, Set<String> bound,
                             Set<String> local, Set<String> free, Set<String> global) {
        if (flags.contains(DEF_GLOBAL)) {
            if (flags.contains(DEF_PARAM)) {
                throw errorAtDirective(name, "name '%s' is parameter and global");
            }
            if (flags.contains(DEF_NONLOCAL)) {
                throw errorAtDirective(name, "name '%s' is nonlocal and global");
            }
            scopes.put(name, GLOBAL_EXPLICIT);
            global.add(name);
            if (!bound.isEmpty()) {
                bound.remove(name);
            }
            return;
        }

        if (flags.contains(Flag.DEF_NONLOCAL)) {
            if (flags.contains(Flag.DEF_PARAM)) {
                throw errorAtDirective(name, "name '%s' is parameter and nonlocal");
            }
            if (bound.isEmpty()) {
                throw errorAtDirective(name, "nonlocal declaration '%s' not allowed at module level");
            }
            if (!bound.contains(name)) {
                throw errorAtDirective(name, "no binding for nonlocal '%s' found");
            }
            scopes.put(name, FREE);
            this.free = true;
            free.add(name);
            return;
        }

        if (flags.stream().anyMatch(Flag.DEF_BOUND::contains)) {
            scopes.put(name, LOCAL);
            local.add(name);
            global.remove(name);
            return;
        }

        /* If an enclosing block has a binding for this name, it
           is a free variable rather than a global variable.
           Note that having a non-NULL bound implies that the block
           is nested.
        */
        if (bound.contains(name)) {
            scopes.put(name, FREE);
            this.free = true;
            free.add(name);
            return;
        }

        /* If a parent has a global statement, then call it global
           explicit?  It could also be global implicit.
         */
        if (global.contains(name)) {
            scopes.put(name, GLOBAL_IMPLICIT);
            return;
        }

        if (nested) {
            this.free = true;
        }
        scopes.put(name, GLOBAL_IMPLICIT);
    }

    private PySyntaxError errorAtDirective(String name, String template) {
        return new PySyntaxError(String.format(template, name), lineno, colOffset, "", table.getFilename());
    }

    public enum BlockType {
        FunctionBlock(0), ClassBlock(1), ModuleBlock(2);

        private int value;

        private BlockType(int v) {
            value = v;
        }
    }
}
