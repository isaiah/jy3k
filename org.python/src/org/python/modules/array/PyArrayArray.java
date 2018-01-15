package org.python.modules.array;

import org.python.annotations.ExposedClassMethod;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.bootstrap.Import;
import org.python.core.ArgParser;
import org.python.core.BufferProtocol;
import org.python.core.BuiltinDocs;
import org.python.core.CompareOp;
import org.python.core.Py;
import org.python.core.PyBuffer;
import org.python.core.PyBytes;
import org.python.core.PyException;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PySlice;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.core.buffer.SimpleBuffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ExposedType(name = "array.array", doc = BuiltinDocs.array_array_doc)
public class PyArrayArray extends PyObject implements BufferProtocol {
    public static final PyType TYPE = PyType.fromClass(PyArrayArray.class);
    private static final int INITIAL_CAPACITY = 128;
    protected MachineFormatCode formatCode;
    private ByteBuffer buf;

    public PyArrayArray(PyType subtype) {
        this(subtype, MachineFormatCode.SIGNED_INT8, INITIAL_CAPACITY);
    }

    public PyArrayArray(PyType type, MachineFormatCode code) {
        this(type, code, INITIAL_CAPACITY);
    }

    public PyArrayArray(PyType type, MachineFormatCode code, int size) {
        super(type);
        this.formatCode = code;
        this.buf = ByteBuffer.allocate(size);
    }

    @ExposedNew
    static PyObject _new(PyNewWrapper new_, boolean init, PyType subtype, PyObject[] args,
                              String[] keywords) {
        ArgParser ap = new ArgParser("array", args, keywords, "typecode", "initializer");
        char typecode = ap.getString(0).charAt(0);
        PyObject initial = ap.getPyObject(1, null);
        PyArrayArray ret;
        if (subtype == TYPE) {
            ret = new PyArrayArray(subtype, MachineFormatCode.formatCode(typecode), INITIAL_CAPACITY);
        } else {
            ret = new PyArrayArrayDerived(subtype, MachineFormatCode.formatCode(typecode), INITIAL_CAPACITY);
        }
        if (initial != null) {
            if (initial instanceof PyArrayArray) {
                PyArrayArray initArray = (PyArrayArray) initial;
                if (initArray.formatCode.typecode() == 'u' && typecode != 'u') {
                    throw Py.TypeError(String.format("cannot use a unicode array to initialize an array with typecode '%s'", typecode));
                }
                ByteBuffer readBuf = initArray.buf.asReadOnlyBuffer();
                readBuf.flip();
                ret.buf = ByteBuffer.allocate(readBuf.remaining());
                ret.buf.put(readBuf);
            } else {
                if (initial instanceof PyUnicode && typecode != 'u') {
                    throw Py.TypeError(String.format("cannot use a str to initialize an array with typecode '%s'", typecode));
                }
                ret.doExtend(initial);
            }

        }
        return ret;
    }

    @ExposedClassMethod
    public static PyArrayArray zeros(PyType subtype, int n, char typecode) {
        return new PyArrayArray(subtype, MachineFormatCode.formatCode(typecode), n);
    }

    @ExposedMethod(names = {"fromstring", "frombytes"})
    public PyObject frombytes(PyObject bytes) {
        if (!(bytes instanceof BufferProtocol)) {
            throw Py.TypeError(String.format("a bytes-like object is required, not '%s'", bytes.getType().fastGetName()));
        }
        doExtend(bytes);
        return this;
    }

    @ExposedMethod(names = {"tostring", "tobytes"})
    public PyObject tobytes() {
        return new PyBytes(readBuf());
    }

    @ExposedMethod
    public PyObject fromlist(PyObject list) {
        if (!(list instanceof PyList)) {
            throw Py.TypeError("arg must be list");
        }
        doExtend(list);
        return this;
    }

    @ExposedMethod
    public PyObject tolist() {
        return new PyList(asList());
    }

    @ExposedMethod
    public void tofile(PyObject f){
        f.invoke("write", tobytes());
    }

    @ExposedMethod
    public void fromfile(PyObject f, PyObject n) {
        PyObject bytes = f.invoke("read", n);
        doExtend(bytes);
    }

    @ExposedMethod
    public PyObject append(PyObject val) {
        checkCapacity(formatCode.getItemSize());
        formatCode.setitem(buf, buf.position(), val);
        buf.position(buf.position()+ formatCode.getItemSize());
        return this;
    }

    // insert one byte
    private void ins1(byte b) {
        checkCapacity(1);
        buf.put(b);
    }

