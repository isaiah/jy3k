package org.python.compiler;

/**
 * Created by isaiah on 5/3/17.
 */
public enum  CompilerConstants {
    GEN("__gen"),
    ITER("iter"),
    SAVE_OPRANDS("__save_operands__"),
    RESTORE_OPRANDS("__restore_operands__"),
    // mark a return op
    YIELD("__yield__"),
    // mark a label location
    MARK("__mark__"),
    RETURN(":return");


    private CompilerConstants(String name) {
        this.symbolName = name;
    }

    public String symbolName() {
        return symbolName;
    }

    private String symbolName;
}
