package org.python.core;

import org.python.antlr.ast.Await;
import org.python.antlr.ast.BinOp;
import org.python.antlr.ast.BoolOp;
import org.python.antlr.ast.Compare;
import org.python.antlr.ast.Expr;
import org.python.antlr.ast.IfExp;
import org.python.antlr.ast.Lambda;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.NameConstant;
import org.python.antlr.ast.Pass;
import org.python.antlr.ast.Starred;
import org.python.antlr.ast.Tuple;
import org.python.antlr.ast.UnaryOp;
import org.python.antlr.ast.Yield;
import org.python.antlr.ast.YieldFrom;
import org.python.antlr.ast.arg;
import org.python.antlr.ast.arguments;
import org.python.antlr.ast.boolopType;
import org.python.antlr.ast.cmpopType;
import org.python.antlr.ast.expr_contextType;
import org.python.antlr.ast.operatorType;
import org.python.antlr.ast.unaryopType;
import org.python.antlr.base.expr;
import org.python.antlr.base.mod;
import org.python.antlr.base.stmt;
import org.python.antlr.op.Load;
import org.python.parser.Node;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.IntStream;

import static org.python.parser.GramInit.*;
import static org.python.parser.ParserGenerator.REQ;
import static org.python.parser.TokenType.*;

/* Python/ast.c */
public class Ast {

    /**
     * Transform the CST rooted at node * to the appropriate AST
     * @param n
     * @param flags
     * @param filename
     * @return
     */
    mod PyAST_FromNodeObject(final Node n, EnumSet flags, String filename) {
        compiling c = new compiling(filename);
        mod res = null;
        int i, j, k, num;
        k = 0;
        List<stmt> stmts;
        stmt s;
        Node ch;

        switch(n.type()) {
            case FILE_INPUT:
                stmts = new ArrayList<>(num_stmts(n));
                for (i = 0; i < n.nch(); i++) {
                    ch = n.child(i);
                    if (ch.type() == NEWLINE) {
                        continue;
                    }
                    REQ(ch, STMT);
                    num = num_stmts(ch);
                    if (num == 1) {
                        s = ast_for_stmt(c, ch);
                    }
                }

        }
        return null;
    }

    /* num_stmts() returns number of contained statements.

       Use this routine to determine how big a sequence is needed for
       the statements in a parse tree.  Its raison d'etre is this bit of
       grammar:

       stmt: simple_stmt | compound_stmt
       simple_stmt: small_stmt (';' small_stmt)* [';'] NEWLINE

       A simple_stmt can contain multiple small_stmt elements joined
       by semicolons.  If the arg is a simple_stmt, the number of
       small_stmt elements is returned.
    */
    static int num_stmts(final Node n) {
        int i, l;
        switch(n.type()) {
            case SINGLE_INPUT:
                if (n.child(0).type() == NEWLINE) {
                    return 0;
                }
                return num_stmts(n.child(0));
            case FILE_INPUT:
                l = 0;
                for (i = 0; i < n.nch(); i++) {
                    Node ch = n.child(i);
                    if (ch.type() == STMT) {
                        l += num_stmts(ch);
                    }
                }
                return l;
            case STMT:
                return num_stmts(n.child(0));
            case COMPOUND_STMT:
                return 1;
            case SIMPLE_STMT:
                return n.nch() / 2; /* Divide by 2 to remove count of semi-colons */
            case SUITE:
                if (n.nch() == 1) {
                    return num_stmts(n.child(0));
                }
                l = 0;
                for (i = 2; i < n.nch() - 1; i++) {
                    l+= num_stmts(n.child(i));
                }
                return l;
            default:
                throw new RuntimeException(String.format("Non-statment found: %d %d", n.type(), n.nch()));
        }
    }

