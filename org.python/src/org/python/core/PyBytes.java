// Copyright (c) Corporation for National Research Initiatives
package org.python.core;

import org.python.core.buffer.BaseBuffer;
import org.python.core.buffer.SimpleBuffer;
import org.python.core.buffer.SimpleStringBuffer;
import org.python.core.stringlib.Encoding;
import org.python.core.stringlib.FieldNameIterator;
import org.python.core.stringlib.MarkupIterator;
import org.python.core.util.StringUtil;
import org.python.annotations.ExposedClassMethod;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.expose.MethodType;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.python.core.PyBUF.FULL_RO;
import static org.python.core.stringlib.Encoding.asUTF16StringOrError;

/**
 * A builtin python string.
 */
@Untraversable
@ExposedType(name = "bytes", base = PyObject.class, doc = BuiltinDocs.bytes_doc)
public class PyBytes extends PySequence implements BufferProtocol {

    private static final String BYTES_REQUIRED_ERROR = "a bytes-like object is required, not 'str'";

    public static final PyType TYPE = PyType.fromClass(PyBytes.class);
    protected String string; // cannot make final because of Python intern support
    private final ByteBuffer buffer;
    protected transient boolean interned = false;
    /** Supports the buffer API, see {@link #getBuffer(int)}. */
    private Reference<BaseBuffer> export;
    private static final int INITIAL_CAPACITY = 1024;

    public String getString() {
        return string;
    }

    // for PyJavaClass.init()
    public PyBytes() {
        this("", true);
    }

    public PyBytes(byte[] buf) {
        this(buf, 0, buf.length);
    }
    public PyBytes(byte[] buf, int off, int ending) {
        super(TYPE);
        StringBuilder v = new StringBuilder(buf.length);
        for (int i = off; i < ending; i++) {
            v.appendCodePoint(buf[i] & 0xFF);
        }
        string = v.toString();
        buffer = ByteBuffer.wrap(buf, off, ending - off);
    }

    public PyBytes(int[] buf) {
        super(TYPE);
        StringBuilder v = new StringBuilder(buf.length);
        for (int i: buf) {
            v.appendCodePoint(i);
        }
        string = v.toString();
        buffer = ByteBuffer.allocate(buf.length * 4);
        for (int x : buf) {
            buffer.putInt(x);
        }
        buffer.flip();
    }

    /**
     * Fundamental constructor for <code>PyBytes</code> objects when the client provides a Java
     * <code>String</code>, necessitating that we range check the characters.
     *
     * @param subType the actual type being constructed
     * @param string a Java String to be wrapped
     */
    public PyBytes(PyType subType, CharSequence string) {
        super(subType);
        if (string == null) {
            throw Py.ValueError("Cannot create PyBytes from null");
        } else if (!isBytes(string)) {
            throw Py.ValueError("Cannot create PyBytes with non-byte value");
        }
        this.string = string.toString();
        buffer = ByteBuffer.allocate(string.length() * 2);
        string.chars().forEach(chr -> buffer.put((byte) (chr & 0xFF)));
        buffer.flip();
    }

    public PyBytes(ByteBuffer buf) {
        super(TYPE);
        StringBuilder v = new StringBuilder(buf.limit());
        for(int i = 0; i < buf.limit(); i++) {
            v.appendCodePoint(buf.get(i) & 0xFF);
        }
        string = v.toString();
        buffer = buf.asReadOnlyBuffer();
    }

    public PyBytes(CharSequence string) {
        this(TYPE, string);
    }

    public PyBytes(char c) {
        this(TYPE, String.valueOf(c));
    }

    PyBytes(StringBuilder buffer) {
        this(TYPE, new String(buffer));
    }

    /**
     * Local-use constructor in which the client is allowed to guarantee that the
     * <code>String</code> argument contains only characters in the byte range. We do not then
     * range-check the characters.
     *
     * @param string a Java String to be wrapped (not null)
     * @param isBytes true if the client guarantees we are dealing with bytes
     */
    private PyBytes(CharSequence string, boolean isBytes) {
        super(TYPE);
        if (isBytes || isBytes(string)) {
            this.string = string.toString();
            buffer = ByteBuffer.allocate(string.length());
            string.chars().forEach(chr -> buffer.put((byte) (chr & 0xFF)));
            buffer.flip();
        } else {
            throw new IllegalArgumentException("Cannot create PyBytes with non-byte value");
        }
    }

    /**
     * Determine whether a string consists entirely of characters in the range 0 to 255. Only such
     * characters are allowed in the <code>PyBytes</code> (<code>str</code>) type, when it is not a
     * {@link PyUnicode}.
     *
     * @return true if and only if every character has a code less than 256
     */
    private static boolean isBytes(CharSequence s) {
        int k = s.length();
        if (k == 0) {
            return true;
        } else {
            // Bitwise-or the character codes together in order to test once.
            char c = 0;
            // Blocks of 8 to reduce loop tests
            while (k > 8) {
                c |= s.charAt(--k);
                c |= s.charAt(--k);
                c |= s.charAt(--k);
                c |= s.charAt(--k);
                c |= s.charAt(--k);
                c |= s.charAt(--k);
                c |= s.charAt(--k);
                c |= s.charAt(--k);
            }
            // Now the rest
            while (k > 0) {
                c |= s.charAt(--k);
            }
            // We require there to be no bits set from 0x100 upwards
            return c < 0x100;
        }
    }

