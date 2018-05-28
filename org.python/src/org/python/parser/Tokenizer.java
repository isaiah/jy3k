package org.python.parser;

import org.antlr.v4.runtime.Token;
import org.python.antlr.ast.Ellipsis;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.CharBuffer;
import java.util.function.Predicate;

import static org.python.parser.TokenType.*;

/**
 * Tokenizer.c
 */
public class Tokenizer implements Errcode {
    private static final int ALTTABSIZE = 1;
    private static final char EOF = '\uffff';
    private static final int MAXINDENT = 100;


    CharBuffer buf; /* Input buffer */
//    int cur; /* Next character in buffer */
    int inp; /* End of data in buffer */
    int end; /* End of input buffer */
    Integer start; /* Start of current token */
    int done; /* E_OF normally */
    Reader fp; /* Rest of input; NULL if tokenizing a string */
    int tabsize; /* Tab spacing */
    int indent; /* Current indent index */
    int[] indstack = new int[MAXINDENT]; /* Stack of indents */
    int atbol; /* Nonzero if at begin of new line */
    int pendin; /* Pending indents (if > 0) or dedents (if < 0) */
    int lineno; /* Current line number */
    int level; /* Parentheses nesting level */
    int[] altindstack = new int[MAXINDENT]; /* Stack of alternative indents */
    int lineStart;
    String prompt;
    boolean contLine; /* Whether we are in a continuation line */

    static final int BUFSIZ = 1024;

    public Tokenizer(Reader fp) {
        inp = 0;
        this.buf = CharBuffer.allocate(BUFSIZ);
        this.end = BUFSIZ;
        this.fp = fp;
    }

    public static Tokenizer fromFile(Reader fp) {
        Tokenizer tok = new Tokenizer(fp);
        return tok;
    }

    Node parsetok(Grammar g, int start) {
        Parser ps = new Parser(g, start);

        boolean started = false;
        for (;;) {
            int type;
            int len;
            char[] str;
            int colOffset;

            Tok t = get();
            type = t.type;
            if (t == ERR_TOK) {
                // err_ret->error = done;
                break;

            }
            if (type == ENDMARKER && started) {
                type = NEWLINE;
                started = false;
                /* Add the right number of dedent tokens,
                   except if a certain flag is given -- codeop.py uses this. */
//                if (tok->indent &&
//                    !(*flags & PyPARSE_DONT_IMPLY_DEDENT))
//                {
//                    tok->pendin = -tok->indent;
//                    tok->indent = 0;
//                }

                if (indent > 0) { // && flags & PyPARSE_DONT_IMPLY_DEDENT) {
                    pendin = - indent;
                    indent = 0;
                }
            } else {
                started = true;
            }
            len = t.length();
            str = new char[len];
            // rewind and load the char[]
            buf.position(t.start);
            buf.get(str);
            if (t.start >= lineStart) {
                colOffset = t.start - lineStart;
            } else {
                colOffset = -1;
            }

            int error = ps.addToken(type, new String(str), lineno, colOffset);
            if (error != E_OK) {
                break;
            }
        }

        Node n = ps.tree;
        ps.tree = null;
        // #ifndef PGEN
        // TODO extra logic for non pgen parser
        // #endif
        return n;
    }

