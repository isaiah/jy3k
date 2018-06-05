package org.python.core;

import org.python.antlr.GrammarActions;
import org.python.antlr.ast.AnnAssign;
import org.python.antlr.ast.Assert;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.AsyncFor;
import org.python.antlr.ast.AsyncFunctionDef;
import org.python.antlr.ast.AsyncWith;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.AugAssign;
import org.python.antlr.ast.Await;
import org.python.antlr.ast.BinOp;
import org.python.antlr.ast.BoolOp;
import org.python.antlr.ast.Break;
import org.python.antlr.ast.Bytes;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.Compare;
import org.python.antlr.ast.Continue;
import org.python.antlr.ast.Delete;
import org.python.antlr.ast.Dict;
import org.python.antlr.ast.DictComp;
import org.python.antlr.ast.Ellipsis;
import org.python.antlr.ast.ExceptHandler;
import org.python.antlr.ast.Expr;
import org.python.antlr.ast.Expression;
import org.python.antlr.ast.For;
import org.python.antlr.ast.FormattedValue;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.GeneratorExp;
import org.python.antlr.ast.Global;
import org.python.antlr.ast.If;
import org.python.antlr.ast.IfExp;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Index;
import org.python.antlr.ast.Interactive;
import org.python.antlr.ast.JoinedStr;
import org.python.antlr.ast.Lambda;
import org.python.antlr.ast.ListComp;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.NameConstant;
import org.python.antlr.ast.Nonlocal;
import org.python.antlr.ast.Num;
import org.python.antlr.ast.Pass;
import org.python.antlr.ast.Raise;
import org.python.antlr.ast.Return;
import org.python.antlr.ast.Set;
import org.python.antlr.ast.SetComp;
import org.python.antlr.ast.Slice;
import org.python.antlr.ast.Starred;
import org.python.antlr.ast.Str;
import org.python.antlr.ast.Subscript;
import org.python.antlr.ast.Try;
import org.python.antlr.ast.Tuple;
import org.python.antlr.ast.UnaryOp;
import org.python.antlr.ast.While;
import org.python.antlr.ast.With;
import org.python.antlr.ast.Yield;
import org.python.antlr.ast.YieldFrom;
import org.python.antlr.ast.alias;
import org.python.antlr.ast.arg;
import org.python.antlr.ast.arguments;
import org.python.antlr.ast.boolopType;
import org.python.antlr.ast.cmpopType;
import org.python.antlr.ast.comprehension;
import org.python.antlr.ast.expr_contextType;
import org.python.antlr.ast.keyword;
import org.python.antlr.ast.operatorType;
import org.python.antlr.ast.unaryopType;
import org.python.antlr.ast.withitem;
import org.python.antlr.base.excepthandler;
import org.python.antlr.base.expr;
import org.python.antlr.base.mod;
import org.python.antlr.base.slice;
import org.python.antlr.base.stmt;
import org.python.parser.Node;

import java.util.EnumSet;
import java.util.List;

import static org.python.antlr.ast.expr_contextType.Load;
import static org.python.parser.GramInit.*;
import static org.python.parser.ParserGenerator.REQ;
import static org.python.parser.TokenType.*;

/* Python/ast.c */
public class Ast {
    static GrammarActions actions = new GrammarActions();

    /**
     * Transform the CST rooted at node * to the appropriate AST
     * @param n
     * @param flags
     * @param filename
     * @return
     */
    public static mod PyAST_FromNodeObject(Node n, EnumSet flags, String filename) {
        compiling c = new compiling(filename);
        int i, j, k, num;
        k = 0;
        stmt[] stmts;
        stmt s;
        Node ch;

        switch(n.type()) {
            case FILE_INPUT:
                stmts = new stmt[num_stmts(n)];
                for (i = 0; i < n.nch(); i++) { /* skip the ENDMARKER */
                    ch = n.child(i);
                    if (ch.type() == NEWLINE) {
                        continue;
                    }
                    REQ(ch, STMT);
                    num = num_stmts(ch);
                    if (num == 1) {
                        s = ast_for_stmt(c, ch);
                        stmts[k++] = s;
                    } else {
                        ch = ch.child(0);
                        REQ(ch, SIMPLE_STMT);
                        for (j = 0; j < num; j++) {
                            s = ast_for_stmt(c, ch.child(j*2));
                            stmts[k++] = s;
                        }
                    }
                }
                BodyWithDocstring ret = docstring_from_stmts(asList(stmts));
                return new org.python.antlr.ast.Module(n, ret.stmts, ret.docstring);
            case EVAL_INPUT:
                expr testlistAst;
                testlistAst = ast_for_testlist(c, n.child(0));
                return new Expression(n, testlistAst);
            case SINGLE_INPUT:
                if (n.child(0).type() == NEWLINE) {
                    stmts = new stmt[1];
                    stmts[0] = new Pass(n);
                    return new Interactive(n, asList(stmts));
                }
                n = n.child(0);
                num = num_stmts(n);
                stmts = new stmt[num];
                if (num == 1) {
                    s = ast_for_stmt(c, n);
                    stmts[0] = s;
                } else {
                    /* Only a simple_stmt can contain multiple statements. */
                    REQ(n, SIMPLE_STMT);
                    for (i = 0; i < n.nch(); i+=2) {
                        if (n.child(i).type() == NEWLINE) {
                            break;
                        }
                        s = ast_for_stmt(c, n.child(i));
                        stmts[i/2] = s;
                    }
                }
                return new Interactive(n, asList(stmts));
            default:
                throw Py.SystemError(String.format("invalid node %d for PyAST_FromNode", n.type()));

        }
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
                    return new Pass(n);
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

    static class BodyWithDocstring {
        final List<stmt> stmts;
        final String docstring;

        public BodyWithDocstring(List<stmt> stmts, String doc) {
            this.stmts = stmts;
            this.docstring = doc;
        }
    }

    static BodyWithDocstring ast_for_body(compiling c, Node n) {
        List<stmt> stmts = ast_for_suite(c, n);
        return docstring_from_stmts(stmts);
    }

    static stmt ast_for_if_stmt(compiling c, Node n) {
        /* if_stmt: 'if' test ':' suite ('elif' test ':' suite)*
           ['else' ':' suite]
        */
        String s;
        REQ(n, IF_STMT);
        if (n.nch() == 4) {
            expr expression;
            List<stmt> suite;

            expression = ast_for_expr(c, n.child(1));
            suite = ast_for_suite(c, n.child(3));
            return new If(n, expression, suite, null);
        }

        s = n.child(4).str();
        /* s[2], the third character in the string, will be
           's' for el_s_e, or
           'i' for el_i_f
        */
        if (s.charAt(2) == 's') {
            expr expression;
            List<stmt> seq1, seq2;
            expression = ast_for_expr(c, n.child(1));
            seq1 = ast_for_suite(c, n.child(3));
            seq2 = ast_for_suite(c, n.child(6));
            return new If(n, expression, seq1, seq2);
        } else if (s.charAt(2) == 'i') {
            int i, n_elif;
            boolean has_else = false;
            expr expression;
            List<stmt> suite;
            stmt[] orelse = null;
            n_elif = n.nch() - 4;
            /* must reference the child n_elif+1 since 'else' token is third,
               not fourth, child from the end. */
            if (n.child(n_elif + 1).type() == NAME && n.child(n_elif +1).str().charAt(2) == 's') {
                has_else = true;
                n_elif -= 3;
            }
            n_elif /= 4;

            if (has_else) {
                List<stmt> suite2;
                orelse = new stmt[1];
                expression = ast_for_expr(c, n.child(n.nch() - 6));
                suite = ast_for_suite(c, n.child(n.nch() - 4));
                suite2 = ast_for_suite(c, n.child(n.nch() - 1));
                orelse[0] = new If(n.child(n.nch() - 6), expression, suite, suite2);
                n_elif--;
            }

            for (i = 0; i < n_elif; i++) {
                int off = 5 + (n_elif - i - 1) * 4;
                stmt[] newobj = new stmt[1];
                expression = ast_for_expr(c, n.child(off));
                suite = ast_for_suite(c, n.child(off + 2));
                newobj[0] = new If(n.child(off), expression, suite, asList(orelse));
                orelse = newobj;
            }

            expression = ast_for_expr(c, n.child(1));
            suite = ast_for_suite(c, n.child(3));
            return new If(n, expression, suite, asList(orelse));
        }
        throw Py.SystemError(String.format("unexpected token in 'if' statement: %s", s));
    }

