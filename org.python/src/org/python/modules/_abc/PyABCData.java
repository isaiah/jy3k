package org.python.modules._abc;

import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyType;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@ExposedType(name = "_abc_data_type")
public class PyABCData extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyABCData.class);

    final Set<PyObject> abcRegistry = Collections.newSetFromMap(new WeakHashMap<>());
    final Set<PyObject> abcCache = Collections.newSetFromMap(new WeakHashMap<>());
    final Set<PyObject> abcNegativeCache = Collections.newSetFromMap(new WeakHashMap<>());

    int abcNegativeCacheVersion = 0;

    public PyABCData() {
        super(TYPE);
    }

    public PyABCData(PyType subtype) {
        super(subtype);
    }

    @ExposedNew
    public static PyObject _abc_data_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        return new PyABCData(subtype);
    }

    protected void addToRegistry(PyObject subclass) {
        abcRegistry.add(subclass);
    }
}
