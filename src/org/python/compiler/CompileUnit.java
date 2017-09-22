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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.python.compiler.Symtable.Flag.*;
import static org.python.util.CodegenUtils.*;

class CompileUnit {
    final PySTEntryObject ste;
    final CompilerScope scopeType;
    private final Map<?, ?> consts;
    private final int firstlineno;
    Module module;
    static int access = ACC_STATIC | ACC_FINAL;
    String name;

    String co_name;
    int argcount;
    int kwonlyargcount;
    Map<String, Integer> varnames;
    Map<String, Integer> names;
    List<PythonTree> constants;
    int id;
    int co_firstlineno;
    boolean arglist, keywordlist;
    String fname;
    // for nested scopes
    Map<String, Integer> cellvars;
    Map<String, Integer> freevars;
    int jy_npurecell;
    int moreflags;

    String _private;
    int nestedlevel;
    public String qualname;
    public Code methodEmitter;


    public CompileUnit(CompilerScope scopeType, String name, PySTEntryObject ste, int lineno, Module module) {
        this.module = module;
        this.scopeType = scopeType;
        this.name = name;
        this.ste = ste;
        this.varnames = list2dict(ste.varnames);
        this.cellvars = dictbytype(ste.symbols, CELL, SENTINAL, 0);
        if (ste.needsClassClosure) {
            /** Cook up an implicit __class__ cell */
            assert scopeType == CompilerScope.CLASS;
            assert cellvars.isEmpty();
            cellvars.put("__class__", 0);
        }
        this.freevars = dictbytype(ste.symbols, FREE, DEF_FREE_CLASS, cellvars.size());
        this.firstlineno = lineno;
        this.consts = new HashMap<>();
        this.names = new HashMap<>();
        this.id = module.codes.size();
        this.fname = isJavaIdentifier(name) ? name + "$" + id : "f$" + id;
        this.co_name = fname;
    }

    private static Map<String, Integer> list2dict(List<String> varnames) {
        Map<String, Integer> dict = new HashMap<>(varnames.size());
        int i = 0;
        for (String name : varnames) {
            dict.put(name, i++);
        }
        return dict;
    }

    private static Map<String, Integer> dictbytype(Map<String, EnumSet<Symtable.Flag>> src, Symtable.Flag scopeType, Symtable.Flag flag, int offset) {
        List<String> sortedKeys = src.keySet().stream().sorted().collect(Collectors.toList());
        Map<String, Integer> dest = new HashMap<>();
        int i = offset;
        for (String k : sortedKeys) {
            EnumSet<Symtable.Flag> v = src.get(k);
            if (v.contains(scopeType) || v.contains(flag)) {
                dest.put(k, i++);
            }
        }
        return dest;
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
        assert module != null: "module should not be null";
        assert module.classfile != null: "classfile should not be null";
        assert module.classfile.name != null: "no classfile name";
        assert name != null: "no name";
        c.getstatic(module.classfile.name, co_name, ci(PyTableCode.class));
    }

    void put(Code c) {
        module.classfile.addField(co_name, ci(PyTableCode.class), access);
        c.iconst(argcount);

        // Make all var names
        CodeCompiler.loadStrings(c, varnames.keySet());
        c.aload(1);
        c.ldc(co_name);
        c.iconst(co_firstlineno);

        c.iconst(arglist ? 1 : 0);
        c.iconst(keywordlist ? 1 : 0);

        c.getstatic(module.classfile.name, "self", "L" + module.classfile.name + ";");

        c.iconst(id);

        if (cellvars != null) {
            CodeCompiler.loadStrings(c, cellvars.keySet());
        } else {
            c.aconst_null();
        }
        if (freevars != null) {
            CodeCompiler.loadStrings(c, freevars.keySet());
        } else {
            c.aconst_null();
        }

        if (names != null) {
            CodeCompiler.loadStrings(c, names.keySet());
        } else {
            c.aconst_null();
        }
        if (constants != null) {
            int constArr = module.makeConstArray(c, constants);
            c.aload(constArr);
            c.freeLocal(constArr);
        } else {
            c.aconst_null();
        }

        c.iconst(jy_npurecell);
        c.iconst(kwonlyargcount);
        c.iconst(moreflags);
        c.ldc(co_name);

        c.invokestatic(
                p(Py.class),
                "newCode",
                sig(PyTableCode.class, Integer.TYPE, String[].class, String.class, String.class,
                        Integer.TYPE, Boolean.TYPE, Boolean.TYPE, PyFunctionTable.class,
                        Integer.TYPE, String[].class, String[].class, String[].class, PyObject[].class,
                        Integer.TYPE, Integer.TYPE, Integer.TYPE, String.class));
        c.putstatic(module.classfile.name, co_name, ci(PyTableCode.class));
    }
}
