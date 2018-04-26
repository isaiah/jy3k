package org.python.modules._abc;

import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;
import org.python.core.Abstract;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PyFrozenSet;
import org.python.core.PyObject;
import org.python.core.PyType;

import javax.swing.table.AbstractTableModel;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ExposedModule(name = "_abc")
public class ABCModule {

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
        PyABCData imp = getImpl(self);
        if (imp == null) {
            return Py.None;
        }
//        if (impl == NULL) {
//            return NULL;
//        }
//        if (_add_to_weak_set(&impl->_abc_registry, subclass) < 0) {
//            Py_DECREF(impl);
//            return NULL;
//        }
//        Py_DECREF(impl);
//
//        /* Invalidate negative cache */
//        abc_invalidation_counter++;
        return subclass;
    }

    @ExposedFunction
    public static boolean _abc_instancecheck(PyObject self, PyObject instance) {
        return false;
    }

    @ExposedFunction
    public static boolean _abc_subclasscheck(PyObject self, PyObject subclass) {
        return false;
    }

    @ExposedFunction
    public static void _get_dump(PyObject self) {}

    @ExposedFunction
    public static void _reset_registry(PyObject self) {}

    @ExposedFunction
    public static void _reset_caches(PyObject self) {}

    @ExposedFunction
    public static void get_cache_token() {}

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
        PyObject ns = Abstract._PyObject_GetAttrId(self, "__dict__");
        Set<PyObject> abstracts;
        if (PyDictionary.checkExact(ns)) {
            abstracts = ((PyDictionary) ns).filter(e -> Abstract._PyObject_IsAbstract(e.getValue()), Map.Entry::getKey);
        } else {
            PyObject items = Abstract.PyMapping_Items(self);
            for (int i = 0; i < Abstract.PyObject_Size(self, Py.getThreadState()); i++) {
            }
            abstracts = new HashSet<>();
        }
        Abstract._PyObject_SetAttrId(self, "__abstractmethods__", new PyFrozenSet(abstracts));
    }
}
