package org.python.antlr;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.python.antlr.ast.AnnAssign;
import org.python.antlr.ast.Assert;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.AugAssign;
import org.python.antlr.ast.Await;
import org.python.antlr.ast.BinOp;
import org.python.antlr.ast.BoolOp;
import org.python.antlr.ast.Break;
import org.python.antlr.ast.Bytes;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.Compare;
import org.python.antlr.ast.Context;
import org.python.antlr.ast.Continue;
import org.python.antlr.ast.Delete;
import org.python.antlr.ast.Dict;
import org.python.antlr.ast.DictComp;
import org.python.antlr.ast.Ellipsis;
import org.python.antlr.ast.ExceptHandler;
import org.python.antlr.ast.Expr;
import org.python.antlr.ast.Expression;
import org.python.antlr.ast.ExtSlice;
import org.python.antlr.ast.For;
import org.python.antlr.ast.FormattedValue;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.GeneratorExp;
import org.python.antlr.ast.Global;
import org.python.antlr.ast.If;
import org.python.antlr.ast.IfExp;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Index;
import org.python.antlr.ast.Interactive;
import org.python.antlr.ast.JoinedStr;
import org.python.antlr.ast.Lambda;
import org.python.antlr.ast.List;
import org.python.antlr.ast.ListComp;
import org.python.antlr.ast.Module;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.NameConstant;
import org.python.antlr.ast.Nonlocal;
import org.python.antlr.ast.Num;
import org.python.antlr.ast.Pass;
import org.python.antlr.ast.Raise;
import org.python.antlr.ast.Return;
import org.python.antlr.ast.Set;
import org.python.antlr.ast.SetComp;
import org.python.antlr.ast.Slice;
import org.python.antlr.ast.Starred;
import org.python.antlr.ast.Str;
import org.python.antlr.ast.Subscript;
import org.python.antlr.ast.Try;
import org.python.antlr.ast.Tuple;
import org.python.antlr.ast.UnaryOp;
import org.python.antlr.ast.While;
import org.python.antlr.ast.Yield;
import org.python.antlr.ast.YieldFrom;
import org.python.antlr.ast.alias;
import org.python.antlr.ast.arg;
import org.python.antlr.ast.arguments;
import org.python.antlr.ast.boolopType;
import org.python.antlr.ast.cmpopType;
import org.python.antlr.ast.comprehension;
import org.python.antlr.ast.expr_contextType;
import org.python.antlr.ast.keyword;
import org.python.antlr.ast.operatorType;
import org.python.antlr.ast.unaryopType;
import org.python.antlr.ast.withitem;
import org.python.antlr.base.excepthandler;
import org.python.antlr.base.expr;
import org.python.antlr.base.slice;
import org.python.antlr.base.stmt;
import org.python.compiler.AnnotationsCreator;
import org.python.compiler.ClassClosureGenerator;
import org.python.compiler.Lower;
import org.python.compiler.NameMangler;
import org.python.core.ParserFacade;
import org.python.core.Py;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Collectors;

public class BuildAstVisitor extends PythonBaseVisitor<PythonTree> {
    private String filename;
    private GrammarActions actions = new GrammarActions();
    private expr_contextType exprContextType = expr_contextType.Load;

    public BuildAstVisitor(String filename) {
        this.filename = filename;
    }

    @Override
    public PythonTree visitTry_stmt(PythonParser.Try_stmtContext ctx) {
        java.util.List<stmt> body = visit_Suite(ctx.suite(0));
        java.util.List<excepthandler> handlers = new ArrayList<>();
        for (int i = 0; i < ctx.except_clause().size(); i++) {
            PythonParser.Except_clauseContext except_clauseContext = ctx.except_clause(i);
            expr type = null;
            if (except_clauseContext.test() != null) {
                type = (expr) visit(except_clauseContext.test());
            }
            String name = null;
            if (except_clauseContext.NAME() != null) {
                name = except_clauseContext.NAME().getText();
            }
            java.util.List<stmt> handlerBody = visit_Suite(ctx.suite(i + 1));
            excepthandler handler = new ExceptHandler(except_clauseContext.getStart(), type, name, handlerBody);
            handlers.add(handler);
        }
        java.util.List<stmt> orelse = null;
        if (ctx.ELSE() != null) {
            orelse = visit_Suite(ctx.suite(ctx.except_clause().size() + 1));
        }
        java.util.List<stmt> finalBody = null;
        if (ctx.FINALLY() != null) {
            finalBody = visit_Suite(ctx.suite(ctx.suite().size() - 1));
        }
        return new Try(ctx.getStart(), body, handlers, orelse, finalBody);
    }

    @Override
    public PythonTree visitFile_input(PythonParser.File_inputContext ctx) {
        java.util.List<stmt> stmts = new ArrayList<>();
        ctx.stmt().stream().forEach(stmtContext -> stmts.addAll(visit_Stmt(stmtContext)));
        return new Module(ctx.getStart(), stmts);
    }

    @Override
    public PythonTree visitSingle_input(PythonParser.Single_inputContext ctx) {
        /**
         *  Experimental, the body will be modified if it's necessary to create a class closure,
         *  linked list is more efficient for that
         */
        java.util.List<stmt> body = new LinkedList<>();
        if (ctx.compound_stmt() != null) {
            body.add((stmt) visit(ctx.compound_stmt()));
        } else if (ctx.simple_stmt() != null){
            body = visit_Simple_stmt(ctx.simple_stmt());
        }
        return new Interactive(ctx.getStart(), body);
    }

    @Override
    public PythonTree visitEval_input(PythonParser.Eval_inputContext ctx) {
        return new Expression(ctx.getStart(), (expr) visit(ctx.testlist()));
    }

    private java.util.List<stmt> visit_Stmt(PythonParser.StmtContext ctx) {
        if (ctx.simple_stmt() != null) {
            return visit_Simple_stmt(ctx.simple_stmt());
        }
        return Arrays.asList((stmt) visit(ctx.compound_stmt()));
    }

