/*
 * Copyright (c) Corporation for National Research Initiatives
 * Copyright (c) Jython Developers
 */
package org.python.core;

import org.python.annotations.ExposedClassMethod;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;
import org.python.bootstrap.Import;
import org.python.expose.MethodType;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.python.core.CompareOp.EQ;


/**
 * A builtin python dictionary.
 */
@ExposedType(name = "dict", doc = BuiltinDocs.dict_doc)
public class PyDictionary extends PyObject implements ConcurrentMap, Traverseproc, PyDict {

    public static final PyType TYPE = PyType.fromClass(PyDictionary.class);

    static {
        // Ensure dict is not Hashable
        TYPE.object___setattr__("__hash__", Py.None);
    }

    private final ConcurrentMap<PyObject, PyObject> internalMap;

    public ConcurrentMap<PyObject, PyObject> getMap() {
        return internalMap;
    }

    /**
     * Create an empty dictionary.
     */
    public PyDictionary() {
        this(TYPE);
    }

    /**
     * Create a dictionary of type with the specified initial capacity.
     */
    public PyDictionary(PyType type, int capacity) {
        super(type);
        internalMap = new ConcurrentHashMap<>(capacity);
    }

    /**
     * For derived types
     */
    public PyDictionary(PyType type) {
        super(type);
        internalMap = new ConcurrentHashMap<>();
    }

    /**
     * Create a dictionary with keys and values list, used when create keyvalue-only parameter defaults
     */
    public PyDictionary(String[] keys, PyObject[] values) {
        this();
        ConcurrentMap<PyObject, PyObject> map = getMap();
        for (int i = 0; i < keys.length; i++) {
            map.put(Py.newUnicode(keys[i]), values[i]);
        }
    }

    /**
     * Create a new dictionary which is based on given map.
     */
    public PyDictionary(Map<PyObject, PyObject> map) {
        this(TYPE, map);
    }

    public PyDictionary(ConcurrentMap<PyObject, PyObject> backingMap, boolean useBackingMap) {
        super(TYPE);
        internalMap = backingMap;
    }

    public PyDictionary(PyType type, ConcurrentMap<PyObject, PyObject> backingMap, boolean useBackingMap) {
        super(type);
        internalMap = backingMap;
    }

    /**
     * Create a new dictionary which is populated with entries the given map.
     */
    public PyDictionary(PyType type, Map<PyObject, PyObject> map) {
        this(type);
        getMap().putAll(map);
    }

    /**
     * Create a new dictionary without initializing table. Used for dictionary
     * factories, with different backing maps, at the cost that it prevents us from making table be final.
     */
    // TODO we may want to revisit this API, but our chain calling of super makes this tough
    protected PyDictionary(PyType type, boolean initializeBacking) {
        super(type);
        if (initializeBacking) {
            internalMap = new ConcurrentHashMap<>();
        } else {
            internalMap = null; // for later initialization
        }
    }

    /**
     * Create a new dictionary with the element as content.
     *
     * @param elements The initial elements that is inserted in the dictionary. Even numbered elements
     *                 are keys, odd numbered elements are values.
     */
    public PyDictionary(PyObject elements[]) {
        this();
        ConcurrentMap<PyObject, PyObject> map = getMap();
        for (int i = 0; i < elements.length; i += 2) {
            map.put(elements[i], elements[i + 1]);
        }
    }

    public static boolean checkExact(PyObject o) {
        return o instanceof PyDictionary && o.getType() == TYPE;
    }

    public static PyDictionary fromKV(String[] keys, PyObject[] values) {
        return new PyDictionary(keys, values);
    }