    static stmt ast_for_while_stmt(compiling c, Node n) {
        /* while_stmt: 'while' test ':' suite ['else' ':' suite] */
        REQ(n, WHILE_STMT);
        if (n.nch() == 4) {
            expr expression = ast_for_expr(c, n.child(1));
            List<stmt> suite = ast_for_suite(c, n.child(3));
            return new While(n, expression, suite, null);
        } else if (n.nch() == 7) {
            expr expression = ast_for_expr(c, n.child(1));
            List<stmt> seq1 = ast_for_suite(c, n.child(3));
            List<stmt> seq2 = ast_for_suite(c, n.child(6));
            return new While(n, expression, seq1, seq2);
        }
        throw Py.SystemError(String.format("wrong number of tokens for 'while' statement: %d", n.nch()));
    }

    static stmt ast_for_for_stmt(compiling c, Node n, boolean isAsync) {
        List<stmt> seq = null, suite;
        List<expr> _target;
        expr expression;
        expr target, first;
        Node nodeTarget;
        /* for_stmt: 'for' exprlist 'in' testlist ':' suite ['else' ':' suite] */
        REQ(n, FOR_STMT);

        if (n.nch() == 9) {
            seq = ast_for_suite(c, n.child(8));
        }

        nodeTarget = n.child(1);
        _target = ast_for_exprlist(c, nodeTarget, expr_contextType.Store);
        /* Check the # of children rather than the length of _target, since
           for x, in ... has 1 element in _target, but still requires a Tuple. */
        first = _target.get(0);
        if (nodeTarget.unary()) {
            target = first;
        } else {
            target = new Tuple(first, _target, expr_contextType.Store);
        }

        expression = ast_for_testlist(c, n.child(3));
        suite = ast_for_suite(c, n.child(5));

        if (isAsync) {
            return new AsyncFor(n, target, expression, suite, seq);
        }
        return new For(n, target, expression, suite, seq);
    }

    static excepthandler ast_for_except_clause(compiling c, Node exc, Node body) {
        /* except_clause: 'except' [test ['as' test]] */
        REQ(exc, EXCEPT_CLAUSE);
        REQ(body, SUITE);

        if (exc.unary()) {
            List<stmt> suite = ast_for_suite(c, body);
            return new ExceptHandler(exc, null, null, suite);
        } else if (exc.nch() == 2) {
            expr expression;
            List<stmt> suite;
            expression = ast_for_expr(c, exc.child(1));

            suite = ast_for_suite(c, body);
            return new ExceptHandler(exc, expression, null, suite);
        } else if (exc.nch() == 4) {
            List<stmt> suite;
            expr expression;
            String e = exc.child(3).str();
            if (forbiddenName(c, e, exc.child(3), false)) {
                return null;
            }
            expression = ast_for_expr(c, exc.child(1));
            suite = ast_for_suite(c, body);
            return new ExceptHandler(exc, expression, e, suite);
        }
        throw Py.SystemError(String.format("wrong number of children for 'except' clause: %d", exc.nch()));
    }

    static stmt ast_for_try_stmt(compiling c, Node n) {
        int nch = n.nch();
        int n_except = (nch - 3) / 3;
        List<stmt> body, orelse = null, _finally = null;
        excepthandler[] handlers = null;
        REQ(n, TRY_STMT);

        body = ast_for_suite(c, n.child(2));

        if (n.child(nch - 3).type() == NAME) {
            if (n.child(nch - 3).str().equals("finally")) {
                if (nch >= 9 && n.child(nch - 6).type() == NAME) {
                    /* we can assume it's an "else",
                       because nch >= 9 for try-else-finally and
                       it would otherwise have a type of except_clause */
                    orelse = ast_for_suite(c, n.child(nch - 4));
                    n_except--;
                }
                _finally  = ast_for_suite(c, n.child(nch - 1));
                n_except--;
            } else {
                /* we can assume it's an "else",
                   otherwise it would have a type of except_clause */
                orelse = ast_for_suite(c, n.child(nch - 1));
                n_except--;
            }
        } else if (n.child(nch - 3).type() != EXCEPT_CLAUSE) {
            throw ast_error(c, n, "malformed 'try' statement");
        }
        if (n_except > 0) {
            int i;
            /* process except statements to create a try ... except */
            handlers = new excepthandler[n_except];

            for (i = 0; i < n_except; i++) {
                excepthandler e = ast_for_except_clause(c, n.child(3 + i * 3), n.child(5 + i * 3));
                handlers[i] = e;
            }
        }
        return new Try(n, body, asList(handlers), orelse, _finally);
    }

    /* with_item: test['as' expr] */
    static withitem ast_for_with_item(compiling c, Node n) {
        expr contextExpr, optionalVars  = null;
        REQ(n, WITH_ITEM);

        contextExpr = ast_for_expr(c, n.child(0));
        if (n.nch() == 3) {
            optionalVars = ast_for_expr(c, n.child(2));
            set_context(c, optionalVars, expr_contextType.Store, n);
        }
        return new withitem(n, contextExpr, optionalVars);
    }

    static stmt ast_for_with_stmt(compiling c, Node n, boolean isAsync) {
        /* with_stmt: 'with' with_item (',' with_item)* ':' suite */
        int i, nItems;
        withitem[] items;
        List<stmt> body;

        REQ(n, WITH_STMT);

        nItems = (n.nch() - 2) / 2;
        items = new withitem[nItems];

        for (i = 1; i < n.nch(); i+=2) {
            withitem item = ast_for_with_item(c, n.child(i));
            items[(i-1)/2] = item;
        }
        body = ast_for_suite(c, n.child(n.nch() - 1));
        if (isAsync) {
            return new AsyncWith(n, asList(items), body);
        }
        return new With(n, asList(items), body);
    }

    static stmt ast_for_classdef(compiling c, Node n, List<expr> decoratorSeq) {
        /* classdef: 'class' NAME ['(' arglist ')'] ':' suite */
        String classname;
        BodyWithDocstring s;
        Call call;

        REQ(n, CLASSDEF);

        if (n.nch() == 4) { /* class Name ':' suite */
            s = ast_for_body(c, n.child(3));

            classname = n.child(1).str();
            if (forbiddenName(c, classname, n.child(3), false)) {
                return null;
            }
            return new ClassDef(n, classname, null, null, s.stmts, decoratorSeq, s.docstring);
        }
        if (n.child(3).type() == RPAR) { /* class NAME '(' ')' ':' suite */
            s = ast_for_body(c, n.child(5));
            classname = n.child(1).str();
            if (forbiddenName(c, classname, n.child(3), false)) {
                return null;
            }
            return new ClassDef(n, classname, null, null, s.stmts, decoratorSeq, s.docstring);
        }
        /* class NAME '(' arglist ')' ':' suite */
        /* build up a fake Call node so we can extract its pieces */

        String dummyName = n.child(1).str();
        expr dummy = new Name(n, dummyName, expr_contextType.Load);
        call = (Call) ast_for_call(c, n.child(3), dummy, false);
        s = ast_for_body(c, n.child(6));
        classname = n.child(1).str();
        if (forbiddenName(c, classname, n.child(1), false)) {
            return null;
        }
        return new ClassDef(n, classname, call.getInternalArgs(), call.getInternalKeywords(), s.stmts, decoratorSeq, s.docstring);
    }

    static stmt ast_for_del_stmt(compiling c, Node n) {
        List<expr> exprList;
        /* del_stmt: 'del' exprlist */
        REQ(n, DEL_STMT);
        exprList = ast_for_exprlist(c, n.child(1), expr_contextType.Del);
        return new Delete(n, exprList);
    }

