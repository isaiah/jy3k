package org.python.modules._io;

import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.core.Py;
import org.python.core.PyBytes;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.modules.array.PyArrayArray;

@ExposedType(name = "_io.StringIO")
public class PyStringIO extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyStringIO.class);

    public boolean softspace = false;
    public boolean closed = false;
    public int pos = 0;

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
        buf = new StringBuilder(new String(Py.unwrapBuffer(args[0])));
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

    public PyObject __next__() {
        _complain_ifclosed();
        PyBytes r = readline();
        if (r.__len__() == 0)
            return null;
        return r;
    }

    /**
     * Free the memory buffer.
     */
    public void close() {
        closed = true;
        // No point in zeroing the buf, because it won't be reused.
        // buf is a final variable, so can't set to null.
        // Therefore, just leave it and let it be GC'ed when the enclosing object is GC'ed
        // Or remove the final declaration
        // buf = null;
    }


    /**
     * Return false.
     * @return      false.
     */
    public boolean isatty() {
        _complain_ifclosed();
        return false;
    }


    /**
     * Position the file pointer to the absolute position.
     * @param       pos the position in the file.
     */
    public void seek(long pos) {
        seek(pos, os.SEEK_SET);
    }


    /**
     * Position the file pointer to the position in the .
     *
     * @param pos
     *            the position in the file.
     * @param mode
     *            0=from the start, 1=relative, 2=from the end.
     */
    public synchronized void seek(long pos, int mode) {
        _complain_ifclosed();
        switch (mode) {
            case os.SEEK_CUR:
                this.pos += pos;
                break;
            case os.SEEK_END:
                this.pos = _convert_to_int(pos + buf.length());
                break;
            case os.SEEK_SET:
            default:
                this.pos = _convert_to_int(pos);
                break;
        }
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
    public synchronized int tell() {
        _complain_ifclosed();
        return pos;
    }



    /**
     * Read all data until EOF is reached.
     * An empty string is returned when EOF is encountered immediately.
     * @return     A string containing the data.
     */
    public PyBytes read() {
        return read(-1);
    }


    /**
     * Read at most size bytes from the file (less if the read hits EOF).
     * If the size argument is negative, read all data until EOF is
     * reached. An empty string is returned when EOF is encountered
     * immediately.
     * @param size  the number of characters to read.
     * @return     A string containing the data read.
     */

    public synchronized PyBytes read(long size) {
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
        return new PyBytes(substr);
    }

    /**
     * Read one entire line from the file. A trailing newline character
     * is kept in the string (but may be absent when a file ends with
     * an incomplete line).
     * An empty string is returned when EOF is hit immediately.
     * @return data from the file up to and including the newline.
     */
    public PyBytes readline() {
        return readline(-1);
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
    public synchronized PyBytes readline(long size) {
        _complain_ifclosed();
        _convert_to_int(size);
        int len = buf.length();
        if (pos == len) {
            return new PyBytes("");
        }
        int i = buf.indexOf("\n", pos);
        int newpos = (i < 0) ? len : i + 1;
        if (size >= 0) {
            newpos = _convert_to_int(Math.min(newpos - pos, size) + pos);
        }
        String r = buf.substring(pos, newpos);
        pos = newpos;
        return new PyBytes(r);
    }


    /**
     * Read and return a line without the trailing newline.
     * Usind by _pickle as an optimization.
     */
    public synchronized PyBytes readlineNoNl() {
        _complain_ifclosed();
        int len = buf.length();
        int i = buf.indexOf("\n", pos);
        int newpos = (i < 0) ? len : i;
        String r = buf.substring(pos, newpos);
        pos = newpos;
        if (pos  < len) // Skip the newline
            pos++;
        return new PyBytes(r);
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
        PyBytes line = readline();
        while (line.__len__() > 0) {
            lines.append(line);
            total += line.__len__();
            if (0 < sizehint_int  && sizehint_int <= total)
                break;
            line = readline();
        }
        return lines;
    }

    /**
     * truncate the file at the current position.
     */
    public synchronized void truncate() {
        buf.setLength(this.pos);
    }

    /**
     * truncate the file at the position pos.
     */
    public synchronized void truncate(long pos) {
        if (pos < 0) {
            throw Py.IOError("Negative size not allowed");
        }
        int pos_int = _convert_to_int(pos);
        if (pos_int < 0)
            pos_int = this.pos;
        buf.setLength(pos_int);
        this.pos = pos_int;
    }

    /**
     * Write a string to the file.
     * @param obj     The data to write.
     */
    public void write(PyObject obj) {
        write(obj.toString());
    }

    public synchronized void write(String s) {
        _complain_ifclosed();

        int spos = pos;
        int slen = buf.length();

        if (spos == slen) {
            buf.append(s);
            buf.setLength(slen + s.length());
            pos = spos + s.length();

            return;
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
    public void writelines(PyObject lines) {
        for (PyObject line : lines.asIterable()) {
            write(line);
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
    public synchronized PyBytes getvalue() {
        _complain_ifclosed();
        return new PyBytes(buf.toString());
    }

    private static class os {
        public static final int SEEK_SET = 0;
        public static final int SEEK_CUR = 1;
        public static final int SEEK_END = 2;
    }
}
