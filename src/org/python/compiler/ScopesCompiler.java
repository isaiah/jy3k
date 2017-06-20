// (C) Copyright 2001 Samuele Pedroni

package org.python.compiler;

import org.python.antlr.ParseException;
import org.python.antlr.Visitor;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.*;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;
import java.util.List;

public class ScopesCompiler extends Visitor implements ScopeConstants {

    private CompilationContext code_compiler;

    private Stack<ScopeInfo> scopes;
    private ScopeInfo cur = null;
    private Map<PythonTree,ScopeInfo> nodeScopes;

    private int level = 0;
    private int func_level = 0;

    public ScopesCompiler(CompilationContext code_compiler, Map<PythonTree,ScopeInfo> nodeScopes) {
        this.code_compiler = code_compiler;
        this.nodeScopes = nodeScopes;
        scopes = new Stack<ScopeInfo>();
    }

    public void beginScope(String name, int kind, PythonTree node,
            ArgListCompiler ac) {
        if (cur != null) {
            scopes.push(cur);
        }
        if (kind == FUNCSCOPE) {
            func_level++;
        }
        String qualname;
        if ((cur != null && cur.isGlobal(name)) || level == 1) {
            qualname = name;
        } else if (level > 1) {
            qualname = cur.qualname;
            if (cur.kind == FUNCSCOPE && kind != CLASSSCOPE) { // ignore the __class__ closure
                 qualname += ".<locals>";
            }
            if (kind == CLASSSCOPE) {
                int dot = qualname.lastIndexOf(".");
                if (dot != -1) {
                    qualname = qualname.substring(0, dot + 1) + name; // remove the __class__ closure
                } else {
                    qualname = name;
                }
            } else {
                qualname += "." + name;
            }
        } else {
            qualname = "";
        }
        cur = new ScopeInfo(name, node, level++, kind, func_level, ac);
        cur.qualname = qualname;
        nodeScopes.put(node, cur);
    }

    public void endScope() throws Exception {
        if (cur.kind == FUNCSCOPE) {
            func_level--;
        }
        level--;
        ScopeInfo up = null;
        if (!scopes.empty()) {
            up = scopes.pop();
        }
        //Go into the stack to find a non class containing scope to use making the closure
        //See PEP 227
        int dist = 1;
        ScopeInfo referenceable = up;
        for (int i = scopes.size() - 1; i >= 0
                && referenceable.kind == CLASSSCOPE;i--,dist++) {
            referenceable = scopes.get(i);
        }

        cur.cook(referenceable, dist, code_compiler);
        cur.dump(); // debug
        cur = up;
    }

    public void parse(PythonTree node) throws Exception {
        visit(node);
    }

    @Override
    public Object visitInteractive(Interactive node) throws Exception {
        beginScope("<single-top>", TOPSCOPE, node, null);
        suite(node.getInternalBody());
        endScope();
        return null;
    }

    @Override
    public Object visitModule(org.python.antlr.ast.Module node)
            throws Exception {
        beginScope("<file-top>", TOPSCOPE, node, null);
        suite(node.getInternalBody());
        endScope();
        return null;
    }

    @Override
    public Object visitExpression(Expression node) throws Exception {
        beginScope("<eval-top>", TOPSCOPE, node, null);
        visit(new Return(node,node.getInternalBody()));
        endScope();
        return null;
    }

    private void def(String name) {
        cur.addBound(name);
    }

    @Override
    public Object visitAsyncFunctionDef(AsyncFunctionDef node) throws Exception {
        String name = node.getInternalName();
        arguments args = node.getInternalArgs();
        List<expr> decs = node.getInternalDecorator_list();
        List<stmt> body = node.getInternalBody();
        expr return_ = node.getInternalReturns();
        return compileFunction(name, args, decs, body, node, return_);
    }

    @Override
    public Object visitFunctionDef(FunctionDef node) throws Exception {
        String name = node.getInternalName();
        arguments args = node.getInternalArgs();
        List<expr> decs = node.getInternalDecorator_list();
        List<stmt> body = node.getInternalBody();
        expr return_ = node.getInternalReturns();
        return compileFunction(name, args, decs, body, node, return_);
    }

