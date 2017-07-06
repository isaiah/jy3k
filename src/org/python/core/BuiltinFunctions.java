package org.python.core;

@Untraversable
public class BuiltinFunctions extends PyBuiltinFunctionSet {
    public static final PyObject module = new PyUnicode("builtins");

    public BuiltinFunctions(String name, int index, int argcount) {
        this(name, index, argcount, argcount);
    }

    public BuiltinFunctions(String name, int index, int minargs, int maxargs) {
        super(name, index, minargs, maxargs);
    }

    @Override
    public PyObject __call__() {
        switch (this.index) {
            case 4:
                return BuiltinModule.globals();
            case 16:
                return BuiltinModule.dir();
            case 28:
                return BuiltinModule.locals();
            case 34:
                return BuiltinModule.raw_input();
            case 41:
                return BuiltinModule.vars();
            default:
                throw info.unexpectedCall(0, false);
        }
    }

    @Override
    public PyObject __call__(PyObject arg1) {
        switch (this.index) {
            case 0:
                return Py.newUnicode(BuiltinModule.chr(Py.py2long(arg1)));
            case 1:
                return Py.newLong(BuiltinModule.len(arg1));
            case 3:
                return Py.newLong(BuiltinModule.ord(arg1));
            case 5:
                return BuiltinModule.hash(arg1);
            case 7:
                return BuiltinModule.abs(arg1);
            case 9:
                return BuiltinModule.ascii(arg1);
            case 11:
                return Py.newLong(BuiltinModule.id(arg1));
            case 12:
                return BuiltinModule.sum(arg1);
            case 14:
                return Py.newBoolean(BuiltinModule.callable(arg1));
            case 16:
                return BuiltinModule.dir(arg1);
            case 18:
                return BuiltinModule.eval(arg1);
            case 19:
                BuiltinModule.execfile(arg1.asString());
                return Py.None;
            case 23:
                return BuiltinModule.hex(arg1);
            case 25:
                return BuiltinModule.intern(arg1);
            case 27:
                return BuiltinModule.iter(arg1);
            case 32:
                return BuiltinModule.oct(arg1);
            case 34:
                return BuiltinModule.raw_input(arg1);
            case 37:
                return BuiltinModule.repr(arg1);
            case 41:
                return BuiltinModule.vars(arg1);
            case 30:
                return fancyCall(new PyObject[] {arg1});
            case 31:
                return fancyCall(new PyObject[] {arg1});
            case 45:
                return BuiltinModule.reversed(arg1);
            case 46:
                return BuiltinModule.exec(arg1);
            default:
                throw info.unexpectedCall(1, false);
        }
    }

    @Override
    public PyObject __call__(PyObject arg1, PyObject arg2) {
        switch (this.index) {
            case 10:
                return Py.newBoolean(BuiltinModule.isinstance(arg1, arg2));
            case 12:
                return BuiltinModule.sum(arg1, arg2);
            case 15:
                BuiltinModule.delattr(arg1, arg2);
                return Py.None;
            case 17:
                return BuiltinModule.divmod(arg1, arg2);
            case 18:
                return BuiltinModule.eval(arg1, arg2);
            case 19:
                BuiltinModule.execfile(arg1.asString(), arg2);
                return Py.None;
            case 21:
                return BuiltinModule.getattr(arg1, arg2);
            case 22:
                return Py.newBoolean(BuiltinModule.hasattr(arg1, arg2));
            case 26:
                return Py.newBoolean(BuiltinModule.issubclass(arg1, arg2));
            case 27:
                return BuiltinModule.iter(arg1, arg2);
            case 33:
                return BuiltinModule.pow(arg1, arg2);
            case 29:
                return fancyCall(new PyObject[] {arg1, arg2});
            case 30:
                return fancyCall(new PyObject[] {arg1, arg2});
            case 31:
                return fancyCall(new PyObject[] {arg1, arg2});
            case 46:
                return BuiltinModule.exec(arg1, arg2);
            default:
                throw info.unexpectedCall(2, false);
        }
    }

    @Override
    public PyObject __call__(PyObject arg1, PyObject arg2, PyObject arg3) {
        switch (this.index) {
            case 9:
                try {
                    if (arg3 instanceof PyStringMap) {
                        PyDictionary d = new PyDictionary();
                        d.update(arg3);
                        arg3 = d;
                    }
                    PyDictionary d = (PyDictionary) arg3;
                    return BuiltinModule.apply(arg1, arg2, d);
                } catch (ClassCastException e) {
                    throw Py.TypeError("apply() 3rd argument must be a "
                                       + "dictionary with string keys");
                }
            case 18:
                return BuiltinModule.eval(arg1, arg2, arg3);
            case 19:
                BuiltinModule.execfile(arg1.asString(), arg2, arg3);
                return Py.None;
            case 21:
                return BuiltinModule.getattr(arg1, arg2, arg3);
            case 33:
                return BuiltinModule.pow(arg1, arg2, arg3);
            case 39:
                BuiltinModule.setattr(arg1, arg2, arg3);
                return Py.None;
            case 44:
                return fancyCall(new PyObject[] {arg1, arg2, arg3});
            case 29:
                return fancyCall(new PyObject[] {arg1, arg2, arg3});
            case 30:
                return fancyCall(new PyObject[] {arg1, arg2, arg3});
            case 31:
                return fancyCall(new PyObject[] {arg1, arg2, arg3});
            case 46:
                return BuiltinModule.exec(arg1, arg2, arg3);
            default:
                throw info.unexpectedCall(3, false);
        }
    }

    @Override
    public PyObject __call__(PyObject arg1, PyObject arg2, PyObject arg3, PyObject arg4) {
        switch (this.index) {
            case 44:
                return fancyCall(new PyObject[] {arg1, arg2, arg3, arg4});
            case 29:
                return fancyCall(new PyObject[] {arg1, arg2, arg3, arg4});
            case 30:
                return fancyCall(new PyObject[] {arg1, arg2, arg3, arg4});
            case 31:
                return fancyCall(new PyObject[] {arg1, arg2, arg3, arg4});
            default:
                throw info.unexpectedCall(4, false);
        }
    }

    @Override
    public PyObject fancyCall(PyObject[] args) {
        throw info.unexpectedCall(args.length, false);
    }

    @Override
    public PyObject getModule() {
        return module;
    }
}
