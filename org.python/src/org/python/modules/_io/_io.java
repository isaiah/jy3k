/* Copyright (c)2012 Jython Developers */
package org.python.modules._io;

import org.python.annotations.ExposedConst;
import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;
import org.python.core.ArgParser;
import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.io.ChannelFD;
import org.python.io.util.FilenoUtil;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The Python _io module implemented in Java.
 */
@ExposedModule(doc = BuiltinDocs.io_doc)
public class _io {

    /**
     * This method is called when the module is loaded, to populate the namespace (dictionary) of
     * the module. The dictionary has been initialised at this point reflectively from the methods
     * of this class and this method nulls those entries that ought not to be exposed.
     *
     * @param dict namespace of the module
     */
    @ModuleInit
    public static void init(PyObject dict) {
        dict.__setitem__("_IOBase", PyIOBase.TYPE);
        dict.__setitem__("_RawIOBase", PyRawIOBase.TYPE);
        dict.__setitem__("_BufferedIOBase", PyBufferedIOBase.TYPE);
        dict.__setitem__("BufferedRWPair", PyBufferedRWPair.TYPE);
        dict.__setitem__("BufferedReader", PyBufferedReader.TYPE);
        dict.__setitem__("BufferedWriter", PyBufferedWriter.TYPE);
        dict.__setitem__("BufferedRandom", PyBufferedRandom.TYPE);
        dict.__setitem__("_TextIOBase", PyTextIOBase.TYPE);
        dict.__setitem__("TextIOWrapper", PyTextIOWrapper.TYPE);
        dict.__setitem__("FileIO", PyFileIO.TYPE);
        dict.__setitem__("BytesIO", PyBytesIO.TYPE);
        dict.__setitem__("StringIO", PyStringIO.TYPE);
        dict.__setitem__("IncrementalNewlineDecoder", PyIncrementalNewlineDecoder.TYPE);

        // Define UnsupportedOperation exception by constructing the type
        PyObject exceptions = Py.getSystemState().builtins;
        PyObject ValueError = exceptions.__finditem__("ValueError");
        PyObject OSError = exceptions.__finditem__("OSError");
        // Equivalent to class UnsupportedOperation(ValueError, IOError) : pass
        // UnsupportedOperation = makeException(dict, "UnsupportedOperation", ValueError, IOError);
        // XXX Work-around: slots not properly initialised unless IOError comes first
        UnsupportedOperation = makeException(dict, "UnsupportedOperation", OSError, ValueError);

        PyObject BlockingIOError = exceptions.__finditem__("BlockingIOError");
        dict.__setitem__("BlockingIOError", BlockingIOError);
    }

    /** A Python class for the <code>UnsupportedOperation</code> exception. */
    public static PyType UnsupportedOperation;

    /**
     * A function that returns a {@link PyException}, which is a Java exception suitable for
     * throwing, and that will be raised as an <code>UnsupportedOperation</code> Python exception.
     *
     * @param message text message parameter to the Python exception
     * @return nascent <code>UnsupportedOperation</code> Python exception
     */
    public static PyException UnsupportedOperation(String message) {
        return new PyException(UnsupportedOperation, message);
    }

    /**
     * Convenience method for constructing a type object of a Python exception, named as given, and
     * added to the namespace of the "_io" module.
     *
     * @param dict module dictionary
     * @param excname name of the exception
     * @param bases one or more bases (superclasses)
     * @return the constructed exception type
     */
    private static PyType makeException(PyObject dict, String excname, PyObject... bases) {
        PyStringMap classDict = new PyStringMap();
        classDict.__setitem__("__module__", Py.newString("_io"));
        PyType type = (PyType)Py.makeClass(excname, classDict, bases);
        dict.__setitem__(excname, type);
        return type;
    }

