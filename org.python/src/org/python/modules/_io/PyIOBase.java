/* Copyright (c)2012 Jython Developers */
package org.python.modules._io;

import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSet;
import org.python.annotations.ExposedType;
import org.python.core.BufferProtocol;
import org.python.core.Py;
import org.python.core.PyBUF;
import org.python.core.PyBuffer;
import org.python.core.PyByteArray;
import org.python.core.PyBytes;
import org.python.core.PyException;
import org.python.core.PyList;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.core.Traverseproc;
import org.python.core.Visitproc;
import org.python.core.buffer.SimpleStringBuffer;
import org.python.core.finalization.FinalizableBuiltin;
import org.python.core.finalization.FinalizeTrigger;
import org.python.modules.array.PyArrayArray;

/**
 * The Python module <code>_io._IOBase</code>, on which the <code>io</code> module depends directly.
 * <p>
 * <b>Implementation note:</b> The code is based heavily on the Jython 2.6-ish
 * <code>_fileio.PyFileIO</code>, the purely Java-accessible {@link org.python.core.io.IOBase} (both
 * Philip Jenvey's work), and the Python implementation in <code>Lib/_pyio</code>. We do not simply
 * delegate to the implementation in {@link org.python.core.io} because of the need to override
 * parts of the implementation in Python subclasses. A call to {@link #close()}, for example, is
 * required to call {@link #flush()}, but if delegated to the pure Java implementation would not
 * call the version of <code>flush()</code> overridden in a Python sub-class of
 * <code>_io._IOBase</code>. Equally, the use made by <code>PyRawIOBase.read(int)</code> of
 * <code>readinto(bytearray)</code> would not pick up the version of <code>readinto</code> defined
 * in Python.
 */
@ExposedType(name = "_io._IOBase")
public class PyIOBase extends PyObject implements FinalizableBuiltin, Traverseproc {

    public static final PyType TYPE = PyType.fromClass(PyIOBase.class);

    /** The ioDelegate's closer object; ensures the stream is closed at shutdown */
    private Closer<PyIOBase> closer;

    protected int fileno;

    @ExposedGet
    protected PyObject name;

    @ExposedGet
    protected String mode;

    @ExposedMethod(names = {"fileno"})
    public int fileno() {
        return fileno;
    }

    protected PyIOBase() {
        this(TYPE);
        FinalizeTrigger.ensureFinalizer(this);
    }

    protected PyIOBase(PyType subtype) {
        super(subtype);
        closer = new Closer<PyIOBase>(this, Py.getSystemState());
        FinalizeTrigger.ensureFinalizer(this);
    }

    /**
     * Provide a dictionary in the object, so that methods and attributes may be overridden at
     * instance level.
     */
    @ExposedGet
    protected PyStringMap __dict__ = new PyStringMap();

    @Override
    public PyStringMap fastGetDict() {
        return __dict__;
    }

    @ExposedNew
    static PyObject _IOBase___new__(PyNewWrapper new_, boolean init, PyType subtype,
            PyObject[] args, String[] keywords) {
        if (new_.for_type == subtype) {
            // We only want an _io._IOBase, so the constructor does it all
            return new PyIOBase();
        } else {
            // We want some sub-class of it (in which __init__ will be called by the caller)
            return new PyIOBaseDerived(subtype);
        }
    }

    /**
     * Convenience method returning a PyException(_io.UnsupportedOperation), ripe for throwing, and
     * naming the unsupported operation. Like many similar methods, this does not actually throw
     * (raise) the exception: it should be thrown from the place where the problem is.
     *
     * @param op name of operation
     * @return the exception for throwing
     */
    protected PyException unsupported(String op) {
        String fmt = "%s.%s() not supported";
        String msg = String.format(fmt, getType().fastGetName(), op);
        return _io.UnsupportedOperation(msg);
    }

    @ExposedMethod(doc = "Internal: raise an exception for unsupported operations.")
    public final void _IOBase__unsupported(String name) {
        throw unsupported(name);
    }

