package org.python.modules._abc;

import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;
import org.python.core.Abstract;
import org.python.core.Call;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PyFrozenSet;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.core.PyUnicode;

import javax.swing.table.AbstractTableModel;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@ExposedModule(name = "_abc")
public class ABCModule {

    private final static AtomicInteger abcInvalidationCounter = new AtomicInteger(0);

    @ExposedFunction
    public static PyObject _abc_init(PyObject self) {
        computeAbstractMethods(self);
        PyABCData data = new PyABCData();
        Abstract._PyObject_SetAttrId(self, "_abc_impl", data);
        return Py.None;
    }

    @ExposedFunction
    public static PyObject _abc_register(PyObject self, PyObject subclass) {
        if (!(subclass instanceof PyType)) {
            throw Py.TypeError("Can only register classes");
        }
        if (Py.isSubClass(subclass, self)) {
            /* Already a subclass */
            return subclass;
        }
        if (Py.isSubClass(self, subclass)) {
            /* This would create a cycle, which is bad for the algorithm below. */
            throw Py.RuntimeError("Refusing to create an inheritance cycle");
        }
        PyABCData imp = getImpl(self);
        if (imp == null) {
            return Py.None;
        }
        imp.addToRegistry(subclass);
        abcInvalidationCounter.incrementAndGet();
        return subclass;
    }

    @ExposedFunction
    public static PyObject _abc_instancecheck(PyObject self, PyObject instance) {
        PyABCData impl = getImpl(self);
        PyObject subclass = Abstract._PyObject_GetAttrId(instance, "__class__");
        if (impl.abcCache.contains(subclass)) {
            return Py.True;
        }
        PyObject subtype = instance.getType();
        Supplier<PyObject> handler = () -> Py.None;
        if (subclass == subtype) {
            if (abcInvalidationCounter.get() == impl.abcNegativeCacheVersion) {
                if (impl.abcNegativeCache.contains(subclass)) {
                    return Py.False;
                }
            }
            return Call._PyOBject_CallMethodIdObjArgs(self, "__subclasscheck__", handler, subclass);
        }
        PyObject result = Call._PyOBject_CallMethodIdObjArgs(self, "__subclasscheck__", handler, subclass);
        if (Abstract.PyObject_IsTrue(Py.getThreadState(), result)) {
            return result;
        }
        return Call._PyOBject_CallMethodIdObjArgs(self, "__subclasscheck__", handler, subtype);
    }

    @ExposedFunction
    public static PyObject _abc_subclasscheck(PyObject self, PyObject subclass) {
        if (!(subclass instanceof PyType)) {
            throw Py.TypeError("issubclass() arg 1 must be a class");
        }

        PyABCData impl = getImpl(self);
        /* 1. Check cache */
        if (impl.abcCache.contains(subclass)) {
            return Py.True;
        }
        /* 2. Check negative cache; may have to invalidate */
        if (impl.abcNegativeCacheVersion < abcInvalidationCounter.get()) {
            impl.abcNegativeCache.clear();
            impl.abcNegativeCacheVersion = abcInvalidationCounter.get();
        } else {
            if (impl.abcNegativeCache.contains(subclass)) {
                return Py.False;
            }
        }
        /* 3. Check the subclass hook */
        PyObject ok = Call._PyOBject_CallMethodIdObjArgs(self, "__subclasshook__", () -> Py.NotImplemented, subclass);
        if (ok == Py.True) {
            impl.abcCache.add(subclass);
            return ok;
        }
        if (ok == Py.False) {
            impl.abcNegativeCache.add(subclass);
            return ok;
        }

        if (ok != Py.NotImplemented) {
            throw Py.AssertionError("__subclasshook__ must return either False, True or NotImplemented");
        }

        /* 4. Check if it's a direct subclass. */
        PyTuple mro = ((PyType) subclass).getMro();
        for (PyObject mroItem : mro.getArray()) {
            if (self == mroItem) {
                impl.abcCache.add(subclass);
                return Py.True;
            }
        }

        /* 5. Check if it's a subclass of a registered class (recursive). */
        if (subclassCheckRegistery(impl, subclass)) {
            return Py.True;
        }

        /* 6. Check if it's subclass of a subclass (recursive). */
        PyObject subclasses = Call.PyObject_CallMethod(self, "__subclasses__");
        if (!(subclasses instanceof PyList)) {
            throw Py.TypeError("__subclasses__ must return a list");
        }

        for (PyObject scls : ((PyList) subclasses).getArray()) {
            if (Py.isSubClass(subclass, scls)) {
                impl.abcCache.add(subclass);
                return Py.True;
            }
        }
        impl.abcNegativeCache.add(subclass);
        return Py.False;
    }

