package org.python.modules._abc;

import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.core.PyObject;
import org.python.core.PyType;

@ExposedType(name = "_abc_data_type")
public class PyABCData extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyABCData.class);

    public PyABCData() {
        super(TYPE);
    }

    public PyABCData(PyType subtype) {
        super(subtype);
    }

    @ExposedNew
    public static PyObject _abc_data_new() {
        return new PyABCData();
    }
}
