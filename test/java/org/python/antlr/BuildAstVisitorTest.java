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
        Assert.assertEquals(expectedAst, parse(program).toStringTree());
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
                {"continue", "Continue"},
                {"break", "Break"},
                {"del a", "Delete"},
                {"pass", "Pass"},
                {"import foo.bar as xxx", "Import"},
                {"global a", "Global"},
                {"nonlocal a", "Nonlocal"},
                {"assert True, 'not true'", "Assert"},
//                {"while 1:\n  pass\n\n", "While"},
//                {"for x in a:\n   pass\n\n", "For"},
                {"2 + 3", "BinOp"},
                {"2 - 3", "BinOp"},
                {"2 * 3", "BinOp"},
                {"2 / 3", "BinOp"},
                {"2 % 3", "BinOp"},
                {"2 @ 3", "BinOp"},
                {"2 + 3 * 4", "BinOp"},
                {"2 ** 4", "BinOp"},
                {"foo(1)", "Call"},
                {"foo[1]", "Subscript"},
                {"foo.bar", "Attribute"},
                {"[x, y]", "List"},
                {"(x, y)", "Tuple"},
                {"[x for x in y]", "ListComp"},
                {"(x for x in y)", "GeneratorExp"},
                {"{1,2}", "Set"},
                {"{1, *a}", "Set"},
                {"{x for x in y}", "SetComp"},
                {"{foo: bar}", "Dict"},
                {"{foo: bar, **quzz}", "Dict"},
                {"{foo: bar for foo, bar in a}", "DictComp"},
                {"True and False", "BoolOp"},
                {"a not in b", "Compare"},
                {"a > b", "Compare"},
                {"lambda a: a.split", "Lambda"},
                {"1 if 1 else 0", "IfExp"},
                {"1if 1else 0", "IfExp"},
//                {"if 1:\n  x\nelse:\n  y\n", "IfExp"},
                {"from a import b", "ImportFrom"},
                {"from a import b as c", "ImportFrom"},
                {"from .. import b", "ImportFrom"},
                {"raise a from b", "Raise"},
        });
    }
}
