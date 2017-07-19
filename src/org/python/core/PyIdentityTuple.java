package org.python.core;

/*
 * A very specialized tuple-like class used when detecting cycles during
 * object comparisons. This classes is different from an normal tuple
 * by hashing and comparing its elements by identity.
 */
class PyIdentityTuple extends PyObject implements Traverseproc {

    PyObject[] list;

    public PyIdentityTuple(PyObject elements[]) {
        list = elements;
    }

    @Override
    public int hashCode() {
        int x, y;
        int len = list.length;
        x = 0x345678;

        for (len--; len >= 0; len--) {
            y = System.identityHashCode(list[len]);
            x = (x + x + x) ^ y;
        }
        x ^= list.length;
        return x;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PyIdentityTuple))
            return false;
        PyIdentityTuple that = (PyIdentityTuple) o;
        if (list.length != that.list.length)
            return false;
        for (int i = 0; i < list.length; i++) {
            if (list[i] != that.list[i])
                return false;
        }
        return true;
    }


    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        if (list != null) {
            int retVal;
            for (PyObject ob: list) {
                if (ob != null) {
                    retVal = visit.visit(ob, arg);
                    if (retVal != 0) {
                        return retVal;
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        if (ob == null || list == null) {
            return false;
        }
        for (PyObject obj: list) {
            if (ob == obj) {
                return true;
            }
        }
        return false;
    }
}
