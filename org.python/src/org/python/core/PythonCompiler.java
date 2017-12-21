package org.python.core;

import org.python.antlr.base.mod;

public interface PythonCompiler {

    PythonCodeBundle compile(mod node, String name, String filename, boolean linenumbers, CompilerFlags cflags)
            throws Exception;

}
