package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.expose.MethodType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@ExposedType(name = "frozenset", base = PyObject.class, doc = BuiltinDocs.frozenset_doc)
public class PyFrozenSet extends BaseSet {

    public static final PyType TYPE = PyType.fromClass(PyFrozenSet.class);

    public PyFrozenSet() {
        super(TYPE, Set.of());
    }

    public PyFrozenSet(Set<PyObject> data) {
        super(TYPE, data);
    }

    public PyFrozenSet(PyObject[] data) {
        super(TYPE, Set.of(data));
    }

    public PyFrozenSet(PyObject data) {
        this(TYPE, data);
    }

    public PyFrozenSet(PyType type, PyObject data) {
        super(type, _update(new HashSet<>(), data));
    }

    @ExposedNew
    public static PyObject frozenset_new(PyNewWrapper new_, boolean init, PyType subtype,
                                             PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("frozenset", args, keywords, new String[] {"iterable"}, 0);
        PyObject iterable = ap.getPyObject(0, null);
        if (iterable == null || iterable instanceof PyFrozenSet) {
            /* frozenset(f) is idempotent */
            return iterable;
        }
        return new PyFrozenSet(subtype, iterable);
    }


    @ExposedMethod(type = MethodType.BINARY)
    public final PyObject frozenset___or__(PyObject o) {
        return baseset___or__(o);
    }

    @ExposedMethod(type = MethodType.BINARY)
    public final PyObject frozenset___xor__(PyObject o) {
        return baseset___xor__(o);
    }

    @ExposedMethod(type = MethodType.BINARY)
    public final PyObject frozenset___sub__(PyObject o) {
        return baseset___sub__(o);
    }

    @ExposedMethod(type = MethodType.BINARY)
    public final PyObject frozenset___and__(PyObject o) {
        return baseset___and__(o);
    }

    @ExposedMethod
    public final PyObject frozenset___iter__() {
        return baseset___iter__();
    }

    @ExposedMethod
    public final boolean frozenset___contains__(PyObject item) {
        return baseset___contains__(item);
    }

    @ExposedMethod
    public final PyObject frozenset_copy() {
        if (getClass() == PyFrozenSet.class) {
                return this;
        }
        // subclasses should revert to normal behavior of creating a new instance
        return baseset_copy();
    }

    @ExposedMethod
    public final PyObject frozenset_union(PyObject[] args, String [] kws) {
        if (kws.length > 0) {
            throw Py.TypeError("difference() takes no keyword arguments");
        }
        return baseset_union(args);
    }

    @ExposedMethod
    public final PyObject frozenset_difference(PyObject[] args, String [] kws) {
        if (kws.length > 0) {
            throw Py.TypeError("difference() takes no keyword arguments");
        }
        return baseset_difference(args);
    }

    @ExposedMethod
    public final PyObject frozenset_symmetric_difference(PyObject set) {
        return baseset_symmetric_difference(set);
    }

    @ExposedMethod
    public final PyObject frozenset_intersection(PyObject[] args, String [] kws) {
        if (kws.length > 0) {
            throw Py.TypeError("intersection() takes no keyword arguments");
        }
        return baseset_intersection(args);
    }

    @ExposedMethod
    public final PyObject frozenset_issubset(PyObject set) {
        return baseset_issubset(set);
    }

    @ExposedMethod
    public final PyObject frozenset_issuperset(PyObject set) {
        return baseset_issuperset(set);
    }
    
    @ExposedMethod
    public final PyObject frozenset_isdisjoint(PyObject set) {
        return baseset_isdisjoint(set);
    }

    @ExposedMethod
    public final int frozenset___len__() {
        return baseset___len__();
    }

    @ExposedMethod
    public final PyObject frozenset___reduce__() {
        return baseset___reduce__();
    }

    @ExposedMethod
    public final int frozenset___hash__() {
        return _set.hashCode();
    }

    @ExposedMethod(names = "__repr__")
    public final String frozenset_toString() {
        return baseset_toString();
    }

    public int hashCode() {
        return frozenset___hash__();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    public Iterator iterator() {
        final Iterator i = super.iterator();
        return new Iterator() {

            public boolean hasNext() {
                return i.hasNext();
            }
            public Object next() {
                return i.next();
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
