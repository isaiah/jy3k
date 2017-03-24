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

import static org.python.internal.joni.BitStatus.bsAt;
import static org.python.internal.joni.Option.isDynamic;
import static org.python.internal.joni.Option.isIgnoreCase;
import static org.python.internal.joni.Option.isMultiline;
import static org.python.internal.joni.ast.QuantifierNode.isRepeatInfinite;

import org.jcodings.constants.CharacterType;
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
import org.python.internal.joni.constants.AnchorType;
import org.python.internal.joni.constants.EncloseType;
import org.python.internal.joni.constants.NodeType;
import org.python.internal.joni.constants.OPCode;
import org.python.internal.joni.constants.OPSize;
import org.python.internal.joni.constants.TargetInfo;

final class ArrayCompiler extends Compiler {
    private int[]code;
    private int codeLength;

    private byte[][]templates;
    private int templateNum;

    ArrayCompiler(Analyser analyser) {
        super(analyser);
    }

    @Override
    protected final void prepare() {
        int codeSize = Config.USE_STRING_TEMPLATES ? 8 : ((analyser.getEnd() - analyser.getBegin()) * 2 + 2);
        code = new int[codeSize];
        codeLength = 0;
    }

    @Override
    protected final void finish() {
        addOpcode(OPCode.END);
        addOpcode(OPCode.FINISH); // for stack bottom

        regex.code = code;
        regex.codeLength = codeLength;
        regex.templates = templates;
        regex.templateNum = templateNum;
        regex.factory = MatcherFactory.DEFAULT;

        if (Config.USE_SUBEXP_CALL && analyser.env.unsetAddrList != null) {
            analyser.env.unsetAddrList.fix(regex);
            analyser.env.unsetAddrList = null;
        }
    }

    @Override
    protected void compileAltNode(ConsAltNode node) {
        ConsAltNode aln = node;
        int len = 0;

        do {
            len += compileLengthTree(aln.car);
            if (aln.cdr != null) {
                len += OPSize.PUSH + OPSize.JUMP;
            }
        } while ((aln = aln.cdr) != null);

        int pos = codeLength + len;  /* goal position */

        aln = node;
        do {
            len = compileLengthTree(aln.car);
            if (aln.cdr != null) {
                regex.requireStack = true;
                addOpcodeRelAddr(OPCode.PUSH, len + OPSize.JUMP);
            }
            compileTree(aln.car);
            if (aln.cdr != null) {
                len = pos - (codeLength + OPSize.JUMP);
                addOpcodeRelAddr(OPCode.JUMP, len);
            }
        } while ((aln = aln.cdr) != null);
    }

    private boolean isNeedStrLenOpExact(int op) {
        return  op == OPCode.EXACTN         ||
                op == OPCode.EXACTMB2N      ||
                op == OPCode.EXACTMB3N      ||
                op == OPCode.EXACTMBN       ||
                op == OPCode.EXACTN_IC      ||
                op == OPCode.EXACTN_IC_SB;
    }

    private boolean opTemplated(int op) {
        return isNeedStrLenOpExact(op);
    }

    private int selectStrOpcode(int mbLength, int strLength, boolean ignoreCase) {
        int op;

        if (ignoreCase) {
            switch(strLength) {
            case 1: op = enc.toLowerCaseTable() != null ? OPCode.EXACT1_IC_SB : OPCode.EXACT1_IC; break;
            default:op = enc.toLowerCaseTable() != null ? OPCode.EXACTN_IC_SB : OPCode.EXACTN_IC; break;
            } // switch
        } else {
            switch (mbLength) {
            case 1:
                switch (strLength) {
                case 1: op = OPCode.EXACT1; break;
                case 2: op = OPCode.EXACT2; break;
                case 3: op = OPCode.EXACT3; break;
                case 4: op = OPCode.EXACT4; break;
                case 5: op = OPCode.EXACT5; break;
                default:op = OPCode.EXACTN; break;
                } // inner switch
                break;
            case 2:
                switch (strLength) {
                case 1: op = OPCode.EXACTMB2N1; break;
                case 2: op = OPCode.EXACTMB2N2; break;
                case 3: op = OPCode.EXACTMB2N3; break;
                default:op = OPCode.EXACTMB2N;  break;
                } // inner switch
                break;
            case 3:
                op = OPCode.EXACTMB3N;
                break;
            default:
                op = OPCode.EXACTMBN;
            } // switch
        }
        return op;
    }

    private void compileTreeEmptyCheck(Node node, int emptyInfo) {
        int savedNumNullCheck = regex.numNullCheck;

        if (emptyInfo != 0) {
            regex.requireStack = true;
            addOpcode(OPCode.NULL_CHECK_START);
            addMemNum(regex.numNullCheck); /* NULL CHECK ID */
            regex.numNullCheck++;
        }

        compileTree(node);

        if (emptyInfo != 0) {
            switch(emptyInfo) {
            case TargetInfo.IS_EMPTY:
                addOpcode(OPCode.NULL_CHECK_END);
                break;
            case TargetInfo.IS_EMPTY_MEM:
                addOpcode(OPCode.NULL_CHECK_END_MEMST);
                break;
            case TargetInfo.IS_EMPTY_REC:
                addOpcode(OPCode.NULL_CHECK_END_MEMST_PUSH);
                break;
            } // switch

            addMemNum(savedNumNullCheck); /* NULL CHECK ID */
        }
    }

    private int addCompileStringlength(byte[]bytes, int p, int mbLength, int strLength, boolean ignoreCase) {
        int op = selectStrOpcode(mbLength, strLength, ignoreCase);
        int len = OPSize.OPCODE;

        if (Config.USE_STRING_TEMPLATES && opTemplated(op)) {
            // string length, template index, template string pointer
            len += OPSize.LENGTH + OPSize.INDEX + OPSize.INDEX;
        } else {
            if (isNeedStrLenOpExact(op)) len += OPSize.LENGTH;
            len += mbLength * strLength;
        }
        if (op == OPCode.EXACTMBN) len += OPSize.LENGTH;
        return len;
    }