    /**
     * Creates a PyBytes from an already interned String. Just means it won't be reinterned if used
     * in a place that requires interned Strings.
     * TODO remove once bootstrapped
     */
    public static PyBytes fromInterned(String interned) {
        PyBytes str = new PyBytes(TYPE, interned);
        str.interned = true;
        return str;
    }

    @ExposedNew
    static PyObject bytes_new(PyNewWrapper new_, boolean init, PyType subtype, PyObject[] args,
            String[] keywords) {
        ArgParser ap = new ArgParser("str", args, keywords, new String[] {"object", "encoding", "errors"}, 0);
        PyObject S = ap.getPyObject(0, null);
        PyObject encodingObj = ap.getPyObject(1, null);
        PyObject errorsObj = ap.getPyObject(2, null);
        if (encodingObj != null) {
            if (!(encodingObj instanceof PyUnicode)) {
                throw Py.TypeError(String.format("bytes() argument 2 must be str, not %s", encodingObj.getType().fastGetName()));
            }
            String errors = null;
            if (errorsObj != null) {
                if (!(errorsObj instanceof PyUnicode)) {
                    throw Py.TypeError(String.format("bytes() argument 3 must be str, not %s", errorsObj.getType().fastGetName()));
                }
                errors = errorsObj.asString();
            }
            if (!(S instanceof PyUnicode)) {
                throw Py.TypeError("encoding without a string argument");
            }
            return new PyBytes(((PyUnicode) S).encode(encodingObj.asString(), errors));
        }
        if (errorsObj != null) {
            throw Py.TypeError(S instanceof PyUnicode ? "string argument without an encoding" : "errors without a string argument");
        }
        // Get the textual representation of the object into str/bytes form
        String str;
        if (S == null) {
            str = "";
        } else {
            PyObject func = S.__findattr__("__bytes__");
            if (func != null) {
                PyObject newObj = func.__call__();
                if (newObj instanceof PyBytes) {
                    if (new_.for_type == subtype) {
                        return newObj;
                    }
                    return subtype.__call__(new PyObject[]{ newObj }, Py.NoKeywords);
                }
                throw Py.TypeError(String.format("__bytes__ returned non-bytes (type %s)", newObj.getType().fastGetName()));
            } else if (S instanceof PyUnicode) {
                throw Py.TypeError("string arugment without an encoding");
            } else if (S instanceof BufferProtocol) {
                PyBuffer buffer = ((BufferProtocol) S).getBuffer(FULL_RO);
                byte[] buf = new byte[buffer.getLen()];
                buffer.copyTo(buf, 0);
                buffer.close();
                StringBuilder v = new StringBuilder(buf.length);
                for (byte b: buf) {
                    v.appendCodePoint(b & 0xFF);
                }
                str = v.toString();
            } else if (S.isIndex()) {
                int n = S.asIndex(Py.OverflowError);
                if (n < 0) {
                    throw Py.ValueError("negative count");
                }
                byte[] bytes = new byte[n];
                Arrays.fill( bytes, (byte) 0 );
                str = new String(bytes);
            } else { // an iterable yielding integers in range(256)
                StringBuilder v = new StringBuilder();
                for (PyObject x : S.asIterable()) {
                    int i = x.asIndex(Py.ValueError);
                    byteCheck(i);
                    v.appendCodePoint(i);
                }
                str = v.toString();
            }
        }
        if (new_.for_type == subtype) {
            return new PyBytes(str);
        }
        return new PyBytesDerived(subtype, str);
    }

    public int[] toCodePoints() {
        String s = getString();
        int n = s.length();
        int[] codePoints = new int[n];
        for (int i = 0; i < n; i++) {
            codePoints[i] = s.charAt(i);
        }
        return codePoints;
    }

    private ByteBuffer readBuf() {
        return buffer.asReadOnlyBuffer();
    }

    /**
     * Return a read-only buffer view of the contents of the string, treating it as a sequence of
     * unsigned bytes. The caller specifies its requirements and navigational capabilities in the
     * <code>flags</code> argument (see the constants in interface {@link PyBUF} for an
     * explanation). The method may return the same PyBuffer object to more than one consumer.
     *
     * @param flags consumer requirements
     * @return the requested buffer
     */
    @Override
    public synchronized PyBuffer getBuffer(int flags) {
        // If we have already exported a buffer it may still be available for re-use
        BaseBuffer pybuf = getExistingBuffer(flags);
        if (pybuf == null) {
            /*
             * No existing export we can re-use. Return a buffer, but specialised to defer
             * construction of the buf object, and cache a soft reference to it.
             */
            pybuf = new SimpleStringBuffer(flags, string);
            export = new SoftReference<>(pybuf);
        }
        return pybuf;
    }

    @Override
    public ByteBuffer getBuffer() {
        return readBuf();
    }

    @Override
    public int write(ByteBuffer buf) throws PyException {
        throw Py.NotImplementedError("bytes object is immutable");
    }