    static stmt ast_for_stmt(compiling c, Node n) {
        if (n.type() == STMT) {
            assert n.nch() == 1;
            n = n.child(0);
        }
        if (n.type() == SIMPLE_STMT) {
            assert num_stmts(n) == 1;
            n = n.child(0);
        }
        if (n.type() == SMALL_STMT) {
            n = n.child(0);
            /* small_stmt: expr_stmt | del_stmt | pass_stmt | flow_stmt
                      | import_stmt | global_stmt | nonlocal_stmt | assert_stmt
            */
            switch (n.type()) {
                case EXPR_STMT:
                    return ast_for_expr_stmt(c, n);
                case DEL_STMT:
                    return ast_for_del_stmt(c, n);
                case PASS_STMT:
                    return new Pass(n.lineno(), n.coloffset());
                case FLOW_STMT:
                    return ast_for_flow_stmt(c, n);
                case IMPORT_STMT:
                    return ast_for_import_stmt(c, n);
                case GLOBAL_STMT:
                    return ast_for_global_stmt(c, n);
                case NONLOCAL_STMT:
                    return ast_for_nonlocal_stmt(c, n);
                case ASSERT_STMT:
                    return ast_for_assert_stmt(c, n);
                default:
                    throw Py.SystemError(String.format("unhanlded small_stmt: TYPE=%d, NCH=%d\n", n.type(), n.nch()));
            }
        }
        /* compound_stmt: if_stmt | while_stmt | for_stmt | try_stmt
                        | funcdef | classdef | decorated | async_stmt
        */
        Node ch = n.child(0);
        REQ(n, COMPOUND_STMT);
        switch(ch.type()) {
            case IF_STMT:
                return ast_for_if_stmt(c, ch);
            case WHILE_STMT:
                return ast_for_while_stmt(c, ch);
            case FOR_STMT:
                return ast_for_for_stmt(c, ch, false);
            case TRY_STMT:
                return ast_for_try_stmt(c, ch);
            case WITH_STMT:
                return ast_for_with_stmt(c, ch, false);
            case FUNCDEF:
                return ast_for_funcdef(c, ch, null);
            case CLASSDEF:
                return ast_for_classdef(c, ch, null);
            case DECORATED:
                return ast_for_decorated(c, ch);
            case ASYNC_STMT:
                return ast_for_async_stmt(c, ch);
            default:
                throw Py.SystemError(String.format("unhandled compound_stmt: TYPE=%d NCH=%d\n", n.type(), n.nch()));
        }
    }

    /* Create AST for argument list. */

