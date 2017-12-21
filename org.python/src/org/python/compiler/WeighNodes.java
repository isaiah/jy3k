package org.python.compiler;

import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.AsyncFor;
import org.python.antlr.ast.AsyncWith;
import org.python.antlr.ast.DictComp;
import org.python.antlr.ast.For;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.GeneratorExp;
import org.python.antlr.ast.ListComp;
import org.python.antlr.ast.SetComp;
import org.python.antlr.ast.Try;
import org.python.antlr.ast.VisitorBase;
import org.python.antlr.ast.With;

import java.util.Map;

/**
 * Computes the "byte code" weight of an AST segment. This is used
 * for Splitting too large class files
 */
public class WeighNodes extends Visitor {
    /*
     * Weight constants.
     */
    static final long FUNCTION_WEIGHT  = 40;
    static final long AASTORE_WEIGHT   =  2;
    static final long ACCESS_WEIGHT    =  4;
    static final long ADD_WEIGHT       = 10;
    static final long BREAK_WEIGHT     =  1;
    static final long CALL_WEIGHT      = 10;
    static final long CATCH_WEIGHT     = 10;
    static final long COMPARE_WEIGHT   =  6;
    static final long CONST_WEIGHT     =  2;
    static final long CONTINUE_WEIGHT  =  1;
    static final long IF_WEIGHT        =  2;
    static final long LITERAL_WEIGHT   = 10;
    static final long LOOP_WEIGHT      =  4;
    static final long NEW_WEIGHT       =  6;
    static final long RETURN_WEIGHT    =  2;
    static final long SWITCH_WEIGHT    =  8;
    static final long THROW_WEIGHT     =  2;
    static final long VAR_WEIGHT       = 40;
    static final long WITH_WEIGHT      = 30;
    static final long OBJECT_WEIGHT    = 16;
    static final long SETPROP_WEIGHT   = 5;
    static final long FOR_WEIGHT       = 20;
    static final long TRY_WEIGHT       = 20;
    static final long GENEXP_WEIGHT    = 20;
    static final long COMP_WEIGHT      = 20;
    static final long ASSIGN_WEIGHT    = 2;

    /** Accumulated weight. */
    private long weight;

    /** Optional cache for weight of block nodes. */
    private final Map<PythonTree, Long> weightCache;

    private final FunctionDef topFunction;

    public WeighNodes(FunctionDef topFunction, Map<PythonTree, Long> weightCache) {
        this.weightCache = weightCache;
        this.topFunction = topFunction;
    }

    static long weigh(final PythonTree node) {
        return weigh(node, null);
    }

    static long weigh(final PythonTree node, final Map<PythonTree, Long> weightCache) {
        final WeighNodes weighNodes = new WeighNodes(node instanceof FunctionDef ? (FunctionDef)node : null, weightCache);
        try {
            node.accept(weighNodes);
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return weighNodes.weight;
    }

    @Override
    public Object visitAsyncWith(AsyncWith node) {
        weight += WITH_WEIGHT;
        return node;
    }

    @Override
    public Object visitWith(With node) {
        weight += WITH_WEIGHT;
        return node;
    }

    @Override
    public Object visitAsyncFor(AsyncFor node) {
        weight += FOR_WEIGHT;
        return node;
    }

    @Override
    public Object visitFor(For node) {
        weight += FOR_WEIGHT;
        return node;
    }

    @Override
    public Object visitTry(Try node) {
        weight += TRY_WEIGHT;
        return node;
    }

    @Override
    public Object visitGeneratorExp(GeneratorExp node) {
        weight += GENEXP_WEIGHT;
        return node;
    }

    @Override
    public Object visitDictComp(DictComp node) {
        weight += COMP_WEIGHT;
        return node;
    }

    @Override
    public Object visitListComp(ListComp node) {
        weight += COMP_WEIGHT;
        return node;
    }

    @Override
    public Object visitSetComp(SetComp node) {
        weight += COMP_WEIGHT;
        return node;
    }

    @Override
    public Object visitAssign(Assign node) {
        weight += ASSIGN_WEIGHT;
        return node;
    }
}
