/* Copyright (c) 2008 Jython Developers */
package org.python.core;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.expose.MethodType;

/**
 * Readonly proxy for dictionaries (actually any mapping).
 *
 */
@ExposedType(name = "mappingproxy", isBaseType = false)
public class PyDictProxy extends PyObject implements Traverseproc {

    /** The dict proxied to. */
    PyObject dict;

    public PyDictProxy(PyObject dict) {
        super();
        this.dict = dict;
    }

    @ExposedNew
    static PyObject mappingproxy_new(PyNewWrapper new_, boolean init, PyType subtype, PyObject[] args,
                            String[] keywords) {
        ArgParser ap = new ArgParser("mappingproxy", args, keywords, new String[] {"object"}, 0);
        PyObject d = ap.getPyObject(0);
        return new PyDictProxy(d);
    }

    @ExposedMethod(names = "__iter__")
    public PyObject mappingproxy___iter__() {
        return PyObject.getIter(dict);
    }

    @Override
    public PyObject __finditem__(PyObject key) {
        return dict.__finditem__(key);
    }

    @Override
    public int __len__() {
        return dict.__len__();
    }

    @ExposedMethod
    public PyObject mappingproxy___getitem__(PyObject key) {
        return dict.__getitem__(key);
    }

    @ExposedMethod(names = {"__contains__", "has_key"})
    public boolean mappingproxy___contains__(PyObject value) {
        return Abstract.PySequence_Contains(dict, value);
    }

    @ExposedMethod(defaults = "null")
    public PyObject mappingproxy_get(PyObject key, PyObject default_object) {
        return dict.invoke("get", key, default_object);
    }

    @ExposedMethod
    public PyObject mappingproxy_keys() {
        return dict.invoke("keys");
    }

    @ExposedMethod
    public PyObject mappingproxy_values() {
        return dict.invoke("values");
    }

    @ExposedMethod
    public PyObject mappingproxy_items() {
        return dict.invoke("items");
    }

    @ExposedMethod
    public PyObject mappingproxy_iterkeys() {
        return dict.invoke("iterkeys");
    }

    @ExposedMethod
    public PyObject mappingproxy_itervalues() {
        return dict.invoke("itervalues");
    }

    @ExposedMethod
    public PyObject mappingproxy_iteritems() {
        return dict.invoke("iteritems");
    }

    @ExposedMethod
    public PyObject mappingproxy_copy() {
        return dict.invoke("copy");
    }

    @ExposedMethod(type = MethodType.BINARY)
    public PyObject mappingproxy___lt__(PyObject other) {
        return dict.__lt__(other);
    }

    @ExposedMethod(type = MethodType.BINARY)
    public PyObject mappingproxy___le__(PyObject other) {
        return dict.__le__(other);
    }

    @ExposedMethod(type = MethodType.BINARY)
    public PyObject mappingproxy___eq__(PyObject other) {
        return dict.__eq__(other);
    }

    @ExposedMethod(type = MethodType.BINARY)
    public PyObject mappingproxy___ne__(PyObject other) {
        return dict.__ne__(other);
    }

    @ExposedMethod(type = MethodType.BINARY)
    public PyObject mappingproxy___gt__(PyObject other) {
        return dict.__gt__(other);
    }

    @ExposedMethod(type = MethodType.BINARY)
    public PyObject mappingproxy___ge__(PyObject other) {
        return dict.__ge__(other);
    }

    @Override
    public String toString() {
        return String.format("mappingproxy(%s)", dict.toString());
    }

    @Override
    public boolean isMappingType() {
        return true;
    }

    @Override
    public boolean isSequenceType() {
        return false;
    }


    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        return dict == null ? 0 : visit.visit(dict, arg);
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        return ob != null && ob == dict;
    }
}
