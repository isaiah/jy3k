package org.python.parser;

import static org.python.parser.TokenType.*;

/**
 * Tokenizer.c
 */
public class Tokenizer {
    char[] buf; /* Input buffer */
    int cur; /* Next character in buffer */
    int start; /* Start of current token */
    boolean done; /* E_OF normally */
    int tabsize; /* Tab spacing */
    int indent; /* Current indent index */
    int[] indstack; /* Stack of indents */
    int atbol; /* Nonzero if at begin of new line */
    int pendin; /* Pending indents (if > 0) or dedents (if < 0) */
    int lineno; /* Current line number */
    int level; /* Parentheses nesting level */

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
    static TokenType PyToken_OneChar(char c) {
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


    static TokenType PyToken_TwoChars(char c1, char c2) {
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

    static TokenType PyToken_ThreeChars(int c1, int c2, int c3) {
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
