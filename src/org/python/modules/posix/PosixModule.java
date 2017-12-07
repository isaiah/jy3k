/* Copyright (c) Jython Developers */
package org.python.modules.posix;

import com.kenai.jffi.Library;
import com.sun.security.auth.module.UnixSystem;
import jnr.constants.Constant;
import jnr.constants.platform.Errno;
import jnr.constants.platform.Signal;
import jnr.constants.platform.Sysconf;
import jnr.posix.FileStat;
import jnr.posix.POSIX;
import jnr.posix.POSIXFactory;
import jnr.posix.Times;
import jnr.posix.util.FieldAccess;
import jnr.posix.util.Platform;
import org.python.core.ArgParser;
import org.python.core.BufferProtocol;
import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyBUF;
import org.python.core.PyBuffer;
import org.python.core.PyBytes;
import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PyFloat;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.core.PySystemState;
import org.python.core.PyTuple;
import org.python.core.PyUnicode;
import org.python.core.io.FileIO;
import org.python.core.io.IOBase;
import org.python.core.io.RawIOBase;
import org.python.core.util.StringUtil;
import org.python.annotations.ExposedConst;
import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;
import org.python.io.ChannelFD;
import org.python.io.util.FilenoUtil;
import org.python.modules._io.OpenMode;
import org.python.modules._io.PyFileIO;
import org.python.util.ChannelFD;
import org.python.util.FilenoUtil;
import org.python.util.PosixShim;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.NotLinkException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.python.core.PyBUF.FULL_RO;

/**
 * The posix/nt module, depending on the platform.
 */
@ExposedModule(name = "posix", doc = BuiltinDocs.posix_doc)
public class PosixModule {

    /** os.open flags. */
    @ExposedConst
    public static final int O_RDONLY = 0x0;
    @ExposedConst
    public static final int O_WRONLY = 0x1;
    @ExposedConst
    public static final int O_RDWR = 0x2;
    @ExposedConst
    public static final int O_APPEND = 0x8;
    @ExposedConst
    public static final int O_SYNC = 0x80;
    @ExposedConst
    public static final int O_CREAT = 0x200;
    @ExposedConst
    public static final int O_TRUNC = 0x400;
    @ExposedConst
    public static final int O_EXCL = 0x800;
    /** os.access constants. */
    @ExposedConst
    public static final int F_OK = 0;
    @ExposedConst
    public static final int X_OK = 1 << 0;
    @ExposedConst
    public static final int W_OK = 1 << 1;
    @ExposedConst
    public static final int R_OK = 1 << 2;
    /** RTLD_* constants */
    @ExposedConst
    public static final int RTLD_LAZY = Library.LAZY;
    @ExposedConst
    public static final int RTLD_NOW = Library.NOW;
    @ExposedConst
    public static final int RTLD_GLOBAL = Library.GLOBAL;
    @ExposedConst
    public static final int RTLD_LOCAL = Library.LOCAL;
    @ExposedConst
    public static final int WNOHANG = 0x00000001;

    /**
     * Current OS information.
     */
    private static final OS os = OS.getOS();
    /**
     * Platform specific POSIX services.
     */
    private static final POSIX posix = POSIXFactory.getPOSIX(new PythonPOSIXHandler(), true);

    @ModuleInit
    public static void init(PyObject dict) {
        // SecurityManager may restrict access to native implementation,
        // so use Java-only implementation as necessary
        boolean nativePosix = false;
        try {
            nativePosix = posix.isNative();
            dict.__setitem__("_native_posix", Py.newBoolean(nativePosix));
            dict.__setitem__("_posix_impl", Py.java2py(posix));
        } catch (SecurityException ex) {}

        dict.__setitem__("environ", getEnviron());
        dict.__setitem__("error", Py.OSError);
        dict.__setitem__("stat_result", PyStatResult.TYPE);

        // Hide from Python
        Hider.hideFunctions(PosixModule.class, dict, os, nativePosix);
        String[] haveFunctions = new String[]{
                "HAVE_FCHDIR", "HAVE_FCHMOD", "HAVE_FCHOWN",
                "HAVE_FEXECVE", "HAVE_FDOPENDIR", "HAVE_FPATHCONF", "HAVE_FSTATVFS", "HAVE_FTRUNCATE",
                "HAVE_LCHOWN", "HAVE_LUTIMES"
        };

        List<PyObject> haveFuncs = new ArrayList<PyObject>();
        for (String haveFunc : haveFunctions) {
            haveFuncs.add(PyUnicode.fromInterned(haveFunc));
        }
        dict.__setitem__("_have_functions", PyList.fromList(haveFuncs));

        PyList keys;
        if (dict instanceof PyStringMap) {
            keys = (PyList) ((PyStringMap) dict).keys();
        } else {
            keys = (PyList) dict.invoke("keys");
        }
        dict.__setitem__("__all__", keys);
    }

    @ExposedFunction(doc = BuiltinDocs.posix__exit_doc, defaults = {"0"})
    public static void _exit(int status) {
        System.exit(status);
    }

