package org.python.modules._io;

import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.core.ArgParser;
import org.python.core.Py;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.modules.array.PyArrayArray;

@ExposedType(name = "_io.StringIO")
public class PyStringIO extends PyTextIOWrapper {
    public static final PyType TYPE = PyType.fromClass(PyStringIO.class);

    public boolean softspace = false;
    public boolean closed = false;
    public int pos = 0;

    @ExposedGet
    public String encoding;
    @ExposedGet
    public String errors;

    private String newline;
    private String writenl = null;
    private boolean writetranslate;

    private StringBuilder buf;

    public PyStringIO(PyType subtype) {
        super(subtype);
        buf = new StringBuilder();
    }

    public PyStringIO(String buffer) {
        super(TYPE);
        buf = new StringBuilder(buffer);
    }

    public PyStringIO(PyArrayArray array) {
        super(TYPE);
        buf = new StringBuilder(array.toString());
    }

    public PyStringIO(PyType subtype, PyObject initvalue) {
        super(subtype);
        buf = new StringBuilder(new String(Py.unwrapBuffer(initvalue)));
    }

    @ExposedNew
    public void __init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("StringIO", args, keywords, "initial_value", "newline");
        PyObject initValue = ap.getPyObject(0, null);
        PyObject newlineObj = ap.getPyObject(1, null);

        if (newlineObj != null && newlineObj != Py.None) {
            if (!(newlineObj instanceof PyUnicode)) {
                throw Py.TypeError(String.format("newline must be str or None, not %s", newlineObj.getType().fastGetName()));
            }
            newline = newlineObj.asString();
            if (newline.length() > 0 && "\r\n".indexOf(newline) < 0) {
                throw Py.ValueError(String.format("illegal newline value: '%s'", newline));
            }
            writetranslate = newline.length() > 0;
            if (writetranslate) {
                writenl = newline;
            }
        } else {
            writetranslate = newlineObj == Py.None;
            this.newline = System.lineSeparator();
        }

