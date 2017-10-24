package org.python.compiler;

import org.python.antlr.Visitor;
import org.python.antlr.ast.AnonymousFunction;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.AsyncFor;
import org.python.antlr.ast.AsyncFunctionDef;
import org.python.antlr.ast.AsyncWith;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.AugAssign;
import org.python.antlr.ast.Await;
import org.python.antlr.ast.BinOp;
import org.python.antlr.ast.Block;
import org.python.antlr.ast.Break;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.Context;
import org.python.antlr.ast.Continue;
import org.python.antlr.ast.DictComp;
import org.python.antlr.ast.ExceptHandler;
import org.python.antlr.ast.ExitFor;
import org.python.antlr.ast.Expr;
import org.python.antlr.ast.For;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.GeneratorExp;
import org.python.antlr.ast.If;
import org.python.antlr.ast.Lambda;
import org.python.antlr.ast.ListComp;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.NameConstant;
import org.python.antlr.ast.Num;
import org.python.antlr.ast.Raise;
import org.python.antlr.ast.Return;
import org.python.antlr.ast.SetComp;
import org.python.antlr.ast.Str;
import org.python.antlr.ast.Try;
import org.python.antlr.ast.UnaryOp;
import org.python.antlr.ast.While;
import org.python.antlr.ast.With;
import org.python.antlr.ast.Yield;
import org.python.antlr.ast.arg;
import org.python.antlr.ast.arguments;
import org.python.antlr.ast.comprehension;
import org.python.antlr.ast.expr_contextType;
import org.python.antlr.ast.unaryopType;
import org.python.antlr.ast.withitem;
import org.python.antlr.base.excepthandler;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.python.compiler.CompilerConstants.EXCINFO;
import static org.python.compiler.CompilerConstants.RETURN;

/**
 * Lower to more primitive operations. After lowering several nodes have been turned into more low level constructs
 * and control flow termination criteria have been computed.
 *
 * Such as:
 * code copying/inlining of finallies
 * expand augassign to normal assign + binop
 * lower comprehensions: convert to anonymous function call
 */
public class Lower extends Visitor {
    private int counter;

    /**
     * Desugar async for, see PEP-492
     *
     *  async for TARGET in ITER:
     *      BLOCK
     *  else:
     *      BLOCK2
     *
     * which is semantically equivalent to:
     *
     *  iter = (ITER)
     *  iter = type(iter).__aiter__(iter)
     *  running = True
     *  while running:
     *      try:
     *          TARGET = await type(iter).__anext__(iter)
     *      except StopAsyncIteration:
     *          running = False
     *      else:
     *          BLOCK
     *  else:
     *     BLOCK2
     * @param node
     * @return
     */
    @Override
    public Object visitAsyncFor(AsyncFor node) {
        traverse(node);
        String tmp = "(tmp)" + counter++;
        Name storeTmp = new Name(node, tmp, expr_contextType.Store);
        Attribute iter = new Attribute(node, node.getInternalIter(), "__aiter__", expr_contextType.Load);
        Call callIter = new Call(node, iter, null, null);
        Assign setTmp = new Assign(node, Arrays.asList(storeTmp), callIter);
        Name loadTmp = new Name(node, tmp, expr_contextType.Load);
        Attribute next = new Attribute(node, loadTmp, "__anext__", expr_contextType.Load);
        Call callNext = new Call(node, next, null, null);
        Await await = new Await(node, callNext);
        Assign setElt = new Assign(node, Arrays.asList(node.getInternalTarget()), await);
        stmt _breakFor = new ExitFor(node);
        excepthandler handler = new ExceptHandler(node, new Name(node, "StopAsyncIteration", expr_contextType.Load),
                null, Arrays.asList(_breakFor));
        Try tryNode = new Try(node, Arrays.asList(setElt), Arrays.asList(handler),
                node.getInternalBody(), null);
        While loop = new While(node, new Name(node, "True", expr_contextType.Load),
                Arrays.asList(tryNode), node.getInternalOrelse());
        node.replaceSelf(setTmp, loop);
        return node;
    }

