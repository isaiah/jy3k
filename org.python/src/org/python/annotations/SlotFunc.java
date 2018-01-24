package org.python.annotations;

import org.python.core.PyType;

import java.lang.invoke.MethodHandle;

public enum SlotFunc {
    CONTAINS {
        @Override
        public void assign(PyType type, MethodHandle mh) {
            type.sqContains = mh;
        }
    },
    GETATTRO {
        @Override
        public void assign(PyType type, MethodHandle mh) {
            type.getattro = mh;
        }
    },
    ITER {
        @Override
        public void assign(PyType type, MethodHandle mh) {
            type.iter = mh;
        }
    },
    ITER_NEXT {
        @Override
        public void assign(PyType type, MethodHandle mh) {
            type.iternext = mh;
            type.isIterator = true;
        }
    };

    public abstract void assign(PyType type, MethodHandle mh);
}
