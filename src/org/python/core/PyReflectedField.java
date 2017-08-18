/*
 * Copyright (c) Corporation for National Research Initiatives
 * Copyright (c) Jython Developers
 */
package org.python.core;

import org.python.annotations.ExposedType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Untraversable
@ExposedType(name = "java_field")
public class PyReflectedField extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyReflectedField.class);

    public Field field;

    public PyReflectedField() {
            super(TYPE);
    }

    public PyReflectedField(Field field) {
        super(TYPE);
        this.field = field;
    }

    @Override
    public PyObject _doget(PyObject self) {
        Object iself = null;
        if (!Modifier.isStatic(field.getModifiers())) {
            if (self == null) {
                return this;
            }
            iself = self.getJavaProxy();
            if (iself == null) {
                iself = self;
            }
        }
        Object value;

        try {
            value = field.get(iself);
        } catch (IllegalAccessException exc) {
            throw Py.JavaError(exc);
        }

        return Py.java2py(value);
    }

    @Override
    public boolean _doset(PyObject self, PyObject value) {
        Object iself = null;
        if (!Modifier.isStatic(field.getModifiers())) {
            if (self == null) {
                throw Py.AttributeError("set instance variable as static: " + field.toString());
            }
            iself = self.getJavaProxy();
            if (iself == null) {
                iself = self;
            }
        }
        Object fvalue = Py.tojava(value, field.getType());

        try {
            field.set(iself, fvalue);
        } catch (IllegalAccessException exc) {
            throw Py.JavaError(exc);
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("<reflected field %s at %s>", field, Py.idstr(this));
    }
}