    @ExposedMethod
    public PyObject extend(PyObject bb) {
        if (bb instanceof PyArrayArray) {
            char c = ((PyArrayArray) bb).typecode();
            if (c != typecode()) {
                if (c == 'u') {
                    throw Py.TypeError(String.format("cannot use a unicode array to initialize an array with typecode '%s'", typecode()));
                }
                throw Py.TypeError("can only extend with array of same kind");
            }
        } else if (bb instanceof PyUnicode && typecode() != 'u') {
            throw Py.TypeError(String.format("cannot use a str to initialize an array with typecode '%s'", typecode()));
        }
        doExtend(bb);
        return this;
    }

    private void doExtend(PyObject bb) {
        if (bb instanceof PyArrayArray) {
            if (((PyArrayArray) bb).byteLength() > buf.remaining()) {
                ByteBuffer newBuf = ByteBuffer.allocate(byteLength() + ((PyArrayArray) bb).byteLength() + INITIAL_CAPACITY);
                newBuf.put(readBuf());
                buf = newBuf;
            }
            buf.put(((PyArrayArray) bb).readBuf());
            return;
        }
        if (bb instanceof BufferProtocol) {
            byte[] buf = Py.unwrapBuffer(bb);
            if (buf.length % itemsize() != 0) {
                throw Py.ValueError("bytes length not a multiple of item size");
            }
            for (byte b : buf) {
                ins1(b);
            }
        } else {
            for (PyObject val : bb.asIterable()) {
                append(val);
            }
        }
    }

    @ExposedMethod
    public PyObject buffer_info() {
        return new PyTuple(Py.Zero, new PyLong(buf.limit()));
    }

    @ExposedGet
    public int itemsize() {
        return formatCode.getItemSize();
    }

    @ExposedMethod(defaults = {"-1"})
    public PyObject pop(int index) {
        if (buf.position() == 0) {
            throw Py.IndexError("pop from empty array");
        }

        index = checkIndex(index);
        int itemsize = itemsize();
        int len = (__len__() - 1) * itemsize;
        int bufIndex = index * itemsize;

        PyObject ret = formatCode.getitem(bufferInternal(), bufIndex);
        for (int i = bufIndex; i <= len; i++) {
            buf.put(i, buf.get(i + itemsize));
        }
        buf.position(buf.position() - itemsize);

        return ret;
    }

    @ExposedMethod
    public PyObject reverse() {
        int i = __len__() - 1;
        for (PyObject v : asList()) {
            formatCode.setitem(buf, i-- * itemsize(), v);
        }
        return this;
    }

    @ExposedMethod
    public PyObject insert(PyObject indexObj, PyObject v) {
        int index = indexObj.asIndex();
        if (index < 0) {
            index += __len__();
        }
        if (index < 0) {
            index = 0;
        } else if (index > __len__()) {
            index = __len__();
        }
        insert(index, v);
        return this;
    }

    private void insert(int index, PyObject v) {
        int itemsize = itemsize();
        checkCapacity(itemsize);
        int len = (index + 1) * itemsize;
        for (int i = (__len__() + 1) * itemsize; i >= len; i--) {
            buf.put(i, buf.get(i - itemsize));
        }
        formatCode.setitem(buf, index * itemsize, v);
        buf.position(buf.position() + itemsize);
    }

    @ExposedMethod
    public PyObject remove(PyObject v) {
        pop(index(v));
        return this;
    }

    @ExposedMethod
    public int count(PyObject v) {
        int i = 0;
        for (PyObject obj: asList()) {
            if (v.do_richCompareBool(obj, CompareOp.EQ)) {
                i++;
            }
        }
        return i;
    }

    @ExposedMethod
    public int index(PyObject v) {
        int i = 0;
        for (PyObject obj: asList()) {
            if (v.do_richCompareBool(obj, CompareOp.EQ)) {
                return i;
            }
            i++;
        }
        throw Py.ValueError("array.index(x): x not in list");
    }

    @Override
    @ExposedMethod
    public int __len__() {
        return byteLength() / formatCode.getItemSize();
    }

    private int byteLength() {
        return readBuf().remaining();
    }

    @ExposedMethod(names = {"__mul__", "__rmul__"})
    public PyObject __mul__(PyObject n) {
        checkNumber(n);
        int x = n.asInt();
        if (x < 0) {
            return new PyArrayArray(TYPE, formatCode, 0);
        }
        ByteBuffer readonly = readBuf();
        readonly.mark();
        long endLen = byteLength() * (long) x;
        if (endLen > Integer.MAX_VALUE || endLen < 0) {
            throw Py.OverflowError("array size too big");
        }
        PyArrayArray ret = new PyArrayArray(TYPE, formatCode, (int) endLen);
        for (int i = 0; i < x; i++) {
            ret.buf.put(readonly);
            readonly.reset();
        }
        return ret;
    }