    static arguments ast_for_arguments(compiling c, Node n) {
         /* This function handles both typedargslist (function definition)
           and varargslist (lambda definition).

           parameters: '(' [typedargslist] ')'
           typedargslist: (tfpdef ['=' test] (',' tfpdef ['=' test])* [',' [
                   '*' [tfpdef] (',' tfpdef ['=' test])* [',' ['**' tfpdef [',']]]
                 | '**' tfpdef [',']]]
             | '*' [tfpdef] (',' tfpdef ['=' test])* [',' ['**' tfpdef [',']]]
             | '**' tfpdef [','])
           tfpdef: NAME [':' test]
           varargslist: (vfpdef ['=' test] (',' vfpdef ['=' test])* [',' [
                   '*' [vfpdef] (',' vfpdef ['=' test])* [',' ['**' vfpdef [',']]]
                 | '**' vfpdef [',']]]
             | '*' [vfpdef] (',' vfpdef ['=' test])* [',' ['**' vfpdef [',']]]
             | '**' vfpdef [',']
           )
           vfpdef: NAME

        */
        int i, j, k, nposargs = 0, nkwonlyargs = 0;
        int nposdefaults = 0;
        boolean foundDefault = false;
        List<arg> posargs, kwonlyargs;
        List<expr> posdefaults, kwdefaults;
        arg vararg = null, kwarg = null;
        arg arg;
        Node ch;
        if (n.type() == PARAMETERS) {
            if (n.nch() == 2) { /* () as argument list */
                return new arguments(null, null, null, null, null, null);
            }
            n = n.child(1);
        }
        assert n.type() == TYPEDARGSLIST || n.type() == VARARGSLIST;

        /* First count the number of positional args & defaults.  The
           variable i is the loop index for this for loop and the next.
           The next loop picks up where the first leaves off.
        */
        for (i = 0; i < n.nch(); i++) {
            ch = n.child(i);
            if (ch.type() == STAR) {
                /* skip star */
                i++;
                if (i < n.nch() && /* skip argument following star */
                        (n.child(i).type() == TFPDEF || n.child(i).type() == VFPDEF)) {
                    i++;
                }
                break;
            }
            if (ch.type() == DOUBLESTAR) break;
            if (ch.type() == VFPDEF || ch.type() == TFPDEF) nposargs++;
            if (ch.type() == EQEQUAL) nposdefaults++;
        }
        /* count the number of keyword only args &
           defaults for keyword only args */
        for (; i < n.nch();i++) {
            ch = n.child(i);
            if (ch.type() == DOUBLESTAR) break;
            if (ch.type() == TFPDEF || ch.type() == VFPDEF) nkwonlyargs++;
        }
        posargs = nposargs > 0 ? new ArrayList<>(nposargs) : null;
        kwonlyargs = nkwonlyargs > 0 ? new ArrayList<>(nkwonlyargs) : null;
        posdefaults = nposdefaults > 0 ? new ArrayList<>(nposdefaults) : null;
        /* The length of kwonlyargs and kwdefaults are same
          since we set NULL as default for keyword only argument w/o default
          - we have sequence data structure, but no dictionary */
        kwdefaults = nkwonlyargs > 0 ? new ArrayList<>(nkwonlyargs) : null;
        /* tfpdef: NAME [':' test]
           vfpdef: NAME
        */
        i = 0;
        j = 0; /* index for defaults */
        k = 0; /* index for args */
        while(i < n.nch()) {
            ch = n.child(i);
            switch(ch.type()) {
                case TFPDEF:
                case VFPDEF:
                    if (i + 1 < n.nch() && n.child(i + 1).type() == EQUAL) {
                        expr expression = ast_for_expr(c, n.child(i+2));
                        assert posdefaults != null;
                        posdefaults.set(j++, expression);
                        i+=2;
                        foundDefault = true;
                    } else if (foundDefault) {
                        throw ast_error(c, ch, "non-default argument follows default argument");
                    }
                    arg = ast_for_arg(c, ch);
                    posargs.set(k++, arg);
                    i+=2; /* the name and the comma */
                    break;
                case STAR:
                    if (i+1 >= n.nch() ||
                            (i+2 == n.nch() && n.child(i+1).type() == COMMA)) {
                        throw ast_error(c, n.child(i), "named arguments must follow bare *");
                    }
                    ch = n.child(i+1); /* tfpdef or COMMA */
                    if (ch.type() == COMMA) {
                        int res = 0;
                        i+=2; /* now follows keyword only arguments */
                        res = handle_keywordonly_args(c, n, i, kwonlyargs, kwdefaults);
                        if (res == -1) return null; // XXX
                        i = res; /* res has new position to process */
                    } else {
                        vararg = ast_for_arg(c, ch);
                        i += 3;
                        if (i < n.nch() && (n.child(i).type() == TFPDEF || n.child(i).type() == VFPDEF)) {
                            int res = 0;
                            res = handle_keywordonly_args(c, n, i, kwonlyargs, kwdefaults);
                            if (res == -1) return null; // XXX
                            i = res; /* res has new position to process */
                        }
                    }
                    break;
                case DOUBLESTAR:
                    ch = n.child(i+1); /* tfpdef */
                    assert ch.type() == TFPDEF || ch.type() == VFPDEF;
                    kwarg = ast_for_arg(c, ch);
                    i+=3;
                    break;
                default:
                    throw Py.SystemError(String.format("unexpected node in varargslist: %d @ %d", ch.type(), i));
            }
        }
        return new arguments(n, posargs, vararg, kwonlyargs, kwdefaults, kwarg, posdefaults);
    }

    static cmpopType ast_for_comp_op(compiling c, Node n) {
        /* comp_op: '<'|'>'|'=='|'>='|'<='|'!='|'in'|'not' 'in'|'is'
                   |'is' 'not'
        */
        REQ(n, COMP_OP);
        if (n.unary()) {
            n = n.child(0);
            switch(n.type()) {
                case LESS:
                    return cmpopType.Lt;
                case GREATER:
                    return cmpopType.Gt;
                case EQEQUAL:
                    return cmpopType.Eq;
                case LESSEQUAL:
                    return cmpopType.LtE;
                case GREATEREQUAL:
                    return cmpopType.GtE;
                case NOTEQUAL:
                    return cmpopType.NotEq;
                case NAME:
                    if (n.str().equals("in")) {
                        return cmpopType.In;
                    }
                    if (n.str().equals("is")) {
                        return cmpopType.Is;
                    }
                    /* fall through */
                default:
                    throw Py.SystemError(String.format("invalid comp_op: %s", n.str()));
            }
        } else if (n.nch() == 2) {
            /* handle "not in" and "is not" */
            switch(n.child(0).type()) {
                case NAME:
                    if (n.child(1).str().equals("in")) {
                        return cmpopType.NotIn;
                    }
                    if (n.child(0).str().equals("is")) {
                        return cmpopType.IsNot;
                    }
                default:
                    throw Py.SystemError(String.format("invalid comp_op: %s %s", n.child(0).str(), n.child(1).str()));
            }
        }
        throw Py.SystemError(String.format("invalid comp_op: has %d children", n.nch()));
    }

