/* Copyright (c) Jython Developers */
package org.python.core;

import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSet;
import org.python.annotations.ExposedType;

@ExposedType(name = "property", doc = BuiltinDocs.property_doc)
public class PyProperty extends PyObject implements Traverseproc {

    public static final PyType TYPE = PyType.fromClass(PyProperty.class);

    @ExposedGet
    protected PyObject fget;

    @ExposedGet
    protected PyObject fset;

    @ExposedGet
    protected PyObject fdel;

    /** Whether this property's __doc__ was copied from its getter. */
    protected boolean docFromGetter;

    @ExposedGet(name = "__doc__")
    @ExposedSet(name = "__doc__")
    protected PyObject doc;

    public PyProperty() {
        this(TYPE);
    }

    public PyProperty(PyType subType) {
        super(subType);
    }

    @ExposedNew
    public static PyObject property_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        return new PyProperty(subtype);
    }

    @ExposedMethod(doc = BuiltinDocs.property___init___doc)
    public void property___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("property", args, keywords,
                                     new String[] {"fget", "fset", "fdel", "doc"}, 0);
        fget = ap.getPyObject(0, null);
        fget = fget == Py.None ? null : fget;
        fset = ap.getPyObject(1, null);
        fset = fset == Py.None ? null : fset;
        fdel = ap.getPyObject(2, null);
        fdel = fdel == Py.None ? null : fdel;
        doc = ap.getPyObject(3, null);

        // if no docstring given and the getter has one, use fget's
        if ((doc == null || doc == Py.None) && fget != null) {
            PyObject getDoc = fget.__findattr__("__doc__");
            if (getType() == TYPE) {
                doc = getDoc;
            } else {
                // Put __doc__ in dict of the subclass instance instead, otherwise it gets
                // shadowed by class's __doc__
                __setattr__("__doc__", getDoc);
            }
            docFromGetter = true;
        }
    }

    @ExposedGet(name = "__isabstractmethod__")
    public boolean isabstractmethod() {
        return fget != null && Abstract._PyObject_IsAbstract(fget) ||
                fset != null && Abstract._PyObject_IsAbstract(fset) ||
                fdel != null && Abstract._PyObject_IsAbstract(fdel);
    }

    @Override
    public PyObject __call__(PyObject arg1, PyObject args[], String keywords[]) {
        return fget.__call__(arg1);
    }

    @Override
    public PyObject __get__(PyObject obj, PyObject type) {
        return property___get__(obj,type);
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.property___get___doc)
    final PyObject property___get__(PyObject obj, PyObject type) {
        if (obj == null || obj == Py.None) {
            return this;
        }
        if (fget == null) {
            throw Py.AttributeError("unreadable attribute");
        }
        return fget.__call__(obj);
    }

    @Override
    public void __set__(PyObject obj, PyObject value) {
        property___set__(obj, value);
    }

    @ExposedMethod(doc = BuiltinDocs.property___set___doc)
    final void property___set__(PyObject obj, PyObject value) {
        if (fset == null) {
            throw Py.AttributeError("can't set attribute");
        }
        fset.__call__(obj, value);
    }

    @Override
    public void __delete__(PyObject obj) {
        property___delete__(obj);
    }

    @ExposedMethod(doc = BuiltinDocs.property___delete___doc)
    final void property___delete__(PyObject obj) {
        if (fdel == null) {
            throw Py.AttributeError("can't delete attribute");
        }
        fdel.__call__(obj);
    }

    @ExposedMethod
    final PyObject getter(PyObject getter) {
        return propertyCopy(getter, null, null);
    }

    @ExposedMethod
    final PyObject setter(PyObject setter) {
        return propertyCopy(null, setter, null);
    }

    @ExposedMethod
    final PyObject deleter(PyObject deleter) {
        return propertyCopy(null, null, deleter);
    }

    /**
     * Return a copy of this property with the optional addition of a get/set/del. Helper
     * method for the getter/setter/deleter methods.
     */
    private PyObject propertyCopy(PyObject get, PyObject set, PyObject del) {
        if (get == null) {
            get = fget != null ? fget : Py.None;
        }
        if (set == null) {
            set = fset != null ? fset : Py.None;
        }
        if (del == null) {
            del = fdel != null ? fdel : Py.None;
        }

        PyObject doc;
        if (docFromGetter) {
            // make _init use __doc__ from getter
            doc = Py.None;
        } else {
            doc = this.doc != null ? this.doc : Py.None;
        }

        PyProperty props = new PyProperty(getType());
        props.fget = get;
        props.fset = set;
        props.fdel = del;
        props.doc = doc;
        return props;
    }


    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        int retVal;
        if (fget != null) {
            retVal = visit.visit(fget, arg);
            if (retVal != 0) {
                return retVal;
            }
        }
        if (fset != null) {
            retVal = visit.visit(fset, arg);
            if (retVal != 0) {
                return retVal;
            }
        }
        if (fdel != null) {
            retVal = visit.visit(fdel, arg);
            if (retVal != 0) {
                return retVal;
            }
        }
        return doc == null ? 0 : visit.visit(doc, arg);
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        return ob != null && (ob == fget || ob == fset || ob == fdel || ob == doc);
    }
}
