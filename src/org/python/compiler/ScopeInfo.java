// (C) Copyright 2001 Samuele Pedroni

package org.python.compiler;

import org.python.antlr.PythonTree;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ScopeInfo extends Object implements ScopeConstants {

    public PythonTree scope_node;
    public String scope_name;
    public int level;
    public int func_level;
    public String qualname;

    public void dump() { // for debugging
        if (org.python.core.Options.verbose < org.python.core.Py.DEBUG)
            return;
        for(int i=0; i<level; i++) System.err.print(' ');
        System.err.print(((kind != CLASSSCOPE)?scope_name:"class "+
                         scope_name)+": ");
        for (Map.Entry<String, SymInfo> entry : tbl.entrySet()) {
            String name = entry.getKey();
            SymInfo info = entry.getValue();
            int flags = info.flags;
            System.err.print(name);
            if ((flags&BOUND) != 0) System.err.print('=');
            // func scope global (affect nested scopes)
            // vs. class scope global
            if ((flags&NGLOBAL) != 0) System.err.print('G');
            else if ((flags&CLASS_GLOBAL) != 0) System.err.print('g');
            if ((flags&PARAM) != 0) System.err.print('P');
            else if ((flags&FROM_PARAM) != 0) System.err.print('p');
            if ((flags&CELL) != 0) System.err.print('!');
            if ((flags&FREE) != 0) System.err.print(",f");
            System.err.print(" ");
        }
        System.err.println();
    }

    public ScopeInfo(String name, PythonTree node, int level, int kind,
                     int func_level, ArgListCompiler ac) {
        scope_name = name;
        scope_node = node;
        this.level = level;
        this.kind = kind;
        this.func_level = func_level;
        this.ac = ac;
    }

    public int kind;

    public boolean unqual_exec;
    public boolean exec;
    public boolean from_import_star;
    public boolean generator;
    public boolean async;
    public boolean async_gen;
    public boolean comprehension;
    private boolean hasReturnWithValue;
    public int yield_count;
    public int max_with_count;

    public ArgListCompiler ac;

    public Map<String, SymInfo> tbl = new LinkedHashMap<String, SymInfo>();
    public List<String> varNames = new ArrayList<>();

    public int addNonlocal(String name) {
        SymInfo info = tbl.get(name);
        if (info == null) {
            tbl.put(name,new SymInfo(FREE));
            return -1;
        }
        int prev = info.flags;
        info.flags |= FREE;
        return prev;
    }

    public int addGlobal(String name) {
        // global kind = func vs. class
        int global = kind==CLASSSCOPE?CLASS_GLOBAL:NGLOBAL;
        SymInfo info = tbl.get(name);
        if (info == null) {
            tbl.put(name,new SymInfo(global|BOUND));
            return -1;
        }
        int prev = info.flags;
        info.flags |= global|BOUND;
        return prev;
    }

    public int local = 0;

    public void addParam(String name) {
//System.out.println("addParam " + name);
        tbl.put(name, new SymInfo(PARAM|BOUND,local++));
        varNames.add(name);
    }

    public void markFromParam() {
        for (SymInfo info : tbl.values()) {
            info.flags |= FROM_PARAM;
        }
    }

    public void addBound(String name) {
        SymInfo info = tbl.get(name);
        if (info == null) {
            tbl.put(name, new SymInfo(BOUND));
            return;
        }
        // don't bound nonlocal variables
        if ((info.flags & FREE) == 0) {
            info.flags |= BOUND;
        }
    }

    public int addAnnotated(String name) {
        SymInfo info = tbl.get(name);
        if (info == null) {
            tbl.put(name, new SymInfo(DEF_ANNOT));
            return 0;
        }

        int prev = info.flags;
        info.flags |= DEF_ANNOT;
        return prev;
    }

    public void addUsed(String name) {
        if (tbl.get(name) == null) {
            tbl.put(name, new SymInfo(0));
        }
    }

    public void addConst(PythonTree node) {
        constants.add(node);
    }

    private final static Object PRESENT = new Object();

    public Hashtable<String,Object> inner_free = new Hashtable<String,Object>();

    public List<String> globalNames = new ArrayList<>(); // co_names
    public List<PythonTree> constants = new ArrayList<>(); // co_consts
    public List<String> freevars = new ArrayList<>(); // co_freevars
    public List<String> cellvars = new ArrayList<>(); // co_cellvars
    public List<String> jy_paramcells = new ArrayList<>();

    public int jy_npurecell;

    public int cell, distance;

    public ScopeInfo up;

    //Resolve the names used in the given scope, and mark any freevars used in the up scope
    public void cook(ScopeInfo up, int distance, CompilationContext ctxt) throws Exception {
        if(up == null)
            return; // top level => nop
        this.up = up;
        this.distance = distance;
        boolean func = kind == FUNCSCOPE;
        List<String> purecells = new ArrayList<>();
        cell = 0;
        boolean some_inner_free = inner_free.size() > 0;

        for (Enumeration e = inner_free.keys(); e.hasMoreElements(); ) {
            String name = (String)e.nextElement();
            SymInfo info = tbl.get(name);
            if (info == null) {
                tbl.put(name,new SymInfo(FREE));
                continue;
            }
            int flags = info.flags;
//            if (func) {
                // not func global and bound ?
            if ((flags&NGLOBAL) == 0 && (flags&BOUND) != 0) {
                info.flags |= CELL;
                if ((flags&PARAM) != 0)
                    jy_paramcells.add(name);
                cellvars.add(name);
                info.env_index = cell++;
                if ((flags&PARAM) == 0) purecells.add(name);
                continue;
            }
            info.flags |= FREE;
//            } else {
//                info.flags |= FREE;
//            }
        }
        boolean some_free = false;

        boolean nested = up.kind != TOPSCOPE;
        for (Map.Entry<String, SymInfo> entry : tbl.entrySet()) {
            String name = entry.getKey();
            SymInfo info = entry.getValue();
            int flags = info.flags;
            if (nested && (flags&FREE) != 0) up.inner_free.put(name,PRESENT);
            if ((flags&(GLOBAL|PARAM|CELL)) == 0) {
                if ((flags&BOUND) != 0) { // ?? only func
                    // System.err.println("local: "+name);
                    varNames.add(name);
                    info.locals_index = local++;
                    continue;
                }
                info.flags |= FREE;
                some_free = true;
                if (nested) up.inner_free.put(name,PRESENT);
            }
        }
        if ((jy_npurecell = purecells.size()) > 0) {
            varNames.addAll(purecells);
        }

        // XXX - this doesn't catch all cases - may depend subtly
        // on how visiting NOW works with antlr compared to javacc
        if ((unqual_exec || from_import_star)) {
            if(some_inner_free) dynastuff_trouble(true, ctxt);
            else if(func_level > 1 && some_free)
                dynastuff_trouble(false, ctxt);
        }

    }

    private void dynastuff_trouble(boolean inner_free, CompilationContext ctxt) throws Exception {
        StringBuilder illegal = new StringBuilder();
        if (unqual_exec && from_import_star) {
            illegal.append("function '")
                    .append(scope_name)
                    .append("' uses import * and bare exec, which are illegal");
        } else if (unqual_exec) {
            illegal.append("unqualified exec is not allowed in function '")
                    .append(scope_name)
                    .append("'");
        } else {
            illegal.append("import * is not allowed in function '").append(scope_name).append("'");
        }
        if (inner_free) {
            illegal.append(" because it contains a function with free variables");
        } else {
            illegal.append(" because it contains free variables");
        }
        ctxt.error(illegal.toString(), true, scope_node);
    }

    /**
     * setup the closure on this scope using the scope passed into cook as up as
     * the containing scope
     */
    public void setup_closure() {
        setup_closure(up);
    }

    /**
     * setup the closure on this scope using the passed in scope. This is used
     * by jythonc to setup its closures.
     */
    public void setup_closure(ScopeInfo up){
        int free = cell; // env = cell...,free...
        Map<String, SymInfo> up_tbl = up.tbl;
        boolean nested = up.kind != TOPSCOPE;
        for (Map.Entry<String, SymInfo> entry : tbl.entrySet()) {
            String name = entry.getKey();
            SymInfo info = entry.getValue();
            int flags = info.flags;
            if ((flags&FREE) != 0) {
                SymInfo up_info = up_tbl.get(name);
                // ?? differs from CPython -- what is the intended behaviour?
                if (up_info != null) {
                    int up_flags = up_info.flags;
                    if ((up_flags&(CELL|FREE)) != 0) {
                        info.env_index = free++;
                        freevars.add(name);
                        continue;
                    }
                    // ! func global affect nested scopes
                    if (nested && (up_flags&NGLOBAL) != 0) {
                        info.flags = NGLOBAL|BOUND;
                        continue;
                    }
                }
                globalNames.add(name);
                info.flags &= ~FREE;
            }
        }

    }

    // check if specified name is defined as global
    public boolean isGlobal(String name) {
        SymInfo info = tbl.get(name);
        return info != null && (info.flags & NGLOBAL) != 0;
    }

    @Override
    public String toString() {
        return "ScopeInfo[" + scope_name + " " + kind + "]@" +
                System.identityHashCode(this);
    }

    public void defineAsGenerator() {
        generator = true;
    }

    public void defineAsComprehension() {
        comprehension = true;
    }

    public void noteReturnValue() {
        hasReturnWithValue = true;
    }

    public boolean isFunction() {
        return kind == FUNCSCOPE;
    }

    public boolean isClassScope() {
        return kind == CLASSSCOPE;
    }

    public boolean isNested() {
        return isFunction() && up.isFunction();
    }
}
