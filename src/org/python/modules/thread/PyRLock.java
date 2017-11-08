// Copyright (c) Corporation for National Research Initiatives
package org.python.modules.thread;

import org.python.annotations.ExposedNew;
import org.python.core.Py;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.Untraversable;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Untraversable
@ExposedType(name = "_thread.RLock")
public class PyRLock extends PyObject {
    public static PyType TYPE = PyType.fromClass(PyRLock.class);
    final private ReentrantLock lock;

    PyRLock() {
        super(TYPE);
        lock = new ReentrantLock();
    }

    @ExposedNew
    public static final PyObject _new(PyNewWrapper new_, boolean init, PyType subtype,
                                      PyObject[] args, String[] keywords) {
        return new PyRLock();
    }

    public boolean acquire() {
        return acquire(true, -1);
    }

    @ExposedMethod(names = "acquire", defaults = {"true", "-1"})
    public synchronized boolean acquire(boolean blocking, int timeout) {
        if (blocking) {
            while (lock.isLocked()) {
                try {
                    if (timeout > 0) {
                        lock.tryLock(timeout, TimeUnit.SECONDS);
                    } else {
                        lock.lockInterruptibly();
                    }
                } catch (InterruptedException e) {
                    throw Py.RuntimeError("thread interrupted");
                }
            }
            lock.lock();
            return true;
        } else {
            if (lock.isLocked()) {
                return false;
            } else {
                lock.lock();
                return true;
            }
        }
    }

    @ExposedMethod(names = "release")
    public synchronized void release() {
        if (!lock.isLocked()) {
            throw Py.RuntimeError("lock not acquired");
        }
        lock.unlock();
        notifyAll();
    }

    @ExposedMethod
    public boolean _is_owned() {
        return lock.isHeldByCurrentThread();
    }

    @ExposedMethod(names = {"__enter__"})
    public PyObject enter() {
        acquire();
        return this;
    }

    @ExposedMethod(names = {"__exit__"})
    public void exit(PyObject type, PyObject value, PyObject traceback) {
        release();
    }

    @Override
    public String toString() {
        return String.format("<%s _thread.RLock object at %s>", lock.isLocked() ? "locked" : "unlocked", Objects.hash(this));
    }
}
