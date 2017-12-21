package org.python.compiler;

import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.Block;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Interactive;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Return;
import org.python.antlr.ast.Starred;
import org.python.antlr.ast.Suite;
import org.python.antlr.ast.arg;
import org.python.antlr.ast.arguments;
import org.python.antlr.ast.expr_contextType;
import org.python.antlr.ast.keyword;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;
import org.python.core.PyList;
import org.python.core.PyObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Create implicit __class__ closure reference if any methods in a class body refer to either __class__ or super.
 * see PEP-3135 New Super
 */
public class ClassClosureGenerator extends Visitor {
    private Deque<Boolean> inClass = new LinkedList<>();
    private Deque<Boolean> needsClosure = new LinkedList<>();

    @Override
    public Object visitClassDef(ClassDef node) {
        inClass.push(true);
        needsClosure.push(false);
        traverse(node);
        inClass.pop();
        boolean thisNeedClosure = needsClosure.pop();
        if (thisNeedClosure) {
            node.setNeedsClassClosure(thisNeedClosure);
        }
        return null;
    }

    @Override
    public Object visitName(Name node) {
        if (!inClass.isEmpty() && inClass.peek() && !needsClosure.peek()) {
            String name = node.getInternalId();
            if (name.equals("__class__") || name.equals("super")) {
                needsClosure.pop();
                needsClosure.push(true);
            }
        }
        return node;
    }
}
