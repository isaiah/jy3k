package org.python.antlr;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by isaiah on 3/11/17.
 */
@RunWith(Parameterized.class)
public class BuildAstVisitorTest {

    public PythonTree parse(String program) {
        ANTLRInputStream inputStream = new ANTLRInputStream(program);
        PythonLexer lexer = new PythonLexer(inputStream);
        TokenStream tokens = new CommonTokenStream(lexer);
        PythonParser parser = new PythonParser(tokens);
        ParseTree ctx = parser.single_input();
        return new BuildAstVisitor().visit(ctx);
    }

    private String program;
    private String expectedAst;

    public BuildAstVisitorTest(String program, String ast) {
        this.program = program;
        this.expectedAst = ast;
    }

    @Test
    public void testBuildAst() {
        Assert.assertEquals(expectedAst, parse(program).toString());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"1", "Num"},
                {"0x11", "Num"},
                {"1.1", "Num"},
                {"2.1j", "Num"},
                {"...", "Ellipsis"},
                {"None", "NameConstant"},
                {"True", "NameConstant"},
                {"False", "NameConstant"},
                {"a", "Name"},
                {"'str'", "Str"},
                {"yield a", "Yield"},
                {"yield from a", "YieldFrom"},
                {"return", "Return"},
                {"return a", "Return"},
        });
    }
}
