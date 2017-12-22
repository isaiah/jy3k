module org.python {
    requires asm;
    requires antlr4;
    requires commons.compress;
    requires jffi;
    requires jnr.ffi;
    requires jnr.posix;
    requires jnr.constants;
    requires icu4j;

    requires java.prefs;
    requires jdk.dynalink;
    requires transitive java.scripting;
    requires java.management;
}