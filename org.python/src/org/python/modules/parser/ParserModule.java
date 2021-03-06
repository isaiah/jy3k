package org.python.modules.parser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.python.annotations.ModuleInit;
import org.python.antlr.PythonLexer;
import org.python.antlr.PythonParser;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;
import org.python.core.PyStringMap;
import org.python.core.PyUnicode;

@ExposedModule(name = "parser")
public class ParserModule {
    /** _csv.Error exception. */
    public static final PyObject Error = Py.makeClass("ParserError", exceptionNamespace(), Py.Exception);

    public static PyException ParserError(String message) {
        return new PyException(Error, message);
    }

    private static PyObject exceptionNamespace() {
        PyObject dict = new PyStringMap();
        dict.__setitem__("__module__", new PyUnicode("parser"));
        return dict;
    }

    @ModuleInit
    public static void init(PyObject dict) {
        dict.__setitem__("ParserError", Error);
    }

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
        CharStream input = CharStreams.fromString(s);
        PythonLexer lexer = new PythonLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new PythonParser(tokens);
    }
}