    /** Convert for loop to a infinite loop
     * for TARGET in ITER:
     *     BLOCK
     * else:
     *     BLOCK2
     *
     * turns into
     *
     * it = y.__iter__()
     * running = True
     * while running:
     *     try:
     *         TARGET = next(it)
     *     except StopIteration:
     *         running = False
     *     else:
     *         BLOCK
     * else:
     *     BLOCK2
     *
     * @param node
     * @return
     */
    @Override
    public Object visitFor(For node) {
        traverse(node);
        String tmp = "(tmp)" + counter++;
        Name storeTmp = new Name(node, tmp, expr_contextType.Store);
        Attribute iter = new Attribute(node, node.getInternalIter(), "__iter__", expr_contextType.Load);
        Call callIter = new Call(node, iter, null, null);
        Assign setTmp = new Assign(node, Arrays.asList(storeTmp), callIter);
        Name loadTmp = new Name(node, tmp, expr_contextType.Load);
        Attribute next = new Attribute(node, loadTmp, "__next__", expr_contextType.Load);
        Call callNext = new Call(node, next, null, null);
        Assign setElt = new Assign(node, Arrays.asList(node.getInternalTarget()), callNext);
        stmt _breakFor = new ExitFor(node);
        excepthandler handler = new ExceptHandler(node, new Name(node, "StopIteration", expr_contextType.Load),
                null, Arrays.asList(_breakFor));
        Try tryNode = new Try(node, Arrays.asList(setElt), Arrays.asList(handler),
                node.getInternalBody(), null);
        While loop = new While(node, new Name(node, "True", expr_contextType.Load),
                Arrays.asList(tryNode), node.getInternalOrelse());
        node.replaceSelf(setTmp, loop);
        return node;
    }

    /**
     * Convert a with statement to a equivalent try/catch clause
     * https://www.python.org/dev/peps/pep-0343/
     * @param node
     * @return
     */
    @Override
    public Object visitWith(With node) {
        traverse(node);
        stmt enterStmt;
        withitem item = node.getInternalItems().get(0);
        String mgr = "(mgr)" + counter++;
        Name setMgr = new Name(node, mgr, expr_contextType.Store);
        Name getMgr = new Name(node, mgr, expr_contextType.Load);
        Assign assignMgr = new Assign(node, Arrays.asList(setMgr), item.getInternalContext_expr());
        Attribute getEnter = new Attribute(node, getMgr, "__enter__", expr_contextType.Load);
        Call enter = new Call(node, getEnter, null, null);
        if (item.getInternalOptional_vars() == null) {
            enterStmt = new Expr(node, enter);
        } else {
            enterStmt = new Assign(node, Arrays.asList(item.getInternalOptional_vars()), enter);
        }
        Attribute getExit = new Attribute(node, getMgr, "__exit__", expr_contextType.Load);
        expr none = new NameConstant(node, "None");
        Expr _exit = new Expr(node, new Call(node, getExit, Arrays.asList(none, none, none), null));
        // This is a hint for the code generator, equals *sys.exc_info()
        expr excinfo = new NameConstant(node, EXCINFO.symbolName());
        expr _exitWithExcinfo = new Call(node, getExit, Arrays.asList(excinfo), null);
        expr ifTest = new UnaryOp(node, unaryopType.Not, _exitWithExcinfo);
        stmt ifStmt = new If(node, ifTest, Arrays.asList(new Raise(node, null, null)), null);
        excepthandler handler = new ExceptHandler(node, null, null, Arrays.asList(ifStmt));
        Block body = new Block(node, node.getInternalBody());
        Try innerTry = new Try(node, asList(body, _exit), Arrays.asList(handler), null, null);
        Visitor withBodyVisitor = new Visitor() {
            // skip function definition
            @Override
            public Object visitFunctionDef(FunctionDef node) {
                return node;
            }

            @Override
            public Object visitBreak(Break node) {
                node.replaceSelf(_exit, node.copy());
                return node;
            }

            @Override
            public Object visitContinue(Continue node) {
                node.replaceSelf(_exit, node.copy());
                return node;
            }

            @Override
            public Object visitReturn(Return node) {
                expr value = node.getInternalValue();
                // no return expression, or returns a primitive literal
                if (value == null || value instanceof Num || value instanceof Str || value instanceof NameConstant) {
                    node.replaceSelf(_exit, node.copy());
                } else {
                    Name resultNode = new Name(node.getToken(), RETURN.symbolName(), expr_contextType.Store);
                    Assign assign = new Assign(value.getToken(), asList(resultNode), value);
                    resultNode = resultNode.copy();
                    resultNode.setContext(expr_contextType.Load);
                    node.replaceSelf(assign, _exit, new Return(node.getToken(), resultNode));
                }
                return node;
            }
        };
        node.traverse(withBodyVisitor);
        node.replaceSelf(assignMgr, enterStmt, innerTry);
        return node;
    }

