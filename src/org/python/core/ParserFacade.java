// Copyright (c) Corporation for National Research Initiatives
package org.python.core;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.IntegerList;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.python.antlr.*;
import org.python.antlr.base.mod;
import org.python.core.io.StreamIO;
import org.python.core.io.TextIOInputStream;
import org.python.core.io.UniversalIOWrapper;
import org.python.core.util.StringUtil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Facade for the classes in the org.python.antlr package.
 */
public class ParserFacade {

    private static int MARK_LIMIT = 100000;
    private ParserFacade() {}

    public static PyException fixParseError(Throwable t, String filename) {
        if (t instanceof PySyntaxError) {
            return (PySyntaxError) t;
        }
        Throwable cause = t.getCause();
        if (t instanceof ParseCancellationException && cause instanceof RecognitionException) {
            boolean indentationError = false;
            String msg = null;
            String text = "";
            Token tok = ((RecognitionException) cause).getOffendingToken();
            if (cause instanceof InputMismatchException) {
                int tokType = tok.getType();
                indentationError = tokType == PythonParser.INDENT || tokType == PythonParser.DEDENT;
                text = tok.getText();
                switch(tokType) {
                    case PythonParser.EOF:
                        msg = "unexpected EOF while parsing";
                        break;
                    case PythonParser.INDENT:
                        msg = "unexpected indent";
                        break;
                    case PythonParser.DEDENT:
                        msg = "unindent does not match any outer indentation level";
                        break;
                    default:
                        IntervalSet toks = ((InputMismatchException) cause).getExpectedTokens();
                        indentationError = toks.contains(PythonParser.INDENT);
                        if (indentationError) {
                            msg = "expected an indented block";
                        }
                        break;
                }
            } else if (cause instanceof NoViableAltException) {
                String cmd = ((NoViableAltException) cause).getCtx().getText();
                if (cmd.equals("exec") || cmd.equals("print")) {
                    msg = String.format("Missing parentheses in call to '%s'", cmd);
                }
            }
            if (msg == null) {
                msg = "invalid syntax";
            }

            int line = tok.getLine();
            int col = tok.getCharPositionInLine();
            if (indentationError) {
                return new PyIndentationError(msg, line, col, text, filename);
            }
            return new PySyntaxError(msg, line, col, text, filename);
        } else if (t instanceof CharacterCodingException) {
            throw Py.SyntaxError(t.getMessage());
        } else {
            return Py.JavaError(t);
        }
    }

    /**
     * Parse Python source as either an expression (if possible) or module.
     *
     * Designed for use by a JSR 223 implementation: "the Scripting API does not distinguish
     * between scripts which return values and those which do not, nor do they make the
     * corresponding distinction between evaluating or executing objects." (SCR.4.2.1)
     */
    public static mod parseExpressionOrModule(Reader reader,
                                String filename,
                                CompilerFlags cflags) {
        ExpectedEncodingBufferedReader bufReader = null;
        try {
            bufReader = prepBufReader(reader, cflags, filename);
            // first, try parsing as an expression
            return parse(bufReader, CompileMode.eval, filename, cflags);
        } catch (Throwable t) {
            if (bufReader == null) {
                throw Py.JavaError(t); // can't do any more
            }
            try {
                // then, try parsing as a module
                bufReader.reset();
                return parse(bufReader, CompileMode.exec, filename, cflags);
            } catch (Throwable tt) {
                throw fixParseError(tt, filename);
            }
        }
    }

    /**
     * Parser entry point.
     *
     * Users of this method should call fixParseError on any Throwable thrown
     * from it, to translate ParserExceptions into PySyntaxErrors or
     * PyIndentationErrors.
     *
     * Also the caller is responsible for closing the reader
     */
    public static mod parseOnly(ExpectedEncodingBufferedReader reader,
                                CompileMode kind,
                                String filename,
                                CompilerFlags cflags) throws Throwable {
        reader.mark(MARK_LIMIT); // We need the ability to move back on the
                                 // reader, for the benefit of fixParseError and
                                 // validPartialSentence
        CharStream cs = new ANTLRInputStream(reader);
        BaseParser parser = new BaseParser(cs, filename, cflags.encoding);
        return kind.dispatch(parser);
    }