    /**
     * Helper for {@link #getBuffer(int)} that tries to re-use an existing exported buffer, or
     * returns null if can't.
     */
    private BaseBuffer getExistingBuffer(int flags) {
        BaseBuffer pybuf = null;
        if (export != null) {
            // A buffer was exported at some time.
            pybuf = export.get();
            if (pybuf != null) {
                /*
                 * And this buffer still exists. Even in the case where the buffer has been released
                 * by all its consumers, it remains safe to re-acquire it because the target String
                 * has not changed.
                 */
                pybuf = pybuf.getBufferAgain(flags);
            }
        }
        return pybuf;
    }

    /**
     * Return a substring of this object as a Java String.
     *
     * @param start the beginning index, inclusive.
     * @param end the ending index, exclusive.
     * @return the specified substring.
     */
    public String substring(int start, int end) {
        return getString().substring(start, end);
    }

    @Override
    public PyUnicode __str__() {
        return bytes___str__();
    }

    @ExposedMethod(doc = BuiltinDocs.bytes___str___doc)
    public final PyUnicode bytes___str__() {
        return new PyUnicode("b" + Encoding.encode_UnicodeEscape(getString(), true));
    }

    @Override
    public int __len__() {
        return bytes___len__();
    }

    @Override
    @ExposedMethod(names = "__iter__")
    public PyObject __iter__() {
        return seq___iter__();
    }

    @ExposedMethod(doc = BuiltinDocs.bytes___len___doc)
    public final int bytes___len__() {
        return getString().length();
    }

    @Override
    public String toString() {
        return getString();
    }

    @Override
    public PyUnicode __repr__() {
        return bytes___repr__();
    }

    @ExposedMethod(doc = BuiltinDocs.bytes___repr___doc)
    public final PyUnicode bytes___repr__() {
        return new PyUnicode("b" + Encoding.encode_UnicodeEscapeAsASCII(getString(), true));
    }

    @ExposedMethod(doc = BuiltinDocs.bytes___getitem___doc)
    public final PyObject bytes___getitem__(PyObject index) {
        PyObject ret = seq___finditem__(index);
        if (ret == null) {
            throw Py.IndexError("string index out of range");
        }
        return ret;
    }

    @Override
    public PyObject richCompare(PyObject other, CompareOp op) {
        String s = coerce(other);
        if (s == null) {
            if (op == CompareOp.EQ) {
                return Py.False;
            }
            if (op == CompareOp.NE) {
                return Py.True;
            }
            return Py.NotImplemented;
        }
        return op.bool(getString().compareTo(s));
    }

    private static String coerce(PyObject o) {
        if (o instanceof PyBytes) {
            return ((PyBytes) o).getString();
        } else if (o instanceof PyByteArray) {
            return o.asString();
        }
        return null;
    }

    @Override
    public int hashCode() {
        return bytes___hash__();
    }

    @ExposedMethod(doc = BuiltinDocs.bytes___hash___doc)
    public final int bytes___hash__() {
        return getString().hashCode();
    }

    /**
     * @return a byte array with one byte for each char in this object's underlying String. Each
     *         byte contains the low-order bits of its corresponding char.
     */
    public byte[] toBytes() {
        return StringUtil.toBytes(getString());
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
                return toBytes();
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

    @Override
    protected PyObject pyget(int i) {
        return new PyLong(buffer.get(i) & 0xFF);
//        return new PyLong(string.charAt(i));
    }

    public int getInt(int i) {
        return buffer.getChar(i * 2);
//        return string.charAt(i);
    }

    @Override
    public PyObject getslice(int start, int stop, int step) {
        CharSequence s = Encoding.getslice(getString(), start, stop, step, sliceLength(start, stop, step));
        return new PyBytes(s);
    }

    @Override
    @ExposedMethod(doc = BuiltinDocs.bytes___contains___doc)
    public boolean __contains__(PyObject o) {
        if (o instanceof PyLong) {
            int n;
            try {
                n = o.asInt();
            } catch (PyException e) {
                if (e.match(Py.OverflowError)) {
                    throw Py.ValueError("byte must be in range(0, 255)");
                }
                throw e;
            }

            byteCheck(n);
            return getString().contains(String.valueOf(Character.toChars(n)));
        }
        if (o instanceof PyUnicode) {
            throw Py.TypeError(BYTES_REQUIRED_ERROR);
        }
        String other = asUTF16StringOrError(o);
        return getString().contains(other);
    }

    @Override
    protected PyObject repeat(int count) {
        if (count < 0) {
            count = 0;
        }
        int s = getString().length();
        if ((long)s * count > Integer.MAX_VALUE) {
            // Since Strings store their data in an array, we can't make one
            // longer than Integer.MAX_VALUE. Without this check we get
            // NegativeArraySize Exceptions when we create the array on the
            // line with a wrapped int.
            throw Py.OverflowError("max str len is " + Integer.MAX_VALUE);
        }
        char new_chars[] = new char[s * count];
        for (int i = 0; i < count; i++) {
            getString().getChars(0, s, new_chars, i * s);
        }
        return new PyBytes(new String(new_chars));
    }

    @Override
    public PyObject __mul__(PyObject o) {
        return bytes___mul__(o);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.bytes___mul___doc)
    public final PyObject bytes___mul__(PyObject o) {
        if (!o.isIndex()) {
            return null;
        }
        return repeat(o.asIndex(Py.OverflowError));
    }

    @Override
    public PyObject __rmul__(PyObject o) {
        return bytes___rmul__(o);
    }

    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.bytes___rmul___doc)
    public final PyObject bytes___rmul__(PyObject o) {
        if (!o.isIndex()) {
            return null;
        }
        return repeat(o.asIndex(Py.OverflowError));
    }

