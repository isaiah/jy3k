package org.python.compiler;

import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.AnnAssign;
import org.python.antlr.ast.Assert;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.AugAssign;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.Delete;
import org.python.antlr.ast.For;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Global;
import org.python.antlr.ast.If;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Raise;
import org.python.antlr.ast.Return;
import org.python.antlr.ast.Try;
import org.python.antlr.ast.While;
import org.python.antlr.ast.arg;
import org.python.antlr.ast.arguments;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PySyntaxError;
import org.python.core.PyTuple;
import org.python.core.PyUnicode;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Symtable extends Visitor {
    public enum Defs {
        GLOBAL(1), LOCAL(2), PARAM(2<<1), NON_LOCAL(2<<2),
        USE(2<<3), FREE(2<<4), FREE_CLASS(2<<5), IMPORT(2<<6),
        ANNOT(2<<7), BOUND(LOCAL.value | PARAM.value | IMPORT.value);

        int value;
        private Defs(int val) {
            value = val;
        }
    }
    public static final int LOCAL = 1;
    public static final int GLOBAL_EXPLICIT = 2;
    public static final int GLOBAL_IMPLICIT = 3;
    public static final int FREE = 4;
    public static final int CELL = 5;

    public static final int DEF_GLOBAL = 1;
    public static final int DEF_LOCAL = 2;
    public static final int DEF_PARAM = 2 << 1;
    public static final int DEF_NONLOCAL = 2 << 2;
    public static final int USE = 2 << 3;
    public static final int DEF_FREE = 2 << 4;
    public static final int DEF_FREE_CLASS = 2 << 5;
    public static final int DEF_IMPORT = 2 << 6;
    public static final int DEF_ANNOT = 2 << 7;
    public static final int DEF_BOUND = DEF_LOCAL | DEF_PARAM | DEF_IMPORT;

    public static final int SCOPE_MASK = DEF_GLOBAL | DEF_LOCAL | DEF_PARAM | DEF_NONLOCAL;

    public static final int TYPE_FUNCTION = 0;
    public static final int TYPE_CLASS = 1;
    public static final int TYPE_MODULE = 2;


    public static final int SCOPE_OFFSET = 11;

//    PyObject *st_filename;          /* name of file being compiled,
//                                       decoded from the filesystem encoding */
//    struct _symtable_entry *st_cur; /* current symbol table entry */
//    struct _symtable_entry *st_top; /* symbol table entry for module */
//    PyObject *st_blocks;            /* dict: map AST node addresses
//                                     *       to symbol table entries */
//    PyObject *st_stack;             /* list: stack of namespace info */
//    PyObject *st_global;            /* borrowed ref to st_top->ste_symbols */
//    int st_nblocks;                 /* number of blocks used. kept for
//                                       consistency with the corresponding
//                                       compiler structure */
//    PyObject *st_private;           /* name of current class or NULL */
//    PyFutureFeatures *st_future;    /* module's future features that affect
//                                       the symbol table */
//    int recursion_depth;            /* current recursion depth */
//    int recursion_limit;            /* recursion limit */
    private String filename;
    private PySTEntryObject cur;
    private PySTEntryObject top;
    private Map<?, ?> blocks;
    private List<PySTEntryObject> stack;
    private Map<String, EnumSet<Defs>> global;

    private int nblocks;
    private PyObject _private;
//    private PyFutureFeatures future;
    private int recursionDeps;
    private int recursionLimit;

    private static PyObject _top = null;

    private Symtable() {
        stack = new ArrayList<>();
        blocks = new HashMap<>();
    }

    // PySymtable_BuildObject
    public static Symtable buildObject(PythonTree mod, String filename, int future) {
        Symtable st = new Symtable();
        st.filename = filename;

        /* Make the initial symbol information gathering pass */
        if (_top == null) {
            st.enterBlock(_top.toString() /** FIXME */, PySTEntryObject.BlockType.ModuleBlock, mod, 0, 0);
        }
        st.top = st.cur;
        mod.traverse(st);
    }

    private void enterBlock(String name, PySTEntryObject.BlockType block, PythonTree ast, int lineno, int colOffset) {
        PySTEntryObject prev = null;
        PySTEntryObject ste = new PySTEntryObject(this, name, block, ast, lineno, colOffset);
        stack.add(ste);
        prev = cur;
        cur = ste;
        if (block == PySTEntryObject.BlockType.ModuleBlock) {
            global = cur.symbols;
        }
        if (prev != null) {
            prev.children.add(ste);
        }
    }

    private void exitBlock(PythonTree node) {
        cur = null;
        int size = stack.size();
        if (size > 1) {
            cur = stack.get(size - 1);
        }
    }

    private void addDef(String name, Defs flag, Defs... otherFlags) {
        EnumSet<Defs> flags = EnumSet.of(flag, otherFlags);
        EnumSet<Defs> val = cur.symbols.get(name);
        if (val != null) {
            if (flags.contains(Defs.PARAM) && val.contains(Defs.PARAM)) {
                throw new PySyntaxError(String.format("duplicated definition of argument: %s", name), cur.lineno, cur.colOffset, "", filename);
            }
            val.addAll(flags);
        } else {
            val = flags;
        }
        cur.symbols.put(name, val);
        if (flags.contains(Defs.PARAM)) {
            cur.varnames.add(name);
        } else if (flags.contains(Defs.GLOBAL)) {
            val = flags;
            if (global.containsKey(name)) {
                val.addAll(global.get(name));
            }
            global.put(name, val);
        }
    }

    private void visitArgannotations(List<arg> args) {
        for (arg a : args) {
            if (a.getInternalAnnotation() != null) {
                visit(a.getInternalAnnotation());
            }
        }
    }

    private void visitAnnotations(arguments a, expr returns) {
        visitArgannotations(a.getInternalArgs());
        if (a.getInternalVararg() != null && a.getInternalVararg().getInternalAnnotation() != null) {
            visit(a.getInternalVararg().getInternalAnnotation());
        }
        if (a.getInternalKwarg() != null && a.getInternalKwarg().getInternalAnnotation() != null) {
            visit(a.getInternalVararg().getInternalAnnotation());
        }
        visitArgannotations(a.getInternalKwonlyargs());
        if (returns != null) {
            visit(returns);
        }
    }

    private void visitSeq(List<? extends PythonTree> seq) {
        if (seq == null) {
            return;
        }
        for (PythonTree s: seq) {
            s.accept(this);
        }
    }

    private EnumSet<Defs> lookup(String name) {
        return cur.symbols.get(name);
    }

    private void recordDirective(String name, stmt s) {
        if (cur.directives == null) {
            cur.directives = new ArrayList<>();
        }
        cur.directives.add(new PyTuple(new PyUnicode(name), new PyLong(s.getLineno()), new PyLong(s.getCol_offset())));
    }

    //region AST Visitor Implementation

    @Override
    public Object visitFunctionDef(FunctionDef node) {
        addDef(node.getInternalName(), Defs.LOCAL);
        arguments args = node.getInternalArgs();
        visitSeq(args.getInternalDefaults());
        visitSeq(args.getInternalDefaults());
        visitAnnotations(args, node.getInternalReturns());
        visitSeq(node.getInternalDecorator_list());
        enterBlock(node.getInternalName(), PySTEntryObject.BlockType.FunctionBlock, node, node.getLineno(), node.getCol_offset());
        visit(args);
        visitSeq(node.getInternalBody());
        exitBlock(node);
        return null;
    }

    @Override
    public Object visitClassDef(ClassDef node) {
        addDef(node.getInternalName(), Defs.LOCAL);
        visitSeq(node.getInternalBases());
        visitSeq(node.getInternalKeywords());
        visitSeq(node.getInternalDecorator_list());
        enterBlock(node.getInternalName(), PySTEntryObject.BlockType.ClassBlock, node, node.getLineno(), node.getCol_offset());
        PyObject tmp = _private;
        _private = node.getName();
        visitSeq(node.getInternalBody());
        _private = tmp;
        exitBlock(node);
        return null;
    }

    @Override
    public Object visitReturn(Return node) {
        if (node.getInternalValue() != null) {
            visit(node.getInternalValue());
            cur.returnsValue = true;
        }
        return null;
    }

    @Override
    public Object visitDelete(Delete node) {
        visitSeq(node.getInternalTargets());
        return null;
    }

    @Override
    public Object visitAssign(Assign node) {
        visitSeq(node.getInternalTargets());
        visit(node.getInternalValue());
        return null;
    }

    @Override
    public Object visitAnnAssign(AnnAssign node) {
        if (node.getInternalTarget() instanceof Name) {
            expr eName = node.getInternalTarget();
            EnumSet<Defs> flags = lookup(((Name) node.getInternalTarget()).getInternalId());
            if ((flags.contains(Defs.GLOBAL) || flags.contains(Defs.NON_LOCAL)) && node.getInternalSimple() > 0) {
                throw new PySyntaxError(
                        String.format(flags.contains(Defs.GLOBAL) ? GLOBAL_ANNO : NONLOCAL_ANNO,
                                ((Name) eName).getInternalId()),
                        node.getLine(), node.getCol_offset(), "", filename);
            }

            if (node.getInternalSimple() > 0) {
                addDef(((Name) eName).getInternalId(), Defs.ANNOT, Defs.LOCAL);
            } else if (node.getInternalValue() != null) {
                addDef(((Name) eName).getInternalId(), Defs.LOCAL);
            }
        } else {
            visit(node.getInternalTarget());
        }
        visit(node.getInternalAnnotation());
        if (node.getInternalValue() != null) {
            visit(node.getInternalValue());
        }

        return null;
    }

    @Override
    public Object visitAugAssign(AugAssign node) {
        visit(node.getInternalTarget());
        visit(node.getInternalValue());
        return null;
    }

    @Override
    public Object visitFor(For node) {
        visit(node.getInternalTarget());
        visit(node.getInternalIter());
        visitSeq(node.getInternalBody());
        visitSeq(node.getInternalOrelse());
        return null;
    }

    @Override
    public Object visitWhile(While node) {
        visit(node.getInternalTest());
        visitSeq(node.getInternalBody());
        visitSeq(node.getInternalOrelse());
        return null;
    }

    @Override
    public Object visitIf(If node) {
        visit(node.getInternalTest());
        visitSeq(node.getInternalBody());
        visitSeq(node.getInternalOrelse());
        return null;
    }

    @Override
    public Object visitRaise(Raise node) {
        if (node.getInternalExc() != null) {
            visit(node.getInternalExc());
            if (node.getInternalCause() != null) {
                visit(node.getInternalCause());
            }
        }
        return null;
    }

    @Override
    public Object visitTry(Try node) {
        visitSeq(node.getInternalBody());
        visitSeq(node.getInternalOrelse());
        visitSeq(node.getInternalHandlers());
        visitSeq(node.getInternalFinalbody());
        return null;
    }

    @Override
    public Object visitAssert(Assert node) {
        visit(node.getInternalTest());
        if (node.getInternalMsg() != null) {
            visit(node.getInternalMsg());
        }
        return null;
    }

    @Override
    public Object visitImport(Import node) {
        visitSeq(node.getInternalNames());
        return null;
    }

    @Override
    public Object visitImportFrom(ImportFrom node) {
        visitSeq(node.getInternalNames());
        return null;
    }

    @Override
    public Object visitGlobal(Global node) {
        for (String name : node.getInternalNames()) {
            EnumSet<Defs> cur = lookup(name);
            EnumSet<Defs> toCheck = EnumSet.of(Defs.LOCAL, Defs.USE, Defs.ANNOT);
            if (cur.stream().anyMatch(toCheck::contains)) {
                String msg;
                if (cur.contains(Defs.USE)) {
                    msg = GLOBAL_AFTER_USE;
                } else if (cur.contains(Defs.ANNOT)) {
                    msg = GLOBAL_ANNO;
                } else {
                    msg = GLOBAL_AFTER_ASSIGN;
                }
                throw new PySyntaxError(String.format(msg, name), node.getLineno(), node.getCol_offset(), "", filename);
            }
            addDef(name, Defs.GLOBAL);
            recordDirective(name, node);
        }
        return null;
    }
    //endregion

    private static final String GLOBAL_ANNO = "annotated name %s cannot be global";
    private static final String NONLOCAL_ANNO = "annotated name %s cannot be nonlocal";
    private static final String GLOBAL_AFTER_ASSIGN = "name %s is assigned to before global declaration";
    private static final String NONLOCAL_AFTER_ASSIGN = "name %s is assigned to before nonlocal declaration";
    private static final String GLOBAL_AFTER_USE = "name %s is used prior to global declaration";
    private static final String NONLOCAL_AFTER_USE = "name %s is used prior to nonlocal declaration";
}