    static List<expr> seq_for_testlist(compiling c, Node n) {
        /* testlist: test (',' test)* [',']
           testlist_star_expr: test|star_expr (',' test|star_expr)* [',']
        */
        List<expr> seq;
        expr expression;
        int i;
        assert n.type() == TESTLIST || n.type() == TESTLIST_STAR_EXPR || n.type() == TESTLIST_COMP;
        seq = new ArrayList<>((n.nch() + 1) / 2);
        for (i = 0; i < n.nch(); i+=2) {
            Node ch = n.child(i);
            assert ch.type() == TEST || ch.type() == TEST_NOCOND || ch.type() == STAR_EXPR;
            expression = ast_for_expr(c, ch);
            seq.set(i / 2, expression);
        }
        return seq;
    }

    static arg ast_for_arg(compiling c, Node n) {
        String name;
        expr annotation = null;
        Node ch;
        arg ret;
        assert n.type() == TFPDEF || n.type() == VFPDEF;
        ch = n.child(0);
        name = ch.str();
        if (forbiddenName(c, name, ch, false)) {
            return null;
        }
        if (n.nch() == 3 && n.child(1).type() == COLON) {
            annotation = ast_for_expr(c, n.child(2));
        }
        return new arg(n, name, annotation);
    }
    /* returns -1 if failed to handle keyword only arguments
       returns new position to keep processing if successful
                   (',' tfpdef ['=' test])*
                         ^^^
       start pointing here
    */
    static int handle_keywordonly_args(compiling c, Node n, int start, List<arg> kwonlyargs, List<expr> kwdefaults) {
        String argname;
        Node ch;
        expr expression, annotation;
        arg arg;
        int i = start;
        int j = 0; /* index for kwdefaults and kwonlyargs */

        if (kwonlyargs == null) {
            throw ast_error(c, n.child(start), "named arguments must follow bare *");
        }
        while (i < n.nch()) {
            ch = n.child(i);
            switch(ch.type()) {
                case VFPDEF:
                case TFPDEF:
                    if (i +1 < n.nch() && n.child(i+1).type() == EQUAL) {
                        expression = ast_for_expr(c, n.child(i+2));
                        kwdefaults.set(j, expression);
                        i+=2; /* '=' and test */
                    } else { /* setting null if no default value exists */
                        kwdefaults.set(j, null);

                    }
                    if (n.nch() == 3) {
                        /* ch is NAME ':' test */
                        annotation = ast_for_expr(c, ch.child(2));
                    } else {
                        annotation = null;
                    }
                    ch = ch.child(0);
                    argname = ch.str();
                    if (forbiddenName(c, argname, ch, false)) {
                        return -1;
                    }
                    arg = new arg(ch, argname, annotation);
                    kwonlyargs.set(j++, arg);
                    i+=2; /* the name and the comma */
                    break;
                case DOUBLESTAR:
                    return i;
                default:
                    throw ast_error(c, ch, "unexpected node");
            }
        }
        return i;
    }

    static boolean forbiddenName(compiling c, String name, Node n, boolean fullChecks) {
        return false;
    }

    static PySyntaxError ast_error(compiling c, Node n, String s) {
        return new PySyntaxError(s, n.lineno(), n.coloffset(), "", c.filename);
    }

    static expr ast_for_lambdef(compiling c, Node n) {
         /* lambdef: 'lambda' [varargslist] ':' test
           lambdef_nocond: 'lambda' [varargslist] ':' test_nocond */
         arguments args;
         expr expression;
         if (n.nch() == 3) {
             args = new arguments(null, null, null, null, null, null);
             expression = ast_for_expr(c, n.child(2));
         } else {
             args = ast_for_arguments(c, n.child(1));
             expression = ast_for_expr(c, n.child(3));
         }
         return new Lambda(n, args, expression);
    }