    @Override
    public PythonTree visitExpr_stmt(PythonParser.Expr_stmtContext ctx) {
        /** Augassign */
        expr value;
        if (ctx.augassign() != null) {
            if (ctx.yield_expr().isEmpty()) {
                value = (expr) visit(ctx.testlist());
            } else {
                value = (expr) visit(ctx.yield_expr(0));
            }
            expr target = (expr) visit(ctx.testlist_star_expr(0));

            recursiveSetContextType(target, expr_contextType.AugStore);
            ((Context) target).setContext(expr_contextType.AugStore);
            return new AugAssign(ctx.getStart(), target, ctx.augassign().op, value);
        }
        if (ctx.annassign() != null) {
            expr target = (expr) visit(ctx.testlist_star_expr(0));
            if (!(target instanceof Context)) {
                throw Py.SyntaxError(ctx, "illegal target for annotation", filename);
            }
            if (target instanceof List) {
                throw Py.SyntaxError(target.getToken(), "only single target (not list) can be annotated", filename);
            } else if (target instanceof Tuple) {
                throw Py.SyntaxError(target.getToken(), "only single target (not tuple) can be annotated", filename);
            }
            ((Context) target).setContext(expr_contextType.Store);
            AnnassignResult annassignResult = visit_Annassign(ctx.annassign());
            int simple = target instanceof Name && !((Name) target).isExpr() ? 1 : 0;
            return new AnnAssign(ctx.getStart(), target, annassignResult.anno, annassignResult.value, simple);
        }
        /** Annotate assign */
        if (ctx.ASSIGN().isEmpty()) {
            return new Expr(ctx.getStart(), (expr) visit(ctx.testlist_star_expr(0)));
        }
        /** Assign */
        int targetsSize = ctx.testlist_star_expr().size();
        if (ctx.yield_expr().isEmpty()) {
            targetsSize--;
        }
        java.util.List<expr> targets = ctx.testlist_star_expr().stream()
                .limit(targetsSize)
                .map(testlist_star_exprContext -> {
                    expr e = (expr) visit(testlist_star_exprContext);
                    recursiveSetContextType(e, expr_contextType.Store);
                    return e;
                }).collect(Collectors.toList());
        if (ctx.yield_expr().isEmpty()) {
            value = (expr) visit(ctx.testlist_star_expr(targetsSize));
        } else {
            value = (expr) visit(ctx.yield_expr(0));
        }
        return new Assign(ctx.getStart(), targets, value);
    }

    @Override
    public PythonTree visitTestlist_star_expr(PythonParser.Testlist_star_exprContext ctx) {
        if (ctx.COMMA().isEmpty()) {
            if (ctx.test().isEmpty()) {
                return visit(ctx.star_expr(0));
            }
            return visit(ctx.test(0));
        }
        java.util.List<expr> elts = ctx.children.stream().map(t -> (expr) visit(t)).filter(Objects::nonNull).collect(Collectors.toList());

        return new Tuple(ctx.getStart(), elts, exprContextType);
    }

    @Override
    public PythonTree visitIf_stmt(PythonParser.If_stmtContext ctx) {
        java.util.List<stmt> orelse = null;
        int i = ctx.test().size();
        if (ctx.suite().size() > i) {
            orelse = visit_Suite(ctx.suite(i));
        }
        i--;
        for (; i > 0; i--) {
            PythonParser.TestContext testContext = ctx.test(i);
            try {
                orelse = Arrays.asList(new If(testContext.getStart(), (expr) visit(testContext), visit_Suite(ctx.suite(i)), orelse));
            } catch (NullPointerException e) {
                throw e;
            }
        }
        return new If(ctx.getStart(), (expr) visit(ctx.test(0)), visit_Suite(ctx.suite(0)), orelse);
    }

    @Override
    public PythonTree visitTest(PythonParser.TestContext ctx) {
        if (ctx.lambdef() != null) {
            return visit(ctx.lambdef());
        }
        if (ctx.IF() != null) {
            return new IfExp(ctx.getStart(), (expr) visit(ctx.or_test(1)),
                    (expr) visit(ctx.or_test(0)), (expr) visit(ctx.test()));
        }
        return visit(ctx.or_test(0));
    }

    @Override
    public PythonTree visitLambdef(PythonParser.LambdefContext ctx) {
        arguments args = null;
        if (ctx.varargslist() != null) {
            args = (arguments) visit(ctx.varargslist());
        }
        return new Lambda(ctx.getStart(), args, (expr) visit(ctx.test()));
    }

    @Override
    public PythonTree visitLambdef_nocond(PythonParser.Lambdef_nocondContext ctx) {
        return new Lambda(ctx.getStart(), (arguments) visit(ctx.varargslist()), (expr) visit(ctx.test_nocond()));
    }

    @Override
    public PythonTree visitArgument(PythonParser.ArgumentContext ctx) {
        if (ctx.ASSIGN() != null) {
            PythonTree key = visit(ctx.key);
            if (key instanceof Name) {
                return new keyword(ctx.getStart(), ((Name) key).getInternalId(), (expr) visit(ctx.val));
            }
            if (key instanceof Lambda) {
                // This is a special case: f(lambda x: x=1) is parsed as:
                // f(key=value) where (key=lambda, value=1)
                // But we want the error message to be consistent with lambda definition
                throw Py.SyntaxError(ctx, "lambda cannot contain assignment", filename);
            }
            throw Py.SyntaxError("keyword can't be an expression");
        } else if (ctx.POWER() != null) {
            return new keyword(ctx.getStart(), null, (expr) visit(ctx.test(0)));
        } else if (ctx.STAR() != null) {
            return new Starred(ctx.getStart(), (expr) visit(ctx.test(0)), expr_contextType.Load);
        } else {
            expr elt = (expr) visit(ctx.test(0));
            if (ctx.comp_for() != null) {
                return new GeneratorExp(ctx.getStart(), elt, visit_Comp_for(ctx.comp_for()));
            }
            return elt;
        }
    }

    @Override
    public PythonTree visitSubscript(PythonParser.SubscriptContext ctx) {
        if (ctx.COLON() == null) {
            return new Index(ctx.getStart(), (expr) visit(ctx.test(0)));
        }
        expr lower = null;
        expr upper = null;
        expr step = null;
        if (ctx.lower != null) {
            lower = (expr) visit(ctx.lower);
        }
        if (ctx.upper != null) {
            upper = (expr) visit(ctx.upper);
        }
        if (ctx.sliceop() != null) {
            step = (expr) visit(ctx.sliceop());
        }
        return new Slice(ctx.getStart(), lower, upper, step);
    }

    @Override
    public PythonTree visitSliceop(PythonParser.SliceopContext ctx) {
        if (ctx.test() == null) {
            return null;
        }
        return visit(ctx.test());
    }

