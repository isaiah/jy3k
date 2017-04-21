package org.python.compiler;

import junit.framework.TestCase;
import org.junit.Test;
import org.python.antlr.PythonTree;

import java.util.HashMap;

/**
 * Created by isaiah on 4/12/17.
 */
public class SplitterTest extends TestCase {

    private static final String program = "def foo():\n  a = 1\n  b = 2\n  return a + b\n";

    @Test
    public void testSplitBody() throws Exception {
        String expected = "foo";
        PythonTree ast = CompilerUtil.parse(program, "file");
        new Splitter().visit(ast);
        new SplitIntoFunctions(new HashMap<>()).visit(ast);
        assertEquals(expected, ast.toStringTree());
    }
}
