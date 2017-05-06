package org.python.compiler;

import org.python.antlr.Visitor;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.Block;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.Delete;
import org.python.antlr.ast.ExceptHandler;
import org.python.antlr.ast.Expr;
import org.python.antlr.ast.For;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.GeneratorExp;
import org.python.antlr.ast.If;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.NameConstant;
import org.python.antlr.ast.Num;
import org.python.antlr.ast.Raise;
import org.python.antlr.ast.Return;
import org.python.antlr.ast.Try;
import org.python.antlr.ast.Yield;
import org.python.antlr.ast.arg;
import org.python.antlr.ast.arguments;
import org.python.antlr.ast.comprehension;
import org.python.antlr.ast.expr_contextType;
import org.python.antlr.base.excepthandler;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.python.compiler.CompilerConstants.GEN;
import static org.python.compiler.CompilerConstants.ITER;
import static org.python.compiler.CompilerConstants.RETURN;

/**
 * Lower to more primitive operations. After lowering several nodes have been turned into more low level constructs
 * and control flow termination criteria have been computed.
 *
 * Such as:
 * code copying/inlining of finallies
 * lower for loop
 * lower comprehensions
 * lower generator expression
 * lower with statement
 */
public class Lower extends Visitor {

    /**
     * lower generator expression PEP-0289
     * g = (x for x in range(10))
     *
     * becomes
     *
     * def __gen(exp):
     *   for x in exp:
     *   yield x**2
     * g = __gen(iter(range(10)))
     *
     * @param node
     * @return
     * @throws Exception
     */
    @Override
    public Object visitAssign(Assign node) throws Exception {
        if (node.getInternalValue() instanceof GeneratorExp) {
            lowerGeneratorExp(node);
        } else {
            super.visitAssign(node);
        }
        return node;
    }

    private void lowerGeneratorExp(Assign assignNode) {
        GeneratorExp node = (GeneratorExp) assignNode.getInternalValue();
        String bound_exp = "_(x)";
        stmt n = new Expr(node, new Yield(node, node.getInternalElt()));

        expr iter = null;
        for (int i = node.getInternalGenerators().size() - 1; i >= 0; i--) {
            comprehension comp = node.getInternalGenerators().get(i);
            for (int j = comp.getInternalIfs().size() - 1; j >= 0; j--) {
                java.util.List<stmt> bod = new ArrayList<>(1);
                bod.add(n);
                n = new If(comp.getInternalIfs().get(j), comp.getInternalIfs().get(j), bod,
                        new ArrayList<>());
            }
            List<stmt> bod = new ArrayList<>(1);
            bod.add(n);
            if (i == 0) {
                n = new For(comp, comp.getInternalTarget(), new Name(node, bound_exp,
                        expr_contextType.Load), bod, new ArrayList<>());
                iter = comp.getInternalIter();
                continue;
            }
            n = new For(comp, comp.getInternalTarget(), comp.getInternalIter(), bod,
                    new ArrayList<>());
        }

        java.util.List<stmt> bod = new ArrayList<>(1);
        bod.add(n);
        arg arg = new arg(node.getToken(), bound_exp, null);
        arguments args = new arguments(node.getToken(), asList(arg), null, null, null, null, null);
        FunctionDef gen = new FunctionDef(node.getToken(), GEN.symbolName(), args, bod, null, null);
        Name genfunc = new Name(node.getToken(), gen.getInternalName(), expr_contextType.Load);
        Call iterCall = new Call(node.getToken(), new Name(node.getToken(), ITER.symbolName(), expr_contextType.Load), asList(iter), null);
        Call genfuncCall = new Call(node.getToken(), genfunc, asList(iterCall), null);
        assignNode.setInternalValue(genfuncCall);
        genfunc = genfunc.copy();
        genfunc.setContext(expr_contextType.Del);
        Delete delGenfunc = new Delete(node.getToken(), asList(genfunc));
        assignNode.replaceSelf(gen, assignNode.copy(), delGenfunc);
    }

    @Override
    public Object visitTry(Try node) throws Exception {
        final List<stmt> finalBody = node.getInternalFinalbody();
        if (finalBody == null || finalBody.isEmpty()) {
            return super.visitTry(node);
        }
        node.setInternalFinalbody(null);

        excepthandler catchAll = catchAllBlock(finalBody.get(0), finalBody);
        List<excepthandler> excepthandlers = node.getInternalHandlers();
        // when there is no except clause
        node.accept(new Visitor() {
            // skip function definition
            @Override
            public Object visitFunctionDef(FunctionDef node) {
                return node;
            }

            @Override
            public Object visitTry(Try node) throws Exception {
                return Lower.this.visitTry(node);
            }

            @Override
            public Object visitReturn(Return node) {
                expr value = node.getInternalValue();
                // no return expression, or returns a primitive literal
                if (value == null || value instanceof Name || value instanceof Num || value instanceof NameConstant) {
                    node.replaceSelf(prependFinalBody(finalBody, node.copy()));
                } else {
                    Name resultNode = new Name(node.getToken(), RETURN.symbolName(), expr_contextType.Store);
                    List<stmt> newStmts = new ArrayList<>(finalBody.size() + 2);
                    Assign assign = new Assign(value.getToken(), asList(resultNode), value);
                    newStmts.add(assign);
                    newStmts.addAll(finalBody);
                    resultNode = resultNode.copy();
                    resultNode.setContext(expr_contextType.Load);
                    newStmts.add(new Return(node.getToken(), resultNode));
                    node.replaceSelf(newStmts);
                }
                return node;
            }
        });
        Try newTryNode;
        if (excepthandlers == null || excepthandlers.isEmpty()) {
            Block newBody = new Block(node.getToken(), node.getInternalBody());
            newTryNode = new Try(node.getToken(), appendFinalBody(finalBody, newBody), asList(catchAll), null, null);
        } else {
            newTryNode = new Try(node.getToken(), appendFinalBody(finalBody, node.copy()), asList(catchAll), null, null);
        }
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

    private excepthandler catchAllBlock(stmt node, List<stmt> body) {
        Raise raiseNode = new Raise(node.getToken(), null, null);
        return new ExceptHandler(node.getToken(), null, null, prependFinalBody(body, raiseNode));
    }
}
