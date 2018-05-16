package org.python.antlr.adapter;

import org.python.antlr.Operators;
import org.python.antlr.ast.alias;
import org.python.antlr.ast.arg;
import org.python.antlr.ast.arguments;
import org.python.antlr.ast.boolopType;
import org.python.antlr.ast.cmpopType;
import org.python.antlr.ast.comprehension;
import org.python.antlr.ast.expr_contextType;
import org.python.antlr.ast.keyword;
import org.python.antlr.ast.operatorType;
import org.python.antlr.ast.unaryopType;
import org.python.antlr.ast.withitem;
import org.python.antlr.base.excepthandler;
import org.python.antlr.base.expr;
import org.python.antlr.base.slice;
import org.python.antlr.base.stmt;
import org.python.core.Abstract;
import org.python.core.Py;
import org.python.core.PyBytes;
import org.python.core.PyObject;
import org.python.core.PyUnicode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AstAdapter turns Python and Java objects into ast nodes.
 */
public class AstAdapters {
    public final static AstAdapter astAdapter = new AstAdapter();
    public static java.util.List<alias> py2aliasList(PyObject o) {
        return astAdapter.iter2ast(o);
    }

    public static java.util.List<arg> py2argList(PyObject o) {
        return astAdapter.iter2ast(o);
    }

    public static java.util.List<cmpopType> py2cmpopList(PyObject o) {
        return Abstract._PySequence_Stream(o).map(Operators::valueOfCmpOp).collect(Collectors.toList());
    }

    public static java.util.List<comprehension> py2comprehensionList(PyObject o) {
        return astAdapter.iter2ast(o);
    }

    public static java.util.List<excepthandler> py2excepthandlerList(PyObject o) {
        return astAdapter.iter2ast(o);
    }

    public static java.util.List<expr> py2exprList(PyObject o) {
        return astAdapter.iter2ast(o);
    }

    public static java.util.List<String> py2identifierList(PyObject o) {
        return Abstract._PySequence_Stream(o).map(PyObject::asString).collect(Collectors.toList());
    }

    public static java.util.List<keyword> py2keywordList(PyObject o) {
        return astAdapter.iter2ast(o);
    }

    public static java.util.List<slice> py2sliceList(PyObject o) {
        return astAdapter.iter2ast(o);
    }

    public static java.util.List<stmt> py2stmtList(PyObject o) {
        return astAdapter.iter2ast(o);
    }

    public static arg py2arg(PyObject o) {
        return astAdapter.py2ast(o);
    }

    public static expr py2expr(PyObject o) {
        return astAdapter.py2ast(o);
    }

    public static Integer py2int(Object o) {
        if (o == null || o instanceof Integer) {
            return (Integer)o;
        }
        return null;
    }

    public static String py2identifier(PyObject o) {
        return o.asString();
    }

    public static expr_contextType py2expr_context(PyObject o) {
        return Operators.valueOfContexttype(o);
    }

    public static slice py2slice(PyObject o) {
        return astAdapter.py2ast(o);
    }

    public static stmt py2stmt(PyObject o) {
        return astAdapter.py2ast(o);
    }

    public static String py2string(PyObject o) {
        return o.toString();
    }

    public static operatorType py2operator(PyObject o) {
        return Operators.valueOfBinOp(o);
    }

    public static PyObject operator2py(operatorType o) {
        return o.getImpl();
    }

    public static PyObject boolop2py(boolopType o) {
        return o.getImpl();
    }

    public static PyObject unaryop2py(unaryopType o) {
        return o.getImpl();
    }

    public static PyObject expr_context2py(expr_contextType o) {
        return o.getImpl();
    }

    public static boolopType py2boolop(PyObject o) {
        return Operators.valueOfBoolOp(o);
    }

    public static arguments py2arguments(PyObject o) {
        if (o instanceof arguments) {
            return (arguments)o;
        }
        return null;
    }

    public static unaryopType py2unaryop(PyObject o) {
        return Operators.valueOfUnaryOp(o);
    }

    public static List<withitem> py2withitemList(PyObject items) {
        return astAdapter.iter2ast(items);
    }

    public static PyBytes bytes2py(String bytes) {
        return new PyBytes(bytes);
    }

    public static String py2bytes(PyObject obj) {
        return obj.toString();
    }

    public static PyObject string2py(String c) {
        return c == null ? Py.None : new PyUnicode(c);
    }
}