    @Override
    protected final void addCompileString(byte[]bytes, int p, int mbLength, int strLength, boolean ignoreCase) {
        int op = selectStrOpcode(mbLength, strLength, ignoreCase);
        addOpcode(op);

        if (op == OPCode.EXACTMBN) addLength(mbLength);

        if (isNeedStrLenOpExact(op)) {
            if (op == OPCode.EXACTN_IC || op == OPCode.EXACTN_IC_SB) {
                addLength(mbLength * strLength);
            } else {
                addLength(strLength);
            }
        }

        if (Config.USE_STRING_TEMPLATES && opTemplated(op)) {
            addInt(templateNum);
            addInt(p);
            addTemplate(bytes);
        } else {
            addBytes(bytes, p, mbLength * strLength);
        }
    }

    private int compileLengthStringNode(Node node) {
        StringNode sn = (StringNode)node;
        if (sn.length() <= 0) return 0;
        boolean ambig = sn.isAmbig();

        int p, prev;
        p = prev = sn.p;
        int end = sn.end;
        byte[]bytes = sn.bytes;
        int prevLen = enc.length(bytes, p, end);
        p += prevLen;

        int slen = 1;
        int rlen = 0;

        while (p < end) {
            int len = enc.length(bytes, p, end);
            if (len == prevLen) {
                slen++;
            } else {
                int r = addCompileStringlength(bytes, prev, prevLen, slen, ambig);
                rlen += r;
                prev = p;
                slen = 1;
                prevLen = len;
            }
            p += len;
        }
        int r = addCompileStringlength(bytes, prev, prevLen, slen, ambig);
        rlen += r;
        return rlen;
    }

    private int compileLengthStringRawNode(StringNode sn) {
        if (sn.length() <= 0) return 0;
        return addCompileStringlength(sn.bytes, sn.p, 1 /*sb*/, sn.length(), false);
    }

    private void addMultiByteCClass(CodeRangeBuffer mbuf) {
        addLength(mbuf.used);
        addInts(mbuf.p, mbuf.used);
    }

    private int compileLengthCClassNode(CClassNode cc) {
        if (cc.isShare()) return OPSize.OPCODE + OPSize.POINTER;

        int len;
        if (cc.mbuf == null) {
            len = OPSize.OPCODE + BitSet.BITSET_SIZE;
        } else {
            if (enc.minLength() > 1 || cc.bs.isEmpty()) {
                len = OPSize.OPCODE;
            } else {
                len = OPSize.OPCODE + BitSet.BITSET_SIZE;
            }

            len += OPSize.LENGTH + cc.mbuf.used;
        }
        return len;
    }

    @Override
    protected void compileCClassNode(CClassNode cc) {
        if (cc.isShare()) { // shared char class
            addOpcode(OPCode.CCLASS_NODE);
            addPointer(cc);
            return;
        }

        if (cc.mbuf == null) {
            if (cc.isNot()) {
                addOpcode(enc.isSingleByte() ? OPCode.CCLASS_NOT_SB : OPCode.CCLASS_NOT);
            } else {
                addOpcode(enc.isSingleByte() ? OPCode.CCLASS_SB : OPCode.CCLASS);
            }
            addInts(cc.bs.bits, BitSet.BITSET_SIZE); // add_bitset
        } else {
            if (enc.minLength() > 1 || cc.bs.isEmpty()) {
                if (cc.isNot()) {
                    addOpcode(OPCode.CCLASS_MB_NOT);
                } else {
                    addOpcode(OPCode.CCLASS_MB);
                }
                addMultiByteCClass(cc.mbuf);
            } else {
                if (cc.isNot()) {
                    addOpcode(OPCode.CCLASS_MIX_NOT);
                } else {
                    addOpcode(OPCode.CCLASS_MIX);
                }
                // store the bit set and mbuf themself!
                addInts(cc.bs.bits, BitSet.BITSET_SIZE); // add_bitset
                addMultiByteCClass(cc.mbuf);
            }
        }
    }

    @Override
    protected void compileCTypeNode(CTypeNode node) {
        CTypeNode cn = node;
        int op;
        switch (cn.ctype) {
        case CharacterType.WORD:
            if (cn.not) {
                op = enc.isSingleByte() ? OPCode.NOT_WORD_SB : OPCode.NOT_WORD;
            } else {
                op = enc.isSingleByte() ? OPCode.WORD_SB : OPCode.WORD;
            }
            break;

        default:
            newInternalException(ERR_PARSER_BUG);
            return; // not reached
        } // inner switch
        addOpcode(op);
    }

    @Override
    protected void compileAnyCharNode() {
        if (isMultiline(regex.options)) {
            addOpcode(enc.isSingleByte() ? OPCode.ANYCHAR_ML_SB : OPCode.ANYCHAR_ML);
        } else {
            addOpcode(enc.isSingleByte() ? OPCode.ANYCHAR_SB : OPCode.ANYCHAR);
        }
    }

    @Override
    protected void compileCallNode(CallNode node) {
        addOpcode(OPCode.CALL);
        node.unsetAddrList.add(codeLength, node.target);
        addAbsAddr(0); /*dummy addr.*/
    }

    @Override
    protected void compileBackrefNode(BackRefNode node) {
        BackRefNode br = node;
        if (Config.USE_BACKREF_WITH_LEVEL && br.isNestLevel()) {
            addOpcode(OPCode.BACKREF_WITH_LEVEL);
            addOption(regex.options & Option.IGNORECASE);
            addLength(br.nestLevel);
            // !goto add_bacref_mems;!
            addLength(br.backNum);
            for (int i=br.backNum-1; i>=0; i--) addMemNum(br.back[i]);
            return;
        } else { // USE_BACKREF_AT_LEVEL
            if (br.backNum == 1) {
                if (isIgnoreCase(regex.options)) {
                    addOpcode(OPCode.BACKREFN_IC);
                    addMemNum(br.back[0]);
                } else {
                    switch (br.back[0]) {
                    case 1:
                        addOpcode(OPCode.BACKREF1);
                        break;
                    case 2:
                        addOpcode(OPCode.BACKREF2);
                        break;
                    default:
                        addOpcode(OPCode.BACKREFN);
                        addOpcode(br.back[0]);
                        break;
                    } // switch
                }
            } else {
                if (isIgnoreCase(regex.options)) {
                    addOpcode(OPCode.BACKREF_MULTI_IC);
                } else {
                    addOpcode(OPCode.BACKREF_MULTI);
                }
                // !add_bacref_mems:!
                addLength(br.backNum);
                for (int i=br.backNum-1; i>=0; i--) addMemNum(br.back[i]);
            }
        }
    }

