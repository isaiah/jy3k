package org.python.modules._collections;

import org.python.annotations.ExposedSlot;
import org.python.annotations.SlotFunc;
import org.python.core.Abstract;
import org.python.core.ArgParser;
import org.python.core.BuiltinModule;
import org.python.core.CompareOp;
import org.python.core.PyList;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.ThreadState;
import org.python.core.Traverseproc;
import org.python.core.Visitproc;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSet;
import org.python.annotations.ExposedType;
import org.python.expose.MethodType;

import java.util.Objects;

/**
 * PyDeque - This class implements the functionalities of Deque data structure. Deques are a
 * generalization of stacks and queues (the name is pronounced 'deck' and is short for 'double-ended
 * queue'). Deques support thread-safe, memory efficient appends and pops from either side of the
 * deque with approximately the same O(1) performance in either direction.
 * 
 * Though list objects support similar operations, they are optimized for fast fixed-length
 * operations and incur O(n) memory movement costs for pop(0) and insert(0, v) operations which
 * change both the size and position of the underlying data representation.
 * 
 * collections.deque([iterable[, maxlen]]) - returns a new deque object initialized left-to-right
 * (using append()) with data from iterable. If iterable is not specified, the new deque is empty.
 * If maxlen is not specified or is None, deques may grow to an arbitrary length. Otherwise, the
 * deque is bounded to the specified maximum length. Once a bounded length deque is full, when new
 * items are added, a corresponding number of items are discarded from the opposite end.
 */
@ExposedType(name = "collections.deque")
public class PyDeque extends PyObject implements Traverseproc {

    public static final PyType TYPE = PyType.fromClass(PyDeque.class);

    long state = 0;
    private int size = 0;

    private int maxlen = -1;

    Node header = new Node(null, null, null);

    public PyDeque() {
        this(TYPE);
    }

