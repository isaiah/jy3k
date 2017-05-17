package org.python.compiler;

import org.objectweb.asm.Type;
import org.python.core.Py;
import org.python.core.PyFrame;
import org.python.core.PyObject;

public interface ClassConstants {

    String $pyObj      = "Lorg/python/core/PyObject;";
    String $pyObjArr   = "[Lorg/python/core/PyObject;";
    String $pyStr      = "Lorg/python/core/PyBytes;";
    String $pyUnicode  = "Lorg/python/core/PyUnicode;";
    String $pyExc      = "Lorg/python/core/PyException;";
    String $pyFrame    = "Lorg/python/core/PyFrame;";
    String $threadState= "Lorg/python/core/ThreadState;";
    String $pyCode     = "Lorg/python/core/PyCode;";
    String $pyInteger  = "Lorg/python/core/PyInteger;";
    String $pyLong     = "Lorg/python/core/PyLong;";
    String $pyFloat    = "Lorg/python/core/PyFloat;";
    String $pyComplex  = "Lorg/python/core/PyComplex;";
    String $pyRunnable = "Lorg/python/core/PyRunnable;";
    String $pyFuncTbl  = "Lorg/python/core/PyFunctionTable;";
    String $pyProxy    = "Lorg/python/core/PyProxy;";

    String $obj       = "Ljava/lang/Object;";
    String $objArr    = "[Ljava/lang/Object;";
    String $clss      = "Ljava/lang/Class;";
    String $str       = "Ljava/lang/String;";
    String $strArr    = "[Ljava/lang/String;";
    String $throwable = "Ljava/lang/Throwable;";

    Type PYOBJ = Type.getType(PyObject.class);
    Type PYARR = Type.getType(PyObject[].class);
    Type OBJARR = Type.getType(Object[].class);
    Type OBJ = Type.getType(Object.class);
    Type PY = Type.getType(Py.class);
    Type PYFRAME = Type.getType(PyFrame.class);

    Type INTEGER_TYPE = Type.getType(Integer.class);
}
