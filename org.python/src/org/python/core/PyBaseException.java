/* Copyright (c) 2008 Jython Developers */
package org.python.core;

import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSet;
import org.python.annotations.ExposedType;

/**
 * The base class for all standard Python Exceptions.
 *
 */
@ExposedType(name = "BaseException", doc = BuiltinDocs.BaseException_doc)
public class PyBaseException extends PyObject implements Traverseproc {

    public static final PyType TYPE = PyType.fromClass(PyBaseException.class);

    /** Exception message. */
    private PyObject message = Py.EmptyByte;

    /** Exception's arguments. */
    @ExposedGet
    public PyObject args = Py.EmptyTuple;

    /** Exception's underlying dictionary, lazily created. */
    public PyObject __dict__;

    /** The reference to the wrapping PyException instance */
    protected PyException wrapper;

    @ExposedGet
    public PyObject __cause__;

    @ExposedGet
    @ExposedSet
    public boolean __suppress_context__;

    @ExposedGet
    public PyObject __context__;

    @ExposedGet
    public PyObject __traceback__;

    public PyBaseException() {
        super(TYPE);
    }

    public PyBaseException(PyType subType) {
        super(subType);
    }

    public void __init__(PyObject[] args, String[] keywords) {
        BaseException___init__(args, keywords);
    }

    @ExposedNew
    public static PyObject BaseException_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        PyBaseException self = new PyBaseException(subtype);
        if (args.length == 0) {
            self.args = new PyTuple();
        } else {
            self.args = args[0];
            if (args.length == 1) {
                self.message = args[0];
            }
        }

        self.__context__ = Py.None;
        self.__suppress_context__ = false;
        return self;
    }

    @ExposedMethod(doc = BuiltinDocs.BaseException___init___doc)
    public final void BaseException___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser(getType().getName(), args, keywords, "args");
        ap.noKeywords();
    }

    public PyObject with_traceback(PyObject tb) {
        return BaseException_with_traceback(tb);
    }

    @ExposedMethod(doc = BuiltinDocs.BaseException_with_traceback_doc)
    public final PyObject BaseException_with_traceback(PyObject tb) {
        __traceback__ = tb;
        return this;
    }

    @Override
    public PyObject __reduce__() {
        return BaseException___reduce__();
    }

    @ExposedMethod(doc = BuiltinDocs.BaseException___reduce___doc)
    public final PyObject BaseException___reduce__() {
        if (__dict__ != null) {
            return new PyTuple(getType(), args, __dict__);
        } else {
            return new PyTuple(getType(), args);
        }
    }

    public PyObject __setstate__(PyObject state) {
        return BaseException___setstate__(state);
    }

    @ExposedMethod(doc = BuiltinDocs.BaseException___setstate___doc)
    public final PyObject BaseException___setstate__(PyObject state) {
        if (state != Py.None) {
            if (!(state instanceof PyStringMap) && !(state instanceof PyDictionary)) {
                throw Py.TypeError("state is not a dictionary");
            }
            for (PyObject key : state.asIterable()) {
                __setattr__((PyUnicode)key, state.__finditem__(key));
            }
        }
        return Py.None;
    }

    @Override
    public PyObject __findattr_ex__(String name) {
        return BaseException___findattr__(name);
    }

    final PyObject BaseException___findattr__(String name) {
        if (__dict__ != null) {
            PyObject attr = __dict__.__finditem__(name);
            if (attr != null) {
                return attr;
            }
        }

        return super.__findattr_ex__(name);
    }

    @Override
    public void __setattr__(String name, PyObject value) {
        BaseException___setattr__(name, value);
    }

    @ExposedMethod(doc = BuiltinDocs.BaseException___setattr___doc)
    public final void BaseException___setattr__(String name, PyObject value) {
        ensureDict();
        super.__setattr__(name, value);
    }

    @Override
    public PyObject fastGetDict() {
        return __dict__;
    }

    @Override
    @ExposedGet(name = "__dict__")
    public PyObject getDict() {
        ensureDict();
        return __dict__;
    }

    @Override
    @ExposedSet(name = "__dict__")
    public void setDict(PyObject val) {
        if (!(val instanceof PyStringMap) && !(val instanceof PyDictionary)) {
            throw Py.TypeError("__dict__ must be a dictionary");
        }
        __dict__ = val;
    }

    @ExposedSet(name = "__traceback__")
    public void setTraceback(PyObject val) {
        if (val != Py.None && !Py.isInstance(val, PyTraceback.TYPE)) {
            throw Py.TypeError("__traceback__ must be a traceback");
        }
        __traceback__ = val;
    }

    @ExposedSet(name = "__context__")
    public void setContext(PyObject val) {
        ensureException(val);
        __context__ = val;
    }

    @ExposedSet(name = "__cause__")
    public void setCause(PyObject val) {
        ensureException(val);
        if (PyException.isExceptionClass(val)) {
            __cause__ = val.__call__(Py.EmptyObjects);
        } else {
            __cause__ = val;
        }
        if (val != null) {
            __suppress_context__ = true;
        }
    }

    private void ensureDict() {
        // XXX: __dict__ should really be volatile
        if (__dict__ == null) {
            __dict__ = new PyStringMap();
        }
    }

    private void ensureException(PyObject val) {
        if (val != Py.None && !PyException.isExceptionClass(val) && !PyException.isExceptionInstance(val)) {
            throw Py.TypeError("exception cause must be None or derive from BaseException");
        }
    }

    @ExposedMethod(doc = BuiltinDocs.BaseException___str___doc)
    public final PyObject BaseException___str__() {
        ThreadState ts = Py.getThreadState();
        // CPython issue6108: if __str__ has been overridden in the subclass, unicode()
        // should return the message returned by __str__ as used to happen before this
        // method was implemented
        PyType type = getType();
        PyObject[] where = new PyObject[1];
        PyObject str = type.lookup_where("__str__", where);
        if (str != null && where[0] != TYPE) {
            // Unlike str(), __str__ can return unicode (i.e. return the equivalent
            // of unicode(e.__str__()) instead of unicode(str(e)))
            return Abstract.PyObject_Str(ts, str.__get__(this, type).__call__());
        }
        
        switch (args.__len__()) {
        case 0:
            return new PyUnicode("");
        case 1:
            return Abstract.PyObject_Str(ts, args.__getitem__(0));
        default:
            return Abstract.PyObject_Str(ts, args);
        }
    }

    @Override
    public String toString() {
        return BaseException_toString().asString();
    }

    @ExposedMethod(names = "__repr__", doc = BuiltinDocs.BaseException___repr___doc)
    public final PyUnicode BaseException_toString() {
        PyObject reprSuffix = BuiltinModule.repr(args);
        String name = getType().fastGetName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot != -1) {
            name = name.substring(lastDot + 1);
        }
        return new PyUnicode(name + reprSuffix.toString());
    }

    @ExposedSet(name = "args")
    public void setArgs(PyObject val) {
        args = PyTuple.fromIterable(val);
    }

    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        int retValue;
        if (message != null) {
            retValue = visit.visit(message, arg);
            if (retValue != 0) {
                return retValue;
            }
        }
        if (args != null) {
            retValue = visit.visit(args, arg);
            if (retValue != 0) {
                return retValue;
            }
        }
        return __dict__ != null ? visit.visit(__dict__, arg) : 0;
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        return ob != null && (ob == message || ob == args || ob == __dict__);
    }
}
