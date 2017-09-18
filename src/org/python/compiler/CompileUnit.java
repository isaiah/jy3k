package org.python.compiler;

import org.python.antlr.PythonTree;
import org.python.antlr.ast.Suite;
import org.python.antlr.base.mod;
import org.python.core.CodeFlag;
import org.python.core.CompilerFlags;
import org.python.core.Py;
import org.python.core.PyFunctionTable;
import org.python.core.PyObject;
import org.python.core.PyTableCode;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.python.util.CodegenUtils.*;

class CompileUnit {
    Module module;
    static int access = ACC_STATIC | ACC_FINAL;
    String name;

    final String co_name;
    final int argcount;
    final int kwonlyargcount;
    final List<String> varnames;
    final List<String> names;
    final List<PythonTree> constants;
    final int id;
    final int co_firstlineno;
    final boolean arglist, keywordlist;
    final String fname;
    // for nested scopes
    final List<String> cellvars;
    final List<String> freevars;
    final int jy_npurecell;
    final int moreflags;

    CompileUnit(mod tree, String name, boolean fast_locals, int firstlineno, ScopeInfo scope, CompilerFlags cflags,
                Module module) {
        this.co_name = name;
        this.co_firstlineno = firstlineno;
        this.module = module;

        // Needed so that moreflags can be final.
        int _moreflags = 0;

        if (scope.ac != null) {
            arglist = scope.ac.arglist;
            keywordlist = scope.ac.keywordlist;
            kwonlyargcount = scope.ac.kwonlyargcount;
            argcount = scope.ac.names.size() - kwonlyargcount;

            // Do something to add init_code to tree
            // XXX: not sure we should be modifying scope.ac in a PyCodeConstant
            // constructor.
            if (scope.ac.init_code.size() > 0) {
                scope.ac.appendInitCode((Suite)tree);
            }
        } else {
            arglist = false;
            keywordlist = false;
            argcount = 0;
            kwonlyargcount = 0;
        }

        id = module.codes.size();

        // Better names in the future?
        if (isJavaIdentifier(name)) {
            fname = name + "$" + id;
        } else {
            fname = "f$" + id;
        }
        // XXX: is fname needed at all, or should we just use "name"?
        this.name = fname;


        varnames = toNameAr(scope.varNames, false);
        constants = scope.constants;
        names = toNameAr(scope.globalNames, true);
        cellvars = toNameAr(scope.cellvars, true);
        freevars = toNameAr(scope.freevars, true);
        jy_npurecell = scope.jy_npurecell;

        if (CodeCompiler.checkOptimizeGlobals(fast_locals, scope)) {
            _moreflags |= CodeFlag.CO_OPTIMIZED.flag | CodeFlag.CO_NEWLOCALS.flag;
        }

        if (scope.isNested()) {
            _moreflags |= CodeFlag.CO_NESTED.flag;
        }

        if (scope.freevars.isEmpty() && scope.cellvars.isEmpty()) {
            _moreflags |= CodeFlag.CO_NOFREE.flag;
        }

        if (scope.async_gen) {
            _moreflags |= CodeFlag.CO_ASYNC_GENERATOR.flag;
        } else if (scope.async) {
            _moreflags |= CodeFlag.CO_COROUTINE.flag;
        } else if (scope.generator) {
            _moreflags |= CodeFlag.CO_GENERATOR.flag;
        }
//        if (cflags != null) {
//            if (cflags.isFlagSet(CodeFlag.CO_GENERATOR_ALLOWED)) {
//                _moreflags |= CodeFlag.CO_GENERATOR_ALLOWED.flag;
//            }
//            if (cflags.isFlagSet(CodeFlag.CO_FUTURE_DIVISION)) {
//                _moreflags |= CodeFlag.CO_FUTURE_DIVISION.flag;
//            }
//        }
        moreflags = _moreflags;
    }

    // XXX: this can probably go away now that we can probably just copy the list.
    private List<String> toNameAr(List<String> names, boolean nullok) {
        int sz = names.size();
        if (sz == 0 && nullok) {
            return null;
        }
        List<String> nameArray = new ArrayList<String>();
        nameArray.addAll(names);
        return nameArray;
    }

    private boolean isJavaIdentifier(String s) {
        char[] chars = s.toCharArray();
        if (chars.length == 0) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(chars[0])) {
            return false;
        }

        for (int i = 1; i < chars.length; i++) {
            if (!Character.isJavaIdentifierPart(chars[i])) {
                return false;
            }
        }
        return true;
    }

    void get(Code c) {
        c.getstatic(module.classfile.name, name, ci(PyTableCode.class));
    }

    void put(Code c) {
        module.classfile.addField(name, ci(PyTableCode.class), access);
        c.iconst(argcount);

        // Make all var names
        CodeCompiler.loadStrings(c, varnames);
        c.aload(1);
        c.ldc(co_name);
        c.iconst(co_firstlineno);

        c.iconst(arglist ? 1 : 0);
        c.iconst(keywordlist ? 1 : 0);

        c.getstatic(module.classfile.name, "self", "L" + module.classfile.name + ";");

        c.iconst(id);

        if (cellvars != null) {
            CodeCompiler.loadStrings(c, cellvars);
        } else {
            c.aconst_null();
        }
        if (freevars != null) {
            CodeCompiler.loadStrings(c, freevars);
        } else {
            c.aconst_null();
        }

        if (names != null) {
            CodeCompiler.loadStrings(c, names);
        } else {
            c.aconst_null();
        }
        if (constants != null) {
            int constArr = module.makeConstArray(c, constants);
            c.aload(constArr);
            c.freeLocal(constArr);
        }

        c.iconst(jy_npurecell);
        c.iconst(kwonlyargcount);
        c.iconst(moreflags);
        c.ldc(name);

        c.invokestatic(
                p(Py.class),
                "newCode",
                sig(PyTableCode.class, Integer.TYPE, String[].class, String.class, String.class,
                        Integer.TYPE, Boolean.TYPE, Boolean.TYPE, PyFunctionTable.class,
                        Integer.TYPE, String[].class, String[].class, String[].class, PyObject[].class,
                        Integer.TYPE, Integer.TYPE, Integer.TYPE, String.class));
        c.putstatic(module.classfile.name, name, ci(PyTableCode.class));
    }
}