    @Override
    @ExposedMethod(type = MethodType.BINARY, doc = BuiltinDocs.bytes___add___doc)
    public PyObject __add__(PyObject other) {
        String otherStr = Encoding.asStringOrNull(other);
        if (otherStr != null) {
            return new PyBytes(getString().concat(otherStr), true);
        } else if (other instanceof PyUnicode) {
            throw Py.TypeError("can't concat str to bytes");
        } else {
            // Allow PyObject._basic_add to pick up the pieces or raise informative error
            return null;
        }
    }

    @ExposedMethod(doc = BuiltinDocs.bytes___getnewargs___doc)
    final PyTuple bytes___getnewargs__() {
        return new PyTuple(new PyBytes(this.getString()));
    }

    @Override
    public PyTuple __getnewargs__() {
        return bytes___getnewargs__();
    }

    @Override
    public PyObject __mod__(PyObject other) {
        return bytes___mod__(other);
    }

    @ExposedMethod(doc = BuiltinDocs.bytes___mod___doc)
    public PyObject bytes___mod__(PyObject other) {
        StringFormatter fmt = new StringFormatter(getString(), false);
        return fmt.format(other);
    }

    @ExposedMethod(doc = BuiltinDocs.bytes___rmod___doc)
    public PyObject __rmod__(PyObject other) {
        if (other instanceof PyBytes) {
            return other.__mod__(this);
        } else if (other instanceof PyUnicode) {
            return Py.NotImplemented;
        }
        throw Py.TypeError(String.format("unsupported operand type(s) for %%: '%s' and 'bytes'", other.getType().fastGetName()));
    }

    public PyObject atol(int base) {
        return Encoding.atol(getString(), base);
    }

    public double atof() {
        return Encoding.atof(getString());
    }

    @Override
    public PyObject __int__() {
        return Encoding.atol(getString(), 10);
    }

    @Override
    public PyFloat __float__() {
        return new PyFloat(Encoding.atof(getString()));
    }

    @Override
    public PyObject __pos__() {
        throw Py.TypeError("bad operand type for unary +");
    }

    @Override
    public PyObject __neg__() {
        throw Py.TypeError("bad operand type for unary -");
    }

    @Override
    public PyObject __invert__() {
        throw Py.TypeError("bad operand type for unary ~");
    }

    @Override
    public PyComplex __complex__() {
        return Encoding.atocx(getString());
    }

    @ExposedMethod(doc = BuiltinDocs.bytes_lower_doc)
    public final PyObject bytes_lower() {
        return new PyBytes(getString().toLowerCase(Locale.ROOT));
    }

    @ExposedMethod(doc = BuiltinDocs.str_upper_doc)
    public final PyObject bytes_upper() {
        return new PyBytes(getString().toUpperCase(Locale.ROOT));
    }

    @ExposedMethod(doc = BuiltinDocs.bytes_title_doc)
    public final PyObject bytes_title() {
        return new PyBytes(Encoding.title(getString()));
    }

    @ExposedMethod(doc = BuiltinDocs.bytes_swapcase_doc)
    public final PyObject bytes_swapcase() {
        return new PyBytes(Encoding.swapcase(getString()));
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.bytes_strip_doc)
    public final PyObject bytes_strip(PyObject chars) {
        if (chars instanceof PyUnicode) {
            throw Py.TypeError(BYTES_REQUIRED_ERROR);
        } else {
            // It ought to be None, null, some kind of bytes with the buffer API.
            String stripChars = Encoding.asStringNullOrError(chars, "strip");
            // Strip specified characters or whitespace if stripChars == null
            return new PyBytes(Encoding._strip(getString(), stripChars), true);
        }
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.bytes_lstrip_doc)
    public final PyObject bytes_lstrip(PyObject chars) {
        if (chars instanceof PyUnicode) {
            throw Py.TypeError(BYTES_REQUIRED_ERROR);
        } else {
            // It ought to be None, null, some kind of bytes with the buffer API.
            String stripChars = Encoding.asStringNullOrError(chars, "lstrip");
            // Strip specified characters or whitespace if stripChars == null
            return new PyBytes(Encoding._lstrip(getString(), stripChars), true);
        }
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.bytes_rstrip_doc)
    public final PyObject bytes_rstrip(PyObject chars) {
        if (chars instanceof PyUnicode) {
            throw Py.TypeError(BYTES_REQUIRED_ERROR);
        } else {
            // It ought to be None, null, some kind of bytes with the buffer API.
            String stripChars = Encoding.asStringNullOrError(chars, "rstrip");
            // Strip specified characters or whitespace if stripChars == null
            return new PyBytes(Encoding._rstrip(getString(), stripChars), true);
        }
    }

    @ExposedMethod(doc = BuiltinDocs.bytes_split_doc)
    public final PyList bytes_split(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("split", args, keywords, "sep", "maxsplit");
        PyObject sep = ap.getPyObject(0, Py.None);
        int maxsplit = ap.getInt(1, -1);
        // Split on specified string or whitespace if sep == null
        return toPyList(Encoding._split(getString(), Encoding.asStringNullOrError(sep, "sep"), maxsplit));
    }

