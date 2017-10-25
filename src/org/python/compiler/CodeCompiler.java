// Copyright (c) Corporation for National Research Initiatives
package org.python.compiler;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.AnnAssign;
import org.python.antlr.ast.AnonymousFunction;
import org.python.antlr.ast.Assert;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.AsyncFunctionDef;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.Await;
import org.python.antlr.ast.BinOp;
import org.python.antlr.ast.Block;
import org.python.antlr.ast.BoolOp;
import org.python.antlr.ast.Break;
import org.python.antlr.ast.Bytes;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.Compare;
import org.python.antlr.ast.Continue;
import org.python.antlr.ast.Delete;
import org.python.antlr.ast.Dict;
import org.python.antlr.ast.Ellipsis;
import org.python.antlr.ast.ExceptHandler;
import org.python.antlr.ast.ExitFor;
import org.python.antlr.ast.Expr;
import org.python.antlr.ast.Expression;
import org.python.antlr.ast.ExtSlice;
import org.python.antlr.ast.FormattedValue;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Global;
import org.python.antlr.ast.If;
import org.python.antlr.ast.IfExp;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Index;
import org.python.antlr.ast.Interactive;
import org.python.antlr.ast.JoinedStr;
import org.python.antlr.ast.List;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.NameConstant;
import org.python.antlr.ast.Nonlocal;
import org.python.antlr.ast.Num;
import org.python.antlr.ast.Pass;
import org.python.antlr.ast.Raise;
import org.python.antlr.ast.Return;
import org.python.antlr.ast.Set;
import org.python.antlr.ast.Slice;
import org.python.antlr.ast.Starred;
import org.python.antlr.ast.Str;
import org.python.antlr.ast.Subscript;
import org.python.antlr.ast.Suite;
import org.python.antlr.ast.Try;
import org.python.antlr.ast.Tuple;
import org.python.antlr.ast.UnaryOp;
import org.python.antlr.ast.VisitorIF;
import org.python.antlr.ast.While;
import org.python.antlr.ast.Yield;
import org.python.antlr.ast.YieldFrom;
import org.python.antlr.ast.alias;
import org.python.antlr.ast.arguments;
import org.python.antlr.ast.cmpopType;
import org.python.antlr.ast.expr_contextType;
import org.python.antlr.ast.keyword;
import org.python.antlr.base.expr;
import org.python.antlr.base.mod;
import org.python.antlr.base.stmt;
import org.python.core.BaseCode;
import org.python.core.CompareOp;
import org.python.core.CompilerFlags;
import org.python.core.Py;
import org.python.core.PyBoolean;
import org.python.core.PyCode;
import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PyFrame;
import org.python.core.PyFunction;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PySet;
import org.python.core.PySlice;
import org.python.core.PyTraceback;
import org.python.core.PyTuple;
import org.python.core.PyUnicode;
import org.python.core.ThreadState;
import org.python.core.linker.Bootstrap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

import static org.python.compiler.CompilerConstants.*;
import static org.python.util.CodegenUtils.*;

public class CodeCompiler extends Visitor implements Opcodes, ClassConstants {
    private static final Handle LINKERBOOTSTRAP = new Handle(H_INVOKESTATIC, Bootstrap.BOOTSTRAP.getClassName(),
            Bootstrap.BOOTSTRAP.getName(), Bootstrap.BOOTSTRAP.getDescriptor(), false);

    private static final char ESCAPE_C = '\\';
    private static final char NULL_ESCAPE_C = '=';
    /**
     * Operation that without a name, such as GET_ELEMENT
     */
    private static final String EMPTY_NAME = String.valueOf(new char[]{ESCAPE_C, NULL_ESCAPE_C});

    private static final Object Exit = Integer.valueOf(1);
    private static final Object NoExit = null;
    private Module module;
    private Code code;
    private CompilerFlags cflags;

    private boolean optimizeGlobals = true;
    private String className;
    private Deque<Label> continueLabels, breakLabels, exitLabels;
    private Deque<ExceptionHandler> exceptionHandlers;

    private Symtable st;
    private PySTEntryObject ste;
    private CompileUnit u;

    /*
     * break/continue finally's level. This is the lowest level in the exceptionHandlers which
     * should be executed at break or continue. It is saved/updated/restored when compiling loops. A
     * similar level for returns is not needed because a new CodeCompiler is used for each PyCode,
     * in other words: each 'function'. When returning through finally's all the exceptionHandlers
     * are executed.
     */
    private int bcfLevel = 0;
    private Deque<CompileUnit> stack;
    private int nestlevel;

    public CodeCompiler(Module module) {
        this.module = module;
        st = module.st;

        continueLabels = new LinkedList<>();
        breakLabels = new LinkedList<>();
        exitLabels = new LinkedList<>();
        exceptionHandlers = new LinkedList<>();
        stack = new LinkedList<>();
    }

    static boolean checkOptimizeGlobals(PySTEntryObject scope) {
        return false;
//        return fast_locals && !scope.exec && !scope.from_import_star;
    }

    private static String mangle(String _private, String name) {
        if (!name.startsWith("__") || name.endsWith("__") || name.indexOf('.') != -1) {
            return name;
        }
        return _private + "_" + name;
    }

    /**
     * Counterpart of makeStrings, instead of put the string array in local variable slot, leave it in stack
     *
     * @param c
     * @param entries
     * @return
     * @throws IOException
     */
    static void loadStrings(Code c, Map<String, Integer> entries) {
        if (entries != null) {
            c.iconst(entries.size());
        } else {
            c.iconst(0);
        }
        c.anewarray(p(String.class));
        if (entries != null) {
            int i = 0;
            for (Map.Entry<String, Integer> entry : entries.entrySet()) {
                c.dup();
                c.iconst(entry.getValue());
                c.ldc(entry.getKey());
                c.aastore();
                i++;
            }
        }
    }
    static void loadStrings(Code c, Collection<String> names) {
        if (names != null) {
            c.iconst(names.size());
        } else {
            c.iconst(0);
        }
        c.anewarray(p(String.class));
        if (names != null) {
            int i = 0;
            for (String name : names) {
                c.dup();
                c.iconst(i);
                c.ldc(name);
                c.aastore();
                i++;
            }
        }
    }

    public void popException() {
        loadThreadState();
        code.invokevirtual(p(ThreadState.class), "popexc", sig(PyException.class));
        code.pop();
    }

    public void doRaise() {
        loadThreadState();
        code.invokestatic(p(PyException.class), "doRaise", sig(PyException.class, ThreadState.class));
    }

    public void getNone() {
        code.getstatic(p(Py.class), "None", ci(PyObject.class));
    }

    public void getExcInfo() {
        loadThreadState();
        code.invokevirtual(p(ThreadState.class), "getexc", sig(PyException.class));
        code.dup();
        code.getfield(p(PyException.class), "type", ci(PyObject.class));
        code.swap();
        code.dup();
        code.getfield(p(PyException.class), "value", ci(PyObject.class));
        code.swap();
        code.getfield(p(PyException.class), "traceback", ci(PyTraceback.class));
    }

    public void loadFrame() {
        code.aload(2);
    }

    public void loadThreadState() {
        code.aload(1);
    }

    public void setLastI(int idx) {
        loadFrame();
        code.iconst(idx);
        code.putfield(p(PyFrame.class), "f_lasti", "I");
    }

    public void getLastI() {
        code.getfield(p(PyFrame.class), "f_lasti", "I");
    }

    private void loadf_back() {
        code.getfield(p(PyFrame.class), "f_back", ci(PyFrame.class));
    }

    /* Set the i_lineno member of the instruction at offset off if the
       jine number for the current expression/statement has not
       already been set.  If it has been set, the call has no effect.

       The line number is reset in the following cases:
       - when entering a new scope
       - on each statement
       - on each expression that start a new line
       - before the "except" clause
       - before the "for" and "while" expressions
    */
    public void setline(int line) {
        u.co_lineno = line;
        if (module.linenumbers) {
            code.setline(line);
            loadFrame();
            code.iconst(line);
            code.invokevirtual(p(PyFrame.class), "setline", sig(Void.TYPE, Integer.TYPE));
        }
    }

    @Override
    public Object visit(PythonTree node) {
        int lineno = node.getLineno();
        if (node instanceof stmt) {
            setline(lineno);
        } else if (node instanceof expr) {
            if (lineno > u.co_lineno) {
                u.co_lineno = lineno;
                setline(lineno);
            }
        }
        return super.visit(node);
    }


    void exitScope() {
        nestlevel--;
        CompileUnit cur = u;
        if (!stack.isEmpty()) {
            u = stack.pop();
            ste = u.ste;
            code = u.methodEmitter;
        }
        cur.get(code); // place the getstatic instruction in the parent scope
    }

