package org.python.compiler;

import org.python.antlr.Visitor;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.AugAssign;
import org.python.antlr.ast.BinOp;
import org.python.antlr.ast.Block;
import org.python.antlr.ast.Context;
import org.python.antlr.ast.ExceptHandler;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.NameConstant;
import org.python.antlr.ast.Num;
import org.python.antlr.ast.Raise;
import org.python.antlr.ast.Return;
import org.python.antlr.ast.Str;
import org.python.antlr.ast.Try;
import org.python.antlr.ast.expr_contextType;
import org.python.antlr.base.excepthandler;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.python.compiler.CompilerConstants.RETURN;

/**
 * Lower to more primitive operations. After lowering several nodes have been turned into more low level constructs
 * and control flow termination criteria have been computed.
 *
 * Such as:
 * code copying/inlining of finallies
 * expand augassign to normal assign + binop
 * lower for loop
 */
public class Lower extends Visitor {
     /**
      * lower list comprehension PEP-0289
      * [x for x in range(10)]
      *
      * becomes
      *
      * _tmp = []
      * append = _tmp.append
      * for x in range(10):
      *   append(x)
      * del append
      * # do something with _tmp
      * del _tmp
      *
      * @param node
      * @return
      * @throws Exception
      */
//    @Override
//    public Object visitListComp(ListComp node) {
//        Name tmp = new Name(node.getToken(), "__(tmp)" + count++, expr_contextType.Store);
//        Assign initTmp = new Assign(node.getToken(), asList(tmp), new org.python.antlr.ast.List(node.getToken(), null, expr_contextType.Load));
//        Name append = new Name(node.getToken(), "__append" + count++, expr_contextType.Store);
//        tmp = tmp.copy();
//        tmp.setContext(expr_contextType.Load);
//        expr getAppend = new Attribute(node.getToken(), tmp, "append", expr_contextType.Load);
//        Assign assignAppend = new Assign(node, asList(append), getAppend);
//        append = append.copy();
//        append.setContext(expr_contextType.Load);
//        stmt n = new Expr(node, new Call(node, append, asList(node.getInternalElt()), null));
//        for (int i = 0; i < node.getInternalGenerators().size(); i++) {
//            comprehension comp = node.getInternalGenerators().get(i);
//            for (int j = comp.getInternalIfs().size() - 1; j >= 0; j--) {
//                java.util.List<stmt> bod = new ArrayList<>(1);
//                bod.add(n);
//                n = new If(comp.getInternalIfs().get(j), comp.getInternalIfs().get(j), bod,
//                        new ArrayList<>());
//            }
//            List<stmt> bod = new ArrayList<>(1);
//            bod.add(n);
//            n = new For(comp, comp.getInternalTarget(), comp.getInternalIter(), bod,
//                    new ArrayList<>());
//        }
//
//        append = append.copy();
//        append.setContext(expr_contextType.Del);
//        Delete del = new Delete(node, asList(append));
//        PythonTree parent = node.getParent();
//        parent.replaceField(node, tmp);
//        tmp = tmp.copy();
//        tmp.setContext(expr_contextType.Del);
//        Delete delTmp = new Delete(node, asList(tmp));
//        while(!(parent instanceof stmt)) {
//            parent = parent.getParent();
//        }
//        parent.replaceSelf(initTmp, assignAppend, n, del, delTmp);
//        return node;
//    }
//
//    @Override
//    public Object visitDictComp(DictComp node) {
//        Name tmp = new Name(node.getToken(), "__(tmp)" + count++, expr_contextType.Store);
//        Assign initTmp = new Assign(node.getToken(), asList(tmp), new org.python.antlr.ast.Dict(node.getToken(), null, null));
//        tmp = tmp.copy();
//        tmp.setContext(expr_contextType.Load);
//        Subscript sub = new Subscript(node, tmp, new Index(node, node.getInternalKey()), expr_contextType.Store);
//        stmt n = new Assign(node, asList(sub), node.getInternalValue());
//        for (int i = 0; i < node.getInternalGenerators().size(); i++) {
//            comprehension comp = node.getInternalGenerators().get(i);
//            for (int j = comp.getInternalIfs().size() - 1; j >= 0; j--) {
//                java.util.List<stmt> bod = new ArrayList<>(1);
//                bod.add(n);
//                n = new If(comp.getInternalIfs().get(j), comp.getInternalIfs().get(j), bod,
//                        new ArrayList<>());
//            }
//            List<stmt> bod = new ArrayList<>(1);
//            bod.add(n);
//            n = new For(comp, comp.getInternalTarget(), comp.getInternalIter(), bod,
//                    new ArrayList<>());
//        }
//
//        PythonTree parent = node.getParent();
//        parent.replaceField(node, tmp);
//        tmp = tmp.copy();
//        tmp.setContext(expr_contextType.Del);
//        Delete delTmp = new Delete(node, asList(tmp));
//        while(!(parent instanceof stmt)) {
//            parent = parent.getParent();
//        }
//        parent.replaceSelf(initTmp, n, delTmp);
//        return node;
//    }

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

//    @Override
//    public Object visitGeneratorExp(GeneratorExp node) {
//        String bound_exp = "_(exp)";
//        stmt n = new Expr(node, new Yield(node, node.getInternalElt()));
//
//        expr iter = null;
//        for (int i = 0; i < node.getInternalGenerators().size(); i++) {
//            comprehension comp = node.getInternalGenerators().get(i);
//            for (int j = comp.getInternalIfs().size() - 1; j >= 0; j--) {
//                java.util.List<stmt> bod = new ArrayList<>(1);
//                bod.add(n);
//                n = new If(comp.getInternalIfs().get(j), comp.getInternalIfs().get(j), bod,
//                        new ArrayList<>());
//            }
//            List<stmt> bod = new ArrayList<>(1);
//            bod.add(n);
//            if (i == node.getInternalGenerators().size() - 1) {
//                n = new For(comp, comp.getInternalTarget(), new Name(node, bound_exp,
//                        expr_contextType.Load), bod, new ArrayList<>());
//                iter = comp.getInternalIter();
//                continue;
//            }
//            n = new For(comp, comp.getInternalTarget(), comp.getInternalIter(), bod,
//                    new ArrayList<>());
//        }
//
//        java.util.List<stmt> bod = new ArrayList<>(1);
//        bod.add(n);
//        arg arg = new arg(node.getToken(), bound_exp, null);
//        arguments args = new arguments(node.getToken(), asList(arg), null, null, null, null, null);
//        FunctionDef gen = new FunctionDef(node.getToken(), GEN.symbolName() + count++, args, bod, null, null);
//        Name genfunc = new Name(node.getToken(), gen.getInternalName(), expr_contextType.Load);
//        Call iterCall = new Call(node.getToken(), new Name(node.getToken(), ITER.symbolName(), expr_contextType.Load), asList(iter), null);
//        Call genfuncCall = new Call(node.getToken(), genfunc, asList(iterCall), null);
//        PythonTree parent = node.getParent();
//
//        genfunc = genfunc.copy();
//        genfunc.setContext(expr_contextType.Del);
//        Delete delGenfunc = new Delete(node.getToken(), asList(genfunc));
//        // replace genexp with call
//        parent.replaceField(node, genfuncCall);
//        // Find the statement that hold the parent
//        while(!(parent instanceof stmt)) {
//            parent = parent.getParent();
//        }
//        // insert function definition and del
//        parent.replaceSelf(gen, ((stmt) parent).copy(), delGenfunc);
//        return node;
//    }

