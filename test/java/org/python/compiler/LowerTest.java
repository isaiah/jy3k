package org.python.compiler;

import junit.framework.TestCase;
import org.junit.Test;
import org.python.antlr.PythonTree;

public class LowerTest extends TestCase {
    private static final String program =
                    "def foo():\n" +
                    "  try:\n" +
                    "    a = 1\n" +
                    "    return bar(a)\n" +
                    "  finally:\n" +
                    "    a = 3\n";

    @Test
    public void testLowerTryFinally() throws Exception {
        String expected = "foo";
        PythonTree ast = CompilerUtil.parse(program, "single");
        new Lower("<string>").visit(ast);
        assertEquals(expected, ast.toStringTree());
    }

    @Test
    public void testLowerGeneratorExpr() throws Exception {
        String expected = "foo";
        String genexp = "list((i,j) for i in range(4) for j in range(i) )";
        PythonTree ast;// = CompilerUtil.parse(genexp, "file");
//        new Lower().visit(ast);
//        assertEquals(expected, ast.toStringTree());

        genexp = "if (any(\"__next__\" in B.__dict__ for B in C.__mro__):\n  pass";
        ast = CompilerUtil.parse(genexp, "file");
        new Lower("<string>").visit(ast);
        assertEquals(expected, ast.toStringTree());
    }
}