    private Object compileFunction(String name, arguments args, List<expr> decs, List<stmt> body, stmt node, expr return_) throws Exception {
        def(name);
        ArgListCompiler ac = new ArgListCompiler();
        ac.visitArgs(args);
        ac.addAnnotation("return", return_);

        List<expr> defaults = ac.getDefaults();
        if (defaults != null) {
            for (int i = 0; i < defaults.size(); i++) {
                visit(defaults.get(i));
            }
        }

        for (int i = decs.size() - 1; i >= 0; i--) {
            visit(decs.get(i));
        }

        beginScope(name, FUNCSCOPE, node, ac);
        if (node instanceof AsyncFunctionDef) {
            cur.async = true;
            cur.defineAsGenerator();
        }
        int n = ac.names.size();
        for (int i = 0; i < n; i++) {
            String curName = ac.names.get(i);
            if (cur.async && curName.equals("await")) {
                throw new ParseException("invalid syntax", node);
            }
            cur.addParam(curName);
        }
        for (int i = 0; i < ac.init_code.size(); i++) {
            visit(ac.init_code.get(i));
        }
        cur.markFromParam();
        suite(body);
        endScope();
        return null;
    }

    @Override
    public Object visitAnonymousFunction(AnonymousFunction node) throws Exception {
        ArgListCompiler ac = new ArgListCompiler();
        ac.visitArgs(node.getInternalArgs());

        List<? extends PythonTree> defaults = ac.getDefaults();
        if (defaults != null) {
            for (int i = 0; i < defaults.size(); i++) {
                visit(defaults.get(i));
            }
        }

        beginScope("<lambda>", FUNCSCOPE, node, ac);
        for (Object o : ac.names) {
            cur.addParam((String) o);
        }
        for (Object o : ac.init_code) {
            visit((stmt) o);
        }
        cur.markFromParam();
        suite(node.getInternalBody());
        endScope();
        return null;
    }

    public Object visitBlock(Block node) throws Exception {
        for (stmt s: node.getInternalBody()) {
            visit(s);
        }
        return null;
    }

    public void suite(List<stmt> stmts) throws Exception {
        if (stmts == null) return;
        for (int i = 0; i < stmts.size(); i++) {
            if (stmts.get(i) != null) {
                visit(stmts.get(i));
            }
        }
    }

    @Override
    public Object visitImport(Import node) throws Exception {
        for (int i = 0; i < node.getInternalNames().size(); i++) {
            if (node.getInternalNames().get(i).getInternalAsname() != null) {
                cur.addBound(node.getInternalNames().get(i).getInternalAsname());
            } else {
                String name = node.getInternalNames().get(i).getInternalName();
                if (name.indexOf('.') > 0) {
                    name = name.substring(0, name.indexOf('.'));
                }
                cur.addBound(name);
            }
        }
        return null;
    }

    @Override
    public Object visitImportFrom(ImportFrom node) throws Exception {
        Future.checkFromFuture(node); // future stmt support
        int n = node.getInternalNames().size();
        if (n == 0) {
            cur.from_import_star = true;
            return null;
        }
        for (int i = 0; i < n; i++) {
            if (node.getInternalNames().get(i).getInternalAsname() != null) {
                cur.addBound(node.getInternalNames().get(i).getInternalAsname());
            } else {
                cur.addBound(node.getInternalNames().get(i).getInternalName());
            }
        }
        return null;
    }

    @Override
    public Object visitGlobal(Global node) throws Exception {
        int n = node.getInternalNames().size();
        for (int i = 0; i < n; i++) {
            String name = node.getInternalNames().get(i);
            int prev = cur.addGlobal(name);
            if (prev >= 0) {
                if ((prev & FROM_PARAM) != 0) {
                    code_compiler.error("name '" + name
                            + "' is local and global", true, node);
                }
                if ((prev & GLOBAL) != 0) {
                    continue;
                }
                if ((prev & DEF_ANNOT) != 0) {
                    code_compiler.error("annotated name '" + name
                            + "' can't be global", true, node);
                }
                String what;
                if ((prev & BOUND) != 0) {
                    what = "assignment";
                } else {
                    what = "use";
                }
                code_compiler.error("name '" + name
                        + "' declared global after " + what, true, node);
            }
        }
        return null;
    }

