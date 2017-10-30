package org.python.compiler;

import org.objectweb.asm.Label;
import org.python.antlr.base.expr;
import org.python.core.CodeFlag;
import org.python.core.Py;
import org.python.core.PyFunctionTable;
import org.python.core.PyObject;
import org.python.core.PyTableCode;

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
    int yieldCount;
    Module module;
    static int access = ACC_STATIC | ACC_FINAL;
    String name;

    String co_name;
    int argcount;
    int kwonlyargcount;
    Map<String, Integer> varnames;
    Map<String, Integer> names;
    List<expr> constants;
    int id;
    int co_firstlineno;
    int co_lineno;
    boolean co_lineno_set;
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

    // This are the labels used for generator/coroutine lookup table
    public Label genswitch;
    public Label start;


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
        this.co_lineno = this.co_firstlineno = lineno;
        this.consts = new HashMap<>();
        this.names = new HashMap<>();
        this.id = module.codes.size();
        this.fname = isJavaIdentifier(name) ? name + "$" + id : "f$" + id;
        this.co_name = fname;
        this.argcount = ste.ac.argcount;
        this.kwonlyargcount = ste.ac.kwonlyargcount;
        this.arglist = ste.ac.arglist;
        this.keywordlist = ste.ac.keywordlist;
        this.moreflags = computeCodeFlags(ste);
        this.yieldCount = 0;
    }

    private static int computeCodeFlags(PySTEntryObject ste) {
        int flags = CodeFlag.CO_OPTIMIZED.flag;
        if (ste.generator) {
            flags |= CodeFlag.CO_GENERATOR.flag;
        } else if (ste.coroutine) {
            flags |= CodeFlag.CO_COROUTINE.flag;
        }
        return flags;
    }

    private static Map<String, Integer> list2dict(List<String> varnames) {
        Map<String, Integer> dict = new HashMap<>(varnames.size());
        int i = 0;
        for (String name : varnames) {
            if (name.equals("__debug__")) {
                throw Py.SyntaxError("assignment to keyword");
            }
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
        CodeCompiler.loadStrings(c, names);
        c.aload(1);
        c.ldc(name);
        c.iconst(co_firstlineno);

        c.iconst(arglist);
        c.iconst(keywordlist);

        c.getstatic(module.classfile.name, "self", "L" + module.classfile.name + ";");

        c.iconst(id);

        CodeCompiler.loadStrings(c, cellvars);
        CodeCompiler.loadStrings(c, freevars.keySet());
        CodeCompiler.loadStrings(c, varnames);

        if (constants != null) {
            module.makeConstArray(c, constants);
        } else {
            c.aconst_null();
        }

        c.iconst(kwonlyargcount);
        c.iconst(moreflags);
        c.ldc(co_name);

        c.invokestatic(
                p(Py.class),
                "newCode",
                sig(PyTableCode.class, Integer.TYPE, String[].class, String.class, String.class,
                        Integer.TYPE, Boolean.TYPE, Boolean.TYPE, PyFunctionTable.class,
                        Integer.TYPE, String[].class, String[].class, String[].class, PyObject[].class,
                        Integer.TYPE, Integer.TYPE, String.class));
        c.putstatic(module.classfile.name, co_name, ci(PyTableCode.class));
    }
}