    CompileUnit enterScope(String name, CompilerScope scopeType, PythonTree key, int lineno) {
        PySTEntryObject ste = st.Symtable_Lookup(key);
        if (ste == null && scopeType == CompilerScope.MODULE) {
            ste = st.getTop();
        }
        assert ste != null : "ste shouldn't be null";
        this.ste = ste;
        CompileUnit compileUnit = new CompileUnit(scopeType, name, ste, lineno, module);
        if (this.u != null) {
            stack.push(this.u);
            compileUnit._private = u._private;
        }
        this.u = compileUnit;

        module.codes.add(u);
        this.nestlevel++;
        if (u.scopeType != CompilerScope.MODULE) {
            this.setQualname();
        }
        Code c = module.classfile.addMethod(u.fname, //
                sig(PyObject.class, ThreadState.class, PyFrame.class), ACC_PUBLIC);
        u.methodEmitter = c;
        this.code = c;
        if (ste.generator) {
            u.genswitch = new Label();
            code.goto_(u.genswitch);
            u.start = new Label();
            code.mark(u.start);
        }

        setline(lineno);
        return compileUnit;
    }

    private void setQualname() {
        String dot = ".", dotLocals = ".<locals>", base = null, name;
        boolean forceGlobal = false;
        int stackSize = this.stack.size();
        assert stackSize >= 1;
        if (stackSize > 1) {
            CompileUnit parent = stack.peek();
            assert parent != null;
            if (u.scopeType == CompilerScope.FUNCTION
                    || u.scopeType == CompilerScope.ASYNC_FUNCTION
                    || u.scopeType == CompilerScope.CLASS) {
                assert u.name != null;
//                String mangled = mangle(parent._private, u.name);
                EnumSet<Symtable.Flag> scope = parent.ste.symbols.getOrDefault(u.name, Symtable.Flag.NULL);

                assert !scope.contains(Symtable.Flag.GLOBAL_IMPLICIT);
                if (scope.contains(Symtable.Flag.GLOBAL_EXPLICIT)) {
                    forceGlobal = true;
                }
            }

            if (!forceGlobal) {
                if (parent.scopeType == CompilerScope.FUNCTION
                        || parent.scopeType == CompilerScope.ASYNC_FUNCTION
                        || parent.scopeType == CompilerScope.LAMBDA) {
                    base = parent.qualname + dotLocals;
                } else {
                    base = parent.qualname;
                }
            }
        }

        if (base != null) {
            name = base + dot + u.name;
        } else {
            name = u.name;
        }
        u.qualname = name;
    }

    void parse(mod node, String className, CompilerFlags cflags, boolean needsClassClosure) {
        this.className = className;
        this.cflags = cflags;
//        this.tbl = scope.tbl;

        // BEGIN preparse

//        int nparamcell = ste.jy_paramcells.size();
//        if (nparamcell > 0) {
//            java.util.List<String> paramcells = ste.jy_paramcells;
//            for (int i = 0; i < nparamcell; i++) {
//                loadFrame();
//                SymInfo syminf = tbl.get(paramcells.get(i));
//                code.iconst(syminf.locals_index);
//                code.iconst(syminf.env_index);
//                code.invokevirtual(p(PyFrame.class), "to_cell",
//                        sig(Void.TYPE, Integer.TYPE, Integer.TYPE));
//            }
//        }
        // END preparse

        optimizeGlobals = checkOptimizeGlobals(ste);

        visit(node);

        setLastI(-1);
        getNone();
        code.areturn();

        // BEGIN postparse
        // END postparse
    }

    @Override
    public Object visitModule(org.python.antlr.ast.Module suite) {
        java.util.List<stmt> body = suite.getInternalBody();
        int docstring = getDocStr(body);
        if (docstring == 0) {
            loadFrame();
            visit(((Expr) body.get(0)).getInternalValue());
            nameop("__doc__", expr_contextType.Store);
        }
        int size = body.size();
        for (int i = docstring + 1; i < size; i++) {
            visit(body.get(i));
        }
        return null;
    }

    @Override
    public Object visitInteractive(Interactive node) {
        traverse(node);
        return null;
    }

    @Override
    public Object visitExpression(Expression node) {
        return visitReturn(new Return(node, node.getInternalBody()), true);
    }

    @Override
    public Object visitSuite(Suite node) {
        return suite(node.getInternalBody());
    }

    @Override
    public Object visitFunctionDef(FunctionDef node) {
        String name = node.getInternalName();
        java.util.List<expr> decs = node.getInternalDecorator_list();
        java.util.List<stmt> body = node.getInternalBody();
        return compileFunction(name, decs, node.getInternalArgs(), body, node);
    }

    @Override
    public Object visitAsyncFunctionDef(AsyncFunctionDef node) {
        String name = node.getInternalName();
        java.util.List<expr> decs = node.getInternalDecorator_list();
        java.util.List<stmt> body = node.getInternalBody();
        return compileFunction(name, decs, node.getInternalArgs(), body, node);
    }

    private void compileBody(java.util.List<stmt> stmts) {
        stmts.stream().forEach(this::visit);
    }

    @Override
    public Object visitClassDef(ClassDef node) {
        loadFrame(); // pair with nameop
        loadBuildClass();
        code.new_(p(PyFunction.class));
        code.dup();

        enterScope(node.getInternalName(), CompilerScope.CLASS, node, node.getLineno());
        u._private = node.getInternalName();
        loadFrame();
        code.dup();
        code.dup();
        nameop("__name__", expr_contextType.Load);
        nameop("__module__", expr_contextType.Store);
        assert u.qualname != null: "class qualname is null";
        module.unicodeConstant(u.qualname).get(code); // TODO check how to handle constants in the better way
        nameop("__qualname__", expr_contextType.Store);
        compileBody(node.getInternalBody());

        if (u.ste.needsClassClosure) {
            int i = u.cellvars.get("__class__");
            assert i == 0: "__class__ must be the only cell var in class scope";
            loadFrame();
            code.dup();
            code.iconst(i);
            code.invokevirtual(p(PyFrame.class), "getclosure", sig(PyObject.class, Integer.TYPE));
            code.dup_x1();
            nameop("__classcell__", expr_contextType.Store);
        } else {
            assert u.cellvars.isEmpty(): "class scope should not have cell vars other than __class__";
            getNone();
        }
        code.areturn();
        CompileUnit innerUnit = u;
        exitScope();
        make_closure(innerUnit, 0, null);
        module.unicodeConstant(node.getInternalName()).get(code);
        callHelper(2, node.getInternalBases(), node.getInternalKeywords());
        code.visitInvokeDynamicInsn(EMPTY_NAME, sig(PyObject.class, PyObject.class, ThreadState.class,
                PyObject[].class, String[].class), LINKERBOOTSTRAP, Bootstrap.CALL);
        nameop(node.getInternalName(), expr_contextType.Store);
        return null;
    }

    private void callHelper(int n /** args already pushed */,
                            java.util.List<expr> args, java.util.List<keyword> keywords) {

        java.util.List<String> keys = new ArrayList<>();
        java.util.List<expr> values = new ArrayList<>();

        expr kwarg = null;
        for (int i = 0; i < keywords.size(); i++) {
            keyword kw = keywords.get(i);
            if (kw.getInternalArg() == null) {
                kwarg = kw.getInternalValue();
                break;
            }
            keys.add(kw.getInternalArg());
            values.add(kw.getInternalValue());
        }
        loadArray(code, args, values, n);
        loadThreadState();
        code.swap();
        loadStrings(code, keys);
    }

    private void make_closure(CompileUnit u, int funcflags, String qualname) {
        if (!makeClosure(u)) {
            code.aconst_null();
        }
        code.invokespecial(
                p(PyFunction.class),
                "<init>",
                sig(Void.TYPE, PyCode.class, PyObject[].class));
    }

    private void loadBuildClass() {
        loadFrame();
        code.invokevirtual(p(PyFrame.class), "loadbuildclass", sig(PyObject.class));
    }

    @Override
    public Object visitReturn(Return node) {
        return visitReturn(node, false);
    }

    @Override
    public Object visitDelete(Delete node) {
        traverse(node);
        return null;
    }

    @Override
    public Object visitAssign(Assign node) {
        java.util.List<expr> targets = node.getInternalTargets();
        for (expr e : targets) {
            e.enter(this);
        }
        visit(node.getInternalValue());

        for (int i = targets.size() - 1; i >= 0; i--) {
            targets.get(i).leave(this);
        }
        code.pop(); // pop the value from stack
        return null;
    }

    @Override
    public Object visitAnnAssign(AnnAssign node) {
        if (node.getInternalValue() != null) {
            node.getInternalTarget().enter(this);
            visit(node.getInternalValue());
            node.getInternalTarget().leave(this);
        }
        return null;
    }

