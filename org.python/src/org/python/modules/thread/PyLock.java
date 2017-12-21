// Copyright (c) Corporation for National Research Initiatives
package org.python.modules.thread;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.Untraversable;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;

import java.util.Objects;

@Untraversable
@ExposedType(name = "_thread.lock")
public class PyLock extends PyObject {
    public static PyType TYPE = PyType.fromClass(PyLock.class);

    PyLock() {
        super(TYPE);
    }

    private volatile boolean locked = false;

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
            throw Py.RuntimeError("lock not acquired");
        }
    }

    @ExposedMethod
    public boolean lock_locked() {
        return locked;
    }

    @ExposedMethod(names = {"__enter__"})
    public PyObject enter() {
        acquire();
        return this;
    }

    @ExposedMethod(names = {"__exit__"})
    public boolean exit(PyObject type, PyObject value, PyObject traceback) {
        release();
        return false;
    }

    @Override
    public String toString() {
        return String.format("<%s _thread.lock object at %s>", locked ? "locked" : "unlocked", Objects.hash(this));
    }
}
