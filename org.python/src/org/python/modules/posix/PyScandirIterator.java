package org.python.modules.posix;

import org.python.annotations.ExposedSlot;
import org.python.annotations.SlotFunc;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * Created by isaiah on 7/16/16.
 */
@ExposedType(name = "posix.ScandirIterator")
public class PyScandirIterator extends PyObject {
    private Iterator<Path> iter;
    private DirectoryStream<Path> stream;
    private boolean bytes;
    public PyScandirIterator(DirectoryStream<Path> dirs, boolean bytes) {
        this.iter = dirs.iterator();
        this.stream = dirs;
        this.bytes = bytes;
    }

    @ExposedSlot(SlotFunc.ITER_NEXT)
    public static PyObject next(PyObject scandirIter) {
        PyScandirIterator self = (PyScandirIterator) scandirIter;
        if (!self.iter.hasNext()) throw Py.StopIteration();
        return new PyDirEntry(self.iter.next(), self.bytes);
    }

    @ExposedMethod(names = {"close", "__exit__"})
    public PyObject close(PyObject[] args, String[] kwds) {
        try {
            stream.close();
        } catch (IOException e) {
            throw Py.IOError(e);
        }
        return Py.None;
    }

    @ExposedMethod
    public PyObject ScandirIterator___enter__() {
        return this;
    }
}
