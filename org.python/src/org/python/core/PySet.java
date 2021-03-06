package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;
import org.python.expose.MethodType;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

@ExposedType(name = "set", base = PyObject.class, doc = BuiltinDocs.set_doc)
public class PySet extends BaseSet {

    public static final PyType TYPE = PyType.fromClass(PySet.class);
    {
        // Ensure set is not Hashable
        TYPE.object___setattr__("__hash__", Py.None);
    }

    public PySet() {
        this(TYPE);
    }

    public PySet(PyType type) {
        super(type, Collections.synchronizedSet(new HashSet<>()));
    }

    public PySet(PyObject data) {
        super(TYPE, _update(Collections.synchronizedSet(new HashSet<>()), data));
    }

    public PySet(PyObject[] data) {
        super(TYPE, _update(Collections.synchronizedSet(new HashSet<>()), data));
    }

    public PySet(PyType subtype, PyObject data) {
        super(subtype, _update(Collections.synchronizedSet(new HashSet<>()), data));
    }
    public PySet(Set backing_set, PyObject data) {
        super(TYPE, _update(backing_set, data));
    }

    public PySet(PyType type, Set backing_set, PyObject data) {
        super(type, _update(backing_set, data));
    }

    @ExposedNew
    public static PyObject set_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        return new PySet(subtype);
    }

    @ExposedMethod(doc = BuiltinDocs.set___init___doc)
    public final void set___init__(PyObject[] args, String[] kwds) {
        if (args.length == 0) {
            return;
        }

        _set.clear();
        _update(args[0]);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___or___doc)
    public final PyObject set___or__(PyObject o) {
        return baseset___or__(o);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___xor___doc)
    public final PyObject set___xor__(PyObject o) {
        return baseset___xor__(o);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___sub___doc)
    public final PyObject set___sub__(PyObject o) {
        return baseset___sub__(o);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___and___doc)
    public final PyObject set___and__(PyObject o) {
        return baseset___and__(o);
    }

    @ExposedSlot(SlotFunc.ITER)
    public static PyObject set___iter__(PyObject set) {
        return ((PySet) set).baseset___iter__();
    }

    @ExposedMethod(doc = BuiltinDocs.set___contains___doc)
    public final boolean set___contains__(PyObject item) {
        return baseset___contains__(item);
    }

    @ExposedMethod(doc = BuiltinDocs.set_copy_doc)
    public final PyObject set_copy() {
        return baseset_copy();
    }

    @ExposedMethod(doc = BuiltinDocs.set_union_doc)
    public final PyObject set_union(PyObject[] args, String [] kws) {
        if (kws.length > 0) {
            throw Py.TypeError("union() takes no keyword arguments");
        }
        return baseset_union(args);
    }

    @ExposedMethod(doc = BuiltinDocs.set_difference_doc)
    public final PyObject set_difference(PyObject[] args, String [] kws) {
        if (kws.length > 0) {
            throw Py.TypeError("difference() takes no keyword arguments");
        }
        return baseset_difference(args);
    }

    @ExposedMethod(doc = BuiltinDocs.set_symmetric_difference_doc)
    public final PyObject set_symmetric_difference(PyObject set) {
        return baseset_symmetric_difference(set);
    }

    @ExposedMethod(doc = BuiltinDocs.set_intersection_doc)
    public final PyObject set_intersection(PyObject[] args, String [] kws) {
        if (kws.length > 0) {
            throw Py.TypeError("intersection() takes no keyword arguments");
        }
        return baseset_intersection(args);
    }

    @ExposedMethod(doc = BuiltinDocs.set_issubset_doc)
    public final PyObject set_issubset(PyObject set) {
        return baseset_issubset(set);
    }

    @ExposedMethod(doc = BuiltinDocs.set_issuperset_doc)
    public final PyObject set_issuperset(PyObject set) {
        return baseset_issuperset(set);
    }
    
    @ExposedMethod(doc = BuiltinDocs.set_isdisjoint_doc)
    public final PyObject set_isdisjoint(PyObject set) {
        return baseset_isdisjoint(set);
    }

    @ExposedMethod(doc = BuiltinDocs.set___len___doc)
    public final int set___len__() {
        return baseset___len__();
    }

    @ExposedMethod(doc = BuiltinDocs.set___reduce___doc)
    public final PyObject set___reduce__() {
        return baseset___reduce__();
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___ior___doc)
    public final PyObject set___ior__(PyObject other) {
        if (!(other instanceof BaseSet)) {
            return null;
        }
        _set.addAll(((BaseSet)other)._set);
        return this;
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___ixor___doc)
    public final PyObject set___ixor__(PyObject other) {
        if (!(other instanceof BaseSet)) {
            return null;
        }
        set_symmetric_difference_update(other);
        return this;
    }

    public PyObject __iand__(PyObject other) {
        return set___iand__(other);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___iand___doc)
    public final PyObject set___iand__(PyObject other) {
        if (!(other instanceof BaseSet)) {
            return null;
        }
        _set = ((BaseSet)__and__(other))._set;
        return this;
    }

    public PyObject __isub__(PyObject other) {
        return set___isub__(other);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___isub___doc)
    public final PyObject set___isub__(PyObject other) {
        if (!(other instanceof BaseSet)) {
            return null;
        }
        _set.removeAll(((BaseSet)other)._set);
        return this;
    }

    @Override
    public boolean addAll(Collection<? extends PyObject> collection) {
        return _set.addAll(collection);
    }

    public int hashCode() {
        return set___hash__();
    }

    @ExposedMethod
    public final int set___hash__() {
        throw Py.TypeError("set objects are unhashable");
    }

    @ExposedMethod(doc = BuiltinDocs.set_add_doc)
    public final void set_add(PyObject o) {
        _set.add(o);
    }

    @ExposedMethod(doc = BuiltinDocs.set_remove_doc)
    public final void set_remove(PyObject o) {
        boolean b = false;
        try {
            b = _set.remove(o);
        } catch (PyException e) {
            PyObject frozen = asFrozen(e, o);
            b = _set.remove(frozen);
        }
        if (!b) {
            throw new PyException(Py.KeyError, o);
        }
    }

    @ExposedMethod(doc = BuiltinDocs.set_discard_doc)
    public final void set_discard(PyObject o) {
        try {
            _set.remove(o);
        } catch (PyException e) {
            PyObject frozen = asFrozen(e, o);
            _set.remove(frozen);
        }
    }

    @ExposedMethod(doc = BuiltinDocs.set_pop_doc)
    final synchronized PyObject set_pop() {
        Iterator<PyObject> iterator = _set.iterator();
        try {
            Object first = iterator.next();
            _set.remove(first);
            return (PyObject)first;
        } catch (NoSuchElementException e) {
            throw new PyException(Py.KeyError, "pop from an empty set");
        }
    }

    @ExposedMethod(doc = BuiltinDocs.set_clear_doc)
    final void set_clear() {
        _set.clear();
    }

    @ExposedMethod(doc = BuiltinDocs.set_update_doc)
    final void set_update(PyObject[] args, String [] kws) {
        if (kws.length > 0) {
            throw Py.TypeError("update() takes no keyword arguments");
        }
        for (PyObject item: args) {
        	_update(item);
        }
    }

    @ExposedMethod(doc = BuiltinDocs.set_intersection_update_doc)
    final void set_intersection_update(PyObject[] args, String [] kws) {
        if (kws.length > 0) {
            throw Py.TypeError("intersection_update() takes no keyword arguments");
        }
        
    	for (PyObject other: args) {
    		if (other instanceof BaseSet) {
    			__iand__(other);
    		} else {
    			BaseSet set = (BaseSet)baseset_intersection(other);
    			_set = set._set;
    		}
    	}
    }

    @ExposedMethod(doc = BuiltinDocs.set_symmetric_difference_update_doc)
    final void set_symmetric_difference_update(PyObject other) {
        if (this == other) {
            set_clear();
            return;
        }

        BaseSet bs = (other instanceof BaseSet) ? (BaseSet)other : new PySet(other);
        for (PyObject o : bs._set) {
            if (_set.contains(o)) {
                _set.remove(o);
            } else {
                _set.add(o);
            }
        }
    }

    @ExposedMethod(doc = BuiltinDocs.set_difference_update_doc)
    final void set_difference_update(PyObject[] args, String [] kws) {
        if (kws.length > 0) {
            throw Py.TypeError("difference_update() takes no keyword arguments");
        }
        
    	if (args.length == 0) {
    		return;
        }
    	
    	for (PyObject other: args) {
    		if (other instanceof BaseSet) {
    			__isub__(other);
    		}
    		for (PyObject o : other.asIterable()) {
    			if (Abstract.PySequence_Contains(this, o)) {
    				_set.remove(o);
    			}
    		}
    	}
    }

    @Override
    public String toString() {
        ThreadState ts = Py.getThreadState();
        if (!ts.enterRepr(this)) {
            return "set(...)";
        }
        StringBuilder buf = new StringBuilder("{");
        for (Iterator<PyObject> i = _set.iterator(); i.hasNext();) {
            buf.append(BuiltinModule.repr(i.next()).toString());
            if (i.hasNext()) {
                buf.append(", ");
            }
        }
        buf.append("}");
        ts.exitRepr(this);
        return buf.toString();
    }
}
