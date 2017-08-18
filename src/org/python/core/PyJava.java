package org.python.core;

import jdk.dynalink.beans.StaticClass;
import org.python.annotations.ExposedClassMethod;
import org.python.annotations.ExposedType;

@ExposedType(name = "Java")
public class PyJava extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyJava.class);

    public PyJava() {
        super(TYPE);
    }

    @ExposedClassMethod
    public static Object type(final PyType _self, final String objTypeName) throws ClassNotFoundException {
        return StaticClass.forClass(Class.forName(objTypeName));
    }
}
