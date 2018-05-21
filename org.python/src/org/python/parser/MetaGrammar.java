package org.python.parser;

/* meta_grammar.c */
public class MetaGrammar {
    static Grammar.Arc[] arcs_0_0 = new Grammar.Arc[]{
            new Grammar.Arc(2, 0),
            new Grammar.Arc(3, 0),
            new Grammar.Arc(4, 1)
    };
    static Grammar.Arc[] arcs_0_1 = new Grammar.Arc[]{
            new Grammar.Arc(0, 1)
    };
    static Grammar.State[] states_0 = new Grammar.State[]{
            new Grammar.State(arcs_0_0),
            new Grammar.State(arcs_0_1)
    };
    static Grammar.Arc[] arcs_1_0 = {
            new Grammar.Arc(5, 1)
    };
    static Grammar.Arc[] arcs_1_1 = {
            new Grammar.Arc(6, 2)
    };
    static Grammar.Arc[] arcs_1_2 = {
            new Grammar.Arc(7, 3)
    };
    static Grammar.Arc[] arcs_1_3 = {
            new Grammar.Arc(3, 4)
    };
    static Grammar.Arc[] arcs_1_4 = {
            new Grammar.Arc(0, 4)
    };
    static Grammar.State[] states_1 = {
            new Grammar.State(arcs_1_0),
            new Grammar.State(arcs_1_1),
            new Grammar.State(arcs_1_2),
            new Grammar.State(arcs_1_3),
            new Grammar.State(arcs_1_4)
    };

    static Grammar.Arc[] arcs_2_0 = {
            new Grammar.Arc(8, 1)
    };
    static Grammar.Arc[] arcs_2_1 = {
            new Grammar.Arc(9, 0),
            new Grammar.Arc(0, 1)
    };
    static Grammar.State[] states_2 = {
            new Grammar.State(arcs_2_0),
            new Grammar.State(arcs_2_1),
    };
    static Grammar.Arc[] arcs_3_0 = {
            new Grammar.Arc(10, 1)
    };
    static Grammar.Arc[] arcs_3_1 = {
            new Grammar.Arc(10, 1),
            new Grammar.Arc(0, 1)
    };
    static Grammar.State[] states_3 = {
            new Grammar.State(arcs_3_0),
            new Grammar.State(arcs_3_1),
    };
    static Grammar.Arc[] arcs_4_0 = {
            new Grammar.Arc(11, 1),
            new Grammar.Arc(13, 2),
    };
    static Grammar.Arc[] arcs_4_1 = {
            new Grammar.Arc(7, 3),
    };
    static Grammar.Arc[] arcs_4_2 = {
            new Grammar.Arc(14, 4),
            new Grammar.Arc(15, 4),
            new Grammar.Arc(0, 2),
    };
    static Grammar.Arc[] arcs_4_3 = {
            new Grammar.Arc(12, 4),
    };
    static Grammar.Arc[] arcs_4_4 = {
            new Grammar.Arc(0, 4),
    };
    static Grammar.State[] states_4 = {
            new Grammar.State(arcs_4_0),
            new Grammar.State(arcs_4_1),
            new Grammar.State(arcs_4_2),
            new Grammar.State(arcs_4_3),
            new Grammar.State(arcs_4_4),
    };
    static Grammar.Arc[] arcs_5_0 = {
            new Grammar.Arc(5, 1),
            new Grammar.Arc(16, 1),
            new Grammar.Arc(17, 2),
    };
    static Grammar.Arc[] arcs_5_1 = {
            new Grammar.Arc(0, 1),
    };
    static Grammar.Arc[] arcs_5_2 = {
            new Grammar.Arc(7, 3),
    };
    static Grammar.Arc[] arcs_5_3 = {
            new Grammar.Arc(18, 1),
    };
    static Grammar.State[] states_5 = {
            new Grammar.State(arcs_5_0),
            new Grammar.State(arcs_5_1),
            new Grammar.State(arcs_5_2),
            new Grammar.State(arcs_5_3),
    };
    static Grammar.DFA[] dfas = {
            new Grammar.DFA(256, "MSTART", 0, states_0, "\070\000\000"),
            new Grammar.DFA(257, "RULE", 0, states_1, "\040\000\000"),
            new Grammar.DFA(258, "RHS", 0, states_2, "\040\010\003"),
            new Grammar.DFA(259, "ALT", 0, states_3, "\040\010\003"),
            new Grammar.DFA(260, "ITEM", 0, states_4, "\040\010\003"),
            new Grammar.DFA(261, "ATOM", 0, states_5, "\040\000\003"),
    };
    static Grammar.Label[] labels = new Grammar.Label[]{
            new Grammar.Label(0, "EMPTY"),
            new Grammar.Label(256, ""),
            new Grammar.Label(257, null),
            new Grammar.Label(4, null),
            new Grammar.Label(0, null),
            new Grammar.Label(1, null),
            new Grammar.Label(11, null),
            new Grammar.Label(258, null),
            new Grammar.Label(259, null),
            new Grammar.Label(18, null),
            new Grammar.Label(260, null),
            new Grammar.Label(9, null),
            new Grammar.Label(10, null),
            new Grammar.Label(261, null),
            new Grammar.Label(16, null),
            new Grammar.Label(14, null),
            new Grammar.Label(3, null),
            new Grammar.Label(7, null),
            new Grammar.Label(8, null)
    };
    final static Grammar PARSER_GRAMMAR = new Grammar(dfas, labels);
}