    private static final int REPEAT_RANGE_ALLOC = 8;
    private void entryRepeatRange(int id, int lower, int upper) {
        if (regex.repeatRangeLo == null) {
            regex.repeatRangeLo = new int[REPEAT_RANGE_ALLOC];
            regex.repeatRangeHi = new int[REPEAT_RANGE_ALLOC];
        } else if (id >= regex.repeatRangeLo.length){
            int[]tmp = new int[regex.repeatRangeLo.length + REPEAT_RANGE_ALLOC];
            System.arraycopy(regex.repeatRangeLo, 0, tmp, 0, regex.repeatRangeLo.length);
            regex.repeatRangeLo = tmp;
            tmp = new int[regex.repeatRangeHi.length + REPEAT_RANGE_ALLOC];
            System.arraycopy(regex.repeatRangeHi, 0, tmp, 0, regex.repeatRangeHi.length);
            regex.repeatRangeHi = tmp;
        }

        regex.repeatRangeLo[id] = lower;
        regex.repeatRangeHi[id] = isRepeatInfinite(upper) ? 0x7fffffff : upper;
    }

    private void compileRangeRepeatNode(QuantifierNode qn, int targetLen, int emptyInfo) {
        regex.requireStack = true;
        int numRepeat = regex.numRepeat;
        addOpcode(qn.greedy ? OPCode.REPEAT : OPCode.REPEAT_NG);
        addMemNum(numRepeat); /* OP_REPEAT ID */
        regex.numRepeat++;
        addRelAddr(targetLen + OPSize.REPEAT_INC);

        entryRepeatRange(numRepeat, qn.lower, qn.upper);

        compileTreeEmptyCheck(qn.target, emptyInfo);

        if ((Config.USE_SUBEXP_CALL && regex.numCall > 0) || qn.isInRepeat()) {
            addOpcode(qn.greedy ? OPCode.REPEAT_INC_SG : OPCode.REPEAT_INC_NG_SG);
        } else {
            addOpcode(qn.greedy ? OPCode.REPEAT_INC : OPCode.REPEAT_INC_NG);
        }

        addMemNum(numRepeat); /* OP_REPEAT ID */
    }

    private static final int QUANTIFIER_EXPAND_LIMIT_SIZE   = 50; // was 50

    private static boolean cknOn(int ckn) {
        return ckn > 0;
    }

    private int compileCECLengthQuantifierNode(QuantifierNode qn) {
        boolean infinite = isRepeatInfinite(qn.upper);
        int emptyInfo = qn.targetEmptyInfo;

        int tlen = compileLengthTree(qn.target);
        int ckn = regex.numCombExpCheck > 0 ? qn.combExpCheckNum : 0;
        int cklen = cknOn(ckn) ? OPSize.STATE_CHECK_NUM : 0;

        /* anychar repeat */
        if (qn.target.getType() == NodeType.CANY) {
            if (qn.greedy && infinite) {
                if (qn.nextHeadExact != null && !cknOn(ckn)) {
                    return OPSize.ANYCHAR_STAR_PEEK_NEXT + tlen * qn.lower + cklen;
                } else {
                    return OPSize.ANYCHAR_STAR + tlen * qn.lower + cklen;
                }
            }
        }

        int modTLen;
        if (emptyInfo != 0) {
            modTLen = tlen + (OPSize.NULL_CHECK_START + OPSize.NULL_CHECK_END);
        } else {
            modTLen = tlen;
        }

        int len;
        if (infinite && qn.lower <= 1) {
            if (qn.greedy) {
                if (qn.lower == 1) {
                    len = OPSize.JUMP;
                } else {
                    len = 0;
                }
                len += OPSize.PUSH + cklen + modTLen + OPSize.JUMP;
            } else {
                if (qn.lower == 0) {
                    len = OPSize.JUMP;
                } else {
                    len = 0;
                }
                len += modTLen + OPSize.PUSH + cklen;
            }
        } else if (qn.upper == 0) {
            if (qn.isRefered) { /* /(?<n>..){0}/ */
                len = OPSize.JUMP + tlen;
            } else {
                len = 0;
            }
        } else if (qn.upper == 1 && qn.greedy) {
            if (qn.lower == 0) {
                if (cknOn(ckn)) {
                    len = OPSize.STATE_CHECK_PUSH + tlen;
                } else {
                    len = OPSize.PUSH + tlen;
                }
            } else {
                len = tlen;
            }
        } else if (!qn.greedy && qn.upper == 1 && qn.lower == 0) { /* '??' */
            len = OPSize.PUSH + cklen + OPSize.JUMP + tlen;
        } else {
            len = OPSize.REPEAT_INC + modTLen + OPSize.OPCODE + OPSize.RELADDR + OPSize.MEMNUM;

            if (cknOn(ckn)) {
                len += OPSize.STATE_CHECK;
            }
        }
        return len;
    }