    private static PyList toPyList(Collection<CharSequence> list) {
        return new PyList(list.stream().map(PyBytes::new).collect(Collectors.toList()));
    }

    @ExposedMethod(doc = BuiltinDocs.bytes_split_doc)
    public final PyList bytes_rsplit(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("rsplit", args, keywords, "sep", "maxsplit");
        PyObject sep = ap.getPyObject(0, Py.None);
        int maxsplit = ap.getInt(1, -1);
        // Split on specified string or whitespace if sep == null
        Collection<CharSequence> list = Encoding._rsplit(getString(), Encoding.asStringNullOrError(sep, "sep"), maxsplit);
        return new PyList(list.stream().map(PyBytes::new).collect(Collectors.toList()));
    }

    @ExposedMethod(doc = BuiltinDocs.bytes_partition_doc)
    public final PyTuple bytes_partition(PyObject sepObj) {

        if (sepObj instanceof PyUnicode) {
            // Deal with Unicode separately
            throw Py.TypeError(BYTES_REQUIRED_ERROR);

        } else {
            // It ought to be some kind of bytes with the buffer API.
            String sep = Encoding.asStringOrError(sepObj);

            if (sep.length() == 0) {
                throw Py.ValueError("empty separator");
            }

            int index = getString().indexOf(sep);
            if (index != -1) {
                return new PyTuple(fromSubstring(0, index), sepObj, fromSubstring(
                        index + sep.length(), getString().length()));
            } else {
                return new PyTuple(this, Py.EmptyByte, Py.EmptyByte);
            }
        }
    }

    @ExposedMethod(doc = BuiltinDocs.bytes_rpartition_doc)
    public final PyTuple bytes_rpartition(PyObject sepObj) {

        if (sepObj instanceof PyUnicode) {
            throw Py.TypeError(BYTES_REQUIRED_ERROR);
        } else {
            // It ought to be some kind of bytes with the buffer API.
            String sep = Encoding.asStringOrError(sepObj);

            if (sep.length() == 0) {
                throw Py.ValueError("empty separator");
            }

            int index = getString().lastIndexOf(sep);
            if (index != -1) {
                return new PyTuple(fromSubstring(0, index), sepObj, fromSubstring(
                        index + sep.length(), getString().length()));
            } else {
                return new PyTuple(Py.EmptyByte, Py.EmptyByte, this);
            }
        }
    }

    @ExposedMethod(doc = BuiltinDocs.bytes_splitlines_doc)
    public final PyList bytes_splitlines(PyObject[] args, String[] keywords) {
        ArgParser arg = new ArgParser("splitlines", args, keywords, "keepends");
        boolean keepends = arg.getPyObject(0, Py.False).__bool__();

        return toPyList(Encoding.splitlines(getString(), keepends));
    }

    /**
     * Return a new object <em>of the same type as this one</em> equal to the slice
     * <code>[begin:end]</code>. (Python end-relative indexes etc. are not supported.) Subclasses (
     * {@link PyUnicode#fromSubstring(int, int)}) override this to return their own type.)
     *
     * @param begin first included character.
     * @param end first excluded character.
     * @return new object.
     */
    protected PyBytes fromSubstring(int begin, int end) {
        // Method is overridden in PyUnicode, so definitely a PyBytes
        return new PyBytes(getString().substring(begin, end), true);
    }

    @ExposedMethod(defaults = {"null", "null"}, doc = BuiltinDocs.bytes_index_doc)
    public final int bytes_index(PyObject subObj, PyObject start, PyObject end) {
        return checkIndex(bytes_find(subObj, start, end));
    }

