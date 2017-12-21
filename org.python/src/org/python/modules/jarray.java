// Copyright (c) Corporation for National Research Initiatives
package org.python.modules;
import org.python.core.PyArray;
import org.python.core.PyObject;
import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;

import static org.python.core.PyArray.char2class;

@ExposedModule
public class jarray {
    @ExposedFunction
    public static PyArray array(PyObject seq, char typecode) {
//        if (typecode instanceof PyLong) {
        return PyArray.array(seq, char2class(typecode));
//        }
//        return PyArray.array(seq, typecode);
    }

    public static PyArray array(PyObject seq, Class type) {
        return PyArray.array(seq, type);
    }
    @ExposedFunction
    public static PyArray zeros(int n, char typecode) {
        return PyArray.zeros(n, typecode);
    }

    public static PyArray zeros(int n, Class type) {
        return PyArray.zeros(n, type);
    }

    public static Class<?> array_class(PyObject type) {
        return PyArray.array_class((Class<?>)type.__tojava__(Class.class));
    }
}
