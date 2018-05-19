package org.python.parser;

import org.python.antlr.ast.BinOp;

import javax.net.ssl.SSLProtocolException;

import static org.python.parser.TokenKind.BINARY;
import static org.python.parser.TokenKind.BRACKET;
import static org.python.parser.TokenKind.LITERAL;
import static org.python.parser.TokenKind.SPECIAL;

public enum TokenType {
    ENDMARKER(SPECIAL, null),
    NAME(LITERAL, null),
    NUMBER(LITERAL, null),
    STRING(LITERAL, null),
    NEWLINE(SPECIAL, null),
    INDENT(SPECIAL, null),
    DEDENT(SPECIAL, null),
    LPAR(BRACKET, "("),
    RPAR(BRACKET, ")"),
    LSQB(BRACKET, "["),
    RSQB(BRACKET, "]"),
    COLON(BINARY, ":"),
    COMMA(BINARY, ","),
    SEMI(BINARY, ";"),
    PLUS(BINARY, "+"),
    MINUS(BINARY, "-"),
    STAR(BINARY, "*"),
    SLASH(BINARY, "/"),
    VBAR(BINARY, "|"),
    AMPER(BINARY, "&"),
    LESS(BINARY, "<"),
    GREATER(BINARY, ">"),
    EQUAL(BINARY, "="),
    DOT(BRACKET, "."),
    PERCENT(BINARY, "%"),
    LBRACE(BRACKET, "{"),
    RBRACE(BRACKET, "}"),
    EQEQUAL(BINARY, "=="),
    NOTEQUAL(BINARY, "!="),
    LESSEQUAL(BINARY, "<="),
    GREATEREQUAL(BINARY, ">="),
    TILDE(BINARY, "~"),
    CIRCUMFLEX(BINARY, "^"),
    LEFTSHIFT(BINARY, "<<"),
    RIGHTSHIT(BINARY, ">>"),
    DOUBLESTART(BINARY, "**"),
    PLUSEQUAL(BINARY, "+="),
    MINUSEQUAL(BINARY, "-="),
    STARTEQUAL(BINARY, "*="),
    SLASHEQUAL(BINARY, "/="),
    PERCENTEQUAL(BINARY, "%="),
    AMPEREQUAL(BINARY, "&="),
    VBAREQUAL(BINARY, "|="),
    CIRCUMFLEXEQUAL(BINARY, "^="),
    LEFTSHIFTEQUAL(BINARY, "<<="),
    RIGHTSHIFTEQUAL(BINARY, ">>="),
    DOUBLESTAREQUAL(BINARY, "**="),
    DOUBLESLASH(BINARY, "//"),
    DOUBLESLASHEQUAL(BINARY, "//="),
    AT(BINARY, "@"),
    ATEQUAL(BINARY, "@="),
    RARROW(BINARY, "->"),
    ELLIPSIS(LITERAL, "..."),
    OP(SPECIAL, null),
    ERRORTOKEN(SPECIAL, null),
    COMMENT(SPECIAL, null),
    NL(SPECIAL, null),
    ENCODING(SPECIAL, null),
    N_TOKENS(SPECIAL, null),
    /* Meta Grammar */
    MSTART(SPECIAL, null),
    RULE(SPECIAL, null),
    RHS(SPECIAL, null),
    ALT(SPECIAL, null),
    ITEM(SPECIAL, null),
    ATOM(SPECIAL, null)
    ;
    /**
#define ENDMARKER       0
#define NAME            1
#define NUMBER          2
#define STRING          3
#define NEWLINE         4
#define INDENT          5
#define DEDENT          6
#define LPAR            7
#define RPAR            8
#define LSQB            9
#define RSQB            10
#define COLON           11
#define COMMA           12
#define SEMI            13
#define PLUS            14
#define MINUS           15
#define STAR            16
#define SLASH           17
#define VBAR            18
#define AMPER           19
#define LESS            20
#define GREATER         21
#define EQUAL           22
#define DOT             23
#define PERCENT         24
#define LBRACE          25
#define RBRACE          26
#define EQEQUAL         27
#define NOTEQUAL        28
#define LESSEQUAL       29
#define GREATEREQUAL    30
#define TILDE           31
#define CIRCUMFLEX      32
#define LEFTSHIFT       33
#define RIGHTSHIFT      34
#define DOUBLESTAR      35
#define PLUSEQUAL       36
#define MINEQUAL        37
#define STAREQUAL       38
#define SLASHEQUAL      39
#define PERCENTEQUAL    40
#define AMPEREQUAL      41
#define VBAREQUAL       42
#define CIRCUMFLEXEQUAL 43
#define LEFTSHIFTEQUAL  44
#define RIGHTSHIFTEQUAL 45
#define DOUBLESTAREQUAL 46
#define DOUBLESLASH     47
#define DOUBLESLASHEQUAL 48
#define AT              49
#define ATEQUAL         50
#define RARROW          51
#define ELLIPSIS        52
#define OP              53
#define ERRORTOKEN      54
#define COMMENT         55
#define NL              56
#define ENCODING                57
#define N_TOKENS        58

     */
    TokenType(TokenKind kind, String name) {
        this.kind = kind;
        this.name = name;
    }

    private TokenKind kind;
    private String name;
}