    @ExposedFunction(doc = BuiltinDocs.posix_access_doc)
    public static boolean access(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("access", args, keywords, "path", "mode", "*",
                "dir_fd", "effective_ids", "follow_symlinks");
        PyObject path = ap.getPyObject(0);
        int mode = ap.getInt(1);
        File file = absolutePath(path).toFile();
        boolean result = true;

        if (!file.exists()) {
            result = false;
        }
        if ((mode & R_OK) != 0 && !file.canRead()) {
            result = false;
        }
        if ((mode & W_OK) != 0 && !file.canWrite()) {
            result = false;
        }
        if ((mode & X_OK) != 0 && !file.canExecute()) {
            // Previously Jython used JNR Posix, but this is unnecessary -
            // File#canExecute uses the same code path
            // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6379654
            result = false;
        }
        return result;
    }

    @ExposedFunction(doc = BuiltinDocs.posix_chdir_doc)
    public static void chdir(PyObject path) {
        PySystemState sys = Py.getSystemState();
        Path absolutePath = absolutePath(path);
        // stat raises ENOENT for us if path doesn't exist
        if (!basicstat(path, absolutePath).isDirectory()) {
            throw Py.OSError(Errno.ENOTDIR, path);
        }
        if (os == OS.NT) {
            // No symbolic links and preserve dos-like names (e.g. PROGRA~1)
            sys.setCurrentWorkingDir(absolutePath.toString());
        } else {
            // Resolve symbolic links
            try {
                sys.setCurrentWorkingDir(absolutePath.toRealPath().toString());
            } catch (IOException ioe) {
                throw Py.OSError(ioe);
            }
        }
    }

    @ExposedFunction(doc = BuiltinDocs.posix_chmod_doc)
    public static void chmod(PyObject path, int mode) {
        if (os == OS.NT) {
            try {
                // We can only allow/deny write access (not read & execute)
                boolean writable = (mode & FileStat.S_IWUSR) != 0;
                File f = absolutePath(path).toFile();
                if (!f.exists()) {
                    throw Py.OSError(Errno.ENOENT, path);
                } else if (!f.setWritable(writable)) {
                    throw Py.OSError(Errno.EPERM, path);
                }
            } catch (SecurityException ex) {
                throw Py.OSError(Errno.EACCES, path);
            }

        } else if (posix.chmod(absolutePath(path).toString(), mode) < 0) {
            throw errorFromErrno(path);
        }
    }

    @Hide(OS.NT)
    @ExposedFunction(doc = BuiltinDocs.posix_chown_doc)
    public static void chown(PyObject path, int uid, int gid) {
        if (posix.chown(absolutePath(path).toString(), uid, gid) < 0) {
            throw errorFromErrno(path);
        }
    }

    @ExposedFunction(doc = BuiltinDocs.posix_close_doc)
    public static void close(PyObject fd) {
        if (fd instanceof PyFileIO) {
            ((PyFileIO) fd).close();
            return;
        }
        Object obj = fd.__tojava__(RawIOBase.class);
        if (obj != Py.NoConversion) {
            ((RawIOBase)obj).close();
        } else {
            posix.close(getFD(fd).getIntFD());
        }
    }

    @ExposedFunction(doc = BuiltinDocs.posix_closerange_doc)
    public static void closerange(PyObject fd_lowObj, PyObject fd_highObj) {
        int fd_low = getFD(fd_lowObj).getIntFD(false);
        int fd_high = getFD(fd_highObj).getIntFD(false);
        for (int i = fd_low; i < fd_high; i++) {
            try {
                posix.close(i);
            } catch (Exception e) {
            }
        }
    }

    @Hide(OS.NT)
    @ExposedFunction(doc = BuiltinDocs.posix_fdatasync_doc)
    public static void fdatasync(PyObject fd) {
        Object javaobj = fd.__tojava__(RawIOBase.class);
        if (javaobj != Py.NoConversion) {
            fsync((RawIOBase) javaobj, false);
        } else {
            posix.fdatasync(getFD(fd).getIntFD());
        }
    }

    @ExposedFunction(doc = BuiltinDocs.posix_fsync_doc)
    public static void fsync(PyObject fd) {
        Object javaobj = fd.__tojava__(RawIOBase.class);
        if (javaobj != Py.NoConversion) {
            fsync((RawIOBase) javaobj, true);
        } else {
            posix.fsync(getFD(fd).getIntFD());
        }
    }

    /**
     * Internal fsync implementation.
     */
    private static void fsync(RawIOBase rawIO, boolean metadata) {
        rawIO.checkClosed();
        Channel channel = rawIO.getChannel();
        if (!(channel instanceof FileChannel)) {
            throw Py.OSError(Errno.EINVAL);
        }

        try {
            ((FileChannel)channel).force(metadata);
        } catch (ClosedChannelException cce) {
            // In the rare case it's closed but the rawIO wasn't
            throw Py.ValueError("I/O operation on closed file");
        } catch (IOException ioe) {
            throw Py.OSError(ioe);
        }
    }

    @ExposedFunction(doc = BuiltinDocs.posix_ftruncate_doc)
    public static void ftruncate(PyObject fd, long length) {
        Object javaobj = fd.__tojava__(RawIOBase.class);
        if (javaobj != Py.NoConversion) {
            try {
                ((RawIOBase) javaobj).truncate(length);
            } catch (PyException pye) {
                throw Py.OSError(Errno.EBADF);
            }
        } else {
            posix.ftruncate(getFD(fd).getIntFD(), length);
        }
    }

    @ExposedFunction(doc = BuiltinDocs.posix_getcwd_doc)
    public static PyObject getcwd() {
        return Py.newUnicode(Py.getSystemState().getCurrentWorkingDir());
    }

