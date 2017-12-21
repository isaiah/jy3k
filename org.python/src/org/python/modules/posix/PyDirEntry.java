package org.python.modules.posix;

import jnr.constants.platform.Errno;
import org.python.core.ArgParser;
import org.python.core.Py;
import org.python.core.PyBytes;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Created by isaiah on 7/16/16.
 */
@ExposedType(name = "posix.DirEntry")
public class PyDirEntry extends PyObject {
    private static final PyType TYPE = PyType.fromClass(PyDirEntry.class);

    private Path entry;
    private boolean bytes;

    public PyDirEntry(Path entry, boolean bytes) {
        super(TYPE);
        this.entry = entry;
        this.bytes = bytes;
    }

    @ExposedMethod
    public PyObject is_dir(PyObject[] args, String[] keywords) {
        return Py.newBoolean(Files.isDirectory(entry, follow_symlinks("is_dir", args, keywords)));
    }

    @ExposedMethod
    public PyObject is_file(PyObject[] args, String[] keywords) {
        return Py.newBoolean(Files.isRegularFile(entry, follow_symlinks("is_file", args, keywords)));
    }

    @ExposedMethod
    public PyObject is_symlink() {
        return Py.newBoolean(Files.isSymbolicLink(entry));
    }

    @ExposedMethod
    public PyObject stat(PyObject[] args, String[] keywords) {
        return stat(follow_symlinks("stat", args, keywords));
    }

    private PyObject stat(LinkOption... linkOptions) {
        try {
            Map<String, Object> attributes = Files.readAttributes(entry, "unix:*", linkOptions);
            return PyStatResult.fromUnixFileAttributes(attributes);
        } catch (NoSuchFileException ex) {
            throw Py.OSError(Errno.ENOENT, entry.toAbsolutePath().toString());
        } catch (IOException ioe) {
            throw Py.OSError(Errno.EBADF, entry.toAbsolutePath().toString());
        } catch (SecurityException ex) {
            throw Py.OSError(Errno.EACCES, entry.toAbsolutePath().toString());
        }
    }

    @ExposedMethod
    public PyObject inode() {
        return ((PyStatResult) stat()).st_ino;
    }

    @ExposedGet(name = "path")
    public PyObject getPath() {
        return new PyUnicode(entry.toAbsolutePath().toString());
    }

    @ExposedGet(name = "name")
    public PyObject getName() {
        String name = entry.getFileName().toString();
        if (bytes) {
            return new PyBytes(name);
        }
        return new PyUnicode(name);
    }

    @Override
    public String toString() {
        return String.format("<%.500s '%s'>", getType().fastGetName(), getName());
    }

    private LinkOption[] follow_symlinks(String methodName, PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser(methodName, args, keywords, "*", "follow_symlinks");
        if (ap.getPyObject(1, Py.True).__bool__()) {
            return new LinkOption[] {LinkOption.NOFOLLOW_LINKS};
        }
        return new LinkOption[0];
    }
}
