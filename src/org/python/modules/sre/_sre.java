package org.python.modules.sre;

import org.python.core.ArgParser;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.expose.ExposedConst;
import org.python.expose.ExposedFunction;
import org.python.expose.ExposedModule;

@ExposedModule(name="_sre", doc="Native implementation of sre_compile")
public class _sre {

    @ExposedFunction
    public static PyObject compile(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("compile", args, keywords, "pattern", "flags");
        PyObject flags = ap.getPyObject(1, Py.Zero);
        PyObject s = ap.getPyObject(0);
        return new PySRE_Pattern(s, flags);
    }

    public static final int FLAG_TEMPLATE = 1;
    public static final int FLAG_IGNORECASE = 2;
    public static final int FLAG_LOCALE = 4;
    public static final int FLAG_MULTILINE = 8;
    public static final int FLAG_DOTALL = 16;
    public static final int FLAG_UNICODE = 32;
    public static final int FLAG_VERBOSE = 64;
    public static final int FLAG_DEBUG = 128;
    public static final int FLAG_ASCII = 256;
    public static final int INFO_PREFIX = 1;
    public static final int INFO_LITERAL = 2;
    public static final int INFO_CHARSET = 4;

    static byte[] sre_char_lower = new byte[]{
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
            10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43,
            44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60,
            61, 62, 63, 64, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107,
            108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121,
            122, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105,
            106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119,
            120, 121, 122, 123, 124, 125, 126, 127};

    @ExposedConst
    public static final int MAGIC = 20140917;

    @ExposedConst
    public static final int MAXREPEAT = Character.MAX_VALUE;

    @ExposedConst
    public static final int MAXGROUPS = Integer.MAX_VALUE;

    @ExposedConst
    public static final int CODESIZE = 4;

    @ExposedFunction
    public static int getcodesize() {
        return CODESIZE;
    }

    @ExposedFunction
    public static int getlower(PyObject chObj, PyObject flagsObj) {
        int ch = chObj.asInt();
        int flags = flagsObj.asInt();
        if ((flags & _sre.FLAG_LOCALE) != 0)
            return ((ch) < 256 ? Character.toLowerCase((char) ch) : ch);
        if ((flags & _sre.FLAG_UNICODE) != 0)
            return Character.toLowerCase((char) ch);
        return ((ch) < 128 ? (char) sre_char_lower[ch] : ch);
    }
}
