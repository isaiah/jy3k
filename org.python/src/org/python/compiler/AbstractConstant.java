package org.python.compiler;

import org.objectweb.asm.Opcodes;
import org.python.core.PyObject;

import static org.python.util.CodegenUtils.ci;

public abstract class AbstractConstant implements Constant, ClassConstants, Opcodes {
    AbstractConstant(String name) {
        this.name = name;
    }

    Module module;
    static int access = ACC_STATIC;
    String name;

    public void get(Code mv) {
        mv.getstatic(module.classfile.name, name, ci(PyObject.class));
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setModule(Module m) {
        this.module = m;
    }
}
