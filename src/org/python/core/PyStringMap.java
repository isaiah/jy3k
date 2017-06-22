/*
 * Copyright (c) Corporation for National Research Initiatives
 * Copyright (c) Jython Developers
 */
package org.python.core;

import org.python.expose.ExposedClassMethod;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedNew;
import org.python.expose.ExposedType;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Special fast dict implementation for __dict__ instances. Allows interned String keys in addition
 * to PyObject unlike PyDictionary.
 */
// TODO: this needs proper key, value, item views
@ExposedType(name = "stringmap", isBaseType = false)
public class PyStringMap extends PyObject implements Traverseproc, PyDict {

    /**
     * TYPE computed lazily, PyStringMap is used early in the bootstrap process and
     * statically calling fromClass(PyStringMap.class) is unsafe.
     */
    private static PyType lazyType;

    private final ConcurrentMap<Object, PyObject> table;

    public ConcurrentMap<Object, PyObject> getMap() {
        return table;
    }

    public PyStringMap() {
        this(4);
    }

    public PyStringMap(int capacity) {
        super(getLazyType());
        table = new ConcurrentHashMap<>(capacity);
    }

    public PyStringMap(Map<Object, PyObject> map) {
        super(getLazyType());
        table = new ConcurrentHashMap<>();
        table.putAll(map);
    }

    public PyStringMap(PyObject elements[]) {
        this(elements.length);
        for (int i = 0; i < elements.length; i += 2) {
            __setitem__(elements[i], elements[i + 1]);
        }
    }

    private static PyType getLazyType() {
        if (lazyType == null) {
            lazyType = PyType.fromClass(PyStringMap.class);
        }
        return lazyType;
    }

    @ExposedNew
    final static PyObject stringmap_new(PyNewWrapper new_, boolean init, PyType subtype,
                                        PyObject[] args, String[] keywords) {
        PyStringMap map = new PyStringMap();
        map.stringmap_update(args, keywords);
        return map;
    }

    public static PyObject fromkeys(PyObject keys) {
        return fromkeys(keys, Py.None);
    }

    public static PyObject fromkeys(PyObject keys, PyObject value) {
        return stringmap_fromkeys(TYPE, keys, value);
    }

    @ExposedClassMethod(defaults = "Py.None", doc = BuiltinDocs.dict_fromkeys_doc)
    static PyObject stringmap_fromkeys(PyType type, PyObject keys, PyObject value) {
        PyObject d = type.__call__();
        for (PyObject o : keys.asIterable()) {
            d.__setitem__(o, value);
        }
        return d;
    }

    @Override
    public int __len__() {
        return stringmap___len__();
    }

    @ExposedMethod(doc = BuiltinDocs.dict___len___doc)
    final int stringmap___len__() {
        return table.size();
    }

    @Override
    public boolean __bool__() {
        return table.size() != 0;
    }

    @Override
    public PyObject __finditem__(String key) {
        if (key == null) {
            return null;
        }
        return table.get(key);
    }

    @Override
    public PyObject __finditem__(PyObject key) {
        if (key instanceof PyUnicode) {
            return __finditem__(((PyUnicode)key).internedString());
        }
        return table.get(key);
    }

    public PyObject __getitem__(String key) {
        PyObject o = __finditem__(key);
        if (null == o) {
            throw Py.KeyError(key);
        } else {
            return o;
        }
    }

    @Override
    public PyObject __getitem__(PyObject key) {
        return stringmap___getitem__(key);
    }

    @ExposedMethod(doc = BuiltinDocs.dict___getitem___doc)
    final PyObject stringmap___getitem__(PyObject key) {
        if (key instanceof PyUnicode) {
            return __getitem__(((PyUnicode)key).internedString());
        } else {
            PyObject o = __finditem__(key);
            if (null == o) {
                throw Py.KeyError(key);
            } else {
                return o;
            }
        }
    }

    @Override
    public PyObject __iter__() {
        return stringmap___iter__();
    }

    @ExposedMethod(doc = BuiltinDocs.dict___iter___doc)
    final PyObject stringmap___iter__() {
        return stringmap_keys();
    }

    @Override
    public void __setitem__(String key, PyObject value) {
        if (value == null) {
            table.remove(key);
        } else {
            table.put(key, value);
        }
    }

    @Override
    public void __setitem__(PyObject key, PyObject value) {
        stringmap___setitem__(key, value);
    }

    @ExposedMethod(doc = BuiltinDocs.dict___setitem___doc)
    final void stringmap___setitem__(PyObject key, PyObject value) {
        if (value == null) {
            table.remove(pyToKey(key));
        } else if (key instanceof PyUnicode) {
            __setitem__(((PyUnicode)key).internedString(), value);
        } else {
            table.put(key, value);
        }
    }