    @Override
    public Object visitWhile(While node) {
        int savebcf = beginLoop();
        Label continue_loop = continueLabels.peek();
        Label break_loop = breakLabels.peek();
        Label exit_loop = exitLabels.peek();

        Label start_loop = new Label();

        code.goto_(continue_loop);
        code.mark(start_loop);

        // Do suite
        suite(node.getInternalBody());

        code.mark(continue_loop);

        // Do test
        expr test = node.getInternalTest();
        if (test instanceof NameConstant && ((NameConstant) test).getInternalValue().equals("True")) {
            // optimisation for while True loop
            code.goto_(start_loop);
        } else {
            visit(test);
            code.invokevirtual(p(PyObject.class), "__bool__", sig(Boolean.TYPE));
            code.ifne(start_loop);
        }
        code.mark(exit_loop);
        finishLoop(savebcf);

        if (node.getInternalOrelse() != null) {
            // Do else
            suite(node.getInternalOrelse());
        }
        code.mark(break_loop);

        // Probably need to detect "guaranteed exits"
        return null;
    }

    @Override
    public Object visitIf(If node) {
        Label end_of_if = null;
        if (node.getInternalOrelse() != null) {
            end_of_if = new Label();
        }

        Object exit = doTest(end_of_if, node, 0);
        if (end_of_if != null) {
            code.mark(end_of_if);
        }
        return exit;
    }

    @Override
    public Object visitRaise(Raise node) {
        if (node.getInternalExc() != null) {
            visit(node.getInternalExc());
        }
        if (node.getInternalCause() != null) {
            visit(node.getInternalCause());
        }

        if (node.getInternalExc() == null) {
            doRaise();
        } else if (node.getInternalCause() == null) {
            code.invokestatic(p(PyException.class), "doRaise", sig(PyException.class, PyObject.class));
        } else {
            code.invokestatic(p(PyException.class), "doRaise",
                    sig(PyException.class, PyObject.class, PyObject.class));
        }
        code.dup();
        loadFrame();
        code.invokevirtual(p(PyException.class), "tracebackHere", sig(Void.TYPE, PyFrame.class));
        code.athrow();
        return Exit;
    }

    @Override
    public Object visitGlobal(Global node) {
        return null;
    }

    @Override
    public Object visitNonlocal(Nonlocal node) {
        return null;
    }

    @Override
    public Object visitTry(Try node) {
        Label start = new Label();
        Label end = new Label();
        Label handler_start = new Label();
        Label handler_end = new Label();
        ExceptionHandler handler = new ExceptionHandler();

        code.mark(start);
        handler.exceptionStarts.addElement(start);
        exceptionHandlers.push(handler);
        // Do suite
        Object exit = suite(node.getInternalBody());
        exceptionHandlers.pop();
        code.mark(end);
        handler.exceptionEnds.addElement(end);

        if (exit == null) {
            code.goto_(handler_end);
        }

        code.mark(handler_start);

        loadFrame();

        code.invokestatic(p(Py.class), "setException",
                sig(PyException.class, Throwable.class, PyFrame.class));

        int exc = code.getFinallyLocal(p(Throwable.class));
        code.astore(exc);

        if (node.getInternalOrelse() == null) {
            // No else clause to worry about
            exceptionTest(exc, handler_end, node);
            code.mark(handler_end);
        } else {
            // Have else clause
            Label else_end = new Label();
            exceptionTest(exc, else_end, node);
            code.mark(handler_end);

            // do else clause
            suite(node.getInternalOrelse());
            code.mark(else_end);
        }

//        popException();
        code.freeFinallyLocal(exc);
        handler.addExceptionHandlers(handler_start);
        return null;
    }

    @Override
    public Object visitAssert(Assert node) {
        Label end_of_assert = new Label();

        /* First do an if __debug__: */
        loadFrame();
        emitGetGlobal("__debug__");

        code.invokevirtual(p(PyObject.class), "__bool__", sig(Boolean.TYPE));

        code.ifeq(end_of_assert);

        /*
         * Now do the body of the assert. If PyObject.__bool__ is true, then the assertion
         * succeeded, the message portion should not be processed. Otherwise, the message will be
         * processed.
         */
        visit(node.getInternalTest());
        code.invokevirtual(p(PyObject.class), "__bool__", sig(Boolean.TYPE));

        /* If evaluation is false, then branch to end of method */
        code.ifne(end_of_assert);

        /* Visit the message part of the assertion, or pass Py.None */
        if (node.getInternalMsg() != null) {
            visit(node.getInternalMsg());
            code.invokestatic(p(Py.class), "AssertionError",
                    sig(PyException.class, PyObject.class));
        } else {
            code.invokestatic(p(Py.class), "AssertionError",
                    sig(PyException.class));
        }

        /* Raise assertion error. Only executes this logic if assertion failed */
        code.athrow();

        /* And finally set the label for the end of it all */
        code.mark(end_of_assert);

        return null;
    }

    @Override
    public Object visitImport(Import node) {
        for (alias a : node.getInternalNames()) {
            String asname, name = a.getInternalName();
            int dot = name.indexOf('.');
            boolean aliased = false;
            if (a.getInternalAsname() != null) {
                asname = a.getInternalAsname();
                aliased = true;
            } else {
                if (dot > 0) {
                    asname = name.substring(0, dot);
                } else {
                    asname = name;
                }
            }
            loadFrame();
            code.dup();
            code.ldc(name);
            code.aconst_null();
            code.iconst(0);
            code.invokestatic(p(org.python.bootstrap.Import.class), "importName",
                    sig(PyObject.class, PyFrame.class, String.class, String[].class, Integer.TYPE));
            if (aliased) {
                while (dot > 0) {
                    int nextDot = name.indexOf('.', ++dot);
                    String attrName;
                    if (nextDot > 0) {
                        attrName = name.substring(dot, nextDot);
                    } else {
                        attrName = name.substring(dot);
                    }
                    code.ldc(attrName);
                    code.invokevirtual(p(PyObject.class), "__getattr__", sig(PyObject.class, String.class));
                    dot = nextDot;
                }
            }
            nameop(asname, expr_contextType.Store);
        }
        return null;
    }

    @Override
    public Object visitImportFrom(ImportFrom node) {
        java.util.List<alias> aliases = node.getInternalNames();
        loadFrame();
        code.ldc(node.getInternalModule());
        loadStrings(code, aliases.stream().map(a -> a.getInternalName()).collect(Collectors.toList()));
        code.iconst(node.getInternalLevel());
        code.invokestatic(p(org.python.bootstrap.Import.class), "importName",
                sig(PyObject.class, PyFrame.class, String.class, String[].class, Integer.TYPE));
        if (aliases == null || aliases.size() == 0) {
            throw Py.SyntaxError(node.getToken(), "Internel parser error", module.getFilename());
        } else if (aliases.size() == 1 && aliases.get(0).getInternalName().equals("*")) {
            if (ste.nested) {
                module.error("import * only allowed at module level", false, node);
            }

            loadFrame();
            code.swap();
            code.invokestatic(p(org.python.bootstrap.Import.class), "importAllFrom",
                    sig(Void.TYPE, PyFrame.class, PyObject.class));

        } else {
            for (alias asName : aliases) {
                String from = asName.getInternalName();
                String as = asName.getInternalAsname();
                if (as == null) {
                    as = from;
                }
                code.dup();
                code.ldc(from);
                code.invokestatic(p(org.python.bootstrap.Import.class), "importFrom",
                        sig(PyObject.class, PyObject.class, String.class));
                loadFrame();
                code.swap();
                nameop(as, expr_contextType.Store);
            }
            code.pop();
        }
        return null;
    }

    @Override
    public Object visitExpr(Expr node) {
        visit(node.getInternalValue());

        if (node.isPrint()) {
            code.invokestatic(p(Py.class), "printResult", sig(Void.TYPE, PyObject.class));
        } else {
            code.pop();
        }
        return null;
    }

    @Override
    public Object visitPass(Pass node) {
        return null;
    }

    @Override
    public Object visitBreak(Break node) {
        if (breakLabels.isEmpty()) {
            throw Py.SyntaxError(node.getToken(), "'break' outside loop", module.getFilename());
        }

        code.goto_(breakLabels.peek());
        return null;
    }

    @Override
    public Object visitContinue(Continue node) {
        if (continueLabels.isEmpty()) {
            throw Py.SyntaxError(node.getToken(), "'continue' not properly in loop", module.getFilename());
        }

        code.goto_(continueLabels.peek());
        return Exit;
    }

    @Override
    public Object visitExitFor(ExitFor node) {
        popException();

        code.goto_(exitLabels.peek());
        return null;
    }

    @Override
    public Object visitBlock(Block node) {
        for (stmt s : node.getInternalBody()) {
            visit(s);
        }
        return null;
    }

    @Override
    public Object visitBoolOp(BoolOp node) {
        Label end = new Label();
        visit(node.getInternalValues().get(0));
        for (int i = 1; i < node.getInternalValues().size(); i++) {
            code.dup();
            code.invokevirtual(p(PyObject.class), "__bool__", sig(Boolean.TYPE));
            switch (node.getInternalOp()) {
                case Or:
                    code.ifne(end);
                    break;
                case And:
                    code.ifeq(end);
                    break;
            }
            code.pop();
            visit(node.getInternalValues().get(i));
        }
        code.mark(end);
        return null;
    }

