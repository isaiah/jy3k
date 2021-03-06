/* Copyright (c) Jython Developers */
package org.python.modules._json;

import org.python.bootstrap.Import;
import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyBytes;
import org.python.core.PyException;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyUnicode;
import org.python.core.codecs;
import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;

import java.util.Iterator;

/**
 * This module is a nearly exact line by line port of _json.c to Java. Names and comments  are retained
 * to make it easy to follow, but classes and methods are modified to following Java calling conventions.
 *
 * (Retained comments use the standard commenting convention for C.)
 */
@ExposedModule(name="_json", doc = BuiltinDocs._json_doc)
public class _json {

    @ModuleInit
    public static void init(PyObject dict) {
        dict.__setitem__("make_encoder", PyEncoder.TYPE);
        dict.__setitem__("make_scanner", PyScanner.TYPE);
    }

    private static PyObject JSONDecodeError;

    private static synchronized PyObject get_errmsg_fn() {
        if (JSONDecodeError == null) {
            PyObject decoder = Import.importModule("json.decoder");
            if (decoder != null) {
                JSONDecodeError = decoder.__findattr__("JSONDecodeError");
            }
        }
        return JSONDecodeError;
    }

    static void raise_errmsg(String msg, PyObject s, int pos) {
        raise_errmsg(msg, s, Py.newInteger(pos));
    }

    static void raise_errmsg(String msg, PyObject s, PyObject pos) {
        /* Use the Python function json.decoder.errmsg to raise a nice
        looking ValueError exception */
        if (JSONDecodeError == null) {
            get_errmsg_fn();
        }
        if (JSONDecodeError != null) {
            PyObject err = JSONDecodeError.__call__(new PyUnicode(msg), s, pos);
            throw new PyException(err.getType(), err);
        } else {
            throw Py.ValueError(msg);
        }
    }

    @ExposedFunction(defaults = {"true"})
    public static PyTuple scanstring(PyObject obj, int end, boolean strict) {
        PyUnicode pystr = (PyUnicode) obj;
        int len = pystr.__len__();
        int begin = end - 1;
        if (end < 0 || len <= end) {
            throw Py.ValueError("end is out of bounds");
        }
        int next;
        final PyList chunks = new PyList();
        while (true) {
            /* Find the end of the string or the next escape */
            int c = 0;

            for (next = end; next < len; next++) {
                c = pystr.getInt(next);
                if (c == '"' || c == '\\') {
                    break;
                } else if (strict && c <= 0x1f) {
                    raise_errmsg("Invalid control character at", pystr, next);
                }
            }
            if (!(c == '"' || c == '\\')) {
                raise_errmsg("Unterminated string starting at", pystr, begin);
            }

            /* Pick up this chunk if it's not zero length */
            if (next != end) {
                PyObject strchunk = pystr.getslice(end, next);
                if (strchunk instanceof PyUnicode) {
                    chunks.append(strchunk);
                }
            }
            next++;
            if (c == '"') {
                end = next;
                break;
            }
            if (next == len) {
                raise_errmsg("Unterminated string starting at", pystr, begin);
            }
            c = pystr.getInt(next);
            if (c != 'u') {
                /* Non-unicode backslash escapes */
                end = next + 1;
                switch (c) {
                    case '"':
                        break;
                    case '\\':
                        break;
                    case '/':
                        break;
                    case 'b':
                        c = '\b';
                        break;
                    case 'f':
                        c = '\f';
                        break;
                    case 'n':
                        c = '\n';
                        break;
                    case 'r':
                        c = '\r';
                        break;
                    case 't':
                        c = '\t';
                        break;
                    default:
                        c = 0;
                }
                if (c == 0) {
                    raise_errmsg("Invalid \\escape", pystr, end - 2);
                }
            } else {
                c = 0;
                next++;
                end = next + 4;
                if (end >= len) {
                    raise_errmsg("Invalid \\uXXXX escape", pystr, next - 1);
                }
                /* Decode 4 hex digits */
                for (; next < end; next++) {
                    int digit = pystr.getInt(next);
                    c <<= 4;
                    switch (digit) {
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            c |= (digit - '0');
                            break;
                        case 'a':
                        case 'b':
                        case 'c':
                        case 'd':
                        case 'e':
                        case 'f':
                            c |= (digit - 'a' + 10);
                            break;
                        case 'A':
                        case 'B':
                        case 'C':
                        case 'D':
                        case 'E':
                        case 'F':
                            c |= (digit - 'A' + 10);
                            break;
                        default:
                            raise_errmsg("Invalid \\uXXXX escape", pystr, end - 5);
                    }
                }
                /* Surrogate pair */
                if ((c & 0xfc00) == 0xd800) {
                    int c2 = 0;
                    if (end + 6 >= len) {
                        raise_errmsg("Unpaired high surrogate", pystr, end - 5);
                    }
                    if (pystr.getInt(next++) != '\\' || pystr.getInt(next++) != 'u') {
                        raise_errmsg("Unpaired high surrogate", pystr, end - 5);
                    }
                    end += 6;
                    /* Decode 4 hex digits */
                    for (; next < end; next++) {
                        int digit = pystr.getInt(next);
                        c2 <<= 4;
                        switch (digit) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                c2 |= (digit - '0');
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                c2 |= (digit - 'a' + 10);
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                c2 |= (digit - 'A' + 10);
                                break;
                            default:
                                raise_errmsg("Invalid \\uXXXX escape", pystr, end - 5);
                        }
                    }
                    // if is low surrogate
                    if ((c2 & 0xfc00) == 0xdc00) {
                        c = 0x10000 + (((c - 0xd800) << 10) | (c2 - 0xdc00));
                        chunks.append(new PyUnicode(c));
                    } else {
                        end -= 6;
                    }
                } else if ((c & 0xfc00) == 0xdc00) {
                    raise_errmsg("Unpaired low surrogate", pystr, end - 5);
                }
            }
            chunks.append(new PyUnicode(c));
        }

