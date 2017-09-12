// Copyright (c) Corporation for National Research Initiatives

package org.python.compiler;

import java.io.IOException;

import org.objectweb.asm.Opcodes;

abstract class Constant implements Opcodes{
    Module module;
    static int access = ACC_STATIC | ACC_FINAL;
    String name;

    abstract void get(Code mv);

    abstract void put(Code mv);
}
