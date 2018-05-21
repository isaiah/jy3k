package org.python.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;

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

    public Grammar(DFA[] dfas, Label[] labels) {
        this.dfas = Arrays.asList(dfas);
        this.ll = Arrays.asList(labels);
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
        return ll.size() - 1;
    }

    static int findLabel(List<Label> ll, int type, String str) {
        for (int i = 0; i < ll.size(); i++) {
            Label label = ll.get(i);
            if (label.type == type && label.str.equals(str)) {
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
            return states.size();
        }

        public void addarc(int from, int to, int label) {
            State s = states.get(from);
            Arc arc = new Arc(label, to);
            s.arcs.add(arc);
        }
    }

    static class State {
        final List<Arc> arcs;

        private State() {
            arcs = new ArrayList<>();
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
        ll.forEach(this::translatelabel);
    }

    private void translatelabel(Label lb) {
        if (lb.type == NAME.ordinal()) {
            for (DFA dfa : dfas) {
                if (lb.str.equals(dfa.name)) {
                    lb.type = dfa.type;
                    lb.str = null;
                    return;
                }
            }
            for (int i = 0; i < TOKEN_NAMES.length; i++) {
                if (lb.str.equals(TOKEN_NAMES[i])) {
                    lb.type = i;
                    lb.str = null;
                    return;
                }
            }
            System.out.println(String.format("Cannot translate NAME label '%s'", lb.str));
            return;
        }
        if (lb.type == STRING.ordinal()) {
            if (Character.isAlphabetic(lb.str.charAt(1)) || lb.str.charAt(1) == '_') {
                /* Label is a keyword */
                lb.type = NAME.ordinal();
                String src = lb.str.substring(1);
                int p = src.indexOf('\'');
                int nameLen = p >= 0 ? p : src.length();
                lb.str = src;
            } else if (lb.str.charAt(2) == lb.str.charAt(0)) {
                TokenType type = PyToken_OneChar(lb.str.charAt(1));
                if (type != OP) {
                    lb.type = type.ordinal();
                    lb.str = null;
                } else {
                    System.out.println(String.format("Unknown OP label %s", lb.str));
                }
            } else if (lb.str.charAt(3) == lb.str.charAt(0)) { //&& lb.str[2]
                TokenType type = PyToken_TwoChars(lb.str.charAt(1), lb.str.charAt(2));
                if (type != OP) {
                    lb.type = type.ordinal();
                    lb.str = null;
                } else {
                    System.out.println(String.format("Unknown OP label %s", lb.str));
                }
            } else if (lb.str.charAt(4) == lb.str.charAt(0)) {
                TokenType type = PyToken_ThreeChars(lb.str.charAt(1), lb.str.charAt(2), lb.str.charAt(3));
                if (type != OP) {
                    lb.str = null;
                    lb.type = type.ordinal();
                } else {
                    System.out.println(String.format("Unknown OP label %s", lb.str));
                }
            } else {
                System.out.println(String.format("Cannot translate STRING label %s", lb.str));
            }
        } else {
            System.out.println(String.format("Cannot translate label '%s'", lb));
        }
    }

    /* Computation of FIRST sets */
    void addfirstsets() {
        for (DFA dfa : dfas) {
            if (dfa.first != null) {
                calcfirstset(dfa);
            }
        }
    }

    static BitSet DUMMY = new BitSet(1);

    void calcfirstset(DFA d) {
        d.first = DUMMY;
        Label l0 = ll.get(0);
        int nbits = ll.size();
        BitSet result = new BitSet(nbits);
        List<Integer> symbols = new ArrayList<>();
        symbols.add(findLabel(ll, d.type, null));
        State s = d.states.get(d.initial);
        for (int i = 0; i < s.arcs.size(); i++) {
            Arc a = s.arcs.get(i);
            int j = 0;
            for (; j < symbols.size(); j++) {
                if (symbols.get(j) == a.lbl) {
                    break;
                }
            }
            if (j >= symbols.size()) { /* New label */
                symbols.add((int) a.lbl);
                int type = ll.get(a.lbl).type;
                if (isNonTerminal(type)) {
                    DFA d1 = PyGrammar_FindDFA(type);
                    if (d1.first == DUMMY) {
                        System.err.println(String.format("Left-recursion below '%s'", d.name));
                    } else {
                        if (d1.first == null) {
                            calcfirstset(d1);
                        }
                        result.or(d1.first);
                    }

                } else { // ISTERMINAL
                    result.set(a.lbl);
                }
            }
        }
        d.first = result;
    }

    DFA PyGrammar_FindDFA(int type) {
        return dfas.get(type - NT_OFFSET);
    }

    boolean isTerminal(int type) {
        return type < NT_OFFSET;
    }
    boolean isNonTerminal(int type) {
        return type >= NT_OFFSET;
    }

    final List<DFA> dfas;
    List<Label> ll;
    int start; /* Start symbol of this grammar */
}
