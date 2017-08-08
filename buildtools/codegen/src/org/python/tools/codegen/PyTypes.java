package org.python.tools.codegen;

import org.objectweb.asm.Type;
import org.python.compiler.ClassConstants;
import org.python.core.PyBoolean;
import org.python.core.PyBuiltinCallable;
import org.python.core.PyBuiltinMethod;
import org.python.core.PyDataDescr;
import org.python.core.PyException;
import org.python.core.PyFloat;
import org.python.core.PyLong;
import org.python.core.PyModule;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyBytes;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.core.ThreadState;
import org.python.expose.ExposeAsSuperclass;
import org.python.annotations.ExposedClassMethod;
import org.python.annotations.ExposedConst;
import org.python.annotations.ExposedDelete;
import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedModule;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSet;
import org.python.annotations.ExposedType;
import org.python.annotations.ModuleInit;
import org.python.expose.TypeBuilder;

import java.lang.invoke.MethodHandle;

/**
 * Type objects used by exposed generation.
 */
public interface PyTypes extends ClassConstants {

    // Core Jython types
    public static final Type APYOBJ = Type.getType(PyObject[].class);

    public static final Type PYTYPE = Type.getType(PyType.class);

    public static final Type PYMODULE = Type.getType(PyModule.class);

    public static final Type ASSUPER = Type.getType(ExposeAsSuperclass.class);

    public static final Type PYEXCEPTION = Type.getType(PyException.class);

    public static final Type PYBYTES = Type.getType(PyBytes.class);

    public static final Type PYSTR = Type.getType(PyUnicode.class);

    public static final Type PYBOOLEAN = Type.getType(PyBoolean.class);

    public static final Type PYLONG = Type.getType(PyLong.class);

    public static final Type PYFLOAT = Type.getType(PyFloat.class);

    public static final Type PYNEWWRAPPER = Type.getType(PyNewWrapper.class);

    public static final Type BUILTIN_METHOD = Type.getType(PyBuiltinMethod.class);

    public static final Type ABUILTIN_METHOD = Type.getType(PyBuiltinMethod[].class);

    public static final Type BUILTIN_FUNCTION = Type.getType(PyBuiltinCallable.class);

    public static final Type ABUILTIN_FUNCTION = Type.getType(PyBuiltinCallable[].class);

    public static final Type DATA_DESCR = Type.getType(PyDataDescr.class);

    public static final Type ADATA_DESCR = Type.getType(PyDataDescr[].class);

    public static final Type BUILTIN_INFO = Type.getType(PyBuiltinCallable.Info.class);

    public static final Type THREAD_STATE = Type.getType(ThreadState.class);

    // Exposer Jython types
    public static final Type EXPOSED_TYPE = Type.getType(ExposedType.class);

    public static final Type EXPOSED_MODULE = Type.getType(ExposedModule.class);

    public static final Type EXPOSED_METHOD = Type.getType(ExposedMethod.class);

    public static final Type EXPOSED_FUNCTION = Type.getType(ExposedFunction.class);

    public static final Type MODULE_INIT = Type.getType(ModuleInit.class);

    public static final Type EXPOSED_CLASS_METHOD = Type.getType(ExposedClassMethod.class);

    public static final Type EXPOSED_NEW = Type.getType(ExposedNew.class);

    public static final Type EXPOSED_GET = Type.getType(ExposedGet.class);

    public static final Type EXPOSED_SET = Type.getType(ExposedSet.class);

    public static final Type EXPOSED_DELETE = Type.getType(ExposedDelete.class);

    public static final Type EXPOSED_CONST = Type.getType(ExposedConst.class);

    public static final Type TYPEBUILDER = Type.getType(TypeBuilder.class);

    // Java types
    public static final Type OBJECT = Type.getType(Object.class);

    public static final Type STRING = Type.getType(String.class);

    public static final Type METHOD_HANDLE = Type.getType(MethodHandle.class);

    public static final Type ASTRING = Type.getType(String[].class);

    public static final Type STRING_BUILDER = Type.getType(StringBuilder.class);

    public static final Type CLASS = Type.getType(Class.class);

    // Primitives
    public static final Type BYTE = Type.BYTE_TYPE;

    public static final Type SHORT = Type.SHORT_TYPE;

    public static final Type CHAR = Type.CHAR_TYPE;

    public static final Type INT = Type.INT_TYPE;

    public static final Type VOID = Type.VOID_TYPE;

    public static final Type BOOLEAN = Type.BOOLEAN_TYPE;

    // module init method descriptor
    String INIT_DESCRIPTOR = Type.getMethodDescriptor(VOID, PYOBJ);
}