    @Override
    public Object visitBinOp(BinOp node) {
        visit(node.getInternalLeft());
        visit(node.getInternalRight());
        String name = null;
        switch (node.getInternalOp()) {
            case Add:
                name = "add";
                break;
            case Sub:
                name = "sub";
                break;
            case Mult:
                name = "mul";
                break;
            case MatMult:
                name = "matmul";
                break;
            case Div:
                name = "truediv";
                break;
            case Mod:
                name = "mod";
                break;
            case Pow:
                name = "pow";
                break;
            case LShift:
                name = "lshift";
                break;
            case RShift:
                name = "rshift";
                break;
            case BitOr:
                name = "or";
                break;
            case BitXor:
                name = "xor";
                break;
            case BitAnd:
                name = "and";
                break;
            case FloorDiv:
                name = "floordiv";
                break;
        }

//        if (node.getInternalOp() == operatorType.Div && module.getFutures().areDivisionOn()) {
//            name = "_truediv";
//        }
        name = node.isInplace() ? "_i" + name : "_" + name;
        code.invokevirtual(p(PyObject.class), name, sig(PyObject.class, PyObject.class));
        return null;
    }

    @Override
    public Object visitUnaryOp(UnaryOp node) {
        visit(node.getInternalOperand());
        String name = null;
        switch (node.getInternalOp()) {
            case Invert:
                name = "__invert__";
                break;
            case Not:
                name = "__not__";
                break;
            case UAdd:
                name = "__pos__";
                break;
            case USub:
                name = "__neg__";
                break;
        }
        code.invokevirtual(p(PyObject.class), name, sig(PyObject.class));
        return null;
    }

    @Override
    public Object visitAnonymousFunction(AnonymousFunction node) {
        return compileFunction(node.getInternalName(), Arrays.asList(), node.getInternalArgs(), node.getInternalBody(), node);
    }

    @Override
    public Object visitIfExp(IfExp node) {
        Label end = new Label();
        Label end_of_else = new Label();

        visit(node.getInternalTest());
        code.invokevirtual(p(PyObject.class), "__bool__", sig(Boolean.TYPE));

        code.ifeq(end_of_else);
        visit(node.getInternalBody());
        code.goto_(end);

        code.mark(end_of_else);
        visit(node.getInternalOrelse());

        code.mark(end);

        return null;
    }

    @Override
    public Object visitDict(Dict node) {
        java.util.List<expr> elts = new ArrayList<>();
        java.util.List<expr> keys = node.getInternalKeys();
        java.util.List<expr> vals = node.getInternalValues();
        for (int i = 0; i < keys.size(); i++) {
            elts.add(keys.get(i));
            elts.add(vals.get(i));
        }

        if (ste.generator) {
            code.new_(p(PyDictionary.class));
            code.dup();
            loadArray(code, elts);
            code.invokespecial(p(PyDictionary.class), "<init>", sig(Void.TYPE, PyObject[].class));
        } else {
            code.new_(p(PyDictionary.class));
            code.dup();
            loadArray(code, elts);
            code.invokespecial(p(PyDictionary.class), "<init>", sig(Void.TYPE, PyObject[].class));
        }
        if (vals.size() > keys.size()) {
            for (int i = keys.size(); i < vals.size(); i++) {
                code.dup();
                visit(vals.get(i));
                code.invokevirtual(p(PyDictionary.class), "merge", sig(Void.TYPE, PyObject.class));
            }
        }
        return null;
    }

    @Override
    public Object visitSet(Set node) {
        java.util.List<expr> elts = node.getInternalElts();
        java.util.List<expr> stars = new ArrayList<>();
        java.util.List<expr> scalars = new ArrayList<>();
        for (expr e : elts) {
            if (e instanceof Starred) {
                stars.add(e);
            } else {
                scalars.add(e);
            }
        }

        loadArray(code, scalars);
        code.invokestatic(p(Py.class), "newSet", sig(PySet.class, PyObject[].class));
        for (expr e : stars) {
            code.dup();
            visit(e);
            code.invokevirtual(p(PySet.class), "_update", sig(Void.TYPE, PyObject.class));
        }
        return null;
    }

    @Override
    public Object visitAwait(Await node) {
        code.invokestatic(p(Py.class), SAVE_OPRANDS.symbolName(), sig(Void.TYPE));
        visit(node.getInternalValue());

        setLastI(++u.yieldCount);
        code.invokestatic(p(Py.class), "getAwaitableIter", sig(PyObject.class, PyObject.class));
        loadFrame();
        code.swap();
        code.putfield(p(PyFrame.class), "f_yieldfrom", ci(PyObject.class));
        saveLocals();

        code.invokestatic(p(Py.class), MARK.symbolName(), sig(Void.TYPE));
        loadFrame();
        code.invokestatic(p(Py.class), "yieldFrom", sig(PyObject.class, PyFrame.class));
        code.invokestatic(p(Py.class), YIELD.symbolName(), sig(Void.TYPE, PyObject.class));
        u.yieldCount++;
        code.invokestatic(p(Py.class), MARK.symbolName(), sig(Void.TYPE));
        restoreLocals();
        code.invokestatic(p(Py.class), RESTORE_OPRANDS.symbolName(), sig(Void.TYPE));

        // restore return value from subgenerator
        loadFrame();
        code.invokevirtual(p(PyFrame.class), "getf_stacktop", sig(PyObject.class));
        return null;
    }

    @Override
    public Object visitYield(Yield node) {
        if (u.scopeType != CompilerScope.FUNCTION) {
            throw Py.SyntaxError(node.getToken(), "'yield' outside function", module.getFilename());
        }

        /**
         *  this is a placeholder for coroutine, the bytecode will be instrumented
         *  to save and restore the operand stack
         */
        code.invokestatic(p(Py.class), SAVE_OPRANDS.symbolName(), sig(Void.TYPE));
        expr value = node.getInternalValue();
        if (value != null) {
            visit(value);
        } else {
            getNone();
        }
        setLastI(++u.yieldCount);
        code.invokestatic(p(Py.class), YIELD.symbolName(), sig(Void.TYPE, PyObject.class));
        code.invokestatic(p(Py.class), MARK.symbolName(), sig(Void.TYPE));
        code.invokestatic(p(Py.class), RESTORE_OPRANDS.symbolName(), sig(Void.TYPE));

        loadFrame();
        code.invokevirtual(p(PyFrame.class), "getGeneratorInput", sig(Object.class));
        code.checkcast(p(PyObject.class));

        return null;
    }

    /**
     * use the same mechanism as yield, but use two labels to guard the execution, e.g.
     * #1 print(a)
     * #2 yield from b
     * #3 print(x)
     * vtable f_lasti
     * 0 goto #1
     * 1 goto #2
     * 2 goto #3
     * <p>
     * so it can return to yield from repeatly, until f_lasti is modified by the generator
     */
    @Override
    public Object visitYieldFrom(YieldFrom node) {
        if (u.scopeType != CompilerScope.FUNCTION) {
            throw Py.SyntaxError(node.getToken(), "'yield from' outside function", module.getFilename());
        }

        code.invokestatic(p(Py.class), SAVE_OPRANDS.symbolName(), sig(Void.TYPE));
        visit(node.getInternalValue());
        setLastI(++u.yieldCount);
        loadFrame();
        code.invokestatic(p(Py.class), "getYieldFromIter", sig(Void.TYPE, PyObject.class, PyFrame.class));

        code.invokestatic(p(Py.class), MARK.symbolName(), sig(Void.TYPE));

        loadFrame();
        code.invokestatic(p(Py.class), "yieldFrom", sig(PyObject.class, PyFrame.class));
        code.invokestatic(p(Py.class), YIELD.symbolName(), sig(Void.TYPE, PyObject.class));
        u.yieldCount++;
        code.invokestatic(p(Py.class), MARK.symbolName(), sig(Void.TYPE));
        code.invokestatic(p(Py.class), RESTORE_OPRANDS.symbolName(), sig(Void.TYPE));

        // restore return value from subgenerator
        loadFrame();
        code.invokevirtual(p(PyFrame.class), "getf_stacktop", sig(PyObject.class));
        return null;
    }

    @Override
    public Object visitCompare(Compare node) {
        Label end = new Label();
        Label _return = new Label();

        visit(node.getInternalLeft());

        int n = node.getInternalOps().size();
        if (n > 1) {
            for (int i = 0; i < n; i++) {
                visit(node.getInternalComparators().get(i));
                code.dup_x1();
                visitCmpop(node.getInternalOps().get(i));
                code.invokevirtual(p(PyObject.class), "__bool__", sig(Boolean.TYPE));
                code.ifeq(end);
            }
            code.pop();
            code.getstatic(p(Py.class), "True", ci(PyBoolean.class));
            code.goto_(_return);
            code.mark(end);
            code.pop();
            code.getstatic(p(Py.class), "False", ci(PyBoolean.class));
            code.mark(_return);
        } else {
            visit(node.getInternalComparators().get(n - 1));
            visitCmpop(node.getInternalOps().get(n - 1));
        }
        return null;
    }

