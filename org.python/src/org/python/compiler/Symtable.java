package org.python.compiler;

import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.AnnAssign;
import org.python.antlr.ast.AnonymousFunction;
import org.python.antlr.ast.Assert;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.AsyncFor;
import org.python.antlr.ast.AsyncFunctionDef;
import org.python.antlr.ast.AsyncWith;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.AugAssign;
import org.python.antlr.ast.Await;
import org.python.antlr.ast.BinOp;
import org.python.antlr.ast.BoolOp;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.Compare;
import org.python.antlr.ast.Delete;
import org.python.antlr.ast.Dict;
import org.python.antlr.ast.ExceptHandler;
import org.python.antlr.ast.Expr;
import org.python.antlr.ast.For;
import org.python.antlr.ast.FormattedValue;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Global;
import org.python.antlr.ast.If;
import org.python.antlr.ast.IfExp;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.JoinedStr;
import org.python.antlr.ast.Module;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Nonlocal;
import org.python.antlr.ast.Raise;
import org.python.antlr.ast.Return;
import org.python.antlr.ast.Set;
import org.python.antlr.ast.Starred;
import org.python.antlr.ast.Subscript;
import org.python.antlr.ast.Try;
import org.python.antlr.ast.Tuple;
import org.python.antlr.ast.UnaryOp;
import org.python.antlr.ast.While;
import org.python.antlr.ast.With;
import org.python.antlr.ast.Yield;
import org.python.antlr.ast.YieldFrom;
import org.python.antlr.ast.alias;
import org.python.antlr.ast.arg;
import org.python.antlr.ast.arguments;
import org.python.antlr.ast.expr_contextType;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PySyntaxError;
import org.python.core.PyTuple;
import org.python.core.PyUnicode;