    @ExposedNew
    public static PyObject dict_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        PyDictionary self = new PyDictionary(subtype);
        return self;
    }

    @ExposedMethod(doc = BuiltinDocs.dict___init___doc)
    public final void dict___init__(PyObject[] args, String[] keywords) {
        updateCommon(args, keywords, "dict");
    }

    public static PyObject fromkeys(PyObject keys) {
        return fromkeys(keys, Py.None);
    }

    public static PyObject fromkeys(PyObject keys, PyObject value) {
        return dict_fromkeys(TYPE, keys, value);
    }

    @ExposedClassMethod(defaults = "Py.None", doc = BuiltinDocs.dict_fromkeys_doc)
    static PyObject dict_fromkeys(PyType type, PyObject keys, PyObject value) {
        PyObject d = type.__call__();
        for (PyObject o : keys.asIterable()) {
            d.__setitem__(o, value);
        }
        return d;
    }

    @Override
    public int __len__() {
        return dict___len__();
    }

    @ExposedMethod(doc = BuiltinDocs.dict___len___doc)
    public final int dict___len__() {
        return getMap().size();
    }

    @Override
    public boolean isTrue() {
        return getMap().size() != 0;
    }

    @Override
    public PyObject __finditem__(int index) {
        throw Py.TypeError("loop over non-sequence");
    }

    @Override
    public PyObject __finditem__(PyObject key) {
        return getMap().get(key);
    }

    @ExposedSlot(SlotFunc.GETITEM)
    public static PyObject getitem(PyObject map, PyObject key) {
        PyDictionary self = (PyDictionary) map;
        PyObject result = self.getMap().get(key);
        if (result != null) {
            return result;
        }

        // Look up __missing__ method if we're a subclass.
        PyType type = self.getType();
        if (type != TYPE) {
            PyObject missing = type.lookup("__missing__");
            if (missing != null) {
                return missing.__get__(self, type).__call__(key);
            }
        }
        throw Py.KeyError(key);
    }

    @Override
    public void __setitem__(PyObject key, PyObject value) {
        dict___setitem__(this, key, value);
    }

    @ExposedSlot(SlotFunc.SETITEM)
    public static void dict___setitem__(PyObject self, PyObject key, PyObject value) {
        ((PyDictionary) self).getMap().put(key, value);
    }

    @Override
    public void __delitem__(PyObject key) {
        dict___delitem__(key);
    }

    @ExposedMethod(doc = BuiltinDocs.dict___delitem___doc)
    public final void dict___delitem__(PyObject key) {
        Object ret = getMap().remove(key);
        if (ret == null) {
            throw Py.KeyError(key.toString());
        }
    }

    @ExposedMethod(doc = BuiltinDocs.dict___iter___doc)
    public final PyObject dict___iter__() {
        return new ValuesIter(getMap().keySet());
    }

    @Override
    public String toString() {
        return dict_toString();
    }

    @ExposedMethod(names = {"__repr__", "__str__"}, doc = BuiltinDocs.dict___str___doc)
    public final String dict_toString() {
        ThreadState ts = Py.getThreadState();
        if (!ts.enterRepr(this)) {
            return "{...}";
        }

        StringBuilder buf = new StringBuilder("{");
        for (Entry<PyObject, PyObject> entry : getMap().entrySet()) {
            buf.append(BuiltinModule.repr(entry.getKey()).toString());
            buf.append(": ");
            buf.append(BuiltinModule.repr(entry.getValue()).toString());
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
    public PyObject richCompare(PyObject other, CompareOp op) {
        PyObject res;
        if (!(other instanceof PyDict)) {
            res = Py.NotImplemented;
        } else if (op == CompareOp.NE || op == CompareOp.EQ) {
            res = op.bool(dict_equal(other));
        } else {
            res = Py.NotImplemented;
        }
        return res;
    }

    final int dict_equal(PyObject otherObj) {
        PyDictionary other = (PyDictionary) otherObj;
        int an = getMap().size();
        int bn = other.getMap().size();
        if (an != bn) {
            return -1;
        }

        PyList akeys = keys_as_list();
        for (int i = 0; i < an; i++) {
            PyObject akey = akeys.pyget(i);
            PyObject bvalue = other.__finditem__(akey);
            if (bvalue == null) {
                return -1;
            }
            PyObject avalue = __finditem__(akey);
            if (avalue == null) {
                return -1;
            }
            if (!avalue.richCompare(bvalue, EQ).isTrue()) {
                return -1;
            }
        }
        return 0;
    }

    @ExposedMethod(doc = BuiltinDocs.dict___contains___doc)
    public final boolean dict___contains__(PyObject o) {
        return getMap().containsKey(o);
    }

    /**
     * Return this[key] if the key exists in the mapping, defaultObj is returned
     * otherwise.
     *
     * @param key        the key to lookup in the dictionary.
     * @param defaultObj the value to return if the key does not exists in the mapping.
     */
    public PyObject get(PyObject key, PyObject defaultObj) {
        return dict_get(key, defaultObj);
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.dict_get_doc)
    public final PyObject dict_get(PyObject key, PyObject defaultObj) {
        PyObject o = getMap().get(key);
        return o == null ? defaultObj : o;
    }

    /**
     * Return this[key] if the key exists in the mapping, None
     * is returned otherwise.
     *
     * @param key the key to lookup in the dictionary.
     */
    public PyObject get(PyObject key) {
        return dict_get(key, Py.None);
    }

    /**
     * Return a shallow copy of the dictionary.
     */
    @Override
    public PyDictionary copy() {
        return dict_copy();
    }

    @ExposedMethod(doc = BuiltinDocs.dict_copy_doc)
    public final PyDictionary dict_copy() {
        return new PyDictionary(getMap()); // no need to clone()
    }

    /**
     * Remove all items from the dictionary.
     */
    public void clear() {
        dict_clear();
    }

    @ExposedMethod(doc = BuiltinDocs.dict_clear_doc)
    public final void dict_clear() {
        getMap().clear();
    }

    /**
     * Insert all the key:value pairs from <code>d</code> into
     * this dictionary.
     */
    @Override
    public void update(PyObject other) {
        dict_update(new PyObject[]{other}, Py.NoKeywords);
    }

    @ExposedMethod(doc = BuiltinDocs.dict_update_doc)
    public final void dict_update(PyObject[] args, String[] keywords) {
        updateCommon(args, keywords, "update");
    }

    public void updateCommon(PyObject[] args, String[] keywords, String methName) {
        int nargs = args.length - keywords.length;
        if (nargs == 1) {
            PyObject arg = args[0];

            Object proxy = arg.getJavaProxy();
            if (proxy instanceof Map) {
                merge((Map) proxy);
            } else if (arg.__findattr__("keys") != null) {
                merge(arg);
            } else {
                mergeFromSeq(arg);
            }
        }
        for (int i = 0; i < keywords.length; i++) {
            dict___setitem__(this, Py.newUnicode(keywords[i]), args[nargs + i]);
        }
    }

    private void merge(Map<Object, Object> other) {
        for (Entry<Object, Object> entry : other.entrySet()) {
            dict___setitem__(this, Py.java2py(entry.getKey()), Py.java2py(entry.getValue()));
        }
    }


    /**
     * Merge another PyObject that supports keys() with this
     * dict.
     *
     * @param other a PyObject with a keys() method
     */
    public void merge(PyObject other) {
        if (other instanceof PyDictionary) {
            getMap().putAll(((PyDictionary) other).getMap());
        } else if (other instanceof PyStringMap) {
            mergeFromKeys(other, ((PyStringMap) other).keys());
        } else {
            mergeFromKeys(other, other.invoke("keys"));
        }
    }

    /**
     * Merge another PyObject via its keys() method
     *
     * @param other a PyObject with a keys() method
     * @param keys  the result of other's keys() method
     */
    private void mergeFromKeys(PyObject other, PyObject keys) {
        ThreadState ts = Py.getThreadState();
        for (PyObject key : keys.asIterable()) {
            Abstract.PyObject_SetItem(ts, this, key, Abstract.PyObject_GetItem(ts, other, key));
        }
    }

    /**
     * Merge any iterable object producing iterable objects of length
     * 2 into this dict.
     *
     * @param other another PyObject
     */
    private void mergeFromSeq(PyObject other) {
        PyObject pairs = PyObject.getIter(other);
        PyObject pair;
        int i = 0;
        try {
            for (; ; ) {
                pair = PyObject.getIter(PyObject.iterNext(pairs));
                dict___setitem__(this, PyObject.iterNext(pair), PyObject.iterNext(pair));
                try {
                    PyObject.iterNext(pair);
                    throw Py.ValueError(String.format("dictionary update sequence element #%d "
                            + "should have exactly 2 elements", i));
                } catch (PyException e) {
                    // expected
                }
                i++;
            }
        } catch (PyException e) {
            if (!e.match(Py.StopIteration)) {
                throw e;
            }
        }

//        throw Py.TypeError(String.format("cannot convert dictionary update sequence "
//                + "element #%d to a sequence", i));
    }

    /**
     * Return this[key] if the key exist, otherwise insert key with
     * a None value and return None.
     *
     * @param key the key to lookup in the dictionary.
     */
    public PyObject setdefault(PyObject key) {
        return dict_setdefault(key, Py.None);
    }

    /**
     * Return this[key] if the key exist, otherwise insert key with
     * the value of failobj and return failobj
     *
     * @param key     the key to lookup in the dictionary.
     * @param failobj the default value to insert in the dictionary
     *                if key does not already exist.
     */
    public PyObject setdefault(PyObject key, PyObject failobj) {
        return dict_setdefault(key, failobj);
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.dict_setdefault_doc)
    public final PyObject dict_setdefault(PyObject key, PyObject failobj) {
        PyObject oldValue = getMap().putIfAbsent(key, failobj);
        return oldValue == null ? failobj : oldValue;
    }

    // XXX: needs __doc__ but CPython does not define setifabsent
//    @ExposedMethod(defaults = "Py.None")
//    public final PyObject dict_setifabsent(PyObject key, PyObject failobj) {
//        PyObject oldValue = getMap().putIfAbsent(key, failobj);
//        return oldValue == null ? Py.None : oldValue;
//    }

    /**
     * Return a value based on key
     * from the dictionary.
     */
    public PyObject pop(PyObject key) {
        return dict_pop(key, null);
    }

    /**
     * Return a value based on key
     * from the dictionary or default if that key is not found.
     */
    public PyObject pop(PyObject key, PyObject defaultValue) {
        return dict_pop(key, defaultValue);
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.dict_pop_doc)
    public final PyObject dict_pop(PyObject key, PyObject defaultValue) {
        if (!getMap().containsKey(key)) {
            if (defaultValue == null) {
                throw Py.KeyError(key);
            }
            return defaultValue;
        }
        return getMap().remove(key);
    }


    /**
     * Return a random (key, value) tuple pair and remove the pair
     * from the dictionary.
     */
    public PyObject popitem() {
        return dict_popitem();
    }

    @ExposedMethod(doc = BuiltinDocs.dict_popitem_doc)
    public final PyObject dict_popitem() {
        Optional<Entry<PyObject, PyObject>> ret = getMap().entrySet().stream().findFirst();
        if (ret.isPresent()) {
            Entry<PyObject, PyObject> entry = ret.get();
            PyObject key = entry.getKey();
            getMap().remove(key);
            return new PyTuple(key, entry.getValue());
        }
        throw Py.KeyError("popitem(): dictionary is empty");
    }

    public final PyList keys_as_list() {
        return new PyList(getMap().keySet());
    }

    public final PyObject dict_iteritems() {
        return new ItemsIter(getMap().entrySet());
    }

    public final PyObject dict_iterkeys() {
        return new ValuesIter(getMap().keySet());
    }

    /**
     * Returns a dict_keys on the dictionary's keys
     */
    @ExposedMethod(doc = BuiltinDocs.dict_keys_doc)
    public PyObject dict_keys() {
        return new PyDictionaryViewKeys(this);
    }

    /**
     * Returns a dict_items on the dictionary's items
     */
    @ExposedMethod(doc = BuiltinDocs.dict_items_doc)
    public PyObject dict_items() {
        return new PyDictionaryViewItems(this);
    }

    /**
     * Returns a dict_values on the dictionary's values
     */
    @ExposedMethod(doc = BuiltinDocs.dict_values_doc)
    public PyObject dict_values() {
        return new PyDictionaryViewValues(this);
    }

    @Override
    public int hashCode() {
        return dict___hash__();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof PyDictionary) {
            return ((PyDictionary) obj).getMap().equals(getMap());
        } else if (obj instanceof Map) {
            return getMap().equals((Map) obj);
        }
        return false;
    }

    @ExposedMethod
    final int dict___hash__() {
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

    @ExposedType(name = "dict_valueiterator")
    public static class ValuesIter extends PyObject {
        public final PyType TYPE = PyType.fromClass(ValuesIter.class);

        private final Iterator<PyObject> iterator;

        private final int size;

        public ValuesIter(Collection<PyObject> values) {
            iterator = values.iterator();
            size = values.size();
        }

        @ExposedSlot(SlotFunc.ITER)
        public static PyObject dict_valueiterator___iter__(PyObject self) {
            return self;
        }

        @ExposedSlot(SlotFunc.ITER_NEXT)
        public static PyObject dict_valueiterator___next__(PyObject iter) {
            ValuesIter self = (ValuesIter) iter;
            if (!self.iterator.hasNext()) {
                throw Py.StopIteration();
            }
            return self.iterator.next();
        }

        @ExposedMethod(names = "__length_hint__")
        public int __length_hint__() {
            return size;
        }

        @ExposedMethod
        public PyObject __reduce__() {
            PyObject builtins = Import.importModule("builtins");
            return new PyTuple(builtins.__findattr__("iter"), new PyTuple(new PyList(iterator)));
        }
    }

    @ExposedType(name = "dict_itemiterator")
    public static class ItemsIter extends PyObject {

        private final Iterator<Entry<PyObject, PyObject>> iterator;

        private final int size;

        public ItemsIter(Set<Entry<PyObject, PyObject>> items) {
            iterator = items.iterator();
            size = items.size();
        }

        @ExposedSlot(SlotFunc.ITER)
        public static PyObject __iter__(PyObject self) {
            return self;
        }

        @ExposedSlot(SlotFunc.ITER_NEXT)
        public static PyObject next(PyObject iter) {
            ItemsIter self = (ItemsIter) iter;
            if (!self.iterator.hasNext()) {
                throw Py.StopIteration();
            }
            Entry<PyObject, PyObject> entry = self.iterator.next();
            return new PyTuple(entry.getKey(), entry.getValue());
        }

        @Override
        public int __len__() {
            return size;
        }
    }

    @ExposedType(name = "dict_values", base = PyObject.class, doc = "")
    public class PyDictionaryViewValues extends BaseDictionaryView {
        public final PyType TYPE = PyType.fromClass(PyDictionaryViewValues.class);

        public PyDictionaryViewValues(PyDictionary dvDict) {
            super(dvDict);
        }

        @ExposedMethod(doc = BuiltinDocs.set___iter___doc)
        public final PyObject dict_values___iter__() {
            return new ValuesIter(dvDict.getMap().values());
        }

        @ExposedMethod(doc = BuiltinDocs.set___len___doc)
        final int dict_values___len__() {
            return dict_view___len__();
        }

        @ExposedMethod(names = {"__repr__", "__str__"}, doc = BuiltinDocs.set___str___doc)
        final String dict_values_toString() {
            return dict_view_toString();
        }
    }

    @ExposedType(name = "dict_keys", base = PyObject.class)
    public static class PyDictionaryViewKeys extends BaseDictionaryView {
        public final PyType TYPE = PyType.fromClass(PyDictionaryViewKeys.class);

        public PyDictionaryViewKeys(PyDictionary dvDict) {
            super(dvDict);
        }

        @ExposedSlot(SlotFunc.ITER)
        public static PyObject dict_keys___iter__(PyObject self) {
            return new ValuesIter(((PyDictionaryViewKeys) self).dvDict.getMap().keySet());
        }

        @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___ne___doc)
        public final PyObject dict_keys___ne__(PyObject otherObj) {
            return dict_view___ne__(otherObj);
        }

        @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___eq___doc)
        public final PyObject dict_keys___eq__(PyObject otherObj) {
            return dict_view___eq__(otherObj);
        }

        @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___lt___doc)
        public final PyObject dict_keys___lt__(PyObject otherObj) {
            return dict_view___lt__(otherObj);
        }

        @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___gt___doc)
        public final PyObject dict_keys___gt__(PyObject otherObj) {
            return dict_view___gt__(otherObj);
        }

        @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___ge___doc)
        final PyObject dict_keys___ge__(PyObject otherObj) {
            return dict_view___ge__(otherObj);
        }

        @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___le___doc)
        public final PyObject dict_keys___le__(PyObject otherObj) {
            return dict_view___le__(otherObj);
        }

        @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___or___doc)
        final PyObject dict_keys___or__(PyObject otherObj) {
            PySet result = new PySet(dvDict);
            result.set_update(new PyObject[]{otherObj}, new String[]{});
            return result;
        }

        @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___xor___doc)
        public final PyObject dict_keys___xor__(PyObject otherObj) {
            PySet result = new PySet(dvDict);
            result.set_symmetric_difference_update(otherObj);
            return result;
        }

        @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___sub___doc)
        public final PyObject dict_keys___sub__(PyObject otherObj) {
            PySet result = new PySet(dvDict);
            result.set_difference_update(new PyObject[]{otherObj}, new String[]{});
            return result;
        }

        @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___and___doc)
        public final PyObject dict_keys___and__(PyObject otherObj) {
            PySet result = new PySet(dvDict);
            result.set_intersection_update(new PyObject[]{otherObj}, new String[]{});
            return result;
        }

        @ExposedMethod(doc = BuiltinDocs.set___contains___doc)
        public final boolean dict_keys___contains__(PyObject item) {
            return Abstract.PySequence_Contains(dvDict, item);
        }

        @ExposedMethod(names = "__repr__", doc = BuiltinDocs.set___repr___doc)
        public final String dict_keys_toString() {
            return dict_view_toString();
        }
    }

    @ExposedType(name = "dict_items")
    public static class PyDictionaryViewItems extends BaseDictionaryView {
        public final PyType TYPE = PyType.fromClass(PyDictionaryViewItems.class);

        public PyDictionaryViewItems(PyDictionary dvDict) {
            super(dvDict);
        }

        @ExposedSlot(SlotFunc.ITER)
        public static PyObject dict_items___iter__(PyObject self) {
            return new ItemsIter(((PyDictionaryViewItems) self).dvDict.getMap().entrySet());
        }

        @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___ne___doc)
        public final PyObject dict_items___ne__(PyObject otherObj) {
            return dict_view___ne__(otherObj);
        }

        @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___eq___doc)
        public final PyObject dict_items___eq__(PyObject otherObj) {
            return dict_view___eq__(otherObj);
        }

        @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___lt___doc)
        public final PyObject dict_items___lt__(PyObject otherObj) {
            return dict_view___lt__(otherObj);
        }

        @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___gt___doc)
        public final PyObject dict_items___gt__(PyObject otherObj) {
            return dict_view___gt__(otherObj);
        }

        @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___ge___doc)
        public final PyObject dict_items___ge__(PyObject otherObj) {
            return dict_view___ge__(otherObj);
        }

        @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___le___doc)
        public final PyObject dict_items___le__(PyObject otherObj) {
            return dict_view___le__(otherObj);
        }

        @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___or___doc)
        public final PyObject dict_items___or__(PyObject otherObj) {
            PySet result = new PySet(dvDict.dict_iteritems());
            result.set_update(new PyObject[]{otherObj}, new String[]{});
            return result;
        }

        @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___xor___doc)
        public final PyObject dict_items___xor__(PyObject otherObj) {
            PySet result = new PySet(dvDict.dict_iteritems());
            result.set_symmetric_difference_update(otherObj);
            return result;
        }

        @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___sub___doc)
        public final PyObject dict_items___sub__(PyObject otherObj) {
            PySet result = new PySet(dvDict.dict_iteritems());
            result.set_difference_update(new PyObject[]{otherObj}, new String[]{});
            return result;
        }

        @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.set___and___doc)
        public final PyObject dict_items___and__(PyObject otherObj) {
            PySet result = new PySet(dvDict.dict_iteritems());
            result.set_intersection_update(new PyObject[]{otherObj}, new String[]{});
            return result;
        }

        @ExposedMethod(doc = BuiltinDocs.set___contains___doc)
        public final boolean dict_items___contains__(PyObject item) {
            if (item instanceof PyTuple) {
                PyTuple tupleItem = (PyTuple) item;
                if (tupleItem.size() == 2) {
                    SimpleEntry entry = new SimpleEntry(tupleItem.get(0), tupleItem.get(1));
                    return dvDict.entrySet().contains(entry);
                }
            }
            return false;
        }

        @ExposedMethod(names = "__repr__", doc = BuiltinDocs.set___repr___doc)
        public final String dict_keys_toString() {
            return dict_view_toString();
        }
    }

    public Set<PyObject> filter(Predicate<Map.Entry<PyObject, PyObject>> predicate, Function<Entry<PyObject, PyObject>, PyObject> mapper) {
        return internalMap.entrySet().stream().filter(predicate).map(mapper).collect(Collectors.toSet());
    }
    /*
     * The following methods implement the java.util.Map interface which allows PyDictionary to be
     * passed to java methods that take java.util.Map as a parameter. Basically, the Map methods are
     * a wrapper around the PyDictionary's Map container stored in member variable 'table'. These
     * methods convert java Object to PyObjects on insertion, and PyObject to Objects on retrieval.
     */

    /**
     * @see java.util.Map#entrySet()
     */
    public Set entrySet() {
        return new PyMapEntrySet(getMap().entrySet());
    }

    /**
     * @see java.util.Map#keySet()
     */
    public Set keySet() {
        return new PyMapKeyValSet(getMap().keySet());
    }

    /**
     * @see java.util.Map#values()
     */
    public Collection values() {
        return new PyMapKeyValSet(getMap().values());
    }

    /**
     * @see java.util.Map#putAll(Map map)
     */
    public void putAll(Map map) {
        for (Object o : map.entrySet()) {
            Entry entry = (Entry) o;
            getMap().put(Py.java2py(entry.getKey()), Py.java2py(entry.getValue()));
        }
    }

    /**
     * @see java.util.Map#remove(Object key)
     */
    public Object remove(Object key) {
        return tojava(getMap().remove(Py.java2py(key)));
    }

    /**
     * @see java.util.Map#put(Object key, Object value)
     */
    public Object put(Object key, Object value) {
        return tojava(getMap().put(Py.java2py(key), Py.java2py(value)));
    }

    /**
     * @see java.util.Map#get(Object key)
     */
    public Object get(Object key) {
        return tojava(getMap().get(Py.java2py(key)));
    }

    /**
     * @see java.util.Map#containsValue(Object key)
     */
    public boolean containsValue(Object value) {
        return getMap().containsValue(Py.java2py(value));
    }

    /**
     * @see java.util.Map#containsValue(Object key)
     */
    public boolean containsKey(Object key) {
        return getMap().containsKey(Py.java2py(key));
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return getMap().isEmpty();
    }

    /**
     * @see java.util.Map#size()
     */
    public int size() {
        return getMap().size();
    }

    /**
     * Convert return values to java objects
     */
    static Object tojava(Object val) {
        return val == null ? null : ((PyObject) val).__tojava__(Object.class);
    }

    public Object putIfAbsent(Object key, Object value) {
        return tojava(getMap().putIfAbsent(Py.java2py(key), Py.java2py(value)));
    }

    public boolean remove(Object key, Object value) {
        return getMap().remove(Py.java2py(key), Py.java2py(value));
    }

    public boolean replace(Object key, Object oldValue, Object newValue) {
        return getMap().replace(Py.java2py(key), Py.java2py(oldValue), Py.java2py(newValue));
    }

    public Object replace(Object key, Object value) {
        return tojava(getMap().replace(Py.java2py(key), Py.java2py(value)));
    }


    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        int retVal;
        for (Map.Entry<PyObject, PyObject> ent : internalMap.entrySet()) {
            retVal = visit.visit(ent.getKey(), arg);
            if (retVal != 0) {
                return retVal;
            }
            if (ent.getValue() != null) {
                retVal = visit.visit(ent.getValue(), arg);
                if (retVal != 0) {
                    return retVal;
                }
            }
        }
        return 0;
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        return ob != null && (internalMap.containsKey(ob) || internalMap.containsValue(ob));
    }
}

