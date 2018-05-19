package org.python.parser;

/** Tokenizer.c */
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


}