    @Override
    public PythonTree visitAtom_expr(PythonParser.Atom_exprContext ctx) {
        expr left = (expr) visit(ctx.atom());
        for (PythonParser.TrailerContext trailerCtx : ctx.trailer()) {
            if (trailerCtx.OPEN_PAREN() != null) {
                ArglistResult arglistResult = visit_Arglist(trailerCtx.arglist());
                left = new Call(ctx.getStart(), left, arglistResult.args, arglistResult.keywords);
            } else if (trailerCtx.OPEN_BRACK() != null) {
                left = new Subscript(ctx.getStart(), left, visit_Subscriptlist(trailerCtx.subscriptlist()), exprContextType);
            } else if (trailerCtx.DOT() != null) {
                left = new Attribute(ctx.getStart(), left, trailerCtx.attr().getText(), exprContextType);
            }
        }
        if (ctx.AWAIT() != null) {
            return new Await(ctx.AWAIT().getSymbol(), left);
        }
        return left;
    }

    @Override
    public PythonTree visitPower(PythonParser.PowerContext ctx) {
        expr left = (expr) visit(ctx.atom_expr());
        if (ctx.factor() != null) {
            return new BinOp(ctx.getStart(), left, operatorType.Pow, (expr) visit(ctx.factor()));
        }
        return left;
    }

    @Override
    public PythonTree visitFactor(PythonParser.FactorContext ctx) {
        if (ctx.factor() != null) {
            unaryopType op;
            switch (ctx.op.getType()) {
                case PythonLexer.MINUS:
                    op = unaryopType.USub;
                    break;
                case PythonLexer.NOT_OP:
                    op = unaryopType.Invert;
                    break;
                default:
                    op = unaryopType.UAdd;
            }
            return new UnaryOp(ctx.getStart(), op, (expr) visit(ctx.factor()));
        }
        return visit(ctx.power());
    }

    @Override
    public PythonTree visitOr_test(PythonParser.Or_testContext ctx) {
        if (ctx.OR().isEmpty()) {
            return visit(ctx.and_test(0));
        }
        java.util.List<expr> values = ctx.and_test().stream()
                .map(and_testContext -> (expr) visit(and_testContext))
                .collect(Collectors.toList());
        return new BoolOp(ctx.getStart(), boolopType.Or, values);
    }

    @Override
    public PythonTree visitAnd_test(PythonParser.And_testContext ctx) {
        if (ctx.AND().isEmpty()) {
            return visit(ctx.not_test(0));
        }
        java.util.List<expr> values = ctx.not_test().stream()
                .map(not_testContext -> (expr) visit(not_testContext))
                .collect(Collectors.toList());
        return new BoolOp(ctx.getStart(), boolopType.And, values);
    }

    @Override
    public PythonTree visitNot_test(PythonParser.Not_testContext ctx) {
        if (ctx.NOT() != null) {
            return new UnaryOp(ctx.getStart(), unaryopType.Not, (expr) visit(ctx.not_test()));
        }
        return visit(ctx.comparison());
    }

    @Override
    public PythonTree visitComparison(PythonParser.ComparisonContext ctx) {
        if (ctx.comp_op().isEmpty()) {
            return visit(ctx.expr(0));
        }
        expr left = (expr) visit(ctx.expr(0));
        java.util.List<expr> comparators = ctx.expr().stream()
                .skip(1)
                .map((exprCtx) -> (expr) visit(exprCtx))
                .collect(Collectors.toList());
        java.util.List<cmpopType> ops = ctx.comp_op().stream()
                .map((opCtx) -> opCtx.op)
                .collect(Collectors.toList());
        return new Compare(ctx.getStart(), left, ops, comparators);
    }

    @Override
    public PythonTree visitExpr(PythonParser.ExprContext ctx) {
        if (ctx.OR_OP().isEmpty()) {
            return visit(ctx.xor_expr(0));
        }
        expr left = (expr) visit(ctx.xor_expr(0));
        for (int i = 1; i < ctx.xor_expr().size(); i++) {
            left = new BinOp(ctx.getStart(), left, operatorType.BitOr, (expr) visit(ctx.xor_expr(i)));
        }
        return left;
    }

    @Override
    public PythonTree visitXor_expr(PythonParser.Xor_exprContext ctx) {
        if (ctx.XOR().isEmpty()) {
            return visit(ctx.and_expr(0));
        }
        expr left = (expr) visit(ctx.and_expr(0));
        for (int i = 1; i < ctx.and_expr().size(); i++) {
            left = new BinOp(ctx.getStart(), left, operatorType.BitXor, (expr) visit(ctx.and_expr(i)));
        }
        return left;
    }

    @Override
    public PythonTree visitAnd_expr(PythonParser.And_exprContext ctx) {
        if (ctx.AND_OP().isEmpty()) {
            return visit(ctx.shift_expr(0));
        }
        expr left = (expr) visit(ctx.shift_expr(0));
        for (int i = 1; i < ctx.shift_expr().size(); i++) {
            left = new BinOp(ctx.getStart(), left, operatorType.BitAnd, (expr) visit(ctx.shift_expr(i)));
        }
        return left;
    }

    @Override
    public PythonTree visitShift_expr(PythonParser.Shift_exprContext ctx) {
        expr left = (expr) visit(ctx.arith_expr(0));
        for (int i = 0; i < ctx.ops.size(); i++) {
            operatorType op = ctx.ops.get(i).getType() == PythonLexer.LEFT_SHIFT ? operatorType.LShift : operatorType.RShift;
            left = new BinOp(ctx.getStart(), left, op, (expr) visit(ctx.arith_expr(i + 1)));
        }
        return left;
    }

    @Override
    public PythonTree visitArith_expr(PythonParser.Arith_exprContext ctx) {
        expr left = (expr) visit(ctx.term(0));
        for (int i = 0; i < ctx.ops.size(); i++) {
            operatorType op = ctx.ops.get(i).getType() == PythonLexer.MINUS ? operatorType.Sub : operatorType.Add;
            left = new BinOp(ctx.getStart(), left, op, (expr) visit(ctx.term(i + 1)));
        }
        return left;
    }

    @Override
    public PythonTree visitTerm(PythonParser.TermContext ctx) {
        expr left = (expr) visit(ctx.factor(0));
        for (int i = 0; i < ctx.ops.size(); i++) {
            operatorType op;
            switch (ctx.ops.get(i).getType()) {
                case PythonLexer.DIV:
                    op = operatorType.Div;
                    break;
                case PythonLexer.MOD:
                    op = operatorType.Mod;
                    break;
                case PythonLexer.IDIV:
                    op = operatorType.FloorDiv;
                    break;
                case PythonLexer.STAR:
                    op = operatorType.Mult;
                    break;
                case PythonLexer.AT:
                    op = operatorType.MatMult;
                    break;
                default:
                    op = operatorType.UNDEFINED;
            }
            left = new BinOp(ctx.factor(i).getStart(), left, op, (expr) visit(ctx.factor(i + 1)));
        }
        return left;
    }

