package org.python.modules;

import jnr.constants.platform.Fcntl;
import jnr.posix.POSIX;
import org.python.core.ArgParser;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;
import org.python.modules.posix.PosixModule;

@ExposedModule
public class fcntl {
    private static POSIX posix = PosixModule.getPOSIX();

    @ModuleInit
    public static void init(PyObject dict) {
        for (Fcntl val : Fcntl.values()) {
            dict.__setitem__(val.name(), new PyLong(val.longValue()));
        }
    }

    @ExposedFunction
    public static int fcntl(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("fcntl", args, keywords, "fd", "cmd", "arg");
        PyObject fileDescriptor = ap.getPyObject(0);
        int cmd = ap.getInt(1);
        int fd = PosixModule.getFD(fileDescriptor).getIntFD();
        return posix.fcntl(fd, Fcntl.valueOf(cmd));
    }
}
