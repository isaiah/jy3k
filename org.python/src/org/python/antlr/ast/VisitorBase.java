// Autogenerated AST node
package org.python.antlr.ast;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.python.antlr.AST;
import org.python.antlr.PythonTree;
import org.python.antlr.adapter.AstAdapters;
import org.python.antlr.base.excepthandler;
import org.python.antlr.base.expr;
import org.python.antlr.base.mod;
import org.python.antlr.base.slice;
import org.python.antlr.base.stmt;
import org.python.core.ArgParser;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyUnicode;
import org.python.core.PyStringMap;
import org.python.core.PyType;
import org.python.core.PyList;
import org.python.core.PyNewWrapper;
import org.python.core.Visitproc;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSet;
import org.python.annotations.ExposedType;
import org.python.annotations.ExposedSlot;
import org.python.annotations.SlotFunc;

public abstract class VisitorBase<R> implements VisitorIF<R> {
    public R visitModule(Module node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterModule(Module node) {
        return true;
    }

    public void leaveModule(Module node) {
    }

    public R visitInteractive(Interactive node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterInteractive(Interactive node) {
        return true;
    }

    public void leaveInteractive(Interactive node) {
    }

    public R visitExpression(Expression node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterExpression(Expression node) {
        return true;
    }

    public void leaveExpression(Expression node) {
    }

    public R visitSuite(Suite node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterSuite(Suite node) {
        return true;
    }

    public void leaveSuite(Suite node) {
    }

    public R visitFunctionDef(FunctionDef node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterFunctionDef(FunctionDef node) {
        return true;
    }

    public void leaveFunctionDef(FunctionDef node) {
    }

    public R visitAsyncFunctionDef(AsyncFunctionDef node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterAsyncFunctionDef(AsyncFunctionDef node) {
        return true;
    }

    public void leaveAsyncFunctionDef(AsyncFunctionDef node) {
    }

    public R visitClassDef(ClassDef node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterClassDef(ClassDef node) {
        return true;
    }

    public void leaveClassDef(ClassDef node) {
    }

    public R visitReturn(Return node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterReturn(Return node) {
        return true;
    }

    public void leaveReturn(Return node) {
    }

    public R visitDelete(Delete node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterDelete(Delete node) {
        return true;
    }

    public void leaveDelete(Delete node) {
    }

    public R visitAssign(Assign node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterAssign(Assign node) {
        return true;
    }

    public void leaveAssign(Assign node) {
    }

    public R visitAugAssign(AugAssign node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterAugAssign(AugAssign node) {
        return true;
    }

    public void leaveAugAssign(AugAssign node) {
    }

    public R visitAnnAssign(AnnAssign node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterAnnAssign(AnnAssign node) {
        return true;
    }

    public void leaveAnnAssign(AnnAssign node) {
    }

    public R visitFor(For node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterFor(For node) {
        return true;
    }

    public void leaveFor(For node) {
    }

    public R visitAsyncFor(AsyncFor node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterAsyncFor(AsyncFor node) {
        return true;
    }

    public void leaveAsyncFor(AsyncFor node) {
    }

    public R visitWhile(While node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterWhile(While node) {
        return true;
    }

    public void leaveWhile(While node) {
    }

    public R visitIf(If node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterIf(If node) {
        return true;
    }

    public void leaveIf(If node) {
    }

    public R visitWith(With node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterWith(With node) {
        return true;
    }

    public void leaveWith(With node) {
    }

    public R visitAsyncWith(AsyncWith node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterAsyncWith(AsyncWith node) {
        return true;
    }

    public void leaveAsyncWith(AsyncWith node) {
    }

    public R visitRaise(Raise node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterRaise(Raise node) {
        return true;
    }

    public void leaveRaise(Raise node) {
    }

    public R visitTry(Try node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterTry(Try node) {
        return true;
    }

    public void leaveTry(Try node) {
    }

    public R visitAssert(Assert node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterAssert(Assert node) {
        return true;
    }

    public void leaveAssert(Assert node) {
    }

    public R visitImport(Import node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterImport(Import node) {
        return true;
    }

    public void leaveImport(Import node) {
    }

    public R visitImportFrom(ImportFrom node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterImportFrom(ImportFrom node) {
        return true;
    }

    public void leaveImportFrom(ImportFrom node) {
    }

    public R visitGlobal(Global node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterGlobal(Global node) {
        return true;
    }

    public void leaveGlobal(Global node) {
    }

    public R visitNonlocal(Nonlocal node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterNonlocal(Nonlocal node) {
        return true;
    }

    public void leaveNonlocal(Nonlocal node) {
    }

    public R visitExpr(Expr node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterExpr(Expr node) {
        return true;
    }

    public void leaveExpr(Expr node) {
    }

    public R visitPass(Pass node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterPass(Pass node) {
        return true;
    }

    public void leavePass(Pass node) {
    }

    public R visitBreak(Break node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterBreak(Break node) {
        return true;
    }

    public void leaveBreak(Break node) {
    }

    public R visitContinue(Continue node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterContinue(Continue node) {
        return true;
    }

    public void leaveContinue(Continue node) {
    }

    public R visitExitFor(ExitFor node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterExitFor(ExitFor node) {
        return true;
    }

    public void leaveExitFor(ExitFor node) {
    }

    public R visitSplitNode(SplitNode node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterSplitNode(SplitNode node) {
        return true;
    }

    public void leaveSplitNode(SplitNode node) {
    }

    public R visitBlock(Block node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterBlock(Block node) {
        return true;
    }

    public void leaveBlock(Block node) {
    }

    public R visitBoolOp(BoolOp node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterBoolOp(BoolOp node) {
        return true;
    }

    public void leaveBoolOp(BoolOp node) {
    }

    public R visitBinOp(BinOp node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterBinOp(BinOp node) {
        return true;
    }

    public void leaveBinOp(BinOp node) {
    }

    public R visitUnaryOp(UnaryOp node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterUnaryOp(UnaryOp node) {
        return true;
    }

    public void leaveUnaryOp(UnaryOp node) {
    }

    public R visitLambda(Lambda node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterLambda(Lambda node) {
        return true;
    }

    public void leaveLambda(Lambda node) {
    }

    public R visitAnonymousFunction(AnonymousFunction node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterAnonymousFunction(AnonymousFunction node) {
        return true;
    }

    public void leaveAnonymousFunction(AnonymousFunction node) {
    }

    public R visitIter(Iter node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterIter(Iter node) {
        return true;
    }

    public void leaveIter(Iter node) {
    }

    public R visitIterNext(IterNext node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterIterNext(IterNext node) {
        return true;
    }

    public void leaveIterNext(IterNext node) {
    }

    public R visitIfExp(IfExp node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterIfExp(IfExp node) {
        return true;
    }

    public void leaveIfExp(IfExp node) {
    }

    public R visitDict(Dict node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterDict(Dict node) {
        return true;
    }

    public void leaveDict(Dict node) {
    }

    public R visitSet(Set node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterSet(Set node) {
        return true;
    }

    public void leaveSet(Set node) {
    }

    public R visitListComp(ListComp node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterListComp(ListComp node) {
        return true;
    }

    public void leaveListComp(ListComp node) {
    }

    public R visitSetComp(SetComp node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterSetComp(SetComp node) {
        return true;
    }

    public void leaveSetComp(SetComp node) {
    }

    public R visitDictComp(DictComp node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterDictComp(DictComp node) {
        return true;
    }

    public void leaveDictComp(DictComp node) {
    }

    public R visitGeneratorExp(GeneratorExp node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterGeneratorExp(GeneratorExp node) {
        return true;
    }

    public void leaveGeneratorExp(GeneratorExp node) {
    }

    public R visitAwait(Await node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterAwait(Await node) {
        return true;
    }

    public void leaveAwait(Await node) {
    }

    public R visitYield(Yield node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterYield(Yield node) {
        return true;
    }

    public void leaveYield(Yield node) {
    }

    public R visitYieldFrom(YieldFrom node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterYieldFrom(YieldFrom node) {
        return true;
    }

    public void leaveYieldFrom(YieldFrom node) {
    }

    public R visitCompare(Compare node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterCompare(Compare node) {
        return true;
    }

    public void leaveCompare(Compare node) {
    }

    public R visitCall(Call node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterCall(Call node) {
        return true;
    }

    public void leaveCall(Call node) {
    }

    public R visitNum(Num node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterNum(Num node) {
        return true;
    }

    public void leaveNum(Num node) {
    }

    public R visitStr(Str node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterStr(Str node) {
        return true;
    }

    public void leaveStr(Str node) {
    }

    public R visitFormattedValue(FormattedValue node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterFormattedValue(FormattedValue node) {
        return true;
    }

    public void leaveFormattedValue(FormattedValue node) {
    }

    public R visitJoinedStr(JoinedStr node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterJoinedStr(JoinedStr node) {
        return true;
    }

    public void leaveJoinedStr(JoinedStr node) {
    }

    public R visitBytes(Bytes node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterBytes(Bytes node) {
        return true;
    }

    public void leaveBytes(Bytes node) {
    }

    public R visitNameConstant(NameConstant node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterNameConstant(NameConstant node) {
        return true;
    }

    public void leaveNameConstant(NameConstant node) {
    }

    public R visitEllipsis(Ellipsis node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterEllipsis(Ellipsis node) {
        return true;
    }

    public void leaveEllipsis(Ellipsis node) {
    }

    public R visitConstant(Constant node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterConstant(Constant node) {
        return true;
    }

    public void leaveConstant(Constant node) {
    }

    public R visitAttribute(Attribute node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterAttribute(Attribute node) {
        return true;
    }

    public void leaveAttribute(Attribute node) {
    }

    public R visitSubscript(Subscript node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterSubscript(Subscript node) {
        return true;
    }

    public void leaveSubscript(Subscript node) {
    }

    public R visitStarred(Starred node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterStarred(Starred node) {
        return true;
    }

    public void leaveStarred(Starred node) {
    }

    public R visitName(Name node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterName(Name node) {
        return true;
    }

    public void leaveName(Name node) {
    }

    public R visitList(List node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterList(List node) {
        return true;
    }

    public void leaveList(List node) {
    }

    public R visitTuple(Tuple node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterTuple(Tuple node) {
        return true;
    }

    public void leaveTuple(Tuple node) {
    }

    public R visitSlice(Slice node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterSlice(Slice node) {
        return true;
    }

    public void leaveSlice(Slice node) {
    }

    public R visitExtSlice(ExtSlice node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterExtSlice(ExtSlice node) {
        return true;
    }

    public void leaveExtSlice(ExtSlice node) {
    }

    public R visitIndex(Index node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterIndex(Index node) {
        return true;
    }

    public void leaveIndex(Index node) {
    }

    public R visitExceptHandler(ExceptHandler node) {
        R ret = unhandled_node(node);
        traverse(node);
        return ret;
    }

    public boolean enterExceptHandler(ExceptHandler node) {
        return true;
    }

    public void leaveExceptHandler(ExceptHandler node) {
    }

    abstract protected R unhandled_node(PythonTree node);
    abstract public void traverse(PythonTree node);
}
