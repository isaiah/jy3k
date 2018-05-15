package org.python.compiler;

import org.python.core.Py;
import org.python.core.PyObject;

import static org.python.util.CodegenUtils.ci;
import static org.python.util.CodegenUtils.p;


public class SingletonConstant extends AbstractConstant {
    private final String type;

    public SingletonConstant(String name, String type) {
        super(name);
        this.type = type;
    }

    @Override
    public void get(Code mv) {
        mv.getstatic(p(Py.class), name, type);
    }

    @Override
    public void put(Code mv) {
    }
}
