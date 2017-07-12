package org.python.core;

import org.python.bootstrap.QuadFunction;
import org.python.bootstrap.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@Untraversable
public class PyBuiltinFunction extends PyBuiltinCallable {
    private Supplier<PyObject> call;
    private Function<PyObject, PyObject> func;
    private BiFunction<PyObject, PyObject, PyObject> biFunc;
    private TriFunction<PyObject, PyObject, PyObject, PyObject> triFunc;
    private QuadFunction<PyObject, PyObject, PyObject, PyObject, PyObject> quadFunction;
    private Function<PyObject[], PyObject> varargFunc;
    private BiFunction<PyObject[], String[], PyObject> wideFunc;

    public static PyBuiltinFunction named(String name, String doc) {
        return new PyBuiltinFunction(name, 0,0, doc);
    }

    protected PyBuiltinFunction(String name, int minargs, int maxargs, String doc) {
        super(new DefaultInfo(name, minargs, maxargs));
        this.doc = doc == null ? null : doc;
    }

    public boolean isMappingType() {
        return false;
    }

    public boolean isNumberType() {
        return false;
    }

    public boolean isSequenceType() {
        return false;
    }

    public PyBuiltinCallable bind(PyObject self) {
        throw Py.TypeError("Can't bind a builtin function");
    }

    public String toString() {
        return "<built-in function " + info.getName() + ">";
    }

    public PyBuiltinFunction with(Supplier<PyObject> func) {
        updateArgs(0);
        this.call = func;
        return this;
    }

    public PyBuiltinFunction with(Function<PyObject, PyObject> func) {
        updateArgs(1);
        this.func = func;
        return this;
    }

    public PyBuiltinFunction with(BiFunction<PyObject, PyObject, PyObject> func) {
        updateArgs(2);
        this.biFunc = func;
        return this;
    }
    public PyBuiltinFunction with(TriFunction<PyObject, PyObject, PyObject, PyObject> func) {
        updateArgs(3);
        this.triFunc = func;
        return this;
    }

    public PyBuiltinFunction with(QuadFunction<PyObject, PyObject, PyObject, PyObject, PyObject> func) {
        updateArgs(4);
        this.quadFunction = func;
        return this;
    }

    public PyBuiltinFunction vararg(Function<PyObject[], PyObject> func) {
        this.varargFunc = func;
        return this;
    }

    public PyBuiltinFunction wide(BiFunction<PyObject[], String[], PyObject> func) {
        this.wideFunc = func;
        return this;
    }


    public PyObject fancyCall(PyObject[] args) {
        if (varargFunc != null) return varargFunc.apply(args);
        if (wideFunc != null) return wideFunc.apply(args, Py.NoKeywords);
        throw info.unexpectedCall(args.length, false);
    }

    public PyObject __call__(PyObject[] args) {
        int nargs = args.length;
        switch(nargs){
            case 0:
                return __call__();
            case 1:
                return __call__(args[0]);
            case 2:
                return __call__(args[0], args[1]);
            case 3:
                return __call__(args[0], args[1], args[2]);
            case 4:
                return __call__(args[0], args[1], args[2], args[3]);
            default:
                return fancyCall(args);
        }
    }

    public PyObject __call__(PyObject[] args, String[] kws) {
        if (kws.length != 0) {
            if (wideFunc != null) return wideFunc.apply(args, kws);
            throw Py.TypeError(fastGetName() + "() takes no keyword arguments");
        }
        if (varargFunc != null) return varargFunc.apply(args);
        return __call__(args);
    }


    public PyObject __call__() {
        if (this.call != null) return call.get();
        if (wideFunc != null) return wideFunc.apply(Py.EmptyObjects, Py.NoKeywords);
        throw info.unexpectedCall(0, false);
    }

    public PyObject __call__(PyObject arg1) {
        if (this.func != null) return func.apply(arg1);
        if (wideFunc != null) return wideFunc.apply(new PyObject[]{arg1}, Py.NoKeywords);
        throw info.unexpectedCall(1, false);
    }

    public PyObject __call__(PyObject arg1, PyObject arg2) {
        if (this.biFunc != null) return biFunc.apply(arg1, arg2);
        if (wideFunc != null) return wideFunc.apply(new PyObject[]{arg1, arg2}, Py.NoKeywords);
        throw info.unexpectedCall(2, false);
    }

    public PyObject __call__(PyObject arg1, PyObject arg2, PyObject arg3) {
        if (this.triFunc != null) return triFunc.apply(arg1, arg2, arg3);
        if (wideFunc != null) return wideFunc.apply(new PyObject[]{arg1, arg2, arg3}, Py.NoKeywords);
        throw info.unexpectedCall(3, false);
    }

    public PyObject __call__(PyObject arg1, PyObject arg2, PyObject arg3, PyObject arg4) {
        if (quadFunction != null) return quadFunction.apply(arg1, arg2, arg3, arg4);
        if (wideFunc != null) return wideFunc.apply(new PyObject[]{arg1, arg2, arg3, arg4}, Py.NoKeywords);
        throw info.unexpectedCall(4, false);
    }

    private void updateArgs(int args) {
        if (info.getMinargs() > args) {
            info.setMinargs(args);
        }
        if (info.getMaxargs() < args) {
            info.setMaxargs(args);
        }
    }

}
