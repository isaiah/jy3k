// Copyright (c) Corporation for National Research Initiatives
package org.python.core;

import java.io.OutputStream;

import org.python.core.util.StringUtil;

public class StdoutWrapper extends OutputStream {
    protected String name;

    public StdoutWrapper() {
        this.name = "stdout";
    }

    protected PyObject getObject(PySystemState ss) {
        return ss.getStdout();
    }

    protected void setObject(PySystemState ss, PyObject out) {
        ss.sysdict.__setitem__("stdout", out);
    }

    protected PyObject myFile() {
        PySystemState ss = Py.getSystemState();
        PyObject out = getObject(ss);
        if (out == null) {
            throw Py.AttributeError("missing sys." + this.name);

        } else if (out instanceof PyAttributeDeleted) {
            throw Py.RuntimeError("lost sys." + this.name);

        }
        return out;
    }

    @Override
    public void flush() {
        PyObject out = myFile();
        try {
            out.invoke("flush");
        } catch (PyException pye) {
            if (!pye.match(Py.AttributeError)) {
                throw pye;
            }
        }
    }

    public void write(String s) {
        PyObject out = myFile();
        out.invoke("write", new PyUnicode(s));
    }

    @Override
    public void write(int i) {
        write(new String(new char[] { (char) i }));
    }

    @Override
    public void write(byte[] data, int off, int len) {
        write(StringUtil.fromBytes(data, off, len));
    }

    public void flushLine() {
        PyObject out = myFile();
        PyObject ss = out.__findattr__("softspace");
        if (ss != null && ss.isTrue()) {
            out.invoke("write", Py.Newline);
        }
        try {
            out.invoke("flush");
        } catch (PyException pye) {
            // ok
        }
        out.__setattr__("softspace", Py.Zero);
    }

    private void printToFileObject(PyObject file, PyObject o) {
        file.invoke("write", Abstract.PyObject_Str(Py.getThreadState(), o));
    }

    /**
     * For __future__ print_function.
     */
    public void print(PyObject[] args, PyObject sep, PyObject end) {
        PyObject out = myFile();
        for (int i=0;i<args.length;i++) {
            printToFileObject(out, args[i]);
            if (i < args.length -1) {
                printToFileObject(out, sep);
            }
        }
        printToFileObject(out, end);
    }

    public void print(PyObject o, boolean space, boolean newline) {
        PyObject out = myFile();
        PyObject ss = out.__findattr__("softspace");
        if (ss != null && ss.isTrue()) {
            out.invoke("write", Py.Space);
            out.__setattr__("softspace", Py.Zero);
        }

        printToFileObject(out, o);

        if (o instanceof PyBytes) {
            String s = o.toString();
            int len = s.length();
            if (len == 0 || !Character.isWhitespace(s.charAt(len - 1))
                    || s.charAt(len - 1) == ' ') {
                out.__setattr__("softspace", space ? Py.One : Py.Zero);
            }
        } else {
            out.__setattr__("softspace", space ? Py.One : Py.Zero);
        }

        if (newline) {
            out.invoke("write", Py.Newline);
            out.__setattr__("softspace", Py.Zero);
        }
    }

    public void print(String s) {
        print(Py.newUnicode(s), false, false);
    }

    public void println(String s) {
        print(Py.newUnicode(s), false, true);
    }

    public void print(PyObject o) {
        print(o, false, false);
    }

    public void printComma(PyObject o) {
        print(o, true, false);
    }

    public void println(PyObject o) {
        print(o, false, true);
    }

    public void println() {
        println(false);
    }

    public void println(boolean useUnicode) {
        PyObject out = myFile();

        if (useUnicode) {
            out.invoke("write", Py.UnicodeNewline);
        } else {
            out.invoke("write", Py.Newline);
        }
        out.__setattr__("softspace", Py.Zero);
    }
}
