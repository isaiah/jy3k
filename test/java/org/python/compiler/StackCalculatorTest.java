package org.python.compiler;

import junit.framework.TestCase;
import org.junit.Test;
import org.python.antlr.PythonTree;

/**
 * Created by isaiah on 12.05.17.
 */
public class StackCalculatorTest extends TestCase {

    @Test
    public void testCalculateYieldStackAsTupleElement() throws Exception {
        String program = "def g(): 1, (yield 1)\n";
        String expected = "foo";
        PythonTree ast = CompilerUtil.parse(program, "single");
        new StackCalculator().visit(ast);
        assertEquals(expected, ast.toStringTree());
    }
}