    static stmt ast_for_flow_stmt(compiling c, Node n) {
        /*
          flow_stmt: break_stmt | continue_stmt | return_stmt | raise_stmt
                     | yield_stmt
          break_stmt: 'break'
          continue_stmt: 'continue'
          return_stmt: 'return' [testlist]
          yield_stmt: yield_expr
          yield_expr: 'yield' testlist | 'yield' 'from' test
          raise_stmt: 'raise' [test [',' test [',' test]]]
        */
        Node ch;

        REQ(n, FLOW_STMT);
        ch = n.child(0);
        switch (ch.type()) {
            case BREAK_STMT:
                return new Break(n);
            case CONTINUE_STMT:
                return new Continue(n);
            case YIELD_STMT: /* will reduce to yield_expr */
                expr exp = ast_for_expr(c, ch.child(0));
                return new Expr(n, exp);
            case RETURN_STMT:
                if (ch.unary()) {
                    return new Return(n, null);
                }
                expr expression = ast_for_testlist(c, ch.child(1));
                return new Return(n, expression);
            case RAISE_STMT:
                if (ch.unary()) {
                    return new Raise(n, null, null);
                } else if (n.nch() >= 2) {

                    expr cause = null;
                    expression = ast_for_expr(c, ch.child(1));
                    if (n.nch() == 4) {
                        cause = ast_for_expr(c, ch.child(3));
                    }
                    return new Raise(n, expression, cause);
                }
                /* fall through */
            default:
                throw Py.SystemError(String.format("unexpected flow_stmt: %d", ch.type()));
        }
    }

    static alias alias_for_import_name(compiling c, Node n, boolean store) {
        /*
          import_as_name: NAME ['as' NAME]
          dotted_as_name: dotted_name ['as' NAME]
          dotted_name: NAME ('.' NAME)*
        */
        String str, name;
        loop:
        for(;;) {
            switch (n.type()) {
                case IMPORT_AS_NAME:
                    Node nameNode = n.child(0);
                    str = null;
                    name = nameNode.str();
                    if (n.nch() == 3) {
                        Node strNode = n.child(2);
                        str = strNode.str();
                        if (store && forbiddenName(c, str, strNode, false)) {
                            return null;
                        }
                    } else {
                        if (forbiddenName(c, name, nameNode, false)) {
                            return null;
                        }
                    }
                    return new alias(n, name, str);
                case DOTTED_AS_NAME:
                    if (n.unary()) {
                        n = n.child(0);
                        continue loop;
                    }
                    Node asnameNode = n.child(2);
                    alias a = alias_for_import_name(c, n.child(0), false);
                    a.setInternalAsname(asnameNode.str());
                    if (forbiddenName(c, a.getInternalAsname(), asnameNode, false)) {
                        return null;
                    }
                    return a;
                case DOTTED_NAME:
                    if (n.unary()) {
                        nameNode = n.child(0);
                        name = nameNode.str();
                        if (store && forbiddenName(c, name, nameNode, false)) {
                            return null;
                        }
                        return new alias(n, name, null);
                    }
                    /* Create a string of the form "a.b.c" */
                    int i;
                    StringBuilder s = new StringBuilder();
                    for (i = 0; i < n.nch(); i+=2) {
                        s.append(n.child(i).str()).append('.');
                    }
                    return new alias(n, s.toString(), null);
                case STAR:
                    return new alias(n, "*", null);
                default:
                    throw Py.SystemError("unhandled import name condition");
            }
        }
    }

    static stmt ast_for_import_stmt(compiling c, Node n) {
        /*
          import_stmt: import_name | import_from
          import_name: 'import' dotted_as_names
          import_from: 'from' (('.' | '...')* dotted_name | ('.' | '...')+)
                       'import' ('*' | '(' import_as_names ')' | import_as_names)
        */
        int i;
        alias[] aliases;

        REQ(n, IMPORT_STMT);
        n = n.child(0);
        if (n.type() == IMPORT_NAME) {
            n = n.child(1);
            REQ(n, DOTTED_AS_NAMES);
            aliases = new alias[(n.nch() + 1) / 2];
            for (i = 0; i < n.nch(); i+=2) {
                alias import_alias = alias_for_import_name(c, n.child(i), true);
                aliases[i/2] = import_alias;
            }
            return new Import(n, asList(aliases));
        } else if (n.type() == IMPORT_FROM) {
            int n_children;
            int idx, ndots = 0;
            alias mod = null;
            String modname = null;
            /* Count the number of dots (for relative imports) and check for the
               optional module name */
            for (idx = 1; idx < n.nch(); idx++) {
                if (n.child(idx).type() == DOTTED_NAME) {
                    mod = alias_for_import_name(c, n.child(idx), false);
                    idx++;
                    break;
                } else if (n.child(idx).type() == ELLIPSIS) {
                    /* three consecutive dots are tokenized as one ELLIPSIS */
                    ndots += 3;
                    continue;
                } else if (n.child(idx).type() != DOT) {
                    break;
                }
                ndots++;
            }
            idx++; /* skip over the 'import' keyword */
            switch (n.child(idx).type()) {
                case STAR:
                    /* from ... import * */
                    n = n.child(idx);
                    n_children = 1;
                    break;
                case LPAR:
                    /* from ... import (x, y, z) */
                    n = n.child(idx + 1);
                    n_children = n.nch();
                    break;
                case IMPORT_AS_NAMES:
                    /* from ... import x, y, z */
                    n = n.child(idx);
                    n_children = n.nch();
                    if (n_children % 2 == 0) {
                        throw ast_error(c, n, "trailing comma not allowed without surrounding parentheses");
                    }
                    break;
                default:
                    throw ast_error(c, n, "unexpected node-type in from-import");
            }

            aliases = new alias[(n_children + 1) / 2];

            /* handle "from ... import *" special b/c there's no children */
            if (n.type() == STAR) {
                alias import_alias = alias_for_import_name(c, n, true);
                aliases[0] = import_alias;
            } else {
                for (i = 0; i < n.nch(); i+=2) {
                    alias import_alias = alias_for_import_name(c, n.child(i), true);
                    aliases[i/2] = import_alias;
                }
            }
            if (mod != null) {
                modname = mod.getInternalName();
            }
            return new ImportFrom(n, modname, asList(aliases), ndots);
        }
        throw Py.SystemError(String.format("unknown import statement: starts with command '%s'", n.child(0).str()));
    }

    static stmt ast_for_global_stmt(compiling c, Node n) {
        /* global_stmt: 'global' NAME (',' NAME)* */
        String name;
        String[] s;
        int i;
        REQ(n, GLOBAL_STMT);
        s = new String[n.nch() / 2];
        for (i = 1; i < n.nch(); i+=2) {
            name = n.child(i).str();
            s[i/2] = name;
        }
        return new Global(n, asList(s));
    }

    static stmt ast_for_nonlocal_stmt(compiling c, Node n) {
        /* nonlocal_stmt: 'nonlocal' NAME (',' NAME)* */
        String name;
        String[] s;
        int i;
        REQ(n, NONLOCAL_STMT);
        s = new String[n.nch() / 2];
        for (i = 1; i < n.nch(); i+=2) {
            name = n.child(i).str();
            s[i/2] = name;
        }
        return new Nonlocal(n, asList(s));
    }

    static stmt ast_for_assert_stmt(compiling c, Node n) {
        /* assert_stmt: 'assert' test [',' test] */
        REQ(n, ASSERT_STMT);
        if (n.nch() == 2) {
            expr expression = ast_for_expr(c, n.child(1));
            return new Assert(n, expression, null);
        } else if (n.nch() == 4) {
            expr expr1 = ast_for_expr(c, n.child(1));
            expr expr2 = ast_for_expr(c, n.child(3));
            return new Assert(n, expr1, expr2);
        }
        throw Py.SystemError(String.format("improper number of parts to 'assert' statement: %d", n.nch()));
    }