    static expr ast_for_ifexpr(compiling c, Node n) {
        /* test: or_test 'if' or_test 'else' test */
        expr expression, body, orelse;
        assert n.nch() == 5;
        body = ast_for_expr(c, n.child(0));
        expression = ast_for_expr(c, n.child(2));
        orelse = ast_for_expr(c, n.child(4));
        return new IfExp(n, expression, body, orelse);
    }

    static expr ast_for_atom(compiling c, Node n) {
        /* atom: '(' [yield_expr|testlist_comp] ')' | '[' [testlist_comp] ']'
           | '{' [dictmaker|testlist_comp] '}' | NAME | NUMBER | STRING+
           | '...' | 'None' | 'True' | 'False'
        */
        Node ch = n.child(0);
        switch(ch.type()) {
            case NAME:
                String name;
                String s = ch.str();
                int len = s.length();
                if (len >= 4 && len <= 5) {
                    switch(s) {
                        case "None":
                            return new NameConstant(n, Py.None);
                        case "True":
                            return new NameConstant(n, Py.True);
                        case "False":
                            return new NameConstant(n, Py.False);
                        default:
                            break;
                    }
                }
                name = s;
                return new Name(n, name, expr_contextType.Load);
            case STRING:
                expr str = parsestrplus(c, n);
        }
    }

    static expr ast_for_atom_expr(compiling c, Node n) {
        int i, nch, start = 0;
        expr e, tmp;
        REQ(n, ATOM_EXPR);
        nch = n.nch();
        if (n.child(0).type() == NAME && n.child(0).str().equals("await")) {
            start = 1;
            assert nch > 1;
        }
        e = ast_for_atom(c, n.child(start));
        if (nch == 1) {
            return e;
        }
        if (start > 0 && nch == 2) {
            return new Await(n, e);
        }
        for (i = start + 1; i < nch; i++) {

        }
    }

    static expr ast_for_power(compiling c, Node n) {
        /* power: atom trailer* ('**' factor)* */
        expr e;
        REQ(n, POWER);
        e = ast_for_atom_expr(c, n.child(0));
        if (n.unary()) {
            return e;
        }
        if (n.child(n.nch() -1).type() == FACTOR) {
            expr f = ast_for_expr(c, n.child(n.nch() -1));
            e = new BinOp(n, e, operatorType.Pow, f);
        }
        return e;
    }

    static expr ast_for_starred(compiling c, Node n) {
        expr tmp;
        REQ(n, STAR_EXPR);
        tmp = ast_for_expr(c, n.child(1));

        /* The Load context is changed later. */
        return new Starred(n, tmp, expr_contextType.Load);
    }

