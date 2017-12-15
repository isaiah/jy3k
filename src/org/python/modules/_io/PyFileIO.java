package org.python.modules._io;

import jnr.constants.platform.Errno;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.core.ArgParser;
import org.python.core.BufferProtocol;
import org.python.core.Py;
import org.python.core.PyBUF;
import org.python.core.PyBuffer;
import org.python.core.PyBytes;
import org.python.core.PyFloat;
import org.python.core.PyLong;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.io.ChannelFD;
import org.python.io.util.FilenoUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;

@ExposedType(name = "_io.FileIO")
public class PyFileIO extends PyIOBase {
    public static final PyType TYPE = PyType.fromClass(PyFileIO.class);
    private static final int INITIAL_CAPACITY = 1024;

    private SeekableByteChannel fileChannel;
    private ReadableByteChannel read;
    private WritableByteChannel write;
    private Path path;
    private int fileno;

    public PyFileIO(ReadableByteChannel ch) {
        read = ch;
        FilenoUtil filenoUtil = Py.getThreadState().filenoUtil();
        initFileno(filenoUtil, ch);
    }

    public PyFileIO(WritableByteChannel ch) {
        write = ch;
        FilenoUtil filenoUtil = Py.getThreadState().filenoUtil();
        initFileno(filenoUtil, ch);
    }

    public PyFileIO(int fileno, Set<OpenOption> options) {
        this.fileno = fileno;
        FilenoUtil filenoUtil = Py.getThreadState().filenoUtil();
        ChannelFD fd = filenoUtil.getWrapperFromFileno(fileno);
        if (fd == null) {
            throw Py.IOError(Errno.EBADF);
        }
        fileChannel = (SeekableByteChannel) fd.ch;
        read = fileChannel;
        write = fileChannel;
    }

    public PyFileIO(Path path, Set<OpenOption> options) {
        super(TYPE);
        ChannelFD fd;
        FilenoUtil filenoUtil = Py.getThreadState().filenoUtil();
        try {
            fileChannel = Files.newByteChannel(path, options);
            read = fileChannel;
            write = fileChannel;
        } catch (java.io.IOException e) {
            throw Py.IOError(e);
        }
        fd = initFileno(filenoUtil, fileChannel);
        fd.attach(path);
        this.path = path;
    }

    @ExposedNew
    public static PyObject _new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("__init__", args, keywords, "file", "mode", "close_fd", "opener");
        PyObject file = ap.getPyObject(0);
        String mode = ap.getString(1, "r");
        int fileno;
        if (file instanceof PyFloat) {
            throw Py.TypeError("integer argument expected, got 'float'");
        }
        OpenMode openMode = new OpenMode(mode);
        Set<OpenOption> options = openMode.toOptions();
        if (file instanceof PyLong) {
            fileno = file.asInt();
            return new PyFileIO(fileno, options);
        }
        Path path;
        try {
            path = Paths.get(file.asString());
        } catch (InvalidPathException e) {
            throw Py.ValueError(e.getMessage());
        }
        PyFileIO fileio = new PyFileIO(path, options);
        fileio.name = file;
        fileio.mode = openMode.toString();
        return fileio;
    }

    @ExposedMethod
    public boolean seekable() {
        _checkClosed();
        return fileChannel != null;
    }

    @ExposedMethod
    public boolean readable() {
        _checkClosed();
        return read != null;
    }

    @ExposedMethod
    public boolean writable() {
        _checkClosed();
        return write != null;
    }

    @ExposedMethod
    public int seek(int pos, int whence) {
        try {
            fileChannel.position(pos);
        } catch (IOException e) {
            throw Py.IOError(e);
        }
        return pos;
    }

    @ExposedMethod
    public int fileno() {
        _checkClosed();
        return fileno;
    }

    @ExposedMethod(defaults = {"-1"})
    public PyObject read(int size) {
        _checkClosed();
        if (size < 0) {
            return readall();
        }
        ByteBuffer buf = ByteBuffer.allocate(size);
        try {
            read.read(buf);
            return new PyBytes(buf);
        } catch (NonReadableChannelException e) {
            throw _io.UnsupportedOperation("read");
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedMethod(defaults = {"-1"})
    public PyObject readlines(int hint) {
        _checkClosed();
        throw _io.UnsupportedOperation("fileio doesn't support line decoding, use a buffered reader");
    }

    @ExposedMethod
    public int write(PyObject buf) {
        assertBufferProtocol(buf);
        PyBuffer buffer = ((BufferProtocol) buf).getBuffer(PyBUF.FULL_RO);
        try {
            return write.write(buffer.getNIOByteBuffer());
        } catch (IOException e) {
            throw Py.IOError(e);
        } finally {
            buffer.release();
        }
    }

    @ExposedMethod
    public void writelines(PyObject lines) {
        for (PyObject line : lines.asIterable()) {
            write(line);
        }
    }

    @ExposedMethod
    public long tell() {
        _checkClosed();
        try {
            return fileChannel.position();
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedMethod(defaults = {"null"})
    public long truncate(PyObject size) {
        _checkClosed();
        try {
            fileChannel.truncate(size.asLong());
            return fileChannel.position();
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedMethod
    public PyObject readall() {
        try {
            long size = fileChannel.size();
            InputStream in = Channels.newInputStream(fileChannel);
            if (size > Integer.MAX_VALUE)
                throw Py.OverflowError("Required array size too large");

            return new PyBytes(in.readAllBytes());
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedMethod
    public void close() {
        try {
            fileChannel.close();
        } catch (IOException e) {
            throw Py.IOError(e);
        }
        __closed = true;
    }

    @ExposedMethod
    public void flush() {
        _checkClosed();
    }

    @ExposedMethod
    public boolean isatty() {
        _checkClosed();
        return fileno < 3 && System.console() != null;
    }

    @ExposedMethod
    public int readinto(PyObject buf) {
        _checkClosed();
        try {
            long size = fileChannel.size();
            InputStream in = Channels.newInputStream(fileChannel);
            if (size > Integer.MAX_VALUE) {
                throw Py.OverflowError("Required array size too large");
            }
            PyBuffer buffer = ((BufferProtocol) buf).getBuffer(PyBUF.WRITABLE);
            byte[] bytes = in.readAllBytes();
            assertBufferProtocol(buf);

            buffer.copyFrom(bytes, 0, 0, bytes.length);
            buffer.release();
            return bytes.length;
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    private ChannelFD initFileno(FilenoUtil filenoUtil, Channel ch) {
        ChannelFD fd = filenoUtil.registerChannel(fileChannel);
        fileno = fd.fileno;
        return fd;
    }

    private void assertBufferProtocol(PyObject buf) {
        if (!(buf instanceof BufferProtocol)) {
            throw Py.TypeError(String.format("a bytes-like object is required, not %s", buf.getType().fastGetName()));
        }
    }
}
