package org.python.util;

import org.python.antlr.base.mod;
import org.python.bootstrap.Import;
import org.python.core.Abstract;
import org.python.core.CompileMode;
import org.python.core.CompilerFlags;
import org.python.core.ParserFacade;
import org.python.core.Py;
import org.python.core.PyCode;
import org.python.core.PyException;
import org.python.core.PyModule;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.core.PyUnicode;
import org.python.modules.sys.SysModule;

import java.io.Closeable;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;

/**
 * The PythonInterpreter class is a standard wrapper for a Jython interpreter for embedding in a
 * Java application.
 */
public class PythonInterpreter implements AutoCloseable, Closeable {

    // Defaults if the interpreter uses thread-local state
    protected PySystemState interp;
    PyObject globals;

    protected boolean useThreadLocalState;

    protected static ThreadLocal<Object[]> threadLocals = new ThreadLocal<Object[]>() {

        @Override
        protected Object[] initialValue() {
            return new Object[1];
        }
    };

    protected CompilerFlags cflags = new CompilerFlags();

    private volatile boolean closed = false;

    /**
     * Initializes the Jython runtime. This should only be called once, before any other Python
     * objects (including PythonInterpreter) are created.
     *
     * @param preProperties A set of properties. Typically System.getProperties() is used.
     *            preProperties override properties from the registry file.
     * @param postProperties Another set of properties. Values like python.home, python.path and all
     *            other values from the registry files can be added to this property set.
     *            postProperties override system properties and registry properties.
     * @param argv Command line arguments, assigned to sys.argv.
     */
    public static void
            initialize(Properties preProperties, Properties postProperties, String[] argv) {
        PySystemState.initialize(preProperties, postProperties, argv);
    }

    /**
     * Creates a new interpreter with an empty local namespace.
     */
    public PythonInterpreter() {
        this(null, null);
    }

    /**
     * Creates a new interpreter with the ability to maintain a separate local namespace for each
     * thread (set by invoking setLocals()).
     *
     * @param dict a Python mapping object (e.g., a dictionary) for use as the default namespace
     */
    public static PythonInterpreter threadLocalStateInterpreter(PyObject dict) {
        return new PythonInterpreter(dict, null, true);
    }

    /**
     * Creates a new interpreter with a specified local namespace.
     *
     * @param dict a Python mapping object (e.g., a dictionary) for use as the namespace
     */
    public PythonInterpreter(PyObject dict) {
        this(dict, null);
    }

    public PythonInterpreter(PyObject dict, PySystemState systemState) {
        this(dict, systemState, false);
    }

    protected PythonInterpreter(PyObject dict, PySystemState systemState, boolean useThreadLocalState) {
        if (systemState == null) {
            systemState = Py.getSystemState();
        }
        this.interp = systemState;
        setSystemState();

        this.useThreadLocalState = useThreadLocalState;
        if (!useThreadLocalState) {
            PyModule module = Import.addModule("__main__");
            dict = module.__dict__;
            dict.__setitem__("__annotations__", Py.newStringMap());
            systemState.modules.__setitem__("__main__", module);
        }

        if (dict == null) {
            dict = Py.newStringMap();
        }

        PyObject loader = dict.__getitem__("__loader__");

        if (loader == null || loader == Py.None) {
            loader = Abstract._PyObject_GetAttrId(interp.importlib, "BuiltinImporter");
            if (loader == null) {
                // FIXME throw Py.FatalError
                throw new RuntimeException("Failed to retrieve BuiltinImporter");
            }
            dict.__setitem__("__loader__", loader);
        }

        globals = dict;
        Py.importSiteIfSelected();
    }

    public PySystemState getInterp() {
        return interp;
    }

    protected void setSystemState() {
        Py.setSystemState(getInterp());
    }

    /**
     * Sets a Python object to use for the standard input stream, <code>sys.stdin</code>. This
     * stream is used in a byte-oriented way, through calls to <code>read</code> and
     * <code>readline</code> on the object.
     *
     * @param inStream a Python file-like object to use as the input stream
     */
    public void setIn(PyObject inStream) {
        SysModule.setObject("stdin", inStream);
    }

    /**
     * Sets a Python object to use for the standard output stream, <code>sys.stdout</code>. This
     * stream is used in a byte-oriented way (mostly) that depends on the type of file-like object.
     * The behaviour as implemented is:
     * <table border=1>
     * <tr align=center>
     * <td></td>
     * <td colspan=3>Python type of object <code>o</code> written</td>
     * </tr>
     * <tr align=left>
     * <th></th>
     * <th><code>str/bytes</code></th>
     * <th><code>unicode</code></th>
     * <th>Any other type</th>
     * </tr>
     * <tr align=left>
     * <td>as bytes directly</td>
     * <td>call <code>str(o)</code> first</td>
     * </tr>
     * <tr align=left>
     * <th>Other {@link PyObject} <code>f</code></th>
     * <td>invoke <code>f.write(str(o))</code></td>
     * <td>invoke <code>f.write(o)</code></td>
     * <td>invoke <code>f.write(str(o))</code></td>
     * </tr>
     * </table>
     *
     * @param outStream Python file-like object to use as the output stream
     */
    public void setOut(PyObject outStream) {
        SysModule.setObject("stdout", outStream);
    }