    @ExposedMethod
    public PyObject __imul__(PyObject n) {
        checkNumber(n);
        int x = n.asInt();
        ByteBuffer readonly = readBuf();
        readonly.mark();
        long endLen = readonly.limit() * (long) x;
        if (endLen > Integer.MAX_VALUE || endLen < 0) {
            throw Py.OverflowError("array size too big");
        }
        checkCapacity((int) endLen - readonly.limit());
        for (int i = 1; i < x; i++) {
            buf.put(readonly);
            readonly.reset();
        }
        return this;
    }

    @ExposedMethod
    public PyObject __add__(PyObject other) {
        if (!(other instanceof PyArrayArray)) {
            throw Py.TypeError(String.format("can only append an array (not \"%s\") to array", other.getType().fastGetName()));
        }
        checkFormatCode((PyArrayArray) other);
        PyArrayArray ret = new PyArrayArray(TYPE, formatCode, byteLength() + ((PyArrayArray) other).byteLength());
        ByteBuffer newBuf = ret.buf;
        newBuf.put(readBuf());
        newBuf.put(((PyArrayArray) other).readBuf());
        return ret;
    }

    @ExposedMethod
    public PyObject __iadd__(PyObject other) {
        if (!(other instanceof PyArrayArray)) {
            throw Py.TypeError(String.format("can only append an array (not \"%s\") to array", other.getType().fastGetName()));
        }
        checkFormatCode((PyArrayArray) other);
        checkCapacity(byteLength() + ((PyArrayArray) other).byteLength());
        buf.put(((PyArrayArray) other).readBuf());
        return this;

    }

    @Override
    @ExposedMethod
    public PyObject __getitem__(PyObject indexObj) {
        if (indexObj.isIndex()) {
            int index = indexObj.asIndex();
            return __getitem__(index);
        } else if (indexObj instanceof PySlice) {
            int[] indices = ((PySlice) indexObj).indicesEx(__len__());
            int start = indices[0], end = indices[1];
            int size = indices[3];
            int step = indices[2];
            int itemsize = itemsize();
            PyArrayArray ret = new PyArrayArray(getType(), formatCode, size * itemsize);
            if (step > 0) {
                for (int i = start; i < end; i += step) {
                    ret.append(formatCode.getitem(buf, i * itemsize));
                }
            } else {
                for (int i = start; i > end; i += step) {
                    ret.append(formatCode.getitem(buf, i * itemsize));
                }
            }
            return ret;
        } else {
            throw Py.TypeError("array indices must be integer");
        }
    }

    @Override
    public PyObject __getitem__(int index) {
        index = checkIndex(index);
        try {
            return formatCode.getitem(bufferInternal(), index * itemsize());
        } catch (IndexOutOfBoundsException e) {
            throw Py.IndexError("array index out of range");
        }
    }

    @Override
    @ExposedMethod
    public void __setitem__(PyObject indexObj, PyObject val) {
        if (val instanceof PyArrayArray) {
            checkFormatCode((PyArrayArray) val);
        }
        if (indexObj.isIndex()) {
            int index = indexObj.asIndex();
            index = checkIndex(index);
            formatCode.setitem(bufferInternal(), index * itemsize(), val);
        } else if (indexObj instanceof PySlice) {
            int[] indices = ((PySlice) indexObj).indicesEx(__len__());
            int itemsize = itemsize();
            int start = indices[0], end = indices[1], step = indices[2];
            if (step != 1) {
                if (val == this) {
                    byte[] replica = new byte[byteLength()];
                    ByteBuffer readonly = readBuf();
                    readonly.get(replica); // copy
                    readonly = ByteBuffer.wrap(replica);
                    if (step > 0) {
                        for (int i = start, j = 0; i < end; i += step, j++) {
                            formatCode.setitem(buf, i * itemsize, formatCode.getitem(readonly, j * itemsize));
                        }
                    } else {
                        for (int i = start, j = 0; i > end; i += step, j++) {
                            formatCode.setitem(buf, i * itemsize, formatCode.getitem(readonly, j * itemsize));
                        }
                    }
                } else {
                    if (step > 0) {
                        for (int i = start, j = 0; i < end; i += step, j++) {
                            formatCode.setitem(buf, i * itemsize, val.__getitem__(j));
                        }
                    } else {
                        for (int i = start, j = 0; i > end; i += step, j++) {
                            formatCode.setitem(buf, i * itemsize, val.__getitem__(j));
                        }
                    }
                }
            } else {
                int i = indices[0] * itemsize;
                byte[] rest = new byte[(__len__() - indices[1]) * itemsize];
                int offset = indices[1] * itemsize;
                if (rest.length > 0) {
                    ByteBuffer readonly = readBuf();
                    readonly.position(offset);
                    readonly.get(rest);
                }
                if (val == this) {
                    ByteBuffer readonly = readBuf();
                    buf.position(i);
                    buf.put(readonly);
                } else {
                    for (PyObject v : val.asIterable()) {
                        formatCode.setitem(buf, i, v);
                        i += itemsize;
                    }
                    buf.position(i);
                }
                checkCapacity(byteLength() + rest.length);
                buf.put(rest);
            }
        } else {
            throw Py.TypeError("array indices must be integer");
        }
    }

