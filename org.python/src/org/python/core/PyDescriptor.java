package org.python.core;

public abstract class PyDescriptor extends PyObject implements Traverseproc {

    protected PyType dtype;

    protected String name;

    @Override
    public PyObject __findattr_ex__(String field) {
        if (field.equals("__qualname__")) {
            return new PyUnicode(String.format("%s.%s", dtype.getName(), name));
        }
        return super.__findattr_ex__(field);
    }

    protected void checkReceiver(PyObject[] args) {
        if (args.length == 0) {
            throw Py.TypeError(String.format("descriptor '%s' of '%s' object needs an argument", name, dtype.fastGetName()));
        }
    }

    protected static PyObject checkCallerType(PyType dtype, PyObject caller) {
        PyType type = caller.getType();
        if (type == dtype || type.isSubType(dtype)) {
            return caller;
        }
        String msg = String.format("descriptor 'requires a '%s' object but received a '%s'",
                                   dtype.fastGetName(), type.fastGetName());
//        name, dtype.fastGetName(), type.fastGetName());
        throw Py.TypeError(msg);
    }
    
    protected void checkGetterType(PyType type) {
        if (type == dtype || type.isSubType(dtype)) {
            return;
        }
        String msg = String.format("descriptor '%s' for '%s' objects doesn't apply to '%s' object",
                                   name, dtype.fastGetName(), type.fastGetName());
        throw Py.TypeError(msg);
    }


    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        return dtype != null ? visit.visit(dtype,  arg) : 0;
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        return ob == dtype;
    }
}