    @Override
    public PythonTree visitStar_expr(PythonParser.Star_exprContext ctx) {
        return new Starred(ctx.getStart(), (expr) visit(ctx.expr()), exprContextType);
    }

    @Override
    public PythonTree visitAsync_stmt(PythonParser.Async_stmtContext ctx) {
        if (ctx.funcdef() != null) {
            FunctionDef func = (FunctionDef) visit(ctx.funcdef());
            return actions.makeAsyncFuncdef(ctx.getStart(), func, Arrays.asList());
        }
        if (ctx.for_stmt() != null) {
            return actions.makeAsyncFor(ctx.getStart(), (For) visit(ctx.for_stmt()));
        }
        java.util.List<withitem> items = ctx.with_stmt().with_item().stream()
                .map(with_itemContext -> (withitem) visit(with_itemContext))
                .collect(Collectors.toList());
        return actions.makeAsyncWith(ctx.getStart(), items, visit_Suite(ctx.with_stmt().suite()));
    }

    @Override
    public PythonTree visitWith_stmt(PythonParser.With_stmtContext ctx) {
        java.util.List<withitem> items = ctx.with_item().stream()
                .map(with_itemContext -> (withitem) visit(with_itemContext))
                .collect(Collectors.toList());
        return actions.makeWith(ctx.getStart(), items, visit_Suite(ctx.suite()));
    }

    @Override
    public PythonTree visitWith_item(PythonParser.With_itemContext ctx) {
        expr as = null;
        if (ctx.expr() != null) {
            as = (expr) visit(ctx.expr());
            recursiveSetContextType(as, expr_contextType.Store);
        }
        return new withitem(ctx.getStart(), (expr) visit(ctx.test()), as);
    }

    @Override
    public PythonTree visitFor_stmt(PythonParser.For_stmtContext ctx) {
        expr iter = (expr) visit(ctx.exprlist());
        recursiveSetContextType(iter, expr_contextType.Store);
        return new For(ctx.getStart(), iter,
                (expr) visit(ctx.testlist()), visit_Suite(ctx.s1), visit_Suite(ctx.s2));
    }

    @Override
    public PythonTree visitWhile_stmt(PythonParser.While_stmtContext ctx) {
        return new While(ctx.getStart(), (expr) visit(ctx.test()), visit_Suite(ctx.s1), visit_Suite(ctx.s2));
    }

    @Override
    public PythonTree visitAssert_stmt(PythonParser.Assert_stmtContext ctx) {
        expr msg = null;
        if (ctx.test().size() > 1) {
            msg = (expr) visit(ctx.test(1));
        }
        return new Assert(ctx.getStart(), (expr) visit(ctx.test(0)), msg);
    }

    @Override
    public PythonTree visitNonlocal_stmt(PythonParser.Nonlocal_stmtContext ctx) {
        return new Nonlocal(ctx.getStart(), actions.makeNames(ctx.NAME()));
    }

    @Override
    public PythonTree visitGlobal_stmt(PythonParser.Global_stmtContext ctx) {
        return new Global(ctx.getStart(), actions.makeNames(ctx.NAME()));
    }

    @Override
    public PythonTree visitInteger(PythonParser.IntegerContext ctx) {
        if (ctx.DECIMAL_INTEGER() != null) {
            return new Num(ctx.DECIMAL_INTEGER().getSymbol(), actions.makeDecimal(ctx.DECIMAL_INTEGER()));
        }
        if (ctx.HEX_INTEGER() != null) {
            return new Num(ctx.HEX_INTEGER().getSymbol(), actions.makeInt(ctx.HEX_INTEGER(), 16));
        }
        if (ctx.BIN_INTEGER() != null)
            return new Num(ctx.BIN_INTEGER().getSymbol(), actions.makeInt(ctx.BIN_INTEGER(), 2));
        if (ctx.OCT_INTEGER() != null)
            return new Num(ctx.OCT_INTEGER().getSymbol(), actions.makeInt(ctx.OCT_INTEGER(), 8));
        return super.visitInteger(ctx);
    }

    @Override
    public PythonTree visitNumber(PythonParser.NumberContext ctx) {
        if (ctx.FLOAT_NUMBER() != null) {
            return new Num(ctx.FLOAT_NUMBER().getSymbol(), actions.makeFloat(ctx.FLOAT_NUMBER()));
        } else if (ctx.IMAG_NUMBER() != null) {
            return new Num(ctx.IMAG_NUMBER().getSymbol(), actions.makeComplex(ctx.IMAG_NUMBER()));
        } else if (ctx.integer() != null) {
            return visit(ctx.integer());
        }
        return super.visitNumber(ctx);
    }

    @Override
    public PythonTree visitDictorsetmaker(PythonParser.DictorsetmakerContext ctx) {
        java.util.List<comprehension> comps = null;
        if (ctx.comp_for() != null) {
            comps = visit_Comp_for(ctx.comp_for());
        }

        if (!ctx.COLON().isEmpty()) {
            if (ctx.comp_for() != null) {
                /** Dict comprehension */
                expr key = (expr) visit(ctx.test(0));
                expr val = (expr) visit(ctx.test(1));
                return new DictComp(ctx.getStart(), key, val, comps);
            }
            /** Dict */
            java.util.List<expr> keys = new ArrayList<>();
            java.util.List<expr> vals = new ArrayList<>();
            for (PythonParser.TestContext testContext : ctx.keys) {
                keys.add((expr) visit(testContext));
            }
            for (PythonParser.TestContext testContext : ctx.vals) {
                vals.add((expr) visit(testContext));
            }
            for (PythonParser.ExprContext exprContext : ctx.dicts) {
                vals.add((expr) visit(exprContext));
            }
            return new Dict(ctx.getStart(), keys, vals);
        }
        if (ctx.comp_for() != null) {
            /** Set comprehension */
            expr elt = (expr) visit(ctx.test(0));
            return new SetComp(ctx.getStart(), elt, comps);
        }
        /** Set */
        java.util.List<expr> elts = new ArrayList<>();
        for (PythonParser.TestContext testContext : ctx.test()) {
            elts.add((expr) visit(testContext));
        }
        for (PythonParser.Star_exprContext star_exprContext : ctx.star_expr()) {
            elts.add((expr) visit(star_exprContext));
        }
        return new Set(ctx.getStart(), elts);
    }