    @ExposedFunction(doc = BuiltinDocs.posix_getcwdb_doc)
    public static PyObject getcwdb() {
        return new PyBytes(Py.getSystemState().getCurrentWorkingDir());
    }

    @Hide(OS.NT)
    @ExposedFunction(doc = BuiltinDocs.posix_getegid_doc)
    public static int getegid() {
        return posix.getegid();
    }

    @Hide(OS.NT)
    @ExposedFunction(doc = BuiltinDocs.posix_geteuid_doc)
    public static int geteuid() {
        return posix.geteuid();
    }

    @ExposedFunction(doc = BuiltinDocs.posix_getgid_doc)
    @Hide(value=OS.NT, posixImpl = PosixImpl.JAVA)
    public static int getgid() {
        return posix.getgid();
    }

    @ExposedFunction(doc = BuiltinDocs.posix_getgroups_doc)
    @Hide(value=OS.NT, posixImpl = PosixImpl.JAVA)
    public static PyObject getgroups() {
        long[] groups = new UnixSystem().getGroups();
        PyObject[] list = new PyObject[groups.length];
        for (int i = 0; i < groups.length; i++) {
            list[i] = new PyLong(groups[i]);
        }
        return new PyList(list);
    }

    @Hide(value=OS.NT, posixImpl = PosixImpl.JAVA)
    @ExposedFunction(doc = BuiltinDocs.posix_getlogin_doc)
    public static PyObject getlogin() {
        return new PyBytes(posix.getlogin());
    }

    @Hide(value=OS.NT, posixImpl = PosixImpl.JAVA)
    @ExposedFunction(doc = BuiltinDocs.posix_getppid_doc)
    public static int getppid() {
        return posix.getppid();
    }

    @Hide(value=OS.NT, posixImpl = PosixImpl.JAVA)
    @ExposedFunction(doc = BuiltinDocs.posix_getuid_doc)
    public static int getuid() {
        return posix.getuid();
    }

    @Hide(posixImpl = PosixImpl.JAVA)
    @ExposedFunction(doc = BuiltinDocs.posix_getpid_doc)
    public static int getpid() {
        return posix.getpid();
    }

    @Hide(value=OS.NT, posixImpl = PosixImpl.JAVA)
    @ExposedFunction(doc = BuiltinDocs.posix_getpgrp_doc)
    public static int getpgrp() {
        return posix.getpgrp();
    }

    @Hide(posixImpl = PosixImpl.JAVA)
    @ExposedFunction(doc = BuiltinDocs.posix_isatty_doc)
    public static boolean isatty(PyObject fdObj) {
        Object tojava = fdObj.__tojava__(IOBase.class);
        if (tojava != Py.NoConversion) {
            try {
                return ((IOBase) tojava).isatty();
            } catch (PyException pye) {
                if (pye.match(Py.ValueError)) {
                    return false;
                }
                throw pye;
            }
        }

        FDUnion fd = getFD(fdObj);
        if (fd.javaFD != null) {
            return posix.isatty(fd.javaFD);
        }
        try {
            fd.getIntFD();  // evaluate for side effect of checking EBADF or raising TypeError
        } catch (PyException pye) {
            if (pye.match(Py.OSError)) {
                return false;
            }
            throw pye;
        }
        return false;
    }

    @Hide(value=OS.NT, posixImpl = PosixImpl.JAVA)
    @ExposedFunction(doc = BuiltinDocs.posix_kill_doc)
    public static void kill(PyObject pidObj, int sig) {
        Object ret = pidObj.__tojava__(Process.class);
        if (ret == Py.NoConversion) {
            int pid = pidObj.asInt();
            if (posix.kill(pid, sig) < 0) {
                throw errorFromErrno();
            }
        } else {
            ((Process) ret).destroy();
        }
    }

    @Hide(value=OS.NT, posixImpl = PosixImpl.JAVA)
    @ExposedFunction
    public static void lchmod(PyObject path, int mode) {
        if (posix.lchmod(absolutePath(path).toString(), mode) < 0) {
            throw errorFromErrno(path);
        }
    }

    @ExposedFunction(doc = BuiltinDocs.posix_lchown_doc)
    @Hide(value=OS.NT, posixImpl = PosixImpl.JAVA)
    public static void lchown(PyObject path, int uid, int gid) {
        if (posix.lchown(absolutePath(path).toString(), uid, gid) < 0) {
            throw errorFromErrno(path);
        }
    }

    @Hide(OS.NT)
    @ExposedFunction(doc = BuiltinDocs.posix_link_doc)
    public static void link(PyObject src, PyObject dst) {
        try {
            Files.createLink(Paths.get(asPath(dst)), Paths.get(asPath(src)));
        } catch (FileAlreadyExistsException ex) {
            throw Py.OSError(Errno.EEXIST);
        } catch (NoSuchFileException ex) {
            throw Py.OSError(Errno.ENOENT);
        } catch (IOException ioe) {
            System.err.println("Got this exception " + ioe);
            throw Py.OSError(ioe);
        } catch (SecurityException ex) {
            throw Py.OSError(Errno.EACCES);
        }
    }

