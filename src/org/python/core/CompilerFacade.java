package org.python.core;

import org.python.antlr.ast.Expr;
import org.python.antlr.ast.Interactive;
import org.python.antlr.base.mod;
import org.python.antlr.base.stmt;
import org.python.compiler.LegacyCompiler;

import java.util.List;

/**
 * Facade for different compiler implementations.
 * 
 * The static methods of this class act as a Facade for the compiler subsystem.
 * This is so that the rest of Jython (even generated code) can statically link
 * to the static interface of this class, while allowing for different
 * implementations of the various components of the compiler subsystem.
 * 
 * @author Tobias Ivarsson
 */
public class CompilerFacade {
    
    private static volatile PythonCompiler compiler = loadDefaultCompiler();

    public static void setCompiler(PythonCompiler compiler) {
        CompilerFacade.compiler = compiler;
    }

    private static PythonCompiler loadDefaultCompiler() {
        return new LegacyCompiler();
    }

    public static PyCode compile(mod node, String name, String filename,
            boolean linenumbers, boolean printResults, CompilerFlags cflags) {
        if (printResults) {
            List<stmt> stmts = ((Interactive) node).getInternalBody();
            if (!stmts.isEmpty() && stmts.get(0) instanceof Expr) {
                ((Expr) stmts.get(0)).setPrint(true);
            }
        }
        try {
            PythonCodeBundle bundle = compiler.compile(node, name, filename,
                    linenumbers, cflags);
            return bundle.loadCode();
        } catch (Throwable t) {
            throw ParserFacade.fixParseError(null, t, filename);
        }
    }
}
