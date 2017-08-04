// Copyright (c) Corporation for National Research Initiatives
package org.python.modules.thread;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.Untraversable;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;

@Untraversable
@ExposedType(name = "_thread.lock")
public class PyLock extends PyObject {
    public static PyType TYPE = PyType.fromClass(PyLock.class);

    PyLock() {
        super(TYPE);
    }

    private boolean locked = false;

    public boolean acquire() {
        return acquire(true, -1);
    }

    @ExposedMethod(names = "acquire", defaults = {"true", "-1"})
    public synchronized boolean acquire(boolean blocking, int timeout) {
        if (blocking) {
            while (locked) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    System.err.println("Interrupted thread");
                }
            }
            locked = true;
            return true;
        } else {
            if (locked) {
                return false;
            } else {
                locked = true;
                return true;
            }
        }
    }

    @ExposedMethod(names = "release")
    public synchronized void release() {
        if (locked) {
            locked = false;
            notifyAll();
        } else {
            throw Py.ValueError("lock not acquired");
        }
    }

    @ExposedMethod
    public boolean lock_locked() {
        return locked;
    }

    @ExposedMethod
    public PyObject lock___enter__() {
        acquire();
        return this;
    }

    @ExposedMethod
    public boolean lock___exit__(PyObject type, PyObject value, PyObject traceback) {
        release();
        return false;
    }
}
