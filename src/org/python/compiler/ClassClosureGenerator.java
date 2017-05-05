package org.python.compiler;

import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.Block;
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

    @Override
    public Object visitClassDef(ClassDef node) throws Exception {
        inClass.push(true);
        needsClosure.push(false);
        traverse(node);
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
            ClassDef copy = PythonTree.copy(node);
            bod.add(copy);

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
            node.replaceSelf(new Block(node.getToken(), Arrays.asList(funcdef, assign)));
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
