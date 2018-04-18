// Copyright (c) Corporation for National Research Initiatives
package org.python.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import java.lang.reflect.Array;
import java.util.Objects;
import java.util.stream.Stream;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;
import org.python.expose.MethodType;

/**
 * A builtin python tuple.
 */
@ExposedType(name = "tuple", base = PyObject.class, doc = BuiltinDocs.tuple_doc)
public class PyTuple extends PySequenceList implements List {

    public static final PyType TYPE = PyType.fromClass(PyTuple.class);

    private final PyObject[] array;

    private volatile List<PyObject> cachedList = null;

    private static final PyTuple EMPTY_TUPLE = new PyTuple();

    public PyTuple() {
        this(TYPE, Py.EmptyObjects);
    }

    public PyTuple(PyObject... elements) {
        this(TYPE, elements);
    }

    public PyTuple(Collection<? extends PyObject> elements) {
        this(TYPE, elements.toArray(new PyObject[0]));
    }

    public PyTuple(PyType subtype, PyObject[] elements) {
        this(subtype, elements, -1);
    }
    public PyTuple(PyType subtype, PyObject[] elements, int length) {
        super(subtype);
        if (elements == null) {
            array = new PyObject[0];
        } else {
            if (length < 0) {
                length = elements.length;
            }
            array = new PyObject[length];
            System.arraycopy(elements, 0, array, 0, length);
        }
    }

    public PyTuple(PyObject[] elements, boolean copy) {
        this(TYPE, elements, copy);
    }

    public PyTuple(PyType subtype, PyObject[] elements, boolean copy) {
        super(subtype);

        if (copy) {
            array = new PyObject[elements.length];
            System.arraycopy(elements, 0, array, 0, elements.length);
        } else {
            array = elements;
        }
    }

    private static PyTuple fromArrayNoCopy(PyObject[] elements) {
        return new PyTuple(elements, false);
    }

    List<PyObject> getList() {
        if (cachedList == null) {
            cachedList = Arrays.asList(array);
        }
        return cachedList;
    }

    @ExposedNew
    final static PyObject tuple_new(PyNewWrapper new_, boolean init, PyType subtype,
                                    PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("tuple", args, keywords, new String[] {"sequence"}, 0);
        PyObject S = ap.getPyObject(0, null);
        if (new_.for_type == subtype) {
            if (S == null) {
                return EMPTY_TUPLE;
            }
            if (PyTuple.checkExact(S)) {
                return S;
            }
            return fromArrayNoCopy(Py.make_array(S));
        }
        PyObject[] data = S == null ? Py.EmptyObjects : Py.make_array(S);
        PyTuple ret = new PyTuple(subtype, data);
        ret.slots = new PyObject[subtype.getNumSlots()];
        ret.dict = subtype.instDict();
        return ret;
    }

    final static boolean checkExact(PyObject pyobj) {
        return pyobj.getType() == TYPE;
    }

    /**
     * Return a new PyTuple from an iterable.
     *
     * Raises a TypeError if the object is not iterable.
     *
     * @param iterable an iterable PyObject
     * @return a PyTuple containing each item in the iterable
     */
    public static PyTuple fromIterable(PyObject iterable) {
        return fromArrayNoCopy(Py.make_array(iterable));
    }

    public PyObject getslice(int start, int stop, int step) {
        if (step > 0 && stop < start) {
            stop = start;
        }
        int n = sliceLength(start, stop, step);
        PyObject[] newArray = new PyObject[n];

        if (step == 1) {
            System.arraycopy(array, start, newArray, 0, stop - start);
            return fromArrayNoCopy(newArray);
        }
        for (int i = start, j = 0; j < n; i += step, j++) {
            newArray[j] = array[i];
        }
        return fromArrayNoCopy(newArray);
    }

    @Override
    protected PyObject repeat(int count) {
        return repeat(this, count);
    }

    @ExposedSlot(SlotFunc.REPEAT)
    public static PyObject repeat(PyObject self, int count) {
        PyTuple tuple = (PyTuple) self;
        if (count < 0) {
            count = 0;
        }
        int size = tuple.size();
        if (size == 0 || count == 1) {
            if (tuple.getType() == TYPE) {
                // Since tuples are immutable, we can return a shared copy in this case
                return self;
            }
            if (size == 0) {
                return EMPTY_TUPLE;
            }
        }

        int newSize = size * count;
        if (newSize / size != count) {
            throw Py.MemoryError("");
        }

        PyObject[] newArray = new PyObject[newSize];
        for (int i = 0; i < count; i++) {
            System.arraycopy(tuple.array, 0, newArray, i * size, size);
        }
        return fromArrayNoCopy(newArray);
    }

    @Override
    public int __len__() {
        return tuple___len__();
    }