        return new PyTuple(Py.EmptyUnicode.join(chunks), Py.newInteger(end));
    }

    @ExposedFunction
    public static PyObject encode_basestring_ascii(PyObject pystr) {
        if (pystr instanceof PyUnicode) {
            return ascii_escape((PyUnicode) pystr);
        } else if (pystr instanceof PyBytes) {
            return ascii_escape((PyBytes) pystr);
        } else {
            throw Py.TypeError(String.format(
                    "first argument must be a string, not %.80s",
                    pystr.getType().fastGetName()));
        }
    }

    private static PyUnicode ascii_escape(PyUnicode pystr) {
        StringBuilder rval = new StringBuilder(pystr.__len__());
        rval.append("\"");
        for (Iterator<Integer> iter = pystr.newSubsequenceIterator(); iter.hasNext(); ) {
            _write_char(rval, iter.next());
        }
        rval.append("\"");
        return new PyUnicode(rval.toString());
    }

    private static PyObject ascii_escape(PyBytes pystr) {
        int len = pystr.__len__();
        String s = pystr.getString();
        StringBuilder rval = new StringBuilder(len);
        rval.append("\"");
        for (int i = 0; i < len; i++) {
            int c = s.charAt(i);
            if (c > 127) {
                return ascii_escape(new PyUnicode(codecs.PyUnicode_DecodeUTF8(s, null)));
            }
            _write_char(rval, c);
        }
        rval.append("\"");
        return new PyBytes(rval.toString());
    }

    private static void _write_char(StringBuilder builder, int c) {
        /* Escape unicode code point c to ASCII escape sequences
        in char *output. output must have at least 12 bytes unused to
        accommodate an escaped surrogate pair "\ u XXXX \ u XXXX" */
        if (c >= ' ' && c <= '~' && c != '\\' & c != '"') {
            builder.append((char) c);
        } else {
            _ascii_escape_char(builder, c);
        }
    }

    private static void _write_hexchar(StringBuilder builder, int c) {
        builder.append("0123456789abcdef".charAt(c & 0xf));
    }

    private static void _ascii_escape_char(StringBuilder builder, int c) {
        builder.append('\\');
        switch (c) {
            case '\\':
                builder.append((char) c);
                break;
            case '"':
                builder.append((char) c);
                break;
            case '\b':
                builder.append('b');
                break;
            case '\f':
                builder.append('f');
                break;
            case '\n':
                builder.append('n');
                break;
            case '\r':
                builder.append('r');
                break;
            case '\t':
                builder.append('t');
                break;
            default:
                if (c >= 0x10000) {
                /* UTF-16 surrogate pair */
                    int v = c - 0x10000;
                    c = 0xd800 | ((v >> 10) & 0x3ff);
                    builder.append('u');
                    _write_hexchar(builder, c >> 12);
                    _write_hexchar(builder, c >> 8);
                    _write_hexchar(builder, c >> 4);
                    _write_hexchar(builder, c);
                    c = 0xdc00 | (v & 0x3ff);
                    builder.append('\\');
                }
                builder.append('u');
                _write_hexchar(builder, c >> 12);
                _write_hexchar(builder, c >> 8);
                _write_hexchar(builder, c >> 4);
                _write_hexchar(builder, c);
        }
    }
}