    /* Get next token, after space stripping etc. */
    Tok get() {
        // nextline:
        start = null;
        boolean blankline = false;
        if (atbol > 0) {
            int col = 0;
            int altcol = 0;
            atbol = 0;
            char c;
            for (;;) {
                c = nextc();
                if (c == ' ') {
                    col++;
                    altcol++;
                } else if (c == '\t') {
                    col = (col / tabsize + 1) * tabsize;
                    altcol = (altcol / ALTTABSIZE + 1) * ALTTABSIZE;
                } else if (c == '\014') { /* Control-L (formfeed) */
                    col = altcol = 0;
                } else {
                    break;
                }
            }
            backup(c);
            if (c == '#' || c == '\n') {
               /* Lines with only whitespace and/or comments
                  shouldn't affect the indentation and are
                  not passed to the parser as NEWLINE tokens,
                  except *totally* empty lines in interactive
                  mode, which signal the end of a command group. */
               if (col == 0 && c == '\n' && prompt != null) {
                   blankline = false; /* Let it through */
               } else {
                   blankline = true; /* Ignore completely */
               }
            }
            if (!blankline && level == 0) {
                if (col == lastIndentCol()) {
                    /* No change */
                    if (altcol != lastAltIndentCol()) {
                        return indenterror();
                    }
                } else if (col > lastIndentCol()) {
                    /* indent -- always one */
                    if (indent + 1 > MAXINDENT) {
                        done = E_TOODEEP;
                        return ERR_TOK;
                    }
                    if (altcol <= lastAltIndentCol()) {
                        return indenterror();
                    }
                    pendin++;
                    pushIndent(col);
                    pushAltIndent(altcol);
                } else { // col < lstIndentCol()
                    /* Dedent -- any number, must be consistent */
                    while (indent > 0 && col < lastIndentCol()) {
                        pendin--;
                        indent--;
                    }
                    if (col != lastIndentCol()) {
                        done = E_DEDENT;
                        return ERR_TOK;
                    }
                    if (altcol != lastAltIndentCol()) {
                        return indenterror();
                    }
                }
            }
        }
        start = buf.position();

        /* Return pending indents/dedents */
        if (pendin != 0) {
            if (pendin < 0) {
                pendin++;
                return new Tok(DEDENT);
            } else {
                pendin--;
                return new Tok(INDENT);
            }
        }

        // again:
        start = null;
        /* Skip spaces */
        char c = nextc();
        while (c == ' ' || c == '\t' || c == '\014') {
            c = nextc();
        }
        /* Set start of current token */
        start = buf.position() - 1;
        /* Skip comment */
        if (c == '#') {
            while (c != EOF && c != '\n') {
                c = nextc();
            }
        }
        /* Check for EOF and errors now */
        if (c == EOF) {
            return done == E_EOF ? new Tok(ENDMARKER) : ERR_TOK;
        }
        boolean nonascii = false;
        /* Identifier (Most frequent token!) */
        if (isPotentialIdentifierStart(c)) {
            /* Process the various legal combinations of b"", r"", u"", and f"". */
            boolean sawb = false, sawr = false, sawu = false, sawf = false;
            for(;;) {
                if (!(sawb || sawu | sawf) && (c == 'b' || c == 'B')) {
                    sawb = true;
                }
                /* Since this is a backwards compatibility support literal we don't
                   want to support it in arbitrary order like byte literals. */
                else if (!(sawb || sawu || sawr || sawf) && (c == 'u' || c == 'U')) {
                    sawu = true;
                }
                /* ur"" and ru"" are not supported */
                else if (!(sawr || sawu) && (c == 'r' || c == 'R')) {
                    sawr = true;
                } else if (!(sawf || sawb || sawu) && (c == 'f' || c == 'F')) {
                    sawf = true;
                } else {
                    break;
                }
                c = nextc();
                if (c == '"' || c == '\'') {
                    return letterQuote(c);
                }
            }
            while (isPotentialIdentifierPart(c)) {
                if (c > 128) {
                    nonascii = true;
                }
                c = nextc();
            }

            backup(c);
            if (nonascii && !verifyIdentifier()) {
                return ERR_TOK;
            }
            return newtok(NAME);
        }

        /* Newline */
        if (c == '\n') {
            atbol = 1;
            if (blankline || level > 0) {
                return get();
            }
            contLine = false;
            return newtok(NEWLINE);
        }

        /* Period or number starting with period? */
        if (c == '.') {
            c = nextc();
            if (Character.isDigit(c)) {
                return fraction();
            } else if (c == '.') {
                c = nextc();
                if (c == '.') {
                    return newtok(ELLIPSIS);
                } else {
                    backup(c);
                }
            } else {
                backup(c);
            }
            return newtok(DOT);
        }
        /* Number */
        if (Character.isDigit(c)) {
            if (c == '0') {
                /* Hex, octal or binary -- maybe. */
                c = nextc();
                if (c == 'x' || c == 'X') {
                    /* Hex */
                    c = nextc();
                    do {
                        if (c == '_') {
                            c = nextc();
                        }
                        Predicate<Character> isxdigit = x -> (Character.isDigit(x) || (x >= 'a' && x <= 'f') || (x >= 'A' && x <= 'F'));
                        if (!isxdigit.test(c)) {
                            done = E_TOKEN;
                            backup(c);
                            return ERR_TOK;
                        }
                        do {
                            c = nextc();
                        } while (isxdigit.test(c));
                    } while (c == '_');
                } else if (c == 'o' || c == 'O') {
                    /* Octal */
                    c = nextc();
                    do {
                        if (c == '_') {
                            c = nextc();
                        }
                        if (c < '0' || c >= '8') {
                            done = E_TOKEN;
                            backup(c);
                            return ERR_TOK;
                        }
                        do {
                            c = nextc();
                        } while (c >= 0 && c < '8');
                    } while (c == '_');
                } else if (c == 'b' || c == 'B') {
                    /* Binary */
                    c = nextc();
                    do {
                        if (c == '_') {
                            c = nextc();
                        }
                        if (c != '0' || c != '1') {
                            done = E_TOKEN;
                            backup(c);
                            return ERR_TOK;
                        }
                        do {
                            c = nextc();
                        } while (c == '0' || c == '1');
                    } while (c == '_');
                } else {
                    boolean nonzero = false;
                    /* maybe old-style octal; c is first char of it */
                    /* in any case, allow '0' as a literal */
                    for (; ; ) {
                        if (c == '_') {
                            c = nextc();
                            if (!Character.isDigit(c)) {
                                done = E_TOKEN;
                                backup(c);
                                return ERR_TOK;
                            }
                        }
                        if (c != '0') {
                            break;
                        }
                        c = nextc();
                    }
                    if (Character.isDigit(c)) {
                        nonzero = true;
                        c = decimalTail();
                        if (c == 0) {
                            return ERR_TOK;
                        }
                    }
                    if (c == '.') {
                        return fraction();
                    } else if (c == 'e' || c == 'E') {
                        char e = c;
                        /* Exponent part */
                        c = nextc();
                        if (c == '+' || c == '-') {
                            c = nextc();
                            if (!Character.isDigit(c)) {
                                done = E_TOKEN;
                                backup(c);
                                return ERR_TOK;
                            }
                        } else if (!Character.isDigit(c)) {
                            backup(c);
                            backup(e);
                            return newtok(NUMBER);
                        }
                        c = decimalTail();
                        if (c == 0) {
                            return ERR_TOK;
                        }
                        if (c == 'j' || c == 'J') {
                            /* Imaginary part */
                            return newtok(NUMBER);
                        }
                        // XXX there is an extra block in C, don't think it's necessary, but it's why the brackets are not the same
                        backup(c);
                        return newtok(NUMBER);
                    } else if (c == 'j' || c == 'J') {
                        return newtok(NUMBER);
                    } else if (nonzero) {
                        /* Old-style octal; now disallowed */
                        done = E_TOKEN;
                        backup(c);
                        return ERR_TOK;
                    }
                }
            } else {
                /* Decimal */
                c = decimalTail();
                if (c == 0) {
                    return ERR_TOK;
                }
                /* Accept floating point numbers. */
                if (c == '.') {
                    c = nextc();
                    // fraction:
                    /* Fraction */
                    if (Character.isDigit(c)) {
                        c = decimalTail();
                        if (c == 0) {
                            return ERR_TOK;
                        }
                    }
                }
                if (c == 'e' || c == 'E') {
                    // exponent:
                    char e = c;
                    /* Exponent part */
                    c = nextc();
                    if (c == '+' || c == '-') {
                        c = nextc();
                        if (!Character.isDigit(c)) {
                            done = E_TOKEN;
                            backup(c);
                            return ERR_TOK;
                        }
                    } else if (!Character.isDigit(c)) {
                        backup(c);
                        backup(e);
                        return newtok(NUMBER);
                    }
                    c = decimalTail();
                    if (c == 0) {
                        return ERR_TOK;
                    }
                }
                if (c == 'j' || c == 'J') {
                    /* Imaginary part */
                    // imaginary:
                    c = nextc();
                }
            }
            // XXX there is an extra block in C, don't think it's necessary, but it's why the brackets are not the same
            backup(c);
            return newtok(NUMBER);
        }
        // letter_quote:
        /* String */
        if (c == '\'' || c == '"') {
            return letterQuote(c);
        }

        /* Line continuation */
        if (c == '\\') {
            c = nextc();
            if (c != '\n') {
                done = E_LINECONT;
                buf.position(inp);
                return ERR_TOK;
            }
            contLine = true;
            // goto again /* Read nextline */
            return get(); // XXX is this safe?
        }

        /* Check for two-character token */
        char c2 = nextc();
        int token = PyToken_TwoChars(c, c2);
        if (token != OP) {
            char c3 = nextc();
            int token3 = PyToken_ThreeChars(c, c2, c3);
            if (token3 != OP) {
                token = token3;
            } else {
                backup(c3);
            }
            return newtok(token);
        }
        backup(c2);

        /* Keep track of parentheses nesting level */
        switch(c) {
            case '(': case '[': case '{':
                level++;
                break;
            case ')': case ']': case '}':
                level--;
                break;
        }
        /* Punctuation character */
        return newtok(PyToken_OneChar(c));
    }