    @Override
    public void __delitem__(String key) {
        Object ret = table.remove(key);
        if (ret == null) {
            throw Py.KeyError(key);
        }
    }

    @Override
    public void __delitem__(PyObject key) {
        stringmap___delitem__(key);
    }

    @ExposedMethod(doc = BuiltinDocs.dict___delitem___doc)
    final void stringmap___delitem__(PyObject key) {
        if (key instanceof PyUnicode) {
            __delitem__(((PyUnicode)key).internedString());
        } else {
            Object ret = table.remove(key);
            if (ret == null) {
                throw Py.KeyError(key);
            }
        }
    }

    @ExposedMethod(doc = BuiltinDocs.dict_items_doc)
    final PyObject stringmap_items() {
        return new ItemsIter(table.entrySet());
    }

    @ExposedMethod(doc = BuiltinDocs.dict_keys_doc)
    final PyObject stringmap_keys() {
        return new KeysIter(table.keySet());
    }

    @ExposedMethod(doc = BuiltinDocs.dict_values_doc)
    final PyObject stringmap_values() {
        return new ValuesIter(table.values());
    }

    /**
     * Remove all items from the dictionary.
     */
    public void clear() {
        stringmap_clear();
    }

    @ExposedMethod(doc = BuiltinDocs.dict_clear_doc)
    final void stringmap_clear() {
        table.clear();
    }

    @Override
    public String toString() {
        return stringmap_toString();
    }

    @ExposedMethod(names = {"__repr__", "__str__"}, doc = BuiltinDocs.dict___str___doc)
    final String stringmap_toString() {
        ThreadState ts = Py.getThreadState();
        if (!ts.enterRepr(this)) {
            return "{...}";
        }
        StringBuilder buf = new StringBuilder("{");
        for (Entry<Object, PyObject> entry : table.entrySet()) {
            Object key = entry.getKey();
            if (key instanceof String) {
                // This is a bit complicated, but prevents us to duplicate
                // PyString#__repr__ logic here.
                buf.append(new PyUnicode((String)key).__repr__().toString());
            } else {
                buf.append(((PyObject)key).__repr__().toString());
            }
            buf.append(": ");
            buf.append(entry.getValue().__repr__().toString());
            buf.append(", ");
        }
        if (buf.length() > 1) {
            buf.delete(buf.length() - 2, buf.length());
        }
        buf.append("}");
        ts.exitRepr(this);
        return buf.toString();
    }

    @Override
    public boolean __contains__(PyObject o) {
        return stringmap___contains__(o);
    }

    @ExposedMethod(doc = BuiltinDocs.dict___contains___doc)
    final boolean stringmap___contains__(PyObject o) {
        return table.containsKey(pyToKey(o));
    }

    /**
     * Return this[key] if the key exists in the mapping, defaultObj is returned otherwise.
     *
     * @param key
     *            the key to lookup in the mapping.
     * @param defaultObj
     *            the value to return if the key does not exists in the mapping.
     */
    public PyObject get(PyObject key, PyObject defaultObj) {
        return stringmap_get(key, defaultObj);
    }

    @ExposedMethod(defaults = "Py.None", doc = BuiltinDocs.dict_get_doc)
    final PyObject stringmap_get(PyObject key, PyObject defaultObj) {
        PyObject obj = __finditem__(key);
        return obj == null ? defaultObj : obj;
    }

    /**
     * Return this[key] if the key exists in the mapping, None is returned otherwise.
     *
     * @param key
     *            the key to lookup in the mapping.
     */
    public PyObject get(PyObject key) {
        return stringmap_get(key, Py.None);
    }

    /**
     * Return a shallow copy of the dictionary.
     */
    public PyStringMap copy() {
        return stringmap_copy();
    }

    @ExposedMethod(doc = BuiltinDocs.dict_copy_doc)
    final PyStringMap stringmap_copy() {
        return new PyStringMap(table);
    }

    @Override
    public void update(PyObject other) {
        stringmap_update(new PyObject[] {other}, Py.NoKeywords);
    }

    /**
     * Insert all the key:value pairs from <code>dict</code> into this mapping.
     */
    @ExposedMethod(doc = BuiltinDocs.dict_update_doc)
    final void stringmap_update(PyObject[] args, String[] keywords) {
        int nargs = args.length - keywords.length;
        if (nargs > 1) {
            throw PyBuiltinCallable.DefaultInfo.unexpectedCall(nargs, false, "update", 0, 1);
        }
        if (nargs == 1) {
            PyObject arg = args[0];
            if (arg.__findattr__("keys") != null) {
                merge(arg);
            } else {
                mergeFromSeq(arg);
            }
        }
        for (int i = 0; i < keywords.length; i++) {
            __setitem__(keywords[i], args[nargs + i]);
        }
    }