    static expr ast_for_expr(compiling c, Node n) {
        /* handle the full range of simple expressions
           test: or_test ['if' or_test 'else' test] | lambdef
           test_nocond: or_test | lambdef_nocond
           or_test: and_test ('or' and_test)*
           and_test: not_test ('and' not_test)*
           not_test: 'not' not_test | comparison
           comparison: expr (comp_op expr)*
           expr: xor_expr ('|' xor_expr)*
           xor_expr: and_expr ('^' and_expr)*
           and_expr: shift_expr ('&' shift_expr)*
           shift_expr: arith_expr (('<<'|'>>') arith_expr)*
           arith_expr: term (('+'|'-') term)*
           term: factor (('*'|'@'|'/'|'%'|'//') factor)*
           factor: ('+'|'-'|'~') factor | power
           power: atom_expr ['**' factor]
           atom_expr: ['await'] atom trailer*
           yield_expr: 'yield' [yield_arg]
        */
        List<expr> seq;
        int i;
        loop:
        for (;;) {
            switch (n.type()) {
                case TEST:
                case TEST_NOCOND:
                    if (n.child(0).type() == LAMBDEF || n.child(0).type() == LAMBDEF_NOCOND) {
                        return ast_for_lambdef(c, n.child(0));
                    } else if (n.nch() > 1) {
                        return ast_for_ifexpr(c, n);
                    }
                    // Fall through
                case OR_TEST:
                case AND_TEST:
                    if (n.unary()) {
                        n = n.child(0);
                        continue loop;
                    }
                    seq = new ArrayList<>((n.nch() + 1) / 2);
                    for (i = 0; i < n.nch(); i+=2) {
                        expr e = ast_for_expr(c, n.child(i));
                        seq.set(i / 2, e);
                    }
                    if (n.child(1).str().equals("and")) {
                        return new BoolOp(n, boolopType.And, seq);
                    }
                    assert(n.child(1).str().equals("or"));
                    return new BoolOp(n, boolopType.Or, seq);
                case NOT_TEST:
                    if (n.unary()) {
                        n = n.child(0);
                        continue loop;
                    }
                    expr expression = ast_for_expr(c, n.child(1));
                    return new UnaryOp(n, unaryopType.Not, expression);
                case COMPARISON:
                    if (n.unary()) {
                        n = n.child(0);
                        continue loop;
                    }
                    List<cmpopType> ops = new ArrayList<>(n.nch() / 2);
                    List<expr> cmps = new ArrayList<>(n.nch() / 2);
                    for (i = 1; i < n.nch(); i+=2) {
                        cmpopType newoperator = ast_for_comp_op(c, n.child(i));
                        expression = ast_for_expr(c, n.child(i+1));
                        ops.set(i/2, newoperator);
                        cmps.set(i/2, expression);
                    }
                    expression = ast_for_expr(c, n.child(0));
                    return new Compare(n, expression, ops, cmps);
                case STAR_EXPR:
                    return ast_for_starred(c, n);
                /* The next five cases all handle BinOps.  The main body of code
                   is the same in each case, but the switch turned inside out to
                   reuse the code for each type of operator.
                */
                case EXPR:
                case XOR_EXPR:
                case AND_EXPR:
                case SHIFT_EXPR:
                case ARITH_EXPR:
                case TERM:
                    if (n.unary()) {
                        n = n.child(0);
                        continue loop;
                    }
                    return ast_for_binop(c, n);
                case YIELD_EXPR:
                    Node an = null;
                    Node en = null;
                    boolean isFrom = false;
                    expr exp = null;
                    if (n.nch() > 1) {
                        an = n.child(1); /* yield_arg */
                    }
                    if (an != null) {
                        en = an.child(an.nch() - 1);
                        if (an.nch() == 2) {
                            isFrom = true;
                            exp = ast_for_expr(c, en);
                        } else {
                            exp = ast_for_testlist(c, en);
                        }
                    }
                    if (isFrom) {
                        return new YieldFrom(n, exp);
                    }
                    return new Yield(n, exp);
                case FACTOR:
                    if (n.unary()) {
                        n = n.child(0);
                        continue loop;
                    }
                    return ast_for_factor(c, n);
                case POWER:
                    return ast_for_power(c, n);
                default:
                    throw Py.SystemError(String.format("unhandled expr: %d", n.type()));
            }
        }
    }

    static expr ast_for_testlist(compiling c, Node n) {
        /* testlist_comp: test (comp_for | (',' test)* [',']) */
        /* testlist: test (',' test)* [','] */
        assert n.nch() > 0;
        if (n.type() == TESTLIST_COMP) {
            if (n.nch() > 1) {
                assert n.child(1).type() != COMP_FOR;
            }
        } else {
            assert n.type() == TESTLIST || n.type() == TESTLIST_STAR_EXPR;
        }
        if (n.unary()) {
            return ast_for_expr(c, n.child(0));
        }
        List<expr> tmp = seq_for_testlist(c, n);
        return new Tuple(n, tmp, expr_contextType.Load);
    }

    static stmt ast_for_expr_stmt(compiling c, Node n) {
        /* expr_stmt: testlist_star_expr (annassign | augassign (yield_expr|testlist) |
                        ('=' (yield_expr|testlist_star_expr))*)
           annassign: ':' test ['=' test]
           testlist_star_expr: (test|star_expr) (',' test|star_expr)* [',']
           augassign: '+=' | '-=' | '*=' | '@=' | '/=' | '%=' | '&=' | '|=' | '^='
                    | '<<=' | '>>=' | '**=' | '//='
           test: ... here starts the operator precedence dance
        */
        REQ(n, EXPR_STMT);
        if (n.nch() == 1) {
            expr e = ast_for_testlist(c, n.child(0));
            return new Expr(n, e);
        }
    }

    /* Data structure used internally */
    static class compiling {
        String filename;
        PyObject nomalize; /* Normalization function from unicodedata. */

        public compiling(String filename) {
            this.filename = filename;
            this.nomalize = null;
        }
    }
}
