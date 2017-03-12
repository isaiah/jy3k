package org.python.antlr;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.python.antlr.ast.*;

/**
 * Created by isaiah on 3/10/17.
 */
public class BuildAstVisitor extends PythonBaseVisitor<PythonTree> {
    private GrammarActions actions = new GrammarActions();

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
        }
        if (ctx.IMAG_NUMBER() != null) {
            return new Num(ctx.IMAG_NUMBER(), actions.makeComplex(ctx.IMAG_NUMBER()));
        }
        if (ctx.integer() != null) {
            return visit(ctx.integer());
        }
        return super.visitNumber(ctx);
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