    /**
     * Merge another PyObject that supports keys() with this
     * dict.
     *
     * @param other a PyObject with a keys() method
     */
    private void merge(PyObject other) {
        if (other instanceof PyStringMap) {
            table.putAll(((PyStringMap)other).table);
        } else if (other instanceof PyDictionary) {
            mergeFromKeys(other, ((PyDictionary)other).keys_as_list());
        } else {
            // TODO: fix properly
            mergeFromKeys(other, other.invoke("keys"));
        }
    }

    /**
     * Merge another PyObject via its keys() method
     *
     * @param other a PyObject with a keys() method
     * @param keys the result of other's keys() method
     */
    private void mergeFromKeys(PyObject other, PyObject keys) {
        for (PyObject key : keys.asIterable()) {
            __setitem__(key, other.__getitem__(key));
        }
    }

    /**
     * Merge any iterable object producing iterable objects of length
     * 2 into this dict.
     *
     * @param other another PyObject
     */
    private void mergeFromSeq(PyObject other) {
        PyObject pairs = other.__iter__();
        PyObject pair;

        for (int i = 0; (pair = pairs.__next__()) != null; i++) {
            try {
                pair = PySequence.fastSequence(pair, "");
            } catch(PyException pye) {
                if (pye.match(Py.TypeError)) {
                    throw Py.TypeError(String.format("cannot convert dictionary update sequence "
                                                     + "element #%d to a sequence", i));
                }
                throw pye;
            }
            int n;
            if ((n = pair.__len__()) != 2) {
                throw Py.ValueError(String.format("dictionary update sequence element #%d "
                                                  + "has length %d; 2 is required", i, n));
            }
            __setitem__(pair.__getitem__(0), pair.__getitem__(1));
        }
    }

    /**
     * Return this[key] if the key exist, otherwise insert key with a None value and return None.
     *
     * @param key
     *            the key to lookup in the mapping.
     */
    public PyObject setdefault(PyObject key) {
        return setdefault(key, Py.None);
    }

    /**
     * Return this[key] if the key exist, otherwise insert key with the value of failobj and return
     * failobj
     *
     * @param key
     *            the key to lookup in the mapping.
     * @param failobj
     *            the default value to insert in the mapping if key does not already exist.
     */
    public PyObject setdefault(PyObject key, PyObject failobj) {
        return stringmap_setdefault(key, failobj);
    }

    @ExposedMethod(defaults = "Py.None", doc = BuiltinDocs.dict_setdefault_doc)
    final PyObject stringmap_setdefault(PyObject key, PyObject failobj) {
        Object internedKey = (key instanceof PyUnicode) ? ((PyUnicode)key).internedString() : key;
        PyObject oldValue = table.putIfAbsent(internedKey, failobj);
        return oldValue == null ? failobj : oldValue;
    }

    /**
     * Return a random (key, value) tuple pair and remove the pair from the mapping.
     */
    public PyObject popitem() {
        return stringmap_popitem();
    }

    @ExposedMethod(doc = BuiltinDocs.dict_popitem_doc)
    final PyObject stringmap_popitem() {
        Iterator<Entry<Object, PyObject>> it = table.entrySet().iterator();
        if (!it.hasNext()) {
            throw Py.KeyError("popitem(): dictionary is empty");
        }
        PyTuple tuple = itemTuple(it.next());
        it.remove();
        return tuple;
    }

    // not correct - we need to determine size and remove at the same time!
    public PyObject pop(PyObject key) {
        if (table.size() == 0) {
            throw Py.KeyError("pop(): dictionary is empty");
        }
        return stringmap_pop(key, null);
    }

    public PyObject pop(PyObject key, PyObject failobj) {
        return stringmap_pop(key, failobj);
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.dict_pop_doc)
    final PyObject stringmap_pop(PyObject key, PyObject failobj) {
        PyObject value = table.remove(pyToKey(key));
        if (value == null) {
            if (failobj == null) {
                throw Py.KeyError(key);
            } else {
                return failobj;
            }
        }
        return value;
    }

    private PyTuple itemTuple(Entry<Object, PyObject> entry) {
        return new PyTuple(keyToPy(entry.getKey()), entry.getValue());
    }

    /**
     * Return a copy of the mappings list of keys. We have to take in account that we could be
     * storing String or PyObject objects
     */
    public PyObject keys() {
        return new PyList(stringmap_keys());
    }

    /**
     * Return a copy of the mappings list of values.
     */
    public PyObject values() {
        return new PyList(stringmap_values());
    }

