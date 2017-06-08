/* Copyright (c) Jython Developers */
package org.python.modules;

import jnr.constants.Constant;
import jnr.constants.ConstantSet;
import jnr.constants.platform.Errno;
import jnr.posix.util.Platform;
import org.python.core.BuiltinDocs;
import org.python.core.ClassDictInit;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyObject;
import org.python.core.PyBytes;
import org.python.core.PyUnicode;
import org.python.core.imp;
import org.python.expose.ExposedModule;
import org.python.expose.ModuleInit;

/**
 * The Python errno module.
 *
 * Errno constants can be accessed from Java code via
 * {@link jnr.constants.platform.Errno}, e.g. Errno.ENOENT.
 */
@ExposedModule(doc = BuiltinDocs.errno_doc)
public class errno {
    /** Reverse mapping of codes to names. */
    public static final PyObject errorcode = new PyDictionary();

    @ModuleInit
    public static void init(PyObject dict) {
        if (Platform.IS_WINDOWS) {
            initWindows(dict);
        } else {
            initPosix(dict);
        }

        // XXX: necessary?
        addCode(dict, "ESOCKISBLOCKING", 20000, "Socket is in blocking mode");
        addCode(dict, "EGETADDRINFOFAILED", 20001, "getaddrinfo failed");
    }

    /**
     * Setup errnos for Windows.
     *
     * Windows replaced the BSD/POSIX socket errnos with its own Winsock equivalents
     * (e.g. EINVAL -> WSAEINVAL). We painstakenly map the missing constants to their WSA
     * equivalent values and expose the WSA constants on their own.
     */
    private static void initWindows(PyObject dict) {
        // the few POSIX errnos Windows defines
        ConstantSet winErrnos = ConstantSet.getConstantSet("Errno");
        // WSA errnos (and other Windows LastErrors)
        ConstantSet lastErrors = ConstantSet.getConstantSet("LastError");

        // Fill the gaps by searching through every possible jnr-constants Errno first
        // checking if it's defined on Windows, then falling back to the WSA prefixed
        // version if it exists
        Constant constant;
        for (Constant errno : Errno.values()) {
            String errnoName = errno.name();
            if ((constant = winErrnos.getConstant(errnoName)) != null
                || (constant = lastErrors.getConstant("WSA" + errnoName)) != null) {
                addCode(dict, errnoName, constant.intValue(), constant.toString());
            }
        }
        // Then provide the WSA names
        for (Constant lastError : lastErrors) {
            if (lastError.name().startsWith("WSA")) {
                addCode(dict, lastError.name(), lastError.intValue(), lastError.toString());
            }
        }
    }

    private static void initPosix(PyObject dict) {
        for (Constant constant : ConstantSet.getConstantSet("Errno")) {
            addCode(dict, constant.name(), constant.intValue(), constant.toString());
        }
    }

    private static void addCode(PyObject dict, String name, int code, String message) {
        PyObject nameObj = Py.newUnicode(name);
        PyObject codeObj = Py.newInteger(code);
        dict.__setitem__(nameObj, codeObj);
        errorcode.__setitem__(codeObj, nameObj);
    }

    /**
     * @deprecated Use jnr.constants.platform.Errno.valueOf(code).toString() (or
     *             os.strerror from Python) instead.
     */
    @Deprecated
    public static PyObject strerror(PyObject code) {
        Py.warning(Py.DeprecationWarning,
                   "The errno.strerror function is deprecated, use os.strerror.");
        return imp.load("os").__getattr__("strerror").__call__(code);
    }
}