    @Override
    @ExposedMethod
    public void __delitem__(PyObject indexObj) {
        if (indexObj.isIndex()) {
            int index = indexObj.asIndex();
            pop(index);
        } else if (indexObj instanceof PySlice) {
            int[] indices = ((PySlice) indexObj).indicesEx(__len__());
            for (int i = indices[0]; i < indices[1]; i += indices[2]) {
                pop(i);
            }
        } else {
            throw Py.TypeError("array indices must be integer");
        }
    }

    @ExposedMethod
    public PyObject __reduce_ex__(int version) {
        if (version < 3) {
            return new PyTuple(getType(), new PyTuple(new PyUnicode(typecode()), new PyList(asList())), Py.None);
        }
        PyObject arrayModule = Import.importModule("array");
        PyObject reconstructFunc = arrayModule.__findattr__("_array_reconstructor");
        return new PyTuple(reconstructFunc, new PyTuple(getType(), new PyUnicode(typecode()), new PyLong(itemsize()), tobytes()), Py.None);
    }

    @Override
    public String toString() {
        int len = __len__();
        if (len == 0) {
            return String.format("array('%s')", typecode());
        }
        return String.format("array('%s', [%s])", typecode(), String.join(", ", asList().stream().map(PyObject::toString).collect(Collectors.toList())));
    }

    private List<PyObject> asList() {
        ByteBuffer readonly = readBuf();
        int len = __len__();
        List<PyObject> ret = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            ret.add(formatCode.getitem(readonly, i * itemsize()));
        }
        return ret;
    }

    @Override
    public PyObject do_richCompare(PyObject other, CompareOp op) {
        if (other instanceof PyArrayArray) {
            PyArrayArray o = (PyArrayArray) other;
            int ret = 0;
            ByteBuffer readThis = readBuf();
            ByteBuffer readThat = o.readBuf();
            int len = readThis.remaining();
            if (len == readThat.remaining()) {
                for(int i = 0; i < len; i++) {
                    byte a = readThis.get();
                    byte b = readThat.get();
                    if (a != b) {
                        ret = a > b ? 1 : -1;
                        break;
                    }
                }
            } else {
                ret = len > readThat.remaining() ? 1 : -1;
            }
            return op.bool(ret);
        }
        if (op == CompareOp.NE || op == CompareOp.EQ) {
            return op.bool(1);
        }
        return Py.NotImplemented;
    }

    protected ByteBuffer readBuf() {
        ByteBuffer readonly = buf.asReadOnlyBuffer();
        readonly.flip();
        return readonly;
    }

    protected ByteBuffer bufferInternal() {
        return buf;
    }

    @ExposedGet(name = "typecode")
    public String getTypeCode() {
        return String.valueOf(typecode());
    }

    private char typecode() {
        return formatCode.typecode();
    }

    private void checkFormatCode(PyArrayArray other) {
        if (formatCode != other.formatCode) {
            throw Py.TypeError("requires array of the same kind");
        }
    }

    private void checkCapacity(int size) {
        int newSize = buf.position() + size;
        if (buf.position() + size >= buf.limit()) {
            int proposedSize = buf.limit() * 2 + INITIAL_CAPACITY;
            if (newSize < proposedSize) {
                newSize = proposedSize;
            }
            ByteBuffer newBuf = ByteBuffer.allocate(newSize);
            ByteBuffer readonly = buf.asReadOnlyBuffer();
            readonly.flip();
            newBuf.put(readonly);
            buf = newBuf;
        }
    }

    private int checkIndex(int index) {
        if (index < 0) {
            index += __len__();
        }
        if (index >= __len__() || index < 0) {
            throw Py.IndexError("array index out of bounds");
        }
        return index;
    }

    private void checkNumber(PyObject n) {
        if (!(n instanceof PyLong)) {
            throw Py.TypeError(String.format("can't multiply sequence by non-int of type '%s'", n.getType().fastGetName()));
        }
    }

    @Override
    public PyBuffer getBuffer(int flags) throws PyException {
        return new SimpleBuffer(flags, buf.array());
    }

    @Override
    public ByteBuffer getBuffer() {
        return readBuf();
    }

    @Override
    public int write(ByteBuffer bytes) throws PyException {
        int len = bytes.remaining();
        if (len % itemsize() != 0) {
            throw Py.ValueError("bytes length not a multiple of item size");
        }
        checkCapacity(len);
        buf.put(bytes);
        return len;
    }
}
