package org.python.compiler;

/**
 * Created by isaiah on 5/3/17.
 */
public enum  CompilerConstants {
    RETURN(":return");


    private CompilerConstants(String name) {
        this.symbolName = name;
    }

    public String symbolName() {
        return symbolName;
    }

    private String symbolName;
}
