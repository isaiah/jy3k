package org.python.modules._io;

import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.core.ArgParser;
import org.python.core.BufferProtocol;
import org.python.core.Py;
import org.python.core.PyBUF;
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
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
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
        Set<OpenOption> options = new OpenMode(mode).toOptions();
        if (file instanceof PyLong) {
            fileno = file.asInt();
            return new PyFileIO(fileno, options);
        }
        return new PyFileIO(Paths.get(file.asString()), options);
    }

    @ExposedMethod
    public boolean seekable() {
        return fileChannel != null;
    }

    @ExposedMethod
    public boolean readable() {
        return read != null;
    }

    @ExposedMethod
    public boolean writable() {
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
        return fileno;
    }

    @ExposedMethod(defaults = {"-1"})
    public PyObject read(int size) {
        if (size < 0) {
            return readall();
        }
        ByteBuffer buf = ByteBuffer.allocate(size);
        try {
            read.read(buf);
            return new PyBytes(buf);
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedMethod(defaults = {"-1"})
    public PyObject readlines(int hint) {
        throw _io.UnsupportedOperation("fileio doesn't support line decoding, use a buffered reader");
    }

    @ExposedMethod
    public int write(PyObject buf) {
        try {
            return write.write(((BufferProtocol) buf).getBuffer(PyBUF.FULL_RO).getNIOByteBuffer());
        } catch (IOException e) {
            throw Py.IOError(e);
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
        try {
            return fileChannel.position();
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedMethod(defaults = {"null"})
    public long truncate(PyObject size) {
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
    }

    @ExposedMethod
    public void flush() {
    }

    @ExposedMethod
    public boolean isatty() {
        return fileno < 3 && System.console() != null;
    }

    @ExposedMethod
    public int readinto(PyObject buf) {
        try {
            long size = fileChannel.size();
            InputStream in = Channels.newInputStream(fileChannel);
            if (size > Integer.MAX_VALUE) {
                throw Py.OverflowError("Required array size too large");
            }
            byte[] bytes = in.readAllBytes();
            ((BufferProtocol) buf).getBuffer(PyBUF.WRITABLE).copyFrom(bytes, 0, 0, bytes.length);
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
}
