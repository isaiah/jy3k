package org.python.compiler;

import org.python.antlr.Visitor;
import org.python.antlr.ast.AnnAssign;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.AsyncFor;
import org.python.antlr.ast.AsyncWith;
import org.python.antlr.ast.Block;
import org.python.antlr.ast.Dict;
import org.python.antlr.ast.ExceptHandler;
import org.python.antlr.ast.For;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.If;
import org.python.antlr.ast.Index;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Slice;
import org.python.antlr.ast.Str;
import org.python.antlr.ast.Subscript;
import org.python.antlr.ast.Try;
import org.python.antlr.ast.While;
import org.python.antlr.ast.With;
import org.python.antlr.ast.expr_contextType;
import org.python.antlr.base.excepthandler;
import org.python.antlr.base.slice;
import org.python.antlr.base.stmt;

import java.util.Arrays;
import java.util.List;

/**
 * Add __annotations__ initialization and modification based annotation or direct access
 */
public class AnnotationsCreator extends Visitor {

    @Override
    public Object visitFunctionDef(FunctionDef node) {
        // skip local variable
        return node;
    }

    @Override
    public Object visitAnnAssign(AnnAssign node) {
        if (node.getInternalTarget() instanceof Name) {
            Name anno = new Name(node, "__annotations__", expr_contextType.Load);
            slice val = new Index(node, new Str(node, node.getInternalTarget().getToken().getText()));
            Subscript item = new Subscript(node, anno, val, expr_contextType.Store);
            Assign updateAnno = new Assign(node, Arrays.asList(item), node.getInternalAnnotation());
            node.replaceSelf(node.copy(), updateAnno);
        }
        return node;
    }

    @Override
    public Object visitModule(org.python.antlr.ast.Module node) throws Exception {
        if (findAnno(node.getInternalBody())) {
            Name anno = new Name(node, "__annotations__", expr_contextType.Store);
            Dict value = new Dict(anno, null, null);
            Assign init = new Assign(node, Arrays.asList(anno), value);
            node.setInternalBody(Arrays.asList(init, new Block(node, node.getInternalBody())));
        }
        return super.visitModule(node);
    }

    private boolean findAnno(List<stmt> stmts) {
        boolean res = false;
        for (stmt s: stmts) {
            if (s instanceof AnnAssign) {
                return true;
            } else if (s instanceof For) {
                res = findAnno(((For) s).getInternalBody()) || findAnno(((For) s).getInternalOrelse());
            } else if (s instanceof AsyncFor) {
                res = findAnno(((AsyncFor) s).getInternalBody()) || findAnno(((AsyncFor) s).getInternalOrelse());
            } else if (s instanceof While) {
                res = findAnno(((While) s).getInternalBody()) || findAnno(((While) s).getInternalOrelse());
            } else if (s instanceof If) {
                res = findAnno(((If) s).getInternalBody()) || findAnno(((If) s).getInternalOrelse());
            } else if (s instanceof With) {
                res = findAnno(((With) s).getInternalBody());
            } else if (s instanceof AsyncWith) {
                res = findAnno(((AsyncWith) s).getInternalBody());
            } else if (s instanceof Try) {
                for (excepthandler handler: ((Try) s).getInternalHandlers()) {
                    if (findAnno(((ExceptHandler) handler).getInternalBody())) {
                        return true;
                    }
                }
                res = findAnno(((Try) s).getInternalBody()) || findAnno(((Try) s).getInternalFinalbody()) ||
                        findAnno(((Try) s).getInternalOrelse());
            }
            if (res) break;
        }
        return res;
    }
}