    /**
     * Position the read or write pointer at a given byte offset <code>pos</code> relative to a
     * position indicated by <code>whence</code>.
     * <ul>
     * <li>If <code>whence</code>=0, the position will be set to <code>pos</code> bytes.</li>
     * <li>If <code>whence</code>=1 the position will be set to the current position plus
     * <code>pos</code>.</li>
     * <li>If <code>whence</code>=2 the position will be set to the stream size plus
     * <code>pos</code> (and usually <code>pos</code>&lt;=0).</li>
     * </ul>
     *
     * @param pos relative to the specified point
     * @param whence 0=from start, 1=from here, 2=from end
     * @return the new current position
     */
    public long seek(long pos, int whence) {
        return _IOBase_seek(pos, whence);
    }

    /**
     * Position the read or write pointer at a given byte offset <code>pos</code> relative to the
     * start.
     *
     * @param pos relative to the start
     * @return the new current position
     */
    public final long seek(long pos) {
        return seek(pos, 0);
    }

    @ExposedMethod(defaults = "0")
    public final int _IOBase_seek(long pos, int whence) {
        throw unsupported("seek");
    }

    /**
     * Get the current stream position.
     *
     * @return stream position
     */
    @ExposedMethod
    public int tell() {
        return 0;
    }

    /**
     * Truncate file to <code>size</code> bytes.
     *
     * @param size requested for stream
     * @return the new size
     */
    public long truncate(long size) {
        return _IOBase_truncate(null); // Just raises an exception anyway
    }

    /**
     * Truncate file to <code>size</code> bytes to the current position (as reported by
     * <code>tell()</code>).
     *
     * @return the new size
     */
    public long truncate() {
        return _IOBase_truncate(null);
    }

    @ExposedMethod(defaults = "null")
    public final long _IOBase_truncate(PyObject size) {
        throw unsupported("truncate");
    }

    /**
     * Flush write buffers, or no-op for read-only and non-blocking streams. Irrespective of the
     * concrete type of the i/o object, locally-buffered write data is written downstream. Whether
     * the downstream in object is also flushed depends upon the specific type of this object.
     */
    public void flush() {
        _IOBase_flush();
    }

    @ExposedMethod
    public final void _IOBase_flush() {
        // Even types for which this remains a no-op must complain if closed (e.g. BytesIO)
        _checkClosed();
    }

    /**
     * True if the object is closed to further <b>client</b> operations. It is the state accessed by
     * <code>closed</code> and checked by {@link #_checkClosed()}. It may be set without making it
     * an error (for this object and its subclasses) to access some downstream object, notably in
     * the implementation of <code>close()</code>.
     */
    /*
     * This is the analogue of the IS_CLOSED(self) macro and iobase_closed_get() in CPython
     * modules/_io/iobase.c. That function checks for the *existence* of an attribute
     * __IOBase_closed. You can find this exposed in CPython. _pyio.IOBase exposes _IOBase__closed
     * (aka "__closed") for a similar purpose (note different name).
     */
    @ExposedGet(name = "closed")
    protected boolean __closed;

    @ExposedSet(name = "closed")
    public final void closed_readonly(boolean value) {
        readonlyAttributeError("closed");
    }

    /**
     * Close the stream. If closed already, this is a no-op.
     */
    public void close() {
        _IOBase_close();
    }

    @ExposedMethod
    public final void _IOBase_close() {
        if (!__closed) {
            /*
             * The downstream file (file descriptor) will sometimes have been closed by another
             * client. This should raise an error for us, (since data potentially in our buffers
             * will be discarded) but we still must end up closed. the local close comes after the
             * flush, in case operations within flush() test for "the wrong kind of closed".
             */
            try {
                // Cancel the wake-up call
                closer.dismiss();
                // Close should invoke (possibly overridden) flush here.
                invoke("flush");
            } finally {
                // Become closed to further client operations (other than certain enquiries)
                __closed = true;
            }
        }
    }

    /**
     * Is the stream capable of positioning the read/write pointer?
     *
     * @return <code>True</code> if may be positioned
     * @throws PyException(ValueError) if the object is closed to client operations
     */
    public boolean seekable() throws PyException {
        return _IOBase_seekable();
    }

