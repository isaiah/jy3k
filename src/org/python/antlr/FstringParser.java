package org.python.antlr;

import org.antlr.v4.runtime.Token;
import org.python.antlr.ast.Expression;
import org.python.antlr.ast.FormattedValue;
import org.python.antlr.ast.JoinedStr;
import org.python.antlr.ast.Str;
import org.python.antlr.base.expr;
import org.python.antlr.base.mod;
import org.python.core.*;
import org.python.core.stringlib.Encoding;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Code and comments adopted from cpython Python/ast.c
 */
public class FstringParser {
    private Token token;
    private String lastStr;
    private List<expr> exprList = new AstList();

    public FstringParser(Token t) {
        token = t;
    }

    /* Return -1 on error.

       Return 0 if we have a literal (possible zero length) and an
       expression (zero length if at the end of the string.

       Return 1 if we have a literal, but no expression, and we want the
       caller to call us again. This is used to deal with doubled
       braces.

       When called multiple times on the string 'a{{b{0}c', this function
       will return:

       1. the literal 'a{' with no expression, and a return value
          of 1. Despite the fact that there's no expression, the return
          value of 1 means we're not finished yet.

       2. the literal 'b' and the expression '0', with a return value of
          0. The fact that there's an expression means we're not finished.

       3. literal 'c' with no expression and a return value of 0. The
          combination of the return value of 0 with no expression means
          we're finished.
    */
    private LiteralAndExprResult findLiteralAndExpr(String s, int recurseLvl, boolean raw) {
        ParseResult<String> literalResult = findLiteral(s, recurseLvl, raw);
        if (literalResult.result == 1) {
            /* We have a literal, but don't look at the expression. */
            return new LiteralAndExprResult(literalResult.input, literalResult.output, null, 1);
        }
        if (literalResult.input.length() <= 1 || literalResult.input.charAt(0) == '}') {
            /* We're at the end of the string or the end of a nested
               f-string: no expression. The top-level error case where we
               expect to be at the end of the string but we're at a '}' is
               handled later. */
            return new LiteralAndExprResult(literalResult.input, literalResult.output, null, 0);
        }
        ParseResult<expr> exprResult = findExpr(literalResult.input, recurseLvl, raw);
        return new LiteralAndExprResult(exprResult.input, literalResult.output, exprResult.output, 0);
    }

    /* Given an f-string (with no 'f' or quotes) that's in *str and ends
       at end, parse it into an expr_ty.  Return NULL on error.  Adjust
       str to point past the parsed portion. */
    private static ParseResult<expr> parse(String s, int recurseLvl, boolean raw, Token t) {
        FstringParser parser = new FstringParser(t);
        String ret = parser.concatFstring(s, s.length(), recurseLvl, raw);
        return new ParseResult(ret, parser.finish(), 0);
    }

