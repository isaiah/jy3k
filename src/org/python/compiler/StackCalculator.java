package org.python.compiler;

import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.BinOp;
import org.python.antlr.ast.Tuple;
import org.python.antlr.ast.UnaryOp;
import org.python.antlr.ast.Yield;
import org.python.antlr.base.expr;
import org.python.core.PyObject;

import java.util.Deque;
import java.util.LinkedList;

import static org.python.util.CodegenUtils.p;

/**
 * Calculate operand stack information before yield call, so that we can restore the stack afterwards
 */
public class StackCalculator extends Visitor {
    Deque<String> stack;

    public StackCalculator() {
        stack = new LinkedList<>();
    }

    @Override
    public Object visitBinOp(BinOp node) throws Exception {
        traverse(node);
        consume(2);
        return node;
    }

    @Override
    public Object visitUnaryOp(UnaryOp node) throws Exception {
        traverse(node);
        consume();
        return node;
    }

    @Override
    public Object visitYield(Yield node) throws Exception {
        traverse(node);
        consume();
        node.stack = stack.toArray(new String[0]);
        return node;
    }

    @Override
    public Object visitTuple(Tuple node) throws Exception {
        produce(p(PyObject[].class)); // temporary variable to hold the array
        traverse(node);
        consume(node.getInternalElts().size());
        return node;
    }

    @Override
    public void traverse(PythonTree node) throws Exception {
        if (node instanceof expr) {
            produce();
        }
        super.traverse(node);
    }

    private void consume(int n) {
        for (int i = 0; i < n; i++) stack.pop();
    }

    private void consume() {
        stack.pop();
    }

    private void produce() {
        produce(p(PyObject.class));
    }

    private void produce(String sig) {
        stack.push(sig);
    }
}