    static List<stmt> ast_for_suite(compiling c, Node n) {
        /* suite: simple_stmt | NEWLINE INDENT stmt+ DEDENT */
        stmt[] seq;
        stmt s;
        int i, total, num, end, pos = 0;
        Node ch;

        REQ(n, SUITE);

        total = num_stmts(n);
        seq = new stmt[total];

        if (n.child(0).type() == SIMPLE_STMT) {
            n = n.child(0);
            /* simple_stmt always ends with a NEWLINE,
               and may have a trailing SEMI
            */
            end = n.nch() - 1;
            if (n.child(end - 1).type() == SEMI) {
                end--;
            }
            /* loop by 2 to skip semi-colons */
            for (i = 0; i < end; i += 2) {
                ch = n.child(i);
                s = ast_for_stmt(c, ch);
                seq[pos++] = s;
            }
        } else {
            for (i = 2; i < n.nch() - 1; i++) {
                ch = n.child(i);
                REQ(n, STMT);

                num = num_stmts(ch);
                if  (num == 1) {
                    /* small_stmt or compound_stmt with only one child */
                    s = ast_for_stmt(c, ch);
                    seq[pos++] = s;
                } else {
                    int j;
                    ch = ch.child(0);
                    REQ(ch, SIMPLE_STMT);
                    for (j = 0; j < ch.nch(); j+=2) {
                        /* statement terminates with a semi-colon ';' */
                        if (ch.child(j).nch() == 0) {
                            assert j+1 == ch.nch();
                            break;
                        }
                        s = ast_for_stmt(c, ch.child(j));
                        seq[pos++] = s;
                    }
                }
            }
        }
        return asList(seq);
    }

    static BodyWithDocstring docstring_from_stmts(List<stmt> stmts) {
        if (!stmts.isEmpty()) {
            stmt s = stmts.get(0);
            /* If first statement is a literal string, it's the doc string. */
            if (s instanceof Expr && ((Expr) s).getInternalValue() instanceof Str) {
                String doc = ((Str) ((Expr) s).getInternalValue()).getInternalS();
                return new BodyWithDocstring(stmts.subList(1, stmts.size()), doc);
            }
        }
        return new BodyWithDocstring(stmts, null);
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
        arg[] posargs, kwonlyargs;
        expr[] posdefaults, kwdefaults;
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
        posargs = nposargs > 0 ? new arg[nposargs] : null;
        kwonlyargs = nkwonlyargs > 0 ? new arg[nkwonlyargs] : null;
        posdefaults = nposdefaults > 0 ? new expr[nposdefaults] : null;
        /* The length of kwonlyargs and kwdefaults are same
          since we set NULL as default for keyword only argument w/o default
          - we have sequence data structure, but no dictionary */
        kwdefaults = nkwonlyargs > 0 ? new expr[nkwonlyargs] : null;
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
                        posdefaults[j++] = expression;
                        i+=2;
                        foundDefault = true;
                    } else if (foundDefault) {
                        throw ast_error(c, ch, "non-default argument follows default argument");
                    }
                    arg = ast_for_arg(c, ch);
                    posargs[k++] = arg;
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
        return new arguments(n, asList(posargs), vararg, asList(kwonlyargs), asList(kwdefaults), kwarg, asList(posdefaults));
    }