    @Override
    public Object visitCall(Call node) {
        java.util.List<expr> kwargs = new ArrayList<>();
        java.util.List<String> keys = new ArrayList<>();
        java.util.List<expr> values = node.getInternalArgs();
        boolean stararg = values.stream().anyMatch(a -> a instanceof Starred);

        java.util.List<keyword> keywords = node.getInternalKeywords();
        for (int i = 0; i < keywords.size(); i++) {
            String key = keywords.get(i).getInternalArg();
            expr value = keywords.get(i).getInternalValue();
            if (key == null) {
                kwargs.add(value);
            } else {
                if (keys.contains(key)) {
                    throw Py.SyntaxError("keyword argument repeated");
                }
                keys.add(key);
                values.add(value);
            }
        }

        visit(node.getInternalFunc());

        if (stararg || !kwargs.isEmpty()) {
            code.dup();
            loadList(code, values);
            loadStrings(code, keys);
            loadArray(code, kwargs);
            code.invokestatic(p(BaseCode.class), "destructArguments", sig(Object[].class, PyObject.class, java.util.List.class, String[].class, PyObject[].class));
            loadThreadState();
            code.swap();
            code.dup();
            code.iconst(0);
            code.aaload();
            code.checkcast(p(PyObject[].class));
            code.swap();
            code.iconst(1);
            code.aaload();
            code.checkcast(p(String[].class));

            code.visitInvokeDynamicInsn(EMPTY_NAME, sig(PyObject.class, PyObject.class, ThreadState.class,
                    PyObject[].class, String[].class), LINKERBOOTSTRAP, Bootstrap.CALL);

        } else if (keys.size() > 0 || values.size() > 4) {
            loadThreadState();
            loadArray(code, values);
            loadStrings(code, keys);
            code.visitInvokeDynamicInsn(EMPTY_NAME, sig(PyObject.class, PyObject.class, ThreadState.class,
                    PyObject[].class, String[].class), LINKERBOOTSTRAP, Bootstrap.CALL);
        } else {
            loadThreadState();
            switch (values.size()) {
                case 0:
                    code.visitInvokeDynamicInsn(EMPTY_NAME, sig(PyObject.class, PyObject.class, ThreadState.class), LINKERBOOTSTRAP, Bootstrap.CALL);
                    break;
                case 1:
                    expr arg = values.get(0);
                    visit(arg);
                    if (arg instanceof NameConstant && ((NameConstant) arg).getInternalValue().equals(EXCINFO.symbolName())) {
                        // special case for sys.excinfo hack, used by desugared "With" stmt
                        code.invokevirtual(p(PyObject.class), "__call__",
                                sig(PyObject.class, ThreadState.class, PyObject.class, PyObject.class, PyObject.class));
//                        code.visitInvokeDynamicInsn(EMPTY_NAME, sig(PyObject.class, PyObject.class, ThreadState.class,
//                                PyObject.class, PyObject.class, PyObject.class), LINKERBOOTSTRAP, Bootstrap.CALL);
                    } else {
                        code.visitInvokeDynamicInsn(EMPTY_NAME, sig(PyObject.class, PyObject.class, ThreadState.class,
                                PyObject.class), LINKERBOOTSTRAP, Bootstrap.CALL);
                    }
                    break;
                case 2:
                    visit(values.get(0));
                    visit(values.get(1));
                    code.visitInvokeDynamicInsn(EMPTY_NAME, sig(PyObject.class, PyObject.class, ThreadState.class,
                            PyObject.class, PyObject.class), LINKERBOOTSTRAP, Bootstrap.CALL);
                    break;
                case 3:
                    visit(values.get(0));
                    visit(values.get(1));
                    visit(values.get(2));
                    code.visitInvokeDynamicInsn(EMPTY_NAME, sig(PyObject.class, PyObject.class, ThreadState.class,
                            PyObject.class, PyObject.class, PyObject.class), LINKERBOOTSTRAP, Bootstrap.CALL);

                    break;
                case 4:
                    visit(values.get(0));
                    visit(values.get(1));
                    visit(values.get(2));
                    visit(values.get(3));
                    code.visitInvokeDynamicInsn(EMPTY_NAME, sig(PyObject.class, PyObject.class, ThreadState.class,
                            PyObject.class, PyObject.class, PyObject.class, PyObject.class), LINKERBOOTSTRAP, Bootstrap.CALL);

                    break;
                default:
                    break;
            }
        }
        return null;
    }

    @Override
    public Object visitNum(Num node) {
        module.constant(node).get(code);
        return null;
    }

    @Override
    public Object visitStr(Str node) {
        module.constant(node).get(code);
        return null;
    }

    @Override
    public Object visitFormattedValue(FormattedValue node) {
        visit(node.getInternalValue());
        int conversion = node.getInternalConversion();
        if (conversion == 'r') {
            code.invokevirtual(p(PyObject.class), "__repr__", sig(PyUnicode.class));
        } else {
            code.invokevirtual(p(PyObject.class), "__str__", sig(PyUnicode.class));
        }
        expr formatSpec = node.getInternalFormat_spec();
        if (formatSpec != null) {
            visit(formatSpec);
            code.invokevirtual(p(PyObject.class), "__format__", sig(PyObject.class, PyObject.class));
            code.checkcast(p(PyUnicode.class));
        }
        return null;
    }

    @Override
    public Object visitJoinedStr(JoinedStr node) {
        java.util.List<expr> values = node.getInternalValues();
        int n = values.size();
        code.iconst(n);
        code.anewarray(p(PyObject.class));

        for (int i = 0; i < values.size(); i++) {
            code.dup();
            code.iconst(i);
            visit(values.get(i));
            code.aastore();
        }
        code.invokestatic(p(Py.class), "buildString", sig(PyObject.class, PyObject[].class));
        return null;
    }

    @Override
    public Object visitBytes(Bytes node) {
        module.constant(node).get(code);
        return null;
    }

    @Override
    public Object visitNameConstant(NameConstant node) {
        String name = node.getInternalValue();
        if (name.equals("None")) {
            getNone();
        } else if (name.equals(EXCINFO.symbolName())) {
            getExcInfo();
        } else {
            loadFrame();
            emitGetGlobal(name);
        }
        return null;
    }

    @Override
    public Object visitEllipsis(Ellipsis node) {
        code.getstatic(p(Py.class), "Ellipsis", ci(PyObject.class));
        return null;
    }

    @Override
    public boolean enterAttribute(Attribute node) {
        visit(node.getInternalValue());
        return true;
    }

    @Override
    public void leaveAttribute(Attribute node) {
        expr_contextType ctx = node.getInternalCtx();

        switch (ctx) {
            case Del:
                code.ldc(getName(node.getInternalAttr()));
                code.invokevirtual(p(PyObject.class), "__delattr__", sig(Void.TYPE, String.class));
                break;
            case Load:
                code.visitInvokeDynamicInsn(node.getInternalAttr(), sig(PyObject.class, PyObject.class), LINKERBOOTSTRAP, Bootstrap.GET_PROPERTY);
                break;
            case Param:
            case Store:
                code.dup_x1();
                code.visitInvokeDynamicInsn(node.getInternalAttr(), sig(void.class, PyObject.class, PyObject.class), LINKERBOOTSTRAP, Bootstrap.SET_PROPERTY);
                break;
        }
    }

    @Override
    public Object visitAttribute(Attribute node) {
        visit(node.getInternalValue());
        expr_contextType ctx = node.getInternalCtx();

        switch (ctx) {
            case Del:
                code.ldc(getName(node.getInternalAttr()));
                code.invokevirtual(p(PyObject.class), "__delattr__", sig(Void.TYPE, String.class));
                return null;
            case Load:
                code.visitInvokeDynamicInsn(node.getInternalAttr(), sig(PyObject.class, PyObject.class), LINKERBOOTSTRAP, Bootstrap.GET_PROPERTY);
                return null;
            case Param:
            case Store:
                code.visitInvokeDynamicInsn(node.getInternalAttr(), sig(void.class, PyObject.class, PyObject.class), LINKERBOOTSTRAP, Bootstrap.SET_PROPERTY);
                return null;
        }
        return null;
    }

    @Override
    public boolean enterSubscript(Subscript node) {
        visit(node.getInternalValue());
        visit(node.getInternalSlice());

        return true;
    }