/**
 * Basic implementation of Entry that just holds onto a key and value and returns them.
 */
class SimpleEntry implements Entry {

    protected Object key;

    protected Object value;

    public SimpleEntry(Object key, Object value) {
        this.key = key;
        this.value = value;
    }

    public Object getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Map.Entry)) {
            return false;
        }
        Map.Entry e = (Map.Entry) o;
        return eq(key, e.getKey()) && eq(value, e.getValue());
    }

    private static boolean eq(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    @Override
    public int hashCode() {
        return ((key == null) ? 0 : key.hashCode()) ^ ((value == null) ? 0 : value.hashCode());
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }

    public Object setValue(Object val) {
        throw new UnsupportedOperationException("Not supported by this view");
    }
}

/**
 * Wrapper for a Entry object returned from the java.util.Set object which in turn is returned by
 * the entrySet method of java.util.Map. This is needed to correctly convert from PyObjects to java
 * Objects. Note that we take care in the equals and hashCode methods to make sure these methods are
 * consistent with Entry objects that contain java Objects for a value so that on the java side they
 * can be reliable compared.
 */
class PyToJavaMapEntry extends SimpleEntry {

    /**
     * Create a copy of the Entry with Py.None converted to null
     */
    PyToJavaMapEntry(Entry entry) {
        super(entry.getKey(), entry.getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Entry)) {
            return false;
        }
        Entry me = new JavaToPyMapEntry((Entry) o);
        return o.equals(me);
    }

    // tojava is called in getKey and getValue so the raw key and value can be
    // used to create a new SimpleEntry in getEntry.
    @Override
    public Object getKey() {
        return PyDictionary.tojava(key);
    }

    @Override
    public Object getValue() {
        return PyDictionary.tojava(value);
    }

    /**
     * @return an entry that returns the original values given to this entry.
     */
    public Entry getEntry() {
        return new SimpleEntry(key, value);
    }

    @Override
    public int hashCode() {
        return ((key == null) ? 0 : key.hashCode()) ^ ((value == null) ? 0 : value.hashCode());
    }

}

