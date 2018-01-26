package org.python.core;

import com.ibm.icu.lang.UCharacter;
import org.python.annotations.ExposedClassMethod;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;
import org.python.core.stringlib.Encoding;
import org.python.core.stringlib.FieldNameIterator;
import org.python.core.stringlib.MarkupIterator;
import org.python.expose.MethodType;
import org.python.modules._codecs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.python.core.stringlib.Encoding.encode_UnicodeEscape;

/**
 * a builtin python unicode string.
 */
@Untraversable
@ExposedType(name = "str", base = PyObject.class, doc = BuiltinDocs.str_doc)
public class PyUnicode extends PySequence implements Iterable {
    // Note: place this after BASIC, since the initialization of str type __doc__ depends on BASIC
    public static final PyType TYPE = PyType.fromClass(PyUnicode.class);
    /**
     * Nearly every significant method comes in two versions: one applicable when the string
     * contains only basic plane characters, and one that is correct when supplementary characters
     * are also present. Set this constant <code>true</code> to treat all strings as containing
     * supplementary characters, so that these versions will be exercised in tests.
     */
    private static final boolean DEBUG_NON_BMP_METHODS = false;
    /**
     * The instance of index translation in use in this string. It will be set to either
     * {@link #BASIC} or and instance of {@link #Supplementary}.
     */
    private final IndexTranslator translator;
    protected String string; // cannot make final because of Python intern support
    /**
     * A singleton provides the translation service (which is a pass-through) for all BMP strings.
     */
    final IndexTranslator BASIC = new IndexTranslator() {

        @Override
        public int suppCount() {
            return 0;
        }

        @Override
        public int codePointIndex(int u) {
            return string.offsetByCodePoints(0, u);
//            return u;
        }

        @Override
        public int utf16Index(int i) {
            return string.codePointCount(0, i);
//            return i;
        }
    };
    protected transient boolean interned = false;

    // for PyJavaClass.init()
    public PyUnicode() {
        this(TYPE, "", true);
    }

    /**
     * Construct a PyUnicode interpreting the Java String argument as UTF-16.
     *
     * @param string UTF-16 string encoding the characters (as Java).
     */
    public PyUnicode(String string) {
        this(TYPE, string, false);
    }

    /**
     * Construct a PyUnicode interpreting the Java String argument as UTF-16. If it is known that
     * the string contains no supplementary characters, argument isBasic may be set true by the
     * caller. If it is false, the PyUnicode will scan the string to find out.
     *
     * @param string  UTF-16 string encoding the characters (as Java).
     * @param isBasic true if it is known that only BMP characters are present.
     */
    public PyUnicode(String string, boolean isBasic) {
        this(TYPE, string, isBasic);
    }

    public PyUnicode(PyType subtype, String string) {
        this(subtype, string, false);
    }

    public PyUnicode(PyBytes pystring) {
        this(TYPE, pystring);
    }

    public PyUnicode(PyType subtype, PyObject pystring) {
        this(subtype, //
                pystring.toString());
    }

    public PyUnicode(char c) {
        this(TYPE, String.valueOf(c), true);
    }

    public PyUnicode(int codepoint) {
        this(TYPE, new String(Character.toChars(codepoint)));
    }

    public PyUnicode(byte[] codepoints) {
        this(new String(codepoints, 0, codepoints.length));
    }

    public PyUnicode(int[] codepoints) {
        this(new String(codepoints, 0, codepoints.length));
    }

    PyUnicode(StringBuilder buffer) {
        this(TYPE, buffer.toString());
    }

    public PyUnicode(Iterator<Integer> iter) {
        this(fromCodePoints(iter));
    }

    public PyUnicode(Collection<Integer> ucs4) {
        this(ucs4.iterator());
    }

    public PyUnicode(CharSequence charSequence) {
        this(charSequence.toString());
    }

    /**
     * Fundamental all-features constructor on which the others depend. If it is known that the
     * string contains no supplementary characters, argument isBasic may be set true by the caller.
     * If it is false, the PyUnicode will scan the string to find out.
     *
     * @param subtype actual type to create.
     * @param string  UTF-16 string encoding the characters (as Java).
     * @param isBasic true if it is known that only BMP characters are present.
     */
    public PyUnicode(PyType subtype, String string, boolean isBasic) {
        super(subtype);
        this.string = string;
        translator = isBasic ? BASIC : this.chooseIndexTranslator();
    }

    private static StringBuilder fromCodePoints(Iterator<Integer> iter) {
        StringBuilder buffer = new StringBuilder();
        while (iter.hasNext()) {
            buffer.appendCodePoint(iter.next());
        }
        return buffer;
    }

    // ------------------------------------------------------------------------------------------
    // Index translation for Unicode beyond the BMP
    // ------------------------------------------------------------------------------------------

    /**
     * Creates a PyUnicode from an already interned String. Just means it won't be reinterned if
     * used in a place that requires interned Strings.
     */
    public static PyUnicode fromInterned(String interned) {
        PyUnicode uni = new PyUnicode(TYPE, interned);
        uni.interned = true;
        return uni;
    }

    public static String checkEncoding(String s) {
        if (s == null || s.chars().allMatch(c -> c < 127)) {
            return s;
        }
        return codecs.PyUnicode_EncodeASCII(s, s.length(), null);
    }

    @ExposedNew
    final static PyObject str_new(PyNewWrapper new_, boolean init, PyType subtype,
                                  PyObject[] args, String[] keywords) {
        ArgParser ap =
                new ArgParser("str", args, keywords, new String[]{"string", "encoding",
                        "errors"}, 0);
        PyObject x = ap.getPyObject(0, null);
        String encoding = ap.getString(1, null);
        String errors = ap.getString(2, null);
        ThreadState ts = Py.getThreadState();

        if (new_.for_type == subtype) {
            if (x == null) {
                return Py.EmptyUnicode;
            }

            if (encoding == null) {
                return Abstract.PyObject_Str(ts, x);
            }
            checkEncoding(errors);
            checkEncoding(encoding);

            if (x instanceof PyUnicode) {
                return new PyUnicode(((PyUnicode) x).getString());
            }
            if (x instanceof PyBytes) {
                if (x.getType() != PyBytes.TYPE && encoding == null && errors == null) {
                    return new PyUnicode((PyBytes) x);
                }
                PyObject decoded = codecs.decode((PyBytes) x, encoding, errors);
                if (decoded instanceof PyUnicode) {
                    return decoded;
                } else {
                    throw Py.TypeError("decoder did not return an unicode object (type="
                            + decoded.getType().fastGetName() + ")");
                }
            }
            return Abstract.PyObject_Str(ts, x);
        } else {
            if (x == null) {
                return new PyUnicodeDerived(subtype, Py.EmptyUnicode);
            }
            if (!(x instanceof PyUnicode)) {
                x = Abstract.PyObject_Str(ts, x);
            }
            return new PyUnicodeDerived(subtype, x);
        }
    }

