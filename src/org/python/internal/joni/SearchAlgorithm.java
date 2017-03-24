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
import org.jcodings.IntHolder;

public abstract class SearchAlgorithm {

    public abstract String getName();
    public abstract int search(Matcher matcher, byte[]text, int textP, int textEnd, int textRange);
    public abstract int searchBackward(Matcher matcher, byte[]text, int textP, int adjustText, int textEnd, int textStart, int s_, int range_);


    public static final SearchAlgorithm NONE = new SearchAlgorithm() {

        public final String getName() {
            return "NONE";
        }

        public final int search(Matcher matcher, byte[]text, int textP, int textEnd, int textRange) {
            return textP;
        }

        public final int searchBackward(Matcher matcher, byte[]text, int textP, int adjustText, int textEnd, int textStart, int s_, int range_) {
            return textP;
        }

    };

    public static final SearchAlgorithm SLOW = new SearchAlgorithm() {

        public final String getName() {
            return "EXACT";
        }

        public final int search(Matcher matcher, byte[]text, int textP, int textEnd, int textRange) {
            Regex regex = matcher.regex;
            Encoding enc = regex.enc;
            byte[]target = regex.exact;
            int targetP = regex.exactP;
            int targetEnd = regex.exactEnd;


            int end = textEnd;
            end -= targetEnd - targetP - 1;

            if (end > textRange) end = textRange;

            int s = textP;

            while (s < end) {
                if (text[s] == target[targetP]) {
                    int p = s + 1;
                    int t = targetP + 1;
                    while (t < targetEnd) {
                        if (target[t] != text[p++]) break;
                        t++;
                    }

                    if (t == targetEnd) return s;
                }
                s += enc.length(text, s, textEnd);
            }

            return -1;
        }

        public final int searchBackward(Matcher matcher, byte[]text, int textP, int adjustText, int textEnd, int textStart, int s_, int range_) {
            Regex regex = matcher.regex;
            Encoding enc = regex.enc;
            byte[]target = regex.exact;
            int targetP = regex.exactP;
            int targetEnd = regex.exactEnd;

            int s = textEnd;
            s -= targetEnd - targetP;

            if (s > textStart) {
                s = textStart;
            } else {
                s = enc.leftAdjustCharHead(text, adjustText, s, textEnd);
            }

            while (s >= textP) {
                if (text[s] == target[targetP]) {
                    int p = s + 1;
                    int t = targetP + 1;
                    while (t < targetEnd) {
                        if (target[t] != text[p++]) break;
                        t++;
                    }
                    if (t == targetEnd) return s;
                }
                s = enc.prevCharHead(text, adjustText, s, textEnd);
            }
            return -1;
        }
    };

    public static final SearchAlgorithm SLOW_SB = new SearchAlgorithm() {

        public final String getName() {
            return "EXACT_SB";
        }

        public final int search(Matcher matcher, byte[]text, int textP, int textEnd, int textRange) {
            Regex regex = matcher.regex;
            byte[]target = regex.exact;
            int targetP = regex.exactP;
            int targetEnd = regex.exactEnd;

            int end = textEnd;
            end -= targetEnd - targetP - 1;

            if (end > textRange) end = textRange;

            int s = textP;

            while (s < end) {
                if (text[s] == target[targetP]) {
                    int p = s + 1;
                    int t = targetP + 1;
                    while (t < targetEnd) {
                        if (target[t] != text[p++]) break;
                        t++;
                    }

                    if (t == targetEnd) return s;
                }
                s++;
            }

            return -1;
        }

        public final int searchBackward(Matcher matcher, byte[]text, int textP, int adjustText, int textEnd, int textStart, int s_, int range_) {
            Regex regex = matcher.regex;
            byte[]target = regex.exact;
            int targetP = regex.exactP;
            int targetEnd = regex.exactEnd;

            int s = textEnd;
            s -= targetEnd - targetP;

            if (s > textStart) s = textStart;

            while (s >= textP) {
                if (text[s] == target[targetP]) {
                    int p = s + 1;
                    int t = targetP + 1;
                    while (t < targetEnd) {
                        if (target[t] != text[p++]) break;
                        t++;
                    }
                    if (t == targetEnd) return s;
                }
                //s = s <= adjustText ? -1 : s - 1;
                s--;
            }
            return -1;
        }
    };


