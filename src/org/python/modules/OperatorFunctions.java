package org.python.modules;

import org.python.core.CompareOp;
import org.python.core.Py;
import org.python.core.PyBuiltinFunctionSet;
import org.python.core.PyObject;
import org.python.core.PySlice;
import org.python.core.Untraversable;

@Untraversable
public class OperatorFunctions extends PyBuiltinFunctionSet
{
    public static final PyObject module = Py.newUnicode("operator");

    public OperatorFunctions(String name, int index, int argcount) {
        this(name, index, argcount, argcount);
    }

    public OperatorFunctions(String name, int index, int minargs, int maxargs)
    {
        super(name, index, minargs, maxargs);
    }

    @Override
    public PyObject getModule() {
        return module;
    }

    public PyObject __call__(PyObject arg1) {
        switch (index) {
        case 10: return arg1.__abs__();
        case 11: return arg1.__invert__();
        case 12: return arg1.__neg__();
        case 13: return arg1.__not__();
        case 14: return arg1.__pos__();
        case 15: return Py.newBoolean(arg1.__bool__());
        case 16: return Py.newBoolean(arg1.isCallable());
        case 17: return Py.newBoolean(arg1.isMappingType());
        case 18: return Py.newBoolean(arg1.isNumberType());
        case 19: return Py.newBoolean(arg1.isSequenceType());
        case 32: return arg1.__invert__();
        case 52: return arg1.__index__();
        default:
            throw info.unexpectedCall(1, false);
        }
    }

    public PyObject __call__(PyObject arg1, PyObject arg2) {
        switch (index) {
        case 0: return arg1._add(arg2);
        case 1: return arg1._and(arg2);
        case 2: return arg1._div(arg2);
        case 3: return arg1._lshift(arg2);
        case 4: return arg1._mod(arg2);
        case 5: return arg1._mul(arg2);
        case 6: return arg1._or(arg2);
        case 7: return arg1._rshift(arg2);
        case 8: return arg1._sub(arg2);
        case 9: return arg1._xor(arg2);
        case 20: return Py.newBoolean(arg1.__contains__(arg2));
        case 21:
            arg1.__delitem__(arg2);
            return Py.None;
        case 23: return arg1.__getitem__(arg2);
        case 27: return arg1.do_richCompare(arg2, CompareOp.GE);
        case 28: return arg1.do_richCompare(arg2, CompareOp.LE);
        case 29: return arg1.do_richCompare(arg2, CompareOp.EQ);
        case 30: return arg1._floordiv(arg2);
        case 31: return arg1.do_richCompare(arg2, CompareOp.GT);
        case 33: return arg1.do_richCompare(arg2, CompareOp.LT);
        case 34: return arg1.do_richCompare(arg2, CompareOp.NE);
        case 35: return arg1._truediv(arg2);
        case 36: return arg1._pow(arg2);
        case 37: return arg1._is(arg2);
        case 38: return arg1._isnot(arg2);
        case 39: return arg1._iadd(arg2);
        case 40: return arg1._iand(arg2);
        case 41: return arg1._idiv(arg2);
        case 42: return arg1._ifloordiv(arg2);
        case 43: return arg1._ilshift(arg2);
        case 44: return arg1._imod(arg2);
        case 45: return arg1._imul(arg2);
        case 46: return arg1._ior(arg2);
        case 47: return arg1._ipow(arg2);
        case 48: return arg1._irshift(arg2);
        case 49: return arg1._isub(arg2);
        case 50: return arg1._itruediv(arg2);
        case 51: return arg1._ixor(arg2);
        default:
            throw info.unexpectedCall(2, false);
        }
    }

    public PyObject __call__(PyObject arg1, PyObject arg2, PyObject arg3) {
        switch (index) {
        case 22: arg1.__delitem__(new PySlice(arg2.__index__(), arg3.__index__(), Py.None)); return Py.None;
        case 24: return arg1.__getitem__(new PySlice(arg2.__index__(), arg3.__index__(), Py.None));
        case 25: arg1.__setitem__(arg2, arg3); return Py.None;
        default:
            throw info.unexpectedCall(3, false);
        }
    }

    public PyObject __call__(PyObject arg1, PyObject arg2, PyObject arg3,
                             PyObject arg4)
    {
        switch (index) {
        case 26:
            arg1.__setitem__(new PySlice(arg2.__index__(), arg3.__index__(), Py.None), arg4);
            return Py.None;
        default:
            throw info.unexpectedCall(4, false);
        }
    }
}