    private static boolean subclassCheckRegistery(PyABCData impl, PyObject subclass) {
        if (impl.abcRegistry.contains(subclass)) {
            return true;
        }
        for (PyObject scls : impl.abcRegistry) {
            if (Py.isSubClass(subclass, scls)) {
                impl.abcCache.add(subclass);
                return true;
            }
        }
        return false;
    }

    @ExposedFunction
    public static void _get_dump(PyObject self) {}

    @ExposedFunction
    public static void _reset_registry(PyObject self) {
        PyABCData impl = getImpl(self);
        impl.abcRegistry.clear();
    }

    @ExposedFunction
    public static void _reset_caches(PyObject self) {
        PyABCData impl = getImpl(self);
        impl.abcCache.clear();
        impl.abcNegativeCache.clear();
    }

    @ExposedFunction
    public static int get_cache_token() {
        return abcInvalidationCounter.get();
    }

    private static PyABCData getImpl(PyObject self) {
        try {
            PyObject impl = Abstract._PyObject_GetAttrId(self, "_abc_impl");
            return (PyABCData) impl;
        } catch (PyException e) {
            if (e.match(Py.AttributeError)) {
                return null;
            }
            throw e;
        }
    }

    private static void computeAbstractMethods(PyObject self) {
        /* 1. direct abstract methods. */
        PyObject ns = Abstract._PyObject_GetAttrId(self, "__dict__");
        Set<PyObject> abstracts;
        if (PyDictionary.checkExact(ns)) {
            abstracts = ((PyDictionary) ns).filter(e -> Abstract._PyObject_IsAbstract(e.getValue()), Map.Entry::getKey);
        } else if (ns instanceof PyStringMap) {
            abstracts = ((PyStringMap) ns).filter(e -> Abstract._PyObject_IsAbstract(e.getValue()), e -> {
                Object key = e.getKey();
                if (key instanceof PyObject) {
                    return (PyObject) key;
                }
                return new PyUnicode(key.toString());
            });
        } else {
            abstracts = Abstract.PyMapping_Items(ns).filter(obj -> {
                PyObject value = Abstract.PySequence_GetItem(obj, 1);
                return Abstract._PyObject_IsAbstract(value);
            }).map(obj -> Abstract.PySequence_GetItem(obj, 0)).collect(Collectors.toSet());
        }
        /* 2. inherited abstract methods. */
        PyObject bases = Abstract._PyObject_GetAttrId(self, "__bases__");
        if (!(bases instanceof PyTuple)) {
            throw Py.TypeError("__bases__ is not tuple");
        }
        for (PyObject base : ((PyTuple) bases).getArray()) {
            try {
                PyObject baseAbstracts = Abstract._PyObject_GetAttrId(base, "__abstractmethods__");
                Abstract._PySequence_Stream(baseAbstracts).forEach(key -> {
                    try {
                        PyObject value = Abstract._PyObject_GetAttrId(self, key.asString());
                        if (Abstract._PyObject_IsAbstract(value)) {
                            abstracts.add(key);
                        }
                    } catch (PyException e) {
                        if (!e.match(Py.AttributeError)) {
                            throw e;
                        }
                    }
                });
            } catch (PyException e) {
                if (e.match(Py.AttributeError)) {
                    continue;
                }
                throw e;
            }
        }
        Abstract._PyObject_SetAttrId(self, "__abstractmethods__", new PyFrozenSet(abstracts));
    }
}