    @ExposedMethod
    public final boolean _IOBase_seekable() throws PyException {
        return false;
    }

    /**
     * Raise an error if the pointer of underlying IO stream <b>is not</b> capable of being
     * positioned.
     *
     * @param msg optional custom message
     * @throws PyException(ValueError) if the object is closed to client operations
     * @throws PyException(IOError) if the stream <b>is not</b> capable of being positioned.
     */
    public void _checkSeekable(String msg) {
        _IOBase__checkSeekable(msg);
    }

    /**
     * Raise an error if the pointer of underlying IO stream <b>is not</b> capable of being
     * positioned.
     *
     * @throws PyException(ValueError) if the object is closed to client operations
     * @throws PyException(IOError) if the stream <b>is not</b> capable of being positioned.
     */
    public final void _checkSeekable() {
        _checkSeekable(null);
    }

    @ExposedMethod(defaults = "null")
    public final void _IOBase__checkSeekable(String msg) {
        if (!invoke("seekable").isTrue()) {
            throw tailoredIOError(msg, "seek");
        }
    }

    /**
     * Is the stream readable?
     *
     * @return <code>true</code> if readable
     * @throws PyException(ValueError) if the object is closed to client operations
     */
    public boolean readable() throws PyException {
        return _IOBase_readable();
    }

    @ExposedMethod
    public final boolean _IOBase_readable() throws PyException {
        return false;
    }

    /**
     * Raise an error if the underlying IO stream <b>is not</b> readable.
     *
     * @param msg optional custom message
     * @throws PyException(ValueError) if the object is closed to client operations
     * @throws PyException(IOError) if the stream <b>is not</b> readable.
     */
    public void _checkReadable(String msg) {
        _IOBase__checkReadable(msg);
    }

    /**
     * Raise an error if the underlying IO stream <b>is not</b> readable.
     *
     * @throws PyException(ValueError) if the object is closed to client operations
     * @throws PyException(IOError) if the stream <b>is not</b> readable.
     */
    public final void _checkReadable() {
        _checkReadable(null);
    }

    @ExposedMethod(defaults = "null")
    public final void _IOBase__checkReadable(String msg) {
        if (!invoke("readable").isTrue()) {
            throw tailoredIOError(msg, "read");
        }
    }

    /**
     * Is the stream writable?
     *
     * @return <code>true</code> if writable
     * @throws PyException(ValueError) if the object is closed to client operations
     */
    public boolean writable() throws PyException {
        return _IOBase_writable();
    }

    @ExposedMethod
    public final boolean _IOBase_writable() throws PyException {
        return false;
    }

    /**
     * Raise an error if the underlying IO stream <b>is not</b> writable.
     *
     * @param msg optional custom message
     * @throws PyException(ValueError) if the object is closed to client operations
     * @throws PyException(IOError) if the stream <b>is not</b> writable.
     */
    public void _checkWritable(String msg) throws PyException {
        _IOBase__checkWritable(msg);
    }

    /**
     * Raise an error if the underlying IO stream <b>is not</b> writable.
     *
     * @throws PyException(ValueError) if the object is closed to client operations
     * @throws PyException(IOError) if the stream <b>is not</b> writable.
     */
    public final void _checkWritable() throws PyException {
        _checkWritable(null);
    }

    @ExposedMethod(defaults = "null")
    public final void _IOBase__checkWritable(String msg) throws PyException {
        if (!invoke("writable").isTrue()) {
            throw tailoredIOError(msg, "writ");
        }
    }

    /**
     * Is the stream closed against further client operations?
     *
     * @return <code>true</code> if closed
     */
    public final boolean closed() {
        return __closed;
    }

    /**
     * Raise an error if the underlying IO stream <b>is</b> closed. (Note opposite sense from
     * {@link #_checkSeekable}, etc..
     *
     * @param msg optional custom message
     * @throws PyException(ValueError) if the object is closed to client operations
     */
    public void _checkClosed(String msg) throws PyException {
        _IOBase__checkClosed(msg);
    }

    public final void _checkClosed() throws PyException {
        _checkClosed(null);
    }