    private Tok fraction() {
        char c = nextc();
        if (Character.isDigit(c)) {
            c = decimalTail();
            if (c == 0) {
                return ERR_TOK;
            }
        }
        if (c == 'e' || c == 'E') {
            char e = c;
            /* Exponent part */
            c = nextc();
            if (c == '+' || c == '-') {
                c = nextc();
                if (!Character.isDigit(c)) {
                    done = E_TOKEN;
                    backup(c);
                    return ERR_TOK;
                }
            } else if (!Character.isDigit(c)) {
                backup(c);
                backup(e);
                return newtok(NUMBER);
            }
            c = decimalTail();
            if (c == 0) {
                return ERR_TOK;
            }
            if (c == 'j' || c == 'J') {
                /* Imaginary part */
                return newtok(NUMBER);
            }
        }
        backup(c);
        return newtok(NUMBER);
    }

    private Tok letterQuote(char c) {
        char quote = c;
        int quoteSize = 1; /* 1 or 3 */
        int endQuoteSize = 0;
        /* Find the quote size and start of string */
        c = nextc();
        if (c == quote) {
            c = nextc();
            if (c == quote) {
                quoteSize = 3;
            } else {
                quoteSize = 1; /* Empty string found */
            }
        }
        if (c != quote) {
            backup(c);
        }
        /* Get rest of string */
        while (endQuoteSize != quoteSize) {
            c = nextc();
            if (c == EOF) {
                if (quoteSize == 3) {
                    done = E_EOFS;
                } else {
                    done = E_EOLS;
                }
                buf.position(inp);
                return ERR_TOK;
            }
            if (quoteSize == 1 && c == '\n') {
                done = E_EOLS;
                buf.position(inp);
                return ERR_TOK;
            }
            if (c == quote) {
                endQuoteSize++;
            } else {
                endQuoteSize = 0;
                if (c == '\\') {
                    nextc(); /* Skip escaped char */
                }
            }
        }
        return newtok(STRING);
    }


