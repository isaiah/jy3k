package org.python.annotations;

import org.python.core.PyType;

import java.lang.invoke.MethodHandle;

public enum SlotFunc {
    BOOL("__bool__") {
        @Override
        public void assign(PyType type, MethodHandle mh) {
            type.nbBool = mh;
        }
    },
    CONTAINS("__contains__") {
        @Override
        public void assign(PyType type, MethodHandle mh) {
            type.sqContains = mh;
        }
    },
    GETATTRO("__getattribute__") {
        @Override
        public void assign(PyType type, MethodHandle mh) {
            type.getattro = mh;
        }
    },
    ITER("__iter__") {
        @Override
        public void assign(PyType type, MethodHandle mh) {
            type.iter = mh;
        }
    },
    ITER_NEXT("__next__") {
        @Override
        public void assign(PyType type, MethodHandle mh) {
            type.iternext = mh;
            type.isIterator = true;
        }
    },
    LENGTH("__len__") {
        @Override
        public void assign(PyType type, MethodHandle mh) {
            type.sqLen = mh;
        }
    },
    REPEAT {
        @Override
        public void assign(PyType type, MethodHandle mh) {
            type.sqRepeat = mh;
        }
    },
    STR("__str__") {
        @Override
        public void assign(PyType type, MethodHandle mh) {
            type.str = mh;
        }
    };

    SlotFunc() {
        name = null;
    }

    SlotFunc(String name) {
        this.name = name;
    }

    private final String name;

    public String getName() {
        return name;
    }

    public boolean hasName() {
        return name != null;
    }

    public abstract void assign(PyType type, MethodHandle mh);
}