    public static final SearchAlgorithm SLOW_IC = new SearchAlgorithm() {

        public final String getName() {
            return "EXACT_IC";
        }

        public final int search(Matcher matcher, byte[]text, int textP, int textEnd, int textRange) {
            Regex regex = matcher.regex;
            Encoding enc = regex.enc;
            byte[]target = regex.exact;
            int targetP = regex.exactP;
            int targetEnd = regex.exactEnd;

            int end = textEnd;
            end -= targetEnd - targetP - 1;

            if (end > textRange) end = textRange;
            int s = textP;

            byte[]buf = matcher.icbuf();
            while (s < end) {
                if (lowerCaseMatch(target, targetP, targetEnd, text, s, textEnd, enc, buf, regex.caseFoldFlag)) return s;
                s += enc.length(text, s, textEnd);
            }
            return -1;
        }

        public final int searchBackward(Matcher matcher, byte[]text, int textP, int adjustText, int textEnd, int textStart, int s_, int range_) {
            Regex regex = matcher.regex;
            Encoding enc = regex.enc;
            byte[]target = regex.exact;
            int targetP = regex.exactP;
            int targetEnd = regex.exactEnd;

            int s = textEnd;
            s -= targetEnd - targetP;

            if (s > textStart) {
                s = textStart;
            } else {
                s = enc.leftAdjustCharHead(text, adjustText, s, textEnd);
            }
            byte[]buf = matcher.icbuf();
            while (s >= textP) {
                if (lowerCaseMatch(target, targetP, targetEnd, text, s, textEnd, enc, buf, regex.caseFoldFlag)) return s;
                s = enc.prevCharHead(text, adjustText, s, textEnd);
            }
            return -1;
        }

        private boolean lowerCaseMatch(byte[]t, int tP, int tEnd,
                                       byte[]bytes, int p, int end, Encoding enc, byte[]buf, int caseFoldFlag) {
            final IntHolder holder = new IntHolder();
            holder.value = p;
            while (tP < tEnd) {
                int lowlen = enc.mbcCaseFold(caseFoldFlag, bytes, holder, end, buf);
                if (lowlen == 1) {
                    if (t[tP++] != buf[0]) return false;
                } else {
                    int q = 0;
                    while (lowlen > 0) {
                        if (t[tP++] != buf[q++]) return false;
                        lowlen--;
                    }
                }
            }
            return true;
        }
    };

    public static final SearchAlgorithm SLOW_IC_SB = new SearchAlgorithm() {

        public final String getName() {
            return "EXACT_IC_SB";
        }

        public final int search(Matcher matcher, byte[]text, int textP, int textEnd, int textRange) {
            Regex regex = matcher.regex;
            final byte[]toLowerTable = regex.enc.toLowerCaseTable();
            byte[]target = regex.exact;
            int targetP = regex.exactP;
            int targetEnd = regex.exactEnd;

            int end = textEnd;
            end -= targetEnd - targetP - 1;

            if (end > textRange) end = textRange;
            int s = textP;

            while (s < end) {
                if (target[targetP] == toLowerTable[text[s] & 0xff]) {
                    int p = s + 1;
                    int t = targetP + 1;
                    while (t < targetEnd) {
                        if (target[t] != toLowerTable[text[p++] & 0xff]) break;
                        t++;
                    }

                    if (t == targetEnd) return s;
                }
                s++;
            }
            return -1;
        }

        public final int searchBackward(Matcher matcher, byte[]text, int textP, int adjustText, int textEnd, int textStart, int s_, int range_) {
            Regex regex = matcher.regex;
            final byte[]toLowerTable = regex.enc.toLowerCaseTable();
            byte[]target = regex.exact;
            int targetP = regex.exactP;
            int targetEnd = regex.exactEnd;

            int s = textEnd;
            s -= targetEnd - targetP;

            if (s > textStart) s = textStart;

            while (s >= textP) {
                if (target[targetP] == toLowerTable[text[s] & 0xff]) {
                    int p = s + 1;
                    int t = targetP + 1;
                    while (t < targetEnd) {
                        if (target[t] != toLowerTable[text[p++] & 0xff]) break;
                        t++;
                    }
                    if (t == targetEnd) return s;
                }
                //s = s <= adjustText ? -1 : s - 1;
                s--;
            }
            return -1;
        }

    };