    static operatorType ast_for_augassign(compiling c, Node n) {
        REQ(n, AUGASSIGN);
        n = n.child(0);
        switch(n.str().charAt(0)) {
            case '+':
                return operatorType.Add;
            case '-':
                return operatorType.Sub;
            case '/':
                if (n.str().charAt(1) == '/') {
                    return operatorType.FloorDiv;
                } else {
                    return operatorType.Div;
                }
            case '%':
                return operatorType.Mod;
            case '<':
                return operatorType.LShift;
            case '>':
                return operatorType.RShift;
            case '&':
                return operatorType.BitAnd;
            case '^':
                return operatorType.BitXor;
            case '|':
                return operatorType.BitOr;
            case '*':
                if (n.str().charAt(1) == '*') {
                    return operatorType.Pow;
                } else {
                    return operatorType.Mult;
                }
            case '@':
                return operatorType.MatMult;
            default:
                throw Py.SystemError(String.format("invalid augassign: %s", n.str()));

        }
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
        expr[] seq;
        expr expression;
        int i;
        assert n.type() == TESTLIST || n.type() == TESTLIST_STAR_EXPR || n.type() == TESTLIST_COMP;
        seq = new expr[(n.nch() + 1) / 2];
        for (i = 0; i < n.nch(); i+=2) {
            Node ch = n.child(i);
            assert ch.type() == TEST || ch.type() == TEST_NOCOND || ch.type() == STAR_EXPR;
            expression = ast_for_expr(c, ch);
            seq[i / 2] = expression;
        }
        return asList(seq);
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
    static int handle_keywordonly_args(compiling c, Node n, int start, arg[] kwonlyargs, expr[] kwdefaults) {
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
                        kwdefaults[j] = expression;
                        i+=2; /* '=' and test */
                    } else { /* setting null if no default value exists */
                        kwdefaults[j] = null;

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
                    kwonlyargs[j++] = arg;
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

    static expr ast_for_dotted_name(compiling c, Node n) {
        expr e;
        String id;
        int i;

        REQ(n, DOTTED_NAME);
        id = n.child(0).str();
        e = new Name(n, id, expr_contextType.Load);
        for (i = 2; i < n.nch(); i+=2) {
            id = n.child(i).str();
            e = new Attribute(n, e, id, expr_contextType.Load);
        }
        return e;
    }

    static expr ast_for_decorator(compiling c, Node n) {
        /* decorator: '@' dotted_name [ '(' [arglist] ')' ] NEWLINE */
        expr d = null;
        expr nameExpr;

        REQ(n, DECORATOR);
        REQ(n.child(0), AT);
        REQ(n.child(n.nch() - 1), NEWLINE);

        nameExpr = ast_for_dotted_name(c, n.child(1));
        if (n.nch() == 3) { /* No arguments */
            d = nameExpr;
            nameExpr = null;
        } else if (n.nch() == 5) { /* Call with no arguments */
            d = new Call(n, nameExpr, null, null);
            nameExpr = null;
        } else {
            d = ast_for_call(c, n.child(3), nameExpr, true);
            nameExpr = null;
        }
        return d;
    }

    static List<expr> ast_for_decorators(compiling c, Node n) {
        expr[] decoratorSeq;
        expr d;
        int i;

        REQ(n, DECORATORS);

        decoratorSeq = new expr[n.nch()];
        for (i = 0; i < n.nch(); i++) {
            d = ast_for_decorator(c, n.child(i));
            decoratorSeq[i] = d;
        }
        return asList(decoratorSeq);
    }

    static stmt ast_for_funcdef_impl(compiling c, Node n, List<expr> decoratorSeq, boolean isAsync) {
        /* funcdef: 'def' NAME parameters ['->' test] ':' suite */
        String name;
        arguments args;
        BodyWithDocstring body;
        expr returns = null;
        int name_i = 1;
        REQ(n, FUNCDEF);

        name = n.child(name_i).str();
        if (forbiddenName(c, name, n.child(name_i), false));
        args = ast_for_arguments(c, n.child(name_i + 1));

        if (n.child(name_i+2).type() == RARROW) {
            returns = ast_for_expr(c, n.child(name_i + 3));
            name_i += 2;
        }
        body = ast_for_body(c, n.child(name_i + 3));
        if (isAsync) {
            return new AsyncFunctionDef(n, name, args, body.stmts, decoratorSeq, returns, body.docstring);
        }
        return new FunctionDef(n, name, args, body.stmts, decoratorSeq, returns, body.docstring);
    }

    static stmt ast_for_async_funcdef(compiling c, Node n, List<expr> seq) {
        /* async_funcdef: 'async' funcdef */
        return ast_for_funcdef_impl(c, n.child(1), seq, true);
    }

    static stmt ast_for_funcdef(compiling c, Node n, List<expr> seq) {
        /* funcdef: 'def' NAME parameters ['->' test] ':' suite */
        return ast_for_funcdef_impl(c, n, seq, false);
    }

    static stmt ast_for_async_stmt(compiling c, Node n) {
        switch (n.child(1).type()) {
            case FUNCDEF:
                return ast_for_funcdef_impl(c, n.child(1), null, true);
            case WITH_STMT:
                return ast_for_with_stmt(c, n.child(1), true);
            case FOR_STMT:
                return ast_for_for_stmt(c, n.child(1), true);
            default:
                throw Py.SystemError(String.format("invalid async statement: %s", n.child(1).str()));
        }
    }

    static stmt ast_for_decorated(compiling c, Node n) {
        /* decorated: decorators (classdef | funcdef | async_funcdef) */
        stmt thing = null;
        List<expr> decoratorSeq = null;

        REQ(n, DECORATED);
        decoratorSeq = ast_for_decorators(c, n.child(0));


        Node ch = n.child(1);
        switch (ch.type()) {
            case FUNCDEF:
                thing = ast_for_funcdef(c, ch, decoratorSeq);
                break;
            case CLASSDEF:
                thing = ast_for_classdef(c, ch, decoratorSeq);
                break;
            case ASYNC_FUNCDEF:
                thing = ast_for_async_funcdef(c, ch, decoratorSeq);
                break;
            default:
                break;
        }

        if (thing != null) {
            // TODO update thing lineno and col_offset
        }
        return thing;
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

    static List<expr> ast_for_exprlist(compiling c, Node n, expr_contextType context) {
        expr[] seq;
        int i;
        expr e;
        REQ(n, EXPRLIST);
        seq = new expr[(n.nch() + 1) / 2];
        for (i = 0; i < n.nch(); i+=2) {
            e = ast_for_expr(c, n.child(i));
            seq[i / 2] = e;
            set_context(c, e, context, n.child(i));
        }
        return asList(seq);
    }

    enum Comp {
        GENEXP, LISTCOMP, SETCOMP
    }

    static List<comprehension> ast_for_comprehension(compiling c, Node n) {
        int i, n_fors;
        comprehension[] comps;

        n_fors = count_comp_fors(c, n);
        comps = new comprehension[n_fors];
        for (i = 0; i < n_fors; i++) {
            comprehension comp;
            List<expr> t;
            expr expression, first;
            Node for_ch;
            Node sync_n;
            int isAsync = 0;
            REQ(n, COMP_FOR);
            if (n.nch() == 2) {
                isAsync = 1;
                REQ(n.child(0), NAME);
                assert(n.child(0).str().equals("async"));
                sync_n = n.child(1);
            } else {
                sync_n = n.child(0);
            }
            REQ(sync_n, SYNC_COMP_FOR);

            for_ch = sync_n.child(1);
            t = ast_for_exprlist(c, for_ch, expr_contextType.Store);
            expression = ast_for_expr(c, sync_n.child(3));
            /* Check the # of children rather than the length of t, since
               (x for x, in ...) has 1 element in t, but still requires a Tuple. */
            first = t.get(0);
            if (for_ch.unary()) {
                comp = new comprehension(for_ch, first, expression, null, isAsync);
            } else {
                comp = new comprehension(for_ch, new Tuple(first, t, expr_contextType.Store), expression, null, isAsync);
            }
            if (sync_n.nch() == 5) {
                int j, n_ifs;
                expr[] ifs;
                n = sync_n.child(4);
                n_ifs = count_comp_ifs(c, n);
                ifs = new expr[n_ifs];
                for (j = 0; j < n_ifs; j++) {
                    REQ(n, COMP_ITER);
                    n = n.child(0);
                    REQ(n, COMP_IF);

                    expression = ast_for_expr(c, n.child(1));
                    ifs[j] = expression;
                    if (n.nch() == 3) {
                        n = n.child(2);
                    }
                }

                /* on exit, must garantee that n is a comp_for */
                if (n.type() == COMP_ITER) {
                    n = n.child(2);
                }
                comp.setInternalIfs(asList(ifs));
            }
            comps[i] = comp;
        }
        return asList(comps);
    }

    /* Set the context ctx for expr_ty e, recursively traversing e.

       Only sets context for expr kinds that "can appear in assignment context"
       (according to ../Parser/Python.asdl).  For other expr kinds, it sets
       an appropriate syntax error and returns false.
    */
    static boolean set_context(compiling c, expr e, expr_contextType ctx, Node n) {
        List<expr> s = null;
        /* If a particular expression type can't be used for assign / delete,
           set expr_name to its name and an error message will be generated.
        */
        String expr_name = null;
        /* The ast defines augmented store and load contexts, but the
           implementation here doesn't actually use them.  The code may be
           a little more complex than necessary as a result.  It also means
           that expressions in an augmented assignment have a Store context.
           Consider restructuring so that augmented assignment uses
           set_context(), too.
        */
        assert(ctx != expr_contextType.AugStore && ctx != expr_contextType.AugLoad);

        if (e instanceof Attribute) {
            ((Attribute) e).setContext(ctx);
            if (ctx == expr_contextType.Store && forbiddenName(c, ((Attribute) e).getInternalAttr(), n, true)) {
                return false;
            }
        } else if (e instanceof Subscript) {
            ((Subscript) e).setContext(ctx);
        } else if (e instanceof Starred) {
            ((Starred) e).setContext(ctx);
            if (!set_context(c, ((Starred) e).getInternalValue(), ctx, n)) {
                return false;
            }
        } else if (e instanceof Name) {
            if (ctx == expr_contextType.Store && forbiddenName(c, ((Name) e).getInternalId(), n, false)) {
                return false; /* forbiddenName() calls ast_error() */
            }
            ((Name) e).setContext(ctx);
        } else if (e instanceof org.python.antlr.ast.List) {
            ((org.python.antlr.ast.List) e).setContext(ctx);
            s = ((org.python.antlr.ast.List) e).getInternalElts();
        } else if (e instanceof Tuple) {
            ((Tuple) e).setContext(ctx);
            s = ((Tuple) e).getInternalElts();
        } else if (e instanceof Lambda) {
            expr_name = "lambda";
        } else if (e instanceof Call) {
            expr_name = "function call";
        } else if (e instanceof BoolOp || e instanceof BinOp || e instanceof UnaryOp) {
            expr_name = "operator";
        } else if (e instanceof GeneratorExp) {
            expr_name = "generator expression";
        } else if (e instanceof Yield || e instanceof YieldFrom) {
            expr_name = "yield expression";
        } else if (e instanceof Await) {
            expr_name = "await expression";
        } else if (e instanceof ListComp) {
            expr_name = "list comprehension";
        } else if (e instanceof SetComp) {
            expr_name = "set comprehension";
        } else if (e instanceof DictComp) {
            expr_name = "dict comprehension";
        } else if (e instanceof Dict ||
                e instanceof Set ||
                e instanceof  Num ||
                e instanceof Str ||
                e instanceof Bytes ||
                e instanceof JoinedStr ||
                e instanceof FormattedValue) {
            expr_name = "literal";
        } else if (e instanceof NameConstant) {
            expr_name = "keyword";
        } else if (e instanceof Ellipsis) {
            expr_name = "Ellipsis";
        } else if (e instanceof Compare) {
            expr_name = "comparison";
        } else if (e instanceof IfExp) {
            expr_name = "conditional expression";
        } else {
            throw Py.SystemError(String.format("unexpected expression in argument %s (line %d)", e.getClass().getName(), e.getLineno()));
        }
        /* Check for error string set by switch (if/else if) */
        if (expr_name != null) {
            throw ast_error(c, n, String.format("can't %s %s", ctx == expr_contextType.Store ? "assign to" : "delete", expr_name));
        }
        /* If the LHS is a list or tuple, we need to set the assignment
           context for all the contained elements.
        */
        if (s != null) {
            for (int i = 0; i < s.size(); i++) {
                if (!set_context(c, s.get(i), ctx, n)) {
                    return false;
                }
            }
        }
        return true;
    }

    /* Count the number of 'if' statements in a comprehension.

       Helper for ast_for_comprehension().
    */
    private static int count_comp_ifs(compiling c, Node n) {
        int n_ifs = 0;
        for(;;) {
            REQ(n, COMP_ITER);
            if (n.child(0).type() == COMP_FOR) {
                return n_ifs;
            }
            n = n.child(0);
            REQ(n, COMP_IF);
            n_ifs++;
            if (n.nch() == 2) {
                return n_ifs;
            }
            n = n.child(2);
        }
    }

    static int count_comp_fors(compiling c, Node n) {
        int n_fors = 0;
        count_comp_for:
        for (;;) {
            n_fors++;
            REQ(n, COMP_FOR);
            if (n.nch() == 2) {
                REQ(n.child(0), NAME);
                assert (n.child(0).str().equals("async"));
                n = n.child(1);
            } else if (n.unary()) {
                n = n.child(0);
            } else {
                throw Py.SystemError("logic error in count_comp_fors");
            }
            if (n.nch() == 5) {
                n = n.child(4);
            } else {
                return n_fors;
            }
            count_comp_iter:
            for (;;) {
                REQ(n, COMP_ITER);
                n = n.child(0);
                if (n.type() == COMP_FOR) {
                    continue count_comp_for;
                } else if (n.type() == COMP_IF) {
                    if (n.nch() == 3) {
                        n = n.child(2);
                        continue count_comp_iter;
                    } else {
                        return n_fors;
                    }
                }
            }
        }
    }

    static expr ast_for_dictdisplay(compiling c, Node n) {
        int i, j, size;
        expr[] keys;
        expr[] values;
        size = (n.nch() + 1) / 2; /* +1 in case no trailing comma */
        keys = new expr[size];
        values = new expr[size];
        for (i = 0, j= 0; i < n.nch(); i++) {
            dictelement del = ast_for_dictelement(c, n, i);
            keys[j] = del.key;
            values[j] = del.value;
            i = del.i;
            j++;
        }
        return new Dict(n, asList(keys), asList(values));
    }

    static class dictelement {
        expr key, value;
        int i;
    }

    static dictelement ast_for_dictelement(compiling c, Node n, int i) {
        dictelement ret = new dictelement();
        expr expression;
        if (n.child(i).type() == DOUBLESTAR) {
            assert n.nch() - i >= 2;
            expression = ast_for_expr(c, n.child(i+1));
            ret.key = null;
            ret.value = expression;
            ret.i = i + 2;
        } else {
            assert n.nch() >= i + 3;
            expression = ast_for_expr(c, n.child(i));
            ret.key = expression;
            REQ(n.child(i+1), COLON);
            expression = ast_for_expr(c, n.child(i+2));
            ret.value = expression;
            ret.i = i+3;
        }
        return ret;
    }

    static expr ast_for_dictcomp(compiling c, Node n) {
        List<comprehension> comps;
        dictelement el = ast_for_dictelement(c, n, 0);
        int i = el.i;
        comps = ast_for_comprehension(c, n.child(i));
        return new DictComp(n, el.key, el.value, comps);
    }

    static expr ast_for_itercomp(compiling c, Node n, Comp type) {
        /* testlist_comp: (test|star_expr)
         *                ( comp_for | (',' (test|star_expr))* [','] ) */
        expr elt;
        List<comprehension> comps;
        Node ch;
        assert n.nch() > 1;

        ch = n.child(0);
        elt = ast_for_expr(c, ch);
        if (elt instanceof Starred) {
            throw ast_error(c, ch, "iterable unpacking cannot be used in comprehension");
        }
        comps = ast_for_comprehension(c, n.child(1));

        switch (type) {
            case GENEXP:
                return new GeneratorExp(n, elt, comps);
            case LISTCOMP:
                return new ListComp(n, elt, comps);
            default:
                assert type == Comp.SETCOMP;
                return new SetComp(n, elt, comps);
        }
    }

    static expr ast_for_genexp(compiling c, Node n) {
        assert n.type() == TESTLIST_COMP || n.type() == ARGUMENT;
        return ast_for_itercomp(c, n, Comp.GENEXP);
    }

    static expr ast_for_listcomp(compiling c, Node n) {
        assert n.type() == TESTLIST_COMP;
        return ast_for_itercomp(c, n, Comp.LISTCOMP);
    }

    static expr ast_for_setcomp(compiling c, Node n) {
        assert n.type() == DICTORSETMAKER;
        return ast_for_itercomp(c, n, Comp.SETCOMP);
    }

    static expr ast_for_setdisplay(compiling c, Node n) {
        int i;
        int size;
        expr[] elts;
        assert n.type() == DICTORSETMAKER;
        size = (n.nch() + 1) / 2; /* +1 in case no trailing comma */
        elts = new expr[size];
        for (i = 0; i < n.nch(); i+=2) {
            expr expression = ast_for_expr(c, n.child(i));
            elts[i/2]  =expression;
        }
        return new Set(n, asList(elts));
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
                    switch (s) {
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
                return new Name(n, name, Load);
            case STRING:
                expr str = actions.parsestrplus(n);
                if (str == null) {
                    // TODO
                }
                return str;
            case NUMBER:
                PyObject pynum = parsenumber(c, ch.str());
                return new Num(ch, pynum);
            case ELLIPSIS:
                return new Ellipsis(n);
            case LPAR: /* some parenthesized expression */
                ch = n.child(1);
                if (ch.type() == RPAR) {
                    return new Tuple(n, null, Load);
                }
                if (ch.type() == YIELD_EXPR) {
                    return ast_for_expr(c, ch);
                }
                if (ch.nch() > 1 && ch.child(1).type() == COMP_FOR) {
                    return ast_for_genexp(c, ch);
                }
                return ast_for_testlist(c, ch);
            case LSQB: /* list (or list comprehension) */
                ch = n.child(1);
                if (ch.type() == RSQB) {
                    return new org.python.antlr.ast.List(n, null, Load);
                }
                REQ(ch, TESTLIST_COMP);
                if (ch.unary() || ch.child(1).type() == COMMA) {
                    List<expr> elts = seq_for_testlist(c, ch);
                    return new org.python.antlr.ast.List(n, elts, Load);
                }
                return ast_for_listcomp(c, ch);
            case LBRACE:
                /* dictorsetmaker: ( ((test ':' test | '**' test)
                 *                    (comp_for | (',' (test ':' test | '**' test))* [','])) |
                 *                   ((test | '*' test)
                 *                    (comp_for | (',' (test | '*' test))* [','])) ) */
                expr res;
                ch = n.child(1);
                if (ch.type() == RBRACE) {
                    /* it's an empty dict. */
                    return new Dict(n, null, null);
                }
                boolean isDict = ch.child(0).type() == DOUBLESTAR;
                int delta = 3;
                if (isDict) delta--;
                if (ch.unary() || (ch.nch() > 1 && ch.child(1).type() == COMMA)) {
                    /* It's a set display. */
                    res = ast_for_setdisplay(c, ch);
                } else if (ch.nch() > 1 && ch.child(1).type() == COMP_FOR) {
                    /* It's a set comprehension. */
                    res = ast_for_setcomp(c, ch);
                } else if (ch.nch() > delta && ch.child(delta).type() == COMP_FOR) {
                    /* It's a dictionary comprehension */
                    if (isDict) {
                        throw ast_error(c, n, "dict unpacking cannot be used in dict comprehension");
                    }
                    res = ast_for_dictcomp(c, ch);
                } else {
                    /* It's a dictionary display. */
                    res = ast_for_dictdisplay(c, ch);
                }
                if (res != null) {
                    // XXX set lineno and coloffset to n, it's from ch at the moment
                }
                return res;
            default:
                throw Py.SystemError(String.format("unhandled atom %d", ch.type()));
        }
    }

    private static PyObject parsenumber(compiling c, String s) {
        if (s.indexOf('_') < 0) {
            return parsenumber_raw(c, s);
        }
        StringBuilder sb = new StringBuilder(s.length());
        for (char ch : s.toCharArray()) {
            if (ch != '_') {
                sb.append(ch);
            }
        }
        return parsenumber_raw(c, sb.toString());
    }

    private static PyObject parsenumber_raw(compiling c, String s) {
        char lastChar = s.charAt(s.length() - 1);
        boolean imflag = lastChar == 'j' || lastChar == 'J';
        if (s.charAt(0) == '0') {
            char ch = s.charAt(1);
            switch (ch) {
                case 'x':
                case 'X':
                    return actions.makeInt(s.substring(2), 16);
                case 'b':
                case 'B':
                    return actions.makeInt(s.substring(2), 2);
                case 'o':
                case 'O':
                    return actions.makeInt(s.substring(2), 8);
                default:
                    return actions.makeInt(s.substring(1), 8);
            }
        }
        if (imflag) {
            return actions.makeComplex(s);
        }
        if (s.indexOf('.') >= 0) {
            return actions.makeFloat(s);
        }
        return actions.makeInt(s, 10);
    }

    static slice ast_for_slice(compiling c, Node n) {
        Node ch;
        expr lower = null, upper = null, step = null;

        REQ(n, SUBSCRIPT);

        /*
           subscript: test | [test] ':' [test] [sliceop]
           sliceop: ':' [test]
        */
        ch = n.child(0);
        if (n.unary() && ch.type() == TEST) {
            /* 'step' variable hold no significance in terms of being used over
               other vars */
            step = ast_for_expr(c, ch);
            return new Index(ch, step);
        }

        if (ch.type() == TEST) {
            lower = ast_for_expr(c, ch);
        }

        /* If there's an upper bound it's in the second or third position. */
        if (ch.type() == COLON) {
            if (n.nch() > 1) {
                Node n2 = n.child(1);
                if (n2.type() == TEST) {
                    upper = ast_for_expr(c, n2);
                }
            }
        } else if (n.nch() > 2) {
            Node n2 = n.child(2);
            if (n2.type() == TEST) {
                upper = ast_for_expr(c, n2);
            }
        }
        ch = n.child(n.nch() - 1);
        if (ch.type() == SLICEOP) {
            if (!ch.unary()) {
                ch = ch.child(1);
                if (ch.type() == TEST) {
                    step = ast_for_expr(c, ch);
                }
            }
        }
        return new Slice(n, lower, upper, step);
    }

    static expr ast_for_binop(compiling c, Node n) {
        /* Must account for a sequence of expressions.
           How should A op B op C by represented?
           BinOp(BinOp(A, op, B), op, C).
        */
        int i, nops;
        expr expr1, expr2, result;
        operatorType newoperator;

        expr1 = ast_for_expr(c, n.child(0));
        expr2 = ast_for_expr(c, n.child(2));

        newoperator = get_operator(n.child(1));

        result = new BinOp(n, expr1, newoperator, expr2);

        nops = (n.nch() - 1) / 2;
        for (i = 1; i < nops; i++) {
            expr tmp_result, tmp;
            Node next_oper = n.child(i * 2 + 1);

            newoperator = get_operator(next_oper);
            tmp = ast_for_expr(c, n.child(i*2+2));
            tmp_result = new BinOp(next_oper, result, newoperator, tmp);
            result = tmp_result;
        }
        return result;
    }

    static expr ast_for_trailer(compiling c, Node n, expr left_expr) {
        /* trailer: '(' [arglist] ')' | '[' subscriptlist ']' | '.' NAME
           subscriptlist: subscript (',' subscript)* [',']
           subscript: '.' '.' '.' | test | [test] ':' [test] [sliceop]
        */
        REQ(n, TRAILER);
        switch (n.child(0).type()) {
            case LPAR:
                if (n.nch() == 2) {
                    return new Call(n, left_expr, null, null);
                }
                return ast_for_call(c, n.child(1), left_expr, true);
            case DOT:
                String attrId = n.child(1).str();
                return new Attribute(n, left_expr, attrId, Load);
            default:
                REQ(n.child(0), LSQB);
                REQ(n.child(2), RSQB);
                n = n.child(1);
                if (n.unary()) {
                    slice slc = ast_for_slice(c, n.child(0));
                    return new Subscript(n, left_expr, slc, Load);
                }
                /* The grammar is ambiguous here. The ambiguity is resolved
                   by treating the sequence as a tuple literal if there are
                   no slice features.
                */
                int j;
                slice slc;
                expr e;
                int simple = 1;
                slice[] slices;
                expr[] elts;
                slices = new slice[(n.nch() + 1) / 2];
                for (j = 0; j < n.nch(); j+=2) {
                    slc = ast_for_slice(c, n.child(j));
                    if (slc instanceof Index) {
                        simple = 0;
                    }
                    slices[j/2] = slc;
                }
                /* extract Index values and put them in a Tuple */
                elts = new expr[slices.length];
                for (j = 0; j < slices.length; j++) {
                    slc = slices[j];
                    elts[j] = ((Index) slc).getInternalValue();
                }
                e = new Tuple(n, asList(elts), Load);
                return new Subscript(n, left_expr, new Index(n, e), Load);
        }
    }

    static expr ast_for_factor(compiling c, Node n) {
        expr expression = ast_for_expr(c, n.child(1));
        switch (n.child(0).type()) {
            case PLUS:
                return new UnaryOp(n, unaryopType.UAdd, expression);
            case MINUS:
                return new UnaryOp(n, unaryopType.USub, expression);
            case TILDE:
                return new UnaryOp(n, unaryopType.Invert, expression);
        }
        throw Py.SystemError(String.format("unhandled factor: %d", n.child(0).type()));
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
            Node ch = n.child(i);
            if (ch.type() != TRAILER) {
                break;
            }
            tmp = ast_for_trailer(c, ch, e);
//            tmp.setLine(e.getLine());
//            tmp.setColoffset(e.getCol_offset());
            e = tmp;
        }

        if (start > 0) {
            /* there was an 'await' */
            return new Await(n, e);
        }
        return e;
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
        return new Starred(n, tmp, Load);
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
        expr[] seq;
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
                    seq = new expr[(n.nch() + 1) / 2];
                    for (i = 0; i < n.nch(); i+=2) {
                        expr e = ast_for_expr(c, n.child(i));
                        seq[i/2] = e;
                    }
                    if (n.child(1).str().equals("and")) {
                        return new BoolOp(n, boolopType.And, asList(seq));
                    }
                    assert(n.child(1).str().equals("or"));
                    return new BoolOp(n, boolopType.Or, asList(seq));
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
                    cmpopType[] ops = new cmpopType[n.nch() / 2];
                    expr[] cmps = new expr[n.nch() / 2];
                    for (i = 1; i < n.nch(); i+=2) {
                        cmpopType newoperator = ast_for_comp_op(c, n.child(i));
                        expression = ast_for_expr(c, n.child(i+1));
                        ops[i/2] = newoperator;
                        cmps[i/2] = expression;
                    }
                    expression = ast_for_expr(c, n.child(0));
                    return new Compare(n, expression, asList(ops), asList(cmps));
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

    static expr ast_for_call(compiling c, Node n, expr func, boolean allowgen) {
        /*
          arglist: argument (',' argument)*  [',']
          argument: ( test [comp_for] | '*' test | test '=' test | '**' test )
        */
        int i, nargs, nkeywords;
        int ndoublestars;
        expr[] args;
        keyword[] keywords;

        REQ(n, ARGLIST);
        nargs = 0;
        nkeywords = 0;
        for (i = 0; i < n.nch(); i++) {
            Node ch = n.child(i);
            if (ch.type() == ARGUMENT) {
                if (ch.unary()) {
                    nargs++;
                } else if (ch.child(1).type() == COMP_FOR) {
                    nargs++;
                    if (!allowgen) {
                        throw ast_error(c, ch, "invalid syntax");
                    }
                    if (n.nch() > 1) {
                        throw ast_error(c, ch, "Generator expression must be parenthesized");
                    }
                } else if (n.child(0).type() == STAR) {
                    nargs++;
                } else {
                    /* TYPE(CHILD(ch, 0)) == DOUBLESTAR or keyword argument */
                    nkeywords++;
                }
            }
        }

        args = new expr[nargs];
        keywords = new keyword[nkeywords];
        nargs = 0; /* positional argumetns + iterable argument unpacking */
        nkeywords = 0; /* keyword argumetns + keyword argument unpacking */
        ndoublestars = 0; /* just keyword argument unpacking */

        for (i = 0; i < n.nch(); i++) {
            Node ch = n.child(i);
            if (ch.type() == ARGUMENT) {
                expr e;
                Node chch = ch.child(0);
                if (ch.unary()) {
                    /* a positional argument */
                    if (nkeywords > 0) {
                        if (ndoublestars > 0) {
                            throw ast_error(c, chch, "positional argument follows keyword argument unpacking");
                        }
                        throw ast_error(c, chch, "positional argument follows keyword argument");
                    }
                    e = ast_for_expr(c, chch);
                    args[nargs++] = e;
                } else if (chch.type() == STAR) {
                    /* an iterable argument unpacking */
                    expr starred;
                    if (ndoublestars > 0) {
                        throw ast_error(c, chch, "iterable argument unpacking follows keyword argument unpacking");
                    }
                    e = ast_for_expr(c, ch.child(1));
                    starred = new Starred(chch, e, Load);
                    args[nargs++] = starred;
                } else if (chch.type() == DOUBLESTAR) {
                    /* a keyword argument unpacking */
                    keyword kw;
                    i++;
                    e = ast_for_expr(c, ch.child(1));
                    kw = new keyword(ch, null, e);
                    keywords[nkeywords++] = kw;
                    ndoublestars++;
                } else if (ch.child(1).type() == COMP_FOR) {
                    /* the lone generator expression */
                    e = ast_for_genexp(c, ch);
                    args[nargs++] = e;
                } else {
                    /* a keyword argument */
                    keyword kw;
                    String key, tmp;
                    int k;
                    /* chch is test, but must be an identifier? */
                    e = ast_for_expr(c, chch);
                    /* f(lambda x: x[0] = 3) ends up getting parsed with
                     * LHS test = lambda x: x[0], and RHS test = 3.
                     * SF bug 132313 points out that complaining about a keyword
                     * then is very confusing.
                     */
                    if (e instanceof Lambda) {
                        throw ast_error(c, chch, "lambda cannot contain assignment");
                    } else if (e instanceof Name) {
                        throw ast_error(c, chch, "keyword can't be an expression");
                    } else if (forbiddenName(c, ((Name)e).getInternalId(), ch, true)) {
                        return null;
                    }

                    key = ((Name)e).getInternalId();
                    for (k = 0; k < nkeywords; k++) {
                        tmp = keywords[k].getInternalArg();
                        if (tmp != null && key.equals(tmp)) {
                            throw ast_error(c, chch, "keyword argument repeated");
                        }
                    }
                    e = ast_for_expr(c, ch.child(2));
                    kw = new keyword(ch.child(2), key, e);
                    keywords[nkeywords++] = kw;
                }
            }
        }
        return new Call(func, func, asList(args), asList(keywords));
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
        return new Tuple(n, tmp, Load);
    }

    static stmt ast_for_expr_stmt(compiling c, Node n) {
        REQ(n, EXPR_STMT);
        /* expr_stmt: testlist_star_expr (annassign | augassign (yield_expr|testlist) |
                        ('=' (yield_expr|testlist_star_expr))*)
           annassign: ':' test ['=' test]
           testlist_star_expr: (test|star_expr) (',' test|star_expr)* [',']
           augassign: '+=' | '-=' | '*=' | '@=' | '/=' | '%=' | '&=' | '|=' | '^='
                    | '<<=' | '>>=' | '**=' | '//='
           test: ... here starts the operator precedence dance
        */
        if (n.nch() == 1) {
            expr e = ast_for_testlist(c, n.child(0));
            return new Expr(n, e);
        } else if (n.child(1).type() == AUGASSIGN) {
            expr expr1, expr2;
            operatorType newoperator;
            Node ch = n.child(0);

            expr1 = ast_for_testlist(c, ch);
            set_context(c, expr1, expr_contextType.Store, ch);
            /* set_context checks that most expressions are not the left side.
              Augmented assignments can only have a name, a subscript, or an
              attribute on the left, though, so we have to explicitly check for
              those. */
            if (!(expr1 instanceof Name || expr1 instanceof Attribute || expr1 instanceof Subscript)) {
                throw ast_error(c, ch, "illegal expression for augumented assignment");
            }

            ch = n.child(2);
            if (ch.type() == TESTLIST) {
                expr2 = ast_for_testlist(c, ch);
            } else {
                expr2 = ast_for_expr(c, ch);
            }

            newoperator = ast_for_augassign(c, n.child(1));
            return new AugAssign(n, expr1, newoperator, expr2);
        } else if (n.child(1).type() == ANNASSIGN) {
            expr expr1, expr2, expr3;
            Node ch = n.child(0);
            Node deep, ann = n.child(1);
            int simple = 1;

            /* we keep track of parens to qualify (x) as expression not name */
            deep = ch;
            while (deep.unary()) {
                deep = deep.child(0);
            }
            if (deep.nch() > 0 && deep.child(0).type() == LPAR) {
                simple = 0;
            }
            expr1 = ast_for_testlist(c, ch);
            if (expr1 instanceof Name) {
                if (forbiddenName(c, ((Name)expr1).getInternalId(), n, false)) {
                    return null;
                }
                ((Name) expr1).setContext(expr_contextType.Store);
            } else if (expr1 instanceof Attribute) {
                if (forbiddenName(c, ((Attribute) expr1).getInternalAttr(), n, true)) {
                    return null;
                }
                ((Attribute) expr1).setContext(expr_contextType.Store);
            } else if (expr1 instanceof Subscript) {
                ((Subscript) expr1).setContext(expr_contextType.Store);
            } else if (expr1 instanceof org.python.antlr.ast.List) {
                throw ast_error(c, ch, "only single target (not list) can be annotated");
            } else if (expr1 instanceof Tuple) {
                throw ast_error(c, ch, "only single target (not tuple) can be annotated");
            } else {
                throw ast_error(c, ch, "illegal target for annotation");
            }

            if (!(expr1 instanceof Name)) {
                simple = 0;
            }
            ch = ann.child(1);
            expr2 = ast_for_expr(c, ch);
            if (ann.nch() == 2) {
                return new AnnAssign(n, expr1, expr2, null, simple);
            }
            ch = ann.child(3);
            expr3 = ast_for_expr(c, ch);
            return new AnnAssign(n, expr1, expr2, expr3, simple);
        } else {
            int i;
            expr[] targets;
            Node value;
            expr expression;

            /* a normal assignment */
            REQ(n.child(1), EQUAL);
            targets = new expr[n.nch() / 2];
            for (i = 0; i < n.nch() - 2; i+=2) {
                expr e;
                Node ch = n.child(i);
                if (ch.type() == YIELD_EXPR) {
                    throw ast_error(c, ch, "assignment to yield expression not possible");
                }
                e = ast_for_testlist(c, ch);

                /* set context to assign */
                set_context(c, e, expr_contextType.Store, n.child(i));
                targets[i/2] = e;
            }
            value = n.child(n.nch() - 1);
            if (value.type() == TESTLIST_STAR_EXPR) {
                expression = ast_for_testlist(c, value);
            } else {
                expression = ast_for_expr(c, value);
            }
            return new Assign(n, asList(targets), expression);
        }
    }

    static operatorType get_operator(final Node n) {
        switch (n.type()) {
            case VBAR:
                return operatorType.BitOr;
            case CIRCUMFLEX:
                return operatorType.BitXor;
            case AMPER:
                return operatorType.BitAnd;
            case LEFTSHIFT:
                return operatorType.LShift;
            case RIGHTSHIFT:
                return operatorType.RShift;
            case PLUS:
                return operatorType.Add;
            case MINUS:
                return operatorType.Sub;
            case STAR:
                return operatorType.Mult;
            case AT:
                return operatorType.MatMult;
            case SLASH:
                return operatorType.Div;
            case DOUBLESLASH:
                return operatorType.FloorDiv;
            case PERCENT:
                return operatorType.Mod;
            default:
                throw new RuntimeException("unknown operator");
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

    private static <T> List<T> asList(T[] elts) {
        if (elts == null) {
            return List.of();
        }
        return List.of(elts);
    }
}
