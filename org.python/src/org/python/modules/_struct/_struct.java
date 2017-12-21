/*
 * Copyright 1999 Finn Bock.
 *
 * This program contains material copyrighted by:
 * Copyright 1991-1995 by Stichting Mathematisch Centrum, Amsterdam,
 * The Netherlands.
 *
 */
package org.python.modules._struct;

import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyBytes;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.core.PyTuple;
import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ExposedModule(doc = BuiltinDocs._struct_doc)
public class _struct {

    /**
     * Exception raised on various occasions; argument is a
     * string describing what is wrong.
     */
    public static final PyObject error = Py.makeClass("error", exceptionNamespace(), Py.Exception);
    private static final Map<String, PyStruct> cache = new ConcurrentHashMap<>();

    @ModuleInit
    public static void init(PyObject dict) {
        dict.__setitem__("error", error);
        dict.__setitem__("Struct", PyStruct.TYPE);
    }

    /**
     * Return the size of the struct (and hence of the string)
     * corresponding to the given format.
     */
    @ExposedFunction
    public static int calcsize(String format) {
        PyStruct s = cacheStruct(format);
        return s.size;
    }

    /**
     * Return a string containing the values v1, v2, ... packed according
     * to the given format. The arguments must match the
     * values required by the format exactly.
     */
    @ExposedFunction
    public static PyObject pack(PyObject[] args, String[] kws) {
        if (args.length < 1)
            Py.TypeError("illegal argument type for built-in operation");

        String format = args[0].toString();

        PyStruct s =cacheStruct(format);
        return s.pack(Arrays.copyOfRange(args, 1, args.length), kws);
    }
    
    // xxx - may need to consider doing a generic arg parser here
    @ExposedFunction
    static public void pack_into(PyObject[] args, String[] kws) {
        if (args.length < 3)
            Py.TypeError("illegal argument type for built-in operation");
        String format = args[0].toString();
        PyStruct s = cacheStruct(format);
        s.pack_into(Arrays.copyOfRange(args, 1, args.length), kws);
    }

    @ExposedFunction
    public static PyObject iter_unpack(String format, PyObject buffer) {
        PyStruct s = cacheStruct(format);
        return s.iter_unpack(buffer);
    }

    @ExposedFunction
    public static PyTuple unpack(String format, PyObject buffer) {
        PyStruct s = cacheStruct(format);
        return s.unpack(buffer);
    }
    
    @ExposedFunction(defaults = {"0"})
    public static PyTuple unpack_from(String format, PyObject buf, int offset) {
        PyStruct s = cacheStruct(format);
        return s.unpack_from(buf, offset);
    }

    @ExposedFunction
    public static PyObject _clearcache() {
        cache.clear();
        return Py.None;
    }

    static PyException StructError(String explanation) {
        return new PyException(error, explanation);
    }

    private static PyObject exceptionNamespace() {
        PyObject dict = new PyStringMap();
        dict.__setitem__("__module__", new PyBytes("struct"));
        return dict;
    }

    private static PyStruct cacheStruct(String fmt) {
        PyStruct s = cache.get(fmt);
        if (s != null) {
            return s;
        }

        s = new PyStruct(fmt);
        cache.put(fmt, s);
        return s;
    }

}