    /**
     * async with EXPR as VAR:
     *     BLOCK
     *
     * which is semantically equivalent to:
     *
     * mgr = (EXPR)
     * aexit = type(mgr).__aexit__
     * aenter = type(mgr).__aenter__(mgr)
     * exc = True

     * VAR = await aenter
     * try:
     *     BLOCK
     * except:
     *     if not await aexit(mgr, *sys.exc_info()):
     *         raise
     * else:
     *     await aexit(mgr, None, None, None)
     *
     * @param node
     * @return
     */
    @Override
    public Object visitAsyncWith(AsyncWith node) {
        traverse(node);
        stmt enterStmt;
        withitem item = node.getInternalItems().get(0);
        String mgr = "(mgr)" + counter++;
        Name setMgr = new Name(node, mgr, expr_contextType.Store);
        Name getMgr = new Name(node, mgr, expr_contextType.Load);
        Assign assignMgr = new Assign(node, Arrays.asList(setMgr), item.getInternalContext_expr());
        Attribute getEnter = new Attribute(node, getMgr, "__aenter__", expr_contextType.Load);
        expr enter = new Await(node, new Call(node, getEnter, null, null));
        if (item.getInternalOptional_vars() == null) {
            enterStmt = new Expr(node, enter);
        } else {
            enterStmt = new Assign(node, Arrays.asList(item.getInternalOptional_vars()), enter);
        }
        Attribute getExit = new Attribute(node, getMgr, "__aexit__", expr_contextType.Load);
        expr none = new NameConstant(node, "None");
        expr aexit = new Await(node, new Call(node, getExit, Arrays.asList(none, none, none), null));
        Expr _exit = new Expr(node, aexit);
        // This is a hint for the code generator, equals *sys.exc_info()
        expr excinfo = new NameConstant(node, EXCINFO.symbolName());
        expr _exitWithExcinfo = new Await(node, new Call(node, getExit, Arrays.asList(excinfo), null));
        expr ifTest = new UnaryOp(node, unaryopType.Not, _exitWithExcinfo);
        stmt ifStmt = new If(node, ifTest, Arrays.asList(new Raise(node, null, null)), null);
        excepthandler handler = new ExceptHandler(node, null, null, Arrays.asList(ifStmt));
        Block body = new Block(node, node.getInternalBody());
        Try innerTry = new Try(node, asList(body, _exit), Arrays.asList(handler), null, null);
        Visitor withBodyVisitor = new Visitor() {
            // skip function definition
            @Override
            public Object visitFunctionDef(FunctionDef node) {
                return node;
            }

            @Override
            public Object visitBreak(Break node) {
                node.replaceSelf(_exit, node.copy());
                return node;
            }

            @Override
            public Object visitContinue(Continue node) {
                node.replaceSelf(_exit, node.copy());
                return node;
            }

            @Override
            public Object visitReturn(Return node) {
                expr value = node.getInternalValue();
                // no return expression, or returns a primitive literal
                if (value == null || value instanceof Num || value instanceof Str || value instanceof NameConstant) {
                    node.replaceSelf(_exit, node.copy());
                } else {
                    Name resultNode = new Name(node.getToken(), RETURN.symbolName(), expr_contextType.Store);
                    Assign assign = new Assign(value.getToken(), asList(resultNode), value);
                    resultNode = resultNode.copy();
                    resultNode.setContext(expr_contextType.Load);
                    node.replaceSelf(assign, _exit, new Return(node.getToken(), resultNode));
                }
                return node;
            }
        };
        node.traverse(withBodyVisitor);
        node.replaceSelf(assignMgr, enterStmt, innerTry);
        return node;
    }

