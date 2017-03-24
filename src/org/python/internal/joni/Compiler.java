/*
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.python.internal.joni;

import org.jcodings.Encoding;
import org.python.internal.joni.ast.AnchorNode;
import org.python.internal.joni.ast.BackRefNode;
import org.python.internal.joni.ast.CClassNode;
import org.python.internal.joni.ast.CTypeNode;
import org.python.internal.joni.ast.CallNode;
import org.python.internal.joni.ast.ConsAltNode;
import org.python.internal.joni.ast.EncloseNode;
import org.python.internal.joni.ast.Node;
import org.python.internal.joni.ast.QuantifierNode;
import org.python.internal.joni.ast.StringNode;
import org.python.internal.joni.constants.NodeType;
import org.python.internal.joni.exception.ErrorMessages;
import org.python.internal.joni.exception.InternalException;
import org.python.internal.joni.exception.SyntaxException;

abstract class Compiler implements ErrorMessages {
    protected final Analyser analyser;
    protected final Encoding enc;
    protected final Regex regex;

    protected Compiler(Analyser analyser) {
        this.analyser = analyser;
        this.regex = analyser.regex;
        this.enc = regex.enc;
    }

    final void compile() {
        prepare();
        compileTree(analyser.root);
        finish();
    }

    protected abstract void prepare();
    protected abstract void finish();

    protected abstract void compileAltNode(ConsAltNode node);

    private void compileStringRawNode(StringNode sn) {
        if (sn.length() <= 0) return;
        addCompileString(sn.bytes, sn.p, 1 /*sb*/, sn.length(), false);
    }

    private void compileStringNode(StringNode node) {
        StringNode sn = node;
        if (sn.length() <= 0) return;

        boolean ambig = sn.isAmbig();

        int p, prev;
        p = prev = sn.p;
        int end = sn.end;
        byte[]bytes = sn.bytes;
        int prevLen = enc.length(bytes, p, end);
        p += prevLen;
        int slen = 1;

        while (p < end) {
            int len = enc.length(bytes, p, end);
            if (len == prevLen) {
                slen++;
            } else {
                addCompileString(bytes, prev, prevLen, slen, ambig);
                prev = p;
                slen = 1;
                prevLen = len;
            }
            p += len;
        }
        addCompileString(bytes, prev, prevLen, slen, ambig);
    }

    protected abstract void addCompileString(byte[]bytes, int p, int mbLength, int strLength, boolean ignoreCase);

    protected abstract void compileCClassNode(CClassNode node);
    protected abstract void compileCTypeNode(CTypeNode node);
    protected abstract void compileAnyCharNode();
    protected abstract void compileCallNode(CallNode node);
    protected abstract void compileBackrefNode(BackRefNode node);
    protected abstract void compileCECQuantifierNode(QuantifierNode node);
    protected abstract void compileNonCECQuantifierNode(QuantifierNode node);
    protected abstract void compileOptionNode(EncloseNode node);
    protected abstract void compileEncloseNode(EncloseNode node);
    protected abstract void compileAnchorNode(AnchorNode node);

    protected final void compileTree(Node node) {
        switch (node.getType()) {
        case NodeType.LIST:
            ConsAltNode lin = (ConsAltNode)node;
            do {
                compileTree(lin.car);
            } while ((lin = lin.cdr) != null);
            break;

        case NodeType.ALT:
            compileAltNode((ConsAltNode)node);
            break;

        case NodeType.STR:
            StringNode sn = (StringNode)node;
            if (sn.isRaw()) {
                compileStringRawNode(sn);
            } else {
                compileStringNode(sn);
            }
            break;

        case NodeType.CCLASS:
            compileCClassNode((CClassNode)node);
            break;

        case NodeType.CTYPE:
            compileCTypeNode((CTypeNode)node);
            break;

        case NodeType.CANY:
            compileAnyCharNode();
            break;

        case NodeType.BREF:
            compileBackrefNode((BackRefNode)node);
            break;

        case NodeType.CALL:
            if (Config.USE_SUBEXP_CALL) {
                compileCallNode((CallNode)node);
                break;
            } // USE_SUBEXP_CALL
            break;

        case NodeType.QTFR:
            if (Config.USE_COMBINATION_EXPLOSION_CHECK) {
                compileCECQuantifierNode((QuantifierNode)node);
            } else {
                compileNonCECQuantifierNode((QuantifierNode)node);
            }
            break;

        case NodeType.ENCLOSE:
            EncloseNode enode = (EncloseNode)node;
            if (enode.isOption()) {
                compileOptionNode(enode);
            } else {
                compileEncloseNode(enode);
            }
            break;

        case NodeType.ANCHOR:
            compileAnchorNode((AnchorNode)node);
            break;

        default:
            // undefined node type
            newInternalException(ERR_PARSER_BUG);
        } // switch
    }

    protected final void compileTreeNTimes(Node node, int n) {
        for (int i=0; i<n; i++) compileTree(node);
    }

    protected void newSyntaxException(String message) {
        throw new SyntaxException(message);
    }

    protected void newInternalException(String message) {
        throw new InternalException(message);
    }
}