/**
 * MapEntry Object for java MapEntry objects passed to the java.util.Set interface which is returned
 * by the entrySet method of PyDictionary. Essentially like PyTojavaMapEntry, but going the other
 * way converting java Objects to PyObjects.
 */
class JavaToPyMapEntry extends SimpleEntry {

    public JavaToPyMapEntry(Entry entry) {
        super(Py.java2py(entry.getKey()), Py.java2py(entry.getValue()));
    }
}

/**
 * Wrapper collection class for the keySet and values methods of java.util.Map
 */
class PyMapKeyValSet extends PyMapSet {

    PyMapKeyValSet(Collection coll) {
        super(coll);
    }

    @Override
    Object toJava(Object o) {
        return PyDictionary.tojava(o);
    }

    @Override
    Object toPython(Object o) {
        return Py.java2py(o);
    }
}

/**
 * Set wrapper for the entrySet method. Entry objects are wrapped further in JavaToPyMapEntry and
 * PyToJavaMapEntry. Note - The set interface is reliable for standard objects like strings and
 * integers, but may be inconsistent for other types of objects since the equals method may return
 * false for Entry object that hold more elaborate PyObject types. However, we insure that this
 * interface works when the Entry object originates from a Set object retrieved from a PyDictionary.
 */
class PyMapEntrySet extends PyMapSet {

