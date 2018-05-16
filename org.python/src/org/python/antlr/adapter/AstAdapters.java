package org.python.antlr.adapter;

import org.python.antlr.Operators;
import org.python.antlr.ast.*;
import org.python.antlr.base.*;
import org.python.antlr.op.*;
import org.python.core.*;

import java.util.List;

/**
 * AstAdapter turns Python and Java objects into ast nodes.
 */
public class AstAdapters {
    public final static AliasAdapter aliasAdapter = new AliasAdapter();
    public final static ArgAdapter argAdapter = new ArgAdapter();
    public final static CmpopAdapter cmpopAdapter = new CmpopAdapter();
    public final static ComprehensionAdapter comprehensionAdapter = new ComprehensionAdapter();
    public final static ExcepthandlerAdapter excepthandlerAdapter = new ExcepthandlerAdapter();
    public final static ExprAdapter exprAdapter = new ExprAdapter();
    public final static IdentifierAdapter identifierAdapter = new IdentifierAdapter();
    public final static KeywordAdapter keywordAdapter = new KeywordAdapter();
    public final static SliceAdapter sliceAdapter = new SliceAdapter();
    public final static StmtAdapter stmtAdapter = new StmtAdapter();
    public static AstAdapter withitemAdapter = new WithitemAdapter();

    public static java.util.List<alias> py2aliasList(PyObject o) {
        return (java.util.List<alias>)aliasAdapter.iter2ast(o);
    }

    public static java.util.List<arg> py2argList(PyObject o) {
        return (java.util.List<arg>)argAdapter.iter2ast(o);
    }

    public static java.util.List<cmpopType> py2cmpopList(PyObject o) {
        return (java.util.List<cmpopType>)cmpopAdapter.iter2ast(o);
    }

    public static java.util.List<comprehension> py2comprehensionList(PyObject o) {
        return (java.util.List<comprehension>)comprehensionAdapter.iter2ast(o);
    }

    public static java.util.List<excepthandler> py2excepthandlerList(PyObject o) {
        return (java.util.List<excepthandler>)excepthandlerAdapter.iter2ast(o);
    }

    public static java.util.List<expr> py2exprList(PyObject o) {
        return (java.util.List<expr>)exprAdapter.iter2ast(o);
    }

    public static java.util.List<String> py2identifierList(PyObject o) {
        return (java.util.List<String>)identifierAdapter.iter2ast(o);
    }

    public static java.util.List<keyword> py2keywordList(PyObject o) {
        return (java.util.List<keyword>)keywordAdapter.iter2ast(o);
    }

    public static java.util.List<slice> py2sliceList(PyObject o) {
        return (java.util.List<slice>)sliceAdapter.iter2ast(o);
    }

    public static java.util.List<stmt> py2stmtList(PyObject o) {
        return (java.util.List<stmt>)stmtAdapter.iter2ast(o);
    }

    public static arg py2arg(PyObject o) {
        return (arg) argAdapter.py2ast(o);
    }

    public static expr py2expr(PyObject o) {
        return (expr)exprAdapter.py2ast(o);
    }

    public static Integer py2int(Object o) {
        if (o == null || o instanceof Integer) {
            return (Integer)o;
        }
        return null;
    }

    public static String py2identifier(PyObject o) {
        return (String)identifierAdapter.py2ast(o);
    }

    public static expr_contextType py2expr_context(PyObject o) {
        return Operators.valueOfContexttype(o);
    }

    public static slice py2slice(PyObject o) {
        return (slice)sliceAdapter.py2ast(o);
    }

    public static stmt py2stmt(PyObject o) {
        return (stmt)stmtAdapter.py2ast(o);
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

    public static PyObject cmpop2py(cmpopType o) {
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

    //XXX: clearly this isn't necessary -- need to adjust the code generation.
    public static Object py2object(Object o) {
        return o;
    }

    public static Boolean py2bool(Object o) {
        if (o instanceof Boolean) {
            return (Boolean)o;
        }
        return null;
    }

    public static unaryopType py2unaryop(PyObject o) {
        return Operators.valueOfUnaryOp(o);
    }

    public static List<withitem> py2withitemList(PyObject items) {
        return (java.util.List<withitem>)withitemAdapter.iter2ast(items);
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
