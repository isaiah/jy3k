// Copyright (c) Corporation for National Research Initiatives

package org.python.compiler;

import org.objectweb.asm.Opcodes;
import org.python.core.PyObject;

import static org.python.util.CodegenUtils.ci;

abstract class Constant implements Opcodes{
    Constant(String name) {
        this.name = name;
    }

    Module module;
    static int access = ACC_STATIC;
    String name;

    void get(Code mv) {
        mv.getstatic(module.classfile.name, name, ci(PyObject.class));
    }

    abstract void put(Code mv);
}
