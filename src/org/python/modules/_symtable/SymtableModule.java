package org.python.modules._symtable;

import org.python.annotations.ExposedConst;
import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;
import org.python.compiler.PySTEntryObject;
import org.python.core.PyObject;

@ExposedModule
public class SymtableModule {
// #define DEF_GLOBAL 1           /* global stmt */
// #define DEF_LOCAL 2            /* assignment in code block */
// #define DEF_PARAM 2<<1         /* formal parameter */
// #define DEF_NONLOCAL 2<<2      /* nonlocal stmt */
// #define USE 2<<3               /* name is used */
// #define DEF_FREE 2<<4          /* name used but not defined in nested block */
// #define DEF_FREE_CLASS 2<<5    /* free variable from class's method */
// #define DEF_IMPORT 2<<6        /* assignment occurred via import */
// #define DEF_ANNOT 2<<7         /* this name is annotated */
// #define DEF_BOUND (DEF_LOCAL | DEF_PARAM | DEF_IMPORT)
// #define LOCAL 1
//#define GLOBAL_EXPLICIT 2
//#define GLOBAL_IMPLICIT 3
//#define FREE 4
//#define CELL 5
//
//#define GENERATOR 1
//#define GENERATOR_EXPRESSION 2

    @ExposedConst
    public static final int LOCAL = 1;
    @ExposedConst
    public static final int GLOBAL_EXPLICIT = 2;
    @ExposedConst
    public static final int GLOBAL_IMPLICIT = 3;
    @ExposedConst
    public static final int FREE = 4;
    @ExposedConst
    public static final int CELL = 5;

    @ExposedConst
    public static final int DEF_GLOBAL = 1;
    @ExposedConst
    public static final int DEF_LOCAL = 2;
    @ExposedConst
    public static final int DEF_PARAM = 2 << 1;
    @ExposedConst
    public static final int DEF_NONLOCAL = 2 << 2;
    @ExposedConst
    public static final int USE = 2 << 3;
    @ExposedConst
    public static final int DEF_FREE = 2 << 4;
    @ExposedConst
    public static final int DEF_FREE_CLASS = 2 << 5;
    @ExposedConst
    public static final int DEF_IMPORT = 2 << 6;
    @ExposedConst
    public static final int DEF_ANNOT = 2 << 7;
    @ExposedConst
    public static final int DEF_BOUND = DEF_LOCAL | DEF_PARAM | DEF_IMPORT;

    @ExposedConst
    public static final int SCOPE_MASK = DEF_GLOBAL | DEF_LOCAL | DEF_PARAM | DEF_NONLOCAL;

    @ExposedConst
    public static final int TYPE_FUNCTION = 0;
    @ExposedConst
    public static final int TYPE_CLASS = 1;
    @ExposedConst
    public static final int TYPE_MODULE = 2;


    @ExposedConst(name = "SCOPE_OFF")
    public static final int SCOPE_OFFSET = 11;

    public static void init(PyObject dict) {
//        dict.__setitem__("SymbolTable", PySymbolTable.TYPE);
    }

    @ExposedFunction
    public static PyObject symtable(PyObject code, PyObject filename, PyObject compileType) {
        return new PySTEntryObject();
    }
}
