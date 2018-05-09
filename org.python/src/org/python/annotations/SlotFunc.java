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
    CALL("__call__") {
        @Override
        public void assign(PyType type, MethodHandle mh) {
            type.call = mh;
        }

        @Override
        public boolean isWide() {
            return true;
        }
    },
    CONTAINS("__contains__") {
        @Override
        public void assign(PyType type, MethodHandle mh) {
            type.sqContains = mh;
        }
    },
    DIVMOD("__divmod__") {
        @Override
        public void assign(PyType type, MethodHandle mh) {
            type.nbDivmod = mh;
        }
    },
    GETATTRO("__getattribute__") {
        @Override
        public void assign(PyType type, MethodHandle mh) {
            type.getattro = mh;
        }
    },
    GETITEM("__getitem__") {
        @Override
        public void assign(PyType type, MethodHandle mh) {
            type.mqSubscript = mh;
        }
    },
    SETITEM("__setitem__") {
        @Override
        public void assign(PyType type, MethodHandle mh) {
            type.mqAssSubscript = mh;
        }
    },
    HASH("__hash__") {
        @Override
        public void assign(PyType type, MethodHandle mh) {
            type.tpHash = mh;
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
    NEW("__new__") {
        @Override
        public void assign(PyType type, MethodHandle mh) {
            type.tpNew = mh;
        }
    },
    REPEAT("__mul__") {
        @Override
        public void assign(PyType type, MethodHandle mh) {
            type.sqRepeat = mh;
        }
    },
    SQ_ITEM() {
        @Override
        public void assign(PyType type, MethodHandle mh) {
            type.sqItem = mh;
        }
    },
    SQ_ASS_ITEM() {
        @Override
        public void assign(PyType type, MethodHandle mh) {
            type.sqAssItem = mh;
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

    public boolean isWide() {
        return false;
    }

    public abstract void assign(PyType type, MethodHandle mh);
}