    @ExposedMethod(defaults = {"null", "null"}, doc = BuiltinDocs.bytes_rindex_doc)
    public final int bytes_rindex(PyObject subObj, PyObject start, PyObject end) {
        return checkIndex(bytes_rfind(subObj, start, end));
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

    @ExposedMethod(defaults = {"null", "null"}, doc = BuiltinDocs.bytes_count_doc)
    public final int bytes_count(PyObject subObj, PyObject start, PyObject end) {
        if (subObj instanceof PyUnicode) {
            throw Py.TypeError(BYTES_REQUIRED_ERROR);
        } else {
            // It ought to be some kind of bytes with the buffer API.
            String sub = Encoding.asStringOrError(subObj);
            return Encoding._count(getString(), sub, start, end, __len__());
        }
    }

    @ExposedMethod(defaults = {"null", "null"}, doc = BuiltinDocs.bytes_find_doc)
    public final int bytes_find(PyObject subObj, PyObject start, PyObject end) {
        if (subObj instanceof PyUnicode) {
            throw Py.TypeError(BYTES_REQUIRED_ERROR);
        } else {
            // It ought to be some kind of bytes with the buffer API.
            String sub = Encoding.asStringOrError(subObj);
            return Encoding._find(getString(), sub, start, end, __len__());
        }
    }

    @ExposedMethod(defaults = {"null", "null"}, doc = BuiltinDocs.bytes_rfind_doc)
    public final int bytes_rfind(PyObject subObj, PyObject start, PyObject end) {
        if (subObj instanceof PyUnicode) {
            throw Py.TypeError(BYTES_REQUIRED_ERROR);
        } else {
            // It ought to be some kind of bytes with the buffer API.
            String sub = Encoding.asStringOrError(subObj);
            return Encoding._rfind(getString(), sub, start, end, __len__());
        }
    }

    private static String padding(int n, char pad) {
        char[] chars = new char[n];
        for (int i = 0; i < n; i++) {
            chars[i] = pad;
        }
        return new String(chars);
    }

    private static char parse_fillchar(String function, String fillchar) {
        if (fillchar == null) {
            return ' ';
        }
        if (fillchar.length() != 1) {
            throw Py.TypeError(function + "() argument 2 must be char, not str");
        }
        return fillchar.charAt(0);
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.bytes_ljust_doc)
    public final PyBytes bytes_ljust(int width, String fillchar) {
        char pad = parse_fillchar("ljust", fillchar);
        int n = width - getString().length();
        if (n <= 0) {
            return new PyBytes(getString());
        }
        return new PyBytes(getString() + padding(n, pad));
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.bytes_rjust_doc)
    public final PyBytes bytes_rjust(int width, String fillchar) {
        char pad = parse_fillchar("rjust", fillchar);
        int n = width - getString().length();
        if (n <= 0) {
            return new PyBytes(getString());
        }
        return new PyBytes(padding(n, pad) + getString());
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.bytes_center_doc)
    public final PyBytes bytes_center(int width, String fillchar) {
        char pad = parse_fillchar("center", fillchar);
        int n = width - getString().length();
        if (n <= 0) {
            return new PyBytes(getString());
        }
        int half = n / 2;
        if (n % 2 > 0 && width % 2 > 0) {
            half += 1;
        }

        return new PyBytes(padding(half, pad) + getString() + padding(n - half, pad));
    }

    @ExposedMethod(doc = BuiltinDocs.bytes_zfill_doc)
    public final PyObject bytes_zfill(int width) {
        return new PyBytes(Encoding.zfill(getString(), width).toString());
    }

    @ExposedMethod(doc = BuiltinDocs.bytes_expandtabs_doc)
    public final PyBytes bytes_expandtabs(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("expandtabs", args, keywords, "tabsize");
        int tabsize = ap.getInt(0, 8);
        return new PyBytes(Encoding.expandtabs(getString(), tabsize));
    }

    @ExposedMethod(doc = BuiltinDocs.bytes_capitalize_doc)
    public final PyBytes bytes_capitalize() {
        return new PyBytes(Encoding.capitalize(getString()));
    }

    @ExposedMethod(defaults = "-1", doc = BuiltinDocs.bytes_replace_doc)
    public final PyBytes bytes_replace(PyObject oldPieceObj, PyObject newPieceObj, int count) {
        if (oldPieceObj instanceof PyUnicode || newPieceObj instanceof PyUnicode) {
            throw Py.TypeError(BYTES_REQUIRED_ERROR);
        } else {
            // Neither is a PyUnicode: both ought to be some kind of bytes with the buffer API.
            String oldPiece = Encoding.asStringOrError(oldPieceObj, false);
            String newPiece = Encoding.asStringOrError(newPieceObj, false);
            return new PyBytes(Encoding._replace(getString(), oldPiece, newPiece, count));
        }
    }

    @ExposedMethod(doc = BuiltinDocs.bytes_join_doc)
    public final PyBytes bytes_join(PyObject obj) {
        PySequence seq = fastSequence(obj, "");
        int seqLen = seq.__len__();
        if (seqLen == 0) {
            return Py.EmptyByte;
        }

        PyObject item;
        if (seqLen == 1) {
            item = seq.pyget(0);
            return new PyBytes(Py.unwrapBuffer(item));
        }

        ByteBuffer readonly = readBuf();
        ByteBuffer ret = ByteBuffer.allocate(__len__() * seqLen + INITIAL_CAPACITY);
        for (int i = 0; i < seqLen; i++) {
            item = seq.pyget(i);
            if (i != 0) {
                try {
                    ret.put(readonly);
                } catch (BufferOverflowException e) {
                    ret = resize(ret, readonly);
                }
                readonly.rewind();
            }

            requiresBytesLike(item);
            try(PyBuffer buf = ((BufferProtocol) item).getBuffer(FULL_RO)) {
                ByteBuffer bytes = buf.getNIOByteBuffer();
                try {
                    ret.put(bytes);
                } catch (BufferOverflowException e) {
                    ret = resize(ret, bytes);
                }
            }
        }
        return new PyBytes(ret.flip());
    }

    private ByteBuffer resize(ByteBuffer buf, ByteBuffer bytes) {
        ByteBuffer tmp = ByteBuffer.allocate(buf.limit() + bytes.limit() + INITIAL_CAPACITY);
        buf.flip();
        tmp.put(buf);
        tmp.put(bytes);
        return tmp;
    }

    @ExposedMethod(defaults = {"null", "null"}, doc = BuiltinDocs.str_startswith_doc)
    public final boolean bytes_startswith(PyObject prefix, PyObject startObj, PyObject endObj) {
        if (prefix instanceof PyUnicode) {
            throw Py.TypeError("startswith first arg must be bytes or a tuple of bytes, not str");
        }
        return Encoding.startswith(getString(), prefix, startObj, endObj, __len__());
    }

    @ExposedMethod(defaults = {"null", "null"}, doc = BuiltinDocs.str_endswith_doc)
    public final boolean bytes_endswith(PyObject suffix, PyObject startObj, PyObject endObj) {
        return Encoding.endswith(getString(), suffix, startObj, endObj, __len__());
    }

    @ExposedMethod(doc = BuiltinDocs.bytes_translate_doc)
    public final PyBytes bytes_translate(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("translate", args, keywords, "table", "delete");
        PyObject tableObj = ap.getPyObject(0);
        if (!(tableObj instanceof BufferProtocol)) {
            throw Py.TypeError(String.format("a bytes-like object is required, not '%s'", tableObj.getType().fastGetName()));
        }
        String table = Encoding.asStringOrNull(tableObj);
        PyObject deletecharsObj = ap.getPyObject(1, null);
        String deletechars = null;
        if (deletecharsObj != null) {
            deletechars = Encoding.asStringOrError(deletecharsObj);
        }
        // Accept anythiong with the buffer API or null
        return _translate(table, deletechars);
    }

    /**
     * Helper common to the Python and Java API implementing <code>str.translate</code> returning a
     * copy of this string where all characters (bytes) occurring in the argument
     * <code>deletechars</code> are removed (if it is not <code>null</code>), and the remaining
     * characters have been mapped through the translation <code>table</code>, which must be
     * equivalent to a string of length 256 (if it is not <code>null</code>).
     *
     * @param table of character (byte) translations (or <code>null</code>)
     * @param deletechars set of characters to remove (or <code>null</code>)
     * @return transformed byte string
     */
    private final PyBytes _translate(String table, String deletechars) {

        if (table != null && table.length() != 256) {
            throw Py.ValueError("translation table must be 256 characters long");
        }

        StringBuilder buf = new StringBuilder(getString().length());

        for (int i = 0; i < getString().length(); i++) {
            char c = getString().charAt(i);
            if (deletechars != null && deletechars.indexOf(c) >= 0) {
                continue;
            }
            if (table == null) {
                buf.append(c);
            } else {
                try {
                    buf.append(table.charAt(c));
                } catch (IndexOutOfBoundsException e) {
                    throw Py.TypeError("translate() only works for 8-bit character strings");
                }
            }
        }
        return new PyBytes(buf.toString());
    }

    @ExposedClassMethod(doc = BuiltinDocs.bytes_fromhex_doc)
    public static final PyObject fromhex(PyType type, PyObject obj) {
        if (!(obj instanceof PyUnicode)) {
            throw Py.TypeError(String.format("fromhex() argument must be str, not %s", obj.getType().fastGetName()));
        }
        byte[] argbuf = ((PyUnicode) obj).getString().getBytes();
        int arglen = argbuf.length;

        StringBuilder retbuf = new StringBuilder(arglen/2);

        for (int i = 0; i < arglen;) {
            byte ch = argbuf[i++];
            while (ch == ' ' && i < arglen) {
                ch = argbuf[i++];
            }
            if (ch == ' ') {
                break;
            }
            int top = Character.digit(ch, 16);
            if (top == -1 || i >= arglen) {
                throw Py.ValueError(String.format("Non-hexadecimal digit found in fromhex() arg at position %d", i - 1));
            }
            int bot = Character.digit(argbuf[i++], 16);
            if (bot == -1)
                throw Py.ValueError(String.format("Non-hexadecimal digit found in fromhex() arg at position %d", i - 1));
            retbuf.append((char) ((top << 4) + bot));
        }
        PyObject value = new PyBytes(retbuf.toString());
        if (type != TYPE) {
            return type.__call__(new PyObject[]{value}, Py.NoKeywords);
        }
        return value;
    }

    @ExposedClassMethod(defaults = {"null"}, doc = BuiltinDocs.bytes_maketrans_doc)
    public static final PyObject bytes_maketrans(PyType type, PyObject fromstr, PyObject tostr, PyObject other) {
        if (fromstr.__len__() != tostr.__len__()) {
            throw Py.ValueError("maketrans arguments must have same length");
        }
        byte[] res = new byte[256];
        for (int i = 0; i < 256; i++) {
            res[i] = (byte) i;
        }
        try(
                PyBuffer frm = BaseBytes.getViewOrError(fromstr);
                PyBuffer to = BaseBytes.getViewOrError(tostr)
                ) {
            for (int i = 0; i < frm.getLen(); i++) {
                res[frm.byteAt(i) & 0xFF] = to.byteAt(i);
            }
        }
        return new PyBytes(new String(res, StandardCharsets.ISO_8859_1));
    }

    @ExposedMethod(doc = BuiltinDocs.bytes_islower_doc)
    public final boolean bytes_islower() {
        return Encoding.isLowercase(getString());
    }

    @ExposedMethod(doc = BuiltinDocs.bytes_isupper_doc)
    public final boolean bytes_isupper() {
        return Encoding.isUppercase(getString());
    }

    @ExposedMethod(doc = BuiltinDocs.bytes_isalpha_doc)
    public final boolean bytes_isalpha() {
        return Encoding.isAlpha(getString());
    }

    @ExposedMethod(doc = BuiltinDocs.bytes_isalnum_doc)
    public final boolean bytes_isalnum() {
        return Encoding.isAlnum(getString());
    }

    @ExposedMethod(doc = BuiltinDocs.str_isdecimal_doc)
    public final boolean bytes_isdecimal() {
        return Encoding.isDecimal(getString());
    }

    @ExposedMethod(doc = BuiltinDocs.bytes_isdigit_doc)
    public final boolean bytes_isdigit() {
        return Encoding.isDigit(getString());
    }

    @ExposedMethod(doc = BuiltinDocs.str_isnumeric_doc)
    public final boolean bytes_isnumeric() {
        return Encoding.isNumeric(getString());
    }

    @ExposedMethod(doc = BuiltinDocs.bytes_istitle_doc)
    public final boolean bytes_istitle() {
        return Encoding.isTitle(getString());
    }

    @ExposedMethod(doc = BuiltinDocs.bytes_isspace_doc)
    public final boolean bytes_isspace() {
        return Encoding.isSpace(getString());
    }

    public PyObject decode() {
        return decode(null, null);
    }

    public PyObject decode(String encoding) {
        return decode(encoding, null);
    }

    public PyObject decode(String encoding, String errors) {
        return codecs.decode(this, encoding, errors);
    }

    @ExposedMethod(doc = BuiltinDocs.bytes_decode_doc)
    public final PyObject bytes_decode(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("decode", args, keywords, "encoding", "errors");
        String encoding = ap.getString(0, "UTF-8");
        String errors = ap.getString(1, "strict");
        return decode(encoding, errors);
    }

    @ExposedMethod(doc = ""/*BuiltinDocs.str__formatter_parser_doc*/)
    public final PyObject bytes__formatter_parser() {
        return new MarkupIterator(getString());
    }

    @ExposedMethod(doc = ""/*BuiltinDocs.str__formatter_field_name_split_doc*/)
    public final PyObject bytes__formatter_field_name_split() {
        FieldNameIterator iterator = new FieldNameIterator(getString(), true);
        return new PyTuple(iterator.pyHead(), iterator);
    }

    @Override
    @ExposedMethod(doc = BuiltinDocs.bytes___format___doc)
    public PyObject __format__(PyObject formatSpec) {
        return Encoding.format(getString(), formatSpec, true);
    }

    @ExposedMethod(doc = BuiltinDocs.bytearray_hex_doc)
    public final PyObject hex() {
        ByteBuffer readonly = readBuf();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < readonly.limit(); i++) {
            sb.append(String.format("%02x", readonly.get(i)));
        }
        return new PyUnicode(sb.toString());
    }