    @Override
    public PythonTree visitAtom(PythonParser.AtomContext ctx) {
        Testlist_compResult testlistCompResult = visit_Testlist_comp(ctx.testlist_comp());
        if (ctx.OPEN_PAREN() != null) {
            if (ctx.yield_expr() != null) {
                return visit(ctx.yield_expr());
            } else if (ctx.testlist_comp() == null || !ctx.testlist_comp().COMMA().isEmpty()) {
                return new Tuple(ctx.getStart(), testlistCompResult.exprs, exprContextType);
            }
            if (testlistCompResult.comps != null) {
                return new GeneratorExp(ctx.getStart(), testlistCompResult.exprs.get(0), testlistCompResult.comps);
            }
            expr e = testlistCompResult.exprs.get(0);
            if (e instanceof Name) {
                ((Name) e).setExpr(true);
            }
            return e;
        } else if (ctx.OPEN_BRACK() != null) {
            if (testlistCompResult.comps != null) {
                return new ListComp(ctx.getStart(), testlistCompResult.exprs.get(0), testlistCompResult.comps);
            }
            return new List(ctx.getStart(), testlistCompResult.exprs, exprContextType);
        } else if (ctx.OPEN_BRACE() != null) {
            if (ctx.dictorsetmaker() == null) {
                return new Dict(ctx.getStart(), null, null);
            }
            return visit(ctx.dictorsetmaker());
        } else if (ctx.number() != null) {
            return visit(ctx.number());
        } else if (ctx.yield_expr() != null) {
            return visit(ctx.yield_expr());
        } else if (ctx.NAME() != null) {
            return new Name(ctx.NAME().getSymbol(), ctx.getText(), exprContextType);
        } else if (ctx.ellipsis != null) {
            return new Ellipsis(ctx.ellipsis);
        } else if (ctx.TRUE() != null) {
            return new NameConstant(ctx.TRUE().getSymbol(), ctx.getText());
        } else if (ctx.FALSE() != null) {
            return new NameConstant(ctx.FALSE().getSymbol(), ctx.getText());
        } else if (ctx.NONE() != null) {
            return new NameConstant(ctx.NONE().getSymbol(), ctx.getText());
        } else if (ctx.str() != null) {
            return actions.parsestrplus(ctx.str());
        }
        return super.visitAtom(ctx);
    }

    private java.util.List<expr> visit_Decorators(PythonParser.DecoratorsContext ctx) {
        return ctx.decorator().stream()
                .map(decoratorContext -> (expr) visit(decoratorContext))
                .collect(Collectors.toList());
    }

    @Override
    public PythonTree visitFuncdef(PythonParser.FuncdefContext ctx) {
        return visit_Funcdef(ctx, Arrays.asList());
    }

    @Override
    public PythonTree visitClassdef(PythonParser.ClassdefContext ctx) {
        return visit_Class_def(ctx, Arrays.asList());
    }

    @Override
    public PythonTree visitDecorated(PythonParser.DecoratedContext ctx) {
        java.util.List<expr> decoratorList = visit_Decorators(ctx.decorators());
        if (ctx.classdef() != null) {
            return visit_Class_def(ctx.classdef(), decoratorList);
        }
        if (ctx.async_funcdef() != null) {
            return visit_Async_funcdef(ctx.async_funcdef(), decoratorList);
        }
        return visit_Funcdef(ctx.funcdef(), decoratorList);
    }

    @Override
    public PythonTree visitDecorator(PythonParser.DecoratorContext ctx) {
        if (ctx.OPEN_PAREN() != null) {
            ArglistResult arglistResult = visit_Arglist(ctx.arglist());
            return new Call(ctx.getStart(), (expr) visit(ctx.dotted_name()), arglistResult.args, arglistResult.keywords);
        }
        return visit(ctx.dotted_name());
    }

    /**
     * NOTE: there is a variant visit_Dotted_name what returns the joined String
     */
    @Override
    public PythonTree visitDotted_name(PythonParser.Dotted_nameContext ctx) {
        expr current = new Name(ctx.NAME(0).getSymbol(), ctx.NAME(0).getText(), exprContextType);
        if (ctx.DOT().isEmpty()) {
            return current;
        }
        for (int i = 1; i < ctx.NAME().size(); i++) {
            TerminalNode nameNode = ctx.NAME(i);
            current = new Attribute(nameNode.getSymbol(), current, nameNode.getText(), exprContextType);
        }
        return current;
    }

    @Override
    public PythonTree visitAsync_funcdef(PythonParser.Async_funcdefContext ctx) {
        return visit_Async_funcdef(ctx, Arrays.asList());
    }

    private PythonTree visit_Async_funcdef(PythonParser.Async_funcdefContext ctx, java.util.List<expr> decoratorList) {
        FunctionDef func = (FunctionDef) visit(ctx.funcdef());
        return actions.makeAsyncFuncdef(ctx.getStart(), func, decoratorList);
    }

    private PythonTree visit_Funcdef(PythonParser.FuncdefContext ctx, java.util.List<expr> decoratorList) {
        arguments args = (arguments) visit(ctx.parameters());
        expr anno = null;
        if (ctx.test() != null) {
            anno = (expr) visit(ctx.test());
        }
        return new FunctionDef(ctx.getStart(), ctx.NAME().getText(), args, visit_Suite(ctx.suite()), decoratorList, anno);
    }

    private PythonTree visit_Class_def(PythonParser.ClassdefContext ctx, java.util.List<expr> decoratorList) {
        ArglistResult arglistResult = visit_Arglist(ctx.arglist());
        return new ClassDef(ctx.getStart(), ctx.NAME().getText(),
                arglistResult.args, arglistResult.keywords, visit_Suite(ctx.suite()), decoratorList);
    }

    @Override
    public PythonTree visitParameters(PythonParser.ParametersContext ctx) {
        if (ctx.typedargslist() != null) {
            return visit(ctx.typedargslist());
        }
        return new arguments();
    }

    @Override
    public PythonTree visitTfpdef(PythonParser.TfpdefContext ctx) {
        expr anno = null;
        if (ctx.test() != null) {
            anno = (expr) visit(ctx.test());
        }
        return new arg(ctx.getStart(), ctx.NAME().getText(), anno);
    }