    public static mod parse(Reader reader,
                                CompileMode kind,
                                String filename,
                                CompilerFlags cflags) {
        ExpectedEncodingBufferedReader bufReader = null;
        try {
            bufReader = prepBufReader(reader, cflags, filename);
            return parseOnly(bufReader, kind, filename, cflags );
        } catch (Throwable t) {
            throw fixParseError(t, filename);
        } finally {
            close(bufReader);
        }
    }

    public static mod parse(InputStream stream,
                                CompileMode kind,
                                String filename,
                                CompilerFlags cflags) {
        ExpectedEncodingBufferedReader bufReader = null;
        try {
            // prepBufReader takes care of encoding detection and universal
            // newlines:
            bufReader = prepBufReader(stream, cflags, filename, false);
            return parseOnly(bufReader, kind, filename, cflags );
        } catch (Throwable t) {
            throw fixParseError(t, filename);
        } finally {
            close(bufReader);
        }
    }

    public static mod parse(String string,
                                CompileMode kind,
                                String filename,
                                CompilerFlags cflags) {
        ExpectedEncodingBufferedReader bufReader = null;
        try {
            bufReader = prepBufReader(string, cflags, filename);
            return parseOnly(bufReader, kind, filename, cflags);
        } catch (Throwable t) {
            throw fixParseError(t, filename);
        } finally {
            close(bufReader);
        }
    }

    public static mod partialParse(String string,
                                       CompileMode kind,
                                       String filename,
                                       CompilerFlags cflags,
                                       boolean stdprompt) {
        // XXX: What's the idea of the stdprompt argument?
        ExpectedEncodingBufferedReader reader = null;
        try {
            reader = prepBufReader(string, cflags, filename);
            return parseOnly(reader, kind, filename, cflags);
        } catch (PySyntaxError e) {
            /** This is thrown from AST builder, don't need fixing */
            throw e;
        } catch (Throwable t) {
            try {
                reader = prepBufReader(string, cflags, filename);
            } catch (IOException e) {
            }
            if (reader != null && validPartialSentence(reader, kind, filename)) {
                return null;
            }
            PyException p = fixParseError(t, filename);
            throw p;
        } finally {
            close(reader);
        }
    }

    private static boolean validPartialSentence(BufferedReader bufreader, CompileMode kind, String filename) {
        PythonLexer lexer = null;
        try {
//            bufreader.reset();
            CharStream cs = new NoCloseReaderStream(bufreader);
            lexer = new PythonLexer(cs);
            lexer.single = true;
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            PythonParser parser = new PythonParser(tokens);
            parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
            parser.setErrorHandler(new BailErrorStrategy());
            switch (kind) {
            case single:
                parser.single_input();
                break;
            case eval:
                parser.eval_input();
                break;
            default:
                return false;
            }
        } catch (Exception e) {
            return !lexer.indents.isEmpty() || lexer._input.LA(1) == PythonParser.EOF;
        }
        return true;
    }

    public static class ExpectedEncodingBufferedReader extends BufferedReader {

        /**
         * The encoding from the source file, or null if none was specified and UTF-8 is being used.
         */
        public final String encoding;

        public ExpectedEncodingBufferedReader(Reader in, String encoding) {
            super(in);
            this.encoding = encoding;
        }
    }

    private static ExpectedEncodingBufferedReader prepBufReader(Reader reader,
                                                                CompilerFlags cflags,
                                                                String filename)
        throws IOException {
        cflags.source_is_utf8 = true;
        cflags.encoding = "utf-8";

        BufferedReader bufferedReader = new BufferedReader(reader);
//        bufferedReader.mark(MARK_LIMIT);
//        if (findEncoding(bufferedReader) != null) {
//            throw new ParseException("encoding declaration in Unicode string");
//        }
//        bufferedReader.reset();

        return new ExpectedEncodingBufferedReader(bufferedReader, null);
    }

    public static ExpectedEncodingBufferedReader prepBufReader(InputStream input,
                                                        CompilerFlags cflags,
                                                        String filename,
                                                        boolean fromString)
        throws IOException {
        return prepBufReader(input, cflags, filename, fromString, true);
    }

