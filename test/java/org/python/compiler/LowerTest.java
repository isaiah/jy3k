package org.python.compiler;

import junit.framework.TestCase;
import org.junit.Test;
import org.python.antlr.PythonTree;

public class LowerTest extends TestCase {
    private static final String program =
                    "def foo():\n" +
                    "  try:\n" +
                    "    a = 1\n" +
                    "    return a\n" +
                    "  except:\n" +
                    "    return 2\n" +
                    "  finally:\n" +
                    "    a = 3\n";

    @Test
    public void testLowerTryFinally() throws Exception {
        String expected = "foo";
        PythonTree ast = CompilerUtil.parse(program, "single");
        new Lower().visit(ast);
        assertEquals(expected, ast.toStringTree());
    }
}
