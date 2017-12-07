package org.python.modules;

import org.python.core.*;
import org.python.modules._io.PyStringIO;

// XXX - add support for StringIO, not just cStringIO

public class PyIOFileFactory {

    private PyIOFileFactory() {
    }

    public static PyIOFile createIOFile(PyObject file) {
        Object f = file.__tojava__(PyStringIO.class);
        if (f != Py.NoConversion) {
            return new cStringIOFile(file);
        } else if (Py.isInstance(file, PyFile.TYPE)) {
            return new FileIOFile(file);
        } else {
            return new ObjectIOFile(file);
        }
    }

    // Use a cStringIO as a file.
    static class cStringIOFile implements PyIOFile {

        PyStringIO file;

        cStringIOFile(PyObject file) {
            this.file = (PyStringIO) file.__tojava__(Object.class);
        }

        public void write(String str) {
            file.write(str);
        }

        public void write(char ch) {
            file.writeChar(ch);
        }

        public void flush() {
        }

        public String read(int len) {
            return file.read(len);
        }

        public String readlineNoNl() {
            return file.readlineNoNl();
        }
    }


    // Use a PyFile as a file.
    static class FileIOFile implements PyIOFile, Traverseproc {

        PyFile file;

        FileIOFile(PyObject file) {
            this.file = (PyFile) file.__tojava__(PyFile.class);
            if (this.file.getClosed()) {
                throw Py.ValueError("I/O operation on closed file");
            }
        }

        public void write(String str) {
            file.write(str);
        }

        public void write(char ch) {
            file.write(String.valueOf(ch));
        }

        public void flush() {
        }

        public String read(int len) {
            return file.read(len).toString();
        }

        public String readlineNoNl() {
            String line = file.readline().toString();
            return line.substring(0, line.length() - 1);
        }


        /* Traverseproc implementation */
        @Override
        public int traverse(Visitproc visit, Object arg) {
            return file == null ? 0 : visit.visit(file, arg);
        }

        @Override
        public boolean refersDirectlyTo(PyObject ob) {
            return ob != null && ob == file;
        }
    }


    // Use any python object as a file.
    static class ObjectIOFile implements PyIOFile, Traverseproc {

        char[] charr = new char[1];
        StringBuilder buff = new StringBuilder();
        PyObject write;
        PyObject read;
        PyObject readline;
        final int BUF_SIZE = 256;

        ObjectIOFile(PyObject file) {
//          this.file = file;
            write = file.__findattr__("write");
            read = file.__findattr__("read");
            readline = file.__findattr__("readline");
        }

        public void write(String str) {
            buff.append(str);
            if (buff.length() > BUF_SIZE) {
                flush();
            }
        }

        public void write(char ch) {
            buff.append(ch);
            if (buff.length() > BUF_SIZE) {
                flush();
            }
        }

        public void flush() {
            write.__call__(new PyBytes(buff.toString()));
            buff.setLength(0);
        }

        public String read(int len) {
            return read.__call__(new PyLong(len)).toString();
        }

        public String readlineNoNl() {
            String line = readline.__call__().toString();
            return line.substring(0, line.length() - 1);
        }


        /* Traverseproc implementation */
        @Override
        public int traverse(Visitproc visit, Object arg) {
            int retVal;
            if (write != null) {
                retVal = visit.visit(write, arg);
                if (retVal != 0) {
                    return retVal;
                }
            }
            if (read != null) {
                retVal = visit.visit(read, arg);
                if (retVal != 0) {
                    return retVal;
                }
            }
            return readline == null ? 0 : visit.visit(readline, arg);
        }

        @Override
        public boolean refersDirectlyTo(PyObject ob) {
            return ob != null && (ob == write || ob == read || ob == readline);
        }
    }
}

