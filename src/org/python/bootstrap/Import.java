package org.python.bootstrap;

import org.python.Version;
import org.python.core.CompareOp;
import org.python.core.CompileMode;
import org.python.core.CompilerFlags;
import org.python.core.ParserFacade;
import org.python.core.Py;
import org.python.core.PyDict;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.core.PyUnicode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by isaiah on 08.06.17.
 */
public class Import {
    private static final String importlib_filename = "_bootstrap.py";
    private static final String external_filename = "_bootstrap_external.py";
    private static final String remove_frames = "_call_with_frames_removed";
    public static final String CACHEDIR = "__pycache__";

    private static final String UNKNOWN_SOURCEFILE = "<unknown>";

    public static final int NO_MTIME = -1;

    public static final int DEFAULT_LEVEL = 0;

    /**
     * Resolve the absolute module name
     * @param name
     * @param globals
     * @param level
     * @return
     */
    public static PyObject resolveName(PyUnicode name, PyObject globals, int level) {
        PySystemState interp = Py.getSystemState();
        PyObject spec, absName, pkg = null, base, parent = null;
        int lastDot;
        if (globals == null) {
            throw Py.KeyError("'__name__' not in globals");
        }
        if (!(globals instanceof PyDict)) {
            throw Py.TypeError("globals must be a dict");
        }
        pkg = globals.__getitem__("__package__");
        if (pkg == Py.None) {
            pkg = null;
        }
        spec = globals.__getitem__("__spec__");
        if (pkg != null) {
            if (spec != null && spec != Py.None) {
                parent = spec.__getattr__("parent");
            }
            boolean equal = pkg.do_richCompareBool(parent, CompareOp.EQ);
            if (!equal) {
                // TODO should be a ImportWarning
//                throw Py.ImportError("__package__ != __spec__.parent");
            }
        } else if (spec != null && spec != Py.None) {
            pkg = spec.__getattr__("parent");
        } else {
            // TODO should be a ImportWarning
//            throw Py.ImportError("can't resolve packge from __spec__ or __package__, falling back on __name__ and __path__");
            pkg = globals.__getitem__("__name__");
            if (globals.__finditem__("__path__") == null) {
                int dot = ((PyUnicode) pkg).getString().indexOf('.');
                if (dot >= 0) {
                    pkg = ((PyUnicode) pkg).getslice(0, dot);
                }
            }
        }
        lastDot = pkg.__len__();
        if (lastDot == 0) {
            throw Py.ImportError("attempted relative import with no known parent package");
        } else if (interp.modules.__finditem__(pkg) == null) {
            throw Py.SystemError(String.format("Parent module %s not loaded, cannot perform relative import", pkg));
        }

        for (int levelUp = 1; levelUp < level; levelUp++) {
            lastDot = ((PyUnicode) pkg).getString().indexOf('.');
            if (lastDot < 0) {
                throw Py.ValueError("attempted relative import beyond top-level package");
            }
        }

        base = ((PyUnicode) pkg).getslice(0, lastDot);
        if (base == null || name.__len__() == 0) {
            return base;
        }
        absName = new PyUnicode(base + "." + name.getString());
        return absName;
    }

    public static PyObject importModuleLevelObject(PyObject name, PyObject globals, PyObject fromList, int level) {
        PySystemState interp = Py.getSystemState();
        PyObject absName;
        PyObject finalMod = null;
        if (name == null) {
            throw Py.ValueError("Empty module name");
        }
        if (level > 0) {
            absName = resolveName((PyUnicode) name, globals, level);
        } else {
            if (name.__len__() == 0) {
                throw Py.ValueError("Empty module name");
            }
            absName = name;
        }
        PyObject mod = interp.modules.__finditem__(absName);
        if (mod != null) {
            PyObject value = null;
            PyObject spec = mod.__getattr__("__spec__");
            if (spec != null) {
                value = spec.__findattr__("initializing");
            }
            if (value != null) {
                boolean initializing = value.__bool__();
                if (initializing) {
                    aquireLock(interp);
                    value = interp.importlib.invoke("_lock_unlock_module", absName);
                    // if (value == null) goto error;
                }
            }
        } else {
            aquireLock(interp);
            mod = interp.importlib.invoke("_find_and_load", absName, interp.importFunc);
            if (mod == null) {
                return finalMod;
            }
        }
        boolean hasFrom = false;
        if (fromList != null && fromList != Py.None) {
            hasFrom = fromList.__bool__();
        }
        if (!hasFrom) {
            int len = name.__len__();
            if (level == 0 || len > 0) {
                int dot = ((PyUnicode) name).getString().indexOf('.');
                if (dot < 0) {
                    return mod;
                }
                if (level == 0) {
                    PyObject front = ((PyUnicode) name).getslice(0, dot);
                    finalMod = importModuleLevelObject(front, null, null, 0);
                } else {
                    int cutOff = len - dot;
                    int absNameLen = absName.__len__();
                    PyObject toReturn = ((PyUnicode) absName).getslice(0, absNameLen - cutOff);
                    finalMod = interp.modules.__finditem__(toReturn);
                    if (finalMod == null) {
                        throw Py.KeyError(String.format("%s not in sys.modules as expected", toReturn));
                    }
                    return finalMod;
                }
            } else {
                return mod;
            }
        } else {
            finalMod = interp.importlib.invoke("_handle_fromlist", mod, fromList, interp.importFunc);
        }
        return finalMod;
    }

    public static void aquireLock(PySystemState interp) {
        ReentrantLock importLock = interp.getImportLock();
        importLock.lock();
    }

    /**
     * Code copied from imp.java
     */
    public static byte[] compileSource(String name, InputStream fp, String filename) {
        return compileSource(name, fp, filename, NO_MTIME);
    }

    public static byte[] compileSource(String name, InputStream fp, String filename, long mtime) {
        ByteArrayOutputStream ofp = new ByteArrayOutputStream();
        ParserFacade.ExpectedEncodingBufferedReader bufReader = null;
        try {
            if (filename == null) {
                filename = UNKNOWN_SOURCEFILE;
            }
            org.python.antlr.base.mod node;
            CompilerFlags cflags = new CompilerFlags();
            bufReader = ParserFacade.prepBufReader(fp, cflags, filename, false);
            node = ParserFacade.parseOnly(bufReader, CompileMode.exec, filename, cflags);
            org.python.compiler.Module.compile(node, ofp, name + Version.PY_CACHE_TAG, filename, true, null, mtime);
            return ofp.toByteArray();
        } catch (Throwable t) {
            throw ParserFacade.fixParseError(t, filename);
        } finally {
            try {
                bufReader.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

}
