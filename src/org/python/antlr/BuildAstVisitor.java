package org.python.antlr;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.python.antlr.ast.*;
import org.python.antlr.base.expr;
import org.python.antlr.base.slice;
import org.python.antlr.base.stmt;

import java.util.ArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by isaiah on 3/10/17.
 */
public class BuildAstVisitor extends PythonBaseVisitor<PythonTree> {
    private GrammarActions actions = new GrammarActions();
    private expr_contextType exprContextType = expr_contextType.Load;


    @Override
    public PythonTree visitArgument(PythonParser.ArgumentContext ctx) {
        if (ctx.ASSIGN() != null) {
            return new keyword(ctx.getStart(), ctx.key.getText(), (expr) visit(ctx.val));
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
        return new Slice(ctx.getStart(), (expr) visit(ctx.lower), (expr) visit(ctx.upper), (expr) visit(ctx.sliceop()));
    }

    @Override
    public PythonTree visitSliceop(PythonParser.SliceopContext ctx) {
        return visit(ctx.test());
    }

    @Override
    public PythonTree visitPower(PythonParser.PowerContext ctx) {
        expr left = (expr) visit(ctx.atom());
        for (PythonParser.TrailerContext trailerCtx : ctx.trailer()) {
            if (trailerCtx.OPEN_PAREN() != null) {
                ArglistResult arglistResult = visit_Arglist(trailerCtx.arglist());
                left = new Call(ctx.getStart(), left, arglistResult.args, arglistResult.keywords);
            } else if (trailerCtx.OPEN_BRACK() != null) {
                left = new Subscript(ctx.getStart(), left, visit_Subscriptlist(trailerCtx.subscriptlist()), expr_contextType.Load);
            } else if (trailerCtx.DOT() != null) {
                left = new Attribute(ctx.getStart(), left, trailerCtx.NAME().getText(), expr_contextType.Load);
            }
        }
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
            operatorType op = ctx.ops.get(i).getType() == PythonLexer.MINUS ? operatorType.Sub : operatorType.Add;
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
    public PythonTree visitFor_stmt(PythonParser.For_stmtContext ctx) {
        return new For(ctx.getStart(), (expr) visit(ctx.exprlist()), (expr) visitTestlist(ctx.testlist()), visit_Suite(ctx.s1), visit_Suite(ctx.s2));
    }

    @Override
    public PythonTree visitWhile_stmt(PythonParser.While_stmtContext ctx) {
        return new While(ctx.getStart(), (expr) visit(ctx.test()), visit_Suite(ctx.s1), visit_Suite(ctx.s2));
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
    public PythonTree visitTfpdef(PythonParser.TfpdefContext ctx) {
        return new arg(ctx.getStart(), ctx.NAME().getText(), (expr) visit(ctx.test()));
    }

    @Override
    public PythonTree visitInteger(PythonParser.IntegerContext ctx) {
        if (ctx.DECIMAL_INTEGER() != null) {
            return new Num(ctx.DECIMAL_INTEGER(), actions.makeDecimal(ctx.DECIMAL_INTEGER()));
        }
        if (ctx.HEX_INTEGER() != null) {
            return new Num(ctx.HEX_INTEGER(), actions.makeInt(ctx.HEX_INTEGER(), 16));
        }
        if (ctx.BIN_INTEGER() != null)
            return new Num(ctx.BIN_INTEGER(), actions.makeInt(ctx.BIN_INTEGER(), 2));
        if (ctx.OCT_INTEGER() != null)
            return new Num(ctx.OCT_INTEGER(), actions.makeInt(ctx.OCT_INTEGER(), 8));
        return super.visitInteger(ctx);
    }

    @Override
    public PythonTree visitNumber(PythonParser.NumberContext ctx) {
        if (ctx.FLOAT_NUMBER() != null) {
            return new Num(ctx.FLOAT_NUMBER(), actions.makeFloat(ctx.FLOAT_NUMBER()));
        } else if (ctx.IMAG_NUMBER() != null) {
            return new Num(ctx.IMAG_NUMBER(), actions.makeComplex(ctx.IMAG_NUMBER()));
        } else if (ctx.integer() != null) {
            return visit(ctx.integer());
        }
        return super.visitNumber(ctx);
    }

    class Testlist_compResult {
        java.util.List<expr> exprs;
        java.util.List<comprehension> comps;

        public Testlist_compResult() {
            exprs = new ArrayList<>();
        }
    }

    private Testlist_compResult visit_Testlist_comp(PythonParser.Testlist_compContext ctx) {
        if (ctx == null) return null;
        Testlist_compResult ret = new Testlist_compResult();
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

    @Override
    public PythonTree visitDictorsetmaker(PythonParser.DictorsetmakerContext ctx) {
        if (!ctx.COLON().isEmpty()) {
            if (ctx.comp_for() != null) {
                /** Dict comprehension */
                expr key = (expr) visit(ctx.test(0));
                expr val = (expr) visit(ctx.test(1));
                return new DictComp(ctx.getStart(), key, val, visit_Comp_for(ctx.comp_for()));
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
            return new SetComp(ctx.getStart(), elt, visit_Comp_for(ctx.comp_for()));
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
            if (testlistCompResult.comps != null) {
                return new GeneratorExp(ctx.getStart(), testlistCompResult.exprs.get(0), testlistCompResult.comps);
            }
            return new Tuple(ctx.getStart(), testlistCompResult.exprs, exprContextType);
        } else if (ctx.OPEN_BRACK() != null) {
            if (testlistCompResult.comps != null) {
                return new ListComp(ctx.getStart(), testlistCompResult.exprs.get(0), testlistCompResult.comps);
            }
            return new List(ctx.getStart(), testlistCompResult.exprs, exprContextType);
        } else if (ctx.OPEN_BRACE() != null) {
            return visit(ctx.dictorsetmaker());
        } else if (ctx.number() != null) {
            return visit(ctx.number());
        } else if (ctx.yield_expr() != null) {
            return visit(ctx.yield_expr());
        } else if (ctx.NAME() != null) {
            return new Name(ctx.NAME(), ctx.getText(), expr_contextType.Load);
        } else if (ctx.ellipsis != null) {
            return new Ellipsis(ctx.ellipsis);
        } else if (ctx.TRUE() != null) {
            return new NameConstant(ctx.TRUE(), ctx.getText());
        } else if (ctx.FALSE() != null) {
            return new NameConstant(ctx.FALSE(), ctx.getText());
        } else if (ctx.NONE() != null) {
            return new NameConstant(ctx.NONE(), ctx.getText());
        } else if (ctx.str() != null) {
            return actions.parsestrplus(ctx.str());
        }
        return super.visitAtom(ctx);
    }

    @Override
    public PythonTree visitImport_name(PythonParser.Import_nameContext ctx) {
        return new Import(ctx.getStart(), visit_Dotted_as_names(ctx.dotted_as_names()));
    }

    @Override
    public PythonTree visitDotted_as_name(PythonParser.Dotted_as_nameContext ctx) {
        return new alias(visit(ctx.dotted_name()), actions.makeNameNode(ctx.NAME()));
    }

    @Override
    public PythonTree visitDel_stmt(PythonParser.Del_stmtContext ctx) {
        return withExprContextType(expr_contextType.Del, () ->
                new Delete(ctx.getStart(), visit_Exprlist(ctx.exprlist()))
        );
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
    public PythonTree visitYield_stmt(PythonParser.Yield_stmtContext ctx) {
        return visit(ctx.yield_expr());
    }

    @Override
    public PythonTree visitYield_expr(PythonParser.Yield_exprContext ctx) {
        PythonParser.Yield_argContext arg = ctx.yield_arg();
        if (arg != null && arg.FROM() != null) {
            return new YieldFrom(ctx.getStart(), (expr) visit(arg));
        } else {
            return new Yield(ctx.getStart(), (expr) visit(arg));
        }
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
     * Temporarily change exprContextType
     */
    private PythonTree withExprContextType(expr_contextType contextType, Supplier<PythonTree> handle) {
        expr_contextType oldContextType = exprContextType;
        exprContextType = contextType;
        PythonTree ret = handle.get();
        exprContextType = oldContextType;
        return ret;
    }

    /**
     * helper method
     */
    private java.util.List<expr> visit_Exprlist(PythonParser.ExprlistContext ctx) {
        java.util.List<expr> elts = new ArrayList<>();
        for (PythonParser.Star_exprContext starExpr : ctx.star_expr()) {
            elts.add((expr) visit(starExpr));
        }
        return elts;
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
        java.util.List<stmt> suite = new ArrayList<>();
        if (ctx != null && ctx.simple_stmt() != null) {
            return visit_Simple_stmt(ctx.simple_stmt());
        }
        return suite;
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
        expr iter = (expr) visit(ctx.or_test());
        if (ctx.comp_iter() != null) {
            Comp_iterResult iterRet = visit_Comp_iter(ctx.comp_iter());
            java.util.List<expr> ifs = new ArrayList<>();
            for (; iterRet.iter != null; iterRet = iterRet.iter) {
                if (iterRet.ifs != null) {
                    ifs.add(iterRet.ifs);
                }
                if (iterRet.comps != null) {
                    ret.addAll(iterRet.comps);
                }
            }
            ret.add(new comprehension(ctx.getStart(), target, iter, ifs));
        }
        return ret;
    }

    private ArglistResult visit_Arglist(PythonParser.ArglistContext ctx) {
        ArglistResult ret = new ArglistResult();
        for (PythonParser.ArgumentContext argCtx : ctx.argument()) {
            PythonTree arg = visit(argCtx);
            if (arg instanceof keyword) {
                ret.keywords.add((keyword) arg);
            } else {
                ret.args.add((expr) arg);
            }
        }
        return ret;
    }

    private slice visit_Subscriptlist(PythonParser.SubscriptlistContext ctx) {
        if (ctx.subscript().size() > 1) {
            java.util.List<slice> dims = new ArrayList<>(ctx.subscript().size());
            for (PythonParser.SubscriptContext subscriptContext : ctx.subscript()) {
                dims.add((slice) visitSubscript(subscriptContext));
            }
            return new ExtSlice(ctx.getStart(), dims);
        }
        return (slice) visit(ctx.subscript(0));
    }

    public static void main(String[] args) {
        String program = "1.1";
        BuildAstVisitor v = new BuildAstVisitor();

        ANTLRInputStream inputStream = new ANTLRInputStream(program);
        PythonLexer lexer = new PythonLexer(inputStream);
        TokenStream tokens = new CommonTokenStream(lexer);
        PythonParser parser = new PythonParser(tokens);
        ParseTree ctx = parser.single_input();
        PythonTree ast = v.visit(ctx);
        System.out.println(ast);
    }
}
