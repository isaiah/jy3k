/* Parser generator main program */

/* This expects a filename containing the grammar as argv[1] (UNIX)
   or asks the console for such a file name (THINK C).
   It writes its output on two files in the current directory:
   - "graminit.c" gets the grammar as a bunch of initialized data
   - "graminit.h" gets the grammar's non-terminals as #defines.
   Error messages and status info during the generation process are
   written to stdout, or sometimes to stderr. */

/* XXX TO DO:
   - check for duplicate definitions of names (instead of fatal err)
*/
package org.python.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntUnaryOperator;

import static org.python.parser.TokenType.*;

/* pgen.c */
public class ParserGenerator {
    static final int NT_OFFSET = 256;
    static final int EMPTY = 0;

    public static void main(String[] args) {
        String filename = args[0];
        String grammarinit = args[1];
        Grammar g = openGrammar(filename);
        File fp = new File(grammarinit);
        printGrammar(g, fp);
    }

    private static Grammar openGrammar(String filename) {
        File fp = new File(filename);
        Grammar g0 = MetaGrammar.PARSER_GRAMMAR;
        Node n = Parser.ParseFile(fp, filename, g0, g0.start);
        return null;
    }

    private static void printGrammar(Grammar g, File fp) {
    }

    private static class NFAArc {
        int label;
        int arrow;

        public NFAArc(int label, int arrow) {
            this.label = label;
            this.arrow = arrow;
        }
    }

    private static class NFAState {
        final List<NFAArc> nfaArcs;

        private NFAState() {
            nfaArcs = new ArrayList<>();
        }

        void addArc(int to, int label) {
            NFAArc arc = new NFAArc(to, label);
            nfaArcs.add(arc);
        }

    }

    private static class NFA {
        int type;
        final String name;
        final List<NFAState> states;
        int start, finish;

        private NFA(String name) {
            this.name = name;
            this.start = this.finish = -1;
            this.type = NT_OFFSET+1;
            states = new ArrayList<>();
        }

        int addState() {
            states.add(new NFAState());
            return states.size() - 1;
        }

        void addNFAArc(int from, int to, int label) {
            states.get(from).addArc(to, label);
        }
    }

    private static class NFAGrammar {
        final List<NFA> nfas;
        final List<Grammar.Label> ll;

        private NFAGrammar() {
            nfas = new ArrayList<>();
            ll = new ArrayList<>();
            addLabel(ENDMARKER, "EMPTY");
        }

        int addLabel(TokenType type, String str) {
            return ParserGenerator.addLabel(ll, type, str);
        }

        NFA addNFA(String name) {
            NFA nf = new NFA(name);
            nfas.add(nf);
            addLabel(NAME, name);
            return nf;
        }

        void compileRule(Node n) {
            REQ(n, RULE);
            REQN(n.nch(), 4);
            int cur = 0;
            Node node = n.nchild(cur++);
            REQ(node, NAME);
            NFA nf = addNFA(node.str());
            node = n.nchild(cur++);
            REQ(node, COLON);
            node = n.nchild(cur++);
            REQ(node, RHS);
            IntTuple tuple = new IntTuple(nf.start, nf.finish);
            compileRHS(ll, nf, node, tuple);
            /* Value is modified in compileRHS */
            /* This is translated from C, hopefully we can refactor in the end */
            nf.start = tuple.a;
            nf.finish = tuple.b;
        }

        void compileRHS(List<Grammar.Label> ll, NFA nf, Node n, IntTuple p) {
            REQ(n, RHS);
            int i = n.nch();
            REQN(i, 1);
            int cur = 0;
            Node node = n.nchild(cur++);
            REQ(node, ALT);
            compileAlt(ll, nf, node, p);
            int a = p.a;
            int b = p.b;
            p.a = nf.addState();
            p.b = nf.addState();
            nf.addNFAArc(a, p.a, EMPTY);
            nf.addNFAArc(b, p.b, EMPTY);
            while (cur < nf.states.size()) {
                node = n.nchild(cur++);
                REQ(node, VBAR);

                node = n.nchild(cur++);
                REQ(node, ALT);
                IntTuple p1 = new IntTuple(a, b);
                compileAlt(ll, nf, node, p1);
                nf.addNFAArc(p.a, p1.a, EMPTY);
                nf.addNFAArc(p.b, p1.b, EMPTY);
            }
        }

