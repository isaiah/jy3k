package org.python.core;


/**
 * A builtin classmethod with a restricted number of arguments.
 */
public abstract class PyBuiltinClassMethodNarrow extends PyBuiltinMethodNarrow {
    protected PyBuiltinClassMethodNarrow(String name) {
        super(name);
    }

    protected PyBuiltinClassMethodNarrow(String name, int minArgs, int maxArgs) {
        super(name, minArgs, maxArgs);
    }

    protected PyBuiltinClassMethodNarrow(PyObject self, Info info) {
        super(self, info);
        if (info instanceof PyClassMethodDescr) {
            isWide = ((PyBuiltinMethodNarrow) ((PyClassMethodDescr) info).meth).isWide;
        }
    }

    protected PyBuiltinClassMethodNarrow(PyType type, PyObject self, Info info) {
        super(type, self, info);
        if (info instanceof PyClassMethodDescr) {
            isWide = ((PyBuiltinMethodNarrow) ((PyClassMethodDescr) info).meth).isWide;
        }
    }
    
    public PyMethodDescr makeDescriptor(PyType t) {
        return new PyClassMethodDescr(t, this);
    }


    public PyObject __call__(PyObject[] args, String[] keywords) {
        if(keywords.length != 0) {
            throw info.unexpectedCall(args.length, true);
        }
        if (isWide) {
            throw info.unexpectedCall(args.length, false);
        }
        return __call__(args);
    }

    public PyObject __call__(PyObject[] args) {
        if (isWide) {
            return __call__(args, Py.NoKeywords);
        }
        return super.__call__(args);
    }

    public PyObject __call__() {
        if (isWide) {
            return __call__(Py.EmptyObjects, Py.NoKeywords);
        }
        return super.__call__();
    }

    public PyObject __call__(PyObject arg0) {
        if (isWide) {
            return __call__(new PyObject[]{arg0}, Py.NoKeywords);
        }
        return super.__call__(arg0);
    }

    public PyObject __call__(PyObject arg0, PyObject arg1) {
        if (isWide) {
            return __call__(new PyObject[]{arg0, arg1}, Py.NoKeywords);
        }
        return super.__call__(arg0, arg1);
    }

    public PyObject __call__(PyObject arg0, PyObject arg1, PyObject arg2) {
        if (isWide) {
            return __call__(new PyObject[]{arg0, arg1, arg2}, Py.NoKeywords);
        }
        return super.__call__(arg0, arg1, arg2);
    }

    public PyObject __call__(PyObject arg0, PyObject arg1, PyObject arg2, PyObject arg3) {
        if (isWide) {
            return __call__(new PyObject[]{arg0, arg1, arg2, arg3}, Py.NoKeywords);
        }
        return super.__call__(arg0, arg1, arg2, arg3);
    }
}
