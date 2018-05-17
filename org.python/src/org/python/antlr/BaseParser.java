package org.python.antlr;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.python.antlr.base.mod;
import org.python.core.Py;
import org.python.internal.runtime.Timing;

public class BaseParser {

    protected final CharStream charStream;
    @Deprecated
    protected final boolean partial;
    protected final String filename;
    protected final String encoding;
//    protected ErrorHandler errorHandler = new FailFastHandler();
    
    public BaseParser(CharStream stream, String filename, String encoding) {
        this(stream, filename, encoding, false);
    }
    
    @Deprecated
    public BaseParser(CharStream stream, String filename, String encoding, boolean partial) {
        this.charStream = stream;
        this.filename = filename;
        this.encoding = encoding;
        this.partial = partial;
    }

    protected PythonParser setupParser(boolean single) {
        PythonLexer lexer = new PythonLexer(charStream);
        lexer.single = single;
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PythonParser parser = new PythonParser(tokens);
        parser.removeErrorListeners();
        parser.setErrorHandler(new BailErrorStrategy());
        return parser;
    }

    public mod parseExpression() {
        PythonParser parser = setupParser(false);
        PythonParser.Eval_inputContext r = parser.eval_input();
        mod tree = (mod) new BuildAstVisitor(filename).visit(r);
        return tree;
    }

    public mod parseInteractive() {
        PythonParser parser = setupParser(true);
        PythonParser.Single_inputContext r = parser.single_input();
        mod tree = (mod) new BuildAstVisitor(filename).visit(r);
        return tree;
    }

    public mod parseModule() {
        long startTime = System.nanoTime();
        PythonParser parser = setupParser(false);
        PythonParser.File_inputContext r = parser.file_input();
        long parseEndTime = System.nanoTime();
        mod tree =  (mod) new BuildAstVisitor(filename).visit(r);
        long endTime = System.nanoTime();
        Timing timing = Py.getThreadState()._timing;
        timing.accumulateTime("Generate CST", parseEndTime - startTime);
        timing.accumulateTime("Convert CST to AST", endTime - parseEndTime);
        return tree;
    }
}