    @ExposedMethod(defaults = "null")
    final void _IOBase__checkClosed(String msg) throws PyException {
        if (closed()) {
            throw Py.ValueError(msg != null ? msg : "I/O operation on closed file");
        }
    }

    /**
     * Called at the start of a context-managed suite (supporting the <code>with</code> clause).
     *
     * @return this object
     */
    public PyObject __enter__() {
        return _IOBase___enter__();
    }

    @ExposedMethod(names = {"__enter__", "__iter__"})
    public final PyObject _IOBase___enter__() {
        _checkClosed();
        return this;
    }

    /**
     * Called at the end of a context-managed suite (supporting the <code>with</code> clause), and
     * will normally close the stream.
     *
     * @return false
     */
    public boolean __exit__(PyObject type, PyObject value, PyObject traceback) {
        return _IOBase___exit__(type, value, traceback);
    }

    @ExposedMethod
    public final boolean _IOBase___exit__(PyObject type, PyObject value, PyObject traceback) {
        invoke("close");
        return false;
    }

    /**
     * Is the stream known to be an interactive console? This relies on the ability of the
     * underlying stream to know, which is not always possible.
     *
     * @return <code>true</code> if a console: <code>false</code> if not or we can't tell
     */
    public boolean isatty() {
        return _IOBase_isatty();
    }

    @ExposedMethod
    final boolean _IOBase_isatty() {
        _checkClosed();
        return false;
    }

    /**
     * Return one line of text (bytes terminates by <code>'\n'</code>), or the specified number of
     * bytes, or the whole stream, whichever is shortest.
     *
     * @param limit maximum number of bytes (<0 means no limit)
     * @return the line (or fragment)
     */
    public PyObject readline(int limit) {
        return _readline(limit);
    }

    /**
     * Return one line of text (bytes terminates by <code>'\n'</code>), or the whole stream,
     * whichever is shorter.
     *
     * @return the line (or fragment)
     */
    public PyObject readline() {
        return _readline(-1);
    }

    @ExposedMethod(defaults = "null")
    public final PyObject _IOBase_readline(PyObject limit) {
        if (limit == null || limit == Py.None) {
            return _readline(-1);
        } else if (limit.isIndex()) {
            return _readline(limit.asInt());
        } else {
            throw tailoredTypeError("integer limit", limit);
        }
    }