    @ExposedMethod(doc = BuiltinDocs.tuple___len___doc)
    final int tuple___len__() {
        return size();
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.tuple___add___doc)
    public final PyObject __add__(PyObject generic_other) {
        PyTuple sum = null;
        if (generic_other instanceof PyTuple) {
            PyTuple other = (PyTuple) generic_other;
            PyObject[] newArray = new PyObject[array.length + other.array.length];
            System.arraycopy(array, 0, newArray, 0, array.length);
            System.arraycopy(other.array, 0, newArray, array.length, other.array.length);
            sum = fromArrayNoCopy(newArray);
        }
        return sum;
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.tuple___mul___doc)
    public final PyObject __mul_(PyObject other) {
        if (other instanceof PyLong) {
            throw Py.TypeErrorFmt("can't multiply sequence by non-int of type '%s'", other);
        }
        int count = other.asInt();
        if (count <= 0) {
            return EMPTY_TUPLE;
        }
        int len = array.length;
        PyObject[] ret = new PyObject[len * count];
        for (int i = 0; i < count; i++) {
            System.arraycopy(array, 0, ret, i * len, len);
        }
        return new PyTuple(ret);
    }

    @ExposedSlot(SlotFunc.ITER)
    public static PyObject iter(PyObject iter) {
        return new PyTupleIterator((PyTuple) iter);
    }

    @Override
    public Iterable<PyObject> asIterable() {
        return () -> Arrays.asList(array).iterator();
    }

    @ExposedMethod(doc = BuiltinDocs.tuple___getitem___doc)
    final PyObject tuple___getitem__(PyObject index) {
        PyObject ret = seq___finditem__(index);
        if (ret == null) {
            throw Py.IndexError("index out of range: " + index);
        }
        return ret;
    }

    @ExposedMethod(doc = BuiltinDocs.tuple___getnewargs___doc)
    final PyTuple tuple___getnewargs__() {
        return new PyTuple(new PyTuple(getArray()));
    }

    @Override
    public PyTuple __getnewargs__() {
        return tuple___getnewargs__();
    }

    @Override
    public int hashCode() {
        return tuple___hash__();
    }

    @ExposedSlot(SlotFunc.CONTAINS)
    public static boolean contains(PyObject self, PyObject other) {
        return Stream.of(((PyTuple) self).array).anyMatch(el -> Objects.equals(el, other));
    }

    @ExposedMethod(doc = BuiltinDocs.tuple___hash___doc)
    final int tuple___hash__() {
        // strengthened hash to avoid common collisions. from CPython
        // tupleobject.tuplehash. See http://bugs.python.org/issue942952
        int y;
        int len = size();
        int mult = 1000003;
        int x = 0x345678;
        while (--len >= 0) {
            y = array[len].hashCode();
            x = (x ^ y) * mult;
            mult += 82520 + len + len;
        }
        return x + 97531;
    }

    private String subobjRepr(PyObject o) {
        if (o == null) {
            return "null";
        }
        return BuiltinModule.repr(o).toString();
    }

    @ExposedMethod(doc = BuiltinDocs.tuple___repr___doc)
    final PyUnicode tuple___repr__() {
        return new PyUnicode(toString());
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("(");
        for (int i = 0; i < array.length - 1; i++) {
            buf.append(subobjRepr(array[i]));
            buf.append(", ");
        }
        if (array.length > 0) {
            buf.append(subobjRepr(array[array.length - 1]));
        }
        if (array.length == 1) {
            buf.append(",");
        }
        buf.append(")");
        return buf.toString();
    }