    /**
     * Sets a Python object to use for the standard output stream, <code>sys.stderr</code>. This
     * stream is used in a byte-oriented way (mostly) that depends on the type of file-like object,
     * in the same way as {@link #setOut(PyObject)}.
     *
     * @param outStream Python file-like object to use as the error output stream
     */
    public void setErr(PyObject outStream) {
        SysModule.setObject("stderr", outStream);
    }

    /**
     * Evaluates a Python code object and returns the result.
     */
    public PyObject eval(PyObject code) {
        setSystemState();
        return Py.runCode((PyCode) code, null, getLocals());
    }

    /**
     * Executes a string of Python source in the local namespace.
     */
    public void exec(String s) {
        setSystemState();
        Py.exec(Py.compile_flags(s, "<string>", CompileMode.exec, cflags), getLocals(), null);
        Py.flushLine();
    }

    /**
     * Executes a Python code object in the local namespace.
     */
    public void exec(PyObject code) {
        setSystemState();
        Py.exec(code, getLocals(), null);
        Py.flushLine();
    }

    public void execfile(java.io.InputStream s, String filename) {
        setSystemState();
        setMainLoader(filename, "SourceFileLoader");
        Py.runCode(Py.compile_flags(s, filename, CompileMode.exec, cflags), getLocals(), null);
        Py.flushLine();
    }

    private void setMainLoader(String filename, String loaderName) {
        PyObject bootstrap = interp.importlib.__getattr__("_bootstrap_external");
        PyObject loaderType = bootstrap.__getattr__(loaderName);
        PyObject loader = loaderType.__call__(new PyUnicode("__main__"), new PyUnicode(filename));
        globals.__setitem__("__loader__", loader);
    }

    /**
     * Compiles a string of Python source as either an expression (if possible) or a module.
     *
     * Designed for use by a JSR 223 implementation: "the Scripting API does not distinguish between
     * scripts which return values and those which do not, nor do they make the corresponding
     * distinction between evaluating or executing objects." (SCR.4.2.1)
     */
    public PyCode compile(String script) {
        return compile(script, "<script>");
    }

    public PyCode compile(Reader reader) {
        return compile(reader, "<script>");
    }

    public PyCode compile(String script, String filename) {
        return compile(new StringReader(script), filename);
    }

    public PyCode compile(Reader reader, String filename) {
        mod node = ParserFacade.parseExpressionOrModule(reader, filename, cflags);
        setSystemState();
        return Py.compile_flags(node, filename, CompileMode.eval, cflags);
    }

    public PyObject getLocals() {
        if (!useThreadLocalState) {
            return globals;
        } else {
            PyObject locals = (PyObject)threadLocals.get()[0];
            if (locals != null) {
                return locals;
            }
            return globals;
        }
    }

    public void setLocals(PyObject dict) {
        if (!useThreadLocalState) {
            globals = dict;
        } else {
            threadLocals.get()[0] = dict;
        }
    }

    /**
     * Sets a variable in the local namespace.
     *
     * @param name the name of the variable
     * @param value the object to set the variable to (as converted to an appropriate Python object)
     */
    public void set(String name, Object value) {
        getLocals().__setitem__(name.intern(), Py.java2py(value));
    }

    /**
     * Sets a variable in the local namespace.
     *
     * @param name the name of the variable
     * @param value the Python object to set the variable to
     */
    public void set(String name, PyObject value) {
        getLocals().__setitem__(name.intern(), value);
    }

    /**
     * Returns the value of a variable in the local namespace.
     *
     * @param name the name of the variable
     * @return the value of the variable, or null if that name isn't assigned
     */
    public PyObject get(String name) {
        return getLocals().__finditem__(name.intern());
    }

    /**
     * Returns the value of a variable in the local namespace.
     *
     * The value will be returned as an instance of the given Java class.
     * <code>interp.get("foo", Object.class)</code> will return the most appropriate generic Java
     * object.
     *
     * @param name the name of the variable
     * @param javaclass the class of object to return
     * @return the value of the variable as the given class, or null if that name isn't assigned
     */
    public <T> T get(String name, Class<T> javaclass) {
        PyObject val = getLocals().__finditem__(name.intern());
        if (val == null) {
            return null;
        }
        return Py.tojava(val, javaclass);
    }

    public void cleanup() {
        setSystemState();
        PySystemState sys = Py.getSystemState();
        sys.callExitFunc();
        try {
            sys.getStdout().invoke("flush");
        } catch (PyException pye) {
            // fall through
        }
        try {
            sys.getStderr().invoke("flush");
        } catch (PyException pye) {
            // fall through
        }
        threadLocals.remove();
        sys.cleanup();
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            cleanup();
        }
    }
}