        void compileAlt(List<Grammar.Label> ll, NFA nf, Node n, IntTuple p) {
            REQ(n, ALT);
            REQN(n.nch(), 1);
            int cur = 0;
            Node node = n.nchild(cur++);
            REQ(node, ITEM);
            compileItem(ll, nf, n, p);
            while (cur < nf.states.size()) {
                node = n.nchild(cur++);
                REQ(node, ITEM);
                IntTuple p1 = new IntTuple();
                compileItem(ll, nf, node, p1);
                nf.addNFAArc(p.b, p1.a, EMPTY);
                p.b = p1.b;
            }
        }

        void compileItem(List<Grammar.Label> ll, NFA nf, Node n, IntTuple p) {
            REQ(n, ITEM);
            REQN(n.nch(), 1);
            int cur = 0;
            Node node = n.nchild(cur++);
            IntTuple p1 = new IntTuple();
            if (n.type() == LSQB) {
                REQN(n.nch(), 3);
                node = n.nchild(cur++);
                REQ(node, RHS);
                p.a = nf.addState();;
                p.b = nf.addState();;
                nf.addNFAArc(p.a, p.b, EMPTY);
                compileRHS(ll, nf, node, p1);
                nf.addNFAArc(p.a, p1.a, EMPTY);
                nf.addNFAArc(p1.b, p.b, EMPTY);
                node = n.nchild(cur++);
                REQ(node, RSQB);
            } else {
                compileAtom(ll, nf, node, p);
                if (cur >= n.nch()) {
                    return;
                }
                node = n.nchild(cur++);
                nf.addNFAArc(p.b, p.a, EMPTY);
                if (n.type() == STAR) {
                    p.b = p.a;
                } else {
                    REQ(node, PLUS);
                }
            }
        }

        void compileAtom(List<Grammar.Label> ll, NFA nf, Node n, IntTuple p) {
            REQN(n.nch(), 1);
            int cur = 0;
            Node node = n.nchild(cur++);
            if (n.type() == LPAR) {
                REQN(n.nch(), 3);
                node = n.nchild(cur++);
                REQ(node, RHS);
                compileRHS(ll, nf, node, p);
                node = n.nchild(cur++);
                REQ(node, RPAR);
            } if (n.type() == NAME || n.type() == STRING) {
                p.a = nf.addState();
                p.b = nf.addState();
                nf.addNFAArc(p.a, p.b, addLabel(n.type(), n.str()));
            } else {
                REQ(node, NAME);
            }
        }

        /** Fake C macros */
        void REQ(Node n, TokenType type) {
            assert n.type() == type;
        }
        void REQN(int n, int count) {
            assert n > count: String.format("metacompile: less than %d children", count);
        }
    }

    /* This acts like the int *pa, int *pb in C, it passes parameter, and receives return value*/
    static class IntTuple {
        int a, b;
        public IntTuple() {
            this.a = this.b = 0;
        }

        public IntTuple(int a, int b) {
            this.a = a;
            this.b = b;
        }
    }

    static NFAGrammar metacompile(Node n) {
        NFAGrammar gr = new NFAGrammar();
        for (Node node : n.children()) {
            if (node.type() != NEWLINE) {
                gr.compileRule(node);
            }
        }
        return gr;
    }

    static int addLabel(List<Grammar.Label> ll, TokenType type, String str) {
        for (int i = 0; i < ll.size(); i++) {
            Grammar.Label label = ll.get(i);
            if (label.type == type && label.str.equals(str)) {
                return i;
            }
        }

        Grammar.Label label = new Grammar.Label(type, str);
        ll.add(label);
        return ll.size() - 1;
    }

    static Grammar.Label findLabel(List<Grammar.Label> ll, TokenType type, String str) {
        Optional<Grammar.Label> l = ll.stream().filter(label -> label.type == type && label.str.equals(str)).findFirst();
        if (l.isPresent()) {
            return l.get();
        }
        System.err.println(String.format("Label %d/'%s' not found", type, str));
        throw new RuntimeException("grammar.c: findlabel()");
    }
}