    private static String padding(int n, int pad) {
        StringBuilder buffer = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            buffer.appendCodePoint(pad);
        }
        return buffer.toString();
    }

    private static int parse_fillchar(String function, String fillchar) {
        if (fillchar == null) {
            return ' ';
        }
        if (fillchar.codePointCount(0, fillchar.length()) != 1) {
            throw Py.TypeError(function + "() argument 2 must be char, not str");
        }
        return fillchar.codePointAt(0);
    }

    public static PyObject maketrans(PyUnicode fromstr, PyUnicode tostr) {
        return str_maketrans(TYPE, fromstr, tostr, null);
    }

    // ------------------------------------------------------------------------------------------

    public static PyObject maketrans(PyUnicode fromstr, PyUnicode tostr, PyUnicode other) {
        return str_maketrans(TYPE, fromstr, tostr, other);
    }

    @ExposedClassMethod(defaults = {"null"}, doc = BuiltinDocs.str_maketrans_doc)
    static final PyObject str_maketrans(PyType type, PyObject fromstr, PyObject tostr, PyObject other) {
        if (fromstr.__len__() != tostr.__len__()) {
            throw Py.ValueError("maketrans arguments must have same length");
        }
        if (!(fromstr instanceof PyUnicode))
            throw Py.TypeError(String.format("must be str, not %s", fromstr.TYPE));
        if (!(tostr instanceof PyUnicode))
            throw Py.TypeError(String.format("must be str, not %s", tostr.TYPE));
        if (other != null && !(other instanceof PyUnicode))
            throw Py.TypeError(String.format("must be str, not %s", other.TYPE));
        int[] fromCodePoints = ((PyUnicode) fromstr).toCodePoints();
        int[] toCodePoints = ((PyUnicode) tostr).toCodePoints();
        Map<PyObject, PyObject> tbl = new HashMap<>();
        for (int i = 0; i < fromCodePoints.length; i++) {
            tbl.put(new PyLong(fromCodePoints[i]), new PyLong(toCodePoints[i]));
        }

        if (other != null) {
            int[] codePoints = ((PyUnicode) other).toCodePoints();
            for (Integer code : codePoints) {
                tbl.put(new PyLong(code), Py.None);
            }
        }
        return new PyDictionary(tbl);
    }

    public String getString() {
        return string;
    }

    public int[] toCodePoints() {
        int n = getCodePointCount();
        int[] codePoints = new int[n];
        int i = 0;
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext(); i++) {
            codePoints[i] = iter.next();
        }
        return codePoints;
    }

    /**
     * Choose an {@link IndexTranslator} implementation for efficient working, according to the
     * contents of the {@link PyBytes#string}.
     *
     * @return chosen <code>IndexTranslator</code>
     */
    private IndexTranslator chooseIndexTranslator() {
        return BASIC;
//        int[] count = getSupplementaryCounts(string);
//        if (DEBUG_NON_BMP_METHODS) {
//            return new Supplementary(count);
//        } else {
//            return count == null ? BASIC : new Supplementary(count);
//        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * In the <code>PyUnicode</code> version, the arguments are code point indices, such as are
     * received from the Python caller, while the first two elements of the returned array have been
     * translated to UTF-16 indices in the implementation string.
     */
    protected int[] translateIndices(PyObject start, PyObject end) {
        int[] indices = Encoding.translateIndices(getString(), start, end, __len__());
        indices[0] = translator.codePointIndex(indices[0]);
        indices[1] = translator.codePointIndex(indices[1]);
        // indices[2] and [3] remain Unicode indices (and may be out of bounds) relative to len()
        return indices;
    }

    /**
     * {@inheritDoc} The indices are code point indices, not UTF-16 (<code>char</code>) indices. For
     * example:
     * <p>
     * <pre>
     * PyUnicode u = new PyUnicode("..\ud800\udc02\ud800\udc03...");
     * // (Python) u = u'..\U00010002\U00010003...'
     *
     * String s = u.substring(2, 4);  // = "\ud800\udc02\ud800\udc03" (Java)
     * </pre>
     */
    public String substring(int start, int end) {
        return getString().substring(translator.codePointIndex(start), translator.codePointIndex(end));
    }

    public String substring(int start) {
        return getString().substring(translator.codePointIndex(start));
    }

    /**
     * {@inheritDoc}
     *
     * @return true if the string consists only of BMP characters
     */
    public boolean isBasicPlane() {
        return string.length() == getCodePointCount();
    }

    public int getCodePointCount() {
        return string.codePointCount(0, string.length());
    }

    public PyUnicode createInstance(String str) {
        return new PyUnicode(str);
    }

    /**
     * @param string  UTF-16 string encoding the characters (as Java).
     * @param isBasic true if it is known that only BMP characters are present.
     */
    protected PyUnicode createInstance(String string, boolean isBasic) {
        return new PyUnicode(string, isBasic);
    }

    @ExposedMethod(doc = BuiltinDocs.str___mod___doc)
    final PyObject str___mod__(PyObject other) {
        StringFormatter fmt = new StringFormatter(getString(), true);
        return fmt.format(other);
    }

    @ExposedMethod(doc = BuiltinDocs.str___str___doc)
    final PyUnicode str___str__() {
        return new PyUnicode(getString());
    }

    @ExposedMethod(doc = BuiltinDocs.str___len___doc)
    final int str___len__() {
        return getCodePointCount();
    }

    public PyUnicode __repr__() {
        return str___repr__();
    }

    @Override
    public String toString() {
        return getString();
    }

    @Override
    public int hashCode() {
        return str___hash__();
    }

    @Override
    public int __len__() {
        return str___len__();
    }

    @Override
    public PyObject _is(PyObject o) {
        return Py.newBoolean(this == o);
    }

    @Override
    public PyComplex __complex__() {
        return Encoding.atocx(encodeDecimal());
    }

    public PyObject __mod__(PyObject other) {
        return str___mod__(other);
    }

    @Override
    public String asString(int index) throws PyObject.ConversionException {
        return getString();
    }

    @Override
    public String asString() {
        return getString();
    }

    @Override
    public String asName(int index) throws PyObject.ConversionException {
        return internedString();
    }

    @ExposedMethod(doc = BuiltinDocs.str___repr___doc)
    final PyUnicode str___repr__() {
        return new PyUnicode(encode_UnicodeEscape(getString(), true));
    }

    @ExposedMethod(doc = BuiltinDocs.str___getitem___doc)
    final PyObject str___getitem__(PyObject index) {
        if (index instanceof PySlice) {
            int[] indices = ((PySlice) index).indicesEx(__len__());
            return getslice(indices[0], indices[1], indices[2]);
        }
        int idx = index.asIndex();
        if (idx >= string.length()) {
            throw Py.IndexError(String.format("string index out of range"));
        }
        if (idx < 0) {
            idx += string.length();
        }
        int codepoint = string.codePointAt(string.offsetByCodePoints(0, idx));
        return new PyUnicode(new String(Character.toChars(codepoint)));
    }

    @ExposedMethod(doc = BuiltinDocs.str___iter___doc)
    public final PyObject str___iter__() {
        return seq___iter__();
    }

    @ExposedMethod(doc = BuiltinDocs.str___hash___doc)
    final int str___hash__() {
        return getString().hashCode();
    }

    @Override
    protected PyObject pyget(int i) {
        int codepoint = getString().codePointAt(translator.codePointIndex(i));
        return new PyUnicode(codepoint);
    }

    @Override
    public PyObject getslice(int start, int stop, int step) {
        if (isBasicPlane()) {
            CharSequence s = Encoding.getslice(getString(), start, stop, step, sliceLength(start, stop, step));
            return new PyUnicode(s);
        }
        if (step > 0 && stop < start) {
            stop = start;
        }

        StringBuilder buffer = new StringBuilder(sliceLength(start, stop, step));
        for (Iterator<Integer> iter = newSubsequenceIterator(start, stop, step); iter.hasNext(); ) {
            buffer.appendCodePoint(iter.next());
        }
        return createInstance(buffer.toString());
    }

    @Override
    protected PyObject repeat(int count) {
        if (count < 0) {
            count = 0;
        }
        String s = getString();
        int len = s.length();
        if ((long) len * count > Integer.MAX_VALUE) {
            // Since Strings store their data in an array, we can't make one
            // longer than Integer.MAX_VALUE. Without this check we get
            // NegativeArraySize Exceptions when we create the array on the
            // line with a wrapped int.
            throw Py.OverflowError("max str len is " + Integer.MAX_VALUE);
        }
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < count; i++) {
            ret.append(s);
        }
        return new PyUnicode(ret);
    }

    @Override
    public PyObject richCompare(PyObject other, CompareOp op) {
        if (!(other instanceof PyUnicode)) {
            if (op == CompareOp.EQ) {
                return Py.False;
            } else if (op == CompareOp.NE) {
                return Py.True;
            }
        }
        if (!(other instanceof PyUnicode)) {
            throw Py.TypeError(String.format("'%s' not supported between instances of 'str' and '%s'",
                    op.toString(), other.getType().fastGetName()));
        }
        String s = ((PyUnicode) other).getString();
        int ret = getString().compareTo(s);
        if (ret < -1) ret = -1;
        return op.bool(ret);
    }

    @Override
    public Object __tojava__(Class<?> c) {
        if (c.isAssignableFrom(String.class)) {
            return getString();
        }

        if (c == Character.TYPE || c == Character.class) {
            if (getString().length() == 1) {
                return Character.valueOf(getString().charAt(0));
            }
        }

        if (c.isArray()) {
            if (c.getComponentType() == Byte.TYPE) {
                return getString().getBytes();
            }
            if (c.getComponentType() == Character.TYPE) {
                return getString().toCharArray();
            }
        }

        if (c.isAssignableFrom(Collection.class)) {
            List<Object> list = new ArrayList();
            for (int i = 0; i < __len__(); i++) {
                list.add(pyget(i).__tojava__(String.class));
            }
            return list;
        }

        if (c.isInstance(this)) {
            return this;
        }

        return Py.NoConversion;
    }

    public int getInt(int i) {
        return getString().codePointAt(string.offsetByCodePoints(0, i));
    }

    // XXX: Parameterize SubsequenceIteratorImpl and friends (and make them Iterable)
    public Iterator<Integer> newSubsequenceIterator() {
        return new SubsequenceIteratorImpl();
    }

    public Iterator<Integer> newSubsequenceIterator(int start, int stop, int step) {
        if (step < 0) {
            return new SteppedIterator(step * -1, new ReversedIterator(new SubsequenceIteratorImpl(
                    stop + 1, start + 1, 1)));
        } else {
            return new SubsequenceIteratorImpl(start, stop, step);
        }
    }

    /**
     * Helper used many times to "coerce" a method argument into a <code>PyUnicode</code> (which it
     * may already be). A <code>null</code> or incoercible argument will raise a
     * <code>TypeError</code>.
     *
     * @param o the object to coerce
     * @return an equivalent <code>PyUnicode</code> (or o itself)
     */
    private PyUnicode coerceToUnicode(PyObject o) {
        if (o instanceof PyUnicode) {
            return (PyUnicode) o;
        } else if (o instanceof PyBytes) {
            throw Py.TypeError("Can't convert 'bytes' object to str implicitly");
        } else if (o instanceof BufferProtocol) {
            // PyByteArray, PyMemoryView
            try (PyBuffer buf = ((BufferProtocol) o).getBuffer(PyBUF.FULL_RO)) {
                return new PyUnicode(buf.toString(), true);
            }
        } else {
            // o is some type not allowed:
            if (o == null) {
                // Do something safe and approximately right
                o = Py.None;
            }
            throw Py.TypeError("coercing to Unicode: need string or buffer, "
                    + o.getType().fastGetName() + " found");
        }
    }

    // compliance requires that we need to support a bit of inconsistency
    // compared to other coercion used

    /**
     * Helper used many times to "coerce" a method argument into a <code>PyUnicode</code> (which it
     * may already be). A <code>null</code> argument or a <code>PyNone</code> causes
     * <code>null</code> to be returned.
     *
     * @param o the object to coerce
     * @return an equivalent <code>PyUnicode</code> (or o itself, or <code>null</code>)
     */
    private PyUnicode coerceToUnicodeOrNull(PyObject o) {
        if (o == null || o == Py.None) {
            return null;
        } else {
            return coerceToUnicode(o);
        }
    }

    @ExposedSlot(SlotFunc.CONTAINS)
    public static boolean str___contains__(PyObject self, PyObject o) {
        if (!(o instanceof PyUnicode)) {
            throw Py.TypeError(String.format("'in <string>' requires string as left operand, not %s", o.getType().fastGetName()));
        }
        String other = Encoding.asUTF16StringOrError(o);
        return ((PyUnicode) self).getString().indexOf(other) >= 0;
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.str___mul___doc)
    final PyObject str___mul__(PyObject o) {
        if (!o.isIndex()) {
            return null;
        }
        return repeat(o.asIndex(Py.OverflowError));
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.str___rmul___doc)
    public final PyObject str___rmul__(PyObject o) {
        if (!o.isIndex()) {
            return null;
        }
        return repeat(o.asIndex(Py.OverflowError));
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.str___add___doc)
    public final PyObject str___add__(PyObject other) {
        PyUnicode otherUnicode;
        if (other instanceof PyUnicode) {
            otherUnicode = (PyUnicode) other;
            return new PyUnicode(getString().concat(otherUnicode.getString()));
        } else if (other instanceof PyBytes) {
            throw Py.TypeError("cannot convert 'bytes' object to str implicitly");
        }
        throw Py.TypeError(String.format("must be str, not %s", other.getType().fastGetName()));
    }

    @ExposedMethod(doc = BuiltinDocs.str_lower_doc)
    public final PyObject str_lower() {
        StringBuffer buffer = new StringBuffer(getString().length());
        for (int i = 0; i < __len__(); i++) {
            buffer.appendCodePoint(Character.toLowerCase(string.codePointAt(i)));
        }
        return new PyUnicode(buffer);
    }

    @ExposedMethod(doc = BuiltinDocs.str_upper_doc)
    public final PyObject str_upper() {
        return new PyUnicode(getString().toUpperCase());
    }

    @ExposedMethod(doc = BuiltinDocs.str_title_doc)
    public final PyObject str_title() {
        if (isBasicPlane()) {
            return new PyUnicode(Encoding.title(getString()));
        }
        StringBuilder buffer = new StringBuilder(getString().length());
        boolean previous_is_cased = false;
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext(); ) {
            int codePoint = iter.next();
            if (previous_is_cased) {
                buffer.appendCodePoint(Character.toLowerCase(codePoint));
            } else {
                buffer.appendCodePoint(Character.toTitleCase(codePoint));
            }

            if (Character.isLowerCase(codePoint) || Character.isUpperCase(codePoint)
                    || Character.isTitleCase(codePoint)) {
                previous_is_cased = true;
            } else {
                previous_is_cased = false;
            }
        }
        return new PyUnicode(buffer);
    }

    @ExposedMethod(doc = BuiltinDocs.str_swapcase_doc)
    public final PyObject str_swapcase() {
        if (isBasicPlane()) {
            return new PyUnicode(Encoding.swapcase(getString()));
        }
        StringBuilder buffer = new StringBuilder(getString().length());
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext(); ) {
            int codePoint = iter.next();
            if (Character.isUpperCase(codePoint)) {
                buffer.appendCodePoint(Character.toLowerCase(codePoint));
            } else if (Character.isLowerCase(codePoint)) {
                buffer.appendCodePoint(Character.toUpperCase(codePoint));
            } else {
                buffer.appendCodePoint(codePoint);
            }
        }
        return new PyUnicode(buffer);
    }

    /**
     * Helper used in <code>.strip()</code> to "coerce" a method argument into a
     * <code>PyUnicode</code> (which it may already be). A <code>null</code> argument or a
     * <code>PyNone</code> causes <code>null</code> to be returned. A buffer type is not acceptable
     * to (Unicode) <code>.strip()</code>. This is the difference from
     * {@link #coerceToUnicodeOrNull(PyObject)}.
     *
     * @param o the object to coerce
     * @return an equivalent <code>PyUnicode</code> (or o itself, or <code>null</code>)
     */
    private PyUnicode coerceStripSepToUnicode(PyObject o) {
        if (o == null) {
            return null;
        } else if (o instanceof PyUnicode) {
            return (PyUnicode) o;
        } else if (o instanceof PyBytes) {
            return new PyUnicode(((PyBytes) o).decode().toString());
        } else if (o == Py.None) {
            return null;
        } else {
            throw Py.TypeError("strip arg must be None, unicode or str");
        }
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.str_strip_doc)
    public final PyObject str_strip(PyObject sepObj) {

        PyUnicode sep = coerceStripSepToUnicode(sepObj);

        if (isBasicPlane()) {
            // this contains only basic plane characters
            if (sep == null) {
                // And we're stripping whitespace, so use the PyBytes implementation
                return new PyUnicode(Encoding._strip(getString()));
            } else if (sep.isBasicPlane()) {
                // And the strip characters are basic plane too, so use the PyBytes implementation
                return new PyUnicode(Encoding._strip(getString(), sep.getString()));
            }
        }

        // Not basic plane: have to do real Unicode
        return new PyUnicode(new ReversedIterator(new StripIterator(sep, new ReversedIterator(
                new StripIterator(sep, newSubsequenceIterator())))));
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.str_lstrip_doc)
    public final PyObject str_lstrip(PyObject sepObj) {

        PyUnicode sep = coerceStripSepToUnicode(sepObj);

        if (isBasicPlane()) {
            // this contains only basic plane characters
            if (sep == null) {
                // And we're stripping whitespace, so use the PyBytes implementation
                return new PyUnicode(Encoding._lstrip(getString()));
            } else if (sep.isBasicPlane()) {
                // And the strip characters are basic plane too, so use the PyBytes implementation
                return new PyUnicode(Encoding._lstrip(getString(), sep.getString()));
            }
        }

        // Not basic plane: have to do real Unicode
        return new PyUnicode(new StripIterator(sep, newSubsequenceIterator()));
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.str_rstrip_doc)
    public final PyObject str_rstrip(PyObject sepObj) {

        PyUnicode sep = coerceStripSepToUnicode(sepObj);

        if (isBasicPlane()) {
            // this contains only basic plane characters
            if (sep == null) {
                // And we're stripping whitespace, so use the PyBytes implementation
                return new PyUnicode(Encoding._rstrip(getString()));
            } else if (sep.isBasicPlane()) {
                // And the strip characters are basic plane too, so use the PyBytes implementation
                return new PyUnicode(Encoding._rstrip(getString(), sep.getString()));
            }
        }

        // Not basic plane: have to do real Unicode
        return new PyUnicode(new ReversedIterator(new StripIterator(sep, new ReversedIterator(
                newSubsequenceIterator()))));
    }

    @ExposedMethod(doc = BuiltinDocs.str_partition_doc)
    public final PyTuple str_partition(ThreadState ts, PyObject sepObj) {
        PyUnicode strObj = this;
        String str = strObj.getString();

        // Will throw a TypeError if not a basestring
        String sep = sepObj.asString();
        sepObj = Abstract.PyObject_Str(ts, sepObj);

        if (sep.length() == 0) {
            throw Py.ValueError("empty separator");
        }

        int index = str.indexOf(sep);
        if (index != -1) {
            return new PyTuple(new PyUnicode(strObj.substring(0, index)), sepObj, new PyUnicode(strObj.substring(index
                    + sep.length())));
        } else {
            return new PyTuple(this, Py.EmptyUnicode, Py.EmptyUnicode);
        }
    }

    private SplitIterator newSplitIterator(PyUnicode sep, int maxsplit) {
        if (sep == null) {
            return new WhitespaceSplitIterator(maxsplit);
        } else if (sep.getCodePointCount() == 0) {
            throw Py.ValueError("empty separator");
        } else {
            return new SepSplitIterator(sep, maxsplit);
        }
    }

    @ExposedMethod(doc = BuiltinDocs.str_rpartition_doc)
    public final PyTuple str_rpartition(ThreadState ts, PyObject sepObj) {
        PyUnicode strObj = this;
        String str = strObj.getString();

        // Will throw a TypeError if not a basestring
        String sep = sepObj.asString();
        sepObj = Abstract.PyObject_Str(ts, sepObj);

        if (sep.length() == 0) {
            throw Py.ValueError("empty separator");
        }

        int index = str.lastIndexOf(sep);
        if (index != -1) {
            return new PyTuple(new PyUnicode(strObj.substring(0, index)), sepObj, new PyUnicode(strObj.substring(index
                    + sep.length())));
        } else {
            PyUnicode emptyUnicode = Py.newUnicode("");
            return new PyTuple(emptyUnicode, emptyUnicode, this);
        }
    }

    @ExposedMethod(doc = BuiltinDocs.str_split_doc)
    public final PyList str_split(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("split", args, keywords, "sep", "maxsplit");
        String sep = ap.getString(0, null);
        int maxsplit = ap.getInt(1, -1);
        Collection<CharSequence> list = Encoding._split(getString(), sep, maxsplit);
        return new PyList(list.stream().map(PyUnicode::new).collect(Collectors.toList()));
    }

    @ExposedMethod(doc = BuiltinDocs.str_rsplit_doc)
    public final PyList str_rsplit(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("rsplit", args, keywords, "sep", "maxsplit");
        String sep = ap.getString(0, null);
        int maxsplit = ap.getInt(1, -1);
        Collection<CharSequence> list = Encoding._rsplit(getString(), sep, maxsplit);
        return new PyList(list.stream().map(PyUnicode::new).collect(Collectors.toList()));
    }

    @ExposedMethod(doc = BuiltinDocs.str_splitlines_doc)
    public final PyList str_splitlines(PyObject[] args, String[] keywords) {
        ArgParser arg = new ArgParser("splitlines", args, keywords, "keepends");
        boolean keepends = arg.getPyObject(0, Py.False).isTrue();
        if (isBasicPlane()) {
            List<CharSequence> list = Encoding.splitlines(getString(), keepends);
            PyList l = new PyList();
            for (CharSequence s : list) {
                l.append(new PyUnicode(s));
            }
            return l;
        }
        return new PyList(new LineSplitIterator(keepends));

    }

    protected PyUnicode fromSubstring(int begin, int end) {
        assert (isBasicPlane()); // can only be used on a codepath from str_ equivalents
        return new PyUnicode(getString().substring(begin, end), true);
    }

    @ExposedMethod(defaults = {"null", "null"}, doc = BuiltinDocs.str_index_doc)
    public final int str_index(PyObject subObj, PyObject start, PyObject end) {
        final PyUnicode sub = coerceToUnicode(subObj);
        // Now use the mechanics of the PyBytes on the UTF-16 of the PyUnicode.
        return checkIndex(Encoding._find(getString(), sub.getString(), start, end, __len__()));
    }

    @ExposedMethod(defaults = {"null", "null"}, doc = BuiltinDocs.str_index_doc)
    public final int str_rindex(PyObject subObj, PyObject start, PyObject end) {
        final PyUnicode sub = coerceToUnicode(subObj);
        // Now use the mechanics of the PyBytes on the UTF-16 of the PyUnicode.
        return checkIndex(Encoding._rfind(getString(), sub.getString(), start, end, __len__()));
    }

    @ExposedMethod(defaults = {"null", "null"}, doc = BuiltinDocs.str_count_doc)
    public final int str_count(PyObject subObj, PyObject start, PyObject end) {
        final PyUnicode sub = coerceToUnicode(subObj);
        if (isBasicPlane()) {
            return Encoding._count(getString(), sub.getString(), start, end, __len__());
        }
        int[] indices = Encoding.translateIndices(getString(), start, end, __len__()); // do not convert to utf-16 indices.
        int count = 0;
        for (Iterator<Integer> mainIter = newSubsequenceIterator(indices[0], indices[1], 1); mainIter
                .hasNext(); ) {
            int matched = sub.getCodePointCount();
            for (Iterator<Integer> subIter = sub.newSubsequenceIterator(); mainIter.hasNext()
                    && subIter.hasNext(); ) {
                if (mainIter.next() != subIter.next()) {
                    break;
                }
                matched--;

            }
            if (matched == 0) {
                count++;
            }
        }
        return count;
    }

    @ExposedMethod(defaults = {"null", "null"}, doc = BuiltinDocs.str_find_doc)
    public final int str_find(PyObject subObj, PyObject start, PyObject end) {
        int found = Encoding._find(getString(), coerceToUnicode(subObj).getString(), start, end, __len__());
        return found < 0 ? -1 : translator.codePointIndex(found);
    }

    @ExposedMethod(defaults = {"null", "null"}, doc = BuiltinDocs.str_rfind_doc)
    public final int str_rfind(PyObject subObj, PyObject start, PyObject end) {
        int found = Encoding._rfind(getString(), coerceToUnicode(subObj).getString(), start, end, __len__());
        return found < 0 ? -1 : translator.codePointIndex(found);
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.str_ljust_doc)
    public final PyObject str_ljust(int width, String padding) {
        int n = width - getCodePointCount();
        if (n <= 0) {
            return new PyUnicode(getString());
        } else {
            return new PyUnicode(getString() + padding(n, parse_fillchar("ljust", padding)));
        }
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.str_rjust_doc)
    public final PyObject str_rjust(int width, String padding) {
        int n = width - getCodePointCount();
        if (n <= 0) {
            return new PyUnicode(getString());
        } else {
            return new PyUnicode(padding(n, parse_fillchar("ljust", padding)) + getString());
        }
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.str_center_doc)
    public final PyObject str_center(int width, String padding) {
        int n = width - getCodePointCount();
        if (n <= 0) {
            return new PyUnicode(getString());
        }
        int half = n / 2;
        if (n % 2 > 0 && width % 2 > 0) {
            half += 1;
        }
        int pad = parse_fillchar("center", padding);
        return new PyUnicode(padding(half, pad) + getString() + padding(n - half, pad));
    }

    @ExposedMethod(doc = BuiltinDocs.str_zfill_doc)
    public final PyObject str_zfill(int width) {
        int n = getCodePointCount();
        if (n >= width) {
            return new PyUnicode(getString());
        }
        if (isBasicPlane()) {
            return new PyUnicode(Encoding.zfill(getString(), width));
        }
        StringBuilder buffer = new StringBuilder(width);
        int nzeros = width - n;
        boolean first = true;
        boolean leadingSign = false;
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext(); ) {
            int codePoint = iter.next();
            if (first) {
                first = false;
                if (codePoint == '+' || codePoint == '-') {
                    buffer.appendCodePoint(codePoint);
                    leadingSign = true;
                }
                for (int i = 0; i < nzeros; i++) {
                    buffer.appendCodePoint('0');
                }
                if (!leadingSign) {
                    buffer.appendCodePoint(codePoint);
                }
            } else {
                buffer.appendCodePoint(codePoint);
            }
        }
        if (first) {
            for (int i = 0; i < nzeros; i++) {
                buffer.appendCodePoint('0');
            }
        }
        return new PyUnicode(buffer);
    }

    @ExposedMethod(defaults = "8", doc = BuiltinDocs.str_expandtabs_doc)
    public final PyObject str_expandtabs(int tabsize) {
        return new PyUnicode(Encoding.expandtabs(getString(), tabsize));
    }

    @ExposedMethod(doc = BuiltinDocs.str_capitalize_doc)
    public final PyObject str_capitalize() {
        if (getString().length() == 0) {
            return this;
        }
        if (isBasicPlane()) {
            return new PyUnicode(Encoding.capitalize(getString()));
        }
        StringBuilder buffer = new StringBuilder(getString().length());
        boolean first = true;
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext(); ) {
            if (first) {
                buffer.appendCodePoint(Character.toUpperCase(iter.next()));
                first = false;
            } else {
                buffer.appendCodePoint(Character.toLowerCase(iter.next()));
            }
        }
        return new PyUnicode(buffer);
    }

    @ExposedMethod(defaults = "-1", doc = BuiltinDocs.str_replace_doc)
    public final PyObject str_replace(PyObject oldPieceObj, PyObject newPieceObj, int count) {

        // Convert other argument types to PyUnicode (or error)
        PyUnicode newPiece = coerceToUnicode(newPieceObj);
        PyUnicode oldPiece = coerceToUnicode(oldPieceObj);

        if (isBasicPlane() && newPiece.isBasicPlane() && oldPiece.isBasicPlane()) {
            // Use the mechanics of PyBytes, since all is basic plane
            return new PyUnicode(Encoding._replace(getString(), oldPiece.getString(), newPiece.getString(), count));

        } else {
            // A Unicode-specific implementation is needed working in code points
            StringBuilder buffer = new StringBuilder();

            if (oldPiece.getCodePointCount() == 0) {
                Iterator<Integer> iter = newSubsequenceIterator();
                for (int i = 1; (count == -1 || i < count) && iter.hasNext(); i++) {
                    if (i == 1) {
                        buffer.append(newPiece.getString());
                    }
                    buffer.appendCodePoint(iter.next());
                    buffer.append(newPiece.getString());
                }
                while (iter.hasNext()) {
                    buffer.appendCodePoint(iter.next());
                }
                return new PyUnicode(buffer);

            } else {
                SplitIterator iter = newSplitIterator(oldPiece, count);
                int numSplits = 0;
                while (iter.hasNext()) {
                    buffer.append(((PyUnicode) iter.next()).getString());
                    if (iter.hasNext()) {
                        buffer.append(newPiece.getString());
                    }
                    numSplits++;
                }
                if (iter.getEndsWithSeparator() && (count == -1 || numSplits <= count)) {
                    buffer.append(newPiece.getString());
                }
                return new PyUnicode(buffer);
            }
        }
    }

    // end utf-16 aware
    public PyObject join(PyObject seq) {
        return str_join(seq);
    }

    @ExposedMethod(doc = BuiltinDocs.str_join_doc)
    public final PyUnicode str_join(PyObject seq) {
        return unicodeJoin(seq);
    }

    final PyUnicode unicodeJoin(PyObject obj) {
        StringJoiner joiner = new StringJoiner(getString());
        PyObject item;
        long totalSize = 0;
        PyObject iter = PyObject.getIter(obj);
        try {
            for (int i = 0; (item = PyObject.iterNext(iter)) != null; i++) {
                if (!(item instanceof PyUnicode)) {
                    throw Py.TypeError(String.format("sequence item %d: expected str instance, %s found",
                            i, item.getType().fastGetName()));
                }
                String s = ((PyUnicode) item).getString();
                totalSize += s.length();
                // A string cannot be longer than the maximum array length
                if (totalSize > Integer.MAX_VALUE) {
                    throw Py.OverflowError("max str len is " + Integer.MAX_VALUE);
                }
                joiner.add(s);
            }
        } catch (PyException e) {
            if (!e.match(Py.StopIteration)) throw e;
        }
        return new PyUnicode(joiner.toString());
    }

    @ExposedMethod(defaults = {"null", "null"}, doc = BuiltinDocs.str_startswith_doc)
    public final boolean str_startswith(PyObject prefix, PyObject start, PyObject end) {
        if (prefix instanceof PyTuple) {
            for (PyObject prefixObj : ((PyTuple) prefix).getArray()) {
                if (!(prefixObj instanceof PyUnicode)) {
                    throw Py.TypeError(String.format("Can't convert '%s' object to str implicitly",
                            prefixObj.getType().fastGetName()));
                }
            }
        } else if (!(prefix instanceof PyUnicode)) {
            throw Py.TypeError(String.format("startswith first arg must be str or a tuple of str, not %s",
                    prefix.getType().fastGetName()));
        }
        return Encoding.startswith(getString(), prefix, start, end, __len__());
    }

    @ExposedMethod(defaults = {"null", "null"}, doc = BuiltinDocs.str_endswith_doc)
    public final boolean str_endswith(PyObject suffix, PyObject start, PyObject end) {
        if (!(suffix instanceof PyUnicode) && !(suffix instanceof PyTuple)) {
            throw Py.TypeError(String.format("endswith first arg must be str or a tuple of str, not %s",
                    suffix.getType().fastGetName()));
        }
        return Encoding.endswith(getString(), suffix, start, end, __len__());
    }

    @ExposedMethod(doc = BuiltinDocs.str_translate_doc)
    public final PyObject str_translate(PyObject table) {
        return _codecs.translateCharmap(this, "ignore", table);
    }

    @ExposedMethod(doc = BuiltinDocs.str_islower_doc)
    public final boolean str_islower() {
        if (isBasicPlane()) {
            return Encoding.isLowercase(getString().trim());
        }
        return _none(codepoint -> Character.isUpperCase(codepoint) || Character.isTitleCase(codepoint));
    }

    @ExposedMethod(doc = BuiltinDocs.str_isupper_doc)
    public final boolean str_isupper() {
        if (isBasicPlane()) {
            return Encoding.isUppercase(getString().trim());
        }
        return _none(codepoint -> UCharacter.isULowercase(codepoint) || UCharacter.isTitleCase(codepoint));
    }

    @ExposedMethod(doc = BuiltinDocs.str_isidentifier_doc)
    public final boolean str_isidentifier() {
        if (getCodePointCount() == 0) {
            return false;
        }
        Iterator<Integer> iter = newSubsequenceIterator();
        int first = iter.next();
        if (!Character.isUnicodeIdentifierStart(first) && first != 0x5F) {
            return false;
        }
        for (; iter.hasNext(); ) {
            if (!Character.isUnicodeIdentifierPart(iter.next())) {
                return false;
            }
        }
        return true;
    }

    @ExposedMethod(doc = BuiltinDocs.str_isalpha_doc)
    public final boolean str_isalpha() {
        if (isBasicPlane()) {
            return Encoding.isAlpha(getString());
        }
        return _all(codepoint -> UCharacter.isLetter(codepoint));
    }

    @ExposedMethod(doc = BuiltinDocs.str_isalnum_doc)
    public final boolean str_isalnum() {
        if (isBasicPlane()) {
            return Encoding.isAlnum(getString());
        }
        return _all(codepoint -> Character.isLetterOrDigit(codepoint) ||
                Character.getType(codepoint) == Character.LETTER_NUMBER);
    }

    @ExposedMethod(doc = BuiltinDocs.str_isdecimal_doc)
    public final boolean str_isdecimal() {
        if (isBasicPlane()) {
            return Encoding.isDecimal(getString());
        }
        return _all(codepoint -> Character.getType(codepoint) == Character.DECIMAL_DIGIT_NUMBER);
    }

    @ExposedMethod(doc = BuiltinDocs.str_isdigit_doc)
    public final boolean str_isdigit() {
        if (isBasicPlane()) {
            return Encoding.isDigit(getString());
        }
        return _all(codepoint -> UCharacter.isDigit(codepoint));
    }

    @ExposedMethod(doc = BuiltinDocs.str_isnumeric_doc)
    public final boolean str_isnumeric() {
        if (isBasicPlane()) {
            return Encoding.isNumeric(getString());
        }
        return _none(codepoint -> {
            int type = Character.getType(codepoint);
            return type != Character.DECIMAL_DIGIT_NUMBER && type != Character.LETTER_NUMBER
                    && type != Character.OTHER_NUMBER;
        });
    }

    @ExposedMethod(doc = BuiltinDocs.str_istitle_doc)
    public final boolean str_istitle() {
        if (isBasicPlane()) {
            return Encoding.isTitle(getString());
        }
        if (getCodePointCount() == 0) {
            return false;
        }
        boolean cased = false;
        boolean previous_is_cased = false;
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext(); ) {
            int codePoint = iter.next();
            if (Character.isUpperCase(codePoint) || Character.isTitleCase(codePoint)) {
                if (previous_is_cased) {
                    return false;
                }
                previous_is_cased = true;
                cased = true;
            } else if (Character.isLowerCase(codePoint)) {
                if (!previous_is_cased) {
                    return false;
                }
                previous_is_cased = true;
                cased = true;
            } else {
                previous_is_cased = false;
            }
        }
        return cased;
    }

    @ExposedMethod(doc = BuiltinDocs.str_isspace_doc)
    public final boolean str_isspace() {
        if (isBasicPlane()) {
            return Encoding.isSpace(getString());
        }
        return _all(c -> Character.isWhitespace(c));
    }

    @ExposedMethod(doc = BuiltinDocs.str_isprintable_doc)
    public final boolean str_isprintable() {
        if (getCodePointCount() == 0) return true;
        return _all(codepoint -> UCharacter.isPrintable(codepoint));
    }

    @ExposedMethod(doc = BuiltinDocs.str_casefold_doc)
    public final PyUnicode str_casefold() {
        return new PyUnicode(_map(new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer input) {
                return Character.toLowerCase(input);
            }
        }));
    }

    public String encode() {
        return encode("UTF-8", null);
    }

    public String encode(String encoding) {
        return encode(encoding, null);
    }

    public String encode(String encoding, String errors) {
        return codecs.encode(this, encoding, errors);
    }

    @ExposedMethod(doc = BuiltinDocs.str_encode_doc)
    public final PyBytes str_encode(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("encode", args, keywords, "encoding", "errors");
        String encoding = ap.getString(0, "UTF-8");
        String errors = ap.getString(1, null);
        return new PyBytes(encode(encoding, errors));
    }

    @ExposedMethod(doc = BuiltinDocs.str___getnewargs___doc)
    public final PyTuple str___getnewargs__() {
        return new PyTuple(new PyUnicode(this.getString()));
    }

    @ExposedMethod(doc = BuiltinDocs.str___format___doc)
    public final PyObject str___format__(PyObject formatSpec) {
        return Encoding.format(getString(), formatSpec, false);
    }

    @ExposedMethod(doc = BuiltinDocs.str_format_doc)
    public final PyObject str_format(PyObject[] args, String[] keywords) {
        try {
            return new PyUnicode(buildFormattedString(args, keywords, null, null));
        } catch (IllegalArgumentException e) {
            throw Py.ValueError(e.getMessage());
        }
    }

    @Override
    public Iterator<Integer> iterator() {
        return newSubsequenceIterator();
    }

    public PyObject atol(int base) {
        return Encoding.atol(encodeDecimal(), base);
    }

    /**
     * Encode unicode into a valid decimal String. Throws a UnicodeEncodeError on invalid
     * characters.
     *
     * @return a valid decimal as an encoded String
     */
    public String encodeDecimal() {
        if (isBasicPlane()) {
            return encodeDecimalBasic();
        }

        int digit;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext(); i++) {
            int codePoint = iter.next();
            if (Character.isWhitespace(codePoint)) {
                sb.append(' ');
                continue;
            }
            digit = Character.digit(codePoint, 10);
            if (digit >= 0) {
                sb.append(digit);
                continue;
            }
            if (0 < codePoint && codePoint < 256) {
                sb.appendCodePoint(codePoint);
                continue;
            }
            // All other characters are considered unencodable
            codecs.encoding_error("strict", "decimal", getString(), i, i + 1,
                    "invalid decimal Unicode string");
        }
        return sb.toString();
    }

    /**
     * Encode unicode in the basic plane into a valid decimal String. Throws a UnicodeEncodeError on
     * invalid characters.
     *
     * @return a valid decimal as an encoded String
     */
    private String encodeDecimalBasic() {
        int digit;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < getString().length(); i++) {
            char ch = getString().charAt(i);
            if (Character.isWhitespace(ch)) {
                sb.append(' ');
                continue;
            }
            digit = Character.digit(ch, 10);
            if (digit >= 0) {
                sb.append(digit);
                continue;
            }
            if (0 < ch && ch < 256) {
                sb.append(ch);
                continue;
            }
            // All other characters are considered unencodable
            codecs.encoding_error("strict", "decimal", getString(), i, i + 1,
                    "invalid decimal Unicode string");
        }
        return sb.toString();
    }

    private boolean _all(Predicate<Integer> predicate) {
        if (getCodePointCount() == 0) {
            return false;
        }
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext(); ) {
            int codepoint = iter.next();
            if (!predicate.test(codepoint)) return false;
        }
        return true;
    }

    private boolean _none(Predicate<Integer> predicate) {
        if (getCodePointCount() == 0) {
            return false;
        }
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext(); ) {
            int codepoint = iter.next();
            if (predicate.test(codepoint)) return false;
        }
        return true;
    }

    private String _map(Function<Integer, Integer> func) {
        StringBuilder res = new StringBuilder(__len__());
        for (Iterator<Integer> iter = newSubsequenceIterator(); iter.hasNext(); ) {
            int codepoint = iter.next();
            res.appendCodePoint(func.apply(codepoint));
        }
        return res.toString();
    }

    /**
     * Implements PEP-3101 {}-formatting methods <code>str.format()</code> and
     * <code>unicode.format()</code>. When called with <code>enclosingIterator == null</code>, this
     * method takes this object as its formatting string. The method is also called (calls itself)
     * to deal with nested formatting sepecifications. In that case, <code>enclosingIterator</code>
     * is a {@link MarkupIterator} on this object and <code>value</code> is a substring of this
     * object needing recursive transaltion.
     *
     * @param args              to be interpolated into the string
     * @param keywords          for the trailing args
     * @param enclosingIterator when used nested, null if subject is this <code>PyBytes</code>
     * @param value             the format string when <code>enclosingIterator</code> is not null
     * @return the formatted string based on the arguments
     */
    protected String buildFormattedString(PyObject[] args, String[] keywords,
                                          MarkupIterator enclosingIterator, CharSequence value) {

        MarkupIterator it;
        if (enclosingIterator == null) {
            // Top-level call acts on this object.
            it = new MarkupIterator(getString());
        } else {
            // Nested call acts on the substring and some state from existing iterator.
            it = new MarkupIterator(enclosingIterator, value);
        }

        // Result will be formed here
        StringBuilder result = new StringBuilder();

        while (true) {
            MarkupIterator.Chunk chunk = it.nextChunk();
            if (chunk == null) {
                break;
            }
            // A Chunk encapsulates a literal part ...
            result.append(chunk.literalText);
            // ... and the parsed form of the replacement field that followed it (if any)
            if (chunk.fieldName != null) {
                // The grammar of the replacement field is:
                // "{" [field_name] ["!" conversion] [":" format_spec] "}"

                // Get the object referred to by the field name (which may be omitted).
                PyObject fieldObj = getFieldObject(chunk.fieldName, false, args, keywords);
                if (fieldObj == null) {
                    continue;
                }

                // The conversion specifier is s = __str__ or r = __repr__.
                if ("r".equals(chunk.conversion)) {
                    fieldObj = BuiltinModule.repr(fieldObj);
                } else if ("s".equals(chunk.conversion)) {
                    fieldObj = Abstract.PyObject_Str(Py.getThreadState(), fieldObj);
                } else if ("a".equals(chunk.conversion)) {
                    // a = ascii
                    fieldObj = BuiltinModule.ascii(fieldObj);
                } else if (chunk.conversion != null) {
                    throw Py.ValueError("Unknown conversion specifier " + chunk.conversion);
                }

                // Check for "{}".format(u"abc")
                if (fieldObj instanceof PyUnicode && !(this instanceof PyUnicode)) {
                    // Down-convert to PyBytes, at the risk of raising UnicodeEncodingError
                    fieldObj = Abstract.PyObject_Str(Py.getThreadState(), fieldObj);
                }

                // The format_spec may be simple, or contained nested replacement fields.
                CharSequence formatSpec = chunk.formatSpec;
                if (chunk.formatSpecNeedsExpanding) {
                    if (enclosingIterator != null) {
                        // PEP 3101 says only 2 levels
                        throw Py.ValueError("Max string recursion exceeded");
                    }
                    // Recursively interpolate further args into chunk.formatSpec
                    formatSpec = buildFormattedString(args, keywords, it, formatSpec);
                }
                renderField(fieldObj, formatSpec, result);
            }
        }
        return result.toString();
    }

    public int readChar(int pos) {
        return string.codePointAt(pos);
    }

    /**
     * A little helper for converting str.find to str.index that will raise
     * <code>ValueError("substring not found")</code> if the argument is negative, otherwise passes
     * the argument through.
     *
     * @param index to check
     * @return <code>index</code> if non-negative
     * @throws PyException(ValueError) if not found
     */
    protected final int checkIndex(int index) throws PyException {
        if (index >= 0) {
            return index;
        } else {
            throw Py.ValueError("substring not found");
        }
    }

    /**
     * Return the object referenced by a given field name, interpreted in the context of the given
     * argument list, containing positional and keyword arguments.
     *
     * @param fieldName to interpret.
     * @param bytes     true if the field name is from a PyBytes, false for PyUnicode.
     * @param args      argument list (positional then keyword arguments).
     * @param keywords  naming the keyword arguments.
     * @return the object designated or <code>null</code>.
     */
    private PyObject getFieldObject(CharSequence fieldName, boolean bytes, PyObject[] args,
                                    String[] keywords) {
        FieldNameIterator iterator = new FieldNameIterator(fieldName, bytes);
        PyObject head = iterator.pyHead();
        PyObject obj = null;
        int positionalCount = args.length - keywords.length;

        if (head.isIndex()) {
            // The field name begins with an integer argument index (not a [n]-type index).
            int index = head.asIndex();
            if (index >= positionalCount) {
                throw Py.IndexError("tuple index out of range");
            }
            obj = args[index];

        } else {
            // The field name begins with keyword.
            for (int i = 0; i < keywords.length; i++) {
                if (keywords[i].equals(head.asString())) {
                    obj = args[positionalCount + i];
                    break;
                }
            }
            // And if we don't find it, that's an error
            if (obj == null) {
                throw Py.KeyError(head);
            }
        }

        // Now deal with the iterated sub-fields
        while (obj != null) {
            FieldNameIterator.Chunk chunk = iterator.nextChunk();
            if (chunk == null) {
                // End of iterator
                break;
            }
            Object key = chunk.value;
            if (chunk.is_attr) {
                // key must be a String
                obj = obj.__getattr__((String) key);
            } else {
                if (key instanceof Integer) {
                    // Can this happen?
                    obj = obj.__getitem__(((Integer) key).intValue());
                } else {
                    obj = obj.__getitem__(new PyUnicode(key.toString()));
                }
            }
        }

        return obj;
    }

    /**
     * Append to a formatting result, the presentation of one object, according to a given format
     * specification and the object's <code>__format__</code> method.
     *
     * @param fieldObj   to format.
     * @param formatSpec specification to apply.
     * @param result     to which the result will be appended.
     */
    private void renderField(PyObject fieldObj, CharSequence formatSpec, StringBuilder result) {
        PyUnicode formatSpecStr = formatSpec == null ? Py.EmptyUnicode : new PyUnicode(formatSpec);
        result.append(BuiltinModule.format2(fieldObj, formatSpecStr).asString());
    }

    public String internedString() {
        if (interned) {
            return getString();
        } else {
            string = getString().intern();
            interned = true;
            return getString();
        }
    }

    /**
     * Index translation between code point index (as seen by Python) and UTF-16 index (as used in
     * the Java String.
     */
    private interface IndexTranslator extends Serializable {

        /**
         * Number of supplementary characters (hence point code length may be found).
         */
        public int suppCount();

        /**
         * Translate a UTF-16 code unit index to its equivalent code point index.
         */
        public int codePointIndex(int utf16Index);

        /**
         * Translate a code point index to its equivalent UTF-16 code unit index.
         */
        public int utf16Index(int codePointIndex);
    }

    private static class SteppedIterator<T> implements Iterator {

        private final Iterator<T> iter;
        private final int step;
        private T lookahead = null;

        public SteppedIterator(int step, Iterator<T> iter) {
            this.iter = iter;
            this.step = step;
            lookahead = advance();
        }

        private T advance() {
            if (iter.hasNext()) {
                T elem = iter.next();
                for (int i = 1; i < step && iter.hasNext(); i++) {
                    iter.next();
                }
                return elem;
            } else {
                return null;
            }
        }

        @Override
        public boolean hasNext() {
            return lookahead != null;
        }

        @Override
        public T next() {
            T old = lookahead;
            if (iter.hasNext()) {
                lookahead = iter.next();
                for (int i = 1; i < step && iter.hasNext(); i++) {
                    iter.next();
                }
            } else {
                lookahead = null;
            }
            return old;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static class StripIterator implements Iterator {

        private final Iterator<Integer> iter;
        private int lookahead = -1;

        public StripIterator(PyUnicode sep, Iterator<Integer> iter) {
            this.iter = iter;
            if (sep != null) {
                Set<Integer> sepSet = new HashSet<>();
                for (Iterator<Integer> sepIter = sep.newSubsequenceIterator(); sepIter.hasNext(); ) {
                    sepSet.add(sepIter.next());
                }
                while (iter.hasNext()) {
                    int codePoint = iter.next();
                    if (!sepSet.contains(codePoint)) {
                        lookahead = codePoint;
                        return;
                    }
                }
            } else {
                while (iter.hasNext()) {
                    int codePoint = iter.next();
                    if (!Character.isWhitespace(codePoint)) {
                        lookahead = codePoint;
                        return;
                    }
                }
            }
        }

        @Override
        public boolean hasNext() {
            return lookahead != -1;
        }

        @Override
        public Object next() {
            int old = lookahead;
            if (iter.hasNext()) {
                lookahead = iter.next();
            } else {
                lookahead = -1;
            }
            return old;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static class PeekIterator<T> implements Iterator {

        private final Iterator<T> iter;
        private T lookahead = null;

        public PeekIterator(Iterator<T> iter) {
            this.iter = iter;
            next();
        }

        public T peek() {
            return lookahead;
        }

        @Override
        public boolean hasNext() {
            return lookahead != null;
        }

        @Override
        public T next() {
            T peeked = lookahead;
            lookahead = iter.hasNext() ? iter.next() : null;
            return peeked;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static class ReversedIterator<T> implements Iterator {

        private final List<T> reversed = new ArrayList<>();
        private final Iterator<T> iter;

        ReversedIterator(Iterator<T> iter) {
            while (iter.hasNext()) {
                reversed.add(iter.next());
            }
            Collections.reverse(reversed);
            this.iter = reversed.iterator();
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public T next() {
            return iter.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class SubsequenceIteratorImpl implements Iterator {

        private int current, stop, step;

        SubsequenceIteratorImpl(int start, int stop, int step) {
            current = start;
            this.stop = stop;
            this.step = step;
        }

        SubsequenceIteratorImpl() {
            this(0, getCodePointCount(), 1);
        }

        @Override
        public boolean hasNext() {
            return current < stop;
        }

        private int nextCodePoint() {
            int k = translator.codePointIndex(current++);
            return string.codePointAt(k);
        }

        @Override
        public Object next() {
            int codePoint = nextCodePoint();
            for (int j = 1; j < step && hasNext(); j++) {
                nextCodePoint();
            }
            return codePoint;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException(
                    "Not supported on PyUnicode objects (immutable)");
        }
    }

    private abstract class SplitIterator implements Iterator {

        protected final int maxsplit;
        protected final Iterator<Integer> iter = newSubsequenceIterator();
        protected final LinkedList<Integer> lookahead = new LinkedList<Integer>();
        protected int numSplits = 0;
        protected boolean completeSeparator = false;

        SplitIterator(int maxsplit) {
            this.maxsplit = maxsplit;
        }

        protected void addLookahead(StringBuilder buffer) {
            for (int codepoint : lookahead) {
                buffer.appendCodePoint(codepoint);
            }
            lookahead.clear();
        }        @Override
        public boolean hasNext() {
            return lookahead.peek() != null
                    || (iter.hasNext() && (maxsplit == -1 || numSplits <= maxsplit));
        }

        public boolean getEndsWithSeparator() {
            return completeSeparator && !hasNext();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }


    }

    private class WhitespaceSplitIterator extends SplitIterator {

        WhitespaceSplitIterator(int maxsplit) {
            super(maxsplit);
        }

        @Override
        public PyUnicode next() {
            StringBuilder buffer = new StringBuilder();

            addLookahead(buffer);
            if (numSplits == maxsplit) {
                while (iter.hasNext()) {
                    buffer.appendCodePoint(iter.next());
                }
                return new PyUnicode(buffer);
            }

            boolean inSeparator = false;
            boolean atBeginning = numSplits == 0;

            while (iter.hasNext()) {
                int codepoint = iter.next();
                if (Character.isWhitespace(codepoint)) {
                    completeSeparator = true;
                    if (!atBeginning) {
                        inSeparator = true;
                    }
                } else if (!inSeparator) {
                    completeSeparator = false;
                    buffer.appendCodePoint(codepoint);
                } else {
                    completeSeparator = false;
                    lookahead.add(codepoint);
                    break;
                }
                atBeginning = false;
            }
            numSplits++;
            return new PyUnicode(buffer);
        }
    }

    private class LineSplitIterator implements Iterator {

        private final PeekIterator<Integer> iter = new PeekIterator(newSubsequenceIterator());
        private final boolean keepends;

        LineSplitIterator(boolean keepends) {
            this.keepends = keepends;
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public Object next() {
            StringBuilder buffer = new StringBuilder();
            while (iter.hasNext()) {
                int codepoint = iter.next();
                if (codepoint == '\r' && iter.peek() != null && iter.peek() == '\n') {
                    if (keepends) {
                        buffer.appendCodePoint(codepoint);
                        buffer.appendCodePoint(iter.next());
                    } else {
                        iter.next();
                    }
                    break;
                } else if (codepoint == '\n' || codepoint == '\r'
                        || Character.getType(codepoint) == Character.LINE_SEPARATOR) {
                    if (keepends) {
                        buffer.appendCodePoint(codepoint);
                    }
                    break;
                } else {
                    buffer.appendCodePoint(codepoint);
                }
            }
            return new PyUnicode(buffer);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class SepSplitIterator extends SplitIterator {

        private final PyUnicode sep;

        SepSplitIterator(PyUnicode sep, int maxsplit) {
            super(maxsplit);
            this.sep = sep;
        }

        @Override
        public PyUnicode next() {
            StringBuilder buffer = new StringBuilder();

            addLookahead(buffer);
            if (numSplits == maxsplit) {
                while (iter.hasNext()) {
                    buffer.appendCodePoint(iter.next());
                }
                return new PyUnicode(buffer);
            }

            boolean inSeparator = true;
            while (iter.hasNext()) {
                // TODO: should cache the first codepoint
                inSeparator = true;
                for (Iterator<Integer> sepIter = sep.newSubsequenceIterator(); sepIter.hasNext(); ) {
                    int codepoint = iter.next();
                    if (codepoint != sepIter.next()) {
                        addLookahead(buffer);
                        buffer.appendCodePoint(codepoint);
                        inSeparator = false;
                        break;
                    } else {
                        lookahead.add(codepoint);
                    }
                }

                if (inSeparator) {
                    lookahead.clear();
                    break;
                }
            }

            numSplits++;
            completeSeparator = inSeparator;
            return new PyUnicode(buffer);
        }
    }
}
