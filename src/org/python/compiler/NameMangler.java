package org.python.compiler;

import org.python.antlr.Visitor;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.Delete;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Nonlocal;
import org.python.antlr.ast.arg;
import org.python.antlr.ast.arguments;

import java.util.List;
import java.util.stream.Collectors;

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
                node.setInternalAttr(mangle(name));
                return super.visitAttribute(node);
            }

            @Override
            public Object visitName(Name node) {
                String name = node.getInternalId();
                node.setInternalId(mangle(name));
                return node;
            }

            @Override
            public Object visitNonlocal(Nonlocal node) {
                List<String> names = node.getInternalNames().stream().map(this::mangle).collect(Collectors.toList());
                node.setInternalNames(names);
                return node;
            }

            @Override
            public Object visitClassDef(ClassDef node) {
                return NameMangler.this.visitClassDef(node);
            }

            @Override
            public Object visitFunctionDef(FunctionDef node) {
                String name = node.getInternalName();
                node.setInternalName(mangle(name));
                arguments args = node.getInternalArgs();
                if (args != null) {
                    manglingArg(args.getInternalVararg());
                    manglingArg(args.getInternalKwarg());
                    for (arg argument: args.getInternalArgs()) {
                        manglingArg(argument);
                    }
                    for (arg argument: args.getInternalKwonlyargs()) {
                        manglingArg(argument);
                    }
                }

                return super.visitFunctionDef(node);
            }

            private void manglingArg(arg argument) {
                if (argument == null) return;
                String name = argument.getInternalArg();
                argument.setInternalArg(mangle(name));
            }

            private String mangle(String name) {
                 if (prefix != null && name.startsWith("__") && !name.endsWith("__")) {
                    return prefix + name;
                }
                return name;
            }
        };
        visitor.traverse(classDef);
        return classDef;
    }

}