    @Override
    public Object visitAugAssign(AugAssign node) {
        expr right = node.getInternalTarget().copy();
        ((Context) right).setContext(expr_contextType.Load);
        BinOp binOp = new BinOp(node, node.getInternalValue(), node.getInternalOp(), right);
        expr target = node.getInternalTarget().copy();
        ((Context) target).setContext(expr_contextType.Store);
        Assign ret = new Assign(node, asList(target), binOp);
        node.replaceSelf(ret);
        return node;
    }

    @Override
    public Object visitTry(Try node) throws Exception {
        final List<stmt> finalBody = node.getInternalFinalbody();
        if (finalBody == null || finalBody.isEmpty()) {
            return super.visitTry(node);
        }
        final Block finalBlock = new Block(node.getToken(), finalBody);
        node.setInternalFinalbody(null);

        Visitor tryVisitor = new Visitor() {
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
            public Object visitAugAssign(AugAssign node) {
                return Lower.this.visitAugAssign(node);
            }

            @Override
            public Object visitReturn(Return node) {
                expr value = node.getInternalValue();
                // no return expression, or returns a primitive literal
                if (value == null || value instanceof Num || value instanceof Str || value instanceof NameConstant) {
                    node.replaceSelf(asList(finalBlock, node.copy()));
                } else {
                    Name resultNode = new Name(node.getToken(), RETURN.symbolName(), expr_contextType.Store);
                    Assign assign = new Assign(value.getToken(), asList(resultNode), value);
                    resultNode = resultNode.copy();
                    resultNode.setContext(expr_contextType.Load);
                    node.replaceSelf(assign, finalBlock, new Return(node.getToken(), resultNode));
                }
                return node;
            }
        };
        traverse(finalBlock);
        tryVisitor.traverse(node);

        excepthandler catchAll = catchAllBlock(finalBody.get(0), finalBlock);
        List<excepthandler> excepthandlers = node.getInternalHandlers();
        Try newTryNode;
        // when there is no except clause
        if (excepthandlers == null || excepthandlers.isEmpty()) {
            Block newBody = new Block(node.getToken(), node.getInternalBody());
            newTryNode = new Try(node.getToken(), asList(finalBlock, newBody), asList(catchAll), null, null);
        } else {
            newTryNode = new Try(node.getToken(), asList(finalBlock, node.copy()), asList(catchAll), null, null);
        }
        node.replaceSelf(newTryNode);
        return null;
    }

    private excepthandler catchAllBlock(stmt node, Block body) {
        Raise raiseNode = new Raise(node.getToken(), null, null);
        return new ExceptHandler(node.getToken(), null, null, asList(body, raiseNode));
    }
}