import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Symtable extends Visitor {
    public static final int SCOPE_OFFSET = 11;

    private static final String GLOBAL_ANNO = "annotated name %s cannot be global";
    private static final String NONLOCAL_ANNO = "annotated name %s cannot be nonlocal";
    private static final String GLOBAL_AFTER_ASSIGN = "name '%s' is assigned to before global declaration";
    private static final String NONLOCAL_AFTER_ASSIGN = "name '%s' is assigned to before nonlocal declaration";
    private static final String GLOBAL_AFTER_USE = "name '%s' is used prior to global declaration";
    private static final String NONLOCAL_AFTER_USE = "name '%s' is used prior to nonlocal declaration";
    private static PyObject _top = null;
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
    PySTEntryObject cur;
    private PySTEntryObject top;
    Map<PythonTree, PySTEntryObject> blocks;
    private Deque<PySTEntryObject> stack;
    private Map<String, EnumSet<Flag>> global;
    private int nblocks;
    private PyObject _private;
    //    private PyFutureFeatures future;
    private int recursionDeps;
    private int recursionLimit;

    private Symtable() {
        stack = new LinkedList<>();
        blocks = new HashMap<>();
    }

    // PySymtable_BuildObject
    public static Symtable buildObject(PythonTree mod, String filename, int future) {
        Symtable st = new Symtable();
        st.filename = filename;

        /* Make the initial symbol information gathering pass */
        if (_top == null) {
            st.enterBlock("top" /** FIXME */, PySTEntryObject.BlockType.ModuleBlock, mod, 0, 0, null, null);
        }
        st.top = st.cur;
        mod.accept(st);
        st.exitBlock(mod);
        /* Make the second symbol analysis pass */
        st.analyze();
        return st;
    }

    public String getFilename() {
        return filename;
    }

    private void analyze() {
        // null means this is a top block
        top.analyzeBlock(null, new HashSet<>(), new HashSet<>());
    }

    public PySTEntryObject getTop() {
        return top;
    }

    private void enterBlock(String name, PySTEntryObject.BlockType block, PythonTree ast, int lineno, int colOffset, arguments args, expr return_) {
        PySTEntryObject prev = null;
        PySTEntryObject ste = new PySTEntryObject(this, name, block, ast, lineno, colOffset, args, return_);
        stack.push(ste);
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
            stack.pop();
            cur = stack.peek();
        }
    }

    private void addDef(String name, Flag flag, Flag... otherFlags) {
        EnumSet<Flag> flags = EnumSet.of(flag, otherFlags);
        EnumSet<Flag> val = cur.symbols.get(name);
        if (val != null) {
            if (flags.contains(Flag.DEF_PARAM) && val.contains(Flag.DEF_PARAM)) {
                throw new PySyntaxError(String.format("duplicated definition of argument: %s", name), cur.lineno, cur.colOffset, "", filename);
            }
            val.addAll(flags);
        } else {
            val = flags;
        }
        cur.symbols.put(name, val);
        if (flags.contains(Flag.DEF_PARAM)) {
            cur.varnames.add(name);
        } else if (flags.contains(Flag.DEF_GLOBAL)) {
            val = flags;
            if (global.containsKey(name)) {
                val.addAll(global.get(name));
            }
            global.put(name, val);
        }
    }

    //region AST Visitor Implementation

    private void visitArgannotations(List<arg> args) {
        for (arg a : args) {
            if (a.getInternalAnnotation() != null) {
                visit(a.getInternalAnnotation());
            }
        }
    }

    private void visitAnnotations(arguments a, expr returns) {
        if (a != null) {
            visitArgannotations(a.getInternalArgs());
            if (a.getInternalVararg() != null && a.getInternalVararg().getInternalAnnotation() != null) {
                visit(a.getInternalVararg().getInternalAnnotation());
            }
            if (a.getInternalKwarg() != null && a.getInternalKwarg().getInternalAnnotation() != null) {
                visit(a.getInternalKwarg().getInternalAnnotation());
            }
            visitArgannotations(a.getInternalKwonlyargs());
        }
        if (returns != null) {
            visit(returns);
        }
    }

    private void visitSeq(List<? extends PythonTree> seq) {
        if (seq == null) {
            return;
        }
        seq.stream().forEach(this::visit);
    }

    public PySTEntryObject Symtable_Lookup(PythonTree node) {
        return blocks.get(node);
    }

    private EnumSet<Flag> lookup(String name) {
        return cur.symbols.getOrDefault(name, Flag.NULL);
    }

    private void recordDirective(String name, stmt s) {
        if (cur.directives == null) {
            cur.directives = new ArrayList<>();
        }
        cur.directives.add(new PyTuple(new PyUnicode(name), new PyLong(s.getLineno()), new PyLong(s.getCol_offset())));
    }

    @Override
    public Object visitModule(Module node) {
        visitSeq(node.getInternalBody());
        return null;
    }

    @Override
    public Object visitFunctionDef(FunctionDef node) {
        addDef(node.getInternalName(), Flag.DEF_LOCAL);
        arguments args = node.getInternalArgs();
        if (args != null) {
            visitSeq(args.getInternalDefaults());
            visitSeq(args.getInternalKw_defaults());
        }
        visitAnnotations(args, node.getInternalReturns());
        visitSeq(node.getInternalDecorator_list());
        enterBlock(node.getInternalName(), PySTEntryObject.BlockType.FunctionBlock, node, node.getLineno(), node.getCol_offset(), node.getInternalArgs(), node.getInternalReturns());
        visitArguments(args);
        visitSeq(node.getInternalBody());
        exitBlock(node);
        return null;
    }

    @Override
    public Object visitAsyncFunctionDef(AsyncFunctionDef node) {
        addDef(node.getInternalName(), Flag.DEF_LOCAL);
        if (node.getInternalArgs() != null) {
            visitSeq(node.getInternalArgs().getInternalDefaults());
            visitSeq(node.getInternalArgs().getInternalKw_defaults());
        }
        visitAnnotations(node.getInternalArgs(), node.getInternalReturns());
        visitSeq(node.getInternalDecorator_list());
        enterBlock(node.getInternalName(), PySTEntryObject.BlockType.FunctionBlock, node, node.getLineno(), node.getCol_offset(), node.getInternalArgs(), node.getInternalReturns());
        cur.coroutine = true;
        visitArguments(node.getInternalArgs());
        visitSeq(node.getInternalBody());
        exitBlock(node);
        return null;
    }

    @Override
    public Object visitClassDef(ClassDef node) {
        addDef(node.getInternalName(), Flag.DEF_LOCAL);
        visitSeq(node.getInternalBases());
        visitSeq(node.getInternalKeywords());
        visitSeq(node.getInternalDecorator_list());
        enterBlock(node.getInternalName(), PySTEntryObject.BlockType.ClassBlock, node, node.getLineno(), node.getCol_offset(), null, null);
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
    public Object visitAugAssign(AugAssign node) {
        visit(node.getInternalTarget());
        visit(node.getInternalValue());
        return null;
    }

    @Override
    public Object visitAnnAssign(AnnAssign node) {
        if (node.getInternalTarget() instanceof Name) {
            Name eName = (Name) node.getInternalTarget();
            EnumSet<Flag> flags = lookup(eName.getInternalId());
            assert flags != null: "flags shouldn't be null";
            if ((flags.contains(Flag.DEF_GLOBAL) || flags.contains(Flag.DEF_NONLOCAL)) && node.getInternalSimple() > 0) {
                throw new PySyntaxError(
                        String.format(flags.contains(Flag.DEF_GLOBAL) ? GLOBAL_ANNO : NONLOCAL_ANNO,
                                eName.getInternalId()),
                        node.getLine(), node.getCol_offset(), "", filename);
            }

            if (node.getInternalSimple() > 0) {
                addDef(eName.getInternalId(), Flag.DEF_ANNO, Flag.DEF_LOCAL);
            } else if (node.getInternalValue() != null) {
                addDef(eName.getInternalId(), Flag.DEF_LOCAL);
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
    public Object visitFor(For node) {
        visit(node.getInternalTarget());
        visit(node.getInternalIter());
        visitSeq(node.getInternalBody());
        visitSeq(node.getInternalOrelse());
        return null;
    }

    @Override
    public Object visitAsyncFor(AsyncFor node) {
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
    public Object visitWith(With node) {
        visitSeq(node.getInternalItems());
        visitSeq(node.getInternalBody());
        return null;
    }

    @Override
    public Object visitAsyncWith(AsyncWith node) {
        visitSeq(node.getInternalItems());
        visitSeq(node.getInternalBody());
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
        node.getInternalNames().stream().forEach(this::visitAlias);
        return null;
    }

    @Override
    public Object visitImportFrom(ImportFrom node) {
        node.getInternalNames().stream().forEach(this::visitAlias);
        return null;
    }

    @Override
    public Object visitGlobal(Global node) {
        EnumSet<Flag> toCheck = EnumSet.of(Flag.DEF_LOCAL, Flag.DEF_USE, Flag.DEF_ANNO);
        for (String name : node.getInternalNames()) {
            EnumSet<Flag> cur = lookup(name);
            if (cur != null) {
                if (toCheck.stream().anyMatch(cur::contains)) {
                    String msg;
                    if (cur.contains(Flag.DEF_USE)) {
                        msg = GLOBAL_AFTER_USE;
                    } else if (cur.contains(Flag.DEF_ANNO)) {
                        msg = GLOBAL_ANNO;
                    } else {
                        msg = GLOBAL_AFTER_ASSIGN;
                    }
                    throw new PySyntaxError(String.format(msg, name), node.getLineno(), node.getCol_offset(), "", filename);
                }
            }
            addDef(name, Flag.DEF_GLOBAL);
            recordDirective(name, node);
        }
        return null;
    }

    @Override
    public Object visitNonlocal(Nonlocal node) {
        EnumSet<Flag> toCheck = EnumSet.of(Flag.DEF_LOCAL, Flag.DEF_USE, Flag.DEF_ANNO);
        for (String name : node.getInternalNames()) {
            EnumSet<Flag> cur = lookup(name);
            if (cur == null) {
                continue;
            }
            if (cur.stream().anyMatch(toCheck::contains)) {
                String msg;
                if (cur.contains(Flag.DEF_USE)) {
                    msg = NONLOCAL_AFTER_USE;
                } else if (cur.contains(Flag.DEF_ANNO)) {
                    msg = NONLOCAL_ANNO;
                } else {
                    msg = NONLOCAL_AFTER_ASSIGN;
                }
                throw new PySyntaxError(String.format(msg, name), node.getLineno(), node.getCol_offset(), "", filename);
            }
            addDef(name, Flag.DEF_NONLOCAL);
            recordDirective(name, node);
        }
        return null;
    }

    @Override
    public Object visitExpr(Expr node) {
        visit(node.getInternalValue());
        return null;
    }

    /**
     * symtable_visit_expr
     */
    @Override
    public Object visitBoolOp(BoolOp node) {
        visitSeq(node.getInternalValues());
        return null;
    }

    @Override
    public Object visitBinOp(BinOp node) {
        visit(node.getInternalLeft());
        visit(node.getInternalRight());
        return null;
    }

    @Override
    public Object visitUnaryOp(UnaryOp node) {
        visit(node.getInternalOperand());
        return null;
    }

    @Override
    public Object visitAnonymousFunction(AnonymousFunction node) {
        if (node.getInternalArgs() != null) {
            visitSeq(node.getInternalArgs().getInternalDefaults());
            visitSeq(node.getInternalArgs().getInternalKw_defaults());
        }
        enterBlock("<lambda>", PySTEntryObject.BlockType.FunctionBlock, node, node.getLineno(), node.getCol_offset(), node.getInternalArgs(), null);
        visitArguments(node.getInternalArgs());
        visitSeq(node.getInternalBody());
        exitBlock(node);
        return null;
    }

    @Override
    public Object visitIfExp(IfExp node) {
        visit(node.getInternalTest());
        visit(node.getInternalBody());
        visit(node.getInternalOrelse());
        return null;
    }

    @Override
    public Object visitDict(Dict node) {
        visitSeq(node.getInternalKeys());
        visitSeq(node.getInternalValues());
        return null;
    }

    @Override
    public Object visitSet(Set node) {
        visitSeq(node.getInternalElts());
        return null;
    }

    @Override
    public Object visitAwait(Await node) {
        visit(node.getInternalValue());
        cur.coroutine = true;
        return null;
    }

    @Override
    public Object visitYield(Yield node) {
        if (node.getInternalValue() != null) {
            visit(node.getInternalValue());
        }
        cur.generator = true;
        return null;
    }

    @Override
    public Object visitYieldFrom(YieldFrom node) {
        visit(node.getInternalValue());
        cur.generator = true;
        return null;
    }

    @Override
    public Object visitCompare(Compare node) {
        visit(node.getInternalLeft());
        visitSeq(node.getInternalComparators());
        return null;
    }

    @Override
    public Object visitCall(Call node) {
        visit(node.getInternalFunc());
        visitSeq(node.getInternalArgs());
        visitSeq(node.getInternalKeywords());
        return null;
    }

    @Override
    public Object visitFormattedValue(FormattedValue node) {
        visit(node.getInternalValue());
        if (node.getInternalFormat_spec() != null) {
            visit(node.getInternalFormat_spec());
        }
        return null;
    }

    @Override
    public Object visitJoinedStr(JoinedStr node) {
        visitSeq(node.getInternalValues());
        return null;
    }

    @Override
    public Object visitAttribute(Attribute node) {
        visit(node.getInternalValue());
        return null;
    }

    @Override
    public Object visitSubscript(Subscript node) {
        visit(node.getInternalValue());
        visit(node.getInternalSlice());
        return null;
    }

    @Override
    public Object visitStarred(Starred node) {
        visit(node.getInternalValue());
        return null;
    }

    @Override
    public Object visitName(Name node) {
        boolean isLoad = node.getInternalCtx() == expr_contextType.Load;
        addDef(node.getInternalId(), isLoad ? Flag.DEF_USE : Flag.DEF_LOCAL);
        if (isLoad && cur.type == PySTEntryObject.BlockType.FunctionBlock && node.getInternalId().equals("super")) {
            addDef("__class__", Flag.DEF_USE);
        }
        return null;
    }

    @Override
    public Object visitList(org.python.antlr.ast.List node) {
        visitSeq(node.getInternalElts());
        return null;
    }

    //endregion

    @Override
    public Object visitTuple(Tuple node) {
        visitSeq(node.getInternalElts());
        return null;
    }

    @Override
    public Object visitExceptHandler(ExceptHandler node) {
        if (node.getInternalType() != null) {
            visit(node.getInternalType());
        }
        if (node.getInternalName() != null) {
            addDef(node.getInternalName(), Flag.DEF_LOCAL);
        }
        visitSeq(node.getInternalBody());
        return null;
    }

    /**
     * end symtable_visit_expr
     */
    public void visitAlias(alias a) {
        String name = a.getInternalAsname();
        if (name == null) {
            name = a.getInternalName();
        }
        int dot = name.indexOf('.');
        String storeName;
        if (dot >= 0) {
            storeName = name.substring(0, dot);
        } else {
            storeName = name;
        }
        if (!name.equals("*")) {
            addDef(storeName, Flag.DEF_IMPORT);
        } else {
            if (cur.type != PySTEntryObject.BlockType.ModuleBlock) {
                throw new PySyntaxError("import * only allowed at module level", cur.lineno, cur.colOffset, "", filename);
            }
        }

    }

    public void visitArguments(arguments a) {
        if (a == null) {
            return;
        }
        visitParams(a.getInternalArgs());
        if (a.getInternalVararg() != null) {
            addDef(a.getInternalVararg().getInternalArg(), Flag.DEF_PARAM);
            cur.varargs = true;
        }
        visitParams(a.getInternalKwonlyargs());
        if (a.getInternalKwarg() != null) {
            addDef(a.getInternalKwarg().getInternalArg(), Flag.DEF_PARAM);
            cur.varkeywords = true;
        }
    }

    public void visitParams(List<arg> args) {
        if (args == null) {
            return;
        }
        for (arg a : args) {
            addDef(a.getInternalArg(), Flag.DEF_PARAM);
        }
    }

    public enum Flag {
        SENTINAL(0), DEF_GLOBAL(1), DEF_LOCAL(2), DEF_PARAM(2 << 1), DEF_NONLOCAL(2 << 2),
        DEF_USE(2 << 3), DEF_FREE(2 << 4), DEF_FREE_CLASS(2 << 5), DEF_IMPORT(2 << 6),
        DEF_ANNO(2 << 7), LOCAL(1 << SCOPE_OFFSET), GLOBAL_EXPLICIT(2 << SCOPE_OFFSET),
        GLOBAL_IMPLICIT(3 << SCOPE_OFFSET),
        FREE(4 << SCOPE_OFFSET), CELL(5 << SCOPE_OFFSET);

        static EnumSet<Flag> DEF_BOUND = EnumSet.of(DEF_LOCAL, DEF_PARAM, DEF_IMPORT);
        static EnumSet<Flag> NULL = EnumSet.of(SENTINAL);

        int value;

        Flag(int val) {
            value = val;
        }
    }
}