    @ExposedFunction(doc = BuiltinDocs.posix_listdir_doc)
    public static PyList listdir(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("listdir", args, keywords, "path");
        String path = ap.getString(0, ".");
        File file = absoluteFile(path);
        String[] names = file.list();

        if (names == null) {
            if (!file.exists()) {
                throw Py.OSError(Errno.ENOENT, path);
            }
            if (!file.isDirectory()) {
                throw Py.OSError(Errno.ENOTDIR, path);
            }
            if (!file.canRead()) {
                throw Py.OSError(Errno.EACCES, path);
            }
            throw Py.OSError("listdir(): an unknown error occurred: " + path);
        }

        PyList list = new PyList();
        if (args.length > 0 && args[0] instanceof PyBytes) {
            for (String name : names) {
                list.append(new PyBytes(name));
            }
        } else {
            for (String name : names) {
                list.append(new PyUnicode(name));
            }
        }
        return list;
    }

    @ExposedFunction(doc = BuiltinDocs.posix_scandir_doc)
    public static PyObject scandir(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("scandir", args, keywords, "path");
        PyObject pathObj;
        String path;
        boolean bytes = false;
        if (args.length > 0) {
            pathObj = args[0];
            if (pathObj instanceof PyBytes) {
                path = ((PyBytes) pathObj).getString();
                bytes = true;
            } else {
                path = ((PyUnicode) pathObj).getString();
            }
        } else {
            path = System.getProperty("user.home");
        }
        Path p = absolutePath(path);
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(p);
            return new PyScandirIterator(stream, bytes);
        } catch (NotDirectoryException e) {
            throw Py.OSError(Errno.ENOENT, path);
        } catch (IOException e) {
            throw Py.OSError(Errno.ENOTDIR, path);
        } catch (SecurityException e) {
            throw Py.OSError(Errno.EACCES, path);
        }
    }

    @ExposedFunction(doc = BuiltinDocs.posix_lseek_doc)
    public static long lseek(PyObject fd, long pos, int how) {
        Object javaobj = fd.__tojava__(RawIOBase.class);
        if (javaobj != Py.NoConversion) {
            try {
                return ((RawIOBase) javaobj).seek(pos, how);
            } catch (PyException pye) {
                throw badFD();
            }
        } else {
            return posix.lseek(getFD(fd).getIntFD(), pos, how);
        }
    }

    @ExposedFunction(doc = BuiltinDocs.posix_mkfifo_doc)
    public static void mkfifo(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("mkfifo", args, keywords, "path", "mode", "*", "dir_fd");
        PyObject dir_fd = ap.getPyObject(3, Py.None);
        if (dir_fd != Py.None) {
            throw Py.NotImplementedError("dir_fd is not supported");
        }
        String path = ap.getString(0);
        int mode = ap.getInt(1, 438);
        posix.mkfifo(path, mode);
    }

    @ExposedFunction(doc = BuiltinDocs.posix_mknod_doc)
    public static void mknod(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("mknod", args, keywords, "path", "mode", "device", "*", "dir_fd");
        PyObject dir_fd = ap.getPyObject(4, Py.None);
        if (dir_fd != Py.None) {
            throw Py.NotImplementedError("dir_fd is not supported");
        }
    }

    @ExposedFunction(doc = BuiltinDocs.posix_mkdir_doc, defaults = {"0777"})
    public static void mkdir(PyObject path, int mode) {
//        if (os == OS.NT) {
            try {
                Path nioPath = absolutePath(path);
                // Windows does not use any mode attributes in creating a directory;
                // see the corresponding function in posixmodule.c, posix_mkdir;
                Files.createDirectory(nioPath);
            } catch (FileAlreadyExistsException ex) {
                throw Py.OSError(Errno.EEXIST, path);
            } catch (IOException ioe) {
                throw Py.OSError(ioe);
            } catch (SecurityException ex) {
                throw Py.OSError(Errno.EACCES, path);
            }
        // Further work on mapping mode to PosixAttributes would have to be done
        // for non Windows platforms. In addition, posix.mkdir would still be necessary
        // for mode bits like stat.S_ISGID
//        } else if (posix.mkdir(absolutePath(path).toString(), mode) < 0) {
//            throw errorFromErrno(path);
//        }
    }

    @ExposedFunction(doc = BuiltinDocs.posix_open_doc, defaults = {"0777"})
    public static int open(PyObject path, int flag, int mode) {
        Path p;
        try {
            p = absolutePath(path);
        } catch (InvalidPathException e) {
            throw Py.ValueError(e.getMessage());
        }
        File file = p.toFile();
        boolean reading = (flag & O_RDONLY) != 0;
        boolean writing = (flag & O_WRONLY) != 0;
        boolean updating = (flag & O_RDWR) != 0;
        boolean creating = (flag & O_CREAT) != 0;
        boolean appending = (flag & O_APPEND) != 0;
        boolean truncating = (flag & O_TRUNC) != 0;
        boolean exclusive = (flag & O_EXCL) != 0;
        boolean sync = (flag & O_SYNC) != 0;

        if (updating && writing) {
            throw Py.OSError(Errno.EINVAL, path);
        }
        if (!creating && !file.exists()) {
            throw Py.OSError(Errno.ENOENT, path);
        }

        if (!writing) {
            if (updating) {
                writing = true;
            } else {
                reading = true;
            }
        }

        Set<OpenOption> openOptions = new HashSet<>();
        if (reading) {
            openOptions.add(StandardOpenOption.READ);
        }
        if (writing) {
            openOptions.add(StandardOpenOption.WRITE);
        }
        if (appending) {
            openOptions.add(StandardOpenOption.APPEND);
        }
        if (creating) {
            openOptions.add(StandardOpenOption.CREATE);
        }
        if (truncating) {
            openOptions.add(StandardOpenOption.TRUNCATE_EXISTING);
        }
        FileChannel ch;
        try {
            ch = FileChannel.open(p, openOptions);
        } catch (IOException e) {
            throw Py.IOError(e);
        }
        FilenoUtil filenoUtil = Py.getThreadState().filenoUtil();
        ChannelFD fd = new ChannelFD(ch, filenoUtil, p);
        filenoUtil.registerWrapper(fd);
        return fd.fileno;
    }

    // XXX handle IOException
    @ExposedFunction(doc = BuiltinDocs.posix_pipe_doc)
    public static PyObject pipe() throws IOException {
        // This is ideal solution, but we need a wrapper in java to read and write into,
        // or else when this file descriptor is passed back to java, we cannot handle it
//        int[] fds = new int[2];
//        int rc = posix.pipe(fds); // XXX check rc
//        return new PyTuple(new PyLong(fds[0]), new PyLong(fds[1]));
        final Pipe pipe = Pipe.open();
        final ReadableByteChannel readChan = pipe.source();
        RawIOBase read = new RawIOBase() {
            @Override
            public Channel getChannel() {
                return readChan;
            }

            @Override
            public boolean readable() {
                return true;
            }

            @Override
            public long seek(long pos, int whence) {
                return -1;
            }

            @Override
            public int readinto(ByteBuffer buf) {
                try {
                    return readChan.read(buf);
                } catch (IOException e) {
                    return -1;
                }
            }
        };
        RawIOBase write = new RawIOBase() {
            @Override
            public Channel getChannel() {
                return pipe.sink();
            }

            @Override
            public boolean writable() {
                return true;
            }
        };
        return new PyTuple(new PyFileIO(read, OpenMode.R_ONLY), new PyFileIO(write, OpenMode.W_ONLY));
    }

    @ExposedFunction(doc = BuiltinDocs.posix_putenv_doc)
    public static void putenv(String key, String value) {
        posix.setenv(key, value, 1);
    }

    @ExposedFunction(doc = BuiltinDocs.posix_read_doc)
    public static PyObject read(PyObject fd, int buffersize) {
        if (fd instanceof PyFileIO) {
            RawIOBase readable = ((PyFileIO) fd).getRawIO();
            return new PyBytes(readable.read(buffersize));
        } else {
            Object javaobj = fd.__tojava__(RawIOBase.class);
            if (javaobj != Py.NoConversion) {
                try {
                    return new PyBytes(((RawIOBase) javaobj).read(buffersize));
                } catch (PyException pye) {
                    throw badFD();
                }
            } else {
                // FIXME: this is broken
                ByteBuffer buffer = ByteBuffer.allocate(buffersize);
                posix.read(getFD(fd).getIntFD(), buffer, buffersize);
                return new PyBytes(buffer);
            }
        }
    }

    @Hide(OS.NT)
    @ExposedFunction(doc = BuiltinDocs.posix_readlink_doc)
    public static PyUnicode readlink(PyObject path) {
        try {
            return Py.newUnicode(Files.readSymbolicLink(absolutePath(path)).toString());
        } catch (NotLinkException ex) {
            throw Py.OSError(Errno.EINVAL, path);
        } catch (NoSuchFileException ex) {
            throw Py.OSError(Errno.ENOENT, path);
        } catch (IOException ioe) {
            throw Py.OSError(ioe);
        } catch (SecurityException ex) {
            throw Py.OSError(Errno.EACCES, path);
        }
    }

    @ExposedFunction(doc = BuiltinDocs.posix_remove_doc)
    public static void remove(PyObject path) {
        unlink(path);
    }

    @ExposedFunction(doc = BuiltinDocs.posix_rename_doc)
    public static void rename(PyObject oldpath, PyObject newpath) {
        if (!(absolutePath(oldpath).toFile().renameTo(absolutePath(newpath).toFile()))) {
            PyObject args = new PyTuple(Py.Zero, new PyBytes("Couldn't rename file"));
            throw new PyException(Py.OSError, args);
        }
    }

    @ExposedFunction(doc = BuiltinDocs.posix_rmdir_doc)
    public static void rmdir(PyObject path) {
        File file = absolutePath(path).toFile();
        if (!file.exists()) {
            throw Py.OSError(Errno.ENOENT, path);
        } else if (!file.isDirectory()) {
            throw Py.OSError(Errno.ENOTDIR, path);
        } else if (!file.delete()) {
            PyObject args = new PyTuple(Py.Zero, new PyBytes("Couldn't delete directory"),
                                        path);
            throw new PyException(Py.OSError, args);
        }
    }

    @ExposedFunction(doc = BuiltinDocs.posix_setpgrp_doc)
    @Hide(value=OS.NT, posixImpl = PosixImpl.JAVA)
    public static void setpgrp() {
        if (posix.setpgrp(0, 0) < 0) {
            throw errorFromErrno();
        }
    }

    @ExposedFunction(doc = BuiltinDocs.posix_setsid_doc)
    @Hide(value=OS.NT, posixImpl = PosixImpl.JAVA)
    public static void setsid() {
        if (posix.setsid() < 0) {
            throw errorFromErrno();
        }
    }

    @ExposedFunction(doc = BuiltinDocs.posix_strerror_doc)
    public static PyObject strerror(int code) {
        Constant errno = Errno.valueOf(code);
        if (errno == Errno.__UNKNOWN_CONSTANT__) {
            return new PyBytes("Unknown error: " + code);
        }
        if (errno.name() == errno.toString()) {
            // Fake constant or just lacks a description, fallback to Linux's
            // XXX: have jnr-constants handle this fallback
            errno = Enum.valueOf(jnr.constants.platform.linux.Errno.class,
                                 errno.name());
        }
        return new PyBytes(errno.toString());
    }

    @Hide(OS.NT)
    @ExposedFunction(doc = BuiltinDocs.posix_symlink_doc)
    public static void symlink(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("symlink", args, keywords, "src", "dst", "target_is_directory", "*", "dir_fd");
        String src = ap.getString(0);
        String dst = ap.getString(1);
//        boolean isDirectory = ap.getPyObject(2, Py.False).__bool__();
        PyObject dir_fd = ap.getPyObject(4, Py.None);
        if (dir_fd != Py.None) {
            throw Py.NotImplementedError("dir_fd is not supported");
        }
        try {
            // Paths.get(dir) is bugged, it doesn't respect user.dir system property
            Files.createSymbolicLink(new File(dst).getCanonicalFile().toPath(), new File(src).getCanonicalFile().toPath());
        } catch (FileAlreadyExistsException ex) {
            throw Py.OSError(Errno.EEXIST);
        } catch (IOException ioe) {
            throw Py.OSError(ioe);
        } catch (SecurityException ex) {
            throw Py.OSError(Errno.EACCES);
        }
    }

    @ExposedFunction(doc = BuiltinDocs.posix_replace_doc)
    public static PyObject replace(PyObject src, PyObject dest) {
        File destFile = absolutePath(dest).toFile();
        if (destFile.exists()) {
            destFile.delete();
        }
        absolutePath(src).toFile().renameTo(destFile);
        return Py.None;
    }

    private static PyFloat ratio(long num, long div) {
        return Py.newFloat(((double)num)/((double)div));
    }

    @ExposedFunction(doc = BuiltinDocs.posix_times_doc)
    @Hide(posixImpl = PosixImpl.JAVA)
    public static PyTuple times() {
        Times times = posix.times();
        long CLK_TCK = Sysconf._SC_CLK_TCK.longValue();
        return new PyTuple(
                ratio(times.utime(), CLK_TCK),
                ratio(times.stime(), CLK_TCK),
                ratio(times.cutime(), CLK_TCK),
                ratio(times.cstime(), CLK_TCK),
                ratio(ManagementFactory.getRuntimeMXBean().getUptime(), 1000)
        );
    }

    @Hide(posixImpl = PosixImpl.JAVA)
    @ExposedFunction(doc = BuiltinDocs.posix_umask_doc)
    public static int umask(int mask) {
        return posix.umask(mask);
    }

    @ExposedFunction(doc = BuiltinDocs.posix_unlink_doc)
    public static void unlink(PyObject path) {
        Path nioPath = absolutePath(path);
        try {
            if (Files.isDirectory(nioPath, LinkOption.NOFOLLOW_LINKS)) {
                throw Py.OSError(Errno.EISDIR, path);
            } else if (!Files.deleteIfExists(nioPath)) {
                // Something went wrong, does stat raise an error?
                basicstat(path, nioPath);
                // It exists, do we not have permissions?
                if (!Files.isWritable(nioPath)) {
                    throw Py.OSError(Errno.EACCES, path);
                }
                throw Py.OSError("unlink(): an unknown error occurred: " + nioPath.toString());
            }
        } catch (IOException ex) {
            PyException pyError = Py.OSError("unlink(): an unknown error occurred: " + nioPath.toString());
            pyError.initCause(ex);
            throw pyError;
        }
    }

    @ExposedFunction(doc = BuiltinDocs.posix_set_inheritable_doc)
    public static void set_inheritable(PyObject fd, PyObject inheritable) {
        // noop
    }

    @ExposedFunction(doc = BuiltinDocs.posix_utime_doc)
    public static void utime(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("utime", args, keywords, "path", "times", "*", "ns", "dir_fd", "follow_symlinks");
        String path = ap.getString(0);
        PyObject times = ap.getPyObject(1, Py.None);
        long[] atimeval;
        long[] mtimeval;

        if (times == Py.None) {
            atimeval = mtimeval = null;
        } else if (times instanceof PyTuple && times.__len__() == 2) {
            atimeval = extractTimeval(times.__getitem__(0));
            mtimeval = extractTimeval(times.__getitem__(1));
        } else {
            throw Py.TypeError("utime() arg 2 must be a tuple (atime, mtime)");
        }
        if (posix.utimes(absolutePath(path).toString(), atimeval, mtimeval) < 0) {
            throw errorFromErrno(new PyUnicode(path));
        }
    }

    /**
     * Convert seconds (with a possible fraction) from epoch to a 2 item array of seconds,
     * microseconds from epoch as longs.
     *
     * @param seconds a PyObject number
     * @return a 2 item long[]
     */
    private static long[] extractTimeval(PyObject seconds) {
        long[] timeval = new long[] {Platform.IS_32_BIT ? seconds.asInt() : seconds.asLong(), 0L};
        if (seconds instanceof PyFloat) {
            // can't exceed 1000000
            long usec = (long)((seconds.asDouble() - timeval[0]) * 1e6);
            if (usec < 0) {
                // If rounding gave us a negative number, truncate
                usec = 0;
            }
            timeval[1] = usec;
        }
        return timeval;
    }

    @Hide(value=OS.NT, posixImpl = PosixImpl.JAVA)
    @ExposedFunction(names = "wait", doc = BuiltinDocs.posix_wait_doc)
    public static PyObject wait$() {
        int[] status = new int[1];
        int pid = posix.wait(status);
        if (pid < 0) {
            throw errorFromErrno();
        }
        return new PyTuple(Py.newLong(pid), new PyLong(status[0]));
    }

    @ExposedFunction(doc = BuiltinDocs.posix_waitpid_doc)
    public static PyObject waitpid(PyObject pidObj, int options) {
        Object ret = pidObj.__tojava__(Process.class);
        if (ret == Py.NoConversion) {
            int pid = pidObj.asInt();
            int[] status = new int[1];
            pid = posix.waitpid(pid, status, options);
            if (pid < 0) {
                throw errorFromErrno();
            }
            return new PyTuple(new PyLong(pid), new PyLong(status[0]));
        }
        try {
            boolean status = ((Process) ret).waitFor(options, TimeUnit.SECONDS);
            int exitVal = status ? ((Process)ret).exitValue() : 0;
            return new PyTuple(pidObj, new PyLong(exitVal));
        } catch (InterruptedException e) {
            throw Py.ChildProcessError(e.getMessage());
        }
    }

    @ExposedFunction(doc = BuiltinDocs.posix_WIFSIGNALED_doc)
    public static boolean WIFSIGNALED(long status) {
        return PosixShim.WAIT_MACROS.WIFSIGNALED(status);
    }

    @ExposedFunction(doc = BuiltinDocs.posix_WIFEXITED_doc)
    public static boolean WIFEXITED(long status) {
        return PosixShim.WAIT_MACROS.WIFEXITED(status);
    }

    @ExposedFunction(doc = BuiltinDocs.posix_WTERMSIG_doc)
    public static int WTERMSIG(long status) {
        return PosixShim.WAIT_MACROS.WTERMSIG(status);
    }

    @ExposedFunction(doc = BuiltinDocs.posix_WEXITSTATUS_doc)
    public static int WEXITSTATUS(long status) {
        return PosixShim.WAIT_MACROS.WEXITSTATUS(status);
    }

    @ExposedFunction(doc = BuiltinDocs.posix_WIFSTOPPED_doc)
    public static boolean WIFSTOPPED(long status) {
        return PosixShim.WAIT_MACROS.WIFSTOPPED(status);
    }

    @ExposedFunction(doc = BuiltinDocs.posix_WSTOPSIG_doc)
    public static int WSTOPSIG(long status) {
        return PosixShim.WAIT_MACROS.WSTOPSIG(status);
    }

    @ExposedFunction(doc = BuiltinDocs.posix_write_doc)
    public static int write(int fileno, PyObject bytes) {
        ChannelFD fd = Py.getThreadState().filenoUtil().getWrapperFromFileno(fileno);
        try {
            return ((FileChannel) fd.ch).write(((BufferProtocol) bytes).getBuffer(FULL_RO).getNIOByteBuffer());
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedFunction(doc = BuiltinDocs.posix_unsetenv_doc)
    public static void unsetenv(String key) {
        posix.unsetenv(key);
    }

    @ExposedFunction(doc = BuiltinDocs.posix_urandom_doc)
    public static PyObject urandom(int n) {
        byte[] buf = new byte[n];
        UrandomSource.INSTANCE.nextBytes(buf);
        return new PyBytes(buf);
    }

    /**
     * Helper function for the subprocess module, returns the potential shell commands for
     * this OS.
     *
     * @return a tuple of lists of command line arguments. E.g. (['/bin/sh', '-c'])
     */
    @ExposedFunction()
    public static PyObject _get_shell_commands() {
        String[][] commands = os.getShellCommands();
        PyObject[] commandsTup = new PyObject[commands.length];
        int i = 0;
        for (String[] command : commands) {
            PyList args = new PyList();
            for (String arg : command) {
                args.append(new PyBytes(arg));
            }
            commandsTup[i++] = args;
        }
        return new PyTuple(commandsTup);
    }

    @ExposedFunction
    public static PyObject fspath(PyObject path) {
        if (path instanceof PyUnicode || path instanceof PyBytes) {
            return path;
        }
        PyObject fspath = path.__findattr__("__fspath__");
        if (fspath != null) {
            return fspath.__call__();
        }
        throw Py.TypeError(String.format("expected str, bytes or os.PathLike object, not %s", path.getType().fastGetName()));
    }

    /**
     * Initialize the environ dict from System.getenv. environ may be empty when the
     * security policy doesn't grant us access.
     */
    private static PyObject getEnviron() {
        PyObject environ = new PyDictionary();
        Map<String, String> env;
        try {
            env = System.getenv();
        } catch (SecurityException se) {
            return environ;
        }
        // don't change the type, the key and values have to be bytes
        for (Map.Entry<String, String> entry : env.entrySet()) {
            environ.__setitem__(
                    new PyBytes(entry.getKey()),
                    new PyBytes(entry.getValue()));
        }
        return environ;
    }

    /**
     * Return a path as a String from a PyObject
     *
     * @param path a PyObject, raising a TypeError if an invalid path type
     * @return a String path
     */
    private static String asPath(PyObject path) {
        if (path instanceof PyUnicode || path instanceof PyBytes) {
            return path.toString();
        }

        throw Py.TypeError(String.format("coercing to Unicode: need string, %s type found",
                                         path.getType().fastGetName()));
    }

    /**
     * Return the absolute, normalised form of path, equivalent to Python os.path.abspath(), except
     * that it is an error for pathObj to be an empty string or unacceptable in the file system.
     *
     * @param pathObj a PyObject, raising a TypeError if an invalid path type
     * @return an absolute path String
     */
    private static Path absolutePath(PyObject pathObj) {
        String pathStr = asPath(pathObj);
        return absolutePath(pathStr);
    }

    private static Path absolutePath(String pathStr) {
        return absoluteFile(pathStr).toPath();
    }

    private static File absoluteFile(String pathStr) {
        return new File(pathStr).getAbsoluteFile();
    }

    private static PyException badFD() {
        return Py.OSError(Errno.EBADF);
    }

    private static PyException errorFromErrno() {
        return Py.OSError(Errno.valueOf(posix.errno()));
    }

    private static PyException errorFromErrno(PyObject path) {
        return Py.OSError(Errno.valueOf(posix.errno()), path);
    }

    public static POSIX getPOSIX() {
        return posix;
    }

    public static String getOSName() {
        return os.getModuleName();
    }

    private static void checkTrailingSlash(String path, Map<String, Object> attributes) {
        Boolean isDirectory = (Boolean) attributes.get("isDirectory");
        if (isDirectory != null && !isDirectory.booleanValue()) {
            if (path.endsWith(File.separator) || path.endsWith("/.")) {
                throw Py.OSError(Errno.ENOTDIR, path);
            }
        }
    }

    private static BasicFileAttributes basicstat(PyObject path, Path absolutePath) {
        try {
            BasicFileAttributes attributes = Files.readAttributes(absolutePath, BasicFileAttributes.class);
            if (!attributes.isDirectory()) {
                String pathStr = path.toString();
                if (pathStr.endsWith(File.separator) || pathStr.endsWith("/")) {
                    throw Py.OSError(Errno.ENOTDIR, path);
                }
            }
            return attributes;
        } catch (NoSuchFileException ex) {
            throw Py.OSError(Errno.ENOENT, path);
        } catch (IOException ioe) {
            throw Py.OSError(Errno.EBADF, path);
        } catch (SecurityException ex) {
            throw Py.OSError(Errno.EACCES, path);
        }
    }

    /**
     * Lazily initialized singleton source for urandom.
     */
    private static class UrandomSource {
        static final SecureRandom INSTANCE = new SecureRandom();
    }

    @ExposedFunction
    public static final PyObject lstat(PyObject path) {
        Path absolutePath = absolutePath(path);
        try {
            Map<String, Object> attributes = Files.readAttributes(
                    absolutePath, "unix:*", LinkOption.NOFOLLOW_LINKS);
            return PyStatResult.fromUnixFileAttributes(attributes);
        } catch (NoSuchFileException ex) {
            throw Py.OSError(Errno.ENOENT, path);
        } catch (IOException ioe) {
            throw Py.OSError(Errno.EBADF, path);
        } catch (SecurityException ex) {
            throw Py.OSError(Errno.EACCES, path);
        }
    }

    @ExposedFunction(doc = BuiltinDocs.posix_stat_doc)
    public static PyObject stat(PyObject path) {
        if (path instanceof PyLong) {
            return fstat(path.asInt());
        }
        Path absolutePath = absolutePath(path);
        try {
            if (os == OS.NT) {
                DosFileAttributes attributes = Files.readAttributes(absolutePath, DosFileAttributes.class);
                return PyStatResult.fromDosFileAttributes(attributes);
            }
            Map<String, Object> attributes = Files.readAttributes(absolutePath, "unix:*");
            checkTrailingSlash(path.toString(), attributes);
            return PyStatResult.fromUnixFileAttributes(attributes);
        } catch (NoSuchFileException ex) {
            throw Py.OSError(Errno.ENOENT, path);
        } catch (IOException ioe) {
            throw Py.OSError(Errno.EBADF, path);
        } catch (SecurityException ex) {
            throw Py.OSError(Errno.EACCES, path);
        }
    }

    @ExposedFunction(doc = BuiltinDocs.posix_fstat_doc)
    public static final PyObject fstat(int fileno) {
        ChannelFD fd = Py.getThreadState().filenoUtil().getWrapperFromFileno(fileno);
        Path path = (Path) fd.getAttachment();
        try {
            if (os == OS.NT) {
                DosFileAttributes attributes = Files.readAttributes(path, DosFileAttributes.class);
                return PyStatResult.fromDosFileAttributes(attributes);
            }
            Map<String, Object> attributes = Files.readAttributes(path, "unix:*");
            checkTrailingSlash(path.toString(), attributes);
            return PyStatResult.fromUnixFileAttributes(attributes);
        } catch (NoSuchFileException ex) {
            throw Py.OSError(Errno.ENOENT, path.toString());
        } catch (IOException ioe) {
            throw Py.OSError(Errno.EBADF, path.toString());
        } catch (SecurityException ex) {
            throw Py.OSError(Errno.EACCES, path.toString());
        }
    }

    public static final void signal(Signal signal, PyObject handler) {
        posix.signal(signal, num -> {
            handler.__call__(new PyLong(num));
        });
    }
}
