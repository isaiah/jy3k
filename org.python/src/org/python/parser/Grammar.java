package org.python.parser;

import java.util.List;

/* grammar.c */
public class Grammar {

    private static class DFA {
        int type;
        String name;
        int initial;
        State[] states;
        int first; /* bitset */
    }

    private static class State {
        private Arc[] arcs;
    }

    /* An arc from one state to another */
    private static class Arc {
        private short lbl; /* Label of this arc */
        private short arrow; /* State where this arc goes to */

    }

    static class Label {
        final TokenType type;
        final String str;

        public Label(TokenType type, String str) {
            this.type = type;
            this.str = str;
        }
    }

    DFA[] dfas;
    List<Label> ll;
    int start; /* Start symbol of this grammar */
}