    @Override
    protected void compileCECQuantifierNode(QuantifierNode qn) {
        regex.requireStack = true;
        boolean infinite = isRepeatInfinite(qn.upper);
        int emptyInfo = qn.targetEmptyInfo;

        int tlen = compileLengthTree(qn.target);

        int ckn = regex.numCombExpCheck > 0 ? qn.combExpCheckNum : 0;

        if (qn.isAnyCharStar()) {
            compileTreeNTimes(qn.target, qn.lower);
            if (qn.nextHeadExact != null && !cknOn(ckn)) {
                if (isMultiline(regex.options)) {
                    addOpcode(enc.isSingleByte() ? OPCode.ANYCHAR_ML_STAR_PEEK_NEXT_SB : OPCode.ANYCHAR_ML_STAR_PEEK_NEXT);
                } else {
                    addOpcode(enc.isSingleByte() ? OPCode.ANYCHAR_STAR_PEEK_NEXT_SB : OPCode.ANYCHAR_STAR_PEEK_NEXT);
                }
                if (cknOn(ckn)) {
                    addStateCheckNum(ckn);
                }
                StringNode sn = (StringNode)qn.nextHeadExact;
                addBytes(sn.bytes, sn.p, 1);
                return;
            } else {
                if (isMultiline(regex.options)) {
                    if (cknOn(ckn)) {
                        addOpcode(enc.isSingleByte() ? OPCode.STATE_CHECK_ANYCHAR_ML_STAR_SB : OPCode.STATE_CHECK_ANYCHAR_ML_STAR);
                    } else {
                        addOpcode(enc.isSingleByte() ? OPCode.ANYCHAR_ML_STAR_SB : OPCode.ANYCHAR_ML_STAR);
                    }
                } else {
                    if (cknOn(ckn)) {
                        addOpcode(enc.isSingleByte() ? OPCode.STATE_CHECK_ANYCHAR_STAR_SB : OPCode.STATE_CHECK_ANYCHAR_STAR);
                    } else {
                        addOpcode(enc.isSingleByte() ? OPCode.ANYCHAR_STAR_SB : OPCode.ANYCHAR_STAR);
                    }
                }
                if (cknOn(ckn)) {
                    addStateCheckNum(ckn);
                }
                return;
            }
        }

        int modTLen;
        if (emptyInfo != 0) {
            modTLen = tlen + (OPSize.NULL_CHECK_START + OPSize.NULL_CHECK_END);
        } else {
            modTLen = tlen;
        }
        if (infinite && qn.lower <= 1) {
            if (qn.greedy) {
                if (qn.lower == 1) {
                    addOpcodeRelAddr(OPCode.JUMP, cknOn(ckn) ? OPSize.STATE_CHECK_PUSH :
                                                                     OPSize.PUSH);
                }
                if (cknOn(ckn)) {
                    addOpcode(OPCode.STATE_CHECK_PUSH);
                    addStateCheckNum(ckn);
                    addRelAddr(modTLen + OPSize.JUMP);
                } else {
                    addOpcodeRelAddr(OPCode.PUSH, modTLen + OPSize.JUMP);
                }
                compileTreeEmptyCheck(qn.target, emptyInfo);
                addOpcodeRelAddr(OPCode.JUMP, -(modTLen + OPSize.JUMP + (cknOn(ckn) ?
                                                                               OPSize.STATE_CHECK_PUSH :
                                                                               OPSize.PUSH)));
            } else {
                if (qn.lower == 0) {
                    addOpcodeRelAddr(OPCode.JUMP, modTLen);
                }
                compileTreeEmptyCheck(qn.target, emptyInfo);
                if (cknOn(ckn)) {
                    addOpcode(OPCode.STATE_CHECK_PUSH_OR_JUMP);
                    addStateCheckNum(ckn);
                    addRelAddr(-(modTLen + OPSize.STATE_CHECK_PUSH_OR_JUMP));
                } else {
                    addOpcodeRelAddr(OPCode.PUSH, -(modTLen + OPSize.PUSH));
                }
            }
        } else if (qn.upper == 0) {
            if (qn.isRefered) { /* /(?<n>..){0}/ */
                addOpcodeRelAddr(OPCode.JUMP, tlen);
                compileTree(qn.target);
            } // else r=0 ???
        } else if (qn.upper == 1 && qn.greedy) {
            if (qn.lower == 0) {
                if (cknOn(ckn)) {
                    addOpcode(OPCode.STATE_CHECK_PUSH);
                    addStateCheckNum(ckn);
                    addRelAddr(tlen);
                } else {
                    addOpcodeRelAddr(OPCode.PUSH, tlen);
                }
            }
            compileTree(qn.target);
        } else if (!qn.greedy && qn.upper == 1 && qn.lower == 0){ /* '??' */
            if (cknOn(ckn)) {
                addOpcode(OPCode.STATE_CHECK_PUSH);
                addStateCheckNum(ckn);
                addRelAddr(OPSize.JUMP);
            } else {
                addOpcodeRelAddr(OPCode.PUSH, OPSize.JUMP);
            }

            addOpcodeRelAddr(OPCode.JUMP, tlen);
            compileTree(qn.target);
        } else {
            compileRangeRepeatNode(qn, modTLen, emptyInfo);
            if (cknOn(ckn)) {
                addOpcode(OPCode.STATE_CHECK);
                addStateCheckNum(ckn);
            }
        }
    }