    private static ExpectedEncodingBufferedReader prepBufReader(InputStream input,
                                                                CompilerFlags cflags,
                                                                String filename,
                                                                boolean fromString,
                                                                boolean universalNewlines)
            throws IOException {
        input = new BufferedInputStream(input);
        boolean bom = adjustForBOM(input);
        String encoding = readEncoding(input);

        if (encoding == null) {
            if (bom) {
                encoding = "utf-8";
            } else if (cflags != null && cflags.encoding != null) {
                encoding = cflags.encoding;
            }
        }
        if (cflags.source_is_utf8) {
            if (encoding != null) {
                throw new ParseException("encoding declaration in Unicode string");
            }
            encoding = "utf-8";
        }
        cflags.encoding = encoding;

        if (universalNewlines) {
            // Enable universal newlines mode on the input
            StreamIO rawIO = new StreamIO(input, true);
            org.python.core.io.BufferedReader bufferedIO =
                    new org.python.core.io.BufferedReader(rawIO, 0);
            UniversalIOWrapper textIO = new UniversalIOWrapper(bufferedIO);
            input = new TextIOInputStream(textIO);
        }

        Charset cs;
        try {
            // Use UTF-8 for the raw bytes when no encoding was specified
            if (encoding == null) {
                cs = Charset.forName("UTF-8");
            } else {
                cs = Charset.forName(encoding);
            }
        } catch (UnsupportedCharsetException exc) {
            throw new PySyntaxError("Unknown encoding: " + encoding, 1, 0, "", filename);
        }
        CharsetDecoder dec = cs.newDecoder();
        dec.onMalformedInput(CodingErrorAction.REPORT);
        dec.onUnmappableCharacter(CodingErrorAction.REPORT);
        return new ExpectedEncodingBufferedReader(new InputStreamReader(input, dec), encoding);
    }

    private static ExpectedEncodingBufferedReader prepBufReader(String string,
            CompilerFlags cflags,
            String filename)
            throws IOException {
        if (cflags.source_is_utf8) {
            return prepBufReader(new StringReader(string), cflags, filename);
        }

        byte[] stringBytes = StringUtil.toBytes(string);
        return prepBufReader(new ByteArrayInputStream(stringBytes), cflags, filename, true, false);
    }

    /**
     * Check for a BOM mark at the beginning of stream.  If there is a BOM
     * mark, advance the stream passed it.  If not, reset() to start at the
     * beginning of the stream again.
     *
     * Only checks for EF BB BF right now, since that is all that CPython 2.5
     * Checks.
     *
     * @return true if a BOM was found and skipped.
     * @throws ParseException if only part of a BOM is matched.
     *
     */
    private static boolean adjustForBOM(InputStream stream) throws IOException {
        stream.mark(3);
        int ch = stream.read();
        if (ch == 0xEF) {
            if (stream.read() != 0xBB) {
                throw new ParseException("Incomplete BOM at beginning of file");
            }
            if (stream.read() != 0xBF) {
                throw new ParseException("Incomplete BOM at beginning of file");
            }
            return true;
        }
        stream.reset();
        return false;
    }

    private static String readEncoding(InputStream stream) throws IOException {
        stream.mark(MARK_LIMIT);
        String encoding = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"), 512);
        encoding = findEncoding(br);
        // XXX: reset() can still raise an IOException if a line exceeds our large mark
        // limit
        stream.reset();
        return encodingMap(encoding);
    }

    /**
     * Reads the first two lines of the reader, searching for an encoding
     * declaration.
     *
     * Note that reseting the reader (if needed) is responsibility of the caller.
     *
     * @return The declared encoding, or null if no encoding declaration is
     *         found
     */
    private static String findEncoding(BufferedReader br)
            throws IOException {
        String encoding = null;
        for (int i = 0; i < 2; i++) {
            String strLine = br.readLine();
            if (strLine == null) {
                break;
            }
            String result = matchEncoding(strLine);
            if (result != null) {
                encoding = result;
                break;
            }
        }
        return encoding;
    }

    private static String encodingMap(String encoding) {
        if (encoding == null) {
            return null;
        }
        if (encoding.equals("Latin-1") || encoding.equals("latin-1")) {
            return "ISO8859_1";
        }
        return encoding;
    }

    private static final Pattern pep263EncodingPattern = Pattern.compile("#.*coding[:=]\\s*([-\\w.]+)");

    private static String matchEncoding(String inputStr) {
        Matcher matcher = pep263EncodingPattern.matcher(inputStr);
        boolean matchFound = matcher.find();

        if (matchFound && matcher.groupCount() == 1) {
            String groupStr = matcher.group(1);
            return groupStr;
        }
        return null;
    }

    private static void close(BufferedReader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException i) {
            // XXX: Log the error?
        }
    }

}
