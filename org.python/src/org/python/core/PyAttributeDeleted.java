package org.python.core;

/**
 * Value of a class or instance variable when the corresponding attribute is deleted. Used only in
 * PySystemState for now.
 */
@Untraversable
class PyAttributeDeleted extends PyObject {

    final static PyAttributeDeleted INSTANCE = new PyAttributeDeleted();

    private PyAttributeDeleted() {}

    @Override
    public String toString() {
        return "";
    }

    @Override
    public Object __tojava__(Class c) {
        if (c == PyObject.class) {
            return this;
        }
        // we can't quite "delete" non-PyObject attributes; settle for
        // null or nothing
        if (c.isPrimitive()) {
            return Py.NoConversion;
        }
        return null;
    }
}
