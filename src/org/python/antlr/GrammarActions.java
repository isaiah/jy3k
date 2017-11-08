package org.python.antlr;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.python.antlr.ast.*;
import org.python.antlr.base.excepthandler;
import org.python.antlr.base.expr;
import org.python.antlr.base.slice;
import org.python.antlr.base.stmt;
import org.python.core.*;
import org.python.core.stringlib.Encoding;
import org.python.core.util.StringUtil;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class GrammarActions {
    private ErrorHandler errorHandler = null;
    public GrammarActions() {
    }

    public void setErrorHandler(ErrorHandler eh) {
        this.errorHandler = eh;
    }

    String makeFromText(List dots, List<Name> names) {
        StringBuilder d = new StringBuilder();
        d.append(PythonTree.dottedNameListToString(names));
        return d.toString();
    }

    List<Name> makeModuleNameNode(List dots, List<Name> names) {
        List<Name> result = new ArrayList<Name>();
        if (dots != null) {
            for (Object o : dots) {
                Token tok = (Token)o;
                result.add(new Name(tok, tok.getText(), expr_contextType.Load));
            }
        }
        if (null != names) {
        	result.addAll(names);
        }
        return result;
    }

    List<Name> makeDottedName(Token top, List<PythonTree> attrs) {
      List<Name> result = new ArrayList<Name>();
      result.add(new Name(top, top.getText(), expr_contextType.Load));
      if (attrs != null) {
        for (PythonTree attr : attrs) {
          Token token = attr.getToken();
          result.add(new Name(token, token.getText(), expr_contextType.Load));
        }
      }
      return result;
    }

    int makeLevel(List lev) {
        if (lev == null) {
            return 0;
        }
        return lev.size();
    }

    List<alias> makeStarAlias(Token t) {
        List<alias> result = new ArrayList<alias>();
        result.add(new alias(t, "*", null));
        return result;
    }

    List<alias> makeAliases(List<alias> atypes) {
        if (atypes == null) {
            return new ArrayList<alias>();
        }
        return atypes;
    }

    List<expr> makeBases(expr etype) {
        List<expr> result = new ArrayList<expr>();
        if (etype != null) {
            if (etype instanceof Tuple) {
                return ((Tuple)etype).getInternalElts();
            }
            result.add(etype);
        }
        return result;
    }

    List<String> makeNames(List<TerminalNode> names) {
        List<String> s = new ArrayList<String>();
        for(int i=0;i<names.size();i++) {
            s.add(names.get(i).getText());
        }
        return s;
    }

    void errorGenExpNotSoleArg(PythonTree t) {
        errorHandler.error("Generator expression must be parenthesized if not sole argument", t);
    }

    void errorPositionalArgFollowsKeywordArg(PythonTree t) {
        errorHandler.error("positional argument follows keyword argument", t);
    }

    void errorNoNamedArguments(PythonTree t) {
        errorHandler.error("named arguments must follow bare *", t);
    }

    arg castArg(Object o) {
        if (o instanceof arg) {
            return (arg)o;
        } else if (o instanceof Name) {
            Name name = (Name) o;
            return new arg(name.getToken(), name.getInternalId(), null);
        }
        return null;
    }

    List<arg> castArgs(List<Object> args) {
        List<arg> ret = new ArrayList<>();
        if (args == null) {
            return ret;
        }
        for (Object o : args) {
            ret.add(castArg(o));
        }
        return ret;
    }

    expr castExpr(Object o) {
        if (o instanceof expr) {
            return (expr)o;
        }
        if (o instanceof PythonTree) {
            return errorHandler.errorExpr((PythonTree)o);
        }
        return null;
    }


    List<expr> castExprs(List exprs) {
        return castExprs(exprs, 0);
    }

    List<expr> castExprs(List exprs, int start) {
        List<expr> result = new ArrayList<expr>();
        if (exprs != null) {
            for (int i=start; i<exprs.size(); i++) {
                Object o = exprs.get(i);
                if (o instanceof expr) {
                    result.add((expr)o);
                } else if (o instanceof PythonParser.TestContext) {
//                    result.add((expr)((PythonParser.TestContext)o).testExpr);
                }
            }
        }
        return result;
    }

    List<expr> castExprs(List exprs1, List exprs2) {
        List exprs = exprs1;
        if (exprs == null) {
            exprs = exprs2;
        } else if (exprs2 != null) {
            exprs.addAll(exprs2);
        }
        return castExprs(exprs);
    }

    List<stmt> makeElse(List elseSuite, PythonTree elif) {
        if (elseSuite != null) {
            return castStmts(elseSuite);
        } else if (elif == null) {
            return new ArrayList<stmt>();
        }
        List <stmt> s = new ArrayList<stmt>();
        s.add(castStmt(elif));
        return s;
    }

    stmt castStmt(Object o) {
        if (o instanceof stmt) {
            return (stmt)o;
//        } else if (o instanceof PythonParser.stmt_return) {
//            return (stmt)((PythonParser.stmt_return)o).tree;
        } else if (o instanceof PythonTree) {
            return errorHandler.errorStmt((PythonTree)o);
        }
        return null;
    }

    List<stmt> castStmts(PythonTree t) {
        stmt s = (stmt)t;
        List<stmt> stmts = new ArrayList<stmt>();
        stmts.add(s);
        return stmts;
    }

    List<stmt> castStmts(List stmts) {
        if (stmts != null) {
            List<stmt> result = new ArrayList<stmt>();
            for (Object o:stmts) {
                result.add(castStmt(o));
            }
            return result;
        }
        return new ArrayList<stmt>();
    }

    stmt makeAsyncWith(Token t, List<withitem> items, List<stmt> body) {
        int last = items.size() - 1;
        AsyncWith result = null;
        for (int i = last; i>=0; i--) {
            withitem current = items.get(i);
            if (i != last) {
                body = new ArrayList<stmt>();
                body.add(result);
            }
            result = new AsyncWith(current.getToken(), Arrays.asList(current), body);
        }
        return result;
    }

    stmt makeWith(Token t, List<withitem> items, List<stmt> body) {
        int last = items.size() - 1;
        With result = null;
        for (int i = last; i>=0; i--) {
            withitem current = items.get(i);
            if (i != last) {
                body = new ArrayList<stmt>();
                body.add(result);
            }
            result = new With(current.getToken(), Arrays.asList(current), body);
        }
        return result;
    }

    stmt makeAsyncFor(Token t, For forStmt) {
        return new AsyncFor(t, forStmt.getInternalTarget(), forStmt.getInternalIter(),
                forStmt.getInternalBody(), forStmt.getInternalOrelse());
    }


    stmt makeAsyncFuncdef(Token t, FunctionDef func, java.util.List<expr> decoratorList) {
        return new AsyncFunctionDef(t, func.getInternalName(), func.getInternalArgs(),
                func.getInternalBody(), decoratorList, func.getInternalReturns());
    }

    List<expr> makeAssignTargets(expr lhs, List rhs) {
        List<expr> e = new ArrayList<expr>();
        checkAssign(lhs);
        e.add(lhs);
        for(int i=0;i<rhs.size() - 1;i++) {
            expr r = castExpr(rhs.get(i));
            checkAssign(r);
            e.add(r);
        }
        return e;
    }

    List<keyword> makeKeywords(List args) {
        List<keyword> keywords = new ArrayList<keyword>();
        if (args != null) {
            for (Object o : args) {
                List e = (List)o;
                Object k = e.get(0);
                Object v = e.get(1);
                checkAssign(castExpr(k));
                if (k instanceof Name) {
                    Name arg = (Name) k;
                    keywords.add(new keyword(arg, arg.getInternalId(), castExpr(v)));
                } else if (k == null) {
                    keywords.add(new keyword(null, castExpr(v)));
                } else {
                    errorHandler.error("keyword must be a name", (PythonTree)k);
                }
            }
        }
        return keywords;
    }

    Object makeFloat(TerminalNode t) {
        return Py.newFloat(Double.valueOf(t.getText().replaceAll("_", "")));
    }

    Object makeComplex(TerminalNode t) {
        String s = t.getText().replaceAll("_", "");
        s = s.substring(0, s.length() - 1);
        return Py.newImaginary(Double.valueOf(s));
    }

    Object makeInt(TerminalNode t, int radix) {
        return new PyLong(new BigInteger(t.getText().substring(2).replaceAll("_", ""), radix));
    }

    Object makeDecimal(TerminalNode t) {
        return new PyLong(new BigInteger(t.getText().replaceAll("_", ""), 10));
    }

    expr parsestrplus(List<PythonParser.StrContext> s) {
        boolean bytesmode = false;
        StringBuilder bytesStr = new StringBuilder();
        FstringParser state = new FstringParser(s.get(0).getStart());
        int i = 0;
        for (PythonParser.StrContext last : s) {
            boolean this_bytesmode = false;
            ParseStrResult ret = parsestr(last);
            this_bytesmode = ret.bytesmode;
            /* Check that we're not mixing bytes with unicode. */
            if (i++ != 0 && bytesmode != this_bytesmode) {
                // TODO ast error "cannot mix bytes and nonbytes literals"
                throw Py.SyntaxError("cannot mix bytes and nonbytes literals");
            }
            bytesmode = this_bytesmode;
            if (ret.fstr != null) {
                assert(ret.s == null && !bytesmode);
                /* This is an f-string. Parse and concatenate it. */
                state.concatFstring(ret.fstr, ret.fstrlen, 0, ret.rawmode);
            } else {
                /* A string or byte string. */
                if (bytesmode) {
                    /* For bytes, concat as we go. */
                    bytesStr.append(ret.s);
                } else {
                    /* This is a regular string. Concatenate it. */
                    state.concat(ret.s);
                }
            }
        }

        if (bytesmode) {
            return new Bytes(extractStringToken(s), bytesStr.toString());
        }

        return state.finish();
    }

    class ParseStrResult {
        boolean bytesmode;
        boolean rawmode;
        String s;
        String fstr;
        int fstrlen;
    }

    ParseStrResult parsestr(PythonParser.StrContext t) {
        int len;
        String s = t.getText();
        int start = 0;
        char quoteChar = s.charAt(start);
        ParseStrResult ret = new ParseStrResult();
        boolean fmode = false;
        ret.bytesmode = false;
        ret.rawmode = false;
        if (Character.isAlphabetic(quoteChar)) {
            while (!ret.bytesmode || !ret.rawmode) {
                if (quoteChar == 'b' || quoteChar == 'B') {
                    quoteChar = s.charAt(++start);
                    ret.bytesmode = true;
                } else if (quoteChar == 'u' || quoteChar == 'U') {
                    quoteChar = s.charAt(++start);
                } else if (quoteChar == 'r' || quoteChar == 'R') {
                    quoteChar = s.charAt(++start);
                    ret.rawmode = true;
                } else if (quoteChar == 'f' || quoteChar == 'F') {
                    quoteChar = s.charAt(++start);
                    fmode = true;
                } else {
                    break;
                }
            }
        }

        if (fmode && ret.bytesmode) {
            throw new RuntimeException("f with b mode");
        }
        if (quoteChar != '\'' && quoteChar != '"') {
            throw new RuntimeException("parsestr error: " + quoteChar);
        }
        // skip the leading quote char
        start++;
        len = s.length();
        if (s.charAt(--len) != quoteChar) {
            throw new RuntimeException("invalid str");
        }
        if (s.length() > 4 && s.charAt(start) == quoteChar && s.charAt(start + 1) == quoteChar) {
            /* A triple quoted string. We've already skipped one quote at
            the start and one at the end of the string. Now skip the
            two at the start. */
            start += 2;
            /* And check that the last two match. */
            if (s.charAt(--len) != quoteChar || s.charAt(--len) != quoteChar) {
                throw new RuntimeException("invalid triple quote str");
            }
        }

        if (fmode) {
            /* Just return the bytes. The caller will parse the resulting string. */
            ret.fstr = s.substring(start, len);
            ret.fstrlen = len;
            return ret;
        }
        // Not an f-string
        // avoid invoking escape decoding routines if possible
        ret.rawmode = ret.rawmode || s.indexOf('\\') == -1;
        ret.s = s.substring(start, len);
        if (ret.bytesmode) {
            // TODO disallow non-ASCII characters.
            if (!ret.rawmode) {
                ret.s = Encoding.decode_UnicodeEscape(ret.s, 0, len - start, "strict", false);
            }
        } else if (!ret.rawmode) {
            ret.s = Encoding.decode_UnicodeEscape(ret.s, 0, len - start, "strict", true);
        }
        return ret;
    }

    Token extractStringToken(List<PythonParser.StrContext> s) {
        return s.get(0).getStart();
        //return (Token)s.get(s.size() - 1);
    }

    void cantBeNone(PythonTree e) {
        if (e.getText().equals("None")) {
            errorHandler.error("can't be None", e);
        }
    }

    private void checkGenericAssign(expr e) {
        if (e instanceof Name && ((Name)e).getInternalId().equals("None")) {
            errorHandler.error("assignment to None", e);
        } else if (e instanceof GeneratorExp) {
            errorHandler.error("can't assign to generator expression", e);
        } else if (e instanceof Num) {
            errorHandler.error("can't assign to number", e);
        } else if (e instanceof Str) {
            errorHandler.error("can't assign to string", e);
        } else if (e instanceof Yield) {
            errorHandler.error("can't assign to yield expression", e);
        } else if (e instanceof BinOp) {
            errorHandler.error("can't assign to operator", e);
        } else if (e instanceof BoolOp) {
            errorHandler.error("can't assign to operator", e);
        } else if (e instanceof Lambda) {
            errorHandler.error("can't assign to lambda", e);
        } else if (e instanceof Call) {
            errorHandler.error("can't assign to function call", e);
        } else if (e instanceof IfExp) {
            errorHandler.error("can't assign to conditional expression", e);
        } else if (e instanceof ListComp) {
            errorHandler.error("can't assign to list comprehension", e);
        } else if (e instanceof SetComp) {
            errorHandler.error("can't assign to set comprehension", e);
        } else if (e instanceof DictComp) {
            errorHandler.error("can't assign to dict comprehension", e);
        }
     }

    void checkAugAssign(expr e) {
        checkGenericAssign(e);
        if (e instanceof Tuple) {
            errorHandler.error("assignment to tuple illegal for augmented assignment", e);
        } else if (e instanceof org.python.antlr.ast.List) {
            errorHandler.error("assignment to list illegal for augmented assignment", e);
        }
    }

    void checkAssign(expr e) {
        checkGenericAssign(e);
        if (e instanceof Tuple) {
            //XXX: performance problem?  Any way to do this better?
            List<expr> elts = ((Tuple)e).getInternalElts();
            if (elts.size() == 0) {
                errorHandler.error("can't assign to ()", e);
            }
            for (int i=0;i<elts.size();i++) {
                checkAssign(elts.get(i));
            }
        } else if (e instanceof org.python.antlr.ast.List) {
            //XXX: performance problem?  Any way to do this better?
            List<expr> elts = ((org.python.antlr.ast.List)e).getInternalElts();
            for (int i=0;i<elts.size();i++) {
                checkAssign(elts.get(i));
            }
        }
    }

    List<expr> makeDeleteList(List deletes) {
        List<expr> exprs = castExprs(deletes);
        for(expr e : exprs) {
            checkDelete(e);
        }
        return exprs;
    }

    void checkDelete(expr e) {
        if (e instanceof Call) {
            errorHandler.error("can't delete function call", e);
        } else if (e instanceof Num) {
            errorHandler.error("can't delete number", e);
        } else if (e instanceof Str) {
            errorHandler.error("can't delete string", e);
        } else if (e instanceof Tuple) {
            //XXX: performance problem?  Any way to do this better?
            List<expr> elts = ((Tuple)e).getInternalElts();
            if (elts.size() == 0) {
                errorHandler.error("can't delete ()", e);
            }
            for (int i=0;i<elts.size();i++) {
                checkDelete(elts.get(i));
            }
        } else if (e instanceof org.python.antlr.ast.List) {
            //XXX: performance problem?  Any way to do this better?
            List<expr> elts = ((org.python.antlr.ast.List)e).getInternalElts();
            for (int i=0;i<elts.size();i++) {
                checkDelete(elts.get(i));
            }
        }
    }

    List<cmpopType> makeCmpOps(List cmps) {
        List<cmpopType> result = new ArrayList<cmpopType>();
        if (cmps != null) {
            for (Object o: cmps) {
                result.add((cmpopType)o);
            }
        }
        return result;
    }

    BoolOp makeBoolOp(Token t, PythonTree left, boolopType op, List right) {
        List values = new ArrayList();
        values.add(left);
        values.addAll(right);
        return new BoolOp(t, op, castExprs(values));
    }

    BinOp makeBinOp(Token t, PythonTree left, operatorType op, List rights) {
        BinOp current = new BinOp(t, castExpr(left), op, castExpr(rights.get(0)));
        for (int i = 1; i< rights.size(); i++) {
            expr right = castExpr(rights.get(i));
            current = new BinOp(left, current, op, right);
        }
        return current;
    }

    BinOp makeBinOp(Token t, PythonTree left, List ops, List rights, List toks) {
        BinOp current = new BinOp(t, castExpr(left), (operatorType)ops.get(0), castExpr(rights.get(0)));
        for (int i = 1; i< rights.size(); i++) {
            expr right = castExpr(rights.get(i));
            operatorType op = (operatorType)ops.get(i);
            current = new BinOp((Token)toks.get(i), current, op, right);
        }
        return current;
    }

    List<slice> castSlices(List slices) {
        List<slice> result = new ArrayList<slice>();
        if (slices != null) {
            for (Object o:slices) {
                result.add(castSlice(o));
            }
        }
        return result;
    }

    slice castSlice(Object o) {
        if (o instanceof slice) {
            return (slice)o;
        }
        return errorHandler.errorSlice((PythonTree)o);
    }

    slice makeSliceType(Token begin, Token c1, Token c2, List sltypes) {
        boolean isTuple = false;
        if (c1 != null || c2 != null) {
            isTuple = true;
        }
        slice s = null;
        boolean extslice = false;

        if (isTuple) {
            List<slice> st;
            List<expr> etypes = new ArrayList<expr>();
            for (Object o : sltypes) {
                if (o instanceof Index) {
                    Index i = (Index)o;
                    etypes.add(i.getInternalValue());
                } else {
                    extslice = true;
                    break;
                }
            }
            if (!extslice) {
                expr t = new Tuple(begin, etypes, expr_contextType.Load);
                s = new Index(begin, t);
            }
        } else if (sltypes.size() == 1) {
            s = castSlice(sltypes.get(0));
        } else if (sltypes.size() != 0) {
            extslice = true;
        }
        if (extslice) {
            List<slice> st = castSlices(sltypes);
            s = new ExtSlice(begin, st);
        }
        return s;
    }
}
