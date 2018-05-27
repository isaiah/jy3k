package org.python.parser;

import org.python.core.Py;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Logger;

import static org.python.parser.ParserGenerator.NT_OFFSET;
import static org.python.parser.ParserGenerator.PyGrammar_FindDFA;
import static org.python.parser.TokenType.NAME;

public class Parser implements Errcode {
    static Logger logger = Logger.getLogger(Parser.class.getName());

    private Deque<StackEntry> stack; /* Stack of parser states */
    private Grammar grammar; /* Grammar to use */
    Node tree; /* Top of parse tree */

    public Parser(Grammar g, int start) {
        if (!g.accel) {
            g.PyGrammar_AddAccelerators();
        }
        this.grammar = g;
        this.tree = new Node(start);
        this.stack = new ArrayDeque<>();

        pushstack(ParserGenerator.PyGrammar_FindDFA(g, start), tree);
    }

    private void popstack() {
        stack.pop();
    }

    private void pushstack(Grammar.DFA d, Node n) {
        stack.push(new StackEntry(d, n));
    }

    public static Node ParseFile(File fp, String filename, Grammar grammar, int start) {
        Tokenizer tok = null;
        try {
            tok = Tokenizer.fromFile(new FileReader(fp));
        } catch (FileNotFoundException e) {
            throw Py.JavaError(e);
        }
        return tok.parsetok(grammar, start);
    }

    public int addToken(int type, String str, int lineno, int colOffset) {
        /* Find out which label this token is */
        int ilabel = classify(type, str);
        if (ilabel < 0) {
            return E_SYNTAX;
        }
        /* Loop until the token is shifted or an error occurred */
        for (;;) {
            /* Fetch current DFA and state */
            Grammar.DFA dfa = top().dfa;
            Grammar.State s = dfa.states.get(top().state);

            logger.info(String.format(" DFA '%s', state '%d':", dfa.name, top().state));

            /* Check accelerator */
            if (s.lower <= ilabel && ilabel < s.upper) {
                int x = s.accel[ilabel - s.lower];
                if (x != -1) {
                    if ((x & (1 << 7)) > 0) {
                        /* Push non-terminal */
                        int nt = (x >> 8) + NT_OFFSET;
                        int arrow = x & ((1 << 7) - 1);
                        Grammar.DFA d1 = PyGrammar_FindDFA(grammar, nt);
                        push(nt, d1, arrow, lineno, colOffset);
                        logger.info("  Push ...");
                        continue;
                    }
                    /* Shift the token */
                    shift(type, str, x, lineno, colOffset);
                    logger.info(" Shift.");
                    /* Pop while we are in an accept-only state */
                    for (s = dfa.states.get(top().state); s.accept && s.arcs.size() == 1; s = dfa.states.get(top().state)) {
                        logger.info(String.format("  DFA '%s', state '%d': Direct pop.", dfa.name, top().state));
                        popstack();
                        if (stack.isEmpty()) {
                            logger.info("   ACCEPT");
                            return E_DONE;
                        }
                        dfa = top().dfa;
                    }
                    return E_OK;
                }
            }
            if (s.accept) {
                /* Pop this DFA and try again */
                popstack();
                logger.info(" Pop ...");
                if (stack.isEmpty()) {
                    logger.info(" Error: bottom of stack.");
                    return E_SYNTAX;
                }
                continue;
            }
            /* Stuck, report syntax error */
            // return the current type
            return E_SYNTAX;
        }
    }

    private StackEntry top() {
        return stack.peek();
    }

    private boolean shift(int type, String str, int newstate, int lineno, int colOffset) {
        stack.peek().parent.addChildren(type, str, lineno, colOffset);
        stack.peek().state = newstate;
        return true;
    }

    private boolean push(int type, Grammar.DFA d, int newstate, int lineno, int colOffset) {
        Node n = stack.peek().parent;
        n.addChildren(type, null, lineno, colOffset);
        stack.peek().state = newstate;
        pushstack(d, n.nchild(n.nch() - 1));
        return true;
    }

    /* PARSER PROPER */

    int classify(int type, String str) {
        Grammar g = grammar;
        int n = g.ll.size();
        int i = n;
        if (type == NAME) {
            for (Grammar.Label l : g.ll) {
                if (l.type != NAME || l.str == null || !l.str.equals(str)) {
                    i--;
                    continue;
                }

                logger.info("it's a keyword");
                return g.ll.size() - i;
            }
        }
        i = 0;
        for (Grammar.Label l : g.ll) {
            if (l.type == type && l.str == null) {
                System.out.println("it's a token we know");
                return i;
            }
            i++;
        }
        logger.info("Illegal token");
        return -1;
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
