package org.python.compiler;

import org.python.antlr.Visitor;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.Block;
import org.python.antlr.ast.ExceptHandler;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.NameConstant;
import org.python.antlr.ast.Num;
import org.python.antlr.ast.Raise;
import org.python.antlr.ast.Return;
import org.python.antlr.ast.Try;
import org.python.antlr.ast.expr_contextType;
import org.python.antlr.base.excepthandler;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;

import java.util.ArrayList;
import java.util.List;

import static org.python.compiler.CompilerConstants.RETURN;
import static java.util.Arrays.asList;

/**
 * Lower to more primitive operations. After lowering several nodes have been turned into more low level constructs
 * and control flow termination criteria have been computed.
 *
 * Such as:
 * code copying/inlining of finallies
 * lower for loop
 * lower comprehensions
 * lower generator expression
 */
public class Lower extends Visitor {

    @Override
    public Object visitTry(Try node) throws Exception {
        final List<stmt> finalBody = node.getInternalFinalbody();
        if (finalBody == null || finalBody.isEmpty()) {
            return super.visitTry(node);
        }
        node.setInternalFinalbody(null);

        excepthandler catchAll = catchAllBlock(node, finalBody);
        List<excepthandler> excepthandlers = node.getInternalHandlers();
        // when there is no except clause
        if (excepthandlers == null || excepthandlers.isEmpty()) {
            node.setInternalHandlers(asList(catchAll));
            return null;
        }
        node.accept(new Visitor() {
            // skip function definition
            @Override
            public Object visitFunctionDef(FunctionDef node) {
                return node;
            }
            @Override
            public Object visitReturn(Return node) {
                expr value = node.getInternalValue();
                // no return expression, or returns a primitive literal
                if (value == null || value instanceof Name || value instanceof Num || value instanceof NameConstant) {
                    node.replaceSelf(new Block(node.getToken(), prependFinalBody(finalBody, node)));
                } else {
                    Name resultNode = new Name(node.getToken(), RETURN.symbolName(), expr_contextType.Store);
                    List<stmt> newStmts = new ArrayList<>(finalBody.size() + 2);
                    Assign assign = new Assign(value.getToken(), asList(resultNode), value);
                    newStmts.add(assign);
                    newStmts.addAll(finalBody);
                    newStmts.add(new Return(node.getToken(), resultNode));
                    node.replaceSelf(new Block(node.getToken(), newStmts));
                }
                return node;
            }
        });
        Try newTryNode = new Try(node.getToken(), appendFinalBody(finalBody, node), asList(catchAll), null, null);
        node.replaceSelf(newTryNode);
        return null;
    }

    private static List<stmt> appendFinalBody(List<stmt> finalBody, stmt statement) {
        List<stmt> stmts = new ArrayList<>(finalBody.size() + 1);
        stmts.add(statement);
        stmts.addAll(finalBody);
        return stmts;
    }

    private static List<stmt> prependFinalBody(List<stmt> finalBody, stmt statement) {
        List<stmt> stmts = new ArrayList<>(finalBody.size() + 1);
        stmts.addAll(finalBody);
        stmts.add(statement);
        return stmts;
    }

    private excepthandler catchAllBlock(Try node, List<stmt> body) {
        Raise raiseNode = new Raise(node.getToken(), null, null);
        return new ExceptHandler(node.getToken(), null, null, prependFinalBody(body, raiseNode));
    }
}
