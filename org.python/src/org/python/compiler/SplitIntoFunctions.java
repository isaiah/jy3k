package org.python.compiler;

import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.Expr;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Return;
import org.python.antlr.ast.SplitNode;
import org.python.antlr.ast.expr_contextType;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A visitor that replaces {@link SplitNode} into nested function and invocations
 */
public class SplitIntoFunctions extends Visitor {
    private Map<PythonTree, PySTEntryObject> nodeScopes;

    public SplitIntoFunctions(Map<PythonTree, PySTEntryObject> scopeInfoMap) {
        this.nodeScopes = scopeInfoMap;
    }

    @Override
    public Object visitFunctionDef(FunctionDef node) {
        if (!node.isSplit()) {
            return node;
        }
        List<stmt> newBody = new ArrayList<>();
        for (stmt statement : node.getInternalBody()) {
            if (statement instanceof SplitNode) {
                newBody.addAll(visitSplitNode((SplitNode) statement));
            } else {
                newBody.add(statement);
            }
        }
        node.setInternalBody(newBody);
        return node;
    }

    public List<stmt> visitSplitNode(SplitNode node) {
        FunctionDef func = createFunctionDef(node);
        expr call = createCall(func);
        return Arrays.asList(func, new Expr(call));
    }

    private expr createCall(FunctionDef func) {
        return new Call(func.getToken(), new Name(func, func.getInternalName(), expr_contextType.Load), null, null);
    }

    private Return createReturn(expr call) {
        return new Return(call);
    }

    private FunctionDef createFunctionDef(SplitNode node) {
        FunctionDef func = new FunctionDef(node.getToken(), node.getInternalName(), null, node.getInternalBody(), null, null, null);
        nodeScopes.put(func, nodeScopes.remove(node));
        return func;
    }

    private boolean isTerminal(SplitNode node) {
        return true;
    }
}
