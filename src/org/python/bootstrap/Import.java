package org.python.bootstrap;

import org.python.Version;
import org.python.core.BytecodeLoader;
import org.python.core.CompareOp;
import org.python.core.CompileMode;
import org.python.core.CompilerFlags;
import org.python.core.ParserFacade;
import org.python.core.Py;
import org.python.core.BaseCode;
import org.python.core.PyCode;
import org.python.core.PyDict;
import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PyFrame;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyModule;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.core.PySystemState;
import org.python.core.PyTableCode;
import org.python.core.PyTraceback;
import org.python.core.PyTuple;
import org.python.core.PyUnicode;
import org.python.core.ThreadState;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Python/import.c
 */
public class Import {
    private static final String importlib_filename = "_bootstrap.py";
    private static final String external_filename = "_bootstrap_external.py";
    private static final String remove_frames = "_call_with_frames_removed";
    public static final String CACHEDIR = "__pycache__";

    private static final String UNKNOWN_SOURCEFILE = "<unknown>";

    public static final int NO_MTIME = -1;
    public static final int API_VERSION = 37;

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

    public static PyObject importModuleLevel(String name, PyObject globals, PyObject fromList, int level) {
        return importModuleLevelObject(new PyUnicode(name), globals, fromList, level);
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
        if (mod != null && mod != Py.None) {
            PyObject value = null;
            PyObject spec = mod.__getattr__("__spec__");
            if (spec != null) {
                value = spec.__findattr__("initializing");
            }
            if (value != null) {
                boolean initializing = value.__bool__();
                if (initializing) {
                    aquireLock(interp);
                    try {
                        value = interp.importlib.invoke("_lock_unlock_module", absName);
                        // if (value == null) goto error;
                    } catch (PyException pye) {
                        removeImportlibFrames(pye);
                        throw pye;
                    }
                }
            }
        } else {
            aquireLock(interp);
            try {
                mod = interp.importlib.invoke("_find_and_load", absName, interp.importFunc);
            } catch (PyException pye) {
                removeImportlibFrames(pye);
                throw pye;
            }
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

    public static final byte[] findFrozen(String name) throws IOException {
        return Import.class.getResourceAsStream("/" + name).readAllBytes();
    }

    public static boolean importFrozenModuleObject(String name) {
        String filename = name.endsWith("_external") ? external_filename : importlib_filename;
        byte[] bytes;
        try {
            bytes = findFrozen(name);
        } catch (IOException e) {
            throw Py.ImportError("frozen module not found " + name);
        }
        PyTableCode code = BytecodeLoader.makeCode(name + Version.PY_CACHE_TAG, bytes, filename);
        PyModule module = addModule(name);

        if (!(code instanceof PyTableCode)) {
            throw Py.TypeError(String.format("expected TableCode, got %s", code.getType().fastGetName()));
        }
        try {
            Py.runCode(code, module.__dict__, module.__dict__);
        } catch (PyException e) {
            removeModule(name);
            throw e;
        }
        return true;
    }

    /**
     * Import a module, either built-in, frozen, or external, and return
     * it's module object
     */
    public static PyObject importModule(String name) {
        PyObject moduleName = new PyUnicode(name);
        PyObject globals = null, builtins, importFunc;
        ThreadState ts = Py.getThreadState();
        if (ts.frame != null) {
            globals = ts.frame.f_globals;
        }
        if (globals != null) {
            builtins = globals.__finditem__("__builtins__");
        } else {
            /** No globals -- use standard builtins and fake globals */
            builtins = importModuleLevel("builtins", null, null, 0);
            globals = new PyStringMap();
            globals.__setitem__("builtins", builtins);
        }
        /** Get the __import__ function from builtins */
        if (builtins instanceof PyDict) {
            importFunc = builtins.__getitem__("__import__");
        } else {
            importFunc = builtins.__getattr__("__import__");
        }
        PyObject r = importFunc.__call__(moduleName, globals, new PyList(), new PyLong(0));
        return r;
    }

    /**
     * If the given name is found in sys.modules, the entry from there is returned. Otherwise a new
     * PyModule is created for the name and added to sys.modules
     */
    public static PyModule addModule(String name) {
        name = name.intern();
        PyObject modules = Py.getSystemState().modules;
        PyModule module = (PyModule)modules.__finditem__(name);
        if (module != null) {
            return module;
        }
        module = new PyModule(name, null);
        PyModule builtins = (PyModule)modules.__finditem__("builtins");
        PyObject __dict__ = module.__getattr__("__dict__");
        __dict__.__setitem__("__builtins__", builtins);
        __dict__.__setitem__("__package__", Py.None);
        modules.__setitem__(name, module);
        return module;
    }

    /**
     * Remove name form sys.modules if it's there.
     *
     * @param name the module name
     */
    private static void removeModule(String name) {
        name = name.intern();
        PyObject modules = Py.getSystemState().modules;
        if (modules.__finditem__(name) != null) {
            try {
                modules.__delitem__(name);
            } catch (PyException pye) {
                // another thread may have deleted it
                if (!pye.match(Py.KeyError)) {
                    throw pye;
                }
            }
        }
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
        /**
     * Selects the parent class loader for Jython, to be used for dynamically loaded classes and
     * resources. Chooses between the current and context classloader based on the following
     * criteria:
     *
     * <ul>
     * <li>If both are the same classloader, return that classloader.
     * <li>If either is null, then the non-null one is selected.
     * <li>If both are not null, and a parent/child relationship can be determined, then the child
     * is selected.
     * <li>If both are not null and not on a parent/child relationship, then the current class
     * loader is returned (since it is likely for the context class loader to <b>not</b> see the
     * Jython classes)
     * </ul>
     *
     * @return the parent class loader for Jython or null if both the current and context
     *         classloaders are null.
     */
    public static ClassLoader getParentClassLoader() {
        ClassLoader current = Import.class.getClassLoader();
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        if (context == current) {
            return current;
        }
        if (context == null) {
            return current;
        }
        if (current == null) {
            return context;
        }
        if (isParentClassLoader(context, current)) {
            return current;
        }
        if (isParentClassLoader(current, context)) {
            return context;
        }
        return current;
    }

    private static boolean isParentClassLoader(ClassLoader suspectedParent, ClassLoader child) {
        try {
            ClassLoader parent = child.getParent();
            if (suspectedParent == parent) {
                return true;
            }
            if (parent == null || parent == child) {
                // We reached the boot class loader
                return false;
            }
            return isParentClassLoader(suspectedParent, parent);

        } catch (SecurityException e) {
            return false;
        }
    }

    /** END imp.java */

    /// The following methods can be found in Python/cevel.c
    // import_name
    public static PyObject importName(PyFrame f, String name, String[] from, int level) {
        List<PyObject> fromList;
        if (from == null) {
            fromList = Py.EmptyTuple;
        } else {
            fromList = Stream.of(from).map(fname -> new PyUnicode(fname)).collect(Collectors.toList());
        }

        PyObject importFunc = f.f_builtins.__finditem__("__import__");
        if (importFunc == null) {
            throw Py.ImportError("__import__ not found");
        }

        /** Fast path for not overloaded __import__. */
        if (importFunc == Py.getSystemState().importFunc) {
            return importModuleLevelObject(new PyUnicode(name), f.f_globals, new PyTuple(fromList), level);
        }
        return importFunc.__call__(new PyObject[] {new PyUnicode(name), f.f_globals, new PyTuple(fromList), new PyLong(level)});
    }

    // import_from
    public static PyObject importFrom(PyObject v, String name) {
        PyObject x = v.__findattr__(name);
        if (x != null) {
            return x;
        }
        PyObject pkgname = v.__getattr__("__name__");
        PyObject fullmodname = new PyUnicode(((PyUnicode) pkgname).getString() +
                "." + name);
        x = Py.getSystemState().modules.__finditem__(fullmodname);
        if (x != null) {
            return x;
        }
        throw Py.ImportError("cannot import module " + name);
    }

    // import_all_from
    public static void importAllFrom(PyFrame f, PyObject v) {
        PyObject locals = f.f_locals;
        PyObject all = v.__findattr__("__all__");
        PyObject dict;
        boolean skipLeadingUnderscore = false;
        if (all == null) {
            dict = v.__findattr__("__dict__");
            if (dict == null) {
                throw Py.ImportError("from-import-* object has no __dict__ and no __all__");
            }
            if (dict instanceof PyDictionary) {
                all = ((PyDictionary) dict).keys_as_list();
            } else {
                all = ((PyStringMap) dict).keys();
            }
            skipLeadingUnderscore = true;
        }
        for (PyObject name: all.asIterable()) {
            if (skipLeadingUnderscore && ((PyUnicode) name).getString().startsWith("_")) {
                continue;
            }
            PyObject value = v.__getattr__((PyUnicode) name);
            locals.__setitem__(name, value);
        }
    }

    /**
     * remove trackback that from '_bootstrap.py' or '_bootstrap_external.py' in case of ImportError
     * or marked with _call_with_frames_removed otherwise
     * FIXME: this works almost as good as CPython, but yet to find a way to remove the first frame,
     * as there is not pointer to pointer trick in java.
     */
    private static void removeImportlibFrames(PyException pye) {
        PyTraceback outer_link = null;
        PyTraceback base_tb = pye.traceback;
        PyTraceback tb = base_tb;
        PyTraceback prev_link = base_tb;
        boolean in_importlib = false;
        boolean always_trim = pye.match(Py.ImportError);
        while (tb != null) {
            PyTraceback next = (PyTraceback) tb.tb_next;
            PyTableCode code = tb.tb_frame.f_code;
            boolean now_in_importlib = code.co_filename.equals(importlib_filename)
                    || code.co_filename.equals(external_filename);
            if (now_in_importlib && !in_importlib) {
                outer_link = prev_link;
            }
            in_importlib = now_in_importlib;
            if (in_importlib && (always_trim || code.co_name.equals(remove_frames))) {
                outer_link.tb_next = next;
            }
            prev_link = tb;
            tb = next;
        }
    }
}
