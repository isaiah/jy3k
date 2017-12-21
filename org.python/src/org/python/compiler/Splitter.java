package org.python.compiler;

import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.If;
import org.python.antlr.ast.Return;
import org.python.antlr.ast.SplitNode;
import org.python.antlr.base.stmt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by isaiah on 4/7/17.
 */
public class Splitter extends Visitor {

    private int counter;

    private Map<PythonTree, Long> weightCache;
    private static final int SPLIT_THRESHOLD = 32 * 1024; // * 1024
    private static final String SPLIT_PREFIX = "__split";

    public Splitter() {
        weightCache = new HashMap<>();

    }

    @Override
    public Object visitFunctionDef(FunctionDef node) {
        if (WeighNodes.weigh(node, weightCache) > SPLIT_THRESHOLD) {
            List<stmt> newBody = splitSuite(node, node.getInternalBody());
            node.setInternalBody(newBody);
            node.setSplit(true);
        }
        return node;
    }

    /**
     * Split a function body into sub methods
     * @param body
     * @return
     */
    private List<stmt> splitSuite(final FunctionDef node, final List<stmt> body) {
        List<stmt> splits = new ArrayList<>();
        List<stmt> statements = new ArrayList<>();
        long statementWeight = 0;
        for (stmt statement : body) {
            long weight = WeighNodes.weigh(statement, weightCache);
            if (statementWeight + weight > SPLIT_THRESHOLD || isTerminal(statement)) {
                if (!statements.isEmpty()) {
                    splits.add(createSplitNode(node, statements));
                    statements = new ArrayList<>();
                    statementWeight = 0;
                }
            }
            if (isTerminal(statement)) {
                splits.add(statement);
            } else {
                statements.add(statement);
                statementWeight += weight;
            }
        }
        return splits;
    }

    private SplitNode createSplitNode(FunctionDef node, List<stmt> body) {
        String name = String.format("__%s%d", node.getInternalName(), counter++);
        return new SplitNode(node.getToken(), name, body, null);
    }

    /**
     * Terminal statements are not split into fragments, as they might terminate the execution of the underline function
     * @param node
     * @return
     */
    private boolean isTerminal(PythonTree node) {
        return node instanceof Return  // keep the return statement in the original function
                || node instanceof If; // It's hard to find whether there is a return or break in the if branches
    }
}
