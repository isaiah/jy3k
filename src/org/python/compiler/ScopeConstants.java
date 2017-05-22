package org.python.compiler;

public interface ScopeConstants {

    public final static int BOUND = 1;
    public final static int NGLOBAL = 1 << 1; // func scope expl global
    public final static int PARAM = 1 << 2;
    public final static int FROM_PARAM = 1 << 3;
    public final static int CELL = 1 << 4;
    public final static int FREE = 1 << 5;
    public final static int CLASS_GLOBAL = 1 << 6; // class scope expl global
    public final static int DEF_ANNOT = 1 << 7; // class scope expl global
    public final static int GLOBAL = NGLOBAL|CLASS_GLOBAL; // all global

    public final static int TOPSCOPE = 0;
    public final static int FUNCSCOPE = 1;
    public final static int CLASSSCOPE = 2;

}