    @Override
    public Object visitClassDef(ClassDef node) throws Exception {
        List<expr> decs = node.getInternalDecorator_list();
        for (int i = decs.size() - 1; i >= 0; i--) {
            visit(decs.get(i));
        }
        def(node.getInternalName());
        int n = node.getInternalBases().size();
        for (int i = 0; i < n; i++) {
            visit(node.getInternalBases().get(i));
        }
        beginScope(node.getInternalName(), CLASSSCOPE, node, null);
        suite(node.getInternalBody());
        endScope();
        return null;
    }

    @Override
    public Object visitNum(Num num) throws Exception {
        cur.addConst(num);
        return null;
    }

    @Override
    public Object visitStr(Str s) throws Exception {
        cur.addConst(s);
        return null;
    }

    @Override
    public Object visitBytes(Bytes b) throws Exception {
        cur.addConst(b);
        return null;
    }

    @Override
    public Object visitAnnAssign(AnnAssign node) throws Exception {
        if (cur.isClassScope()) {
            // TODO collect the types here, and evaluate them in the class scope
            cur.addUsed("__annotations__");
        }
        String name = node.getInternalTarget().getText();
        int prev = cur.addAnnotated(name);
        if ((prev & GLOBAL) != 0) {
            code_compiler.error("annotated name '" + name + "' can't be global", true, node);
        }
        return super.visitAnnAssign(node);
    }


    @Override
    public Object visitName(Name node) throws Exception {
        String name = node.getInternalId();

        if (cur.async && name.equals("await"))
            throw new ParseException("invalid syntax", node);
        if (node.getInternalCtx() == expr_contextType.Load && name.equals("super")) {
            cur.addUsed("__class__");
        }
        if (node.getInternalCtx() != expr_contextType.Load) {

            if (name.equals("__debug__")) {
                code_compiler.error("can not assign to __debug__", true, node);
            }
            if (isSplit) {
                // temporary function bounds every variable to the enclosing function
                scopes.peek().addBound(name);
                cur.addUsed(name);
            } else {
                cur.addBound(name);
            }
        } else {
            cur.addUsed(name);
        }
        return null;
    }

    @Override
    public Object visitNonlocal(Nonlocal node) throws Exception {
        for (String name : node.getInternalNames()) {
            cur.addNonlocal(name);
        }
        return null;
    }

    @Override
    public Object visitAwait(Await node) throws Exception {
        if (!cur.isFunction()) {
            throw new ParseException("'await' outside function", node);
        } else if (cur.comprehension) {
            throw new ParseException("'await' expressions in comprehensions are not supported", node);
//        } else if (!cur.async) {
//            throw new ParseException("invalid syntax", node);
        }
        traverse(node);
        return null;
    }

    @Override
    public Object visitYieldFrom(YieldFrom node) throws Exception {
        if (cur.async) {
            throw new ParseException("'yield from' inside async function", node);
        }
        cur.defineAsGenerator();
        traverse(node);
        return null;
    }

    @Override
    public Object visitYield(Yield node) throws Exception {
        if (cur.async) {
            cur.async_gen = true;
        }
        cur.defineAsGenerator();
        cur.yield_count++;
        traverse(node);
        return null;
    }

    @Override
    public Object visitReturn(Return node) throws Exception {
        if (node.getInternalValue() != null) {
            cur.noteReturnValue();
        }
        traverse(node);
        return null;
    }

    @Override
    public Object visitAsyncWith(AsyncWith node) throws Exception {
        cur.max_with_count++;
        traverse(node);
        return null;
    }

    @Override
    public Object visitWith(With node) throws Exception {
        cur.max_with_count++;
        traverse(node);

        return null;
    }

    @Override
    public Object visitExceptHandler(ExceptHandler node) throws Exception {
        traverse(node);
        if (node.getInternalName() != null) {
            def(node.getInternalName());
        }
        return null;
    }

    private boolean isSplit;
    @Override
    public Object visitSplitNode(SplitNode node) throws Exception {
        String name = node.getInternalName();
        def(name);
        beginScope(name, FUNCSCOPE, node, new ArgListCompiler());
        isSplit = true;
        suite(node.getInternalBody());
        isSplit = false;
        endScope();
        return null;
    }
}
