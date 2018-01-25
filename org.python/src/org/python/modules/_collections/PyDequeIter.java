package org.python.modules._collections;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyType;

@ExposedType(name = "_deque_iterator")
public class PyDequeIter extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyDequeIter.class);

    private PyDeque pyDeque;
    private PyDeque.Node lastReturned;
    private long startState;

    public PyDequeIter(PyDeque pyDeque) {
        super(TYPE);
        this.pyDeque = pyDeque;
        lastReturned = pyDeque.header;
        startState = pyDeque.state;
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @ExposedSlot(SlotFunc.ITER_NEXT)
    public static PyObject __next__(PyObject iter) {
        PyDequeIter self = (PyDequeIter) iter;
        synchronized (self.pyDeque) {
            if (self.startState != self.pyDeque.state) {
                throw Py.RuntimeError("deque changed size during iteration");
            }
            if (self.lastReturned.right != self.pyDeque.header) {
                self.lastReturned = self.lastReturned.right;
                return self.lastReturned.data;
            }
            throw Py.StopIteration();
        }
    }
}