    @Override
    public void leaveSubscript(Subscript node) {
        expr_contextType ctx = node.getInternalCtx();
        switch (ctx) {
            case Del:
                code.invokevirtual(p(PyObject.class), "__delitem__", sig(Void.TYPE, PyObject.class));
                break;
            case Load:
                code.visitInvokeDynamicInsn(EMPTY_NAME, sig(PyObject.class, PyObject.class, PyObject.class), LINKERBOOTSTRAP, Bootstrap.GET_ELEMENT);
                break;
            case Param:
            case Store:
                code.dup_x2();
                code.visitInvokeDynamicInsn(EMPTY_NAME, sig(void.class, PyObject.class, PyObject.class, PyObject.class), LINKERBOOTSTRAP, Bootstrap.SET_ELEMENT);
                break;
        }
    }

    @Override
    public Object visitSubscript(Subscript node) {
        visit(node.getInternalValue());
        visit(node.getInternalSlice());
        expr_contextType ctx = node.getInternalCtx();
        switch (ctx) {
            case Del:
                code.invokevirtual(p(PyObject.class), "__delitem__", sig(Void.TYPE, PyObject.class));
                return null;
            case Load:
                code.visitInvokeDynamicInsn(EMPTY_NAME, sig(PyObject.class, PyObject.class, PyObject.class), LINKERBOOTSTRAP, Bootstrap.GET_ELEMENT);
                return null;
            case Param:
            case Store:
                assert false: "use two phase compile for Subscript";
                return null;
        }
        return null;
    }

    @Override
    public boolean enterStarred(Starred node) {
        return node.getInternalValue().enter(this);
    }

    @Override
    public void leaveStarred(Starred node) {
        node.getInternalValue().leave(this);
    }

    @Override
    public Object visitStarred(Starred node) {
        visit(node.getInternalValue());
        return null;
    }

    @Override
    public boolean enterName(Name node) {
        loadFrame();
        return true;
    }

    @Override
    public void leaveName(Name node) {
        if (node.getInternalCtx() == expr_contextType.Store) {
            code.dup_x1();
        }
        nameop(node.getInternalId(), node.getInternalCtx());
    }

    @Override
    public Object visitName(Name node) {
        loadFrame();
        nameop(node.getInternalId(), node.getInternalCtx());
        return null;
    }

    @Override
    public Object visitList(List node) {
        if (node.getInternalCtx() == expr_contextType.Store) {
            return checkStarred(node.getInternalElts(), node, false);
        }
        if (node.getInternalCtx() == expr_contextType.Del) {
            return seqDel(node.getInternalElts());
        }

        loadArray(code, node.getInternalElts());
        code.invokestatic(p(Py.class), "newList", sig(PyList.class, PyObject[].class));
        return null;
    }

    @Override
    public boolean enterTuple(Tuple node) {
//        loadFrame();
//        node.getInternalElts().stream().forEach(e -> e.enter(this));
        return true;
    }

    @Override
    public void leaveTuple(Tuple node) {
        leaveTuple(node, false);
    }

    private void leaveTuple(Tuple node, boolean nested) {
        java.util.List<expr> elts = node.getInternalElts();
        checkStarred(elts, node, nested);
    }

    @Override
    public Object visitTuple(Tuple node) {
        if (node.getInternalCtx() == expr_contextType.Store) {
            assert false: "use two phase compile for destructive assignment";
            return checkStarred(node.getInternalElts(), node, false);
        }

        if (node.getInternalCtx() == expr_contextType.Del) {
            return seqDel(node.getInternalElts());
        }

        loadArray(code, node.getInternalElts());
        code.invokestatic(p(Py.class), "newTuple", sig(PyTuple.class, PyObject[].class));
        return null;
    }

    @Override
    public Object visitSlice(Slice node) {
        code.new_(p(PySlice.class));
        code.dup();
        if (node.getInternalLower() == null) {
            getNone();
        } else {
            visit(node.getInternalLower());
        }
        if (node.getInternalUpper() == null) {
            getNone();
        } else {
            visit(node.getInternalUpper());
        }
        if (node.getInternalStep() == null) {
            getNone();
        } else {
            visit(node.getInternalStep());
        }

        code.invokespecial(p(PySlice.class), "<init>",
                sig(Void.TYPE, PyObject.class, PyObject.class, PyObject.class));
        return null;
    }

    @Override
    public Object visitExtSlice(ExtSlice node) {
        code.new_(p(PyTuple.class));
        code.dup();
        loadArray(code, node.getInternalDims());
        code.invokespecial(p(PyTuple.class), "<init>", sig(Void.TYPE, PyObject[].class));
        return null;
    }

    @Override
    public Object visitIndex(Index node) {
        traverse(node);
        return null;
    }

    public void loadList(Code code, java.util.List<? extends PythonTree> nodes) {
        final int n = nodes.size();
        code.new_(p(ArrayList.class));
        code.dup();
        code.invokespecial(p(ArrayList.class), "<init>", sig(Void.TYPE));

        if (n == 0) {
            return;
        }
        for (int i = 0; i < n; i++) {
            code.dup();
            PythonTree node = nodes.get(i);
            visit(node);
            if (node instanceof Starred) {
                code.invokestatic(p(Py.class), "addAll", sig(Boolean.TYPE, java.util.List.class, PyObject.class));
            } else {
                code.invokevirtual(p(ArrayList.class), "add", sig(Boolean.TYPE, Object.class));
            }
            code.pop();
        }
    }

    public void loadArray(Code code, java.util.List<? extends PythonTree> nodes) {
        loadArray(code, nodes, null, 0);
    }

    public void loadArray(Code code, java.util.List<? extends PythonTree> nodes, java.util.List<? extends PythonTree> moreNodes, int enqueued) {
        int n = enqueued;

        if (nodes != null) {
            n += nodes.size();
        }

        int m = n;

        if (moreNodes != null) {
            n += moreNodes.size();
        }

        if (n == 0) {
            code.getstatic(p(Py.class), "EmptyObjects", ci(PyObject[].class));
            return;
        } else if (module.emitPrimitiveArraySetters(nodes, code)) {
            return;
        }
        code.iconst(n);
        code.anewarray(p(PyObject.class));
        for (int i = enqueued - 1; i >= 0; i--) {
            code.dup_x1();
            code.swap();
            code.iconst(i);
            code.swap();
            code.aastore();
        }

        for (int i = enqueued; i < m; i++) {
            code.dup();
            code.iconst(i);
            visit(nodes.get(i - enqueued));
            code.aastore();
        }
        for (int i = m; i < n; i++) {
            code.dup();
            code.iconst(i);
            visit(moreNodes.get(i - m));
            code.aastore();
        }
    }

    public int getDocStr(java.util.List<stmt> suite) {
        if (suite.size() > 0) {
            stmt stmt = suite.get(0);
            if (stmt instanceof Expr && ((Expr) stmt).getInternalValue() instanceof Str) {
                return 0;
            }
        }
        return -1;
    }

    public boolean makeClosure(CompileUnit innerUnit) {
        int n = innerUnit.freevars.size();

        if (n == 0) {
            return false;
        }

        code.iconst(n);
        code.anewarray(p(PyObject.class));
        Map<String, Integer> innerFreeVars = innerUnit.freevars;
        int cellSize = innerUnit.cellvars.size();
        for (String name : innerFreeVars.keySet()) {
            code.dup();
            code.iconst(innerFreeVars.get(name) - cellSize);
            loadFrame();
            if (u.cellvars.containsKey(name)) {
                code.iconst(u.cellvars.get(name));
            } else {
                code.iconst(u.freevars.get(name));
            }
            code.invokevirtual(p(PyFrame.class), "getclosure", sig(PyObject.class, Integer.TYPE));
            code.aastore();
        }
        return true;
    }

    private Object compileFunction(String internalName, java.util.List<expr> decos, arguments args, java.util.List<stmt> body, PythonTree node) {
        boolean anonymous = node instanceof expr;

        if (!anonymous) {
            loadFrame(); // pairing nameop
        }
        decos.stream().forEach(deco -> {
            visit(deco);
            loadThreadState();
        });

        code.new_(p(PyFunction.class));
        code.dup();
        loadFrame();
        code.getfield(p(PyFrame.class), "f_globals", ci(PyObject.class));
        ArgListCompiler ac = new ArgListCompiler();
        ac.visitArgs(args);
        loadArray(code, ac.getDefaults());

        // kw_defaults
        loadStrings(code, ac.kw_defaults.keySet());
        loadArray(code, new ArrayList(ac.kw_defaults.values()));
        code.invokestatic(p(PyDictionary.class), "fromKV",
                sig(PyDictionary.class, String[].class, PyObject[].class));

        // annotations
        loadStrings(code, ac.annotations.keySet());
        loadArray(code, new ArrayList(ac.annotations.values()));
        code.invokestatic(p(PyDictionary.class), "fromKV",
                sig(PyDictionary.class, String[].class, PyObject[].class));

        enterScope(internalName, CompilerScope.FUNCTION, node, node.getLineno());
        String qualname = u.qualname;

        int docstring = getDocStr(body);
        for (int i = docstring + 1; i < body.size(); i++) {
            visit(body.get(i));
        }
        // blindly appending `return None`, asm will eliminate it
        if (ste.coroutine || ste.generator) {
            setLastI(-1);
        }
        getNone();
        code.areturn();
        if (ste.generator) {
            code.mark(u.genswitch);

            loadFrame();
            getLastI();
            Label[] y = {u.start};
            code.tableswitch(0, y.length - 1, u.start, y);
        }
        CompileUnit innerUnit = u;
        exitScope();

        if (docstring == 0) {
            visit(((Expr) body.get(0)).getInternalValue());
        } else {
            code.aconst_null();
        }
        module.unicodeConstant(qualname).get(code);

        if (!makeClosure(innerUnit)) {
            code.aconst_null();
        }

        code.invokespecial(
                p(PyFunction.class),
                "<init>",
                sig(Void.TYPE, PyObject.class, PyObject[].class, PyDictionary.class, PyDictionary.class,
                        PyCode.class, PyObject.class, PyObject.class, PyObject[].class));

        for (int i = 0; i < decos.size(); i++) {
            code.visitInvokeDynamicInsn(EMPTY_NAME, sig(PyObject.class, PyObject.class, ThreadState.class, PyObject.class),
                    LINKERBOOTSTRAP, Bootstrap.CALL);
        }

        if (!anonymous) {
            nameop(internalName, expr_contextType.Store);
        }
        return null;
    }