    @Override
    public PythonTree visitTypedargslist(PythonParser.TypedargslistContext ctx) {
        arg kwarg = null;
        arg vararg = null;
        java.util.List<expr> kwdefaults = new ArrayList<>();
        java.util.List<expr> defaults = new ArrayList<>();
        java.util.List<arg> args = new ArrayList<>();
        java.util.List<arg> kwonlyargs = new ArrayList<>(ctx.tfpdkv().size());
        if (ctx.POWER() != null) {
            kwarg = (arg) visit(ctx.kwarg);
        }
        for (PythonParser.TfpdkvContext tfpdkvContext : ctx.tfpdkv()) {
            kwonlyargs.add((arg) visit(tfpdkvContext.tfpdef()));
            if (tfpdkvContext.ASSIGN() != null) {
                kwdefaults.add((expr) visit(tfpdkvContext.test()));
            }
        }
        if (ctx.STAR() != null) {
            vararg = ctx.vararg != null ? (arg) visit(ctx.vararg) :
                    new arg(ctx.STAR().getSymbol(), "", null);
        }
        for (PythonParser.TdefparameterContext tdefparameterContext : ctx.args) {
            arg param = (arg) visit(tdefparameterContext.tfpdef());
            args.add(param);
            PythonParser.TestContext testContext = tdefparameterContext.test();
            if (testContext != null) {
                defaults.add((expr) visit(testContext));
            } else {
                if (!defaults.isEmpty()) {
                    throw Py.SyntaxError(tdefparameterContext, "non-default argument follows default argument", filename);
                }
            }
        }
        return new arguments(ctx.getStart(), args, vararg, kwonlyargs, kwdefaults, kwarg, defaults);
    }

    @Override
    public PythonTree visitVfpdef(PythonParser.VfpdefContext ctx) {
        return new arg(ctx.getStart(), ctx.NAME().getText(), null);
    }

    @Override
    public PythonTree visitVarargslist(PythonParser.VarargslistContext ctx) {
        arg kwarg = null;
        arg vararg = null;
        java.util.List<expr> kwdefaults = new ArrayList<>(ctx.kwd.size());
        java.util.List<expr> defaults = new ArrayList<>();
        java.util.List<arg> args = new ArrayList<>();
        java.util.List<arg> kwonlyargs = new ArrayList<>(ctx.kw.size());
        if (ctx.POWER() != null) {
            exprContextType = expr_contextType.Param; // XXX
            kwarg = (arg) visit(ctx.kwarg);
            exprContextType = expr_contextType.Load;
        }
        for (PythonParser.TestContext testContext : ctx.kwd) {
            kwdefaults.add((expr) visit(testContext));
        }
        if (ctx.STAR() != null) {
            exprContextType = expr_contextType.Param;
            if (ctx.vararg == null) {
                if (ctx.kw.isEmpty()) {
                    throw Py.SyntaxError("named arguments must follow bare *");
                }
                vararg = new arg(ctx.STAR().getSymbol(), null, null);
            } else {
                vararg = (arg) visit(ctx.vararg);
            }
            for (PythonParser.VfpdefContext vfpdefContext : ctx.kw) {
                kwonlyargs.add((arg) visit(vfpdefContext));
            }
            exprContextType = expr_contextType.Load;
        }

        exprContextType = expr_contextType.Load;
        for (PythonParser.TestContext testContext : ctx.defaults) {
            defaults.add((expr) visit(testContext));
        }
        exprContextType = expr_contextType.Param; // XXX
        for (PythonParser.VfpdefContext vfpdefContext : ctx.args) {
            args.add((arg) visit(vfpdefContext));
        }
        exprContextType = expr_contextType.Load;
        return new arguments(ctx.getStart(), args, vararg, kwonlyargs, kwdefaults, kwarg, defaults);
    }

    @Override
    public PythonTree visitRaise_stmt(PythonParser.Raise_stmtContext ctx) {
        expr from = null;
        expr exc = null;
        if (ctx.test().size() > 1) {
            from = (expr) visit(ctx.test(1));
        }
        if (ctx.test().size() > 0) {
            exc = (expr) visit(ctx.test(0));
        }
        return new Raise(ctx.getStart(), exc, from);
    }

    @Override
    public PythonTree visitImport_from(PythonParser.Import_fromContext ctx) {
        int lvl = ctx.DOT().size();
        lvl += ctx.ELLIPSIS().size() * 3;
        if (ctx.STAR() != null) {
            alias star = new alias(ctx.getStart(), "*", null);
            return new ImportFrom(ctx.getStart(), visit_Dotted_name(ctx.dotted_name()), Arrays.asList(star), lvl);
        }
        return new ImportFrom(ctx.getStart(), visit_Dotted_name(ctx.dotted_name()),
                visit_Import_as_names(ctx.import_as_names()), lvl);
    }

    @Override
    public PythonTree visitImport_as_name(PythonParser.Import_as_nameContext ctx) {
        String asName = null;
        if (ctx.NAME().size() > 1) {
            asName = ctx.NAME(1).getText();
        }
        return new alias(ctx.getStart(), ctx.NAME(0).getText(), asName);
    }

    @Override
    public PythonTree visitImport_name(PythonParser.Import_nameContext ctx) {
        return new Import(ctx.getStart(), visit_Dotted_as_names(ctx.dotted_as_names()));
    }

    @Override
    public PythonTree visitDotted_as_name(PythonParser.Dotted_as_nameContext ctx) {
        String as = null;
        if (ctx.NAME() != null) {
            as = ctx.NAME().getText();
        }
        return new alias(ctx.getStart(), visit_Dotted_name(ctx.dotted_name()), as);
    }

    @Override
    public PythonTree visitDel_stmt(PythonParser.Del_stmtContext ctx) {
        java.util.List<expr> exprs = visit_Exprlist(ctx.exprlist());
        exprs.stream().forEach(expr -> {
            recursiveSetContextType(expr, expr_contextType.Del);
        });
        return new Delete(ctx.getStart(), exprs);
    }

    @Override
    public PythonTree visitBreak_stmt(PythonParser.Break_stmtContext ctx) {
        return new Break(ctx.getStart());
    }

    @Override
    public PythonTree visitPass_stmt(PythonParser.Pass_stmtContext ctx) {
        return new Pass(ctx.getStart());
    }

    @Override
    public PythonTree visitContinue_stmt(PythonParser.Continue_stmtContext ctx) {
        return new Continue(ctx.getStart());
    }

