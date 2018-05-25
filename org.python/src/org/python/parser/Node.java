package org.python.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Parse tree node
 */
public class Node implements Errcode {
    private int type;
    private String str;
    private int lineno;
    private int colOffset;
    private final List<Node> children;

    public Node(int type) {
        this.type = type;
        this.lineno = 0;
        this.children = new ArrayList<>();
    }

    public Node(int type, String str, int lineno, int colOffset) {
        this.type = type;
        this.str = str;
        this.lineno = lineno;
        this.colOffset = colOffset;
        this.children = new ArrayList<>();
    }

    public boolean addChildren(int type, String str, int lineno, int colOffset) {
        if (children.size() == Integer.MAX_VALUE) {
            return false;
        }
        Node n = new Node(type, str, lineno, colOffset);
        children.add(n);
        return true;
    }

    public int nch() {
        return children.size();
    }

    public Node nchild(int n) {
        return children.get(n);
    }

    public String str() {
        return str;
    }

    public int lineno() {
        return lineno;
    }

    public int type() {
        return type;
    }

    public Iterable<Node> children() {
        return children;
    }
}