    public PyDeque(PyType subType) {
        super(subType);
        header.left = header.right = header;
    }

    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject deque_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        return new PyDeque(subtype);
    }

    @ExposedMethod
    public synchronized final void deque___init__(PyObject[] args, String[] kwds) {
        ArgParser ap = new ArgParser("deque", args, kwds, new String[] {"iterable", "maxlen",}, 0);

        PyObject maxlenobj = ap.getPyObject(1, null);
        if (maxlenobj != null) {
            if (maxlenobj == Py.None) {
                maxlen = -1;
            } else {
                maxlen = ap.getInt(1);
                if (maxlen < 0) {
                    throw Py.ValueError("maxlen must be non-negative");
                }
            }
        } else {
            maxlen = -1;
        }

        PyObject iterable = ap.getPyObject(0, null);
        if (iterable != null) {
            if (size != 0) {
                // initializing a deque with an iterator when this deque is not empty means that we discard to empty first
                deque_clear();
            }
            deque_extend(iterable);
        }
    }

    /**
     * If maxlen is not specified or is None, deques may grow to an arbitrary length.
     * Otherwise, the deque is bounded to the specified maximum length.
     */
    @ExposedGet(name = "maxlen")
    public PyObject getMaxlen() {
        if (maxlen < 0) {
            return Py.None;
        }
        return Py.newInteger(maxlen);
    }

    @ExposedSet(name = "maxlen")
    public void setMaxlen(PyObject o) {
        // this has to be here because by default, if not defined,
        // the Jython object model raise a TypeError, where we usually expect
        // AttributeError here; due to a CPython 2.7 idiosyncracy that has
        // since been fixed for 3.x in http://bugs.python.org/issue1687163
        throw Py.AttributeError("attribute 'maxlen' of 'collections.deque' objects is not writable");
    }

    /**
     * Add obj to the right side of the deque.
     */    
    @ExposedMethod
    public synchronized final void deque_append(PyObject obj) {
        if (maxlen >= 0) {
            assert (size <= maxlen);
            if (maxlen == 0) {
                // do nothing; this deque will always be empty
                return;
            } else if (size == maxlen) {
                deque_popleft();
            }
        }
        addBefore(obj, header);
    }

    /**
     * Add obj to the left side of the deque.
     */
    @ExposedMethod
    public synchronized final void deque_appendleft(PyObject obj) {
        if (maxlen >= 0) {
            assert (size <= maxlen);
            if (maxlen == 0) {
                // do nothing; this deque will always be empty
                return;
            } else if (size == maxlen) {
                deque_pop();
            }
        }
        addBefore(obj, header.right);
    }

    private Node addBefore(PyObject obj, Node node) {
        // should ALWAYS be called inside a synchronized block
        Node newNode = new Node(obj, node, node.left);
        newNode.left.right = newNode;
        newNode.right.left = newNode;
        size++;
        state++;
        return newNode;
    }

    /**
     * Remove all elements from the deque leaving it with length 0.
     */
    @ExposedMethod
    public synchronized final void deque_clear() {
        Node node = header.right;
        while (node != header) {
            Node right = node.right;
            node.left = null;
            node.right = null;
            node.data = null;
            node = right;
            state++;
        }
        header.right = header.left = header;
        size = 0;
    }

    /**
     * Extend the right side of the deque by appending elements from the 
     * iterable argument.
     */
    @ExposedMethod
    public synchronized final void deque_extend(PyObject iterable) {
        Abstract._PySequence_Stream(iterable).forEach(this::deque_append);
    }

    /**
     * Extend the left side of the deque by appending elements from iterable. 
     * Note, the series of left appends results in reversing the order of 
     * elements in the iterable argument.
     */
    @ExposedMethod
    public synchronized final void deque_extendleft(PyObject iterable) {
        // handle case where iterable == this
        if (this == iterable) {
            deque_extendleft(new PyList(iterable));
        } else {
            for (PyObject item : iterable.asIterable()) {
                deque_appendleft(item);
            }
        }
    }

    /**
     * Remove and return an element from the right side of the deque. If no 
     * elements are present, raises an IndexError.
     */
    @ExposedMethod
    public synchronized final PyObject deque_pop() {
        return removeNode(header.left);
    }

    /**
     * Remove and return an element from the left side of the deque. If no 
     * elements are present, raises an IndexError.
     */
    @ExposedMethod
    public synchronized final PyObject deque_popleft() {
        return removeNode(header.right);
    }

    private PyObject removeNode(Node node) {
        // should ALWAYS be called inside a synchronized block
        if (node == header) {
            throw Py.IndexError("pop from an empty deque");
        }
        PyObject obj = node.data;
        node.left.right = node.right;
        node.right.left = node.left;
        node.right = null;
        node.left = null;
        node.data = null;
        size--;
        state++;
        return obj;
    }

    @ExposedSlot(SlotFunc.CONTAINS)
    public static boolean contains(PyObject iter, PyObject ob) {
        PyDeque self = (PyDeque) iter;
        int n = self.size;
        Node tmp = self.header.right;
        boolean match = false;
        long startState = self.state;
        for (int i = 0; i < n; i++) {
            match = Objects.equals(tmp.data, ob);
            if (startState != self.state) {
                throw Py.IndexError("deque mutated during remove().");
            }
            if (match) {
                break;
            }
            tmp = tmp.right;
        }
        return match;
    }

    /**
     * Removed the first occurrence of value. If not found, raises a 
     * ValueError.
     */
    @ExposedMethod
    public synchronized final PyObject deque_remove(PyObject value) {
        int n = size;
        Node tmp = header.right;
        boolean match = false;
        long startState = state;
        for (int i = 0; i < n; i++) {
            if (tmp.data.equals(value)) {
                match = true;
            }
            if (startState != state) {
                throw Py.IndexError("deque mutated during remove().");
            }
            if (match) {
                return removeNode(tmp);
            }
            tmp = tmp.right;
        }
        throw Py.ValueError("deque.remove(x): x not in deque");
    }

    /**
     * Count the number of deque elements equal to x.
     */
    @ExposedMethod
    public synchronized final PyObject deque_count(PyObject x) {
        int n = size;
        int count = 0;
        Node tmp = header.right;
        long startState = state;
        for (int i = 0; i < n; i++) {
            if (tmp.data.equals(x)) {
                count++;
            }
            if (startState != state) {
                throw Py.RuntimeError("deque mutated during count().");
            }
            tmp = tmp.right;
        }
        return Py.newInteger(count);
    }

    /**
     * Rotate the deque n steps to the right. If n is negative, rotate to the 
     * left. Rotating one step to the right is equivalent to: d.appendleft(d.pop()).
     */
    @ExposedMethod(defaults = {"1"})
    public synchronized final void deque_rotate(int steps) {
        if (size == 0) {
            return;
        }

        int halfsize = (size + 1) >> 1;
        if (steps > halfsize || steps < -halfsize) {
            steps %= size;
            if (steps > halfsize) {
                steps -= size;
            } else if (steps < -halfsize) {
                steps += size;
            }
        }

        //rotate right
        for (int i = 0; i < steps; i++) {
            deque_appendleft(deque_pop());
        }
        //rotate left
        for (int i = 0; i > steps; i--) {
            deque_append(deque_popleft());
        }
    }

    /**
     * Reverse the elements of the deque in-place and then return None.
     * @return Py.None
     */
    @ExposedMethod
    public synchronized final PyObject deque_reverse() {
        Node headerRight = header.right;
        Node headerLeft = header.left;
        Node node = header.right;
        while (node != header) {
            Node right = node.right;
            Node left = node.left;
            node.right = left;
            node.left = right;
            node = right;
        }
        header.right = headerLeft;
        header.left = headerRight;
        state++;
        return Py.None;
    }

    @Override
    public String toString() {
        return deque_toString();
    }

    @ExposedMethod(names = "__repr__")
    synchronized final String deque_toString() {
        ThreadState ts = Py.getThreadState();
        if (!ts.enterRepr(this)) { 
            return "[...]";
        }
        long startState = state;
        StringBuilder buf = new StringBuilder("deque").append("([");
        for (Node tmp = header.right; tmp != header; tmp = tmp.right) {
            buf.append(BuiltinModule.repr(tmp.data).toString());
            if (startState != state) {
                throw Py.RuntimeError("deque mutated during iteration.");
            }
            if (tmp.right != header) {
                buf.append(", ");
            }
        }
        buf.append("]");
        if (maxlen >= 0) {
            buf.append(", maxlen=");
            buf.append(maxlen);
        }
        buf.append(")");
        ts.exitRepr(this);
        return buf.toString();
    }

    @Override
    public int __len__() {
        return deque___len__();
    }

    @ExposedMethod
    synchronized final int deque___len__() {
        return size;
    }

    @Override
    public boolean isTrue() {
        return deque___bool__();
    }

    @ExposedMethod
    synchronized final boolean deque___bool__() {
        return size != 0;
    }

    @Override
    public PyObject __finditem__(PyObject key) {
        try {
            return deque___getitem__(key);
        } catch (PyException pe) {
            if (pe.match(Py.KeyError)) {
                return null;
            }
            throw pe;
        }
    }

    @ExposedMethod
    synchronized final PyObject deque___getitem__(PyObject index) {
        return getNode(index).data;
    }

    @Override
    public void __setitem__(PyObject index, PyObject value) {
        deque___setitem__(index, value);
    }

    @ExposedMethod
    synchronized final void deque___setitem__(PyObject index, PyObject value) {
        Node node = getNode(index).right;
        removeNode(node.left);
        addBefore(value, node);
    }

    @Override
    public void __delitem__(PyObject key) {
        deque___delitem__(key);
    }

    @ExposedMethod
    synchronized final void deque___delitem__(PyObject key) {
            removeNode(getNode(key));
    }

    private Node getNode(PyObject index) {
        // must ALWAYS be called inside a synchronized block
        int pos = 0;
        if (!index.isIndex()) {
            throw Py.TypeError(String.format("sequence index must be integer, not '%.200s'",
                                             index.getType().fastGetName()));
        }
        pos = index.asIndex(Py.IndexError);

        if (pos < 0) {
            pos += size;
        }
        if (pos < 0 || pos >= size) {
            throw Py.IndexError("index out of range: " + index);
        }

        Node tmp = header;
        if (pos < (size >> 1)) {
            for (int i = 0; i <= pos; i++) {
                tmp = tmp.right;
            }
        } else {
            for (int i = size - 1; i >= pos; i--) {
                tmp = tmp.left;
            }
        }
        return tmp;
    }

    @ExposedMethod
    final PyObject deque___iter__() {
        return new PyDequeIter(this);
    }

    @ExposedMethod(type = MethodType.BINARY)
    public final synchronized PyObject __iadd__(PyObject o) {
        deque_extend(o);
        return this;
    }

    @Override
    public int hashCode() {
        return deque_hashCode();
    }

    @ExposedMethod(names = "__hash__")
    final int deque_hashCode() {
        throw Py.TypeError("deque objects are unhashable");
    }

    @Override
    public PyObject __reduce__() {
        return deque___reduce__();
    }

    @ExposedMethod
    final PyObject deque___reduce__() {
        PyObject dict = getDict();
        if (dict == null) {
            dict = Py.None;
        }
        return new PyTuple(getType(), Py.EmptyTuple, dict, PyObject.getIter(this));
    }

    @ExposedMethod
    final PyObject deque___copy__() {
        PyDeque pd = (PyDeque)this.getType().__call__();    
        pd.deque_extend(this);
        return pd;
    }

    @Override
    public final PyObject do_richCompare(PyObject other, CompareOp op) {
        if (!(other instanceof PyDeque)) {
            if (op == CompareOp.EQ) {
                return Py.False;
            }

            if (op == CompareOp.NE) {
                return Py.True;
            }

            return Py.NotImplemented;
        }
        PyDeque ot = (PyDeque) other;
        int l = __len__();
        int ol = ot.__len__();

        int i = 0;
        Node n1 = header.right;
        Node n2 = ot.header.right;
        for (; i < l && i < ol; i++) {
            boolean k = n1.data.do_richCompareBool(n2.data, CompareOp.EQ);
            if (!k) {
                break;
            }
            n1 = n1.right;
            n2 = n2.right;
        }
        int result = l - ol;

        // sanitize the result, because -2 means NotImplemented
        if (result < 0) {
            result = -1;
        }
        if (result > 0) {
            result = 1;
        }

        if (i >= l || i >= ol) {
            // no more items to compare, compare size
            return op.bool(result);
        }
        if (op == CompareOp.EQ) {
            return Py.False;
        }

        if (op == CompareOp.NE) {
            return Py.True;
        }

        // compare the final item again using the proper operator
        return n1.data.do_richCompare(n2.data, op);
    }

    @Override
    public boolean isMappingType() {
        return false;
    }

    @Override
    public boolean isSequenceType() {
        return true;
    }

    static class Node {
        Node left;
        Node right;
        PyObject data;

        Node(PyObject data, Node right, Node left) {
            this.data = data;
            this.right = right;
            this.left = left;
        }
    }


    /* Traverseproc implementation */
    @Override
    public synchronized int traverse(Visitproc visit, Object arg) {
    	if (header == null) {
            return 0;
        }
        int retVal = 0;
        if (header.data != null) {
            retVal = visit.visit(header.data, arg);
            if (retVal != 0) {
                return retVal;
            }
        }
        Node tmp = header.right;
        while (tmp != header) {
            if (tmp.data != null) {
                retVal = visit.visit(tmp.data, arg);
                if (retVal != 0) {
                    return retVal;
                }
            }
            tmp = tmp.right;
        }
        return retVal;
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
