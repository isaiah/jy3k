//Copyright (c) Corporation for National Research Initiatives
package org.python.modules.array;

import org.python.core.BufferProtocol;
import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.annotations.ExposedConst;
import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;

/**
 * The python array module
 */
@ExposedModule(name = "array", doc = BuiltinDocs.array_doc)
public class ArrayModule {

    @ExposedConst(name = "typecodes")
    public static final String TYPE_CODES = "bBuhHiIlLqQfd";

    @ModuleInit
    public static void classDictInit(PyObject dict) {
        dict.__setitem__("array", PyArrayArray.TYPE);
        dict.__setitem__("ArrayType", PyArrayArray.TYPE);
    }

    @ExposedFunction
    public static final PyObject _array_reconstructor(PyObject arraytype, String typecode, PyObject mformat_code,
                                                      PyObject items) {
        if (arraytype instanceof PyType) {
            if (!Py.isSubClass(arraytype, PyArrayArray.TYPE)) {
                throw Py.TypeError(String.format("%s is not a subtype of array.array", arraytype));
            }
        } else {
            throw Py.TypeError(String.format("first argument must be a type object, not %s", arraytype.getType().fastGetName()));
        }
        if (mformat_code instanceof PyLong) {
            int mcode = mformat_code.asInt();
            if (mcode < 0 || mcode > 21) {
                throw Py.ValueError("third argument must be a valid machine format code.");
            }
        } else {
            throw Py.TypeError(String.format("an integer is required (got %s)", mformat_code.getType().fastGetName()));
        }
        if (!(items instanceof BufferProtocol)) {
            throw Py.TypeError(String.format("fourth argument should be bytes, not %s", items.getType().fastGetName()));
        }
        PyArrayArray ret = new PyArrayArray((PyType) arraytype, MachineFormatCode.formatCode(typecode.charAt(0)), items.__len__());
        ret.extend(items);
        return ret;
    }

    /*
     * These are jython extensions (from jarray module).
     * Note that the argument order is consistent with
     * python array module, but is reversed from jarray module.
     */
    public static PyArrayArray zeros(char typecode, int n) {
        return PyArrayArray.zeros(PyArrayArray.TYPE, n, typecode);
    }
}
