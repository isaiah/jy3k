package org.python.compiler;

import org.python.antlr.Visitor;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Interactive;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Return;
import org.python.antlr.ast.Starred;
import org.python.antlr.ast.Suite;
import org.python.antlr.ast.arg;
import org.python.antlr.ast.arguments;
import org.python.antlr.ast.expr_contextType;
import org.python.antlr.ast.keyword;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;
import org.python.core.PyList;
import org.python.core.PyObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Create implicit __class__ closure reference if any methods in a class body refer to either __class__ or super.
 * see PEP-3135 New Super
 */
public class ClassClosureGenerator extends Visitor {
    private Deque<Boolean> inClass = new LinkedList<>();
    private Deque<Boolean> needsClosure = new LinkedList<>();

    private List<stmt> filterClassDef(List<stmt> stmts) throws Exception {
        List<stmt> replacement = null;
        for (int i = 0; i < stmts.size(); i++) {
            stmt s = stmts.get(i);
            Object o = visit(s);
            if (s instanceof ClassDef) {
                stmt[] ret = (stmt[]) o;
                if (ret != null) {
                    if (replacement == null) {
                        replacement = new ArrayList<>(stmts.size() + 2);
                        replacement.addAll(stmts.subList(0, i));
                    }
                    replacement.add(ret[0]);
                    replacement.add(ret[1]);
                } else if (replacement != null) {
                    replacement.add(s);
                }
            } else if (replacement != null) {
                replacement.add(s);
            }
        }
        return replacement == null ? stmts : replacement;
    }

    @Override
    public Object visitFunctionDef(FunctionDef node) throws Exception {
        List<stmt> stmts = node.getInternalBody();
        node.setInternalBody(filterClassDef(stmts));
        return node;
    }

    @Override
    public Object visitModule(org.python.antlr.ast.Module node) throws Exception {
        List<stmt> stmts = node.getInternalBody();
        node.setInternalBody(filterClassDef(stmts));
        return node;
    }

    @Override
    public Object visitSuite(Suite node) throws Exception {
        List<stmt> stmts = node.getInternalBody();
        node.setInternalBody(filterClassDef(stmts));
        return node;
    }

    @Override
    public Object visitInteractive(Interactive node) throws Exception {
        List<stmt> stmts = node.getInternalBody();
        node.setInternalBody(filterClassDef(stmts));
        return node;
    }

    @Override
    public Object visitClassDef(ClassDef node) throws Exception {
        inClass.push(true);
        needsClosure.push(false);
        node.setInternalBody(filterClassDef(node.getInternalBody()));
        inClass.pop();
        boolean thisNeedClosure = needsClosure.pop();
        if (thisNeedClosure) {
            List<expr> bases = node.getInternalBases();
            List<keyword> keywords = node.getInternalKeywords();
            java.util.List<stmt> bod = new ArrayList<>();
            String name = node.getInternalName();
            String vararg = "__args__";
            String kwarg = "__kw__";
            // replace inner class parameters
            Starred starred = new Starred(node.getToken(), new Name(node.getToken(), vararg, expr_contextType.Load), expr_contextType.Load);
            node.setBases(new PyList(new PyObject[]{starred}));
            keyword kw = new keyword(node.getToken(), null, new Name(node.getToken(), kwarg, expr_contextType.Load));
            node.setKeywords(new PyList(new PyObject[]{kw}));
            bod.add(node);

            Name innerName = new Name(node, name, expr_contextType.Load);
            String funcName = "__$" + name;
            Name outerName = new Name(node, funcName, expr_contextType.Load);
            Assign assign = new Assign(node, Arrays.asList(new Name(node, "__class__", expr_contextType.Store)),
                    innerName);
            bod.add(assign);
            Return _ret = new Return(node.getToken(), innerName);
            bod.add(_ret);
            arguments args = new arguments(node, new ArrayList<>(),
                    new arg(node, vararg, null), new ArrayList<>(), new ArrayList<>(),
                    new arg(node, kwarg, null), new ArrayList<>());
            FunctionDef funcdef = new FunctionDef(node.getToken(), funcName, args, bod, new ArrayList<>(), null);
            assign = new Assign(node, Arrays.asList(new Name(node, name, expr_contextType.Store)),
                    new Call(node, outerName, bases, keywords));
            return new stmt[] { funcdef, assign };
        }
        return null;
    }

    @Override
    public Object visitName(Name node) throws Exception {
        if (!inClass.isEmpty() && inClass.peek() && !needsClosure.peek()) {
            String name = node.getInternalId();
            if (name.equals("__class__") || name.equals("super")) {
                needsClosure.pop();
                needsClosure.push(true);
            }
        }
        return node;
    }
}
