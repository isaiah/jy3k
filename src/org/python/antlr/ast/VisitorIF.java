// Autogenerated AST node
package org.python.antlr.ast;

public interface VisitorIF<R> {
    public boolean enterModule(Module node);
    public R visitModule(Module node);
    public void leaveModule(Module node);
    public boolean enterInteractive(Interactive node);
    public R visitInteractive(Interactive node);
    public void leaveInteractive(Interactive node);
    public boolean enterExpression(Expression node);
    public R visitExpression(Expression node);
    public void leaveExpression(Expression node);
    public boolean enterSuite(Suite node);
    public R visitSuite(Suite node);
    public void leaveSuite(Suite node);
    public boolean enterFunctionDef(FunctionDef node);
    public R visitFunctionDef(FunctionDef node);
    public void leaveFunctionDef(FunctionDef node);
    public boolean enterAsyncFunctionDef(AsyncFunctionDef node);
    public R visitAsyncFunctionDef(AsyncFunctionDef node);
    public void leaveAsyncFunctionDef(AsyncFunctionDef node);
    public boolean enterClassDef(ClassDef node);
    public R visitClassDef(ClassDef node);
    public void leaveClassDef(ClassDef node);
    public boolean enterReturn(Return node);
    public R visitReturn(Return node);
    public void leaveReturn(Return node);
    public boolean enterDelete(Delete node);
    public R visitDelete(Delete node);
    public void leaveDelete(Delete node);
    public boolean enterAssign(Assign node);
    public R visitAssign(Assign node);
    public void leaveAssign(Assign node);
    public boolean enterAugAssign(AugAssign node);
    public R visitAugAssign(AugAssign node);
    public void leaveAugAssign(AugAssign node);
    public boolean enterAnnAssign(AnnAssign node);
    public R visitAnnAssign(AnnAssign node);
    public void leaveAnnAssign(AnnAssign node);
    public boolean enterFor(For node);
    public R visitFor(For node);
    public void leaveFor(For node);
    public boolean enterAsyncFor(AsyncFor node);
    public R visitAsyncFor(AsyncFor node);
    public void leaveAsyncFor(AsyncFor node);
    public boolean enterWhile(While node);
    public R visitWhile(While node);
    public void leaveWhile(While node);
    public boolean enterIf(If node);
    public R visitIf(If node);
    public void leaveIf(If node);
    public boolean enterWith(With node);
    public R visitWith(With node);
    public void leaveWith(With node);
    public boolean enterAsyncWith(AsyncWith node);
    public R visitAsyncWith(AsyncWith node);
    public void leaveAsyncWith(AsyncWith node);
    public boolean enterRaise(Raise node);
    public R visitRaise(Raise node);
    public void leaveRaise(Raise node);
    public boolean enterTry(Try node);
    public R visitTry(Try node);
    public void leaveTry(Try node);
    public boolean enterAssert(Assert node);
    public R visitAssert(Assert node);
    public void leaveAssert(Assert node);
    public boolean enterImport(Import node);
    public R visitImport(Import node);
    public void leaveImport(Import node);
    public boolean enterImportFrom(ImportFrom node);
    public R visitImportFrom(ImportFrom node);
    public void leaveImportFrom(ImportFrom node);
    public boolean enterGlobal(Global node);
    public R visitGlobal(Global node);
    public void leaveGlobal(Global node);
    public boolean enterNonlocal(Nonlocal node);
    public R visitNonlocal(Nonlocal node);
    public void leaveNonlocal(Nonlocal node);
    public boolean enterExpr(Expr node);
    public R visitExpr(Expr node);
    public void leaveExpr(Expr node);
    public boolean enterPass(Pass node);
    public R visitPass(Pass node);
    public void leavePass(Pass node);
    public boolean enterBreak(Break node);
    public R visitBreak(Break node);
    public void leaveBreak(Break node);
    public boolean enterContinue(Continue node);
    public R visitContinue(Continue node);
    public void leaveContinue(Continue node);
    public boolean enterExitFor(ExitFor node);
    public R visitExitFor(ExitFor node);
    public void leaveExitFor(ExitFor node);
    public boolean enterPopExcept(PopExcept node);
    public R visitPopExcept(PopExcept node);
    public void leavePopExcept(PopExcept node);
    public boolean enterSplitNode(SplitNode node);
    public R visitSplitNode(SplitNode node);
    public void leaveSplitNode(SplitNode node);
    public boolean enterBlock(Block node);
    public R visitBlock(Block node);
    public void leaveBlock(Block node);
    public boolean enterBoolOp(BoolOp node);
    public R visitBoolOp(BoolOp node);
    public void leaveBoolOp(BoolOp node);
    public boolean enterBinOp(BinOp node);
    public R visitBinOp(BinOp node);
    public void leaveBinOp(BinOp node);
    public boolean enterUnaryOp(UnaryOp node);
    public R visitUnaryOp(UnaryOp node);
    public void leaveUnaryOp(UnaryOp node);
    public boolean enterLambda(Lambda node);
    public R visitLambda(Lambda node);
    public void leaveLambda(Lambda node);
    public boolean enterAnonymousFunction(AnonymousFunction node);
    public R visitAnonymousFunction(AnonymousFunction node);
    public void leaveAnonymousFunction(AnonymousFunction node);
    public boolean enterIfExp(IfExp node);
    public R visitIfExp(IfExp node);
    public void leaveIfExp(IfExp node);
    public boolean enterDict(Dict node);
    public R visitDict(Dict node);
    public void leaveDict(Dict node);
    public boolean enterSet(Set node);
    public R visitSet(Set node);
    public void leaveSet(Set node);
    public boolean enterListComp(ListComp node);
    public R visitListComp(ListComp node);
    public void leaveListComp(ListComp node);
    public boolean enterSetComp(SetComp node);
    public R visitSetComp(SetComp node);
    public void leaveSetComp(SetComp node);
    public boolean enterDictComp(DictComp node);
    public R visitDictComp(DictComp node);
    public void leaveDictComp(DictComp node);
    public boolean enterGeneratorExp(GeneratorExp node);
    public R visitGeneratorExp(GeneratorExp node);
    public void leaveGeneratorExp(GeneratorExp node);
    public boolean enterAwait(Await node);
    public R visitAwait(Await node);
    public void leaveAwait(Await node);
    public boolean enterYield(Yield node);
    public R visitYield(Yield node);
    public void leaveYield(Yield node);
    public boolean enterYieldFrom(YieldFrom node);
    public R visitYieldFrom(YieldFrom node);
    public void leaveYieldFrom(YieldFrom node);
    public boolean enterCompare(Compare node);
    public R visitCompare(Compare node);
    public void leaveCompare(Compare node);
    public boolean enterCall(Call node);
    public R visitCall(Call node);
    public void leaveCall(Call node);
    public boolean enterNum(Num node);
    public R visitNum(Num node);
    public void leaveNum(Num node);
    public boolean enterStr(Str node);
    public R visitStr(Str node);
    public void leaveStr(Str node);
    public boolean enterFormattedValue(FormattedValue node);
    public R visitFormattedValue(FormattedValue node);
    public void leaveFormattedValue(FormattedValue node);
    public boolean enterJoinedStr(JoinedStr node);
    public R visitJoinedStr(JoinedStr node);
    public void leaveJoinedStr(JoinedStr node);
    public boolean enterBytes(Bytes node);
    public R visitBytes(Bytes node);
    public void leaveBytes(Bytes node);
    public boolean enterNameConstant(NameConstant node);
    public R visitNameConstant(NameConstant node);
    public void leaveNameConstant(NameConstant node);
    public boolean enterEllipsis(Ellipsis node);
    public R visitEllipsis(Ellipsis node);
    public void leaveEllipsis(Ellipsis node);
    public boolean enterConstant(Constant node);
    public R visitConstant(Constant node);
    public void leaveConstant(Constant node);
    public boolean enterAttribute(Attribute node);
    public R visitAttribute(Attribute node);
    public void leaveAttribute(Attribute node);
    public boolean enterSubscript(Subscript node);
    public R visitSubscript(Subscript node);
    public void leaveSubscript(Subscript node);
    public boolean enterStarred(Starred node);
    public R visitStarred(Starred node);
    public void leaveStarred(Starred node);
    public boolean enterName(Name node);
    public R visitName(Name node);
    public void leaveName(Name node);
    public boolean enterList(List node);
    public R visitList(List node);
    public void leaveList(List node);
    public boolean enterTuple(Tuple node);
    public R visitTuple(Tuple node);
    public void leaveTuple(Tuple node);
    public boolean enterSlice(Slice node);
    public R visitSlice(Slice node);
    public void leaveSlice(Slice node);
    public boolean enterExtSlice(ExtSlice node);
    public R visitExtSlice(ExtSlice node);
    public void leaveExtSlice(ExtSlice node);
    public boolean enterIndex(Index node);
    public R visitIndex(Index node);
    public void leaveIndex(Index node);
    public boolean enterExceptHandler(ExceptHandler node);
    public R visitExceptHandler(ExceptHandler node);
    public void leaveExceptHandler(ExceptHandler node);
}