    /** Default buffer size for export. */
    @ExposedConst
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * Open file and return a stream. Raise IOError upon failure. This is a port to Java of the
     * CPython _io.open (Modules/_io/_iomodule.c) following the same logic, but expressed with the
     * benefits of Java syntax.
     *
     * @param args array of arguments from Python call via Jython framework
     * @param kwds array of keywords from Python call via Jython framework
     * @return the stream object
     */
    @ExposedFunction
    public static PyObject open(PyObject[] args, String[] kwds) {

        // Get the arguments to variables
        ArgParser ap = new ArgParser("open", args, kwds, openKwds, 1);
        PyObject file = ap.getPyObject(0);
        String m = ap.getString(1, "r");
        int buffering = ap.getInt(2, -1);
        final String encoding = ap.getString(3, null);
        final String errors = ap.getString(4, null);
        final String newline = ap.getString(5, null);
        boolean closefd = ap.getBoolean(6, true);
        final PyObject opener = ap.getPyObject(7, Py.None);
        FilenoUtil filenoUtil = Py.getThreadState().filenoUtil();

        // Decode the mode string
        OpenMode mode = new OpenMode(m) {

            @Override
            public void validate() {
                super.validate();
                validate(encoding, errors, newline);
            }
        };

        mode.checkValid();

        /*
         * Create the Raw file stream. Let the constructor deal with the variants and argument
         * checking.
         */
        PyObject pathMethod = file.__findattr__("__path__");
        if (pathMethod != null) {
            file = pathMethod.__call__();
        }
        Path path = null;
        if (file instanceof PyUnicode) {
            try {
                path = Paths.get(file.asString());
            } catch (InvalidPathException e) {
                throw Py.ValueError(e.getMessage());
            }
        }

        boolean line_buffering = false;

        if (buffering == 0) {
            if (!mode.binary) {
                throw Py.ValueError("can't have unbuffered text I/O");
            }
            if (file instanceof PyLong) {
                return new PyFileIO(file.asInt(), mode.toOptions());
            }
            return new PyFileIO(path, mode.toOptions());

        } else if (buffering == 1) {
            // The stream is to be read line-by-line.
            line_buffering = true;
            // Force default size for actual buffer
            buffering = -1;
        }

        if (buffering < 0) {
            /*
             * We are still being asked for the default buffer size. CPython establishes the default
             * buffer size using fstat(fd), but Java appears to give no clue. A useful study of
             * buffer sizes in NIO is http://www.evanjones.ca/software/java-bytebuffers.html . This
             * leads us to the fixed choice of _DEFAULT_BUFFER_SIZE (=8KB).
             */
            buffering = DEFAULT_BUFFER_SIZE;
        }

        Channel ch;
        int fileno;
        if (file instanceof PyLong) {
            fileno = file.asInt();
            ch = filenoUtil.getWrapperFromFileno(fileno).ch;
        } else {
            try {
                ch = FileChannel.open(path, mode.toOptions());
            } catch (IOException e) {
                throw Py.IOError(e);
            }
            ChannelFD fd = filenoUtil.registerChannel(ch);
            fd.attach(path);
            fileno = fd.fileno;
        }
        // If binary, return the just the buffered file
        if (mode.binary) {
            if (mode.updating) {
                return new PyBufferedRandom(Channels.newInputStream((ReadableByteChannel) ch), Channels.newOutputStream((WritableByteChannel) ch), buffering);
            } else if (mode.writing || mode.appending || mode.creating) {
                return new PyBufferedWriter(Channels.newOutputStream((WritableByteChannel) ch), buffering);
            } else if (mode.reading) {
                return new PyBufferedReader(Channels.newInputStream((ReadableByteChannel) ch), buffering);
            }
        }
        return new PyTextIOWrapper(Channels.newInputStream((ReadableByteChannel) ch), Channels.newOutputStream((WritableByteChannel) ch), buffering, fileno);
    }

    private static final String[] openKwds = {"file", "mode", "buffering", "encoding", "errors",
            "newline", "closefd", "opener"};
}