    public List subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size()) {
            throw new IndexOutOfBoundsException();
        } else if (fromIndex > toIndex) {
            throw new IllegalArgumentException();
        }
        PyObject elements[] = new PyObject[toIndex - fromIndex];
        for (int i = 0, j = fromIndex; i < elements.length; i++, j++) {
            elements[i] = array[j];
        }
        return new PyTuple(elements);
    }

    public Iterator iterator() {
        return new Iterator() {

            private final Iterator<PyObject> iter = getList().iterator();

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public boolean hasNext() {
                return iter.hasNext();
            }

            public Object next() {
                return iter.next().__tojava__(Object.class);
            }
        };
    }

    public boolean add(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(Collection coll) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection coll) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection coll) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public Object set(int index, Object element) {
        throw new UnsupportedOperationException();
    }

    public void add(int index, Object element) {
        throw new UnsupportedOperationException();
    }

    public Object remove(int index) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(int index, Collection c) {
        throw new UnsupportedOperationException();
    }

    public ListIterator listIterator() {
        return listIterator(0);
    }

    public ListIterator listIterator(final int index) {
        return new ListIterator() {

            private final ListIterator<PyObject> iter = getList().listIterator(index);

            public boolean hasNext() {
                return iter.hasNext();
            }

            public Object next() {
                return iter.next().__tojava__(Object.class);
            }

            public boolean hasPrevious() {
                return iter.hasPrevious();
            }

            public Object previous() {
                return iter.previous().__tojava__(Object.class);
            }

            public int nextIndex() {
                return iter.nextIndex();
            }

            public int previousIndex() {
                return iter.previousIndex();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public void set(Object o) {
                throw new UnsupportedOperationException();
            }

            public void add(Object o) {
                throw new UnsupportedOperationException();
            }
        };
    }

    protected String unsupportedopMessage(String op, PyObject o2) {
        if (op.equals("+")) {
            return "can only concatenate tuple (not \"{2}\") to tuple";
        }
        return super.unsupportedopMessage(op, o2);
    }

    public void pyset(int index, PyObject value) {
        throw Py.TypeError("'tuple' object does not support item assignment");
    }

    @Override
    public boolean contains(Object o) {
        return getList().contains(Py.java2py(o));
    }

    @Override
    public boolean containsAll(Collection c) {
        if (c instanceof PyList) {
            return getList().containsAll(((PyList)c).getList());
        } else if (c instanceof PyTuple) {
            return getList().containsAll(((PyTuple)c).getList());
        } else {
            return getList().containsAll(new PyList(c));
        }
    }

    public int count(PyObject value) {
        return tuple_count(value);
    }

    @ExposedMethod(doc = BuiltinDocs.tuple_count_doc)
    final int tuple_count(PyObject value) {
        int count = 0;
        for (PyObject item : array) {
            if (item.do_richCompareBool(value, CompareOp.EQ)) {
                count++;
            }
        }
        return count;
    }

    public int index(PyObject value) {
        return index(value, 0);
    }

    public int index(PyObject value, int start) {
        return index(value, start, size());
    }

    public int index(PyObject value, int start, int stop) {
        return tuple_index(value, start, stop);
    }

    @ExposedMethod(defaults = {"null", "null"}, doc = BuiltinDocs.tuple_index_doc)
    public final int tuple_index(PyObject value, PyObject start, PyObject stop) {
        int startInt = start == null ? 0 : PySlice.calculateSliceIndex(start);
        int stopInt = stop == null ? size() : PySlice.calculateSliceIndex(stop);
        return tuple_index(value, startInt, stopInt);
    }

    @Override
    public final PyObject do_richCompare(PyObject other, CompareOp op) {
        if (!(other instanceof PyTuple)) {
            if (op == CompareOp.EQ) {
                return Py.False;
            }

            if (op == CompareOp.NE) {
                return Py.True;
            }

            return Py.NotImplemented;
        }
        PyTuple ot = (PyTuple) other;
        int l = __len__();
        int ol = ot.__len__();

        int i = 0;
        for (; i < l && i < ol; i++) {
            boolean k = array[i].do_richCompareBool(ot.array[i], CompareOp.EQ);
            if (!k) {
                break;
            }
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
        return array[i].do_richCompare(ot.array[i], op);
    }

    final int tuple_index(PyObject value, int start, int stop) {
        int validStart = boundToSequence(start);
        int validStop = boundToSequence(stop);
        for (int i = validStart; i < validStop; i++) {
            if (array[i].do_richCompareBool(value, CompareOp.EQ)) {
                return i;
            }
        }
        throw Py.ValueError("tuple.index(x): x not in list");
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof PyObject) {
            return do_richCompareBool((PyObject) other, CompareOp.EQ);
        }
        if (other instanceof List) {
            return other.equals(this);
        }
        return false;
    }

    @Override
    public Object get(int index) {
        return array[index].__tojava__(Object.class);
    }

    @Override
    public PyObject[] getArray() {
        return array;
    }

    @Override
    public int indexOf(Object o) {
        return getList().indexOf(Py.java2py(o));
    }

    @Override
    public boolean isEmpty() {
        return array.length == 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        return getList().lastIndexOf(Py.java2py(o));
    }

    @Override
    public void pyadd(int index, PyObject element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean pyadd(PyObject o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PyObject pyget(int index) {
        return array[index];
    }

    @Override
    public void remove(int start, int stop) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public Object[] toArray() {
        Object[] converted = new Object[array.length];
        for (int i = 0; i < array.length; i++) {
            converted[i] = array[i].__tojava__(Object.class);
        }
        return converted;
    }

    @Override
    public Object[] toArray(Object[] converted) {
        Class<?> type = converted.getClass().getComponentType();
        if (converted.length < array.length) {
            converted = (Object[])Array.newInstance(type, array.length);
        }
        for (int i = 0; i < array.length; i++) {
            converted[i] = type.cast(array[i].__tojava__(type));
        }
        if (array.length < converted.length) {
            for (int i = array.length; i < converted.length; i++) {
                converted[i] = null;
            }
        }
        return converted;
    }


    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        int retVal;
        for (PyObject ob: array) {
            if (ob != null) {
                retVal = visit.visit(ob, arg);
                if (retVal != 0) {
                    return retVal;
                }
            }
        }
        if (cachedList != null) {
            for (PyObject ob: cachedList) {
                if (ob != null) {
                    retVal = visit.visit(ob, arg);
                    if (retVal != 0) {
                        return retVal;
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        if (ob == null) {
            return false;
        }
        for (PyObject obj: array) {
            if (obj == ob) {
                return true;
            }
        }
        if (cachedList != null) {
            for (PyObject obj: cachedList) {
                if (obj == ob) {
                    return true;
                }
            }
        }
        return false;
    }
}