    private PyObject _readline(int limit) {

        // Either null, or a thing we can __call__()
        PyObject peekMethod = __findattr__("peek");
        // Either an error, or a thing we can __call__()
        PyObject readMethod = __getattr__("read");

        // We'll count down from the provided limit here
        int remainingLimit = (limit) >= 0 ? limit : Integer.MAX_VALUE;

        /*
         * We have two almost independent versions of the implementation, depending on whether we
         * have a peek() method available.
         */
        if (peekMethod != null) {
            /*
             * This type/object has some kind of read-ahead buffer that we can scan for end of line,
             * and refill as required. We are prepared to make a list of the fragments and join them
             * at the end, but often there is only one.
             */
            PyList list = null;
            PyObject curr = Py.EmptyByte;

            while (remainingLimit > 0) {

                // We hope to get away with just one pass of the loop, but if not ...
                if (curr.isTrue()) {
                    // We have some previous bytes that must be added to the list
                    if (list == null) {
                        // ... be first we need a list to add them to.
                        list = new PyList();
                    }
                    list.add(curr);
                }

                /*
                 * peek() returns a str of bytes from the buffer (if any), doing at most one read to
                 * refill, all without advancing the pointer, or it returns None (in vacuous
                 * non-blocking read).
                 */
                PyObject peekResult = peekMethod.__call__(Py.One);
                if (peekResult.isTrue()) {

                    // Get a look at the bytes themselves
                    PyBuffer peekBuffer = readablePyBuffer(peekResult);

                    try {
                        /*
                         * Scan forwards in the peek buffer to see if there is an end-of-line. Most
                         * frequently this succeeds. The number of bytes to scan is limited to the
                         * (remaining) limit value and the peek-buffer length.
                         */
                        int p = 0, nr = peekBuffer.getLen();
                        if (nr > remainingLimit) {
                            nr = remainingLimit;
                        }

                        while (p < nr) {
                            if (peekBuffer.byteAt(p++) == '\n') {
                                remainingLimit = p; // Engineer an exit from the outer loop
                                break;
                            }
                        }

                        /*
                         * The the next p bytes should be added to the line we are building, and the
                         * pointer advanced: that's a read().
                         */
                        curr = readMethod.__call__(Py.newInteger(p));
                        remainingLimit -= p;

                    } finally {
                        // We must let go of the buffer we were given
                        peekBuffer.release();
                    }

                } else {
                    // peekResult was vacuous: we must be finished
                    curr = Py.EmptyByte;
                    remainingLimit = 0;
                }
            }

            if (list == null) {
                // We went through the loop at most once and the entire answer is in curr
                return curr;

            } else {
                // Previous reads are in the list: return them all stitched together
                if (curr.isTrue()) {
                    list.add(curr);
                }
                return Py.EmptyByte.bytes_join(list);
            }

        } else {
            /*
             * We don't have a peek method, so we'll be reading one byte at a time.
             */
            PyByteArray res = new PyByteArray();

            while (--remainingLimit >= 0) {

                /*
                 * read() returns a str of one byte, doing at most one read to refill, or it returns
                 * None (in vacuous non-blocking read).
                 */
                PyObject curr = readMethod.__call__(Py.One);

                if (curr.isTrue()) {
                    if (curr instanceof PyBytes) {
                        // Should be one-character string holding a byte
                        char c = ((PyBytes)curr).getString().charAt(0);
                        if (c == '\n') {
                            remainingLimit = 0; // Engineer an exit from the outer loop
                        }
                        res.append((byte)c);
                    } else {
                        String fmt = "read() should have returned a bytes object, not '%.200s'";
                        throw Py.IOError(String.format(fmt, curr.getType().fastGetName()));
                    }
                } else {
                    remainingLimit = 0; // Exit the outer loop
                }

            }

            PyBuffer buf = res.getBuffer(PyBUF.FULL_RO);
            byte[] bytes = new byte[buf.getLen()];
            buf.copyTo(bytes, 0);
            return new PyBytes(new String(bytes));
        }

    }

    // _IOBase___iter__ = _IOBase___enter__

    /**
     * May be called repeatedly to produce (usually) lines from this stream or file.
     *
     * @return next line from the stream or file
     * @throws PyException(StopIteration) when iteration has reached a natural conclusion
     * @throws PyException(ValueError) if the file or stream is closed
     * @throws PyException(IOError) reflecting an I/O error in during the read
     */
    public PyObject next() throws PyException {
        return _IOBase_next();
    }

    @ExposedMethod(doc = "x.__next__() <==> next(x)")
    public final PyObject _IOBase_next() throws PyException {
        // Implement directly. Calling __next__() fails when PyIOBaseDerived is considered.
        PyObject line = invoke("readline");
        if (!line.isTrue()) {
            throw Py.StopIteration();
        }
        return line;
    }

    /**
     * Read a stream as a sequence of lines.
     *
     * @param hint stop reading lines after this many bytes (if not EOF first)
     * @return list containing the lines read
     */
    public PyObject readlines(PyObject hint) {
        return _IOBase_readlines(hint);
    }

    @ExposedMethod(defaults = "null")
    public final PyObject _IOBase_readlines(PyObject hint) {

        int h = 0;

        if (hint == null || hint == Py.None) {
            return new PyList(this);

        } else if (!hint.isIndex()) {
            throw tailoredTypeError("integer or None", hint);

        } else if ((h = hint.asIndex()) <= 0) {
            return new PyList(this);

        } else {
            int n = 0;
            PyList lines = new PyList();

            for (PyObject line : this.asIterable()) {
                lines.append(line);
                n += line.__len__();
                if (n >= h) {
                    break;
                }
            }
            return lines;
        }
    }

    /**
     * Write an iterable sequence of strings to the stream.
     *
     * @param lines
     */
    public void writelines(PyObject lines) {
        _IOBase_writelines(lines);
    }

