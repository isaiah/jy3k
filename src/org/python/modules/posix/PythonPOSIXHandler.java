/* Copyright (c) Jython Developers */
package org.python.modules.posix;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jnr.constants.platform.Errno;
import jnr.posix.POSIXHandler;

import org.python.core.imp;
import org.python.core.Options;
import org.python.core.Py;
import org.python.core.PyObject;


/**
 * Jython specific hooks for our underlying POSIX library.
 */
public class PythonPOSIXHandler implements POSIXHandler {

    public void error(Errno error, String extraData) {
        throw Py.OSError(error, Py.newUnicode(extraData));
    }

    public void error(Errno error, String methodName, String extraData) {
        throw Py.OSError(error, Py.newUnicode(extraData));
    }

    public void unimplementedError(String methodName) {
        if (methodName.startsWith("stat.")) {
            // Ignore unimplemented FileStat methods
            return;
        }
        throw Py.NotImplementedError(methodName);
    }

    public void warn(WARNING_ID id, String message, Object... data) {
    }

    public boolean isVerbose() {
        return Options.verbose >= Py.DEBUG;
    }

    public File getCurrentWorkingDirectory() {
        return new File(Py.getSystemState().getCurrentWorkingDir());
    }

    public String[] getEnv() {
        Map<String, String> envs = System.getenv();
        List<String> pairs = envs.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.toList());
        return pairs.toArray(new String[0]);
    }

    public InputStream getInputStream() {
        return System.in;
    }

    public PrintStream getOutputStream() {
        return System.out;
    }

    public int getPID() {
        return 0;
    }

    public PrintStream getErrorStream() {
        return System.err;
    }
}