     /**
      * convert list comprehension into an anonymous function call
      * [x for x in range(10)]
      *
      * turns into (pseudo code)
      *
      * lambda (elt):
      *   _tmp = []
      *   for x in elt:
      *     _tmp.append(x)
      *   return _tmp
      * (iter(range(10)) // invoke lambda
      *
      * @param node
      * @return
      */
    @Override
    public Object visitListComp(ListComp node) {
//        traverse(node);
        org.python.antlr.ast.List emptyList = new org.python.antlr.ast.List(node, null, expr_contextType.Load);
        return visitComp(emptyList, "<listcomp>","append", node, node.getInternalGenerators(), node.getInternalElt());
    }

    @Override
    public Object visitSetComp(SetComp node) {
//        traverse(node);
        org.python.antlr.ast.Set emptySet = new org.python.antlr.ast.Set(node, null);
        return visitComp(emptySet, "<setcomp>","add", node, node.getInternalGenerators(), node.getInternalElt());
    }

    @Override
    public Object visitDictComp(DictComp node) {
//        traverse(node);
        org.python.antlr.ast.Dict emptyDict = new org.python.antlr.ast.Dict(node, null, null);
        return visitComp(emptyDict, "<dictcomp>", "__setitem__", node, node.getInternalGenerators(), node.getInternalKey(), node.getInternalValue());
    }

    @Override
    public Object visitGeneratorExp(GeneratorExp node) {
        traverse(node);
        stmt n = new Expr(node, new Yield(node, node.getInternalElt()));
        expr iter = null;
        List<comprehension> generators = node.getInternalGenerators();

        String elt = "(elt)";
        int last = generators.size() - 1;
        for (int i = 0; i <= last; i++) {
            comprehension comp = generators.get(i);

            for (int j = comp.getInternalIfs().size() - 1; j >= 0; j--) {
                List<stmt> bod = asList(n);
                n = new If(comp.getInternalIfs().get(j), comp.getInternalIfs().get(j), bod, null);
            }
            List<stmt> bod = asList(n);
            if (i == last) {
                iter = comp.getInternalIter();
                n = new For(comp, comp.getInternalTarget(), new Name(node, elt, expr_contextType.Load), bod, null);
            } else {
                n = new For(comp, comp.getInternalTarget(), comp.getInternalIter(), bod, null);
            }
        }

        arg arg = new arg(node, elt, null);
        arguments args = new arguments(node, asList(arg), null, null, null, null, null);
        AnonymousFunction lambda = new AnonymousFunction(node, "<genexp>", args, asList(n));
        // keep this or a new iterator is created for the for loop
        Call getIter = new Call(node, new Name(node, "iter", expr_contextType.Load), asList(iter), null);
        expr result = new Call(node, lambda, asList(getIter), null);

        traverse(result);
        node.replaceSelf(result);
        return node;
    }

    /**
     * Convert a lambda into an anonymous function
     * @param node
     * @return
     */
    @Override
    public Object visitLambda(Lambda node) {
        traverse(node);
        java.util.List<stmt> bod = asList(new Return(node, node.getInternalBody()));
        expr anonymousFunction = new AnonymousFunction(node, "<lambda>", node.getInternalArgs(), bod);
        node.replaceSelf(anonymousFunction);
        return node;
    }

    /**
     * lower generator expression PEP-0289
     * g = (x for x in range(10))
     *
     * becomes
     *
     * g =
     * lambda (exp):
     *   for x in exp:
     *   yield x**2
     * (iter(range(10)))
     *
     * @param node
     * @return
     */

    @Override
    public Object visitAugAssign(AugAssign node) {
        traverse(node);
        expr left = node.getInternalTarget().copy();
        ((Context) left).setContext(expr_contextType.Load);
        BinOp binOp = new BinOp(node, left, node.getInternalOp(), node.getInternalValue());
        binOp.setInplace(true);
        expr target = node.getInternalTarget().copy();
        ((Context) target).setContext(expr_contextType.Store);
        Assign ret = new Assign(node, asList(target), binOp);
        node.replaceSelf(ret);
        return node;
    }