    private int compileNonCECLengthQuantifierNode(QuantifierNode qn) {
        boolean infinite = isRepeatInfinite(qn.upper);
        int emptyInfo = qn.targetEmptyInfo;

        int tlen = compileLengthTree(qn.target);

        /* anychar repeat */
        if (qn.target.getType() == NodeType.CANY) {
            if (qn.greedy && infinite) {
                if (qn.nextHeadExact != null) {
                    return OPSize.ANYCHAR_STAR_PEEK_NEXT + tlen * qn.lower;
                } else {
                    return OPSize.ANYCHAR_STAR + tlen * qn.lower;
                }
            }
        }

        int modTLen = 0;
        if (emptyInfo != 0) {
            modTLen = tlen + (OPSize.NULL_CHECK_START + OPSize.NULL_CHECK_END);
        } else {
            modTLen = tlen;
        }

        int len;
        if (infinite && (qn.lower <= 1 || tlen * qn.lower <= QUANTIFIER_EXPAND_LIMIT_SIZE)) {
            if (qn.lower == 1 && tlen > QUANTIFIER_EXPAND_LIMIT_SIZE) {
                len = OPSize.JUMP;
            } else {
                len = tlen * qn.lower;
            }

            if (qn.greedy) {
                if (qn.headExact != null) {
                    len += OPSize.PUSH_OR_JUMP_EXACT1 + modTLen + OPSize.JUMP;
                } else if (qn.nextHeadExact != null) {
                    len += OPSize.PUSH_IF_PEEK_NEXT + modTLen + OPSize.JUMP;
                } else {
                    len += OPSize.PUSH + modTLen + OPSize.JUMP;
                }
            } else {
                len += OPSize.JUMP + modTLen + OPSize.PUSH;
            }

        } else if (qn.upper == 0 && qn.isRefered) { /* /(?<n>..){0}/ */
            len = OPSize.JUMP + tlen;
        } else if (!infinite && qn.greedy &&
                  (qn.upper == 1 || (tlen + OPSize.PUSH) * qn.upper <= QUANTIFIER_EXPAND_LIMIT_SIZE )) {
            len = tlen * qn.lower;
            len += (OPSize.PUSH + tlen) * (qn.upper - qn.lower);
        } else if (!qn.greedy && qn.upper == 1 && qn.lower == 0) { /* '??' */
            len = OPSize.PUSH + OPSize.JUMP + tlen;
        } else {
            len = OPSize.REPEAT_INC + modTLen + OPSize.OPCODE + OPSize.RELADDR + OPSize.MEMNUM;
        }
        return len;
    }

    @Override
    protected void compileNonCECQuantifierNode(QuantifierNode qn) {
        regex.requireStack = true;
        boolean infinite = isRepeatInfinite(qn.upper);
        int emptyInfo = qn.targetEmptyInfo;

        int tlen = compileLengthTree(qn.target);

        if (qn.isAnyCharStar()) {
            compileTreeNTimes(qn.target, qn.lower);
            if (qn.nextHeadExact != null) {
                if (isMultiline(regex.options)) {
                    addOpcode(enc.isSingleByte() ? OPCode.ANYCHAR_ML_STAR_PEEK_NEXT_SB : OPCode.ANYCHAR_ML_STAR_PEEK_NEXT);
                } else {
                    addOpcode(enc.isSingleByte() ? OPCode.ANYCHAR_STAR_PEEK_NEXT_SB : OPCode.ANYCHAR_STAR_PEEK_NEXT);
                }
                StringNode sn = (StringNode)qn.nextHeadExact;
                addBytes(sn.bytes, sn.p, 1);
                return;
            } else {
                if (isMultiline(regex.options)) {
                    addOpcode(enc.isSingleByte() ? OPCode.ANYCHAR_ML_STAR_SB : OPCode.ANYCHAR_ML_STAR);
                } else {
                    addOpcode(enc.isSingleByte() ? OPCode.ANYCHAR_STAR_SB : OPCode.ANYCHAR_STAR);
                }
                return;
            }
        }

        int modTLen;
        if (emptyInfo != 0) {
            modTLen = tlen + (OPSize.NULL_CHECK_START + OPSize.NULL_CHECK_END);
        } else {
            modTLen = tlen;
        }
        if (infinite && (qn.lower <= 1 || tlen * qn.lower <= QUANTIFIER_EXPAND_LIMIT_SIZE)) {
            if (qn.lower == 1 && tlen > QUANTIFIER_EXPAND_LIMIT_SIZE) {
                if (qn.greedy) {
                    if (qn.headExact != null) {
                        addOpcodeRelAddr(OPCode.JUMP, OPSize.PUSH_OR_JUMP_EXACT1);
                    } else if (qn.nextHeadExact != null) {
                        addOpcodeRelAddr(OPCode.JUMP, OPSize.PUSH_IF_PEEK_NEXT);
                    } else {
                        addOpcodeRelAddr(OPCode.JUMP, OPSize.PUSH);
                    }
                } else {
                    addOpcodeRelAddr(OPCode.JUMP, OPSize.JUMP);
                }
            } else {
                compileTreeNTimes(qn.target, qn.lower);
            }

            if (qn.greedy) {
                if (qn.headExact != null) {
                    addOpcodeRelAddr(OPCode.PUSH_OR_JUMP_EXACT1, modTLen + OPSize.JUMP);
                    StringNode sn = (StringNode)qn.headExact;
                    addBytes(sn.bytes, sn.p, 1);
                    compileTreeEmptyCheck(qn.target, emptyInfo);
                    addOpcodeRelAddr(OPCode.JUMP, -(modTLen + OPSize.JUMP + OPSize.PUSH_OR_JUMP_EXACT1));
                } else if (qn.nextHeadExact != null) {
                    addOpcodeRelAddr(OPCode.PUSH_IF_PEEK_NEXT, modTLen + OPSize.JUMP);
                    StringNode sn = (StringNode)qn.nextHeadExact;
                    addBytes(sn.bytes, sn.p, 1);
                    compileTreeEmptyCheck(qn.target, emptyInfo);
                    addOpcodeRelAddr(OPCode.JUMP, -(modTLen + OPSize.JUMP + OPSize.PUSH_IF_PEEK_NEXT));
                } else {
                    addOpcodeRelAddr(OPCode.PUSH, modTLen + OPSize.JUMP);
                    compileTreeEmptyCheck(qn.target, emptyInfo);
                    addOpcodeRelAddr(OPCode.JUMP, -(modTLen + OPSize.JUMP + OPSize.PUSH));
                }
            } else {
                addOpcodeRelAddr(OPCode.JUMP, modTLen);
                compileTreeEmptyCheck(qn.target, emptyInfo);
                addOpcodeRelAddr(OPCode.PUSH, -(modTLen + OPSize.PUSH));
            }
        } else if (qn.upper == 0 && qn.isRefered) { /* /(?<n>..){0}/ */
            addOpcodeRelAddr(OPCode.JUMP, tlen);
            compileTree(qn.target);
        } else if (!infinite && qn.greedy &&
                  (qn.upper == 1 || (tlen + OPSize.PUSH) * qn.upper <= QUANTIFIER_EXPAND_LIMIT_SIZE)) {
            int n = qn.upper - qn.lower;
            compileTreeNTimes(qn.target, qn.lower);

            for (int i=0; i<n; i++) {
                addOpcodeRelAddr(OPCode.PUSH, (n - i) * tlen + (n - i - 1) * OPSize.PUSH);
                compileTree(qn.target);
            }
        } else if (!qn.greedy && qn.upper == 1 && qn.lower == 0) { /* '??' */
            addOpcodeRelAddr(OPCode.PUSH, OPSize.JUMP);
            addOpcodeRelAddr(OPCode.JUMP, tlen);
            compileTree(qn.target);
        } else {
            compileRangeRepeatNode(qn, modTLen, emptyInfo);
        }
    }

