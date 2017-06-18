package org.python.modules.posix;

import org.python.core.Py;
import org.python.core.PyIterator;
import org.python.core.PyObject;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedType;

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
    public PyScandirIterator(DirectoryStream<Path> dirs) {
        this.iter = dirs.iterator();
        this.stream = dirs;
    }

    @Override
    @ExposedMethod(names = "__next__")
    public PyObject __next__() {
        if (!iter.hasNext()) throw Py.StopIteration();
        return new PyDirEntry(iter.next());
    }

    @Override
    @ExposedMethod(names = "__iter__")
    public PyObject __iter__() {
        return this;
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
