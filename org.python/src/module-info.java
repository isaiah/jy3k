module org.python {
    requires org.objectweb.asm;
    requires org.objectweb.asm.commons;
    requires org.objectweb.asm.util;
    requires antlr4.runtime;
    requires commons.compress;
    requires jffi;
    requires jnr.ffi;
    requires jnr.posix;
    requires jnr.constants;
    requires jnr.netdb;
    requires icu4j;
    requires jline;
    requires jzlib;
    requires bytelist;

    requires java.prefs;
    requires jdk.dynalink;
    requires java.logging;
    requires java.scripting;
    requires java.management;
    requires java.xml;
    requires java.desktop;

    exports org.python;
    exports org.python.core;
    exports org.python.core.generator to org.python;
    exports org.python.modules to org.python;
}
