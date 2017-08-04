package org.python.modules.parser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.python.antlr.PythonLexer;
import org.python.antlr.PythonParser;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;

/**
 * Created by isaiah on 3/22/17.
 */
@ExposedModule(name = "parser")
public class ParserModule {

    @ExposedFunction
    public static PySTType expr(PyObject string) {
        PythonParser parser = setupParser(string.toString());
        return new PySTType(parser.eval_input());
    }

    @ExposedFunction
    public static PySTType suite(PyObject string) {
        PythonParser parser = setupParser(string.toString());
        return new PySTType(parser.file_input());
    }

    @ExposedFunction
    public static PyObject isexpr(PyObject st) {
        if (st instanceof PySTType) {
            return ((PySTType) st).st_isexpr();
        }
        throw Py.TypeError("isexpr() argument 1 must be parser.st, not " + st.getType().fastGetName());
    }

    @ExposedFunction
    public static PyObject issuite(PyObject st) {
        if (st instanceof PySTType) {
            return ((PySTType) st).st_issuite();
        }
        throw Py.TypeError("issuite() argument 1 must be parser.st, not " + st.getType().fastGetName());
    }

    @ExposedFunction
    public static PyObject compilest(PyObject st, PyObject filename) {
        if (!(st instanceof PySTType)) {
            throw Py.TypeError("issuite() argument 1 must be parser.st, not " + st.getType().fastGetName());
        }
        return ((PySTType) st).st_compile(filename);
    }

    private static PythonParser setupParser(String s) {
        ANTLRInputStream input = new ANTLRInputStream(s);
        PythonLexer lexer = new PythonLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new PythonParser(tokens);
    }
}
