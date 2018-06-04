package org.python.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

import static org.python.parser.ParserGenerator.DEBUG;
import static org.python.parser.ParserGenerator.EMPTY;
import static org.python.parser.ParserGenerator.PyGrammar_LabelRepr;
import static org.python.parser.TokenType.NAME;
import static org.python.parser.TokenType.OP;
import static org.python.parser.TokenType.STRING;
import static org.python.parser.Tokenizer.PyToken_OneChar;
import static org.python.parser.Tokenizer.PyToken_ThreeChars;
import static org.python.parser.Tokenizer.PyToken_TwoChars;
import static org.python.parser.Tokenizer.TOKEN_NAMES;

/* grammar.c */
public class Grammar {
    static final int NT_OFFSET = 256;

    public Grammar(int start) {
        this.start = start;
        dfas = new ArrayList<>();
    }

    public Grammar(DFA[] dfas, Label[] labels, int start) {
        this.dfas = Arrays.asList(dfas);
        this.ll = Arrays.asList(labels);
        this.start = start;
    }

    static int addLabel(List<Label> ll, int type, String str) {
        for (int i = 0; i < ll.size(); i++) {
            Label label = ll.get(i);
            if (label.type == type && label.str.equals(str)) {
                return i;
            }
        }

        Label label = new Label(type, str);
        ll.add(label);
        if (DEBUG) {
            System.out.printf("Label @ %8X, %d: %s\n", Objects.hashCode(ll), ll.size(), PyGrammar_LabelRepr(label));
        }
        return ll.size() - 1;
    }

    static int findLabel(List<Label> ll, int type, String str) {
        for (int i = 0; i < ll.size(); i++) {
            Label label = ll.get(i);
            if (label.type == type) { //&& label.str.equals(str)) {
                return i;
            }
        }

        System.err.println(String.format("Label %d/'%s' not found", type, str));
        throw new RuntimeException("grammar.c: findlabel()");
    }

    public static class DFA {
        int type;
        String name;
        int initial;
        final List<State> states;
        BitSet first;

        public DFA(int type, final String name) {
            this.type = type;
            this.name = name;
            states = new ArrayList<>();
        }

        public DFA(int type, final String name, int initial, State[] states, String first) {
            this.type = type;
            this.name = name;
            this.initial = initial;
            this.states = Arrays.asList(states);
            this.first = BitSet.valueOf(first.getBytes());
        }


        public int addstate() {
            states.add(new State());
//            return states.size() - 1;
            return states.size();
        }

        public void addarc(int from, int to, int label) {
            State s = states.get(from - 1);
            Arc arc = new Arc(label, to);
            s.arcs.add(arc);
        }
    }

    static class State {
        final List<Arc> arcs;

        /* Optional accelerator */
        int lower; /* Lowest label index */
        int upper; /* Highest label index */
        int[] accel; /* Accelerator */
        boolean accept; /* accepting state */

        private State() {
            arcs = new ArrayList<>();
            this.lower = this.upper = 0;
            this.accept = false;
        }

        public State(Arc[] arcs) {
            this.arcs = Arrays.asList(arcs);
        }
    }

    /* An arc from one state to another */
    static class Arc {
        int lbl; /* Label of this arc */
        int arrow; /* State where this arc goes to */

        public Arc(int label, int arrow) {
            this.lbl = label;
            this.arrow = arrow;
        }
    }

    static class Label {
        int type;
        String str;

        public Label(int type, String str) {
            this.type = type;
            this.str = str;
        }
    }

    public DFA adddfa(int type, final String name) {
        DFA d = new DFA(type, name);
        dfas.add(d);
        return d;
    }

    public void translatelabels() {
        ll.stream().skip(1).forEach(this::translatelabel);
    }

    private void translatelabel(Label lb) {
        if (DEBUG) {
            System.out.printf("Translating label %s ...\n", PyGrammar_LabelRepr(lb));
        }
        if (lb.type == NAME) {
            for (DFA dfa : dfas) {
                if (lb.str.equals(dfa.name)) {
                    if (DEBUG) {
                        System.out.printf("Label %s is non-terminal %d.\n", lb.str, dfa.type);
                    }
                    lb.type = dfa.type;
                    lb.str = null;
                    return;
                }
            }
            for (int i = 0; i < TOKEN_NAMES.length; i++) {
                if (lb.str.equals(TOKEN_NAMES[i])) {
                    if (DEBUG) {
                        System.out.printf("Label %s is terminal %d.\n", lb.str, i);
                    }
                    lb.type = i;
                    lb.str = null;
                    return;
                }
            }
            System.out.println(String.format("Cannot translate NAME label '%s'", lb.str));
            return;
        }
        if (lb.type == STRING) {
            if (Character.isAlphabetic(lb.str.charAt(1)) || lb.str.charAt(1) == '_') {
                /* Label is a keyword */
                if (DEBUG) {
                    System.out.printf("Label %s is a keyword\n", lb.str);
                }
                lb.type = NAME;
                String src = lb.str.substring(1);
                int p = src.indexOf('\'');
                int nameLen = p >= 0 ? p : src.length();
                lb.str = src.substring(0, nameLen);
            } else if (lb.str.charAt(2) == lb.str.charAt(0)) {
                int type = PyToken_OneChar(lb.str.charAt(1));
                if (type != OP) {
                    lb.type = type;
                    lb.str = null;
                } else {
                    System.out.println(String.format("Unknown OP label %s", lb.str));
                }
            } else if (lb.str.charAt(3) == lb.str.charAt(0)) { //&& lb.str[2]
                int type = PyToken_TwoChars(lb.str.charAt(1), lb.str.charAt(2));
                if (type != OP) {
                    lb.type = type;
                    lb.str = null;
                } else {
                    System.out.println(String.format("Unknown OP label %s", lb.str));
                }
            } else if (lb.str.charAt(4) == lb.str.charAt(0)) {
                int type = PyToken_ThreeChars(lb.str.charAt(1), lb.str.charAt(2), lb.str.charAt(3));
                if (type != OP) {
                    lb.str = null;
                    lb.type = type;
                } else {
                    System.out.println(String.format("Unknown OP label %s", lb.str));
                }
            } else {
                System.out.println(String.format("Cannot translate STRING label %s", lb.str));
            }
        } else {
            System.out.println(String.format("Cannot translate label '%s'", lb.str));
        }
    }

