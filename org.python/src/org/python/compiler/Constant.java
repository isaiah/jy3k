// Copyright (c) Corporation for National Research Initiatives

package org.python.compiler;

import org.objectweb.asm.Opcodes;
import org.python.core.PyObject;

import static org.python.util.CodegenUtils.ci;

interface Constant {
    void setName(String name);

    void setModule(Module m);

    void get(Code mv);

    void put(Code mv);
}