    private char decimalTail() {
        char c;
        for (;;) {
            do {
                c = nextc();
            } while (Character.isDigit(c));
            if (c != '_') {
                break;
            }
            c = nextc();
            if (!Character.isDigit(c)) {
                done = E_TOKEN;
                backup(c);
                return 0;
            }
        }
        return c;
    }


    //static int
//verify_identifier(struct tok_state *tok)
//{
//    PyObject *s;
//    int result;
//    if (tok->decoding_erred)
//        return 0;
//    s = PyUnicode_DecodeUTF8(tok->start, tok->cur - tok->start, NULL);
//    if (s == NULL || PyUnicode_READY(s) == -1) {
//        if (PyErr_ExceptionMatches(PyExc_UnicodeDecodeError)) {
//            PyErr_Clear();
//            tok->done = E_IDENTIFIER;
//        } else {
//            tok->done = E_ERROR;
//        }
//        return 0;
//    }
//    result = PyUnicode_IsIdentifier(s);
//    Py_DECREF(s);
//    if (result == 0)
//        tok->done = E_IDENTIFIER;
//    return result;
//}
    /* Verify that the identifier follows PEP 3131.
       All identifier strings are guaranteed to be "ready" unicode objects.
     */
    private boolean verifyIdentifier() {
        // TODO
        return true;
    }

