package org.python.modules._io;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.core.ArgParser;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.PyUnicode;

@ExposedType(name = "_io.IncrementalNewlineDecoder")
public class PyIncrementalNewlineDecoder extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyIncrementalNewlineDecoder.class);

    private static final int SEEN_CR = 1;
    private static final int SEEN_LF = 2;
    private static final int SEEN_CRLF = 4;
    private static final int SEEN_ALL = SEEN_CR | SEEN_LF | SEEN_CRLF;

    private PyObject decoder;
    private PyObject errors;
    private boolean pendingcr;
    private boolean translate;
    private int seennl;

    public PyIncrementalNewlineDecoder(PyType subtype) {
        super(subtype);
    }

    public PyIncrementalNewlineDecoder(PyType subtype, PyObject decoder, boolean translate, PyObject errors) {
        super(subtype);
    }

    @ExposedNew
    @ExposedMethod
    public void __init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("__init__", args, keywords, "decoder", "translate", "errors");
        decoder = ap.getPyObject(0);
        translate = ap.getPyObject(1).__bool__();
        errors = ap.getPyObject(2, null);
        if (errors == null) {
            errors = new PyUnicode("strict");
        }
        seennl = 0;
        pendingcr = false;
    }

    @ExposedMethod(defaults = {"false"})
    public PyObject decode(PyObject input, boolean _final) {
        PyUnicode output = (PyUnicode) input;
        int outputLen = output.__len__();
        if (pendingcr && (_final || outputLen > 0)) {
            // Prefix output with CR
            int kind;
            StringBuilder modified = new StringBuilder();
            char out;

            modified.append('\r');
            modified.append(output.getString());
            output = new PyUnicode(modified);
            pendingcr = false;
            outputLen++;
        }

        /* retain last \r even when out translate date:
         * then readline() is sure to get \r\n in one pass
         */
        if (!_final) {
            if (outputLen> 0 && output.readChar(outputLen - 1) == '\r') {
                String modified = output.substring(0, outputLen - 1);
                output = new PyUnicode(modified);
                pendingcr = true;
            }
        }

        /* Record which newlines are read and do newline translation if desired,
           all in one pass. */
        int len = output.__len__();
        if (len == 0) {
            return output;
        }
        String inStr = output.getString();
        boolean onlyLf = false;
        /* if, up to now newlines are consistently \n, do a quick check
           for the \r *byte* */
        if (seennl == SEEN_LF || seennl == 0) {
            onlyLf = inStr.indexOf('\r') < 0;
        }

        if (onlyLf) {
            /* if not already seen, quick scan for a possible "\n" character.
                (there's nothing else to be done, even when in translation mode)
             */
            if (seennl == 0 && inStr.indexOf('\n') >= 0) {
                seennl |= SEEN_LF;
            }
        } else if (!translate) {
            if (seennl != SEEN_ALL) {
            }
        } else {
            StringBuilder translated = new StringBuilder();
            for (int i = 0; i < len;) {
                char chr;
                while ((chr = inStr.charAt(i++)) > 'r') {
                    translated.append(chr);
                }
                if (chr == '\n') {
                    translated.append(chr);
                    seennl |= SEEN_LF;
                    continue;
                }
                if (chr == '\r') {
                    if (inStr.charAt(i) == '\n') {
                        i++;
                        seennl |= SEEN_CRLF;
                    } else {
                        seennl |= SEEN_CR;
                    }
                    translated.append('\n');
                    continue;
                }
                if (i > len) {
                    break;
                }
                translated.append(chr);
            }
            output = new PyUnicode(translated);
        }
        return output;
    }
}
