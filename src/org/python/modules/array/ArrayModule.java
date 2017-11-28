//Copyright (c) Corporation for National Research Initiatives
package org.python.modules.array;

import org.python.core.BuiltinDocs;
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
        dict.__setitem__("array", PyType.fromClass(PyArrayArray.class));
        dict.__setitem__("ArrayType", PyType.fromClass(PyArrayArray.class));
    }

    @ExposedFunction
    public static final PyObject _array_reconstructor(PyType arraytype, String typecode, PyObject mformat_code,
                                                      PyObject items) {
        PyArrayArray ret = new PyArrayArray(arraytype, MachineFormatCode.formatCode(typecode.charAt(0)), items.__len__());
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
