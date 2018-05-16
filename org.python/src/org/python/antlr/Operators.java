package org.python.antlr;

import org.python.antlr.ast.boolopType;
import org.python.antlr.ast.cmpopType;
import org.python.antlr.ast.expr_contextType;
import org.python.antlr.ast.operatorType;
import org.python.antlr.ast.unaryopType;
import org.python.core.PyObject;

import java.util.Optional;
import java.util.stream.Stream;

public class Operators {
    public static operatorType valueOfBinOp(PyObject value) {
        return valueOf(operatorType.values(), value).orElse(operatorType.UNDEFINED);
    }
    public static boolopType valueOfBoolOp(PyObject value) {
        return valueOf(boolopType.values(), value).orElse(boolopType.UNDEFINED);
    }
    public static cmpopType valueOfCmpOp(PyObject value) {
        return valueOf(cmpopType.values(), value).orElse(cmpopType.UNDEFINED);
    }
    public static unaryopType valueOfUnaryOp(PyObject value) {
        return valueOf(unaryopType.values(), value).orElse(unaryopType.UNDEFINED);
    }
    public static expr_contextType valueOfContexttype(PyObject value) {
        return valueOf(expr_contextType.values(), value).orElse(expr_contextType.UNDEFINED);
    }

    private static <T extends Operator> Optional<T> valueOf(T[] values, PyObject value) {
        return Stream.of(values).filter(op -> op.getImpl() == value).findFirst();
    }
}
