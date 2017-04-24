// Copyright (c) Corporation for National Research Initiatives
package org.python.core;

import java.util.regex.Pattern;

/**
 * A convenience class for creating Syntax errors. Note that the
 * syntax error is still taken from Py.SyntaxError.
 * <p>
 * Generally subclassing from PyException is not the right way
 * of creating new exception classes.
 */

public class PySyntaxError extends PyException {
    private static Pattern INVALID_SYNTAX = Pattern.compile("no viable alternative at input.*");

    int lineno;
    int column;
    String text;
    String filename;
    String msg;


    public PySyntaxError(String s, int line, int column, String text, String filename)
    {
        super(Py.SyntaxError);
        if (text == null) {
            text = "";
        }
        if (s==null) s = "";
        if (INVALID_SYNTAX.matcher(s).matches()) {
            s = "invalid syntax";
        }
        PyObject[] tmp = new PyObject[] {
            new PyUnicode(filename), new PyLong(line),
            new PyLong(column), new PyUnicode(text)
        };

        this.value = new PyTuple(new PyUnicode(s), new PyTuple(tmp));

        this.lineno = line;
        this.column = column;
        this.text = text;
        this.filename = filename;
        this.msg = s;
    }

    public PyException setFilename(String filename) {
        this.filename = filename;
        PyObject[] tmp = new PyObject[] {
            new PyUnicode(filename), new PyLong(lineno),
            new PyLong(column), new PyUnicode(text)
        };

        this.value = new PyTuple(new PyUnicode(msg), new PyTuple(tmp));

        return this;
    }
}