    @Override
    public PyObject do_richCompare(PyObject other, CompareOp op) {
        if (!(other instanceof BufferProtocol)) {
            if (op == CompareOp.EQ || op == CompareOp.NE) {
                return op.bool(1);
            }
            return Py.NotImplemented;
        }
        if (this == other) {
            switch (op) {
                case EQ:
                case GE:
                case LE:
                    return Py.True;
                default:
                    return Py.False;
            }
        }
        if (__len__() != other.__len__()) {
            if (op == CompareOp.EQ || op == CompareOp.NE) {
                return op.bool(1);
            }
        }
        int ret = 0;
        ByteBuffer readonly = readBuf();
        try (PyBuffer buffer = ((BufferProtocol) other).getBuffer(FULL_RO)) {
            ByteBuffer otherRead = buffer.getNIOByteBuffer();
            int minLen = Math.min(readonly.limit(), otherRead.limit());
            for (int i = 0; i < minLen; i++) {
                byte b1 = readonly.get(i);
                byte b2 = otherRead.get(i);
                if (b1 != b2) {
                    ret = b1 > b2 ? 1 : -1;
                    break;
                }
            }
            int delta = readonly.limit() - otherRead.limit();
            if (ret != 0 || delta == 0) {
                return op.bool(ret);
            }
            return op.bool(delta > 0 ? 1 : -1);
        }
    }