        if (initValue != null) {
            if (!(initValue instanceof PyUnicode)) {
                throw Py.TypeError(String.format("initial_value must be str or None, not %s", initValue.getType().fastGetName()));
            }
            String initial = initValue.asString();
            if (newlineObj == Py.None) {
                initial = initial.replaceAll("\\r[\\n]?", newline);
            } else {
                initial = initial.replaceAll("\\n", newline);
            }
            buf = new StringBuilder(initial);
        } else {
            buf = new StringBuilder();
        }
    }

    private void _complain_ifclosed() {
        if (closed)
            throw Py.ValueError("I/O operation on closed file");
    }

    private int _convert_to_int(long val) {
        if (val > Integer.MAX_VALUE) {
            throw Py.OverflowError("long int too large to convert to int");
        }
        return (int)val;
    }

    public void __setattr__(String name, PyObject value) {
        if (name == "softspace") {
            softspace = value.__bool__();
            return;
        }
        super.__setattr__(name, value);
    }

    @ExposedMethod
    public PyObject __iter__() {
        return this;
    }

    @ExposedMethod
    public PyObject __next__() {
        _complain_ifclosed();
        String r = readline(-1L);
        if (r.length() == 0)
            return null;
        return new PyUnicode(r);
    }

    /**
     * Free the memory buffer.
     */
    @ExposedMethod
    public void close() {
        closed = true;
        buf = null;
    }


    /**
     * Return false.
     * @return      false.
     */
    @ExposedMethod
    public boolean isatty() {
        _complain_ifclosed();
        return false;
    }

    @ExposedGet(name = "line_buffering")
    public boolean lineBuffering() {
        return false;
    }

    /**
     * Position the file pointer to the position in the .
     *
     * @param pos
     *            the position in the file.
     * @param mode
     *            0=from the start, 1=relative, 2=from the end.
     */
    @ExposedMethod(defaults = {"0"})
    public synchronized long seek(long pos, int mode) {
        _complain_ifclosed();
        if (pos < 0) {
            throw Py.ValueError(String.format("negative seek position %d", pos));
        }
        switch (mode) {
            case os.SEEK_CUR:
                this.pos += pos;
                break;
            case os.SEEK_END:
                this.pos = _convert_to_int(pos + buf.length());
                break;
            case os.SEEK_SET:
                this.pos = _convert_to_int(pos);
                break;
            default:
                throw Py.ValueError(String.format("unrecognised whence: %d", mode));
        }
        return (int) pos;
    }

    /**
     * Reset the file position to the beginning of the file.
     */
    public synchronized void reset() {
        pos = 0;
    }

    /**
     * Return the file position.
     * @return     the position in the file.
     */
    @ExposedMethod
    public synchronized int tell() {
        _complain_ifclosed();
        return pos;
    }

    /**
     * Read at most size bytes from the file (less if the read hits EOF).
     * If the size argument is negative, read all data until EOF is
     * reached. An empty string is returned when EOF is encountered
     * immediately.
     * @param size  the number of characters to read.
     * @return     A string containing the data read.
     */
    @ExposedMethod(defaults = {"-1"})
    public synchronized String read(long size) {
        _complain_ifclosed();
        _convert_to_int(size);
        int len = buf.length();
        String substr;
        if (size < 0) {
            substr = pos >= len ? "" : buf.substring(pos);
            pos = len;
        } else {
            // ensure no overflow
            int newpos = _convert_to_int(Math.min(pos + size, len));
            substr = buf.substring(pos, newpos);
            pos = newpos;
        }
        return substr;
    }

    /**
     * Read one entire line from the file. A trailing newline character
     * is kept in the string (but may be absent when a file ends with an
     * incomplete line).
     * If the size argument is non-negative, it is a maximum byte count
     * (including the trailing newline) and an incomplete line may be
     * returned.
     * @return data from the file up to and including the newline.
     */
    @ExposedMethod(defaults = {"-1"})
    public synchronized String readline(long size) {
        _complain_ifclosed();
        _convert_to_int(size);
        int len = buf.length();
        if (pos >= len) {
            return "";
        }
        int i;
        if (newline.length() == 0) {
            i = buf.indexOf("\\r\\n", pos);
            if (i < 0) {
                i = buf.indexOf("\\n", pos);
                if (i < 0) {
                    i = buf.indexOf("\\r", pos);
                }
            } else {
                i++; // two chars
            }
        } else {
            i = buf.indexOf(newline, pos);
            if (i > 0 && newline.length() > 1) {
                i++; // two chars
            }
        }
        int newpos = (i < 0) ? len : i + 1;
        if (size >= 0) {
            newpos = _convert_to_int(Math.min(newpos - pos, size) + pos);
        }
        String r = buf.substring(pos, newpos);
        pos = newpos;
        return r;
    }


    /**
     * Read and return a line without the trailing newline.
     * Used by _pickle as an optimization.
     */
    public synchronized String readlineNoNl() {
        _complain_ifclosed();
        int len = buf.length();
        int i = buf.indexOf(newline, pos);
        int newpos = (i < 0) ? len : i;
        String r = buf.substring(pos, newpos);
        pos = newpos;
        if (pos  < len) // Skip the newline
            pos++;
        return r;
    }



    /**
     * Read until EOF using readline() and return a list containing
     * the lines thus read.
     * @return      a list of the lines.
     */
    public PyObject readlines() {
        return readlines(0);
    }


    /**
     * Read until EOF using readline() and return a list containing
     * the lines thus read.
     * @return      a list of the lines.
     */
    public PyObject readlines(long sizehint) {
        _complain_ifclosed();

        int sizehint_int = (int)sizehint;
        int total = 0;
        PyList lines = new PyList();
        String line = readline(-1L);
        while (line.length() > 0) {
            lines.append(new PyUnicode(line));
            total += line.length();
            if (0 < sizehint_int  && sizehint_int <= total)
                break;
            line = readline(-1L);
        }
        return lines;
    }

    /**
     * truncate the file at the position pos.
     */
    @ExposedMethod(defaults = {"null"})
    public synchronized int truncate(PyObject posObj) {
        int pos = posObj == Py.None ? this.pos : posObj.asInt();
        if (pos < 0) {
            throw Py.ValueError("negative position value");
        }
        pos = _convert_to_int(pos);
        int slen = buf.length();
        buf.setLength(pos);
        this.pos = pos;
        return slen - pos;
    }

    /**
     * Write a string to the file.
     * @param s     The data to write.
     */
    @ExposedMethod
    public synchronized int write(String s) {
        _complain_ifclosed();

        int spos = pos;
        int slen = buf.length();

        int ret = s.length();
        if (writetranslate) {
            if (writenl == null) {
                s = s.replaceAll("\\r[\\n]?", "\\\n");
            } else if (!"\n".equals(writenl)) {
                s = s.replaceAll("\\n", writenl);
            } //else {
//                s = s.replaceAll("\\r[\\n]?", writenl);
//            }
        }
//        if (writetranslate || !"\n".equals(newline)) {
//            s = s.replaceAll("\\n", newline);
//        }

        if (spos == slen) {
            buf.append(s);
            buf.setLength(slen + s.length());
            pos = spos + s.length();

            return ret;
        }

        if (spos > slen) {
            int l = spos - slen;
            char[] bytes = new char[l];

            for (int i = 0; i < l - 1; i++)
                bytes[i] = '\0';

            buf.append(bytes);
            slen = spos;
        }

        int newpos = spos + s.length();

        if (spos < slen) {
            if (newpos > slen) {
                buf.replace(spos, slen, s);
                buf.append(s.substring(slen - spos));
                slen = newpos;
            } else {
                buf.replace(spos, spos + s.length(), s);
            }
        } else {
            buf.append(s);
            slen = newpos;
        }

        buf.setLength(slen);
        pos = newpos;
        return ret;
    }

    /**
     * Write a char to the file. Used by _pickle as an optimization.
     * @param ch    The data to write.
     */
    public synchronized void writeChar(char ch) {
        int len = buf.length();
        if (len <= pos)
            buf.setLength(pos + 1);
        buf.setCharAt(pos++, ch);
    }


    /**
     * Write a list of strings to the file.
     */
    @ExposedMethod
    public void writelines(PyObject lines) {
        _complain_ifclosed();
        for (PyObject line : lines.asIterable()) {
            write(line.asString());
        }
    }


    /**
     * Flush the internal buffer. Does nothing.
     */
    public void flush() {
        _complain_ifclosed();
    }


    /**
     * Retrieve the entire contents of the ``file'' at any time
     * before the StringIO object's close() method is called.
     * @return      the contents of the StringIO.
     */
    @ExposedMethod
    public String getvalue() {
        _complain_ifclosed();
        return buf.toString();
    }

    private static class os {
        public static final int SEEK_SET = 0;
        public static final int SEEK_CUR = 1;
        public static final int SEEK_END = 2;
    }
}