    private int compileLengthOptionNode(EncloseNode node) {
        int prev = regex.options;
        regex.options = node.option;
        int tlen = compileLengthTree(node.target);
        regex.options = prev;

        if (isDynamic(prev ^ node.option)) {
            return OPSize.SET_OPTION_PUSH + OPSize.SET_OPTION + OPSize.FAIL + tlen + OPSize.SET_OPTION;
        } else {
            return tlen;
        }
    }

    @Override
    protected void compileOptionNode(EncloseNode node) {
        int prev = regex.options;

        if (isDynamic(prev ^ node.option)) {
            addOpcodeOption(OPCode.SET_OPTION_PUSH, node.option);
            addOpcodeOption(OPCode.SET_OPTION, prev);
            addOpcode(OPCode.FAIL);
        }

        regex.options = node.option;
        compileTree(node.target);
        regex.options = prev;

        if (isDynamic(prev ^ node.option)) {
            addOpcodeOption(OPCode.SET_OPTION, prev);
        }
    }

    private int compileLengthEncloseNode(EncloseNode node) {
        if (node.isOption()) {
            return compileLengthOptionNode(node);
        }

        int tlen;
        if (node.target != null) {
            tlen = compileLengthTree(node.target);
        } else {
            tlen = 0;
        }

        int len;
        switch (node.type) {
        case EncloseType.MEMORY:
            if (Config.USE_SUBEXP_CALL && node.isCalled()) {
                len = OPSize.MEMORY_START_PUSH + tlen + OPSize.CALL + OPSize.JUMP + OPSize.RETURN;
                if (bsAt(regex.btMemEnd, node.regNum)) {
                    len += node.isRecursion() ? OPSize.MEMORY_END_PUSH_REC : OPSize.MEMORY_END_PUSH;
                } else {
                    len += node.isRecursion() ? OPSize.MEMORY_END_REC : OPSize.MEMORY_END;
                }
            } else { // USE_SUBEXP_CALL
                if (bsAt(regex.btMemStart, node.regNum)) {
                    len = OPSize.MEMORY_START_PUSH;
                } else {
                    len = OPSize.MEMORY_START;
                }
                len += tlen + (bsAt(regex.btMemEnd, node.regNum) ? OPSize.MEMORY_END_PUSH : OPSize.MEMORY_END);
            }
            break;

        case EncloseType.STOP_BACKTRACK:
            if (node.isStopBtSimpleRepeat()) {
                QuantifierNode qn = (QuantifierNode)node.target;
                tlen = compileLengthTree(qn.target);
                len = tlen * qn.lower + OPSize.PUSH + tlen + OPSize.POP + OPSize.JUMP;
            } else {
                len = OPSize.PUSH_STOP_BT + tlen + OPSize.POP_STOP_BT;
            }
            break;

        case EncloseType.CONDITION:
            len = OPSize.CONDITION;
            if (node.target.getType() == NodeType.ALT) {
                ConsAltNode x = (ConsAltNode)node.target;
                tlen = compileLengthTree(x.car); /* yes-node */
                len += tlen + OPSize.JUMP;
                if (x.cdr == null) newInternalException(ERR_PARSER_BUG);
                x = x.cdr;
                tlen = compileLengthTree(x.car); /* no-node */
                len += tlen;
                if (x.cdr != null) newSyntaxException(ERR_INVALID_CONDITION_PATTERN);
            } else {
                newInternalException(ERR_PARSER_BUG);
            }
            break;
        default:
            newInternalException(ERR_PARSER_BUG);
            return 0; // not reached
        } // switch
        return len;
    }