    @Override
    public String asString(int index) {
        return getString();
    }

    @Override
    public String asString() {
        return getString();
    }

    @Override
    public int asInt() {
        // We have to override asInt/Long/Double because we override __int/long/float__,
        // but generally don't want implicit atoi conversions for the base types. blah
        asNumberCheck("__int__", "an integer");
        return super.asInt();
    }

    @Override
    public long asLong() {
        asNumberCheck("__int__", "an integer");
        return super.asLong();
    }

    @Override
    public double asDouble() {
        asNumberCheck("__float__", "a float");
        return super.asDouble();
    }

    private void asNumberCheck(String methodName, String description) {
        PyType type = getType();
        if (type == PyBytes.TYPE || type == PyUnicode.TYPE || type.lookup(methodName) == null) {
            throw Py.TypeError(description + " is required");
        }
    }

    @Override
    protected String unsupportedopMessage(String op, PyObject o2) {
        if (op.equals("+")) {
            return "cannot concatenate ''{1}'' and ''{2}'' objects";
        }
        return super.unsupportedopMessage(op, o2);
    }

    private static void byteCheck(int n) {
        if (n > 0xFF || n < 0) {
            throw Py.ValueError("byte must be in range(0, 255)");
        }
    }

    private void requiresBytesLike(PyObject obj) {
        if (!(obj instanceof BufferProtocol)) {
            throw Py.TypeError(String.format("expected a bytes-like object, %s found", obj.getType().fastGetName()));
        }
    }
}