    public PyObject items() {
        return stringmap_items();
    }

    @Override
    public int hashCode() {
        return stringmap___hash__();
    }

    @ExposedMethod(doc = BuiltinDocs.dict___hash___doc)
    final int stringmap___hash__() {
        throw Py.TypeError(String.format("unhashable type: '%.200s'", getType().fastGetName()));
    }

    @Override
    public boolean isMappingType() {
        return true;
    }

    @Override
    public boolean isSequenceType() {
        return false;
    }

    @Override
    public PyObject richCompare(PyObject other, CompareOp op) {
        if (op == CompareOp.EQ && other instanceof PyDictionary) {
            PyDictionary otherDict = (PyDictionary) other;
            if (!keys().__eq__(otherDict.dict_keys()).__bool__()) {
                return Py.False;
            }
            for (PyObject key: otherDict.keys_as_list().asIterable()) {
                if (!get(key).equals(otherDict.get(key))) {
                    return Py.False;
                }
            }
            return Py.True;
        }
        if (op == CompareOp.EQ && other instanceof PyStringMap) {
            PyStringMap otherDict = (PyStringMap) other;
            return otherDict.table.equals(table) ? Py.True : Py.False;
        }
        return super.richCompare(other, op);
    }

    private abstract class StringMapIter<T> extends PyIterator {

        protected final Iterator<T> iterator;

        private final int size;

        public StringMapIter(Collection<T> c) {
            iterator = c.iterator();
            size = c.size();
        }

        @Override
        public PyObject __next__() {
            if (table.size() != size) {
                throw Py.RuntimeError("dictionary changed size during iteration");
            }
            if (!iterator.hasNext()) {
                return null;
            }
            return stringMapNext();
        }

        protected abstract PyObject stringMapNext();
    }

    @ExposedType(name = "values_iter")
    class ValuesIter extends PyObject {
        public final PyType TYPE = PyType.fromClass(ValuesIter.class);

        private final Iterator<PyObject> iterator;
        private final int size;

        public ValuesIter(Collection<PyObject> c) {
            iterator = c.iterator();
            size = c.size();
        }

        @Override
        @ExposedMethod(names = "__next__")
        public PyObject __next__() {
            if (iterator.hasNext()) {
                return iterator.next();
            }
            throw Py.StopIteration();
        }

        @Override
        @ExposedMethod(names = "__length_hint__")
        public int __len__() {
            return size;
        }
    }

    @ExposedType(name = "keys_iter")
    class KeysIter extends PyObject {
        public final PyType TYPE = PyType.fromClass(KeysIter.class);

        private final Iterator<Object> iterator;

        private final int size;

        public KeysIter(Set<Object> s) {
            iterator = s.iterator();
            size = s.size();
        }

        @Override
        @ExposedMethod(names = "__next__")
        public PyObject __next__() {
            if (iterator.hasNext()) {
                return keyToPy(iterator.next());
            }
            throw Py.StopIteration();
        }

        @Override
        @ExposedMethod(names = "__length_hint__")
        public int __len__() {
            return size;
        }
    }

    @ExposedType(name = "items_iter")
    class ItemsIter extends PyObject {
        public final PyType TYPE = PyType.fromClass(ItemsIter.class);

        private final Iterator<Entry<Object, PyObject>> iterator;

        private final int size;

        public ItemsIter(Set<Entry<Object, PyObject>> s) {
            iterator = s.iterator();
            size = s.size();
        }


        @Override
        @ExposedMethod(names = "__length_hint__")
        public int __len__() {
            return size;
        }

        @Override
        @ExposedMethod(names = "__next__")
        public PyObject __next__() {
            return itemTuple(iterator.next());
        }
    }

    private static PyObject keyToPy(Object objKey){
        if (objKey instanceof String) {
            return PyUnicode.fromInterned((String)objKey);
        } else {
            return (PyObject)objKey;
        }
    }

    private static Object pyToKey(PyObject pyKey) {
        if (pyKey instanceof PyUnicode) {
            return ((PyUnicode)pyKey).internedString();
        } else {
            return pyKey;
        }
    }


    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        int retVal;
        Object key;
        PyObject value;
        for (Map.Entry<Object, PyObject> ent: table.entrySet()) {
        	key = ent.getKey();
        	value = ent.getValue();
            if (key instanceof PyObject) {
                retVal = visit.visit((PyObject) key, arg);
                if (retVal != 0) return retVal;
            }
            if (value != null) {
                retVal = visit.visit(value, arg);
                if (retVal != 0) return retVal;
            }
        }
        return 0;
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        return ob != null && (table.containsKey(ob) || table.containsValue(ob));
    }
}