    @Override
    protected void compileEncloseNode(EncloseNode node) {
        int len;
        switch (node.type) {
        case EncloseType.MEMORY:
            if (Config.USE_SUBEXP_CALL) {
                if (node.isCalled()) {
                    regex.requireStack = true;
                    addOpcode(OPCode.CALL);
                    node.callAddr = codeLength + OPSize.ABSADDR + OPSize.JUMP;
                    node.setAddrFixed();
                    addAbsAddr(node.callAddr);
                    len = compileLengthTree(node.target);
                    len += OPSize.MEMORY_START_PUSH + OPSize.RETURN;
                    if (bsAt(regex.btMemEnd, node.regNum)) {
                        len += node.isRecursion() ? OPSize.MEMORY_END_PUSH_REC : OPSize.MEMORY_END_PUSH;
                    } else {
                        len += node.isRecursion() ? OPSize.MEMORY_END_REC : OPSize.MEMORY_END;
                    }
                    addOpcodeRelAddr(OPCode.JUMP, len);
                }
            } // USE_SUBEXP_CALL

            if (bsAt(regex.btMemStart, node.regNum)) {
                regex.requireStack = true;
                addOpcode(OPCode.MEMORY_START_PUSH);
            } else {
                addOpcode(OPCode.MEMORY_START);
            }

            addMemNum(node.regNum);
            compileTree(node.target);

            if (Config.USE_SUBEXP_CALL && node.isCalled()) {
                if (bsAt(regex.btMemEnd, node.regNum)) {
                    addOpcode(node.isRecursion() ? OPCode.MEMORY_END_PUSH_REC : OPCode.MEMORY_END_PUSH);
                } else {
                    addOpcode(node.isRecursion() ? OPCode.MEMORY_END_REC : OPCode.MEMORY_END);
                }
                addMemNum(node.regNum);
                addOpcode(OPCode.RETURN);
            } else { // USE_SUBEXP_CALL
                if (bsAt(regex.btMemEnd, node.regNum)) {
                    addOpcode(OPCode.MEMORY_END_PUSH);
                } else {
                    addOpcode(OPCode.MEMORY_END);
                }
                addMemNum(node.regNum);
            }
            break;

        case EncloseType.STOP_BACKTRACK:
            regex.requireStack = true;
            if (node.isStopBtSimpleRepeat()) {
                QuantifierNode qn = (QuantifierNode)node.target;

                compileTreeNTimes(qn.target, qn.lower);

                len = compileLengthTree(qn.target);
                addOpcodeRelAddr(OPCode.PUSH, len + OPSize.POP + OPSize.JUMP);
                compileTree(qn.target);
                addOpcode(OPCode.POP);
                addOpcodeRelAddr(OPCode.JUMP, -(OPSize.PUSH + len + OPSize.POP + OPSize.JUMP));
            } else {
                addOpcode(OPCode.PUSH_STOP_BT);
                compileTree(node.target);
                addOpcode(OPCode.POP_STOP_BT);
            }
            break;

        case EncloseType.CONDITION:
            addOpcode(OPCode.CONDITION);
            addMemNum(node.regNum);
            if (node.target.getType() == NodeType.ALT) {
                ConsAltNode x = (ConsAltNode)node.target;
                len = compileLengthTree(x.car); /* yes-node */
                if (x.cdr == null) newInternalException(ERR_PARSER_BUG);
                x = x.cdr;
                int len2 = compileLengthTree(x.car); /* no-node */
                if (x.cdr != null) newSyntaxException(ERR_INVALID_CONDITION_PATTERN);
                x = (ConsAltNode)node.target;
                addRelAddr(len + OPSize.JUMP);
                compileTree(x.car); /* yes-node */
                addOpcodeRelAddr(OPCode.JUMP, len2);
                x = x.cdr;
                compileTree(x.car); /* no-node */
            } else {
                newInternalException(ERR_PARSER_BUG);
            }
            break;

        default:
            newInternalException(ERR_PARSER_BUG);
            break;
        } // switch
    }

    private int compileLengthAnchorNode(AnchorNode node) {
        int tlen;
        if (node.target != null) {
            tlen = compileLengthTree(node.target);
        } else {
            tlen = 0;
        }

        int len;
        switch (node.type) {
        case AnchorType.PREC_READ:
            len = OPSize.PUSH_POS + tlen + OPSize.POP_POS;
            break;

        case AnchorType.PREC_READ_NOT:
            len = OPSize.PUSH_POS_NOT + tlen + OPSize.FAIL_POS;
            break;

        case AnchorType.LOOK_BEHIND:
            len = OPSize.LOOK_BEHIND + tlen;
            break;

        case AnchorType.LOOK_BEHIND_NOT:
            len = OPSize.PUSH_LOOK_BEHIND_NOT + tlen + OPSize.FAIL_LOOK_BEHIND_NOT;
            break;

        default:
            len = OPSize.OPCODE;
            break;
        } // switch
        return len;
    }

    @Override
    protected void compileAnchorNode(AnchorNode node) {
        int len;
        int n;

        switch (node.type) {
        case AnchorType.BEGIN_BUF:          addOpcode(OPCode.BEGIN_BUF);            break;
        case AnchorType.END_BUF:            addOpcode(OPCode.END_BUF);              break;
        case AnchorType.BEGIN_LINE:         addOpcode(OPCode.BEGIN_LINE);           break;
        case AnchorType.END_LINE:           addOpcode(OPCode.END_LINE);             break;
        case AnchorType.SEMI_END_BUF:       addOpcode(OPCode.SEMI_END_BUF);         break;
        case AnchorType.BEGIN_POSITION:     addOpcode(OPCode.BEGIN_POSITION);       break;

        case AnchorType.WORD_BOUND:
            addOpcode(enc.isSingleByte() ? OPCode.WORD_BOUND_SB : OPCode.WORD_BOUND);
            break;

        case AnchorType.NOT_WORD_BOUND:
            addOpcode(enc.isSingleByte() ? OPCode.NOT_WORD_BOUND_SB : OPCode.NOT_WORD_BOUND);
            break;

        case AnchorType.WORD_BEGIN:
            if (Config.USE_WORD_BEGIN_END)
                addOpcode(enc.isSingleByte() ? OPCode.WORD_BEGIN_SB : OPCode.WORD_BEGIN);
            break;

        case AnchorType.WORD_END:
            if (Config.USE_WORD_BEGIN_END)
                addOpcode(enc.isSingleByte() ? OPCode.WORD_END_SB : OPCode.WORD_END);
            break;

        case AnchorType.KEEP:
            addOpcode(OPCode.KEEP);
            break;

        case AnchorType.PREC_READ:
            regex.requireStack = true;
            addOpcode(OPCode.PUSH_POS);
            compileTree(node.target);
            addOpcode(OPCode.POP_POS);
            break;

        case AnchorType.PREC_READ_NOT:
            regex.requireStack = true;
            len = compileLengthTree(node.target);
            addOpcodeRelAddr(OPCode.PUSH_POS_NOT, len + OPSize.FAIL_POS);
            compileTree(node.target);
            addOpcode(OPCode.FAIL_POS);
            break;

        case AnchorType.LOOK_BEHIND:
            addOpcode(enc.isSingleByte() ? OPCode.LOOK_BEHIND_SB : OPCode.LOOK_BEHIND);
            if (node.charLength < 0) {
                n = analyser.getCharLengthTree(node.target);
                if (analyser.returnCode != 0) newSyntaxException(ERR_INVALID_LOOK_BEHIND_PATTERN);
            } else {
                n = node.charLength;
            }
            addLength(n);
            compileTree(node.target);
            break;

        case AnchorType.LOOK_BEHIND_NOT:
            regex.requireStack = true;
            len = compileLengthTree(node.target);
            addOpcodeRelAddr(OPCode.PUSH_LOOK_BEHIND_NOT, len + OPSize.FAIL_LOOK_BEHIND_NOT);
            if (node.charLength < 0) {
                n = analyser.getCharLengthTree(node.target);
                if (analyser.returnCode != 0) newSyntaxException(ERR_INVALID_LOOK_BEHIND_PATTERN);
            } else {
                n = node.charLength;
            }
            addLength(n);
            compileTree(node.target);
            addOpcode(OPCode.FAIL_LOOK_BEHIND_NOT);
            break;

        default:
            newInternalException(ERR_PARSER_BUG);
        } // switch
    }

