package org.python.antlr;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.python.antlr.ast.*;
import org.python.antlr.base.expr;

import java.util.ArrayList;

/**
 * Created by isaiah on 3/10/17.
 */
public class BuildAstVisitor extends PythonBaseVisitor<PythonTree> {
    private GrammarActions actions = new GrammarActions();
    private expr_contextType exprContextType = expr_contextType.Load;

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

    @Override
    public PythonTree visitAtom(PythonParser.AtomContext ctx) {
        if (ctx.number() != null) {
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

    public java.util.List<expr> visitExpr_list(PythonParser.ExprlistContext ctx) {
        java.util.List<expr> elts = new ArrayList<>();
        for (PythonParser.Star_exprContext starExpr : ctx.star_expr()) {
            elts.add((expr) visit(starExpr));
        }
        return elts;
    }

    interface Handle {
        PythonTree visit();
    }

    @Override
    public PythonTree visitDel_stmt(PythonParser.Del_stmtContext ctx) {
        return withExprContextType(expr_contextType.Del, () ->
            new Delete(ctx.getStart(), visitExpr_list(ctx.exprlist()))
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

    /** Temporarily change exprContextType */
    private PythonTree withExprContextType(expr_contextType contextType, Handle handle) {
        expr_contextType oldContextType = exprContextType;
        exprContextType = contextType;
        PythonTree ret = handle.visit();
        exprContextType = oldContextType;
        return ret;
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
