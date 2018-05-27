package org.python.parser;

import static org.python.parser.TokenKind.BINARY;
import static org.python.parser.TokenKind.BRACKET;
import static org.python.parser.TokenKind.LITERAL;
import static org.python.parser.TokenKind.SPECIAL;

public class TokenType {
    //ENDMARKER(SPECIAL, null),
    //NAME(LITERAL, null),
    //NUMBER(LITERAL, null),
    //STRING(LITERAL, null),
    //NEWLINE(SPECIAL, null),
    //INDENT(SPECIAL, null),
    //DEDENT(SPECIAL, null),
    //LPAR(BRACKET, "("),
    //RPAR(BRACKET, ")"),
    //LSQB(BRACKET, "["),
    //RSQB(BRACKET, "]"),
    //COLON(BINARY, ":"),
    //COMMA(BINARY, ","),
    //SEMI(BINARY, ";"),
    //PLUS(BINARY, "+"),
    //MINUS(BINARY, "-"),
    //STAR(BINARY, "*"),
    //SLASH(BINARY, "/"),
    //VBAR(BINARY, "|"),
    //AMPER(BINARY, "&"),
    //LESS(BINARY, "<"),
    //GREATER(BINARY, ">"),
    //EQUAL(BINARY, "="),
    //DOT(BRACKET, "."),
    //PERCENT(BINARY, "%"),
    //LBRACE(BRACKET, "{"),
    //RBRACE(BRACKET, "}"),
    //EQEQUAL(BINARY, "=="),
    //NOTEQUAL(BINARY, "!="),
    //LESSEQUAL(BINARY, "<="),
    //GREATEREQUAL(BINARY, ">="),
    //TILDE(BINARY, "~"),
    //CIRCUMFLEX(BINARY, "^"),
    //LEFTSHIFT(BINARY, "<<"),
    //RIGHTSHIFT(BINARY, ">>"),
    //DOUBLESTAR(BINARY, "**"),
    //PLUSEQUAL(BINARY, "+="),
    //MINEQUAL(BINARY, "-="),
    //STAREQUAL(BINARY, "*="),
    //SLASHEQUAL(BINARY, "/="),
    //PERCENTEQUAL(BINARY, "%="),
    //AMPEREQUAL(BINARY, "&="),
    //VBAREQUAL(BINARY, "|="),
    //CIRCUMFLEXEQUAL(BINARY, "^="),
    //LEFTSHIFTEQUAL(BINARY, "<<="),
    //RIGHTSHIFTEQUAL(BINARY, ">>="),
    //DOUBLESTAREQUAL(BINARY, "**="),
    //DOUBLESLASH(BINARY, "//"),
    //DOUBLESLASHEQUAL(BINARY, "//="),
    //AT(BINARY, "@"),
    //ATEQUAL(BINARY, "@="),
    //RARROW(BINARY, "->"),
    //ELLIPSIS(LITERAL, "..."),
    //OP(SPECIAL, null),
    //ERRORTOKEN(SPECIAL, null),
    //COMMENT(SPECIAL, null),
    //NL(SPECIAL, null),
    //ENCODING(SPECIAL, null),
    //N_TOKENS(SPECIAL, null),
    ///* Meta Grammar */
    //MSTART(SPECIAL, null),
    //RULE(SPECIAL, null),
    //RHS(SPECIAL, null),
    //ALT(SPECIAL, null),
    //ITEM(SPECIAL, null),
    //ATOM(SPECIAL, null)
    //;
    public static final int ENDMARKER = 0;
    public static final int NAME = 1;
    public static final int NUMBER = 2;
    public static final int STRING = 3;
    public static final int NEWLINE = 4;
    public static final int INDENT = 5;
    public static final int DEDENT = 6;
    public static final int LPAR = 7;
    public static final int RPAR = 8;
    public static final int LSQB = 9;
    public static final int RSQB = 10;
    public static final int COLON = 11;
    public static final int COMMA = 12;
    public static final int SEMI = 13;
    public static final int PLUS = 14;
    public static final int MINUS = 15;
    public static final int STAR = 16;
    public static final int SLASH = 17;
    public static final int VBAR = 18;
    public static final int AMPER = 19;
    public static final int LESS = 20;
    public static final int GREATER = 21;
    public static final int EQUAL = 22;
    public static final int DOT = 23;
    public static final int PERCENT = 24;
    public static final int LBRACE = 25;
    public static final int RBRACE = 26;
    public static final int EQEQUAL = 27;
    public static final int NOTEQUAL = 28;
    public static final int LESSEQUAL = 29;
    public static final int GREATEREQUAL = 30;
    public static final int TILDE = 31;
    public static final int CIRCUMFLEX = 32;
    public static final int LEFTSHIFT = 33;
    public static final int RIGHTSHIFT = 34;
    public static final int DOUBLESTAR = 35;
    public static final int PLUSEQUAL = 36;
    public static final int MINEQUAL = 37;
    public static final int STAREQUAL = 38;
    public static final int SLASHEQUAL = 39;
    public static final int PERCENTEQUAL = 40;
    public static final int AMPEREQUAL = 41;
    public static final int VBAREQUAL = 42;
    public static final int CIRCUMFLEXEQUAL = 43;
    public static final int LEFTSHIFTEQUAL = 44;
    public static final int RIGHTSHIFTEQUAL = 45;
    public static final int DOUBLESTAREQUAL = 46;
    public static final int DOUBLESLASH = 47;
    public static final int DOUBLESLASHEQUAL = 48;
    public static final int AT = 49;
    public static final int ATEQUAL = 50;
    public static final int RARROW = 51;
    public static final int ELLIPSIS = 52;
    public static final int OP = 53;
    public static final int ERRORTOKEN = 54;
    public static final int COMMENT = 55;
    public static final int NL = 56;
    public static final int ENCODING = 57;
    public static final int N_TOKENS = 58;
    public static final int MSTART = 256;
    public static final int RULE = 257;
    public static final int RHS = 258;
    public static final int ALT = 259;
    public static final int ITEM = 260;
    public static final int ATOM = 261;

    TokenType(TokenKind kind, String name) {
        this.kind = kind;
        this.name = name;
    }

    private TokenKind kind;
    private String name;
}
