package org.python.parser;

public interface Errcode {
    /* Error codes passed around between file input, tokenizer, parser and
   interpreter.  This is necessary so we can turn them into Python
   exceptions at a higher level.  Note that some errors have a
   slightly different meaning when passed from the tokenizer to the
   parser than when passed from the parser to the interpreter; e.g.
   the parser only returns E_EOF when it hits EOF immediately, and it
   never returns E_OK. */

    static final int E_OK            = 10;      /* No error */
    static final int E_EOF           = 11;      /* End Of File */
    static final int E_INTR          = 12;      /* Interrupted */
    static final int E_TOKEN         = 13;      /* Bad token */
    static final int E_SYNTAX        = 14;      /* Syntax error */
    static final int E_NOMEM         = 15;      /* Ran out of memory */
    static final int E_DONE          = 16;      /* Parsing complete */
    static final int E_ERROR         = 17;      /* Execution error */
    static final int E_TABSPACE      = 18;      /* Inconsistent mixing of tabs and spaces */
    static final int E_OVERFLOW      = 19;      /* Node had too many children */
    static final int E_TOODEEP       = 20;      /* Too many indentation levels */
    static final int E_DEDENT        = 21;      /* No matching outer block for dedent */
    static final int E_DECODE        = 22;      /* Error in decoding into Unicode */
    static final int E_EOFS          = 23;      /* EOF in triple-quoted string */
    static final int E_EOLS          = 24;      /* EOL in single-quoted string */
    static final int E_LINECONT      = 25;      /* Unexpected characters after a line continuation */
    static final int E_IDENTIFIER    = 26;      /* Invalid characters in identifier */
    static final int E_BADSINGLE     = 27;      /* Ill-formed single statement input */


}