    private void restoreLocals() {
        endExceptionHandlers();

        String[] v = code.getActiveLocals();

        loadFrame();
        code.getfield(p(PyFrame.class), "f_savedlocals", ci(Object[].class));

        for (int i = 0; i < v.length; i++) {
            String type = v[i];
            if (type == null) {
                continue;
            }
            code.dup();
            code.iconst(i);
            code.aaload();
            code.checkcast(type);
            code.astore(i);
        }
        code.pop();

        restartExceptionHandlers();
    }

    /**
     * Close all the open exception handler ranges. This should be paired with
     * restartExceptionHandlers to delimit internal code that shouldn't be handled by user handlers.
     * This allows us to set variables without the verifier thinking we might jump out of our
     * handling with an exception.
     */
    private void endExceptionHandlers() {
        Label end = new Label();
        code.mark(end);
        for (ExceptionHandler handler : exceptionHandlers) {
            handler.exceptionEnds.addElement(end);
        }
    }

    private void restartExceptionHandlers() {
        Label start = new Label();
        code.mark(start);
        for (ExceptionHandler handler : exceptionHandlers) {
            handler.exceptionStarts.addElement(start);
        }
    }

    private void saveLocals() {
        String[] v = code.getActiveLocals();
        loadFrame();
        code.iconst(v.length);
        code.anewarray(p(Object.class));

        for (int i = 0; i < v.length; i++) {
            String type = v[i];
            if (type == null) {
                continue;
            }
            code.dup();
            code.iconst(i);
            // code.checkcast(code.pool.Class(p(Object.class)));
            if (i == 2222) {
                code.aconst_null();
            } else {
                code.aload(i);
            }
            code.aastore();
        }

        code.putfield(p(PyFrame.class), "f_savedlocals", ci(Object[].class));
    }

    public Object visitReturn(Return node, boolean inEval) {
        if (!inEval && u.scopeType != CompilerScope.FUNCTION) {
            throw Py.SyntaxError(node.getToken(), "'return' outside function", module.getFilename());
        }
        if (ste.coroutine || ste.generator) {
            setLastI(-1);
        }

        if (node.getInternalValue() != null) {
            visit(node.getInternalValue());
        } else {
            getNone();
        }
        code.areturn();
        return Exit;
    }

    public Object doTest(Label end_of_if, If node, int index) {
        Label end_of_suite = new Label();
        visit(node.getInternalTest());
        code.invokevirtual(p(PyObject.class), "__bool__", sig(Boolean.TYPE));

        code.ifeq(end_of_suite);

        Object exit = suite(node.getInternalBody());

        if (end_of_if != null && exit == null) {
            code.goto_(end_of_if);
        }

        code.mark(end_of_suite);

        if (node.getInternalOrelse() != null) {
            return suite(node.getInternalOrelse()) != null ? exit : null;
        }
        return null;
    }

    public int beginLoop() {
        continueLabels.push(new Label());
        breakLabels.push(new Label());
        exitLabels.push(new Label());
        int savebcf = bcfLevel;
        bcfLevel = exceptionHandlers.size();
        return savebcf;
    }

    public void finishLoop(int savebcf) {
        continueLabels.pop();
        breakLabels.pop();
        exitLabels.pop();
        bcfLevel = savebcf;
    }

    public void exceptionTest(int exc, Label end_of_exceptions, Try node) {
        for (int i = 0; i < node.getInternalHandlers().size(); i++) {
            ExceptHandler handler = (ExceptHandler) node.getInternalHandlers().get(i);

            Label end_of_self = new Label();

            setline(handler.getLineno());
            if (handler.getInternalType() != null) {
                code.aload(exc);
                // get specific exception
                visit(handler.getInternalType());
                code.invokevirtual(p(PyException.class), "match", sig(Boolean.TYPE, PyObject.class));
                code.ifeq(end_of_self);
            } else {
                if (i != node.getInternalHandlers().size() - 1) {
                    throw Py.SyntaxError(node.getToken(), "default 'except:' must be last", module.getFilename());
                }
            }

            if (handler.getInternalName() != null) {
                loadFrame();
                code.aload(exc);
                code.getfield(p(PyException.class), "value", ci(PyObject.class));
                nameop(handler.getInternalName(), expr_contextType.Store);
            }

            // do exception body
            suite(handler.getInternalBody());
            popException();
            code.goto_(end_of_exceptions);
            code.mark(end_of_self);
        }
        code.aload(exc);
        code.athrow();
    }

    public Object suite(java.util.List<stmt> stmts) {
        for (stmt s : stmts) {
            Object exit = visit(s);
            if (exit != null) {
                return Exit;
            }
        }
        return null;
    }

    public void visitCmpop(cmpopType op) {
        if (op == cmpopType.In) {
            code.invokevirtual(p(PyObject.class), "_in", sig(PyObject.class, PyObject.class));
        } else if (op == cmpopType.Is) {
            code.invokevirtual(p(PyObject.class), "_is", sig(PyObject.class, PyObject.class));
        } else if (op == cmpopType.IsNot) {
            code.invokevirtual(p(PyObject.class), "_isnot", sig(PyObject.class, PyObject.class));
        } else if (op == cmpopType.NotIn) {
            code.invokevirtual(p(PyObject.class), "_notin", sig(PyObject.class, PyObject.class));
        } else {
            String name = null;
            switch (op) {
                case Eq:
                    name = "EQ";
                    break;
                case NotEq:
                    name = "NE";
                    break;
                case Lt:
                    name = "LT";
                    break;
                case LtE:
                    name = "LE";
                    break;
                case Gt:
                    name = "GT";
                    break;
                case GtE:
                    name = "GE";
                    break;
            }
            code.getstatic(p(CompareOp.class), name, ci(CompareOp.class));
            code.invokevirtual(p(PyObject.class), "do_richCompare", sig(PyObject.class, PyObject.class, CompareOp.class));
        }
    }


    public Object seqSet(java.util.List<expr> nodes, int count, int countAfter, boolean nested) {
        code.iconst(count);
        code.iconst(countAfter);
        code.invokestatic(p(Py.class), "unpackIterator",
                sig(PyObject[].class, PyObject.class, Integer.TYPE, Integer.TYPE));

        VisitorIF<?> visitor = new Visitor() {
            @Override
            public boolean enterName(Name node) {
                loadFrame();
                code.swap();
                return true;
            }

            @Override
            public boolean enterAttribute(Attribute node) {
                CodeCompiler.this.visit(node.getInternalValue());
                code.swap();
                return true;
            }

            @Override
            public boolean enterSubscript(Subscript node) {
                CodeCompiler.this.visit(node.getInternalValue());
                code.swap();
                CodeCompiler.this.visit(node.getInternalSlice());
                code.swap();
                return true;
            }

            @Override
            public boolean enterStarred(Starred node) {
                return node.getInternalValue().enter(this);
            }

            @Override
            public void leaveName(Name node) {
                nameop(node.getInternalId(), expr_contextType.Store);
            }

            @Override
            public void leaveStarred(Starred node) {
                node.getInternalValue().leave(this);
            }

            @Override
            public void leaveAttribute(Attribute node) {
                code.visitInvokeDynamicInsn(node.getInternalAttr(), sig(void.class, PyObject.class, PyObject.class), LINKERBOOTSTRAP, Bootstrap.SET_PROPERTY);
            }

            @Override
            public void leaveSubscript(Subscript node) {
                code.visitInvokeDynamicInsn(EMPTY_NAME, sig(void.class, PyObject.class, PyObject.class, PyObject.class), LINKERBOOTSTRAP, Bootstrap.SET_ELEMENT);
            }

            @Override
            public void leaveTuple(Tuple node) {
                CodeCompiler.this.leaveTuple(node, true);
            }

//            @Override
//            protected Object unhandled_node(PythonTree node) {
//                return null;
//            }

//            @Override
//            public void traverse(PythonTree node) {
//                node.traverse(this);
//            }
        };
        for (int i = nodes.size() - 1; i >= 0; i--) {
//            if (nested && i != nodes.size() - 1) {
//                // drop the parent unpacked array if nested
//                code.swap();
//                // propagate the parent unpacked array
//                code.dup_x2();
//                code.pop();
//            }
            expr elt = nodes.get(i);
//            if (elt instanceof Subscript) {
//                code.dup_x2();
//            } else {
//                code.dup_x1();
//            }
            code.dup();
            elt.enter(visitor);
            code.iconst(i);
            code.aaload();
            elt.leave(visitor);
            // nested destructive assignment have to clean the value by itself
//            if (nested && i == 0) {
//                code.pop();
//            }
        }
        if (nested) {
            code.pop();
        }
        return null;
    }

