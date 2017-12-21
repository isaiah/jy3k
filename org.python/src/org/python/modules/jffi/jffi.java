
package org.python.modules.jffi;

import com.kenai.jffi.Library;
import org.python.core.Py;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyUnicode;
import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;

@ExposedModule
public class jffi {
    public static final int FUNCFLAG_STDCALL = 0x0;
    public static final int FUNCFLAG_CDECL = 0x1;
    public static final int FUNCFLAG_HRESULT = 0x2;
    public static final int FUNCFLAG_PYTHONAPI = 0x4;
    public static final int FUNCFLAG_USE_ERRNO = 0x8;
    public static final int FUNCFLAG_USE_LASTERROR = 0x10;

    @ModuleInit
    public static void init(PyObject dict) {
        dict.__setitem__("DynamicLibrary", DynamicLibrary.TYPE);
        dict.__setitem__("Type", CType.TYPE);
        dict.__setitem__("Function", Function.TYPE);
        dict.__setitem__("CData", CData.TYPE);
        dict.__setitem__("ArrayCData", ArrayCData.TYPE);
        dict.__setitem__("PointerCData", PointerCData.TYPE);
        dict.__setitem__("ScalarCData", ScalarCData.TYPE);
        dict.__setitem__("StringCData", StringCData.TYPE);
        dict.__setitem__("Structure", Structure.TYPE);
        dict.__setitem__("StructLayout", StructLayout.TYPE);
        dict.__setitem__("FUNCFLAG_STDCALL", Py.newInteger(FUNCFLAG_STDCALL));
        dict.__setitem__("FUNCFLAG_CDECL", Py.newInteger(FUNCFLAG_CDECL));
        
        dict.__setitem__("RTLD_GLOBAL", Py.newInteger(Library.GLOBAL));
        dict.__setitem__("RTLD_LOCAL", Py.newInteger(Library.LOCAL));
        dict.__setitem__("RTLD_LAZY", Py.newInteger(Library.LAZY));
        dict.__setitem__("RTLD_NOW", Py.newInteger(Library.NOW));

        dict.__setitem__("__version__", new PyUnicode("0.0.1"));
    }

    @ExposedFunction
    public static PyObject dlopen(PyObject name, PyObject mode) {
        return new DynamicLibrary(name != Py.None ? name.asString() : null, mode.asInt());
    }

    @ExposedFunction
    public static PyObject get_errno() {
        return Py.newInteger(0);
    }

    @ExposedFunction
    public static PyObject set_errno(PyObject type) {
        return Py.newInteger(0);
    }
    @ExposedFunction
    public static PyObject pointer(PyObject type) {
        return Py.newInteger(0);
    }
    @ExposedFunction
    public static PyObject POINTER(PyObject type) {
        return type;
    }

    private static long getMemoryAddress(PyObject obj) {
        if (obj instanceof Pointer) {
            return ((Pointer) obj).getMemory().getAddress();
        } else if (obj instanceof CData) {
            return ((CData) obj).getReferenceMemory().getAddress();
        } else if (obj instanceof PyLong) {
            return ((PyLong) obj).asLong(0);
        } else {
            throw Py.TypeError("invalid memory address");
        }
    }

    @ExposedFunction
    public static PyObject memmove(PyObject dst, PyObject src, PyObject length) {
        com.kenai.jffi.MemoryIO.getInstance().copyMemory(getMemoryAddress(src),
                getMemoryAddress(dst), length.asInt());

        return Py.None;
    }

    @ExposedFunction
    public static PyObject memset(PyObject dst, PyObject value, PyObject length) {
        com.kenai.jffi.MemoryIO.getInstance().setMemory(getMemoryAddress(dst), length.asInt(), (byte) value.asInt());
        return Py.None;
    }
}