    public static final SearchAlgorithm BM = new SearchAlgorithm() {

        public final String getName() {
            return "EXACT_BM";
        }

        public final int search(Matcher matcher, byte[]text, int textP, int textEnd, int textRange) {
            Regex regex = matcher.regex;
            byte[]target = regex.exact;
            int targetP = regex.exactP;
            int targetEnd = regex.exactEnd;

            int end = textRange + (targetEnd - targetP) - 1;
            if (end > textEnd) end = textEnd;

            int tail = targetEnd - 1;
            int s = textP + (targetEnd - targetP) - 1;

            if (regex.intMap == null) {
                while (s < end) {
                    int p = s;
                    int t = tail;

                    while (text[p] == target[t]) {
                        if (t == targetP) return p;
                        p--; t--;
                    }

                    s += regex.map[text[s] & 0xff];
                }
            } else { /* see int_map[] */
                while (s < end) {
                    int p = s;
                    int t = tail;

                    while (text[p] == target[t]) {
                        if (t == targetP) return p;
                        p--; t--;
                    }

                    s += regex.intMap[text[s] & 0xff];
                }
            }
            return -1;
        }

        private static final int BM_BACKWARD_SEARCH_LENGTH_THRESHOLD = 100;

        public final int searchBackward(Matcher matcher, byte[]text, int textP, int adjustText, int textEnd, int textStart, int s_, int range_) {
            Regex regex = matcher.regex;
            Encoding enc = regex.enc;
            byte[]target = regex.exact;
            int targetP = regex.exactP;
            int targetEnd = regex.exactEnd;

            if (regex.intMapBackward == null) {
                if (s_ - range_ < BM_BACKWARD_SEARCH_LENGTH_THRESHOLD) {
                    // goto exact_method;
                    return SLOW.searchBackward(matcher, text, textP, adjustText, textEnd, textStart, s_, range_);
                }
                setBmBackwardSkip(regex, target, targetP, targetEnd);
            }

            int s = textEnd - (targetEnd - targetP);

            if (textStart < s) {
                s = textStart;
            } else {
                s = enc.leftAdjustCharHead(text, adjustText, s, textEnd);
            }

            while (s >= textP) {
                int p = s;
                int t = targetP;
                while (t < targetEnd && text[p] == target[t]) {
                    p++; t++;
                }
                if (t == targetEnd) return s;

                s -= regex.intMapBackward[text[s] & 0xff];
                s = enc.leftAdjustCharHead(text, adjustText, s, textEnd);
            }
            return -1;
        }


        private void setBmBackwardSkip(Regex regex, byte[]bytes, int p, int end) {
            int[] skip;
            if (regex.intMapBackward == null) {
                skip = new int[Config.CHAR_TABLE_SIZE];
                regex.intMapBackward = skip;
            } else {
                skip = regex.intMapBackward;
            }

            int len = end - p;

            for (int i=0; i<Config.CHAR_TABLE_SIZE; i++) skip[i] = len;
            for (int i=len-1; i>0; i--) skip[bytes[i] & 0xff] = i;
        }
    };

