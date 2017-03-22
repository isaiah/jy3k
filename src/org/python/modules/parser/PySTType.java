package org.python.modules.parser;

import org.antlr.v4.runtime.ParserRuleContext;
import org.python.antlr.BuildAstVisitor;
import org.python.antlr.PythonParser;
import org.python.antlr.base.mod;
import org.python.compiler.Module;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedType;

/**
 * Created by isaiah on 3/22/17.
 */
@ExposedType(name = "parser.st")
public class PySTType extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PySTType.class);

    private ParserRuleContext parseTree;

    public PySTType(ParserRuleContext tree) {
        super(TYPE);
        parseTree = tree;
    }

    @ExposedMethod
    public PyObject st_tolist() {
        return new PyUnicode(parseTree.toStringTree());
    }

    @ExposedMethod
    public PyObject st_totuple() {
        return new PyUnicode(parseTree.toStringTree());
    }

    @ExposedMethod
    public PyObject st_isexpr() {
        return Py.newBoolean(parseTree instanceof PythonParser.Eval_inputContext);
    }

    @ExposedMethod
    public PyObject st_issuite() {
        return Py.newBoolean(parseTree instanceof PythonParser.File_inputContext);
    }

    /** TODO */
    @ExposedMethod
    public PyObject st_compile(PyObject filename) {
//        BuildAstVisitor astBuilder = new BuildAstVisitor();
//        mod m = (mod) astBuilder.visit(parseTree);
//        return Module.codeConstant(m, filename);
        return Py.None;
    }
}
