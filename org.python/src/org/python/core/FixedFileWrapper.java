package org.python.core;

import org.python.modules._io.PyTextIOWrapper;

import java.io.OutputStream;

public class FixedFileWrapper extends StdoutWrapper {

    private PyObject file;

    public FixedFileWrapper(PyObject file) {
        name = "fixed file";
        this.file = file;

        if (file.getJavaProxy() != null) {
            Object tojava = file.__tojava__(OutputStream.class);
            if (tojava != null && tojava != Py.NoConversion) {
                this.file = new PyTextIOWrapper((OutputStream) tojava);
            }
        }
    }

    @Override
    protected PyObject myFile() {
        return file;
    }
}