    @Override
    public PythonTree visitReturn_stmt(PythonParser.Return_stmtContext ctx) {
        if (ctx.testlist() != null) {
            return new Return(ctx.getStart(), (expr) visit(ctx.testlist()));
        }
        return new Return(ctx.getStart(), null);
    }

    @Override
    public PythonTree visitExprlist(PythonParser.ExprlistContext ctx) {
        java.util.List<expr> elts = visit_Exprlist(ctx);
        if (ctx.COMMA().isEmpty()) {
            return elts.get(0);
        }
        return new Tuple(ctx.getStart(), elts, exprContextType);
    }

    @Override
    public PythonTree visitTestlist(PythonParser.TestlistContext ctx) {
        java.util.List<expr> exprs = ctx.test().stream()
                .map(testContext -> (expr) visit(testContext))
                .collect(Collectors.toList());
        if (ctx.COMMA().isEmpty()) return exprs.get(0);
        return new Tuple(ctx.getStart(), exprs, exprContextType);
    }

    @Override
    public PythonTree visitYield_stmt(PythonParser.Yield_stmtContext ctx) {
        return new Expr(ctx.getStart(), (expr) visit(ctx.yield_expr()));
    }

    @Override
    public PythonTree visitYield_expr(PythonParser.Yield_exprContext ctx) {
        PythonParser.Yield_argContext arg = ctx.yield_arg();
        if (arg != null) {
            if (arg.FROM() != null) {
                return new YieldFrom(ctx.getStart(), (expr) visit(arg));
            }
            return new Yield(ctx.getStart(), (expr) visit(arg));
        }
        return new Yield(ctx.getStart(), null);
    }

    @Override
    public PythonTree visitYield_arg(PythonParser.Yield_argContext ctx) {
        if (ctx.test() != null) {
            return visit(ctx.test());
        } else if (ctx.testlist() != null) {
            return visit(ctx.testlist());
        }
        return super.visitYield_arg(ctx);
    }

