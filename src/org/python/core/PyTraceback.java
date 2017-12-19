// Copyright (c) Corporation for National Research Initiatives
package org.python.core;

import org.python.core.util.RelativeFile;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedType;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * A python traceback object.
 */
@ExposedType(name = "traceback", isBaseType = false)
public class PyTraceback extends PyObject implements Traverseproc {
    
    public static final PyType TYPE = PyType.fromClass(PyTraceback.class);

    @ExposedGet
    public PyObject tb_next;

    @ExposedGet
    public PyFrame tb_frame;

    @ExposedGet
    public int tb_lineno;

    public PyTraceback(PyTraceback next, PyFrame frame) {
        super(TYPE);
        tb_next = next;
        tb_frame = frame;
        tb_lineno = frame.f_lineno;
    }

    private String tracebackInfo() {
        if (tb_frame == null || tb_frame.f_code == null) {
            return String.format("  (no code object) at line %s\n", tb_lineno);
        }
        String line = null;
        if (tb_frame.f_code.co_filename != null) {
            line = getLine(tb_frame.f_code.co_filename, tb_lineno);
        }
        return String.format("  File \"%.500s\", line %d, in %.500s\n%s",
                             tb_frame.f_code.co_filename, tb_lineno, tb_frame.f_code.co_name,
                             line == null ? "" : "    " + line);
    }

    /**
     * Return the specified line of code from filename.
     *
     * @param filename a filename String
     * @param lineno the line number
     * @return a String line or null
     */
    private String getLine(String filename, int lineno) {
        RelativeFile file = new RelativeFile(filename);
        try {
            if (!file.isFile() || !file.canRead()) {
                // XXX: We should run through sys.path until the filename is found
                return null;
            }
        } catch (SecurityException e) {
            return null;  // If we don't have read access to the file, return null
        }

        BufferedReader reader;
        try {
            reader = Files.newBufferedReader(Paths.get(tb_frame.f_code.co_filename));
        } catch (IOException e) {
            throw Py.IOError(e);
        }


        String line = null;
        int i = 0;
        try {
            for (i = 0; i < tb_lineno; i++) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
            }
        } catch (IOException e) {
            // Proceed to closing the file
        }
        try {
            reader.close();
        } catch (IOException e) {
        }

        if (line != null && i == tb_lineno) {
            i = 0;
            while (i < line.length()) {
                char c = line.charAt(i);
                if (c != ' ' && c != '\t' && c != '\014') {
                    break;
                }
                i++;
            }
            line = line.substring(i);
            if (!line.endsWith("\n")) {
                line += "\n";
            }
        } else {
            line = null;
        }
        return line;
    }

    public void dumpStack(StringBuilder buf) {
        buf.append(tracebackInfo());
        if (tb_next != null) {
            if (tb_next == this) {
                buf.append("circularity detected!"+this+tb_next);
                return;
            }
            ((PyTraceback) tb_next).dumpStack(buf);
        }
    }

    public String dumpStack() {
        StringBuilder buf = new StringBuilder();
        buf.append("Traceback (most recent call last):\n");
        dumpStack(buf);
        return buf.toString();
    }


    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        if (tb_next != null) {
            int retVal = visit.visit(tb_next, arg);
            if (retVal != 0) {
                return retVal;
            }
        }
        //__dict__ cannot be null
        return tb_frame == null ? 0 : visit.visit(tb_frame, arg);
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        return ob != null && (ob == tb_next || ob == tb_frame);
    }
}
