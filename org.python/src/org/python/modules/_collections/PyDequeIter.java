package org.python.modules._collections;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;
import org.python.core.Py;
import org.python.core.PyObject;

@ExposedType(name = "_deque_iterator")
public class PyDequeIter extends PyObject {

    private PyDeque pyDeque;
    private PyDeque.Node lastReturned;
    private long startState;

    public PyDequeIter(PyDeque pyDeque) {
        this.pyDeque = pyDeque;
        lastReturned = pyDeque.header;
        startState = pyDeque.state;
    }

    @ExposedMethod(names = "__next__")
    public PyObject __next__() {
        synchronized (pyDeque) {
            if (startState != pyDeque.state) {
                throw Py.RuntimeError("deque changed size during iteration");
            }
            if (lastReturned.right != pyDeque.header) {
                lastReturned = lastReturned.right;
                return lastReturned.data;
            }
            throw Py.StopIteration();
        }
    }
}
