/* Copyright (c) Jython Developers */
package org.python.modules._weakref;

import org.python.core.CompareOp;
import org.python.core.JyAttribute;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.Traverseproc;
import org.python.core.Visitproc;
import org.python.modules.gc;

/**
 * Base class for weakref types.
 */
public abstract class AbstractReference extends PyObject implements Traverseproc {

    PyObject callback;

    protected GlobalRef gref;

    public AbstractReference(PyType subType, GlobalRef gref, PyObject callback) {
        super(subType);
        this.gref = gref;
        this.callback = callback;
        gref.add(this);
    }

    void call() {
        if (callback == null)
            return;
        try {
            callback.__call__(this);
        } catch (Exception exc) {
            Py.writeUnraisable(exc, callback);
        }
    }

    protected PyObject py() {
        PyObject o = get();
        if (o == null) {
            throw Py.ReferenceError("weakly-referenced object no longer exists");
        }
        return o;
    }

    // Differentiate reference equality (equals) with what is being referred to (__eq__)
    @Override
    public boolean equals(Object ob_other) {
        return ob_other == this;
    }

    public int hashCode() {
        return gref.pythonHashCode();
    }

    public PyObject __eq__(PyObject other) {
        if (other.getClass() != getClass()) {
            return null;
        }
        PyObject pythis = get();
        PyObject pyother = ((AbstractReference) other).get();
        if (pythis == null || pyother == null) {
            return this == other ? Py.True : Py.False;
        }
        return pythis.richCompare(pyother, CompareOp.EQ);
    }

    public PyObject __ne__(PyObject other) {
        if (other.getClass() != getClass()) {
            return Py.True;
        }
        PyObject pythis = get();
        PyObject pyother = ((AbstractReference) other).get();
        if (pythis == null || pyother == null) {
            return this == other ? Py.False : Py.True;
        }
        return pythis.richCompare(pyother, CompareOp.NE);
    }

    protected PyObject get() {
        PyObject result = gref.get();
        if (result == null && (gc.getJythonGCFlags() & gc.PRESERVE_WEAKREFS_ON_RESURRECTION) != 0) {
            if (gref.cleared) {
                return null;
            }
            if ((gc.getJythonGCFlags() & gc.VERBOSE_WEAKREF) != 0) {
                gc.writeDebug("gc", "pending in get of abstract ref "+this+": "+
                        Thread.currentThread().getId());
            }
            JyAttribute.setAttr(this, JyAttribute.WEAKREF_PENDING_GET_ATTR, Thread.currentThread());
            while (!gref.cleared && result == null) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {}
                result = gref.get();
            }
            JyAttribute.delAttr(this, JyAttribute.WEAKREF_PENDING_GET_ATTR);
            if ((gc.getJythonGCFlags() & gc.VERBOSE_WEAKREF) != 0) {
                gc.writeDebug("gc", "pending of "+this+" resolved: "+
                        Thread.currentThread().getId());
                if (gref.cleared) {
                    gc.writeDebug("gc", "reference was cleared.");
                } else if (result != null){
                    gc.writeDebug("gc", "reference was restored.");
                } else {
                    gc.writeDebug("gc", "something went very wrong.");
                }
            }
            return result;
        } else {
            return result;
        }
    }


    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        return callback != null ? visit.visit(callback, arg) : 0;
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        return ob != null && callback == ob;
    }
}