    @ExposedMethod
    final void _IOBase_writelines(PyObject lines) {
        _checkClosed();
        // Either an error, or a thing we can __call__()
        PyObject writeMethod = __getattr__("write");

        for (PyObject line : lines.asIterable()) {
            writeMethod.__call__(line);
        }
    }

    @ExposedMethod
    public final boolean _IOBase___lt__(PyObject other) {
        return fileno() < other.asInt();
    }

    @Override
    public void __del_builtin__() {
        closer.dismiss();
        invoke("close");
    }

    /** Convenience method providing the exception in the _checkWhatever() methods. */
    private static PyException tailoredIOError(String msg, String oper) {
        if (msg == null) {
            return Py.IOError("File or stream is not " + oper + "able.");
        } else {
            return Py.IOError(msg);
        }
    }

    /**
     * Construct a PyBuffer on a given object suitable for writing to the underlying stream. The
     * buffer returned will be navigable as a 1D contiguous sequence of bytes.
     *
     * @param obj to be wrapped and presented as a buffer
     * @return a 1D contiguous PyBuffer of bytes
     * @throws PyException (BufferError) if object has buffer API, but is not 1D contiguous bytes
     * @throws PyException (TypeError) if object not convertible to a byte array
     */
    protected static PyBuffer readablePyBuffer(PyObject obj) throws PyException {
        if (obj instanceof BufferProtocol) {
            try {
                return ((BufferProtocol)obj).getBuffer(PyBUF.SIMPLE);
            } catch (PyException pye) {
                if (pye.match(Py.BufferError)) {
                    // If we can't get a buffer on the object, say it's the wrong type
                    throw Py.TypeError(String.format("(BufferError) %s", pye.getMessage()));
                } else {
                    throw pye;
                }
            }
        } else {
            // Something else we can view as a String?
            String s;
            if (obj instanceof PyUnicode) {
                s = ((PyUnicode)obj).encode();
            } else if (obj instanceof PyArrayArray) {
                s = obj.asString();
            } else {
                // None of the above: complain
                throw tailoredTypeError("read-write buffer", obj);
            }
            return new SimpleStringBuffer(PyBUF.SIMPLE, s);
        }
    }

    /**
     * Construct a PyBuffer on a given object suitable for reading into from the underlying stream.
     * The buffer returned will be navigable as a 1D contiguous writable sequence of bytes.
     *
     * @param obj to be wrapped and presented as a buffer
     * @return a 1D contiguous PyBuffer of bytes
     * @throws PyException (BufferError) if object has buffer API, but is not 1D contiguous bytes
     * @throws PyException (TypeError) if object not convertible to a byte array
     */
    protected static PyBuffer writablePyBuffer(PyObject obj) throws PyException {
        if (obj instanceof BufferProtocol) {
            try {
                return ((BufferProtocol)obj).getBuffer(PyBUF.WRITABLE);
            } catch (PyException pye) {
                if (pye.match(Py.BufferError)) {
                    // If we can't get a buffer on the object, say it's the wrong type
                    throw Py.TypeError(String.format("(BufferError) %s", pye.getMessage()));
                } else {
                    throw pye;
                }
            }
        } else {
            // Can't be a buffer: complain
            throw tailoredTypeError("read-write buffer", obj);
        }
    }

    /**
     * Convenience method providing the exception when an argument is not the expected type. The
     * format is "<b>type</b> argument expected, got <code>type(arg)</code>."
     *
     * @param type of thing expected (or could any text)
     * @param arg argument provided from which actual type will be reported
     * @return TypeError to throw
     */
    protected static PyException tailoredTypeError(String type, PyObject arg) {
        return Py.TypeError(String.format("%s argument expected, got %.100s.", type, arg.getType()
                .fastGetName()));
    }

    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
    	//closer cannot be null
        if (closer.sys != null) {
        	int retVal = visit.visit(closer.sys, arg);
            if (retVal != 0) {
                return retVal;
            }
        }
        //__dict__ cannot be null
        return visit.visit(__dict__, arg);
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        return ob != null && (ob == closer.sys || ob == __dict__);
    }
}