    public static final SearchAlgorithm BM_NOT_REV = new SearchAlgorithm() {

        public final String getName() {
            return "EXACT_BM_NOT_REV";
        }

        public final int search(Matcher matcher, byte[]text, int textP, int textEnd, int textRange) {
            Regex regex = matcher.regex;
            Encoding enc = regex.enc;
            byte[]target = regex.exact;
            int targetP = regex.exactP;
            int targetEnd = regex.exactEnd;

            int tail = targetEnd - 1;
            int tlen1 = tail - targetP;
            int end = textRange;

            if (Config.DEBUG_SEARCH) {
                Config.log.println("bm_search_notrev: "+
                                    "text: " + textP +
                                    ", text_end: " + textEnd +
                                    ", text_range: " + textRange);
            }

            if (end + tlen1 > textEnd) end = textEnd - tlen1;

            int s = textP;

            if (regex.intMap == null) {
                while (s < end) {
                    int p, se;
                    p = se = s + tlen1;
                    int t = tail;
                    while (text[p] == target[t]) {
                        if (t == targetP) return s;
                        p--; t--;
                    }

                    int skip = regex.map[text[se] & 0xff];
                    t = s;
                    do {
                        s += enc.length(text, s, textEnd);
                    } while ((s - t) < skip && s < end);
                }
            } else {
                while (s < end) {
                    int p, se;
                    p = se = s + tlen1;
                    int t = tail;

                    while (text[p] == target[t]) {
                        if (t == targetP) return s;
                        p--; t--;
                    }

                    int skip = regex.intMap[text[se] & 0xff];
                    t = s;
                    do {
                        s += enc.length(text, s, textEnd);
                    } while ((s - t) < skip && s < end);

                }
            }
            return -1;
        }

        public final int searchBackward(Matcher matcher, byte[]text, int textP, int adjustText, int textEnd, int textStart, int s_, int range_) {
            return BM.searchBackward(matcher, text, textP, adjustText, textEnd, textStart, s_, range_);
        }
    };


    public static final SearchAlgorithm MAP = new SearchAlgorithm() {

        public final String getName() {
            return "MAP";
        }

        // TODO: check 1.9 inconsistent calls to map_search
        public final int search(Matcher matcher, byte[]text, int textP, int textEnd, int textRange) {
            Regex regex = matcher.regex;
            Encoding enc = regex.enc;
            byte[]map = regex.map;
            int s = textP;

            while (s < textRange) {
                if (map[text[s] & 0xff] != 0) return s;
                s += enc.length(text, s, textEnd);
            }
            return -1;
        }

        public final int searchBackward(Matcher matcher, byte[]text, int textP, int adjustText, int textEnd, int textStart, int s_, int range_) {
            Regex regex = matcher.regex;
            Encoding enc = regex.enc;
            byte[]map = regex.map;
            int s = textStart;

            if (s >= textEnd) s = textEnd - 1; // multibyte safe ?
            while (s >= textP) {
                if (map[text[s] & 0xff] != 0) return s;
                s = enc.prevCharHead(text, adjustText, s, textEnd);
            }
            return -1;
        }
    };

    public static final SearchAlgorithm MAP_SB = new SearchAlgorithm() {

        public final String getName() {
            return "MAP_SB";
        }

        public final int search(Matcher matcher, byte[]text, int textP, int textEnd, int textRange) {
            Regex regex = matcher.regex;
            byte[]map = regex.map;
            int s = textP;

            while (s < textRange) {
                if (map[text[s] & 0xff] != 0) return s;
                s++;
            }
            return -1;
        }

        public final int searchBackward(Matcher matcher, byte[]text, int textP, int adjustText, int textEnd, int textStart, int s_, int range_) {
            Regex regex = matcher.regex;
            byte[]map = regex.map;
            int s = textStart;

            if (s >= textEnd) s = textEnd - 1;
            while (s >= textP) {
                if (map[text[s] & 0xff] != 0) return s;
                s--;
            }
            return -1;
        }
    };

}