    /**
     * helper method
     */
    private java.util.List<expr> visit_Exprlist(PythonParser.ExprlistContext ctx) {
        java.util.List<ParseTree> children = ctx.children;

        return children.stream().map(t -> (expr) visit(t)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private java.util.List<alias> visit_Dotted_as_names(PythonParser.Dotted_as_namesContext ctx) {
        java.util.List<alias> aliases = new ArrayList<>();
        for (PythonParser.Dotted_as_nameContext dotted : ctx.dotted_as_name()) {
            aliases.add((alias) visit(dotted));
        }
        return aliases;
    }

    private java.util.List<stmt> visit_Simple_stmt(PythonParser.Simple_stmtContext ctx) {
        java.util.List<stmt> stmts = new ArrayList<>();
        for (PythonParser.Small_stmtContext smallStmtCtx : ctx.small_stmt()) {
            stmts.add((stmt) visit(smallStmtCtx));
        }
        return stmts;
    }

    private java.util.List<stmt> visit_Suite(PythonParser.SuiteContext ctx) {
        if (ctx == null) return null;
        if (ctx.simple_stmt() != null) {
            return visit_Simple_stmt(ctx.simple_stmt());
        }
        return ctx.stmt().stream()
                .flatMap(stmtContext -> visit_Stmt(stmtContext).stream())
                .collect(Collectors.toList());
    }

    class Testlist_compResult {
        java.util.List<expr> exprs;
        java.util.List<comprehension> comps;

        public Testlist_compResult() {
            exprs = new ArrayList<>();
        }
    }

    class ArglistResult {
        java.util.List<expr> args;
        java.util.List<keyword> keywords;

        public ArglistResult() {
            this.args = new ArrayList<>();
            this.keywords = new ArrayList<>();
        }
    }

    class Comp_iterResult {
        expr ifs;
        java.util.List<comprehension> comps;
        Comp_iterResult iter;
    }

    class AnnassignResult {
        expr anno;
        expr value;
    }

    private AnnassignResult visit_Annassign(PythonParser.AnnassignContext ctx) {
        AnnassignResult ret = new AnnassignResult();
        ret.anno = (expr) visit(ctx.test(0));
        if (ctx.ASSIGN() != null) {
            ret.value = (expr) visit(ctx.test(1));
        }
        return ret;
    }

    // e.g. restFiles = [os.path.join(d[0], f) for d in os.walk(".") if not "_test" in d[0] for f in d[2] if f.endswith(".rst")]
    private Comp_iterResult visit_Comp_iter(PythonParser.Comp_iterContext ctx) {
        Comp_iterResult ret = new Comp_iterResult();
        if (ctx.comp_for() != null) {
            ret.comps = visit_Comp_for(ctx.comp_for());
        } else if (ctx.comp_if() != null) {
            ret.iter = visit_Comp_if(ctx.comp_if());
        }
        return ret;
    }

    private Comp_iterResult visit_Comp_if(PythonParser.Comp_ifContext ctx) {
        Comp_iterResult ret = new Comp_iterResult();
        ret.ifs = (expr) visit(ctx.test_nocond());
        if (ctx.comp_iter() != null) {
            ret.iter = visit_Comp_iter(ctx.comp_iter());
        }
        return ret;
    }

    private java.util.List<comprehension> visit_Comp_for(PythonParser.Comp_forContext ctx) {
        java.util.List<comprehension> ret = new ArrayList<>();
        expr target = (expr) visit(ctx.exprlist());
        recursiveSetContextType(target, expr_contextType.Store);
        expr iter = (expr) visit(ctx.or_test());
        java.util.List<expr> ifs = new ArrayList<>();
        if (ctx.comp_iter() != null) {
            Comp_iterResult iterRet = visit_Comp_iter(ctx.comp_iter());
            for (; iterRet != null; iterRet = iterRet.iter) {
                if (iterRet.ifs != null) {
                    ifs.add(iterRet.ifs);
                    iterRet.ifs = null;
                }
                if (iterRet.comps != null) {
                    ret.addAll(iterRet.comps);
                }
            }
        }
        ret.add(new comprehension(ctx.getStart(), target, iter, ifs));
        return ret;
    }

    private ArglistResult visit_Arglist(PythonParser.ArglistContext ctx) {
        ArglistResult ret = new ArglistResult();
        int ndoublestars = 0, nkeywords = 0, nargs = 0, ngens = 0;

        if (ctx == null) return ret;
        for (PythonParser.ArgumentContext argCtx : ctx.argument()) {
            if (argCtx.comp_for() != null) {
                ngens++;
            }
            PythonTree arg = visit(argCtx);
            if (arg instanceof keyword) {
                keyword kw = (keyword) arg;
                ret.keywords.add(kw);
                if (kw.getInternalArg() == null) {
                    ndoublestars++;
                } else {
                    nkeywords++;
                }
            } else {
                ret.args.add((expr) arg);
                nargs++;
                if (ndoublestars > 0) {
                    if (arg instanceof Starred) {
                        throw Py.SyntaxError(ctx, "iterable argument unpacking follows keyword argument unpacking", filename);
                    } else {
                        throw Py.SyntaxError(ctx, "positional argument follows keyword argument unpacking", filename);
                    }
                } else if (nkeywords > 0) {
                    if (!(arg instanceof Starred)) {
                        throw Py.SyntaxError(ctx, "positional argument follows keyword argument", filename);
                    }
                }
            }
        }
        if (ngens > 1 || (ngens > 0 && (nkeywords > 0 || nargs > ngens))) {
            throw Py.SyntaxError(ctx,"Generator expression must be parenthesized if not sole argument", filename);
        }
        return ret;
    }

    private slice visit_Subscriptlist(PythonParser.SubscriptlistContext ctx) {
        if (ctx.COMMA().isEmpty()) {
            return (slice) visit(ctx.subscript(0));
        }
        java.util.List<slice> dims = new ArrayList<>(ctx.subscript().size());
        for (PythonParser.SubscriptContext subscriptContext : ctx.subscript()) {
            dims.add((slice) visitSubscript(subscriptContext));
        }
        return new ExtSlice(ctx.getStart(), dims);
    }

    private String visit_Dotted_name(PythonParser.Dotted_nameContext ctx) {
        /**
         *  When import from relative path without specifying the module
         */
        if (ctx == null) {
            return "";
        }
        return ctx.NAME().stream()
                .map((name) -> name.getText())
                .collect(Collectors.joining("."));
    }

    private Testlist_compResult visit_Testlist_comp(PythonParser.Testlist_compContext ctx) {
        Testlist_compResult ret = new Testlist_compResult();
        if (ctx == null) return ret;
        for (PythonParser.TestContext testContext : ctx.t) {
            ret.exprs.add((expr) visit(testContext));
        }
        for (PythonParser.Star_exprContext star_exprContext : ctx.s) {
            ret.exprs.add((expr) visit(star_exprContext));
        }
        if (ctx.comp_for() != null) {
            ret.comps = visit_Comp_for(ctx.comp_for());
        }
        return ret;
    }

    private java.util.List<alias> visit_Import_as_names(PythonParser.Import_as_namesContext ctx) {
        return ctx.import_as_name().stream()
                .map(import_as_nameContext -> (alias) visit(import_as_nameContext))
                .collect(Collectors.toList());
    }

    private void recursiveSetContextType(expr e, expr_contextType context) {
        if (e instanceof Context) {
            ((Context) e).setContext(context);
            if (e instanceof Tuple) {
                ((Tuple) e).getInternalElts().forEach(elt -> {
                    recursiveSetContextType(elt, context);
                });
            } else if (e instanceof Starred) {
                recursiveSetContextType(((Starred) e).getInternalValue(), context);
            } else if (e instanceof List) {
                ((List) e).getInternalElts().forEach(elt -> recursiveSetContextType(elt, context));
            }
            return;
        }

        String verb;
        if (context == expr_contextType.Del) {
            verb = "delete";
        } else if (context == expr_contextType.Store || context == expr_contextType.AugStore) {
            verb = "assign to";
        } else {
            return;
        }
        String exprName = "";
        if (e instanceof NameConstant) {
            exprName = "keyword";
        } else if (e instanceof Call) {
            exprName = "function call";
        } else if (e instanceof Lambda) {
            exprName = "lambda";
        } else if (e instanceof BinOp || e instanceof BoolOp || e instanceof UnaryOp) {
            exprName = "operator";
        } else if (e instanceof GeneratorExp) {
            exprName = "generator expression";
        } else if (e instanceof Await) {
            exprName = "await expression";
        } else if (e instanceof ListComp) {
            exprName = "list comprehension";
        } else if (e instanceof SetComp) {
            exprName = "set comprehension";
        } else if (e instanceof DictComp) {
            exprName = "dict comprehension";
        } else if (e instanceof Dict || e instanceof Set || e instanceof Num || e instanceof Str ||
                e instanceof Bytes || e instanceof JoinedStr || e instanceof FormattedValue) {
            exprName = "literal";
        } else if (e instanceof Ellipsis) {
            exprName = "Ellipsis";
        } else if (e instanceof Compare) {
            exprName = "comparison";
        } else if (e instanceof IfExp) {
            exprName = "conditional expression";
        }

        throw Py.SyntaxError(e.getToken(), String.format("can't %s %s", verb, exprName), filename);
    }

    public static void main(String[] args) throws Exception {
//        TestRig rig = new TestRig(new String[]{"org.python.antlr.Python", "file_input", "/tmp/foo.py", "-tree"});
//        rig.process();
        String module = "encodings";
        File src = new File("/tmp/foo.py");
//        byte[] bytes = org.python.core.imp.compileSource(module, src);
//        byte[] bytes = org.python.bootstrap.Import.compileSource(module, new FileInputStream(src), module);

        byte[] bytes = compileSource(module, new FileInputStream(src), module);
        BuildAstVisitor v = new BuildAstVisitor("<string>");

        ANTLRInputStream inputStream = new ANTLRInputStream(new FileInputStream(src));
        PythonLexer lexer = new PythonLexer(inputStream);
        TokenStream tokens = new CommonTokenStream(lexer);
        PythonParser parser = new PythonParser(tokens);
        ParseTree ctx = parser.file_input();
        PythonTree ast = v.visit(ctx);
        new NameMangler().visit(ast);
        new ClassClosureGenerator().visit(ast);
        new Lower("/tmp/foo.py").visit(ast);
        new AnnotationsCreator().visit(ast);
        System.out.println(ast.toStringTree());
        FileOutputStream out = new FileOutputStream("/tmp/foo.class");
        out.write(bytes);
        out.close();
    }

    private static byte[] compileSource(String name, InputStream fp, String filename) {
        try {
            return org.python.bootstrap.Import.compileSource(name, fp, name);
        } catch (Throwable t) {
            throw ParserFacade.fixParseError(t, filename);

        }
    }
}
