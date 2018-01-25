// Copyright (c) Corporation for National Research Initiatives
package org.python.core;

import org.python.annotations.ExposedClassMethod;
import org.python.annotations.ExposedDelete;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSet;
import org.python.annotations.ExposedType;
import org.python.bootstrap.Import;
import org.python.core.linker.InvokeByName;
import org.python.modules.gc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * All objects known to the Jython runtime system are represented by an instance
 * of the class {@code PyObject} or one of its subclasses.
 */
@ExposedType(name = "object", doc = BuiltinDocs.object_doc)
public class PyObject implements Serializable {
    public static final PyType TYPE = PyType.fromClass(PyObject.class);
    private static final String UNORDERABLE_ERROR_MSG = "unorderable types: %s() %s %s()";
    private static final InvokeByName add = new InvokeByName("__add__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName radd = new InvokeByName("__radd__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName iadd = new InvokeByName("__iadd__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName sub = new InvokeByName("__sub__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName rsub = new InvokeByName("__rsub__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName isub = new InvokeByName("__isub__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName mul = new InvokeByName("__mul__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName rmul = new InvokeByName("__rmul__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName imul = new InvokeByName("__imul__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName matmul = new InvokeByName("__matmul__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName rmatmul = new InvokeByName("__rmatmul__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName imatmul = new InvokeByName("__imatmul__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName truediv = new InvokeByName("__truediv__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName rtruediv = new InvokeByName("__rtruediv__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName itruediv = new InvokeByName("__itruediv__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName floordiv = new InvokeByName("__floordiv__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName rfloordiv = new InvokeByName("__rfloordiv__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName ifloordiv = new InvokeByName("__ifloordiv__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName mod = new InvokeByName("__mod__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName rmod = new InvokeByName("__rmod__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName imod = new InvokeByName("__imod__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName divmod = new InvokeByName("__divmod__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName rdivmod = new InvokeByName("__rdivmod__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName pow = new InvokeByName("__pow__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName rpow = new InvokeByName("__rpow__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName ipow = new InvokeByName("__ipow__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName rshift = new InvokeByName("__rshift__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName rrshift = new InvokeByName("__rrshift__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName irshift = new InvokeByName("__irshift__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName lshift = new InvokeByName("__lshift__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName rlshift = new InvokeByName("__rlshift__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName ilshift = new InvokeByName("__ilshift__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName and = new InvokeByName("__and__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName rand = new InvokeByName("__rand__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName iand = new InvokeByName("__iand__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName or = new InvokeByName("__or__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName ror = new InvokeByName("__ror__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName ior = new InvokeByName("__ior__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName xor = new InvokeByName("__xor__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName rxor = new InvokeByName("__rxor__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName ixor = new InvokeByName("__ixor__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);

    private static final InvokeByName contains = new InvokeByName("__contains__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    private static final InvokeByName iter = new InvokeByName("__iter__", PyObject.class, PyObject.class, ThreadState.class);
    private static final InvokeByName next = new InvokeByName("__next__", PyObject.class, PyObject.class, ThreadState.class);
    public static final InvokeByName getitem = new InvokeByName("__getitem__", PyObject.class, PyObject.class, ThreadState.class, PyObject.class);
    /**
     * Primitives classes their wrapper classes.
     */
    private static final Map<Class<?>, Class<?>> primitiveMap = new HashMap<>();
    /**
     * This should have been suited at {@link org.python.modules.gc},
     * but that would cause a dependency cycle in the init-phases of
     * {@code gc.class} and {@code PyObject.class}. Now this boolean
     * mirrors the presence of the
     * {@link org.python.modules.gc#MONITOR_GLOBAL}-flag in Jython's
     * gc module.<br>
     * <br>
     * <b>Do not change manually.</b>
     */
    public static boolean gcMonitorGlobal = false;

    static {
        primitiveMap.put(Character.TYPE, Character.class);
        primitiveMap.put(Boolean.TYPE, Boolean.class);
        primitiveMap.put(Byte.TYPE, Byte.class);
        primitiveMap.put(Short.TYPE, Short.class);
        primitiveMap.put(Integer.TYPE, Integer.class);
        primitiveMap.put(Long.TYPE, Long.class);
        primitiveMap.put(Float.TYPE, Float.class);
        primitiveMap.put(Double.TYPE, Double.class);

        if (BootstrapTypesSingleton.getInstance().size() > 0) {
            Py.writeWarning("init", "Bootstrap types weren't encountered in bootstrapping: "
                    + BootstrapTypesSingleton.getInstance());
        }
    }

    /**
     * The type of this object.
     */
    protected PyType objtype;
    /**
     * {@code attributes} is a general purpose linked list of arbitrary
     * Java objects that should be kept alive by this PyObject. These
     * objects can be accessed by the methods and keys in
     * {@link org.python.core.JyAttribute}.
     * A notable attribute is the javaProxy (accessible via
     * {@code JyAttribute.getAttr(this, JyAttribute.JAVA_PROXY_ATTR)}),
     * an underlying Java instance that this object is wrapping or is a
     * subclass of. Anything attempting to use the proxy should go through
     * {@link #getJavaProxy()} which ensures that it's initialized.
     *
     * @see org.python.core.JyAttribute
     * @see org.python.core.JyAttribute#JAVA_PROXY_ATTR
     * @see #getJavaProxy()
     */
    protected Object attributes;

    public PyObject(PyType objtype) {
        this.objtype = objtype;
        if (gcMonitorGlobal)
            gc.monitorObject(this);
    }

    /**
     * The standard constructor for a <code>PyObject</code>. It will set the <code>objtype</code>
     * field to correspond to the specific subclass of <code>PyObject</code> being instantiated.
     **/
    public PyObject() {
        objtype = PyType.fromClass(getClass(), false);
        if (gcMonitorGlobal)
            gc.monitorObject(this);
    }

    /**
     * Creates the PyObject for the base type. The argument only exists to make the constructor
     * distinct.
     */
    PyObject(boolean ignored) {
        objtype = (PyType) this;
        if (gcMonitorGlobal)
            gc.monitorObject(this);
    }

    @ExposedNew
    static final PyObject object___new__(PyNewWrapper new_, boolean init, PyType subtype,
                                         PyObject[] args, String[] keywords) {
        // don't allow arguments if the default object.__init__() is about to be called
        PyObject[] where = new PyObject[1];
        subtype.lookup_where("__init__", where);
        if (where[0] == TYPE && args.length > 0) {
            throw Py.TypeError("object() takes no parameters");
        }

        if (subtype.isAbstract()) {
            // Compute ", ".join(sorted(type.__abstractmethods__)) into methods
            PyObject sorted = Py.getSystemState().getBuiltins().__getitem__("sorted");
            PyObject methods = Py.newUnicode(", ").join(sorted.__call__(subtype.getAbstractmethods()));
            throw Py.TypeError(String.format("Can't instantiate abstract class %s with abstract "
                    + "methods %s", subtype.fastGetName(), methods));
        }

        return new_.for_type == subtype ? new PyObject() : new PyObjectDerived(subtype);
    }

    // tp_richcompare
    public static PyObject richCompare(PyObject obj, PyObject other, CompareOp op) {
        PyObject res;
        switch (op) {
            case EQ:
                res = (obj == other) ? Py.True : Py.NotImplemented;
                break;
            case NE:
                // by default, __ne__() delegates to __eq__() and inverts the result,
                // unless the latter returns NotImplemented
                // NOTE: this is recursive
                res = obj.richCompare(other, CompareOp.EQ);
                if (res != Py.NotImplemented) {
                    res = Py.newBoolean(res != Py.True);
                }
                break;
            default:
                res = Py.NotImplemented;
        }
        return res;
    }

    private final static PyObject check_recursion(
            ThreadState ts,
            PyObject o1,
            PyObject o2) {
        PyDictionary stateDict = ts.getCompareStateDict();

        PyObject pair = o1.make_pair(o2);

        if (stateDict.__finditem__(pair) != null)
            return null;

        stateDict.__setitem__(pair, pair);
        return pair;
    }

    private final static void delete_token(ThreadState ts, PyObject token) {
        if (token == null)
            return;
        PyDictionary stateDict = ts.getCompareStateDict();

        stateDict.__delitem__(token);
    }

    public static final String asName(PyObject obj) {
        try {
            return obj.asName(0);
        } catch (PyObject.ConversionException e) {
            throw Py.TypeError("attribute name must be a string");
        }
    }

    private static PyObject slotnames(PyObject cls) {
        PyObject slotnames;

        slotnames = cls.fastGetDict().__finditem__("__slotnames__");
        if (null != slotnames) {
            return slotnames;
        }

        PyObject copyreg = Import.importModuleLevel("copyreg", null, Py.EmptyTuple, 0);
        PyObject copyreg_slotnames = copyreg.__findattr__("_slotnames");
        slotnames = copyreg_slotnames.__call__(cls);
        if (null != slotnames && Py.None != slotnames && (!(slotnames instanceof PyList))) {
            throw Py.TypeError("copyreg._slotnames didn't return a list or None");
        }

        return slotnames;
    }

    @ExposedClassMethod
    public final static PyObject __init_class__(PyType cls) {
        return Py.None;
    }

    @ExposedClassMethod(doc = BuiltinDocs.object___subclasshook___doc)
    public static PyObject object___subclasshook__(PyType type, PyObject subclass) {
        return Py.NotImplemented;
    }

    @ExposedMethod(doc = BuiltinDocs.object___init___doc)
    public final void object___init__(PyObject[] args, String[] keywords) {
        if (args.length > 0) {
            PyObject[] where = new PyObject[1];
            getType().lookup_where("__new__", where);
            // if called with arguments and __new__ if not override, throw TypeError
            if (where[0] == TYPE) {
                throw Py.TypeError("object.__init__() takes no parameters");
            }
        }
    }

    @ExposedGet(name = "__class__")
    public PyType getType() {
        return objtype;
    }

    @ExposedSet(name = "__class__")
    public void setType(PyType type) {
        if (type.builtin || getType().builtin) {
            throw Py.TypeError("__class__ assignment: only for heap types");
        }
        type.compatibleForAssignment(getType(), "__class__");
        objtype = type;
    }

    @ExposedDelete(name = "__class__")
    public void delType() {
        throw Py.TypeError("can't delete __class__ attribute");
    }

    // xxx
    public PyObject fastGetClass() {
        return objtype;
    }

    /**
     * Dispatch __init__ behavior
     */
    public void dispatch__init__(PyObject[] args, String[] keywords) {
    }

    /**
     * Attempts to automatically initialize our Java proxy if we have one and it wasn't initialized
     * by our __init__.
     */
    void proxyInit() {
        Class<?> c = getType().getProxyType();
        Object javaProxy = JyAttribute.getAttr(this, JyAttribute.JAVA_PROXY_ATTR);
        if (javaProxy != null || c == null) {
            return;
        }
        if (!PyProxy.class.isAssignableFrom(c)) {
            throw Py.SystemError("Automatic proxy initialization should only occur on proxy classes");
        }
        PyProxy proxy;
        Object[] previous = ThreadContext.initializingProxy.get();
        ThreadContext.initializingProxy.set(new Object[]{this});
        try {
            try {
                proxy = (PyProxy) c.getDeclaredConstructor().newInstance();
            } catch (java.lang.InstantiationException e) {
                Class<?> sup = c.getSuperclass();
                String msg = "Default constructor failed for Java superclass";
                if (sup != null) {
                    msg += " " + sup.getName();
                }
                throw Py.TypeError(msg);
            } catch (NoSuchMethodError nsme) {
                throw Py.TypeError("constructor requires arguments");
            } catch (Exception exc) {
                throw Py.JavaError(exc);
            }
        } finally {
            ThreadContext.initializingProxy.set(previous);
        }
        javaProxy = JyAttribute.getAttr(this, JyAttribute.JAVA_PROXY_ATTR);
        if (javaProxy != null && javaProxy != proxy) {
            throw Py.TypeError("Proxy instance already initialized");
        }
        PyObject proxyInstance = proxy._getPyInstance();
        if (proxyInstance != null && proxyInstance != this) {
            throw Py.TypeError("Proxy initialized with another instance");
        }
        JyAttribute.setAttr(this, JyAttribute.JAVA_PROXY_ATTR, proxy);
    }

    /**
     * Equivalent to the standard Python __repr__ method.  This method
     * should not typically need to be overrriden.  The easiest way to
     * configure the string representation of a <code>PyObject</code> is to
     * override the standard Java <code>toString</code> method.
     **/
    // counter-intuitively exposing this as __str__, otherwise stack overflow
    // occurs during regression testing.  XXX: more detail for this comment
    // is needed.
    @ExposedMethod(names = "__str__", doc = BuiltinDocs.object___str___doc)
    public final PyUnicode object__str__() {
//        return __repr__();
        return new PyUnicode(toString());
    }

    @ExposedMethod(names = "__repr__", doc = BuiltinDocs.object___repr___doc)
    public final PyUnicode object___repr__() {
        return new PyUnicode(toString());
    }

    /**
     * Equivalent to the standard Python __str__ method.  This method
     * should not typically need to be overridden.  The easiest way to
     * configure the string representation of a <code>PyObject</code> is to
     * override the standard Java <code>toString</code> method.
     **/
    public PyUnicode __str__() {
//        return (PyUnicode) invoke("__repr__");
        return object__str__();
    }

    /**
     * PyObjects that implement
     * <code>org.python.core.finalization.HasFinalizeTrigger</code>
     * shall implement this method via:<br>
     * <code>FinalizeTrigger.ensureFinalizer(this);</code>
     **/
    @ExposedMethod
    public void __ensure_finalizer__() {
    }

    /**
     * Equivalent to the standard Python __hash__ method.  This method can
     * not be overridden.  Instead, you should override the standard Java
     * <code>hashCode</code> method to return an appropriate hash code for
     * the <code>PyObject</code>.
     **/
    public final PyLong __hash__() {
        return new PyLong(hashCode());
    }

    @Override
    public int hashCode() {
        return object___hash__();
    }

    /**
     * Should almost never be overridden.
     * If overridden, it is the subclasses responsibility to ensure that
     * <code>a.equals(b) == true</code> iff <code>cmp(a,b) == 0</code>
     **/
    @Override
    public boolean equals(Object ob_other) {
        if (ob_other == this) {
            return true;
        }
        if (!(ob_other instanceof PyObject)) {
            return false;
        }
        PyObject res = richCompare((PyObject) ob_other, CompareOp.EQ);
        if (res == Py.NotImplemented) {
            return false;
        }
        return res.isTrue();
    }

    @Override
    public String toString() {
        if (getType() == null) {
            return "unknown object";
        }

        String name = getType().getName();
        if (name == null) {
            return "unknown object";
        }

        PyObject module = getType().getModule();
        if (module instanceof PyUnicode && !module.toString().equals("builtins")) {
            return String.format("<%s.%s object at %s>", module.toString(), name, Py.idstr(this));
        }
        return String.format("<%s object at %s>", name, Py.idstr(this));
    }

    /**
     * <p>
     * From Jython 2.7 on, {@code PyObject}s must not have finalizers directly.
     * If a finalizer, a.k.a. {@code __del__} is needed, follow the instructions in the
     * documentation of {@link org.python.core.finalization.FinalizablePyObject}.
     * </p>
     * <p>
     * Note that this empty finalizer implementation is optimized away by the JVM
     * (See {@link http://www.javaspecialists.eu/archive/Issue170.html}).
     * So {@code PyObject}s are not expensively treated as finalizable objects by the
     * Java-GC. Its single intention is to prevent subclasses from having Java-style
     * finalizers.
     * </p>
     */
    @SuppressWarnings("deprecation")
    protected final void finalize() throws Throwable {
    }

    @ExposedMethod(doc = BuiltinDocs.object___hash___doc)
    final int object___hash__() {
        return System.identityHashCode(this);
    }

    /**
     * Equivalent to the standard Python __bool__ method. Returns whether of
     * not a given <code>PyObject</code> is considered true.
     * https://docs.python.org/3/library/stdtypes.html#truth-value-testing
     */
    public boolean isTrue() {
        return true;
    }

    /**
     * Equivalent to the Jython __tojava__ method.
     * Tries to coerce this object to an instance of the requested Java class.
     * Returns the special object <code>Py.NoConversion</code>
     * if this <code>PyObject</code> can not be converted to the
     * desired Java class.
     *
     * @param c the Class to convert this <code>PyObject</code> to.
     **/
    public Object __tojava__(Class<?> c) {
        if ((c == Object.class || c == Serializable.class) && getJavaProxy() != null) {
            return JyAttribute.getAttr(this, JyAttribute.JAVA_PROXY_ATTR);
        }
        if (c.isInstance(this)) {
            return this;
        }
        if (c.isPrimitive()) {
            Class<?> tmp = primitiveMap.get(c);
            if (tmp != null) {
                c = tmp;
            }
        }
        if (c.isInstance(getJavaProxy())) {
            return JyAttribute.getAttr(this, JyAttribute.JAVA_PROXY_ATTR);
        }

        // convert faux floats
        // XXX: should also convert faux ints, but that breaks test_java_visibility
        // (ReflectedArgs resolution)
        if (c == Double.class || c == Float.class) {
            try {
                return Abstract.PyNumber_Float(Py.getThreadState(), this).asDouble();
            } catch (PyException pye) {
                if (!pye.match(Py.AttributeError)) {
                    throw pye;
                }
            }
        }

        return Py.NoConversion;
    }

    protected synchronized Object getJavaProxy() {
        if (!JyAttribute.hasAttr(this, JyAttribute.JAVA_PROXY_ATTR)) {
            proxyInit();
        }
        return JyAttribute.getAttr(this, JyAttribute.JAVA_PROXY_ATTR);
    }

    /**
     * The basic method to override when implementing a callable object.
     * <p>
     * The first len(args)-len(keywords) members of args[] are plain
     * arguments.  The last len(keywords) arguments are the values of the
     * keyword arguments.
     *
     * @param args     all arguments to the function (including
     *                 keyword arguments).
     * @param keywords the keywords used for all keyword arguments.
     **/
    public PyObject __call__(PyObject args[], String keywords[]) {
        throw Py.TypeError(String.format("'%s' object is not callable", getType().fastGetName()));
    }

    public PyObject __call__(ThreadState state, PyObject args[], String keywords[]) {
        return __call__(args, keywords);
    }

    /**
     * A variant of the __call__ method with one extra initial argument.
     * This variant is used to allow method invocations to be performed
     * efficiently.
     * <p>
     * The default behavior is to invoke <code>__call__(args,
     * keywords)</code> with the appropriate arguments.  The only reason to
     * override this function would be for improved performance.
     *
     * @param arg1     the first argument to the function.
     * @param args     the last arguments to the function (including
     *                 keyword arguments).
     * @param keywords the keywords used for all keyword arguments.
     **/
    public PyObject __call__(PyObject arg1, PyObject args[], String keywords[]) {
        PyObject[] newArgs = new PyObject[args.length + 1];
        System.arraycopy(args, 0, newArgs, 1, args.length);
        newArgs[0] = arg1;
        return __call__(newArgs, keywords);
    }

    public PyObject __call__(ThreadState state, PyObject arg1, PyObject args[], String keywords[]) {
        return __call__(arg1, args, keywords);
    }

    /**
     * A variant of the __call__ method when no keywords are passed.  The
     * default behavior is to invoke <code>__call__(args, keywords)</code>
     * with the appropriate arguments.  The only reason to override this
     * function would be for improved performance.
     *
     * @param args all arguments to the function.
     **/
    public PyObject __call__(PyObject args[]) {
        return __call__(args, Py.NoKeywords);
    }

    public PyObject __call__(ThreadState state, PyObject args[]) {
        return __call__(args);
    }

    /**
     * A variant of the __call__ method with no arguments.  The default
     * behavior is to invoke <code>__call__(args, keywords)</code> with the
     * appropriate arguments.  The only reason to override this function
     * would be for improved performance.
     **/
    public PyObject __call__() {
        return __call__(Py.EmptyObjects, Py.NoKeywords);
    }

    public PyObject __call__(ThreadState state) {
        return __call__();
    }

    /**
     * A variant of the __call__ method with one argument.  The default
     * behavior is to invoke <code>__call__(args, keywords)</code> with the
     * appropriate arguments.  The only reason to override this function
     * would be for improved performance.
     *
     * @param arg0 the single argument to the function.
     **/
    public PyObject __call__(PyObject arg0) {
        return __call__(new PyObject[]{arg0}, Py.NoKeywords);
    }

    public PyObject __call__(ThreadState state, PyObject arg0) {
        return __call__(arg0);
    }

    /**
     * A variant of the __call__ method with two arguments.  The default
     * behavior is to invoke <code>__call__(args, keywords)</code> with the
     * appropriate arguments.  The only reason to override this function
     * would be for improved performance.
     *
     * @param arg0 the first argument to the function.
     * @param arg1 the second argument to the function.
     **/
    public PyObject __call__(PyObject arg0, PyObject arg1) {
        return __call__(new PyObject[]{arg0, arg1}, Py.NoKeywords);
    }

    public PyObject __call__(ThreadState state, PyObject arg0, PyObject arg1) {
        return __call__(arg0, arg1);
    }

    /**
     * A variant of the __call__ method with three arguments.  The default
     * behavior is to invoke <code>__call__(args, keywords)</code> with the
     * appropriate arguments.  The only reason to override this function
     * would be for improved performance.
     *
     * @param arg0 the first argument to the function.
     * @param arg1 the second argument to the function.
     * @param arg2 the third argument to the function.
     **/
    public PyObject __call__(PyObject arg0, PyObject arg1, PyObject arg2) {
        return __call__(new PyObject[]{arg0, arg1, arg2}, Py.NoKeywords);
    }

    public PyObject __call__(ThreadState state, PyObject arg0, PyObject arg1, PyObject arg2) {
        return __call__(arg0, arg1, arg2);
    }

    /**
     * A variant of the __call__ method with four arguments.  The default
     * behavior is to invoke <code>__call__(args, keywords)</code> with the
     * appropriate arguments.  The only reason to override this function
     * would be for improved performance.
     *
     * @param arg0 the first argument to the function.
     * @param arg1 the second argument to the function.
     * @param arg2 the third argument to the function.
     * @param arg3 the fourth argument to the function.
     **/
    public PyObject __call__(PyObject arg0, PyObject arg1, PyObject arg2, PyObject arg3) {
        return __call__(
                new PyObject[]{arg0, arg1, arg2, arg3},
                Py.NoKeywords);
    }

    public PyObject __call__(ThreadState state, PyObject arg0, PyObject arg1, PyObject arg2, PyObject arg3) {
        return __call__(arg0, arg1, arg2, arg3);
    }

    /* The basic functions to implement a mapping */

    public PyObject _callextra(List<PyObject> arglist,
                               String[] keywords,
                               PyObject[] kwargsArray) {
        PyObject[] args = arglist.toArray(new PyObject[0]);

        int argslen = args.length;

        String name;
        if (this instanceof PyFunction) {
            name = ((PyFunction) this).__name__ + "() ";
        } else if (this instanceof PyBuiltinCallable) {
            name = ((PyBuiltinCallable) this).fastGetName().toString() + "() ";
        } else {
            name = getType().fastGetName() + " ";
        }
        for (PyObject kwargs : kwargsArray) {
            argslen += kwargs.__len__();
        }

        PyObject[] newargs = new PyObject[argslen];
        int argidx = args.length;
        if (argslen > args.length)
            System.arraycopy(args, 0, newargs, 0, argidx);

        for (PyObject kwargs : kwargsArray) {

            String[] newkeywords =
                    new String[keywords.length + kwargs.__len__()];
            System.arraycopy(keywords, 0, newkeywords, 0, keywords.length);

            PyObject keys = kwargs.invoke("keys");
            int i = 0;
            Iterator<PyObject> keysIter = keys.asIterable().iterator();
            for (PyObject key; keysIter.hasNext(); ) {
                key = keysIter.next();
                if (!(key instanceof PyUnicode))
                    throw Py.TypeError(name + "keywords must be strings");
                newkeywords[keywords.length + i++] =
                        ((PyUnicode) key).internedString();
                newargs[argidx++] = kwargs.__finditem__(key);
            }
            keywords = newkeywords;
        }
        if (newargs.length > args.length)
            args = newargs;
        return __call__(args, keywords);
    }

    public boolean isCallable() {
        return getType().lookup("__call__") != null;
    }

    public boolean isNumberType() {
        PyType type = getType();
        return type.lookup("__int__") != null || type.lookup("__float__") != null;
    }

    public boolean isMappingType() {
        PyType type = getType();
        return type.lookup("__getitem__") != null
                && !(isSequenceType() && type.lookup("__getslice__") != null);
    }

    public boolean isSequenceType() {
        return getType().lookup("__getitem__") != null;
    }

    /**
     * Determine if this object can act as an int (implements __int__).
     *
     * @return true if the object can act as an int
     */
    public boolean isInteger() {
        return getType().lookup("__int__") != null;
    }

    /**
     * Determine if this object can act as an index (implements __index__).
     *
     * @return true if the object can act as an index
     */
    public boolean isIndex() {
        return getType().lookup("__index__") != null;
    }

    /**
     * Equivalent to the standard Python __len__ method.
     * Part of the mapping discipline.
     *
     * @return the length of the object
     **/
    public int __len__() {
        throw Py.TypeError(String.format("object of type '%.200s' has no len()",
                getType().fastGetName()));
    }

    /**
     * Very similar to the standard Python __getitem__ method.
     * Instead of throwing a KeyError if the item isn't found,
     * this just returns null.
     * <p>
     * Classes that wish to implement __getitem__ should
     * override this method instead (with the appropriate
     * semantics.
     *
     * @param key the key to lookup in this container
     * @return the value corresponding to key or null if key is not found
     **/
    public PyObject __finditem__(PyObject key) {
        throw Py.TypeError(String.format("'%.200s' object is unsubscriptable",
                getType().fastGetName()));
    }

    /**
     * A variant of the __finditem__ method which accepts a primitive
     * <code>int</code> as the key.  By default, this method will call
     * <code>__finditem__(PyObject key)</code> with the appropriate args.
     * The only reason to override this method is for performance.
     *
     * @param key the key to lookup in this sequence.
     * @return the value corresponding to key or null if key is not found.
     * @see #__finditem__(PyObject)
     **/
    public PyObject __finditem__(int key) {
        return __finditem__(new PyLong(key));
    }

    /**
     * A variant of the __finditem__ method which accepts a Java
     * <code>String</code> as the key.  By default, this method will call
     * <code>__finditem__(PyObject key)</code> with the appropriate args.
     * The only reason to override this method is for performance.
     * <p>
     * <b>Warning: key must be an interned string!!!!!!!!</b>
     *
     * @param key the key to lookup in this sequence -
     *            <b> must be an interned string </b>.
     * @return the value corresponding to key or null if key is not found.
     * @see #__finditem__(PyObject)
     **/
    public PyObject __finditem__(String key) {
        return __finditem__(new PyUnicode(key));
    }

    /**
     * Equivalent to the standard Python __getitem__ method.
     * This variant takes a primitive <code>int</code> as the key.
     * This method should not be overridden.
     * Override the <code>__finditem__</code> method instead.
     *
     * @param key the key to lookup in this container.
     * @return the value corresponding to that key.
     * @throws Py.KeyError if the key is not found.
     * @see #__finditem__(int)
     **/
    public PyObject __getitem__(int key) {
        PyObject ret = __finditem__(key);
        if (ret == null)
            throw Py.KeyError("" + key);
        return ret;
    }

    /*The basic functions to implement an iterator */

    /**
     * Equivalent to the standard Python __getitem__ method.
     * This method should not be overridden.
     * Override the <code>__finditem__</code> method instead.
     *
     * @param key the key to lookup in this container.
     * @return the value corresponding to that key.
     * @throws Py.KeyError if the key is not found.
     * @see #__finditem__(PyObject)
     **/
    public PyObject __getitem__(PyObject key) {
        PyObject ret = __finditem__(key);
        if (ret == null) {
            throw Py.KeyError(key);
        }
        return ret;
    }

    public PyObject __getitem__(String key) {
        PyObject ret = __finditem__(key);
        if (ret == null) {
            throw Py.KeyError(key);
        }
        return ret;
    }

    /**
     * Equivalent to the standard Python __setitem__ method.
     *
     * @param key   the key whose value will be set
     * @param value the value to set this key to
     **/
    public void __setitem__(PyObject key, PyObject value) {
        throw Py.TypeError(String.format("'%.200s' object does not support item assignment",
                getType().fastGetName()));
    }

    /*The basic functions to implement a namespace*/

    /**
     * A variant of the __setitem__ method which accepts a String
     * as the key.  <b>This String must be interned</b>.
     * By default, this will call
     * <code>__setitem__(PyObject key, PyObject value)</code>
     * with the appropriate args.
     * The only reason to override this method is for performance.
     *
     * @param key   the key whose value will be set -
     *              <b> must be an interned string </b>.
     * @param value the value to set this key to
     * @see #__setitem__(PyObject, PyObject)
     **/
    public void __setitem__(String key, PyObject value) {
        __setitem__(new PyUnicode(key), value);
    }

    /**
     * A variant of the __setitem__ method which accepts a primitive
     * <code>int</code> as the key.
     * By default, this will call
     * <code>__setitem__(PyObject key, PyObject value)</code>
     * with the appropriate args.
     * The only reason to override this method is for performance.
     *
     * @param key   the key whose value will be set
     * @param value the value to set this key to
     * @see #__setitem__(PyObject, PyObject)
     **/
    public void __setitem__(int key, PyObject value) {
        __setitem__(new PyLong(key), value);
    }

    /**
     * Equivalent to the standard Python __delitem__ method.
     *
     * @param key the key to be removed from the container
     * @throws Py.KeyError if the key is not found in the container
     **/
    public void __delitem__(PyObject key) {
        throw Py.TypeError(String.format("'%.200s' object doesn't support item deletion",
                getType().fastGetName()));
    }

    /**
     * A variant of the __delitem__ method which accepts a String
     * as the key.  <b>This String must be interned</b>.
     * By default, this will call
     * <code>__delitem__(PyObject key)</code>
     * with the appropriate args.
     * The only reason to override this method is for performance.
     *
     * @param key the key who will be removed -
     *            <b> must be an interned string </b>.
     * @throws Py.KeyError if the key is not found in the container
     * @see #__delitem__(PyObject)
     **/
    public void __delitem__(String key) {
        __delitem__(new PyUnicode(key));
    }

    public static PyObject getItem(PyObject o, PyObject key) {
        return Py.None;
    }

    public static PyObject getIter(PyObject o) {
        if (o.getType().isIterator) {
            return o;
        }
        PyObject res;
        try {
            res = o.unaryOp(Py.getThreadState(), iter);
        } catch (PyException e) {
            if (e.match(Py.AttributeError)) {
                Object getitemFunc = null;
                try {
                    getitemFunc = getitem.getGetter().invokeExact(o);
                    return new PySeqIterator(o, getitemFunc);
                } catch (Throwable throwable) {
                    throw Py.TypeError(String.format("%s object is not iterable", o.getType().fastGetName()));
                }
            }
            throw e;
        }
        if (res.getType().iternext == null) {
            try {
                Object nextFunc = o.getType().next.getGetter().invokeExact(res);
            } catch (PyException e) {
                if (e.match(Py.AttributeError)) {
                    throw Py.TypeError(String.format("iter() returned non-iterator of type '%s'", res.getType().fastGetName()));
                }
                throw e;
            } catch (Throwable e) {
                throw Py.JavaError(e);
            }
        }
        return res;
    }

    public static PyObject iterNext(PyObject iterator) {
        PyType tp = iterator.getType();
        if (tp.iternext != null) {
            try {
                return (PyObject) tp.iternext.invokeExact(iterator);
            } catch (Throwable e) {
                throw Py.JavaError(e);
            }
        }
        return iterator.unaryOp(Py.getThreadState(), next);
    }

    /**
     * Returns an Iterable over the Python iterator returned by __iter__ on this object. If this
     * object doesn't support __iter__, a TypeException will be raised when iterator is called on
     * the returned Iterable.
     */
    public Iterable<PyObject> asIterable() {
        return () -> new WrappedIterIterator<PyObject>(getIter(this)) {
            public PyObject next() {
                return getNext();
            }
        };
    }

    /**
     * Very similar to the standard Python __getattr__ method. Instead of
     * throwing a AttributeError if the item isn't found, this just returns
     * null.
     * <p>
     * By default, this method will call
     * <code>__findattr__(name.internedString)</code> with the appropriate
     * args.
     *
     * @param name the name to lookup in this namespace
     * @return the value corresponding to name or null if name is not found
     */
    public final PyObject __findattr__(PyUnicode name) {
        if (name == null) {
            return null;
        }
        return __findattr__(name.internedString());
    }

    /**
     * A variant of the __findattr__ method which accepts a Java
     * <code>String</code> as the name.
     * <p>
     * <b>Warning: name must be an interned string!</b>
     *
     * @param name the name to lookup in this namespace
     *             <b> must be an interned string </b>.
     * @return the value corresponding to name or null if name is not found
     **/
    public final PyObject __findattr__(String name) {
        try {
            return __findattr_ex__(name);
        } catch (PyException exc) {
            if (exc.match(Py.AttributeError)) {
                return null;
            }
            throw exc;
        }
    }

    /**
     * Attribute lookup hook. If the attribute is not found, null may be
     * returned or a Py.AttributeError can be thrown, whatever is more
     * correct, efficient and/or convenient for the implementing class.
     * <p>
     * Client code should use {@link #__getattr__(String)} or
     * {@link #__findattr__(String)}. Both methods have a clear policy for
     * failed lookups.
     *
     * @return The looked up value. May return null if the attribute is not found
     * @throws PyException(AttributeError) if the attribute is not found. This
     *                                     is not mandatory, null can be returned if it fits the implementation
     *                                     better, or for performance reasons.
     */
    public PyObject __findattr_ex__(String name) {
        return object___findattr__(name);
    }

    /**
     * Equivalent to the standard Python __getattr__ method.
     * <p>
     * By default, this method will call
     * <code>__getattr__(name.internedString)</code> with the appropriate
     * args.
     *
     * @param name the name to lookup in this namespace
     * @return the value corresponding to name
     * @throws Py.AttributeError if the name is not found.
     * @see #__findattr_ex__(String)
     **/
    public final PyObject __getattr__(String name) {
        return __getattr__(new PyUnicode(name));
    }

    /**
     * A variant of the __getattr__ method which accepts a Java
     * <code>String</code> as the name.
     * This method can not be overridden.
     * Override the <code>__findattr_ex__</code> method instead.
     * <p>
     * <b>Warning: name must be an interned string!!!!!!!!</b>
     *
     * @param name the name to lookup in this namespace
     *             <b> must be an interned string </b>.
     * @return the value corresponding to name
     * @throws Py.AttributeError if the name is not found.
     * @see #__findattr__(java.lang.String)
     **/
    public final PyObject __getattr__(PyUnicode name) {
        PyType selfType = getType();
        if (selfType == TYPE || selfType.getUsesObjectGetattribute()) {
            return object___getattribute__(name);
        }
        PyObject getattr = selfType.lookup("__getattribute__");
        if (getattr instanceof PyBuiltinMethod) {
            return ((PyBuiltinMethod) getattr).invoke(name);
        }
        PyObject func = getattr.__get__(this, selfType);
        if (func instanceof PyBuiltinMethod) {
            return ((PyBuiltinMethod) func).invoke(name);
        }
        return func.__call__(name);
    }

    public void noAttributeError(String name) {
        throw Py.AttributeError(String.format("'%.50s' object has no attribute '%.400s'",
                getType().fastGetName(), name));
    }

    public void readonlyAttributeError(String name) {
        // XXX: Should be an AttributeError but CPython throws TypeError for read only
        // member descriptors (in structmember.c::PyMember_SetOne), which is expected by a
        // few tests. fixed in py3k: http://bugs.python.org/issue1687163
        throw Py.AttributeError("readonly attribute");
    }

    /**
     * Equivalent to the standard Python __setattr__ method.
     * This method can not be overridden.
     *
     * @param name the name to lookup in this namespace
     * @throws Py.AttributeError if the name is not found.
     * @see #__setattr__(java.lang.String, PyObject)
     **/
    public final void __setattr__(PyUnicode name, PyObject value) {
        __setattr__(name.internedString(), value);
    }

    /**
     * A variant of the __setattr__ method which accepts a String
     * as the key.  <b>This String must be interned</b>.
     *
     * @param name  the name whose value will be set -
     *              <b> must be an interned string </b>.
     * @param value the value to set this name to
     * @see #__setattr__(PyBytes, PyObject)
     **/
    public void __setattr__(String name, PyObject value) {
        object___setattr__(name, value);
    }

    /**
     * Equivalent to the standard Python __delattr__ method.
     * This method can not be overridden.
     *
     * @param name the name to which will be removed
     * @throws Py.AttributeError if the name doesn't exist
     * @see #__delattr__(java.lang.String)
     **/
    public final void __delattr__(PyUnicode name) {
        __delattr__(name.internedString());
    }

    /**
     * A variant of the __delattr__ method which accepts a String
     * as the key.  <b>This String must be interned</b>.
     * By default, this will call
     * <code>__delattr__(PyBytes name)</code>
     * with the appropriate args.
     * The only reason to override this method is for performance.
     *
     * @param name the name which will be removed -
     *             <b> must be an interned string </b>.
     * @throws Py.AttributeError if the name doesn't exist
     * @see #__delattr__(PyBytes)
     **/
    public void __delattr__(String name) {
        object___delattr__(name);
    }

    protected void mergeListAttr(PyDictionary accum, String attr) {
        PyObject obj = __findattr__(attr);
        if (obj == null) {
            return;
        }
        if (obj instanceof PyList) {
            for (PyObject name : obj.asIterable()) {
                accum.__setitem__(name, Py.None);
            }
        }
    }

    protected void mergeDictAttr(PyDictionary accum, String attr) {
        PyObject obj = __findattr__(attr);
        if (obj == null) {
            return;
        }
        if (obj instanceof PyDictionary || obj instanceof PyStringMap
                || obj instanceof PyDictProxy) {
            accum.update(obj);
        }
    }

    /* Numeric coercion */

    protected void mergeClassDict(PyDictionary accum, PyObject aClass) {
        // Merge in the type's dict (if any)
        aClass.mergeDictAttr(accum, "__dict__");

        // Recursively merge in the base types' (if any) dicts
        PyObject bases = aClass.__findattr__("__bases__");
        if (bases == null) {
            return;
        }
        // We have no guarantee that bases is a real tuple
        int len = bases.__len__();
        for (int i = 0; i < len; i++) {
            mergeClassDict(accum, bases.__getitem__(i));
        }
    }

    protected void __rawdir__(PyDictionary accum) {
        mergeDictAttr(accum, "__dict__");
        // Class dict is a slower, more manual merge to match CPython
        PyObject itsClass = __findattr__("__class__");
        if (itsClass != null) {
            mergeClassDict(accum, itsClass);
        }
    }

    /**
     * Equivalent to the standard Python __dir__ method.
     *
     * @return a list of names defined by this object.
     **/
    @ExposedMethod
    public PyObject object___dir__() {
        PyDictionary accum = new PyDictionary();
        __rawdir__(accum);
        PyList ret = accum.keys_as_list();
        ret.sort();
        return ret;
    }

    public PyObject _doget(PyObject container) {
        return this;
    }

    public PyObject _doget(PyObject container, PyObject wherefound) {
        return _doget(container);
    }

    public boolean _doset(PyObject container, PyObject value) {
        return false;
    }

    boolean jdontdel() {
        return false;
    }

    /**
     * Implements numeric coercion
     *
     * @param o the other object involved in the coercion
     * @return null if coercion is not implemented
     * Py.None if coercion was not possible
     * a single PyObject to use to replace o if this is unchanged;
     * or a PyObject[2] consisting of replacements for this and o.
     **/
    public Object __coerce_ex__(PyObject o) {
        return null;
    }

    /**
     * Implements coerce(this,other), result as PyObject[]
     *
     * @param other
     * @return PyObject[]
     */
    PyObject[] _coerce(PyObject other) {
        Object result;
        if (this.getType() == other.getType()) {
            return new PyObject[]{this, other};
        }
        result = this.__coerce_ex__(other);
        if (result != null && result != Py.None) {
            if (result instanceof PyObject[]) {
                return (PyObject[]) result;
            } else {
                return new PyObject[]{this, (PyObject) result};
            }
        }
        result = other.__coerce_ex__(this);
        if (result != null && result != Py.None) {
            if (result instanceof PyObject[]) {
                return (PyObject[]) result;
            } else {
                return new PyObject[]{(PyObject) result, other};
            }
        }
        return null;

    }

    /**
     * Equivalent to the standard Python __coerce__ method.
     * <p>
     * This method can not be overridden.
     * To implement __coerce__ functionality, override __coerce_ex__ instead.
     * <p>
     * Also, <b>do not</b> call this method from exposed 'coerce' methods.
     * Instead, Use adaptToCoerceTuple over the result of the overriden
     * __coerce_ex__.
     *
     * @param pyo the other object involved in the coercion.
     * @return a tuple of this object and pyo coerced to the same type
     * or Py.NotImplemented if no coercion is possible.
     * @see org.python.core.PyObject#__coerce_ex__(org.python.core.PyObject)
     **/
    public final PyObject __coerce__(PyObject pyo) {
        Object o = __coerce_ex__(pyo);
        if (o == null) {
            throw Py.AttributeError("__coerce__");
        }
        return adaptToCoerceTuple(o);
    }

    /**
     * Adapts the result of __coerce_ex__ to a tuple of two elements, with the
     * resulting coerced values, or to Py.NotImplemented, if o is Py.None.
     * <p>
     * This is safe to be used from subclasses exposing '__coerce__'
     * (as opposed to {@link #__coerce__(PyObject)}, which calls the virtual
     * method {@link #__coerce_ex__(PyObject)})
     *
     * @param o either a PyObject[2] or a PyObject, as given by
     *          {@link #__coerce_ex__(PyObject)}.
     */
    protected final PyObject adaptToCoerceTuple(Object o) {
        if (o == Py.None) {
            return Py.NotImplemented;
        }
        if (o instanceof PyObject[]) {
            return new PyTuple((PyObject[]) o);
        } else {
            return new PyTuple(this, (PyObject) o);
        }
    }

    @ExposedMethod(doc = BuiltinDocs.object___eq___doc)
    public PyObject __eq__(PyObject other) {
        return PyObject.richCompare(this, other, CompareOp.EQ);
    }

    @ExposedMethod(doc = BuiltinDocs.object___ne___doc)
    public PyObject __ne__(PyObject other) {
        return PyObject.richCompare(this, other, CompareOp.NE);
    }

    @ExposedMethod
    public PyObject __le__(PyObject other) {
        return PyObject.richCompare(this, other, CompareOp.LE);
    }

    @ExposedMethod
    public PyObject __lt__(PyObject other) {
        return PyObject.richCompare(this, other, CompareOp.LT);
    }

    @ExposedMethod
    public PyObject __ge__(PyObject other) {
        return PyObject.richCompare(this, other, CompareOp.GE);
    }

    @ExposedMethod
    public PyObject __gt__(PyObject other) {
        return PyObject.richCompare(this, other, CompareOp.GT);
    }

    /**
     * Implements cmp(this, other)
     *
     * @param o the object to compare this with.
     * @return -1 if this < 0; 0 if this == o; +1 if this > o
     **/
    public final int _cmp(PyObject o) {
        PyObject res = richCompare(o, CompareOp.EQ);
        if (res != Py.NotImplemented && res.isTrue()) {
            return 0;
        }
        res = do_richCompare(o, CompareOp.LT);
        if (res != Py.NotImplemented) {
            if (res.isTrue()) {
                return -1;
            }
            return 1;
        }
        throw Py.TypeError("not orderable");
//        if (this == o) {
//            return 0;
//        }
//
//        PyObject token = null;
//        ThreadState ts = Py.getThreadState();
//        try {
//            if (++ts.compareStateNesting > 500) {
//                if ((token = check_recursion(ts, this, o)) == null)
//                    return 0;
//            }
//
//            PyObject result;
//            result = __eq__(o);
//            if (result == null || result == Py.NotImplemented) {
//                result = o.__eq__(this);
//            }
//            if (result != null && result != Py.NotImplemented && result.__bool__()) {
//                return 0;
//            }
//
//            result = __lt__(o);
//            if (result == null || result == Py.NotImplemented) {
//                result = o.__gt__(this);
//            }
//            if (result != null && result != Py.NotImplemented && result.__bool__()) {
//                return -1;
//            }
//
//            result = o.__lt__(this);
//            if (result == null || result == Py.NotImplemented) {
//                result = __gt__(o);
//            }
//            if (result != null && result != Py.NotImplemented && result.__bool__()) {
//                return 1;
//            }
//
//            throw Py.TypeError(String.format(UNORDERABLE_ERROR_MSG, getType().getName(), o.getType().getName()));
//        } finally {
//            delete_token(ts, token);
//            ts.compareStateNesting--;
//        }
    }

    private PyObject make_pair(PyObject o) {
        if (System.identityHashCode(this) < System.identityHashCode(o))
            return new PyIdentityTuple(new PyObject[]{this, o});
        else
            return new PyIdentityTuple(new PyObject[]{o, this});
    }

    /**
     * The break up into a static and an instance method is how it breaks up the recursive loop
     * Because I didn't find out how not to call override version of richCompare in __ne__
     *
     * @param other
     * @param op
     * @return
     */
    // slot_tp_richcompare
    public PyObject richCompare(PyObject other, CompareOp op) {
        return PyObject.richCompare(this, other, op);
    }

    public final boolean do_richCompareBool(PyObject other, CompareOp op) {
        if (this == other) {
            if (op == CompareOp.EQ) {
                return true;
            } else if (op == CompareOp.NE) {
                return false;
            }
        }

        PyObject res = do_richCompare(other, op);
        if (res instanceof PyBoolean) {
            return res == Py.True;
        }
        return res.isTrue();
    }

    // Rich comparison entry for bytecode
    public PyObject do_richCompare(PyObject other, CompareOp op) {
        PyObject token = null;
        ThreadState ts = Py.getThreadState();
        try {
            if (++ts.compareStateNesting > 500) {
                throw Py.RecursionError("maximum recursion depth exceeded");
            }

            boolean checkedReverseOp = false;
            PyObject res;
            PyType vt = getType();
            PyType wt = other.getType();
            if (vt != wt && wt.isSubType(vt)) {
                checkedReverseOp = true;
                res = other.richCompare(this, op.reflectedOp());
                if (res != Py.NotImplemented) {
                    return res;
                }
            }

            res = richCompare(other, op);
            if (res != Py.NotImplemented) {
                return res;
            }
            if (!checkedReverseOp) {
                res = other.richCompare(this, op.reflectedOp());
                if (res != Py.NotImplemented) {
                    return res;
                }
            }
            /** if neither object implements it, provide a sensible default
             * for == and !=, but raise an exception for ordering. */
            switch (op) {
                case EQ:
                    return Py.newBoolean(this == other);
                case NE:
                    return Py.newBoolean(this != other);
                default:
                    throw Py.TypeError(String.format("'%s' not supported between instance of '%.100s' and '%.100s'", op, vt.fastGetName(), wt.fastGetName()));
            }
        } finally {
            ts.compareStateNesting--;
        }
    }

    /**
     * Implements <code>is</code> operator.
     *
     * @param o the object to compare this with.
     * @return the result of the comparison
     **/
    public PyObject _is(PyObject o) {
        // Access javaProxy directly here as is is for object identity, and at best getJavaProxy
        // will initialize a new object with a different identity
        return this == o || (JyAttribute.hasAttr(this, JyAttribute.JAVA_PROXY_ATTR) &&
                JyAttribute.getAttr(this, JyAttribute.JAVA_PROXY_ATTR) ==
                        JyAttribute.getAttr(o, JyAttribute.JAVA_PROXY_ATTR)) ? Py.True : Py.False;
    }

    /**
     * Implements <code>is not</code> operator.
     *
     * @param o the object to compare this with.
     * @return the result of the comparison
     **/
    public PyObject _isnot(PyObject o) {
        // Access javaProxy directly here as is is for object identity, and at best getJavaProxy
        // will initialize a new object with a different identity
        return this != o && (!JyAttribute.hasAttr(this, JyAttribute.JAVA_PROXY_ATTR) ||
                JyAttribute.getAttr(this, JyAttribute.JAVA_PROXY_ATTR) !=
                        JyAttribute.getAttr(o, JyAttribute.JAVA_PROXY_ATTR)) ? Py.True : Py.False;
    }

    /* The basic numeric operations */

    final boolean object___contains__(PyObject o) {
        for (PyObject item : asIterable()) {
            if (o.do_richCompareBool(item, CompareOp.EQ)) {
                return true;
            }
        }
        return false;
    }

    @ExposedMethod(doc = BuiltinDocs.object___format___doc)
    final PyObject object___format__(PyObject formatSpec) {
        if (formatSpec != null && formatSpec instanceof PyUnicode && !((PyUnicode) formatSpec).getString().isEmpty()) {
            throw Py.TypeError(String.format("unsupported format string passed to %s.__format__", getType().getName()));
        }
        return __str__().str___format__(formatSpec);
    }

    /**
     * Equivalent to the standard Python __complex__ method.
     * Should only be overridden by numeric objects that can be
     * reasonably coerced into a python complex number.
     *
     * @return a complex number corresponding to the value of this object.
     **/
    public PyComplex __complex__() {
        throw Py.AttributeError("__complex__");
    }

    /**
     * Equivalent to the standard Python conjugate method.
     * Should only be overridden by numeric objects that can calculate a
     * complex conjugate.
     *
     * @return the complex conjugate.
     **/
    public PyObject conjugate() {
        throw Py.AttributeError("conjugate");
    }

    /**
     * Equivalent to the standard Python bit_length method.
     * Should only be overridden by numeric objects that can calculate a
     * bit_length.
     *
     * @return the bit_length of this object.
     **/
    public int bit_length() {
        throw Py.AttributeError("bit_length");
    }

    /**
     * Equivalent to the standard Python __index__ method.
     *
     * @return a PyLong
     * @throws a Py.TypeError if not supported
     **/
    public PyObject __index__() {
        throw Py.TypeError(String.format("'%.200s' object cannot be interpreted as an index",
                getType().fastGetName()));
    }

    /**
     * Should return an error message suitable for substitution where.
     * <p>
     * {0} is the op name.
     * {1} is the left operand type.
     * {2} is the right operand type.
     */
    protected String unsupportedopMessage(String op, PyObject o2) {
        return null;
    }

    /**
     * Should return an error message suitable for substitution where.
     * <p>
     * {0} is the op name.
     * {1} is the left operand type.
     * {2} is the right operand type.
     */
    protected String runsupportedopMessage(String op, PyObject o2) {
        return null;
    }

    /**
     * Implements the three argument power function.
     *
     * @param o2 the power to raise this number to.
     * @param o3 the modulus to perform this operation in or null if no
     *           modulo is to be used
     * @return this object raised to the given power in the given modulus
     **/
    public PyObject __pow__(PyObject o2, PyObject o3) {
        return null;
    }

    /**
     * Determine if the binary op on types t1 and t2 is an add
     * operation dealing with a str/unicode and a str/unicode
     * subclass.
     * <p>
     * This operation is special cased in _binop_rule to match
     * CPython's handling; CPython uses tp_as_number and
     * tp_as_sequence to allow string/unicode subclasses to override
     * the left side's __add__ when that left side is an actual str or
     * unicode object (see test_concat_jy for examples).
     *
     * @param t1 left side PyType
     * @param t2 right side PyType
     * @param op the binary operation's String
     * @return true if this is a special case
     */
    private boolean isStrUnicodeSpecialCase(PyType t1, PyType t2, String op) {
        // XXX: We may need to generalize this rule to apply to other
        // situations
        // XXX: This method isn't expensive but could (and maybe
        // should?) be optimized for worst case scenarios
        return (op == "+") && (t1 == PyBytes.TYPE || t1 == PyUnicode.TYPE) &&
                (t2.isSubType(PyBytes.TYPE) || t2.isSubType(PyUnicode.TYPE));
    }

    /**
     * Implements the Python expression <code>this + o2</code>.
     *
     * @param o2 the object to perform this binary operation with.
     * @return the result of the add.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _add(ThreadState ts, PyObject o2) {
        return binOp(ts, add, radd, o2);
    }

    /**
     * Implements the Python expression <code>this += o2</code>.
     *
     * @param o2 the object to perform this inplace binary
     *           operation with.
     * @return the result of the iadd.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _iadd(ThreadState ts, PyObject o2) {
        return inplaceBinOp(ts, iadd, add, radd, o2);
    }

    public final PyObject inplaceBinOp(ThreadState ts, InvokeByName inplaceOp, InvokeByName op, InvokeByName rop, PyObject value) {
        try {
            Object func = inplaceOp.getGetter().invokeExact(this);
            PyObject ret = (PyObject) inplaceOp.getInvoker().invokeExact(func, ts, value);
            if (ret != Py.NotImplemented) {
                return ret;
            }
        } catch (PyException e) {
            if (!e.match(Py.AttributeError)) {
                throw e;
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        return binOp(ts, op, rop, value);
    }

    public final PyObject binOp(ThreadState ts, InvokeByName op, InvokeByName rop, PyObject value) {
        try {
            Object func = op.getGetter().invokeExact(this);
            PyObject ret = (PyObject) op.getInvoker().invokeExact(func, ts, value);
            if (ret != null && ret != Py.NotImplemented) {
                return ret;
            }
            func = rop.getGetter().invokeExact(value);
            return (PyObject) rop.getInvoker().invokeExact(func, ts, this);
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw Py.JavaError(t);
        }
    }

    public static PyObject unaryOp(ThreadState ts, InvokeByName op, PyObject self, Consumer<PyObject> attrErrorHandle) {
        try {
            Object func = op.getGetter().invokeExact(self);
            return (PyObject) op.getInvoker().invokeExact(func, ts);
        } catch (PyException e) {
            if (e.match(Py.AttributeError)) {
                attrErrorHandle.accept(self);
            }
            throw e;
        } catch (Throwable t) {
            throw Py.JavaError(t);
        }
    }

    public final PyObject unaryOp(ThreadState ts, InvokeByName op) {
        return unaryOp(ts, op, this, (self) -> {});
    }

    /**
     * Implements the Python expression <code>this - o2</code>
     *
     * @param o2 the object to perform this binary operation with.
     * @return the result of the sub.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _sub(ThreadState ts, PyObject o2) {
        return binOp(ts, sub, rsub, o2);
    }

    /**
     * Implements the Python expression <code>this -= o2</code>
     *
     * @param o2 the object to perform this inplace binary
     *           operation with.
     * @return the result of the isub.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _isub(ThreadState ts, PyObject o2) {
        return inplaceBinOp(ts, isub, sub, rsub, o2);
    }

    /**
     * Implements the Python expression <code>this * o2</code>.
     *
     * @param o2 the object to perform this binary operation with.
     * @return the result of the mul.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _mul(ThreadState ts, PyObject o2) {
        return binOp(ts, mul, rmul, o2);
    }

    /**
     * Implements the Python expression <code>this *= o2</code>.
     *
     * @param o2 the object to perform this inplace binary
     *           operation with.
     * @return the result of the imul.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _imul(ThreadState ts, PyObject o2) {
        return inplaceBinOp(ts, imul, mul, rmul, o2);
    }

    /**
     * Implements the Python expression <code>this * o2</code>.
     *
     * @param o2 the object to perform this binary operation with.
     * @return the result of the matmul.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _matmul(ThreadState ts, PyObject o2) {
        return binOp(ts, matmul, rmatmul, o2);
    }

    /**
     * Implements the Python expression <code>this @= o2</code>.
     *
     * @param o2 the object to perform this inplace binary
     *           operation with.
     * @return the result of the imatmul.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _imatmul(ThreadState ts, PyObject o2) {
        return inplaceBinOp(ts, imatmul, matmul, rmatmul, o2);
    }

    /**
     * Implements the Python expression <code>this // o2</code>
     *
     * @param o2 the object to perform this binary operation with.
     * @return the result of the floordiv.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _floordiv(ThreadState ts, PyObject o2) {
        return binOp(ts, floordiv, rfloordiv, o2);
    }

    /**
     * Implements the Python expression <code>this //= o2</code>
     *
     * @param o2 the object to perform this inplace binary
     *           operation with.
     * @return the result of the ifloordiv.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _ifloordiv(ThreadState ts, PyObject o2) {
        return inplaceBinOp(ts, ifloordiv, floordiv, rfloordiv, o2);
    }

    /**
     * Implements the Python expression <code>this / o2</code>
     *
     * @param o2 the object to perform this binary operation with.
     * @return the result of the truediv.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _truediv(ThreadState ts, PyObject o2) {
        return binOp(ts, truediv, rtruediv, o2);
    }

    /**
     * Implements the Python expression <code>this /= o2</code>
     *
     * @param o2 the object to perform this inplace binary
     *           operation with.
     * @return the result of the itruediv.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _itruediv(ThreadState ts, PyObject o2) {
        return inplaceBinOp(ts, itruediv, truediv, rtruediv, o2);
    }

    /**
     * Implements the Python expression <code>this % o2</code>
     *
     * @param o2 the object to perform this binary operation with.
     * @return the result of the mod.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _mod(ThreadState ts, PyObject o2) {
        return binOp(ts, mod, rmod, o2);
    }

    /**
     * Implements the Python expression <code>this %= o2</code>
     *
     * @param o2 the object to perform this inplace binary
     *           operation with.
     * @return the result of the imod.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _imod(ThreadState ts, PyObject o2) {
        return inplaceBinOp(ts, imod, mod, rmod, o2);
    }

    /**
     * Implements the Python expression <code>this divmod o2</code>
     *
     * @param o2 the object to perform this binary operation with.
     * @return the result of the divmod.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _divmod(ThreadState ts, PyObject o2) {
        return binOp(ts, divmod, rdivmod, o2);
    }

    /**
     * Implements the Python expression <code>this ** o2</code>
     *
     * @param o2 the object to perform this binary operation with.
     * @return the result of the pow.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _pow(ThreadState ts, PyObject o2) {
        return binOp(ts, pow, rpow, o2);
    }

    /**
     * Implements the Python expression <code>this **= o2</code>
     *
     * @param o2 the object to perform this inplace binary
     *           operation with.
     * @return the result of the ipow.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _ipow(ThreadState ts, PyObject o2) {
        return inplaceBinOp(ts, ipow, pow, rpow, o2);
    }

    /**
     * Implements the Python expression <code>this << o2</code>
     *
     * @param o2 the object to perform this binary operation with.
     * @return the result of the lshift.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _lshift(ThreadState ts, PyObject o2) {
        return binOp(ts, lshift, rlshift, o2);
    }

    /**
     * Implements the Python expression <code>this <<= o2</code>
     *
     * @param o2 the object to perform this inplace binary
     *           operation with.
     * @return the result of the ilshift.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _ilshift(ThreadState ts, PyObject o2) {
        return inplaceBinOp(ts, ilshift, lshift, rlshift, o2);
    }

    /**
     * Implements the Python expression <code>this >> o2</code>
     *
     * @param o2 the object to perform this binary operation with.
     * @return the result of the rshift.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _rshift(ThreadState ts, PyObject o2) {
        return binOp(ts, rshift, rrshift, o2);
    }

    /**
     * Implements the Python expression <code>this >>= o2</code>
     *
     * @param o2 the object to perform this inplace binary
     *           operation with.
     * @return the result of the irshift.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _irshift(ThreadState ts, PyObject o2) {
        return inplaceBinOp(ts, irshift, rshift, rrshift, o2);
    }

    /**
     * Implements the Python expression <code>this & o2</code>
     *
     * @param o2 the object to perform this binary operation with.
     * @return the result of the and.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _and(ThreadState ts, PyObject o2) {
        return binOp(ts, and, rand, o2);
    }

    /**
     * Implements the Python expression <code>this &= o2</code>
     *
     * @param o2 the object to perform this inplace binary
     *           operation with.
     * @return the result of the iand.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _iand(ThreadState ts, PyObject o2) {
        return inplaceBinOp(ts, iand, and, rand, o2);
    }

    /**
     * Implements the Python expression <code>this | o2</code>
     *
     * @param o2 the object to perform this binary operation with.
     * @return the result of the or.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _or(ThreadState ts, PyObject o2) {
        return binOp(ts, or, ror, o2);
    }

    /**
     * Implements the Python expression <code>this |= o2</code>
     *
     * @param o2 the object to perform this inplace binary
     *           operation with.
     * @return the result of the ior.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _ior(ThreadState ts, PyObject o2) {
        return inplaceBinOp(ts, ior, or, ror, o2);
    }

    /**
     * Implements the Python expression <code>this ^ o2</code>
     *
     * @param o2 the object to perform this binary operation with.
     * @return the result of the xor.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _xor(ThreadState ts, PyObject o2) {
        return binOp(ts, xor, rxor, o2);
    }


    /**
     * Implements the Python expression <code>this ^= o2</code>
     *
     * @param o2 the object to perform this inplace binary
     *           operation with.
     * @return the result of the ixor.
     * @throws Py.TypeError if this operation can't be performed
     *                      with these operands.
     **/
    public final PyObject _ixor(ThreadState ts, PyObject o2) {
        return inplaceBinOp(ts, ixor, xor, rxor, o2);
    }

    /**
     * A convenience function for PyProxys.
     */
    public PyObject _jcallexc(Object[] args) throws Throwable {
        try {
            return __call__(Py.javas2pys(args));
        } catch (PyException e) {
            if (e.value.getJavaProxy() != null) {
                Object t = e.value.__tojava__(Throwable.class);
                if (t != null && t != Py.NoConversion) {
                    throw (Throwable) t;
                }
            } else {
                ThreadState ts = Py.getThreadState();
                if (ts.frame == null) {
                    Py.maybeSystemExit(e);
                }
                if (Options.showPythonProxyExceptions) {
                    Py.stderr.println(
                            "Exception in Python proxy returning to Java:");
                    Py.printException(e);
                }
            }
            throw e;
        }
    }

    public void _jthrow(Throwable t) {
        if (t instanceof RuntimeException)
            throw (RuntimeException) t;
        if (t instanceof Error)
            throw (Error) t;
        throw Py.JavaError(t);
    }

    public PyObject _jcall(Object[] args) {
        try {
            return _jcallexc(args);
        } catch (Throwable t) {
            _jthrow(t);
            return null;
        }
    }

    /**
     * Shortcut for calling a method on a PyObject from Java.
     * This form is equivalent to o.__getattr__(name).__call__(args, keywords)
     *
     * @param name     the name of the method to call.  This must be an
     *                 interned string!
     * @param args     an array of the arguments to the call.
     * @param keywords the keywords to use in the call.
     * @return the result of calling the method name with args and keywords.
     **/
    public PyObject invoke(String name, PyObject[] args, String... keywords) {
        PyObject f = __getattr__(name);
        return f.__call__(args, keywords);
    }

    public PyObject invoke(String name, PyObject... args) {
        PyObject f = __getattr__(name);
        return f.__call__(args);
    }

    /* descriptors and lookup protocols */

    /**
     * Shortcut for calling a method on a PyObject with no args.
     *
     * @param name the name of the method to call.  This must be an
     *             interned string!
     * @return the result of calling the method name with no args
     **/
    public PyObject invoke(String name) {
        PyObject f = __getattr__(name);
        return f.__call__();
    }

    /**
     * Shortcut for calling a method on a PyObject with one arg.
     *
     * @param name the name of the method to call.  This must be an
     *             interned string!
     * @param arg1 the one argument of the method.
     * @return the result of calling the method name with arg1
     **/
    public PyObject invoke(String name, PyObject arg1) {
        PyObject f = __getattr__(name);
        return f.__call__(arg1);
    }

    /**
     * Shortcut for calling a method on a PyObject with two args.
     *
     * @param name the name of the method to call.  This must be an
     *             interned string!
     * @param arg1 the first argument of the method.
     * @param arg2 the second argument of the method.
     * @return the result of calling the method name with arg1 and arg2
     **/
    public PyObject invoke(String name, PyObject arg1, PyObject arg2) {
        PyObject f = __getattr__(name);
        return f.__call__(arg1, arg2);
    }

    /**
     * Shortcut for calling a method on a PyObject with one extra
     * initial argument.
     *
     * @param name     the name of the method to call.  This must be an
     *                 interned string!
     * @param arg1     the first argument of the method.
     * @param args     an array of the arguments to the call.
     * @param keywords the keywords to use in the call.
     * @return the result of calling the method name with arg1 args
     * and keywords
     **/
    public PyObject invoke(String name, PyObject arg1, PyObject[] args, String[] keywords) {
        PyObject f = __getattr__(name);
        return f.__call__(arg1, args, keywords);
    }

    /**
     * xxx implements where meaningful
     *
     * @return internal object per instance dict or null
     */
    public PyObject fastGetDict() {
        return null;
    }

    /**
     * xxx implements where meaningful
     *
     * @return internal object __dict__ or null
     */
    public PyObject getDict() {
        return null;
    }

    public void setDict(PyObject newDict) {
        // fallback if setDict not implemented in subclass
        throw Py.TypeError("can't set attribute '__dict__' of instance of " + getType().fastGetName());
    }

    public void delDict() {
        // fallback to error
        throw Py.TypeError("can't delete attribute '__dict__' of instance of '" + getType().fastGetName() + "'");
    }

    public boolean implementsDescrGet() {
        return objtype.hasGet;
    }

    public boolean implementsDescrSet() {
        return objtype.hasSet;
    }

    public boolean implementsDescrDelete() {
        return objtype.hasDelete;
    }

    public boolean isDataDescr() {
        return objtype.hasSet || objtype.hasDelete;
    }

    /**
     * Get descriptor for this PyObject.
     *
     * @param obj  -
     *             the instance accessing this descriptor. Can be null if this is
     *             being accessed by a type.
     * @param type -
     *             the type accessing this descriptor. Will be null if obj exists
     *             as obj is of the type accessing the descriptor.
     * @return - the object defined for this descriptor for the given obj and
     * type.
     */
    public PyObject __get__(PyObject obj, PyObject type) {
        return _doget(obj, type);
    }

    public void __set__(PyObject obj, PyObject value) {
        if (!_doset(obj, value)) {
            throw Py.AttributeError("object internal __set__ impl is abstract");
        }
    }

    public void __delete__(PyObject obj) {
        throw Py.AttributeError("object internal __delete__ impl is abstract");
    }

    @ExposedMethod(doc = BuiltinDocs.object___getattribute___doc)
    final PyObject object___getattribute__(PyObject arg0) {
        String name = asName(arg0);
        PyObject ret = object___findattr__(name);
        if (ret == null) {
            PyObject __getattr = object___findattr__("__getattr__");
            if (__getattr != null) {
                return __getattr.__call__(arg0);
            }
            ret = __findattr_ex__(name);
            if (ret == null) {
                if (name.equals("__cause__") || name.equals("__context__") || name.equals("__suppress_context__")) {
                    return Py.None;
                }
                noAttributeError(name);
            }
        }
        return ret;
    }

    // name must be interned
    final PyObject object___findattr__(String name) {
        PyObject descr = objtype.lookup(name);
        PyObject res;
        boolean get = false;

        if (descr != null) {
            get = descr.implementsDescrGet();
            if (get && descr.isDataDescr()) {
                return descr.__get__(this, objtype);
            }
        }

        PyObject obj_dict = fastGetDict();
        if (obj_dict != null) {
            res = obj_dict.__finditem__(name);
            if (res != null) {
                return res;
            }
        }

        if (get) {
            return descr.__get__(this, objtype);
        }

        if (descr != null) {
            return descr;
        }

        return null;
    }

    @ExposedMethod(doc = BuiltinDocs.object___setattr___doc)
    final void object___setattr__(PyObject name, PyObject value) {
        hackCheck("__setattr__");
        object___setattr__(asName(name), value);
    }

    final void object___setattr__(String name, PyObject value) {
        PyObject descr = objtype.lookup(name);
        boolean set = false;

        if (descr != null) {
            set = descr.implementsDescrSet();
            if (set && descr.isDataDescr()) {
                descr.__set__(this, value);
                return;
            }
        }

        PyObject obj_dict = fastGetDict();
        if (obj_dict != null) {
            obj_dict.__setitem__(name, value);
            return;
        }

        if (set) {
            descr.__set__(this, value);
        }

        if (descr != null) {
            readonlyAttributeError(name);
        }

        noAttributeError(name);
    }

    @ExposedMethod(doc = BuiltinDocs.object___delattr___doc)
    final void object___delattr__(PyObject name) {
        hackCheck("__delattr__");
        object___delattr__(asName(name));
    }

    final void object___delattr__(String name) {
        PyObject descr = objtype.lookup(name);
        boolean delete = false;

        if (descr != null) {
            delete = descr.implementsDescrDelete();
            if (delete && descr.isDataDescr()) {
                descr.__delete__(this);
                return;
            }
        }

        PyObject obj_dict = fastGetDict();
        if (obj_dict != null) {
            try {
                obj_dict.__delitem__(name);
            } catch (PyException exc) {
                if (exc.match(Py.KeyError)) {
                    noAttributeError(name);
                } else {
                    throw exc;
                }
            }
            return;
        }

        if (delete) {
            descr.__delete__(this);
        }

        if (descr != null) {
            readonlyAttributeError(name);
        }

        noAttributeError(name);
    }

    /**
     * Helper to check for object.__setattr__ or __delattr__ applied to a type (The Carlo
     * Verre hack).
     *
     * @param what String method name to check for
     */
    private void hackCheck(String what) {
        if (this instanceof PyType && ((PyType) this).builtin) {
            throw Py.TypeError(String.format("can't apply this %s to %s object", what,
                    objtype.fastGetName()));
        }
    }

    /**
     * A common helper method, use to prevent infinite recursion
     * when a Python object implements __reduce__ and sometimes calls
     * object.__reduce__. Trying to do it all in __reduce__ex__ caused
     * # this problem. See http://bugs.jython.org/issue2323.
     */
    private PyObject commonReduce(int proto) {
        PyObject res;

        if (proto >= 2) {
            res = reduce_2();
        } else {
            PyObject copyreg = Import.importModuleLevel("copyreg", null, Py.EmptyTuple, 0);
            PyObject copyreg_reduce = copyreg.__findattr__("_reduce_ex");
            res = copyreg_reduce.__call__(this, new PyLong(proto));
        }
        return res;
    }

    /**
     * Used for pickling.  Default implementation calls object___reduce__.
     *
     * @return a tuple of (class, tuple)
     */
    public PyObject __reduce__() {
        return object___reduce__(0);
    }

    @ExposedMethod(doc = BuiltinDocs.object___reduce___doc)
    public final PyObject object___reduce__(int proto) {
        return commonReduce(proto);
    }

    /**
     * Used for pickling.  If the subclass specifies __reduce__, it will
     * override __reduce_ex__ in the base-class, even if __reduce_ex__ was
     * called with an argument.
     *
     * @param arg int specifying reduce algorithm (method without this
     *            argument defaults to 0).
     * @return a tuple of (class, tuple)
     */
    public PyObject __reduce_ex__(int arg) {
        return object___reduce_ex__(arg);
    }

    public PyObject __reduce_ex__() {
        return object___reduce_ex__(0);
    }

    @ExposedMethod(defaults = "0", doc = BuiltinDocs.object___reduce___doc)
    final PyObject object___reduce_ex__(int arg) {
        PyObject res;

        PyObject clsreduce = this.getType().__findattr__("__reduce__");
        PyObject objreduce = (new PyObject()).getType().__findattr__("__reduce__");

        if (clsreduce != objreduce) {
            res = this.__reduce__();
        } else {
            res = commonReduce(arg);
        }
        return res;
    }

    private PyObject reduce_2() {
        PyObject args, state;
        PyObject res = null;
        int n, i;

        PyObject cls = this.__findattr__("__class__");

        PyObject getnewargs = this.__findattr__("__getnewargs__");
        if (null != getnewargs) {
            args = getnewargs.__call__();
            if (null != args && !(args instanceof PyTuple)) {
                throw Py.TypeError("__getnewargs__ should return a tuple");
            }
        } else {
            args = Py.EmptyTuple;
        }

        PyObject getstate = this.__findattr__("__getstate__");
        if (null != getstate) {
            state = getstate.__call__();
            if (null == state) {
                return res;
            }
        } else {
            state = this.__findattr__("__dict__");
            if (null == state) {
                state = Py.None;
            }

            PyObject names = slotnames(cls);
            if (null == names) {
                return res;
            }

            if (names != Py.None) {
                if (!(names instanceof PyList)) {
                    throw Py.AssertionError("slots not a list");
                }
                PyObject slots = new PyDictionary();

                n = 0;
                for (i = 0; i < ((PyList) names).size(); i++) {
                    PyObject name = ((PyList) names).pyget(i);
                    PyObject value = this.__findattr__(name.toString());
                    if (null == value) {
                        // do nothing
                    } else {
                        slots.__setitem__(name, value);
                        n++;
                    }
                }
                if (n > 0) {
                    state = new PyTuple(state, slots);
                }
            }
        }
        PyObject listitems;
        PyObject dictitems;
        if (!(this instanceof PyList)) {
            listitems = Py.None;
        } else {
            listitems = PyObject.getIter(this);
        }
        if (!(this instanceof PyDictionary)) {
            dictitems = Py.None;
        } else {
            dictitems = invoke("iteritems");
        }

        PyObject copyreg = Import.importModuleLevel("copyreg", null, Py.EmptyTuple, 0);
        PyObject newobj = copyreg.__findattr__("__newobj__");

        n = ((PyTuple) args).size();
        PyObject args2[] = new PyObject[n + 1];
        args2[0] = cls;
        for (i = 0; i < n; i++) {
            args2[i + 1] = ((PyTuple) args).pyget(i);
        }

        return new PyTuple(newobj, new PyTuple(args2), state, listitems, dictitems);
    }

    public PyTuple __getnewargs__() {
        // default is empty tuple
        return new PyTuple();
    }

    /* arguments' conversion helpers */

    public String asString(int index) throws ConversionException {
        throw new ConversionException(index);
    }

    public String asString() {
        throw Py.TypeError("expected a str");
    }

    public String asStringOrNull(int index) throws ConversionException {
        return asString(index);
    }

    public String asStringOrNull() {
        return asString();
    }

    // TODO - remove when all asName users are moved to the @Exposed annotation
    public String asName(int index) throws ConversionException {
        throw new ConversionException(index);
    }

    // TODO - remove when all generated users are migrated to @Exposed and asInt()
    public int asInt(int index) throws ConversionException {
        throw new ConversionException(index);
    }

    /**
     * Convert this object into an int. Throws a PyException on failure.
     *
     * @return an int value
     */
    public int asInt() {
        PyObject intObj;
        try {
            intObj = Abstract.PyNumber_Long(Py.getThreadState(), this);
        } catch (PyException pye) {
            if (pye.match(Py.AttributeError)) {
                throw Py.TypeError("an integer is required");
            }
            throw pye;
        }
        if (!(intObj instanceof PyLong)) {
            // Shouldn't happen except with buggy builtin types
            throw Py.TypeError("nb_int should return int object");
        }
        return intObj.asInt();
    }

    public long asLong(int index) throws ConversionException {
        throw new ConversionException(index);
    }

    /**
     * Convert this object longo an long. Throws a PyException on failure.
     *
     * @return an long value
     */
    public long asLong() {
        PyObject longObj;
        try {
            longObj = Abstract.PyNumber_Long(Py.getThreadState(), this);
        } catch (PyException pye) {
            if (pye.match(Py.AttributeError)) {
                throw Py.TypeError("an integer is required");
            }
            throw pye;
        }
        if (!(longObj instanceof PyLong)) {
            // Shouldn't happen except with buggy builtin types
            throw Py.TypeError("integer conversion failed");
        }
        return longObj.asLong();
    }

    /**
     * Convert this object into a double. Throws a PyException on failure.
     *
     * @return a double value
     */
    public double asDouble() {
        return Abstract.PyNumber_Float(Py.getThreadState(),this).asDouble();
    }

    /**
     * Convert this object into an index-sized integer. Throws a PyException on failure.
     *
     * @return an index-sized int
     */
    public int asIndex() {
        return asIndex(null);
    }

    /**
     * Convert this object into an index-sized integer.
     * <p>
     * Throws a Python exception on Overflow if specified an exception type for err.
     *
     * @param err the Python exception to raise on OverflowErrors
     * @return an index-sized int
     */
    public int asIndex(PyObject err) {
        // OverflowErrors are handled in PyLong.asIndex
        return __index__().asInt();
    }

    public static class ConversionException extends Exception {

        public int index;

        public ConversionException(int index) {
            this.index = index;
        }

    }
}