    @Override
    public Object visitTry(Try node) {
        // apply other lowers in this visitor first
        traverse(node);
        final List<stmt> finalBody = node.getInternalFinalbody();
        final stmt finalBlock;
        if (finalBody == null || finalBody.isEmpty()) {
            return super.visitTry(node);
        } else {
            finalBlock = new Block(node.getToken(), finalBody);
        }
        node.setInternalFinalbody(null);

        Visitor tryVisitor = new Visitor() {
            // skip function definition
            @Override
            public Object visitFunctionDef(FunctionDef node) {
                return node;
            }

            @Override
            public Object visitTry(Try node) {
                return Lower.this.visitTry(node);
            }

            @Override
            public Object visitBreak(Break node) {
                node.replaceSelf(finalBlock, node.copy());
                return node;
            }

            @Override
            public Object visitContinue(Continue node) {
                node.replaceSelf(finalBlock, node.copy());
                return node;
            }

            @Override
            public Object visitReturn(Return node) {
                expr value = node.getInternalValue();
                // no return expression, or returns a primitive literal
                if (value == null || value instanceof Num || value instanceof Str || value instanceof NameConstant) {
                    node.replaceSelf(finalBlock, node.copy());
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

        excepthandler catchAll = catchAllBlock(node, finalBlock);
        List<excepthandler> excepthandlers = node.getInternalHandlers();
        Try newTryNode;
        // when there is no except clause
        if (excepthandlers == null || excepthandlers.isEmpty()) {
            Block newBody = new Block(node.getToken(), node.getInternalBody());
            newTryNode = new Try(node.getToken(), asList(newBody, finalBlock), asList(catchAll), null, null);
        } else {
            newTryNode = new Try(node.getToken(), asList(node.copy(), finalBlock), asList(catchAll), null, null);
        }
        node.replaceSelf(newTryNode);
        return null;
    }

    private excepthandler catchAllBlock(stmt node, stmt body) {
        Raise raiseNode = new Raise(node.getToken(), null, null);
        return new ExceptHandler(node.getToken(), null, null, asList(body, raiseNode));
    }

    private Object visitComp(expr initVal, String name, String appendMeth, expr node, List<comprehension> generators, expr... internalElt) {
        String tmp = "(tmp)";
        Name loadTmp = new Name(node, tmp, expr_contextType.Load);
        expr append = new Attribute(node, loadTmp, appendMeth, expr_contextType.Load);
        stmt n = new Expr(node, new Call(node, append, asList(internalElt), null));
        expr iter = null;
        String elt = "(elt)";
        int last = generators.size() - 1;
        for (int i = 0; i <= last; i++) {
            comprehension comp = generators.get(i);

            for (int j = comp.getInternalIfs().size() - 1; j >= 0; j--) {
                List<stmt> bod = asList(n);
                n = new If(comp.getInternalIfs().get(j), comp.getInternalIfs().get(j), bod, null);
            }
            List<stmt> bod = asList(n);
            if (i == last) {
                iter = comp.getInternalIter();
                n = new For(comp, comp.getInternalTarget(), new Name(node, elt, expr_contextType.Load), bod, null);
            } else {
                n = new For(comp, comp.getInternalTarget(), comp.getInternalIter(), bod, null);
            }
        }

        arg arg = new arg(node, elt, null);
        arguments args = new arguments(node, asList(arg), null, null, null, null, null);
        stmt newList = new Assign(node, asList(new Name(node, tmp, expr_contextType.Store)), initVal);
        AnonymousFunction lambda = new AnonymousFunction(node, name, args, asList(newList, n, new Return(node, loadTmp)));

        Call getIter = new Call(node, new Name(node, "iter", expr_contextType.Load), asList(iter), null);
        expr result = new Call(node, lambda, asList(getIter), null);

        traverse(result);
        node.replaceSelf(result);
        return node;
    }
}