    private int compileLengthTree(Node node) {
        int len = 0;

        switch (node.getType()) {
        case NodeType.LIST:
            ConsAltNode lin = (ConsAltNode)node;
            do {
                len += compileLengthTree(lin.car);
            } while ((lin = lin.cdr) != null);
            break;

        case NodeType.ALT:
            ConsAltNode aln = (ConsAltNode)node;
            int n = 0;
            do {
                len += compileLengthTree(aln.car);
                n++;
            } while ((aln = aln.cdr) != null);
            len += (OPSize.PUSH + OPSize.JUMP) * (n - 1);
            break;

        case NodeType.STR:
            StringNode sn = (StringNode)node;
            if (sn.isRaw()) {
                len = compileLengthStringRawNode(sn);
            } else {
                len = compileLengthStringNode(sn);
            }
            break;

        case NodeType.CCLASS:
            len = compileLengthCClassNode((CClassNode)node);
            break;

        case NodeType.CTYPE:
        case NodeType.CANY:
            len = OPSize.OPCODE;
            break;

        case NodeType.BREF:
            BackRefNode br = (BackRefNode)node;

            if (Config.USE_BACKREF_WITH_LEVEL && br.isNestLevel()) {
                len = OPSize.OPCODE + OPSize.OPTION + OPSize.LENGTH +
                      OPSize.LENGTH + (OPSize.MEMNUM * br.backNum);
            } else { // USE_BACKREF_AT_LEVEL
                if (br.backNum == 1) {
                    len = ((!isIgnoreCase(regex.options) && br.back[0] <= 2)
                            ? OPSize.OPCODE : (OPSize.OPCODE + OPSize.MEMNUM));
                } else {
                    len = OPSize.OPCODE + OPSize.LENGTH + (OPSize.MEMNUM * br.backNum);
                }
            }
            break;

        case NodeType.CALL:
            if (Config.USE_SUBEXP_CALL) {
                len = OPSize.CALL;
                break;
            } // USE_SUBEXP_CALL
            break;

        case NodeType.QTFR:
            if (Config.USE_COMBINATION_EXPLOSION_CHECK) {
                len = compileCECLengthQuantifierNode((QuantifierNode)node);
            } else {
                len = compileNonCECLengthQuantifierNode((QuantifierNode)node);
            }
            break;

        case NodeType.ENCLOSE:
            len = compileLengthEncloseNode((EncloseNode)node);
            break;

        case NodeType.ANCHOR:
            len = compileLengthAnchorNode((AnchorNode)node);
            break;

        default:
            newInternalException(ERR_PARSER_BUG);

        } //switch
        return len;
    }

    private void ensure(int size) {
        if (size >= code.length) {
            int length = code.length << 1;
            while (length <= size) length <<= 1;
            int[]tmp = new int[length];
            System.arraycopy(code, 0, tmp, 0, code.length);
            code = tmp;
        }
    }

    private void addInt(int i) {
        if (codeLength >= code.length) {
            int[]tmp = new int[code.length << 1];
            System.arraycopy(code, 0, tmp, 0, code.length);
            code = tmp;
        }
        code[codeLength++] = i;
    }

    void setInt(int i, int offset) {
        ensure(offset);
        regex.code[offset] = i;
    }

    private void addObject(Object o) {
        if (regex.operands == null) {
            regex.operands = new Object[4];
        } else if (regex.operandLength >= regex.operands.length) {
            Object[]tmp = new Object[regex.operands.length << 1];
            System.arraycopy(regex.operands, 0, tmp, 0, regex.operands.length);
            regex.operands = tmp;
        }
        addInt(regex.operandLength);
        regex.operands[regex.operandLength++] = o;
    }

    private void addBytes(byte[]bytes, int p ,int length) {
        ensure(codeLength + length);
        int end = p + length;

        while (p < end) code[codeLength++] = bytes[p++];
    }

    private void addInts(int[]ints, int length) {
        ensure(codeLength + length);
        System.arraycopy(ints, 0, code, codeLength, length);
        codeLength += length;
    }

    private void addOpcode(int opcode) {
        addInt(opcode);
    }

    private void addStateCheckNum(int num) {
        addInt(num);
    }

    private void addRelAddr(int addr) {
        addInt(addr);
    }

    private void addAbsAddr(int addr) {
        addInt(addr);
    }

    private void addLength(int length) {
        addInt(length);
    }

    private void addMemNum(int num) {
        addInt(num);
    }

    private void addPointer(Object o) {
        addObject(o);
    }

    private void addOption(int option) {
        addInt(option);
    }

    private void addOpcodeRelAddr(int opcode, int addr) {
        addOpcode(opcode);
        addRelAddr(addr);
    }

    private void addOpcodeOption(int opcode, int option) {
        addOpcode(opcode);
        addOption(option);
    }

    private void addTemplate(byte[]bytes) {
        if (templateNum == 0) {
            templates = new byte[2][];
        } else if (templateNum == templates.length) {
            byte[][]tmp = new byte[templateNum * 2][];
            System.arraycopy(templates, 0, tmp, 0, templateNum);
            templates = tmp;
        }
        templates[templateNum++] = bytes;
    }
}