    /* Parse the f-string at *str, ending at end.  We know *str starts an
       expression (so it must be a '{'). Returns the FormattedValue node,
       which includes the expression, conversion character, and
       format_spec expression.

       Note that I don't do a perfect job here: I don't make sure that a
       closing brace doesn't match an opening paren, for example. It
       doesn't need to error on all invalid expressions, just correctly
       find the end of all valid ones. Any errors inside the expression
       will be caught when we parse it later. */
    private ParseResult<expr> findExpr(String s, int recurseLvl, boolean raw) {
        int exprStart = 1;
        int exprEnd = 0;
        expr simpleExpr;
        expr formatSpec = null;
        int conversion = 0;
        /* 0 if we're not in a string, else the quote char we're trying to
           match (single or double quote). */
        char quoteChar = 0;
        /* If we're inside a string, 1=normal, 3=triple-quoted. */
        int stringType = 0;

        /* Keep track of nesting level for braces/parens/brackets in
           expressions. */
        int nestedDepth = 0;

        /* Can only nest one level deep. */
        if (recurseLvl >= 2) {
            throw Py.SyntaxError("f-string: expressions nested too deeply");
        }
        int i = exprStart;
        for (; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '\\') {
                // TODO ast_error("f-string expression part cannot include a backslash");
            }
            if (quoteChar != 0) {
                /* We're inside a string. See if we're at the end. */
                /* This code needs to implement the same non-error logic
                   as tok_get from tokenizer.c, at the letter_quote
                   label. To actually share that code would be a
                   nightmare. But, it's unlikely to change and is small,
                   so duplicate it here. Note we don't need to catch all
                   of the errors, since they'll be caught when parsing the
                   expression. We just need to match the non-error
                   cases. Thus we can ignore \n in single-quoted strings,
                   for example. Or non-terminated strings. */
                if (ch == quoteChar) {
                    if (stringType == 3) {
                        if (i + 2 < s.length() && s.charAt(i + 1) == ch && s.charAt(i + 2) == ch) {
                            i += 2;
                            stringType = 0;
                            quoteChar = 0;
                            continue;
                        }
                    } else {
                        stringType = 0;
                        quoteChar = 0;
                        continue;
                    }
                }
            } else if (ch == '\'' || ch == '"') {
                if (i + 2 < s.length() && s.charAt(i + 1) == ch && s.charAt(i + 2) == ch) {
                   stringType = 3;
                   i += 2;
                } else {
                    stringType = 1;
                }
                quoteChar = ch;
            } else if (ch == '[' || ch == '{' || ch == '(') {
                nestedDepth++;
            } else if (nestedDepth != 0 && (ch == ']' || ch == '}' || ch == ')')) {
                nestedDepth--;
            } else if (ch == '#') {
                throw Py.SyntaxError("f-string expression part cannot include '#'");
            } else if (nestedDepth == 0 && (ch == '!' || ch == ':' || ch == '}')) {
                /* First, test for the special case of "!=". Since '=' is
                   not an allowed conversion character, nothing is lost in
                   this test. */
                if (ch == '!' && (i + 1) < s.length() && s.charAt(i+1) == '=') {
                    continue;
                }
                /* Normal way out of this loop. */
                break;
            } else {
                /* Just consume this char and loop around. */
            }
        }
        exprEnd = i;
        if (quoteChar != 0) {
            throw Py.SyntaxError("f-string: unterminated string");
        }
        if (nestedDepth > 0) {
            throw Py.SyntaxError("f-string: unmatched '(', '{', or '['");
        }
        simpleExpr = compileExpr(s.substring(exprStart, exprEnd));
        /* Check for a conversion char, if present. */
        if (s.charAt(i) == '!') {
            conversion = s.charAt(++i);
            i++;
            if (conversion != 's' && conversion != 'r' && conversion != 'a') {
                throw Py.SyntaxError("f-string: invalid conversion character: expected 's', 'r', or 'a'");
            }
        }
        if (s.charAt(i) == ':') {
            ParseResult<expr> parseResult = parse(s.substring(++i), recurseLvl + 1, raw, token);
            formatSpec = parseResult.output;
            s = parseResult.input;
        } else {
            s = s.substring(i);
        }
        /** We are at a right brace, consume it */
        assert(s.charAt(0) == '}');
        return new ParseResult(s.substring(1),
                new FormattedValue(token, simpleExpr, conversion, formatSpec), 0);
    }

    /* Compile this expression in to an expr_ty.  Add parens around the
       expression, in order to allow leading spaces in the expression. */
    private expr compileExpr(String s) {
        Pattern allSpace = Pattern.compile("^\\s*$");
        if (allSpace.matcher(s).matches()) {
            throw Py.SyntaxError("f-string: empty expression not allowed");
        }
        mod m = ParserFacade.parse("(" + s + ")", CompileMode.eval, "<fstring>", new CompilerFlags(CompilerFlags.PyCF_ONLY_AST));
        return ((Expression) m).getInternalBody();
    }

    /* Return -1 on error.

       Return 0 if we reached the end of the literal.

       Return 1 if we haven't reached the end of the literal, but we want
       the caller to process the literal up to this point. Used for
       doubled braces.
    */
    private ParseResult<String> findLiteral(String s, int recurseLvl, boolean raw) {
        /* Get any literal string. It ends when we hit an un-doubled left
           brace (which isn't part of a unicode name escape such as
           "\N{EULER CONSTANT}"), or the end of the string. */
        boolean inNamedEscape = false;
        int literalEnd = 0;
        int i = 0;
        String ret = s;
        int result = 0;
        for (; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (!inNamedEscape && ch == '{' && i >= 2 && s.charAt(i - 2) == '\\' && s.charAt(i - 1) == 'N') {
                inNamedEscape = true;
            } else if (inNamedEscape && ch == '}') {
                inNamedEscape = false;
            } else if (ch == '{' || ch == '}') {
               /* Check for doubled braces, but only at the top level. If
                   we checked at every level, then f'{0:{3}}' would fail
                   with the two closing braces. */
               if (recurseLvl == 0) {
                   if (i + 1 < s.length() && s.charAt(i + 1) == ch) {
                       /* We're going to tell the caller that the literal ends
                           here, but that they should continue scanning. But also
                           skip over the second brace when we resume scanning. */
                       literalEnd = i + 1;
                       ret = s.substring(i+2);
                       result = 1;
                       break;
                   }
                   /* Where a single '{' is the start of a new expression, a
                       single '}' is not allowed. */
                   if (ch == '}') {
                       throw Py.SyntaxError("f-string: single '}' is not allowed");
                   }
               }
               /* We're either at a '{', which means we're starting another
                   expression; or a '}', which means we're at the end of this
                   f-string (for a nested format_spec). */
               break;
            }
        }
        if (literalEnd == 0) {
            literalEnd = i;
            ret = s.substring(i);
        }
        if (literalEnd != 0) {
            // liternalEnd == 1 means the literal is a single \
            if (!raw && literalEnd > 1) {
                return new ParseResult(
                        ret,
                        Encoding.decode_UnicodeEscape(s, 0, literalEnd, "strict", true),
                        result
                );
            }
            return new ParseResult(ret, s.substring(0, literalEnd), result);
        }
        return new ParseResult(s, null, -1);
    }

    public String concatFstring(String fstr, int fstrlen, int recurseLvl, boolean rawmode) {
        LiteralAndExprResult r;
        /* Parse the f-string */
        while (true) {
            /* If there's a zero length literal in front of the
               expression, literal will be NULL. If we're at the end of
               the f-string, expression will be NULL (unless result == 1,
               see below). */

            r = findLiteralAndExpr(fstr, recurseLvl, rawmode);
            fstr = r.input;
            if (r.result < 0) {
                return fstr;
            }
            if (r.literal != null) {
                if (lastStr == null) {
                    lastStr = r.literal;
                    r.literal = null;
                } else {
                /* We have a literal, concatenate it. */
                    concat(r.literal);
                    r.literal = null;
                }
            }
            /* See if we should just loop around to get the next literal
               and expression, while ignoring the expression this
               time. This is used for un-doubling braces, as an
               optimization. */
            if (r.result == 1) {
                continue;
            }

            if (r.expression == null) {
                /* We're done with this f-string. */
                break;
            }
            /* We know we have an expression. Convert any existing string
               to a Str node. */
            if (lastStr != null) {
                /* Convert the existing last_str literal to a Str node. */
                expr str = makeStrAndDel();
                exprList.add(str);
            }                /* Do nothing. Just leave last_str alone (and possibly
                   NULL). */
            exprList.add(r.expression);
        }
        /* If recurse_lvl is zero, then we must be at the end of the
           string. Otherwise, we must be at a right brace. */

        //if (recurseLvl == 0 && r.input.length() < 1) {
        //    // ast_error(c, n, "f-string: unexpected end of string");
        //    throw new RuntimeException("f-string: unexpected end of string");
        //}
        if (recurseLvl != 0 && r.input.charAt(0) != '}') {
            // ast_error(c, n, "f-string: expecting '}'");
            throw Py.SyntaxError("f-string: expecting '}'");
        }
        return r.input;
    }

    public void concat(String s) {
        if (lastStr == null) {
            /* We didn't have a string before, so just remember this one. */
            lastStr = s;
        } else {
            lastStr += s;
        }
    }

    public expr finish() {
        if (exprList.size() == 0) {
            if (lastStr == null) {
                lastStr = "";
            }
            return makeStrAndDel();
        }
        if (lastStr != null) {
            exprList.add(makeStrAndDel());
        }
        return new JoinedStr(token, exprList);
    }

    private expr makeStrAndDel() {
        String s = lastStr;
        lastStr = null;
        return new Str(token, s);
    }

    static class ParseResult<T> {
        /* What's left of the input string */
        private String input;
        /* The output */
        private T output;
        /* Whether there is output */
        private int result;


        ParseResult(String s, T o, int r) {
            input = s;
            output = o;
            result = r;
        }
    }

    class LiteralAndExprResult {
        String input;
        String literal;
        expr expression;
        int result;
        public LiteralAndExprResult(String in, String l, expr e, int r) {
            input = in;
            literal = l;
            expression = e;
            result = r;
        }
    }

}