    PyMapEntrySet(Collection coll) {
        super(coll);
    }

    // We know that PyMapEntrySet will only contains entries, so if the object being passed in is
    // null or not an Entry, then return null which will match nothing for remove and contains
    // methods.
    @Override
    Object toPython(Object o) {
        if (o == null || !(o instanceof Entry)) {
            return null;
        }
        if (o instanceof PyToJavaMapEntry) {
            // Use the original entry from PyDictionary
            return ((PyToJavaMapEntry) o).getEntry();
        } else {
            return new JavaToPyMapEntry((Entry) o);
        }
    }

    @Override
    Object toJava(Object o) {
        return new PyToJavaMapEntry((Entry) o);
    }
}

/**
 * PyMapSet serves as a wrapper around Set Objects returned by the java.util.Map interface of
 * PyDictionary. entrySet, values and keySet methods return this type for the java.util.Map
 * implementation. This class is necessary as a wrapper to convert PyObjects to java Objects for
 * methods that return values, and convert Objects to PyObjects for methods that take values. The
 * translation is necessary to provide java access to jython dictionary objects. This wrapper also
 * provides the expected backing functionality such that changes to the wrapper set or reflected in
 * PyDictionary.
 */
abstract class PyMapSet extends AbstractSet {

    private final Collection coll;

    PyMapSet(Collection coll) {
        this.coll = coll;
    }

    abstract Object toJava(Object obj);

    abstract Object toPython(Object obj);

    @Override
    public int size() {
        return coll.size();
    }

    @Override
    public boolean contains(Object o) {
        return coll.contains(toPython(o));
    }

    @Override
    public boolean remove(Object o) {
        return coll.remove(toPython(o));
    }

    @Override
    public void clear() {
        coll.clear();
    }

    // Iterator wrapper class returned by the PyMapSet iterator
    // method. We need this wrapper to return PyToJavaMapEntry objects
    // for the 'next()' method.
    class PySetIter implements Iterator {
        Iterator itr;

        PySetIter(Iterator itr) {
            this.itr = itr;
        }

        public boolean hasNext() {
            return itr.hasNext();
        }

        public Object next() {
            return toJava(itr.next());
        }

        public void remove() {
            itr.remove();
        }
    }

    @Override
    public Iterator iterator() {
        return new PySetIter(coll.iterator());
    }
}
