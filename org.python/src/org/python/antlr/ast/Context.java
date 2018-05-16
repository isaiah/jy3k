package org.python.antlr.ast;

public interface Context {
    void setContext(expr_contextType ctx);
    expr_contextType getContext();
}
