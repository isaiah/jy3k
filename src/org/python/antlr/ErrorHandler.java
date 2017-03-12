package org.python.antlr;

import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RecognitionException;
import org.python.antlr.base.expr;
import org.python.antlr.base.mod;
import org.python.antlr.base.slice;
import org.python.antlr.base.stmt;

interface ErrorHandler {
    void reportError(Recognizer br, RecognitionException re);
    void recover(Recognizer br, IntStream input, RecognitionException re);
    void recover(Lexer lex, RecognitionException re);

    /**
     * @return True if the caller should handle the mismatch
     */
    boolean mismatch(Recognizer br, IntStream input, int ttype)
        throws RecognitionException;

    /**
     * @return null if the caller should handle the mismatch
     */
    Object recoverFromMismatchedToken(Recognizer br, IntStream input, int ttype)
        throws RecognitionException;

    //expr, mod, slice, stmt
    expr errorExpr(PythonTree t);
    mod errorMod(PythonTree t);
    slice errorSlice(PythonTree t);
    stmt errorStmt(PythonTree t);

    //Exceptions
    void error(String message, PythonTree t);
}
