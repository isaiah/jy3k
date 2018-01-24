// Copyright (c) Corporation for National Research Initiatives
package org.python.modules.thread;

import org.python.annotations.ExposedNew;
import org.python.core.Abstract;
import org.python.core.ArgParser;
import org.python.core.Py;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.ThreadState;
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

    @ExposedMethod(names = "acquire")
    public PyObject acquire(ThreadState ts, PyObject[] args, String[] kws) {
        ArgParser ap = new ArgParser("acqurie", args, kws, "blocking", "timeout");
        boolean blocking = ap.getBoolean(0, true);
        PyObject timeoutObj = ap.getPyObject(1, null);
        double timeout = -1;
        if (timeoutObj != null) {
            timeout = Abstract.PyNumber_Float(ts, timeoutObj).asDouble();
        }
        return Py.newBoolean(acquire(blocking, (long) (timeout * 1000)));
    }

    private boolean acquire(boolean blocking, long timeout) {
        if (blocking) {
            try {
                if (timeout > 0) {
                    return lock.tryLock(timeout, TimeUnit.MILLISECONDS);
                }
                lock.lockInterruptibly();
                return true;
            } catch (InterruptedException e) {
                throw Py.RuntimeError("thread interrupted");
            }
        }
        return lock.tryLock();
    }

    @ExposedMethod(names = "release")
    public void release() {
        try {
            lock.unlock();
        } catch (IllegalMonitorStateException e) {
            throw Py.RuntimeError("lock not acquired");
        }
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
