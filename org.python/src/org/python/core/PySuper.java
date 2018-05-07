/* Copyright (c) Jython Developers */
package org.python.core;

import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;

/**
 * The Python super type.
 */
@ExposedType(name = "super", doc = BuiltinDocs.super_doc)
public class PySuper extends PyObject implements Traverseproc {

    public static final PyType TYPE = PyType.fromClass(PySuper.class);

    @ExposedGet(name = "__thisclass__")
    protected PyType superType;

    @ExposedGet(name = "__self__")
    protected PyObject obj;

    @ExposedGet(name = "__self_class__")
    protected PyType objType;

    public PySuper() {
        this(TYPE);
    }

    public PySuper(PyType subType) {
        super(subType);
    }

    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject super_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        return new PySuper(subtype);
    }

    @ExposedMethod
    public void super___init__(PyObject[] args, String[] keywords) {
        PyType type;
        PyObject obj = null;
        PyType objType = null;
        if (keywords.length != 0 || args.length < 2) {
            PyFrame frame = Py.getThreadState().frame;
            if (frame.f_fastlocals == null || frame.f_fastlocals.length < 1) {
                throw Py.RuntimeError("super(): no arguments");
            }
            obj = frame.f_fastlocals[0];
            if (obj == null) {
                throw Py.RuntimeError("super(): arg[0] deleted");
            }
            PyObject arg0 = frame.getLocals().__finditem__("__class__");
            if (arg0 == null) {
                throw Py.RuntimeError("super(): empty __class__ cell");
            } else if (!(arg0 instanceof PyType)) {
                throw Py.TypeError("super: argument 1 must be type");
            }
            type = (PyType) arg0;

        } else {
            if (!(args[0] instanceof PyType)) {
                throw Py.TypeError("super: argument 1 must be type");
            }
            type = (PyType)args[0];
            if (args.length == 2 && args[1] != Py.None) {
                obj = args[1];
            }
        }
        if (obj != null) {
            objType = supercheck(type, obj);
        }
        this.superType = type;
        this.obj = obj;
        this.objType = objType;
    }

    /**
     * Check that a super() call makes sense.  Return a type object.
     *
     * obj can be a new-style class, or an instance of one:
     * 
     * - If it is a class, it must be a subclass of 'type'.  This case is used for class
     * methods; the return value is obj.
     * 
     * - If it is an instance, it must be an instance of 'type'.  This is the normal case;
     * the return value is obj.__class__.
     *
     * But... when obj is an instance, we want to allow for the case where objType is not
     * a subclass of type, but obj.__class__ is!  This will allow using super() with a
     * proxy for obj.
     *
     * @param type the PyType superType associated with the super
     * @param obj a the PyObject obj associated with the super
     * @return a PyType superType
     */
    private PyType supercheck(PyType type, PyObject obj) {
        // Check for first bullet above (special case)
        if (obj instanceof PyType && ((PyType)obj).isSubType(type)) {
            return (PyType)obj;
        }

        // Normal case
        PyType objType = obj.getType();
        if (objType.isSubType(type)) {
            return objType;
        } else {
            // Try the slow way
            PyObject classAttr = obj.__findattr__("__class__");
            if (classAttr != null && classAttr instanceof PyType) {
                if (((PyType)classAttr).isSubType(type)) {
                    return (PyType)classAttr;
                }
            }
        }
        throw Py.TypeError("super(type, obj): obj must be an instance or subtype of type");
    }

    public PyObject __findattr_ex__(String name) {
        return super___findattr_ex__(name);
    }

    final PyObject super___findattr_ex__(String name) {
        if (objType != null && name != "__class__") {
            PyObject descr = objType.super_lookup(superType, name);
            if (descr != null) {
                return descr.__get__(objType == obj ? null : obj, objType);
            }
        }
        return super.__findattr_ex__(name);
    }

    @ExposedSlot(SlotFunc.GETATTRO)
    public static PyObject getattro(PyObject super$, String name) {
        PySuper self = (PySuper) super$;
        PyObject ret = self.super___findattr_ex__(name);
        if (ret == null) {
            self.noAttributeError(name);
        }
        return ret;
    }

    public PyObject __get__(PyObject obj, PyObject type) {
        return super___get__(obj, type);
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.super___get___doc)
    final PyObject super___get__(PyObject obj, PyObject type) {
        if (obj == null || obj == Py.None || this.obj != null) {
            return this;
        }
        if (getType() != TYPE) {
            // If an instance of a (strict) subclass of super, call its type
            return getType().__call__(type, obj);
        } else {
            // Inline the common case
            PyType objType = supercheck(this.superType, obj);
            PySuper newsuper = new PySuper();
            newsuper.superType = this.superType;
            newsuper.obj = obj;
            newsuper.objType = objType;
            return newsuper;
        }
    }

    public String toString() {
        String superTypeName = superType != null ? superType.fastGetName() : "NULL";
        if (objType != null) {
            return String.format("<super: <class '%s'>, <%s object>>", superTypeName,
                                 objType.fastGetName());
        } else {
            return String.format("<super: <class '%s'>, NULL>", superTypeName);
        }
    }

    public PyType getSuperType() {
        return superType;
    }

    public PyObject getObj() {
        return obj;
    }

    public PyType getObjType() {
        return objType;
    }


    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        int retVal;
        if (superType != null) {
            retVal = visit.visit(superType, arg);
            if (retVal != 0) {
                return retVal;
            }
        }
        if (obj != null) {
            retVal = visit.visit(obj, arg);
            if (retVal != 0) {
                return retVal;
            }
        }
        return objType == null ? 0 : visit.visit(objType, arg);
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        return ob != null && (ob == superType || ob == obj || ob == objType);
    }
}
