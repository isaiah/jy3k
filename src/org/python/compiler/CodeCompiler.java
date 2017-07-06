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
import org.python.antlr.ast.While;
import org.python.antlr.ast.Yield;
import org.python.antlr.ast.YieldFrom;
import org.python.antlr.ast.alias;
import org.python.antlr.ast.cmpopType;
import org.python.antlr.ast.expr_contextType;
import org.python.antlr.ast.keyword;
import org.python.antlr.base.expr;
import org.python.antlr.base.mod;
import org.python.antlr.base.stmt;
import org.python.core.CompareOp;
import org.python.core.CompilerFlags;
import org.python.core.Py;
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
import java.util.Collection;
import java.util.Deque;
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
    /** Operation that without a name, such as GET_ELEMENT */
    private static final String EMPTY_NAME = String.valueOf(new char[]{ESCAPE_C, NULL_ESCAPE_C});

    private static final Object Exit = Integer.valueOf(1);
    private static final Object NoExit = null;
    private Module module;
    private Code code;
    private CompilerFlags cflags;
    private int temporary;

    private boolean fast_locals;
    private Map<String, SymInfo> tbl;
    private ScopeInfo my_scope;
    private boolean optimizeGlobals = true;
    private String className;
    private Deque<Label> continueLabels, breakLabels, exitLabels;
    private Deque<ExceptionHandler> exceptionHandlers;

    /*
     * break/continue finally's level. This is the lowest level in the exceptionHandlers which
     * should be executed at break or continue. It is saved/updated/restored when compiling loops. A
     * similar level for returns is not needed because a new CodeCompiler is used for each PyCode,
     * in other words: each 'function'. When returning through finally's all the exceptionHandlers
     * are executed.
     */
    private int bcfLevel = 0;
    private int yield_count = 0;
    private Deque<String> stack;

    public CodeCompiler(Module module) {
        this.module = module;

        continueLabels = new LinkedList<>();
        breakLabels = new LinkedList<>();
        exitLabels = new LinkedList<>();
        exceptionHandlers = new LinkedList<>();
        stack = new LinkedList<>();
    }

    public void popException() throws Exception {
        loadThreadState();
        code.invokestatic(p(Py.class), "popException", sig(Void.TYPE, ThreadState.class));
    }

    public void doRaise() throws Exception {
        loadThreadState();
        code.invokestatic(p(PyException.class), "doRaise", sig(PyException.class, ThreadState.class));
    }

    public void getNone() throws IOException {
        code.getstatic(p(Py.class), "None", ci(PyObject.class));
    }

    public void getExcInfo() throws Exception {
        int exc = code.getLocal(p(PyException.class));
        loadThreadState();
        code.invokevirtual(p(ThreadState.class), "getexc", sig(PyException.class));
        code.astore(exc);
        code.aload(exc);
        code.getfield(p(PyException.class), "type", ci(PyObject.class));
        code.aload(exc);
        code.getfield(p(PyException.class), "value", ci(PyObject.class));
        code.aload(exc);
        code.getfield(p(PyException.class), "traceback", ci(PyTraceback.class));
        code.freeLocal(exc);
    }

    public void loadFrame() throws Exception {
        code.aload(1);
    }

    public void loadThreadState() throws Exception {
        code.aload(2);
    }

    public void setLastI(int idx) throws Exception {
        loadFrame();
        code.iconst(idx);
        code.putfield(p(PyFrame.class), "f_lasti", "I");
    }

    public void getLastI() {
        code.getfield(p(PyFrame.class), "f_lasti", "I");
    }

    private void loadf_back() throws Exception {
        code.getfield(p(PyFrame.class), "f_back", ci(PyFrame.class));
    }

    public int storeTop() throws Exception {
        int tmp = code.getLocal(p(PyObject.class));
        code.astore(tmp);
        return tmp;
    }

    public void setline(int line) throws Exception {
        if (module.linenumbers) {
            code.setline(line);
            loadFrame();
            code.iconst(line);
            code.invokevirtual(p(PyFrame.class), "setline", sig(Void.TYPE, Integer.TYPE));
        }
    }

    public void setline(PythonTree node) throws Exception {
        setline(node.getLine());
    }

    public void set(PythonTree node) throws Exception {
        int tmp = storeTop();
        set(node, tmp);
//        code.aconst_null();
//        code.astore(tmp);
        code.freeLocal(tmp);
    }

    public void set(PythonTree node, int tmp) throws Exception {
        temporary = tmp;
        visit(node);
    }

    static boolean checkOptimizeGlobals(boolean fast_locals, ScopeInfo scope) {
        return fast_locals && !scope.exec && !scope.from_import_star;
    }

    void parse(mod node, Code code, boolean fast_locals, String className, ScopeInfo scope,
               CompilerFlags cflags) throws Exception {
        this.fast_locals = fast_locals;
        this.className = className;
        this.code = code;
        this.cflags = cflags;
        this.my_scope = scope;
        this.tbl = scope.tbl;

        // BEGIN preparse
        Label genswitch = new Label();
        if (my_scope.generator) {
            code.goto_(genswitch);
        }
        Label start = new Label();
        code.mark(start);

        int nparamcell = my_scope.jy_paramcells.size();
        if (nparamcell > 0) {
            java.util.List<String> paramcells = my_scope.jy_paramcells;
            for (int i = 0; i < nparamcell; i++) {
                code.aload(1);
                SymInfo syminf = tbl.get(paramcells.get(i));
                code.iconst(syminf.locals_index);
                code.iconst(syminf.env_index);
                code.invokevirtual(p(PyFrame.class), "to_cell",
                        sig(Void.TYPE, Integer.TYPE, Integer.TYPE));
            }
        }
        // END preparse

        optimizeGlobals = checkOptimizeGlobals(fast_locals, my_scope);

        Object exit = visit(node);
        if (exit == null) {
            setLastI(-1);

            getNone();
            code.areturn();
        }

        // BEGIN postparse

        // similar to visitResume code in pyasm.py
        if (my_scope.generator) {
            code.mark(genswitch);

            code.aload(1);
            getLastI();
            Label[] y = {start};
            code.tableswitch(0, y.length - 1, start, y);
        }
        // END postparse
    }

    @Override
    public Object visitInteractive(Interactive node) throws Exception {
        traverse(node);
        return null;
    }

    @Override
    public Object visitModule(org.python.antlr.ast.Module suite) throws Exception {
        Str docStr = getDocStr(suite.getInternalBody());
        if (docStr != null) {
            loadFrame();
            code.ldc("__doc__");
            visit(docStr);
            code.invokevirtual(p(PyFrame.class), "setglobal",
                    sig(Void.TYPE, String.class, PyObject.class));
        }
        traverse(suite);
        return null;
    }

    @Override
    public Object visitExpression(Expression node) throws Exception {
//        if (my_scope.generator && node.getInternalBody() != null) {
//            module.error("'return' with argument inside generator", true, node);
//        }
        return visitReturn(new Return(node, node.getInternalBody()), true);
    }

    public void loadList(Code code, java.util.List<? extends PythonTree> nodes) throws Exception {
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

    public void loadArray(Code code, java.util.List<? extends PythonTree> nodes) throws Exception {
        final int n;

        if (nodes == null) {
            n = 0;
        } else {
            n = nodes.size();
        }

        if (n == 0) {
            code.getstatic(p(Py.class), "EmptyObjects", ci(PyObject[].class));
            return;
        } else if (module.emitPrimitiveArraySetters(nodes, code)) {
            return;
        }
        code.iconst(n);
        code.anewarray(p(PyObject.class));
        for (int i = 0; i < n; i++) {
            code.dup();
            code.iconst(i);
            visit(nodes.get(i));
            code.aastore();
        }
    }


    public Str getDocStr(java.util.List<stmt> suite) {
        if (suite.size() > 0) {
            stmt stmt = suite.get(0);
            if (stmt instanceof Expr && ((Expr) stmt).getInternalValue() instanceof Str) {
                return (Str) ((Expr) stmt).getInternalValue();
            }
        }
        return null;
    }

    public boolean makeClosure(ScopeInfo scope) throws Exception {
        if (scope == null || scope.freevars == null) {
            return false;
        }
        int n = scope.freevars.size();
        if (n == 0) {
            return false;
        }

        code.iconst(n);
        code.anewarray(p(PyObject.class));
        Map<String, SymInfo> upTbl = scope.up.tbl;
        for (int i = 0; i < n; i++) {
            code.dup();
            code.iconst(i);
            loadFrame();
            for (int j = 1; j < scope.distance; j++) {
                loadf_back();
            }
            SymInfo symInfo = upTbl.get(scope.freevars.get(i));
            code.iconst(symInfo.env_index);
            code.invokevirtual(p(PyFrame.class), "getclosure", sig(PyObject.class, Integer.TYPE));
            code.aastore();
        }

        return true;
    }

    @Override
    public Object visitAsyncFunctionDef(AsyncFunctionDef node) throws Exception {
        String name = node.getInternalName();
        java.util.List<expr> decs = node.getInternalDecorator_list();
        java.util.List<stmt> body = node.getInternalBody();
        return compileFunction(name, decs, body, node);
    }

    @Override
    public Object visitFunctionDef(FunctionDef node) throws Exception {
        String name = node.getInternalName();
        java.util.List<expr> decs = node.getInternalDecorator_list();
        java.util.List<stmt> body = node.getInternalBody();
        return compileFunction(name, decs, body, node);
    }

    private Object compileFunction(String internalName, java.util.List<expr> decs, java.util.List<stmt> body, stmt node) throws Exception {
        String name = getName(internalName);
        setline(node);

        ScopeInfo scope = module.getScopeInfo(node);

        // NOTE: this is attached to the constructed PyFunction, so it cannot be nulled out
        // with freeArray, unlike other usages of makeArray here
        code.new_(p(PyFunction.class));
        code.dup();
        loadFrame();
        code.getfield(p(PyFrame.class), "f_globals", ci(PyObject.class));
        loadArray(code, scope.ac.getDefaults());

        // kw_defaults
        loadStrings(code, scope.ac.kw_defaults.keySet());
        loadArray(code, new ArrayList<>(scope.ac.kw_defaults.values()));
        code.invokestatic(p(PyDictionary.class), "fromKV",
                sig(PyDictionary.class, String[].class, PyObject[].class));

        // annotations
        loadStrings(code, scope.ac.annotations.keySet());
        loadArray(code, new ArrayList<>(scope.ac.annotations.values()));
        code.invokestatic(p(PyDictionary.class), "fromKV",
                sig(PyDictionary.class, String[].class, PyObject[].class));

        scope.setup_closure();
//        scope.dump();
        module.codeConstant(new Suite(node, body), name, true, className,
                node.getLine(), scope, cflags).get(code);

        Str docStr = getDocStr(body);
        if (docStr != null) {
            visit(docStr);
        } else {
            code.aconst_null();
        }
        code.ldc(scope.qualname);

        if (!makeClosure(scope)) {
            code.invokespecial(p(PyFunction.class), "<init>",
                    sig(Void.TYPE, PyObject.class, PyObject[].class, PyDictionary.class, PyDictionary.class,
                            PyCode.class, PyObject.class, String.class));
        } else {
            code.invokespecial(
                    p(PyFunction.class),
                    "<init>",
                    sig(Void.TYPE, PyObject.class, PyObject[].class, PyDictionary.class, PyDictionary.class,
                            PyCode.class, PyObject.class, String.class, PyObject[].class));
        }

        applyDecorators(decs);

        set(new Name(node, internalName, expr_contextType.Store));
        return null;
    }

    private void applyDecorators(java.util.List<expr> decorators) throws Exception {
        if (decorators != null && !decorators.isEmpty()) {
            int res = storeTop();
            for (expr decorator : decorators) {
                visit(decorator);
            }
            for (int i = decorators.size(); i > 0; i--) {
                loadThreadState();
                code.aload(res);
                code.invokevirtual(p(PyObject.class), "__call__",
                        sig(PyObject.class, ThreadState.class, PyObject.class));
                code.astore(res);
            }
            code.aload(res);
            code.freeLocal(res);
        }
    }

    @Override
    public Object visitExpr(Expr node) throws Exception {
        setline(node);
        visit(node.getInternalValue());

        if (node.isPrint()) {
            code.invokestatic(p(Py.class), "printResult", sig(Void.TYPE, PyObject.class));
        } else {
            code.pop();
        }
        return null;
    }

    @Override
    public Object visitAssign(Assign node) throws Exception {
        setline(node);
        visit(node.getInternalValue());
        if (node.getInternalTargets().size() == 1) {
            set(node.getInternalTargets().get(0));
        } else {
            int tmp = storeTop();
            for (expr target : node.getInternalTargets()) {
                set(target, tmp);
            }
            code.freeLocal(tmp);
        }
        return null;
    }

    @Override
    public Object visitDelete(Delete node) throws Exception {
        setline(node);
        traverse(node);
        return null;
    }

    @Override
    public Object visitPass(Pass node) throws Exception {
        setline(node);
        return null;
    }

    @Override
    public Object visitExitFor(ExitFor node) throws Exception {
        code.goto_(exitLabels.peek());
        return null;
    }

    @Override
    public Object visitBreak(Break node) throws Exception {
        // setline(node); Not needed here...
        if (breakLabels.isEmpty()) {
            throw Py.SyntaxError(node.getToken(), "'break' outside loop", module.getFilename());
        }

//        doFinallysDownTo(bcfLevel);

        code.goto_(breakLabels.peek());
        return null;
    }

    @Override
    public Object visitContinue(Continue node) throws Exception {
        // setline(node); Not needed here...
        if (continueLabels.isEmpty()) {
            throw Py.SyntaxError(node.getToken(), "'continue' not properly in loop", module.getFilename());
        }

//        doFinallysDownTo(bcfLevel);

        code.goto_(continueLabels.peek());
        return Exit;
    }


    @Override
    public Object visitAwait(Await node) throws Exception {
        setline(node);
        code.invokestatic(p(Py.class), SAVE_OPRANDS.symbolName(), sig(Void.TYPE));
        visit(node.getInternalValue());

        setLastI(++yield_count);
        code.invokestatic(p(Py.class), "getAwaitableIter", sig(PyObject.class, PyObject.class));
        loadFrame();
        code.swap();
        code.putfield(p(PyFrame.class), "f_yieldfrom", ci(PyObject.class));
        saveLocals();

        code.invokestatic(p(Py.class), MARK.symbolName(), sig(Void.TYPE));
        loadFrame();
        code.invokestatic(p(Py.class), "yieldFrom", sig(PyObject.class, PyFrame.class));
        code.invokestatic(p(Py.class), YIELD.symbolName(), sig(Void.TYPE, PyObject.class));
        yield_count++;
        code.invokestatic(p(Py.class), MARK.symbolName(), sig(Void.TYPE));
        restoreLocals();
        code.invokestatic(p(Py.class), RESTORE_OPRANDS.symbolName(), sig(Void.TYPE));

        // restore return value from subgenerator
        loadFrame();
        code.invokevirtual(p(PyFrame.class), "getf_stacktop", sig(PyObject.class));
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
    public Object visitYieldFrom(YieldFrom node) throws Exception {
        if (!fast_locals) {
            throw Py.SyntaxError(node.getToken(), "'yield from' outside function", module.getFilename());
        }

        code.invokestatic(p(Py.class), SAVE_OPRANDS.symbolName(), sig(Void.TYPE));
        visit(node.getInternalValue());
        setLastI(++yield_count);
        loadFrame();
        code.invokestatic(p(Py.class), "getYieldFromIter", sig(Void.TYPE, PyObject.class, PyFrame.class));
        saveLocals();

        code.invokestatic(p(Py.class), MARK.symbolName(), sig(Void.TYPE));

        loadFrame();
        code.invokestatic(p(Py.class), "yieldFrom", sig(PyObject.class, PyFrame.class));
        code.invokestatic(p(Py.class), YIELD.symbolName(), sig(Void.TYPE, PyObject.class));
        yield_count++;
        code.invokestatic(p(Py.class), MARK.symbolName(), sig(Void.TYPE));
        restoreLocals();
        code.invokestatic(p(Py.class), RESTORE_OPRANDS.symbolName(), sig(Void.TYPE));

        // restore return value from subgenerator
        loadFrame();
        code.invokevirtual(p(PyFrame.class), "getf_stacktop", sig(PyObject.class));
        return null;
    }

    @Override
    public Object visitYield(Yield node) throws Exception {
        setline(node);
        if (!fast_locals) {
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
        setLastI(++yield_count);
        saveLocals();
        code.invokestatic(p(Py.class), YIELD.symbolName(), sig(Void.TYPE, PyObject.class));
        code.invokestatic(p(Py.class), MARK.symbolName(), sig(Void.TYPE));
        restoreLocals();
        code.invokestatic(p(Py.class), RESTORE_OPRANDS.symbolName(), sig(Void.TYPE));

        loadFrame();
        code.invokevirtual(p(PyFrame.class), "getGeneratorInput", sig(Object.class));
        code.checkcast(p(PyObject.class));

        return null;
    }

    private void restoreLocals() throws Exception {
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
        for (ExceptionHandler handler:  exceptionHandlers) {
            handler.exceptionEnds.addElement(end);
        }
    }

    private void restartExceptionHandlers() {
        Label start = new Label();
        code.mark(start);
        for (ExceptionHandler handler:  exceptionHandlers) {
            handler.exceptionStarts.addElement(start);
        }
    }

    private void saveLocals() throws Exception {
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

    @Override
    public Object visitReturn(Return node) throws Exception {
        return visitReturn(node, false);
    }

    public Object visitReturn(Return node, boolean inEval) throws Exception {
        setline(node);
        if (!inEval && !fast_locals) {
            throw Py.SyntaxError(node.getToken(), "'return' outside function", module.getFilename());
        }
        int tmp = 0;
        if (node.getInternalValue() != null) {
            visit(node.getInternalValue());
            tmp = code.getReturnLocal();
            code.astore(tmp);
        }
//        doFinallysDownTo(0);

        setLastI(-1);

        if (node.getInternalValue() != null) {
            code.aload(tmp);
        } else {
            getNone();
        }
        code.areturn();
        return Exit;
    }

    @Override
    public Object visitRaise(Raise node) throws Exception {
        setline(node);
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
    public Object visitImport(Import node) throws Exception {
        setline(node);
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
            code.ldc(name);
            code.aconst_null();
            code.iconst(0);
            code.invokestatic(p(org.python.bootstrap.Import.class), "importName",
                    sig(PyObject.class,  PyFrame.class, String.class, String[].class, Integer.TYPE));
            if (aliased) {
                while(dot > 0) {
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
            set(new Name(a, asname, expr_contextType.Store));
        }
        return null;
    }

    @Override
    public Object visitImportFrom(ImportFrom node) throws Exception {
        java.util.List<alias> aliases = node.getInternalNames();
        setline(node);
        loadFrame();
        code.ldc(node.getInternalModule());
        loadStrings(code, aliases.stream().map(a -> a.getInternalName()).collect(Collectors.toList()));
        code.iconst(node.getInternalLevel());
        code.invokestatic(p(org.python.bootstrap.Import.class), "importName",
                sig(PyObject.class,  PyFrame.class, String.class, String[].class, Integer.TYPE));
        if (aliases == null || aliases.size() == 0) {
            throw Py.SyntaxError(node.getToken(), "Internel parser error", module.getFilename());
        } else if (aliases.size() == 1 && aliases.get(0).getInternalName().equals("*")) {
            if (my_scope.func_level > 0) {
                module.error("import * only allowed at module level", false, node);
            }

            loadFrame();
            code.swap();
            code.invokestatic(p(org.python.bootstrap.Import.class), "importAllFrom",
                    sig(Void.TYPE, PyFrame.class, PyObject.class));

        } else {
            for (alias asName: aliases) {
                String from = asName.getInternalName();
                String as = asName.getInternalAsname();
                if (as == null) {
                    as = from;
                }
                code.dup();
                code.ldc(from);
                code.invokestatic(p(org.python.bootstrap.Import.class), "importFrom",
                        sig(PyObject.class,  PyObject.class, String.class));
                set(new Name(node, as, expr_contextType.Store));
            }
            code.pop();
        }
        return null;
    }

    @Override
    public Object visitGlobal(Global node) throws Exception {
        return null;
    }

    @Override
    public Object visitNonlocal(Nonlocal node) throws Exception {
        return null;
    }

    @Override
    public Object visitAssert(Assert node) throws Exception {
        setline(node);
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

    public Object doTest(Label end_of_if, If node, int index) throws Exception {
        Label end_of_suite = new Label();

        setline(node.getInternalTest());
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
        } else {
            return null;
        }
    }

    @Override
    public Object visitIf(If node) throws Exception {
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
    public Object visitIfExp(IfExp node) throws Exception {
        setline(node.getInternalTest());
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

    @Override
    public Object visitWhile(While node) throws Exception {
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
        setline(node);

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


    public void exceptionTest(int exc, Label end_of_exceptions, Try node, int index)
            throws Exception {
        for (int i = 0; i < node.getInternalHandlers().size(); i++) {
            ExceptHandler handler = (ExceptHandler) node.getInternalHandlers().get(i);

            // setline(name);
            Label end_of_self = new Label();

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
                code.aload(exc);
                code.getfield(p(PyException.class), "value", ci(PyObject.class));
                set(new Name(handler, handler.getInternalName(), expr_contextType.Store));
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

    @Override
    public Object visitTry(Try node) throws Exception {
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
            exceptionTest(exc, handler_end, node, 1);
            code.mark(handler_end);
        } else {
            // Have else clause
            Label else_end = new Label();
            exceptionTest(exc, else_end, node, 1);
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
    public Object visitSuite(Suite node) throws Exception {
        return suite(node.getInternalBody());
    }

    @Override
    public Object visitBlock(Block node) throws Exception {
        for (stmt s: node.getInternalBody()) {
            visit(s);
        }
        return null;
    }

    public Object suite(java.util.List<stmt> stmts) throws Exception {
        for (stmt s : stmts) {
            Object exit = visit(s);
            if (exit != null) {
                return Exit;
            }
        }
        return null;
    }

    @Override
    public Object visitBoolOp(BoolOp node) throws Exception {
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
    public Object visitCompare(Compare node) throws Exception {
        Label end = new Label();

        visit(node.getInternalLeft());

        int n = node.getInternalOps().size();
        if (n > 1) {
            int result = code.getLocal(p(PyObject.class));
            for (int i = 0; i < n; i++) {
                visit(node.getInternalComparators().get(i));
                code.dup_x1();
                visitCmpop(node.getInternalOps().get(i));
                code.dup();
                code.astore(result);
                code.invokevirtual(p(PyObject.class), "__bool__", sig(Boolean.TYPE));
                code.ifeq(end);
            }
            code.mark(end);
            code.pop();
            code.aload(result);
            code.freeLocal(result);
        } else {
            visit(node.getInternalComparators().get(n - 1));
            visitCmpop(node.getInternalOps().get(n - 1));
        }
        return null;
    }

    public void visitCmpop(cmpopType op) throws Exception {
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

    @Override
    public Object visitBinOp(BinOp node) throws Exception {
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
    public Object visitUnaryOp(UnaryOp node) throws Exception {
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
    public Object visitAnnAssign(AnnAssign node) throws Exception {
        if (node.getInternalValue() != null) {
            setline(node);
            visit(node.getInternalValue());
            set(node.getInternalTarget());
        }
        return null;
    }

    /**
     * Counterpart of makeStrings, instead of put the string array in local variable slot, leave it in stack
     * @param c
     * @param names
     * @return
     * @throws IOException
     */
    static void loadStrings(Code c, Collection<String> names) throws IOException {
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

    public Object invokeNoKeywords(Attribute node, java.util.List<expr> values) throws Exception {
        String name = getName(node.getInternalAttr());
        visit(node.getInternalValue());
        code.ldc(name);
        code.invokevirtual(p(PyObject.class), "__getattr__", sig(PyObject.class, String.class));
        loadThreadState();

        switch (values.size()) {
            case 0:
                code.invokevirtual(p(PyObject.class), "__call__",
                        sig(PyObject.class, ThreadState.class));
                break;
            case 1:
                expr arg = values.get(0);
                visit(arg);
                if (arg instanceof NameConstant && ((NameConstant) arg).getInternalValue().equals(EXCINFO.symbolName())) {
                    // special case for sys.excinfo hack, used by desugared "With" stmt
                    code.invokevirtual(p(PyObject.class), "__call__",
                            sig(PyObject.class, ThreadState.class, PyObject.class, PyObject.class, PyObject.class));
                } else {
                    code.invokevirtual(p(PyObject.class), "__call__",
                            sig(PyObject.class, ThreadState.class, PyObject.class));
                }
                break;
            case 2:
                visit(values.get(0));
                visit(values.get(1));
                code.invokevirtual(p(PyObject.class), "__call__",
                        sig(PyObject.class, ThreadState.class, PyObject.class, PyObject.class));
                break;
            case 3:
                visit(values.get(0));
                visit(values.get(1));
                visit(values.get(2));
                code.invokevirtual(
                        p(PyObject.class),
                        "__call__",
                        sig(PyObject.class, ThreadState.class, PyObject.class, PyObject.class,
                                PyObject.class));
                break;
            case 4:
                visit(values.get(0));
                visit(values.get(1));
                visit(values.get(2));
                visit(values.get(3));
                code.invokevirtual(
                        p(PyObject.class),
                        "__call__",
                        sig(PyObject.class, ThreadState.class, PyObject.class, PyObject.class,
                                PyObject.class, PyObject.class));
                break;
            default:
                loadArray(code, values);
                code.invokevirtual(p(PyObject.class), "__call__",
                        sig(PyObject.class, ThreadState.class, PyObject[].class));
                break;
        }
        return null;
    }

    @Override
    public Object visitCall(Call node) throws Exception {
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
            loadList(code, values);
            loadStrings(code, keys);
            loadArray(code, kwargs);
            code.invokevirtual(
                    p(PyObject.class),
                    "_callextra",
                    sig(PyObject.class, java.util.List.class, String[].class, PyObject[].class));
        } else if (keys.size() > 0) {
            loadThreadState();
            loadArray(code, values);
            loadStrings(code, keys);
            code.invokevirtual(p(PyObject.class), "__call__",
                    sig(PyObject.class, ThreadState.class, PyObject[].class, String[].class));
        } else {
            if (values.size() != 0)
                loadThreadState();
            switch (values.size()) {
                case 0:
                    code.visitInvokeDynamicInsn(EMPTY_NAME, sig(PyObject.class, PyObject.class), LINKERBOOTSTRAP, Bootstrap.CALL);
//                    code.invokevirtual(p(PyObject.class), "__call__",
//                            sig(PyObject.class, ThreadState.class));
                    break;
                case 1:
                    expr arg = values.get(0);
                    visit(arg);
                    if (arg instanceof NameConstant && ((NameConstant) arg).getInternalValue().equals(EXCINFO.symbolName())) {
                        // special case for sys.excinfo hack, used by desugared "With" stmt
                        code.invokevirtual(p(PyObject.class), "__call__",
                                sig(PyObject.class, ThreadState.class, PyObject.class, PyObject.class, PyObject.class));
                    } else {
                        code.invokevirtual(p(PyObject.class), "__call__",
                                sig(PyObject.class, ThreadState.class, PyObject.class));
                    }
                    break;
                case 2:
                    visit(values.get(0));
                    visit(values.get(1));
                    code.invokevirtual(p(PyObject.class), "__call__",
                            sig(PyObject.class, ThreadState.class, PyObject.class, PyObject.class));
                    break;
                case 3:
                    visit(values.get(0));
                    visit(values.get(1));
                    visit(values.get(2));
                    code.invokevirtual(
                            p(PyObject.class),
                            "__call__",
                            sig(PyObject.class, ThreadState.class, PyObject.class, PyObject.class,
                                    PyObject.class));
                    break;
                case 4:
                    visit(values.get(0));
                    visit(values.get(1));
                    visit(values.get(2));
                    visit(values.get(3));
                    code.invokevirtual(
                            p(PyObject.class),
                            "__call__",
                            sig(PyObject.class, ThreadState.class, PyObject.class, PyObject.class,
                                    PyObject.class, PyObject.class));
                    break;
                default:
                    loadArray(code, values);
                    code.invokevirtual(p(PyObject.class), "__call__",
                            sig(PyObject.class, ThreadState.class, PyObject[].class));
                    break;
            }
        }
        return null;
    }

    public Object Slice(Subscript node, Slice slice) throws Exception {
        expr_contextType ctx = node.getInternalCtx();
        visit(node.getInternalValue());
        if (slice.getInternalLower() != null) {
            visit(slice.getInternalLower());
        } else {
            code.aconst_null();
        }
        if (slice.getInternalUpper() != null) {
            visit(slice.getInternalUpper());
        } else {
            code.aconst_null();
        }
        if (slice.getInternalStep() != null) {
            visit(slice.getInternalStep());
        } else {
            code.aconst_null();
        }

        switch (ctx) {
            case Del:
                code.invokevirtual(p(PyObject.class), "__delslice__",
                        sig(Void.TYPE, PyObject.class, PyObject.class, PyObject.class));
                break;
            case Load:
                code.invokevirtual(p(PyObject.class), "__getslice__",
                        sig(PyObject.class, PyObject.class, PyObject.class, PyObject.class));
                break;
            case Param:
            case Store:
                code.aload(temporary);
                code.invokevirtual(
                        p(PyObject.class),
                        "__setslice__",
                        sig(Void.TYPE, PyObject.class, PyObject.class, PyObject.class,
                                PyObject.class));
                break;
        }
        return null;

    }

    @Override
    public Object visitSubscript(Subscript node) throws Exception {
        int value = temporary;
        expr_contextType ctx = node.getInternalCtx();
        visit(node.getInternalValue());
        visit(node.getInternalSlice());

        switch (ctx) {
            case Del:
                code.invokevirtual(p(PyObject.class), "__delitem__", sig(Void.TYPE, PyObject.class));
                return null;
            case Load:
                code.visitInvokeDynamicInsn(EMPTY_NAME, sig(PyObject.class, PyObject.class, PyObject.class), LINKERBOOTSTRAP, Bootstrap.GET_ELEMENT);
//                code.invokevirtual(p(PyObject.class), "__getitem__",
//                        sig(PyObject.class, PyObject.class));
                return null;
            case Param:
            case Store:
                code.aload(value);
                code.visitInvokeDynamicInsn(EMPTY_NAME, sig(void.class, PyObject.class, PyObject.class, PyObject.class), LINKERBOOTSTRAP, Bootstrap.SET_ELEMENT);
//                code.invokevirtual(p(PyObject.class), "__setitem__",
//                        sig(Void.TYPE, PyObject.class, PyObject.class));
                return null;
        }
        return null;
    }

    @Override
    public Object visitIndex(Index node) throws Exception {
        traverse(node);
        return null;
    }

    @Override
    public Object visitExtSlice(ExtSlice node) throws Exception {
        code.new_(p(PyTuple.class));
        code.dup();
        loadArray(code, node.getInternalDims());
        code.invokespecial(p(PyTuple.class), "<init>", sig(Void.TYPE, PyObject[].class));
        return null;
    }

    @Override
    public Object visitAttribute(Attribute node) throws Exception {
        visit(node.getInternalValue());
//        code.ldc(getName(node.getInternalAttr()));
        expr_contextType ctx = node.getInternalCtx();

        switch (ctx) {
            case Del:
                code.ldc(getName(node.getInternalAttr()));
                code.invokevirtual(p(PyObject.class), "__delattr__", sig(Void.TYPE, String.class));
                return null;
            case Load:
                code.visitInvokeDynamicInsn(node.getInternalAttr(), sig(PyObject.class, PyObject.class), LINKERBOOTSTRAP, Bootstrap.GET_PROPERTY);
//                code.invokevirtual(p(PyObject.class), "__getattr__",
//                        sig(PyObject.class, String.class));
                return null;
            case Param:
            case Store:
                code.aload(temporary);
                code.visitInvokeDynamicInsn(node.getInternalAttr(), sig(void.class, PyObject.class, PyObject.class), LINKERBOOTSTRAP, Bootstrap.SET_PROPERTY);
//                code.invokevirtual(p(PyObject.class), "__setattr__",
//                        sig(Void.TYPE, String.class, PyObject.class));
                return null;
        }
        return null;
    }

    public Object seqSet(java.util.List<expr> nodes) throws Exception {
        return seqSet(nodes, nodes.size(), -1);
    }

    public Object seqSet(java.util.List<expr> nodes, int count, int countAfter) throws Exception {
        code.aload(temporary);
        code.iconst(count);
        code.iconst(countAfter);
        code.invokestatic(p(Py.class), "unpackIterator",
                sig(PyObject[].class, PyObject.class, Integer.TYPE, Integer.TYPE));

        for (int i = 0; i < nodes.size(); i++) {
            code.dup();
            code.iconst(i);
            code.aaload();
            set(nodes.get(i));
        }
        code.pop();
        return null;
    }

    public Object seqDel(java.util.List<expr> nodes) throws Exception {
        for (expr e : nodes) {
            visit(e);
        }
        return null;
    }

    private Object checkStarred(java.util.List<expr> elts, PythonTree node) throws Exception {
        boolean foundStarred = false;
        int count = elts.size();
        int countAfter = -1;
        for (int i = 0; i < elts.size(); i++) {
            expr elt = elts.get(i);
            if (elt instanceof Starred) {
                if (!foundStarred) {
                    if (i >= 256) {
                        throw Py.SyntaxError(node.getToken(), "too many expressions in star-unpacking assignment", module.getFilename());
                    }
                    count = i;
                    countAfter = elts.size() - i - 1;
                    foundStarred = true;
                } else {
                    throw Py.SyntaxError(node.getToken(), "two starred expressions in assignment", module.getFilename());
                }
            }
        }
        return seqSet(elts, count, countAfter);
    }

    @Override
    public Object visitTuple(Tuple node) throws Exception {
        if (node.getInternalCtx() == expr_contextType.Store) {
            return checkStarred(node.getInternalElts(), node);
        }

        if (node.getInternalCtx() == expr_contextType.Del) {
            return seqDel(node.getInternalElts());
        }

        loadArray(code, node.getInternalElts());
        code.invokestatic(p(Py.class), "newTuple", sig(PyTuple.class, PyObject[].class));
        return null;
    }

    @Override
    public Object visitList(List node) throws Exception {
        if (node.getInternalCtx() == expr_contextType.Store) {
            return checkStarred(node.getInternalElts(), node);
        }
        if (node.getInternalCtx() == expr_contextType.Del) {
            return seqDel(node.getInternalElts());
        }

        loadArray(code, node.getInternalElts());
        code.invokestatic(p(Py.class), "newList", sig(PyList.class, PyObject[].class));
        return null;
    }

    @Override
    public Object visitDict(Dict node) throws Exception {
        java.util.List<PythonTree> elts = new ArrayList<PythonTree>();
        java.util.List<expr> keys = node.getInternalKeys();
        java.util.List<expr> vals = node.getInternalValues();
        for (int i = 0; i < keys.size(); i++) {
            elts.add(keys.get(i));
            elts.add(vals.get(i));
        }

        if (my_scope.generator) {
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
    public Object visitSet(Set node) throws Exception {
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
    public Object visitAnonymousFunction(AnonymousFunction node) throws Exception {
        String name = "<lambda>";

//        // Add a synthetic return node onto the outside of suite;
//        java.util.List<stmt> bod = Arrays.asList(new LambdaSyntheticReturn(node, node.getInternalBody()));
//        mod retSuite = new Suite(node, bod);
        setline(node);
        ScopeInfo scope = module.getScopeInfo(node);
        code.new_(p(PyFunction.class));

        code.dup();
        loadArray(code, scope.ac.getDefaults());

        loadFrame();
        code.getfield(p(PyFrame.class), "f_globals", ci(PyObject.class));
        code.swap();

        code.new_(p(PyDictionary.class));
        code.dup();
        loadStrings(code, scope.ac.kw_defaults.keySet());
        loadArray(code, new ArrayList<>(scope.ac.kw_defaults.values()));
        code.invokespecial(p(PyDictionary.class), "<init>",
                sig(Void.TYPE, String[].class, PyObject[].class));

        scope.setup_closure();
        scope.dump();
        module.codeConstant(new Suite(node, node.getInternalBody()), name, true, className, node.getLine(), scope, cflags).get(code);

        if (!makeClosure(scope)) {
            code.invokespecial(p(PyFunction.class), "<init>",
                    sig(Void.TYPE, PyObject.class, PyObject[].class, PyDictionary.class, PyCode.class));
        } else {
            code.invokespecial(
                    p(PyFunction.class),
                    "<init>",
                    sig(Void.TYPE, PyObject.class, PyObject[].class, PyDictionary.class, PyCode.class, PyObject[].class));
        }
        code.dup();
        code.ldc(scope.qualname);
        code.putfield(p(PyFunction.class), "__qualname__", ci(String.class));
        return null;
    }

    @Override
    public Object visitEllipsis(Ellipsis node) throws Exception {
        code.getstatic(p(Py.class), "Ellipsis", ci(PyObject.class));
        return null;
    }

    @Override
    public Object visitSlice(Slice node) throws Exception {
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
        int step = storeTop();

        code.new_(p(PySlice.class));
        code.dup();
        code.dup2_x2();
        code.pop2();

        code.aload(step);
        code.freeLocal(step);

        code.invokespecial(p(PySlice.class), "<init>",
                sig(Void.TYPE, PyObject.class, PyObject.class, PyObject.class));
        return null;
    }

    @Override
    public Object visitClassDef(ClassDef node) throws Exception {
        ScopeInfo scope = module.getScopeInfo(node);
        String name = getName(node.getInternalName());
        setline(node);
        code.ldc(name);
        code.ldc(scope.qualname);

        loadArray(code, node.getInternalBases());
        java.util.List<String> keys = new ArrayList<>();
        java.util.List<expr> values = new ArrayList<>();
        java.util.List<keyword> keywords = node.getInternalKeywords();
        if (keywords.size() == 1 && keywords.get(0).getInternalArg() == null) {
            /** when the class closure is created, unwrap the kwarg */
            visit(node.getInternalKeywords().get(0).getInternalValue());
        } else {
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
            loadStrings(code, keys);
            loadArray(code, values);
            code.invokestatic(p(PyDictionary.class), "fromKV",
                    sig(PyDictionary.class, String[].class, PyObject[].class));

            if (kwarg != null) {
                code.dup();
                visit(kwarg);
                code.invokevirtual(p(PyDictionary.class), "update", sig(Void.TYPE, PyObject.class));
            }
        }

        scope.setup_closure();
//        scope.dump();
        // Make code object out of suite

        module.codeConstant(new Suite(node, node.getInternalBody()), name, false, name,
                node.getLine(), scope, cflags).get(code);

        // Make class out of name, bases, and code
        if (!makeClosure(scope)) {
            code.aconst_null();
        }
        Str docStr = getDocStr(node.getInternalBody());
        if (docStr != null) {
            visit(docStr);
        } else {
            code.aconst_null();
        }
        code.invokestatic(p(Py.class), "makeClass",
                sig(PyObject.class, String.class, String.class, PyObject[].class, PyObject.class, PyCode.class,
                        PyObject[].class, PyObject.class));

        applyDecorators(node.getInternalDecorator_list());

        // Assign this new class to the given name
        set(new Name(node, node.getInternalName(), expr_contextType.Store));
        return null;
    }

    @Override
    public Object visitNum(Num node) throws Exception {
        module.constant(node).get(code);
        return null;
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

    void emitGetGlobal(String name) throws Exception {
        code.ldc(name);
        code.invokevirtual(p(PyFrame.class), "getglobal", sig(PyObject.class, String.class));
    }

    @Override
    public Object visitStarred(Starred node) throws Exception {
        visit(node.getInternalValue());
        return null;
    }

    @Override
    public Object visitNameConstant(NameConstant node) throws Exception {
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
    public Object visitName(Name node) throws Exception {
        String name;
        if (fast_locals) {
            name = node.getInternalId();
        } else {
            name = getName(node.getInternalId());
        }

        SymInfo syminf = tbl.get(name);

        expr_contextType ctx = node.getInternalCtx();
        if (ctx == null) {
            System.out.println("oops");
        }

        switch (ctx) {
            case Load:
                loadFrame();
                if (syminf != null) {
                    int flags = syminf.flags;
                    if ((flags & ScopeInfo.GLOBAL) != 0 || optimizeGlobals
                            && (flags & (ScopeInfo.BOUND | ScopeInfo.CELL | ScopeInfo.FREE)) == 0) {
                        emitGetGlobal(name);
                        return null;
                    }
                    if (fast_locals) {
                        if ((flags & ScopeInfo.CELL) != 0) {
                            code.iconst(syminf.env_index);
                            code.invokevirtual(p(PyFrame.class), "getderef",
                                    sig(PyObject.class, Integer.TYPE));
                            return null;
                        }
                        if ((flags & ScopeInfo.BOUND) != 0) {
                            code.iconst(syminf.locals_index);
                            code.invokevirtual(p(PyFrame.class), "getlocal",
                                    sig(PyObject.class, Integer.TYPE));
                            return null;
                        }
                    }
                    if ((flags & ScopeInfo.FREE) != 0 && (flags & ScopeInfo.BOUND) == 0) {
                        code.iconst(syminf.env_index);
                        code.invokevirtual(p(PyFrame.class), "getderef",
                                sig(PyObject.class, Integer.TYPE));
                        return null;
                    }
                }
                code.ldc(name);
                code.invokevirtual(p(PyFrame.class), "getname", sig(PyObject.class, String.class));
                return null;

            case Param:
            case Store:
                loadFrame();
                if (syminf != null && (syminf.flags & ScopeInfo.GLOBAL) != 0) {
                    code.ldc(name);
                    code.aload(temporary);
                    code.invokevirtual(p(PyFrame.class), "setglobal",
                            sig(Void.TYPE, String.class, PyObject.class));
                } else {
                    if (syminf != null && (syminf.flags & (ScopeInfo.CELL | ScopeInfo.FREE)) != 0) {
                        code.iconst(syminf.env_index);
                        code.aload(temporary);
                        code.invokevirtual(p(PyFrame.class), "setderef",
                                sig(Void.TYPE, Integer.TYPE, PyObject.class));
                        return null;
                    }
                    if (!fast_locals) {
                        code.ldc(name);
                        code.aload(temporary);
                        code.invokevirtual(p(PyFrame.class), "setlocal",
                                sig(Void.TYPE, String.class, PyObject.class));
                    } else {
                        if (syminf == null) {
                            throw Py.SyntaxError(node.getToken(), "internal compiler error", module.getFilename());
                        }
                        code.iconst(syminf.locals_index);
                        code.aload(temporary);
                        code.invokevirtual(p(PyFrame.class), "setlocal",
                                sig(Void.TYPE, Integer.TYPE, PyObject.class));
                    }
                }
                return null;
            case Del: {
                loadFrame();
                if (syminf != null && (syminf.flags & ScopeInfo.GLOBAL) != 0) {
                    code.ldc(name);
                    code.invokevirtual(p(PyFrame.class), "delglobal", sig(Void.TYPE, String.class));
                } else {
                    if (!fast_locals) {
                        code.ldc(name);
                        code.invokevirtual(p(PyFrame.class), "dellocal",
                                sig(Void.TYPE, String.class));
                    } else {
                        if (syminf == null) {
                            throw Py.SyntaxError(node.getToken(), "internal compiler error", module.getFilename());
                        }
                        if ((syminf.flags & (ScopeInfo.FREE | ScopeInfo.CELL)) != 0) {
                            code.iconst(syminf.env_index);
                            code.invokevirtual(p(PyFrame.class), "delderef",
                                    sig(Void.TYPE, Integer.TYPE));
                        } else {
                            code.iconst(syminf.locals_index);
                            code.invokevirtual(p(PyFrame.class), "dellocal",
                                    sig(Void.TYPE, Integer.TYPE));
                        }
                    }
                }
                return null;
            }
        }
        return null;
    }

    @Override
    public Object visitJoinedStr(JoinedStr node) throws Exception {
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
    public Object visitFormattedValue(FormattedValue node) throws Exception {
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
    public Object visitBytes(Bytes node) throws Exception {
        module.constant(node).get(code);
        return null;
    }

    @Override
    public Object visitStr(Str node) throws Exception {
        module.constant(node).get(code);
        return null;
    }

    @Override
    protected Object unhandled_node(PythonTree node) throws Exception {
        throw new Exception("Unhandled node " + node);
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
        public boolean bodyDone = false;
        public PythonTree node = null;

        public ExceptionHandler() {
        }

        public ExceptionHandler(PythonTree n) {
            node = n;
        }

        public boolean isFinallyHandler() {
            return node != null;
        }

        public void addExceptionHandlers(Label handlerStart) throws Exception {
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

        public void finalBody(CodeCompiler compiler) throws Exception {
            if (node instanceof Try) {
                compiler.suite(((Try) node).getInternalFinalbody());
            }
        }
    }
}
