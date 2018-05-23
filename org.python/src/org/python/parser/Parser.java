package org.python.parser;

import java.io.File;
import java.util.Deque;
import java.util.LinkedList;

public class Parser {

    private Deque<StackEntry> stack; /* Stack of parser states */
    private Grammar grammar; /* Grammar to use */
    private Node tree; /* Top of parse tree */

    public Parser(Grammar g, int start) {
        this.grammar = g;
        this.tree = new Node(start);
        this.stack = new LinkedList<>();

        pushStack(ParserGenerator.PyGrammar_FindDFA(g, start), tree);
    }

    private void pushStack(Grammar.DFA d, Node n) {
        stack.push(new StackEntry(d, n));
    }

    public static Node ParseFile(File fp, String filename, Grammar grammar, int start) {
        return null;
    }

    static class StackEntry {
        int state; /* State in current DFA */
        Grammar.DFA dfa; /* Current DFA */
        Node parent; /* Where to add next node */

        public StackEntry(Grammar.DFA d, Node n) {
            this.dfa = d;
            this.parent = n;
            this.state = 0;
        }
    }
}