    private static boolean isPotentialIdentifierPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c >= 128;
    }

    private static boolean isPotentialIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_' || c >= 128;
    }

    static Tok ERR_TOK = new Tok(ERRORTOKEN);

    int x = 0;
    Tok newtok(int tt) {
        if (start < x) {
            System.out.println(String.format("buf is reversing old: %d, new: %d", x, start));
        }
        x = start;
        return new Tok(tt, start, buf.position());
    }

    Tok indenterror() {
        done = E_TABSPACE;
        return ERR_TOK;
    }

    void pushIndent(int col) {
        indstack[++indent] = col;
    }

    void pushAltIndent(int col) {
        altindstack[++indent] = col;
    }
    int lastIndentCol() {
        return indstack[indent];
    }

    int lastAltIndentCol() {
        return altindstack[indent];
    }

    /* Back-up one character */
    void backup(char c) {
        if (c != EOF) {
            int pos = buf.position() - 1;
            if (pos < 0) {
                throw new RuntimeException("tok_backup: beginning of buffer");
            }
            buf.put(pos, c);
            buf.position(pos);
        }
    }

    /* Get next char, update state */
    char nextc() {
        for (;;) {
            if (buf.position() != inp) {
                return buf.get(); /* Fast path */
            }
            if (done == E_EOF) {
                return EOF;
            }
            if (fp == null) {
                buf.mark();
                int _end = -1;
                while(buf.hasRemaining()) {
                    if (buf.get() == '\n') {
                        _end = buf.position() + 1;
                        buf.reset();
                        break;
                    }
                }
                if (_end < 0) {
                    done = E_EOF;
                    return '\uffff'; // EOF
                }
                // XXX if (start == null)
                lineStart = buf.position();
                lineno++;
                inp = _end;
                return buf.get();
            }
            // else if (prompt != null) { /* Interative console */
            else {
                boolean eol = false;
                if (start == null) {
                    if (buf == null) {
                        buf = CharBuffer.allocate(BUFSIZ);
                    }
                    try {
                        end = fp.read(buf);
                        if (end == 0) {
                            return EOF;
                        }
                        if (end < buf.capacity()) {
                            done = E_EOF;
                            eol = true;
                            buf.put('\n');
                            buf.flip();
                            inp = buf.limit();
                        } else {
                            done = E_OK;
                            buf.flip();
                            inp = buf.limit();
                            eol = inp == buf.position() || buf.get(inp - 1)  == '\n';
                        }

                    } catch (IOException e) {
                        System.out.println("====== done====== ");
                        throw new RuntimeException("done");
//                        done = E_EOF;
//                        eol = true;
                    }
                } else {
                    if (decodingFEOF(fp)) {
                        done = E_EOF;
                        eol = true;
                    }
                    done = E_OK;
                }
                lineno++;
                System.out.println("lineno: " + lineno);
                /* Read until '\n' or EOF */
                while (!eol) {
                    int curstart = start == null ? -1 : start;
                    int curvalid = buf.limit() - buf.position();
                    int newsize = curvalid + BUFSIZ;
                    CharBuffer newbuf = CharBuffer.allocate(newsize);
                    newbuf.put(buf);
                    this.buf = newbuf;
                    lineStart = buf.position(); // = tok->cur
                    inp = curvalid;
                    start = curstart < 0 ? null : curstart;
                    try {
                        int len = fp.read(newbuf);
                        if (len < BUFSIZ) {
                            /* Last line does not end in \n, fake one */
                            buf.put('\n');
//                            eol = true;
//                            continue;
                        }
                    } catch (IOException e) {
                        return '\uffff'; // EOF
                    }
                    buf.flip();
                    end = buf.capacity();
                    buf.mark();
                    inp = buf.limit();
                    eol = buf.get(inp - 1) == '\n';
//                    try {
//                        for (;;) {
//                            char c = buf.get();
//                            if (c == '\n') {
//                                break;
//                            }
//                        }
//                        inp = buf.position();
//                        eol = true;
//                    } catch (BufferUnderflowException e) {
//                        buf.reset();
//                        eol = false;
//                    }
                }
                if (buf != null) {
                    /* replace '\r\n' with '\n' */
                    int pt = buf.limit() - 2;
                    if (pt >= buf.position() && buf.get(pt) == '\r') {
                        buf.put(pt + 1, '\n');
                        buf.limit(buf.limit() - 1);
                    }
                }
            }
            if (done != E_OK) {
                return '\uffff'; // EOF
            }
        }
    }

    // Check if file has reached EOF
    private boolean decodingFEOF(Reader fp) {
        assert fp.markSupported(): "Must support mark";
        try {
            fp.mark(1);
            fp.read();
            fp.reset();
        } catch (IOException e) {
            return true;
        }
        return false;
    }

    static class Tok {
        public Tok(int type) {
            this.type = type;
        }
        int type;
        int start, end;

        public Tok(int type, Integer start, int end) {
            this.type = type;
            this.start = start;
            this.end = end;
        }

        public int length() {
            return end - start;
        }
    }

    /* Token names */
    public static final String[] TOKEN_NAMES = {
            "ENDMARKER",
            "NAME",
            "NUMBER",
            "STRING",
            "NEWLINE",
            "INDENT",
            "DEDENT",
            "LPAR",
            "RPAR",
            "LSQB",
            "RSQB",
            "COLON",
            "COMMA",
            "SEMI",
            "PLUS",
            "MINUS",
            "STAR",
            "SLASH",
            "VBAR",
            "AMPER",
            "LESS",
            "GREATER",
            "EQUAL",
            "DOT",
            "PERCENT",
            "LBRACE",
            "RBRACE",
            "EQEQUAL",
            "NOTEQUAL",
            "LESSEQUAL",
            "GREATEREQUAL",
            "TILDE",
            "CIRCUMFLEX",
            "LEFTSHIFT",
            "RIGHTSHIFT",
            "DOUBLESTAR",
            "PLUSEQUAL",
            "MINEQUAL",
            "STAREQUAL",
            "SLASHEQUAL",
            "PERCENTEQUAL",
            "AMPEREQUAL",
            "VBAREQUAL",
            "CIRCUMFLEXEQUAL",
            "LEFTSHIFTEQUAL",
            "RIGHTSHIFTEQUAL",
            "DOUBLESTAREQUAL",
            "DOUBLESLASH",
            "DOUBLESLASHEQUAL",
            "AT",
            "ATEQUAL",
            "RARROW",
            "ELLIPSIS",
            /* This table must match the #defines in token.h! */
            "OP",
            "<ERRORTOKEN>",
            "COMMENT",
            "NL",
            "ENCODING",
            "<N_TOKENS>"
    };

    /* Return the token corresponding to a single character */
    static int PyToken_OneChar(char c) {
        switch (c) {
            case '(':
                return LPAR;
            case ')':
                return RPAR;
            case '[':
                return LSQB;
            case ']':
                return RSQB;
            case ':':
                return COLON;
            case ',':
                return COMMA;
            case ';':
                return SEMI;
            case '+':
                return PLUS;
            case '-':
                return MINUS;
            case '*':
                return STAR;
            case '/':
                return SLASH;
            case '|':
                return VBAR;
            case '&':
                return AMPER;
            case '<':
                return LESS;
            case '>':
                return GREATER;
            case '=':
                return EQUAL;
            case '.':
                return DOT;
            case '%':
                return PERCENT;
            case '{':
                return LBRACE;
            case '}':
                return RBRACE;
            case '^':
                return CIRCUMFLEX;
            case '~':
                return TILDE;
            case '@':
                return AT;
            default:
                return OP;
        }
    }


    static int PyToken_TwoChars(char c1, char c2) {
        switch (c1) {
            case '=':
                switch (c2) {
                    case '=':
                        return EQEQUAL;
                }
                break;
            case '!':
                switch (c2) {
                    case '=':
                        return NOTEQUAL;
                }
                break;
            case '<':
                switch (c2) {
                    case '>':
                        return NOTEQUAL;
                    case '=':
                        return LESSEQUAL;
                    case '<':
                        return LEFTSHIFT;
                }
                break;
            case '>':
                switch (c2) {
                    case '=':
                        return GREATEREQUAL;
                    case '>':
                        return RIGHTSHIFT;
                }
                break;
            case '+':
                switch (c2) {
                    case '=':
                        return PLUSEQUAL;
                }
                break;
            case '-':
                switch (c2) {
                    case '=':
                        return MINEQUAL;
                    case '>':
                        return RARROW;
                }
                break;
            case '*':
                switch (c2) {
                    case '*':
                        return DOUBLESTAR;
                    case '=':
                        return STAREQUAL;
                }
                break;
            case '/':
                switch (c2) {
                    case '/':
                        return DOUBLESLASH;
                    case '=':
                        return SLASHEQUAL;
                }
                break;
            case '|':
                switch (c2) {
                    case '=':
                        return VBAREQUAL;
                }
                break;
            case '%':
                switch (c2) {
                    case '=':
                        return PERCENTEQUAL;
                }
                break;
            case '&':
                switch (c2) {
                    case '=':
                        return AMPEREQUAL;
                }
                break;
            case '^':
                switch (c2) {
                    case '=':
                        return CIRCUMFLEXEQUAL;
                }
                break;
            case '@':
                switch (c2) {
                    case '=':
                        return ATEQUAL;
                }
                break;
        }
        return OP;
    }

    static int PyToken_ThreeChars(int c1, int c2, int c3) {
        switch (c1) {
            case '<':
                switch (c2) {
                    case '<':
                        switch (c3) {
                            case '=':
                                return LEFTSHIFTEQUAL;
                        }
                        break;
                }
                break;
            case '>':
                switch (c2) {
                    case '>':
                        switch (c3) {
                            case '=':
                                return RIGHTSHIFTEQUAL;
                        }
                        break;
                }
                break;
            case '*':
                switch (c2) {
                    case '*':
                        switch (c3) {
                            case '=':
                                return DOUBLESTAREQUAL;
                        }
                        break;
                }
                break;
            case '/':
                switch (c2) {
                    case '/':
                        switch (c3) {
                            case '=':
                                return DOUBLESLASHEQUAL;
                        }
                        break;
                }
                break;
            case '.':
                switch (c2) {
                    case '.':
                        switch (c3) {
                            case '.':
                                return ELLIPSIS;
                        }
                        break;
                }
                break;
        }
        return OP;
    }
}
