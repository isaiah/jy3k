package org.python.compiler;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.python.antlr.BuildAstVisitor;
import org.python.antlr.PythonLexer;
import org.python.antlr.PythonParser;
import org.python.antlr.PythonTree;
import org.python.antlr.PythonVisitor;
import org.python.antlr.ast.Expr;
import org.python.antlr.ast.Interactive;

/**
 * Created by isaiah on 4/12/17.
 */
public class CompilerUtil {
    public static PythonTree parse(String program, String mode) {
        ANTLRInputStream inputStream = new ANTLRInputStream(program + "\n");
        PythonLexer lexer = new PythonLexer(inputStream);
        lexer.single = mode.equals("single");
        TokenStream tokens = new CommonTokenStream(lexer);
        PythonParser parser = new PythonParser(tokens);
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
//        parser.setErrorHandler(new BailErrorStrategy());
        ParseTree ctx;
        PythonVisitor<PythonTree> visitor = new BuildAstVisitor();
        switch(mode) {
            case "single":
                ctx = parser.single_input();
                Interactive root = (Interactive) visitor.visit(ctx);
                PythonTree expr = root.getInternalBody().get(0);
                if (expr instanceof Expr) {
                    return ((Expr) expr).getInternalValue();
                }
                return expr;
            case "file":
                return visitor.visit(parser.file_input());
            default:
                return visitor.visit(parser.eval_input());
        }
    }
}