    public Object seqDel(java.util.List<expr> nodes) {
        for (expr e : nodes) {
            visit(e);
        }
        return null;
    }

    private Object checkStarred(java.util.List<expr> elts, PythonTree node, boolean nested) {
        boolean foundStarred = false;
        int count = elts.size();
        int countAfter = -1;
        for (int i = 0; i < elts.size(); i++) {
            expr elt = elts.get(i);
            if (elt instanceof Starred) {
                if (foundStarred) {
                    throw Py.SyntaxError(node.getToken(), "two starred expressions in assignment", module.getFilename());
                }
                if (i >= 256) {
                    throw Py.SyntaxError(node.getToken(), "too many expressions in star-unpacking assignment", module.getFilename());
                }
                count = i;
                countAfter = elts.size() - i - 1;
                foundStarred = true;
            }
        }
        return seqSet(elts, count, countAfter, nested);
    }

    private String getName(String name) {
        if (className != null && name.startsWith("__") && !name.endsWith("__")) {
            // remove leading '_' from classname
            int i = 0;
            while (className.charAt(i) == '_') {
                i++;
            }
            return "_" + className.substring(i) + name;
        }
        return name;
    }

    void emitGetGlobal(String name) {
        code.ldc(name);
        code.invokevirtual(p(PyFrame.class), "getglobal", sig(PyObject.class, String.class));
    }

    /** a named operation in frame
     *  NOTE: frame is not loaded here, please pair loadFrame() manually
     * @param name
     * @param ctx
     */
    private void nameop(String name, expr_contextType ctx) {
        Map<String, Integer> dict = u.names;
        String mangled = name; // mangle(u._private, name);
        Op op = Op.EMPTY;
        OpType optype = OpType.OP_NAME;
        Symtable.Flag scope = u.ste.getScope(mangled);
        switch (scope) {
            case FREE:
                dict = u.freevars;
                optype = OpType.OP_DEREF;
                break;
            case CELL:
                dict = u.cellvars;
                optype = OpType.OP_DEREF;
                break;
            case LOCAL:
                if (u.ste.type == PySTEntryObject.BlockType.FunctionBlock) {
                    optype = OpType.OP_FAST;
                }
                break;
            case GLOBAL_IMPLICIT:
                if (u.ste.type == PySTEntryObject.BlockType.FunctionBlock) {
                    optype = OpType.OP_GLOBAL;
                }
                break;
            case GLOBAL_EXPLICIT:
                optype = OpType.OP_GLOBAL;
                break;
            default:
                /** scope can be 0 */
                break;
        }

        switch (optype) {
            case OP_DEREF:
                switch (ctx) {
                    case Load:
                        op = u.ste.type == PySTEntryObject.BlockType.ClassBlock ? Op.LOAD_CLASSDEREF : Op.LOAD_DEREF;
                        break;
                    case Store:
                        op = Op.STORE_DEREF;
                        break;
                    case AugLoad:
                    case AugStore:
                        break;
                    case Del:
                        op = Op.DELETE_DEREF;
                        break;
                    case Param:
                    default:
                        throw Py.SystemError("param invalid for deref variable");
                }
                break;
            case OP_FAST:
                switch (ctx) {
                    case Load:
                        op = Op.LOAD_FAST;
                        break;
                    case Store:
                        op = Op.STORE_FAST;
                        break;
                    case AugLoad:
                    case AugStore:
                        break;
                    case Del:
                        op = Op.DELETE_FAST;
                        break;
                    case Param:
                    default:
                        throw Py.SystemError("param invalid for local variable");
                }
                addop_o(op, mangled, u.varnames);
                return;
            case OP_GLOBAL:
                switch (ctx) {
                    case Load:
                        op = Op.LOAD_GLOBAL;
                        break;
                    case Store:
                        op = Op.STORE_GLOBAL;
                        break;
                    case AugLoad:
                    case AugStore:
                        break;
                    case Del:
                        op = Op.DELETE_GLOBAL;
                        break;
                    case Param:
                    default:
                        throw Py.SystemError("param invalid for global variable");
                }
                break;
            case OP_NAME:
                switch (ctx) {
                    case Load:
                        op = Op.LOAD_NAME;
                        break;
                    case Store:
                        op = Op.STORE_NAME;
                        break;
                    case AugLoad:
                    case AugStore:
                        break;
                    case Del:
                        op = Op.DELETE_NAME;
                        break;
                    case Param:
                    default:
                        throw Py.SystemError("param invalid for name variable");
                }
                break;
        }

        assert op != Op.EMPTY;
        addop_o(op, mangled, dict);
    }

    private void addop_o(Op op, String mangled, Map<String, Integer> dict) {
        if (dict.containsKey(mangled)) {
            code.iconst(dict.get(mangled));
        } else {
            int i = dict.size();
            dict.put(mangled, i);
            code.iconst(i);
        }
        op.invoke(code);
    }

    @Override
    protected Object unhandled_node(PythonTree node) {
        throw new RuntimeException("Unhandled node " + node);
    }

    enum OpType {
        OP_FAST, OP_GLOBAL, OP_DEREF, OP_NAME;
    }

    enum Op {
        EMPTY(null),
        LOAD_CLASSDEREF("getderef"),
        LOAD_DEREF("getderef"),
        STORE_DEREF("setderef"),
        DELETE_DEREF("delderef"),
        LOAD_FAST("getlocal"),
        STORE_FAST("setlocal"),
        DELETE_FAST("dellocal"),
        LOAD_GLOBAL("getglobal"),
        STORE_GLOBAL("setglobal"),
        DELETE_GLOBAL("delglobal"),
        LOAD_NAME("getname"),
        STORE_NAME("setname"),
        DELETE_NAME("delname");

        private String name;

        Op(String name) {
            this.name = name;
        }

        public void invoke(Code code) {
            if (isSet()) {
                code.invokevirtual(p(PyFrame.class), name, sig(void.class, PyObject.class, int.class));
            } else if (isDel()) {
                code.invokevirtual(p(PyFrame.class), name, sig(void.class, int.class));
            } else {
                code.invokevirtual(p(PyFrame.class), name, sig(PyObject.class, int.class));
            }
        }

        public boolean isSet() {
            return name.startsWith("set");
        }

        public boolean isDel() {
            return name.startsWith("del");
        }
    }

    /**
     * Data about a given exception range whether a try:finally: or a try:except:. The finally needs
     * to inline the finally block for each exit of the try: section, so we carry around that data
     * for it.
     * <p>
     * Both of these need to stop exception coverage of an area that is either the inlined fin ally
     * of a parent try:finally: or the reentry block after a yield. Thus we keep around a set of
     * exception ranges that the catch block will eventually handle.
     */
    class ExceptionHandler {

        /**
         * Each handler gets several exception ranges, this is because inlined finally exit code
         * shouldn't be covered by the exception handler of that finally block. Thus each time we
         * inline the finally code, we stop one range and then enter a new one.
         * <p>
         * We also need to stop coverage for the recovery of the locals after a yield.
         */
        public Vector<Label> exceptionStarts = new Vector<Label>();
        public Vector<Label> exceptionEnds = new Vector<Label>();
        public PythonTree node = null;

        public ExceptionHandler() {
        }

        public ExceptionHandler(PythonTree n) {
            node = n;
        }

        public boolean isFinallyHandler() {
            return node != null;
        }

        public void addExceptionHandlers(Label handlerStart) {
            for (int i = 0; i < exceptionStarts.size(); ++i) {
                Label start = exceptionStarts.elementAt(i);
                Label end = exceptionEnds.elementAt(i);
                // the start and end label has to match
                if (start.getOffset() != end.getOffset()) {
                    code.trycatch(exceptionStarts.elementAt(i), exceptionEnds.elementAt(i),
                            handlerStart, p(Throwable.class));
                }
            }
        }

        public void finalBody(CodeCompiler compiler) {
            if (node instanceof Try) {
                compiler.suite(((Try) node).getInternalFinalbody());
            }
        }
    }
}
