package org.python.antlr;

import org.python.core.Py;
import org.python.core.PyObject;

import org.antlr.v4.runtime.*;

public class ParseException extends RuntimeException {
	public transient IntStream input;
	public int index;
	public Token token;
	public Object node;
	public int c;
	public int line;
	public int charPositionInLine;
	public boolean approximateLineInfo;

    private PyObject type = Py.SyntaxError;

    public ParseException() {
        super();
    }

    public ParseException(String message, int lin, int charPos) {
        super(message);
        this.line = lin;
        this.charPositionInLine = charPos;
    }

    public ParseException(String message) {
        this(message, 0, 0);
    }

    /**
     * n must not be null to use this constructor
     */
    public ParseException(String message, PythonTree n) {
        this(message, n.getLine(), n.getCharPositionInLine());
        this.node = n;
        this.token = n.getToken();
    }

    public ParseException(String message, RecognitionException r) {
        super(message);
        this.input = r.getInputStream();
        this.index = r.getOffendingState();
        this.token = r.getOffendingToken();
    }

    public void setType(PyObject t) {
        this.type = t;
    }

    public PyObject getType() {
        return this.type;
    }

}
