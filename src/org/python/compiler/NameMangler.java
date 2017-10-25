package org.python.compiler;

import org.python.antlr.Visitor;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.arg;
import org.python.antlr.ast.arguments;
import org.python.antlr.base.stmt;

/**
 * Mangle identifiers prefixed with leading underscores
 * https://docs.python.org/3.7/tutorial/classes.html#private-variables
 */
public class NameMangler extends Visitor {
    @Override
    public Object visitClassDef(ClassDef classDef) {
        String clsName = classDef.getInternalName();
        String prefix;
        if (clsName.startsWith("_")) {
            prefix = clsName;
        } else {
            prefix ="_" + clsName;
        }
        Visitor visitor = new Visitor() {
            @Override
            public Object visitAttribute(Attribute node) {
                String name = node.getInternalAttr();
                if (name.startsWith("__") && !name.endsWith("__")) {
                    node.setInternalAttr(prefix + name);
                }
                return super.visitAttribute(node);
            }

            @Override
            public Object visitName(Name node) {
                String name = node.getInternalId();
                if (prefix != null && name.startsWith("__") && !name.endsWith("__")) {
                    node.setInternalId(prefix + name);
                }
                return node;
            }

            @Override
            public Object visitClassDef(ClassDef node) {
                return NameMangler.this.visitClassDef(node);
            }

            @Override
            public Object visitFunctionDef(FunctionDef node) {
                String name = node.getInternalName();
                if (name.startsWith("__") && !name.endsWith("__")) {
                    node.setInternalName(prefix + name);
                }
                arguments args = node.getInternalArgs();
                if (args != null) {
                    mangling(args.getInternalVararg());
                    mangling(args.getInternalKwarg());
                    for (arg argument: args.getInternalArgs()) {
                        mangling(argument);
                    }
                    for (arg argument: args.getInternalKwonlyargs()) {
                        mangling(argument);
                    }
                }

                return super.visitFunctionDef(node);
            }

            private void mangling(arg argument) {
                if (argument == null) return;
                String name = argument.getInternalArg();
                if (name.startsWith("__") && !name.endsWith("__")) {
                    argument.setInternalArg(prefix + name);
                }
            }
        };
        visitor.traverse(classDef);
        return classDef;
    }

}