    /* Computation of FIRST sets */
    void addfirstsets() {
        if (DEBUG) {
            System.out.println("Adding FIRST sets ...");
        }
        for (DFA dfa : dfas) {
            if (dfa.first == null) {
                calcfirstset(dfa);
            }
        }
    }

    static final BitSet DUMMY = new BitSet(1);

    void calcfirstset(DFA d) {
        int i, j;

        DFA d1;
        Arc a;

        // if DEBUG
        System.out.printf("Calculate FIRST set for '%s'\n", d.name);
        if (d.first == DUMMY) {
            System.err.printf("Left-recursion for '%s'\n", d.name);
            return;
        }
        if (d.first != null) {
            System.err.printf("Re-calculating FIRST set for '%s' ???\n", d.name);
        }
        d.first = DUMMY;

        int nbits = ll.size();
        BitSet result = new BitSet(nbits);
        List<Integer> symbols = new ArrayList<>();
        symbols.add(findLabel(ll, d.type, null));

        State s = d.states.get(d.initial);
        for (i = 0; i < s.arcs.size(); i++) {
            a = s.arcs.get(i);
            for (j = 0; j < symbols.size(); j++) {
                if (symbols.get(j) == a.lbl) {
                    break;
                }
            }
            if (j >= symbols.size()) { /* New label */
                symbols.add(a.lbl);
                int type = ll.get(a.lbl).type;
                if (isNonTerminal(type)) {
                    d1 = PyGrammar_FindDFA(type);
                    if (d1.first == DUMMY) {
                        System.err.println(String.format("Left-recursion below '%s'", d.name));
                    } else {
                        if (d1.first == null) {
                            calcfirstset(d1);
                        }
                        result.or(d1.first);
                    }

                } else if (isTerminal(type)) {
                    result.set(a.lbl);
                }
            }
        }
        d.first = result;
        // if DEBUG
        System.out.printf("FIRST set for '%s': {", d.name);
        for (i = 0; i < nbits; i++) {
            if (result.get(i)) {
                System.out.printf(" %s", PyGrammar_LabelRepr(ll.get(i)));
            }
        }
        System.out.println(" }");
    }

    DFA PyGrammar_FindDFA(int type) {
        return dfas.get(type - NT_OFFSET);
    }

    static boolean isTerminal(int type) {
        return type < NT_OFFSET;
    }
    static boolean isNonTerminal(int type) {
        return type >= NT_OFFSET;
    }

    void PyGrammar_AddAccelerators() {
        dfas.forEach(this::fixdfa);
        accel = true;
    }

    private void fixdfa(DFA d) {
        d.states.forEach(this::fixstate);
    }

    private void fixstate(State s) {
        int nl = ll.size();
        int[] accel = new int[nl];
        Arrays.fill(accel, -1);
        for (Arc a : s.arcs) {
            int lbl = a.lbl;
            Label l = ll.get(lbl);
            int type = l.type;
            if (a.arrow >= (1 << 7)) {
                System.out.println("XXX too many states!\n");
                continue;
            }
            if (isNonTerminal(type)) {
                DFA d1 = PyGrammar_FindDFA(type);
                if (type - NT_OFFSET >= (1 << 7)) {
                    System.out.println("XXX too high nonterminal number!");
                    continue;
                }
                for (int ibit = 0; ibit < nl; ibit++) {
                    if (d1.first.get(ibit)) {
                        if (accel[ibit] != -1) {
                            System.out.println("XXX ambiguity!");
                        }
                        accel[ibit] = a.arrow | (1 << 7) | ((type - NT_OFFSET) << 8);
                    }
                }
            } else if (lbl == EMPTY) {
                s.accept = true;
            } else if (lbl >= 0 && lbl < nl) {
                accel[lbl] = a.arrow;
            }
        }
        while (nl > 0 && accel[nl - 1] == -1) {
            nl--;
        }
        int k = 0;
        while ( k < nl && accel[k] == -1) {
            k++;
        }

        if (k < nl) {
            s.accel = new int[nl - k];
            s.lower = k;
            s.upper = nl;
            for (int i = 0; k < nl; i++, k++) {
                s.accel[i] = accel[k];
            }
        }
    }

    final List<DFA> dfas;
    List<Label> ll;
    public int start; /* Start symbol of this grammar */
    boolean accel; /* Set if accelerators present */
}
