// Copyright (c) Corporation for National Research Initiatives
package org.python.core;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;
import org.python.internal.lookup.MethodHandleFactory;
import org.python.internal.lookup.MethodHandleFunctionality;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.SwitchPoint;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * A Python method.
 */
@ExposedType(name = "method", isBaseType = false, doc = BuiltinDocs.method_doc)
public class PyMethod extends PyObject implements DynLinkable, InvocationHandler, Traverseproc {
    static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();
    static final MethodHandleFunctionality MH = MethodHandleFactory.getFunctionality();

    public static final PyType TYPE = PyType.fromClass(PyMethod.class);

    /** The class associated with a method. */
    public PyObject im_class;

    /** The function (or other callable) implementing a method */
    @ExposedGet(doc = BuiltinDocs.method___func___doc)
    public PyObject __func__;

    /** The instance to which a method is bound; None for unbound methods */
    @ExposedGet(doc = BuiltinDocs.method___self___doc)
    public PyObject __self__;

    public PyMethod(PyObject function, PyObject self, PyObject type) {
        super(TYPE);
        if (self == Py.None){
            self = null;
        }
        if (type == Py.None || type == null) {
            if (self == null) {
                throw Py.TypeError("unbound methods must have non-NULL im_class");
            }
            type = self.getType();
        }
        __func__ = function;
        __self__ = self;
        im_class = type;
    }

    @ExposedNew
    static final PyObject method___new__(PyNewWrapper new_, boolean init, PyType subtype,
                                                PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("method", args, keywords, "func");
        ap.noKeywords();
        PyObject func = ap.getPyObject(0);
        PyObject self = ap.getPyObject(1);
        PyObject classObj = ap.getPyObject(2, null);

        if (!func.isCallable()) {
            throw Py.TypeError("first argument must be callable");
        }
        return new PyMethod(func, self, classObj);
    }

    @Override
    public PyObject __findattr_ex__(String name) {
        return method___findattr_ex__(name);
    }
 
    final PyObject method___findattr_ex__(String name) {
        PyObject ret = super.__findattr_ex__(name);
        if (ret != null) {
            return ret;
        }
        return __func__.__findattr_ex__(name);
    }
    
    @ExposedSlot(SlotFunc.GETATTRO)
    public static PyObject getattro(PyObject method, String name) {
        PyMethod self = (PyMethod) method;
        PyObject ret = self.method___findattr_ex__(name);
        if (ret == null) {
            self.noAttributeError(name);
        }
        return ret;
    }

    @Override
    public PyObject __get__(PyObject obj, PyObject type) {
        return method___get__(obj, type);
    }

    @ExposedMethod(defaults = "null", doc = BuiltinDocs.method___get___doc)
    final PyObject method___get__(PyObject obj, PyObject type) {
        // Only if classes are compatible
        if (obj == null || __self__ != null) {
            return this;
        } else if (Py.isSubClass(obj.fastGetClass(), im_class)) {
            return new PyMethod(__func__, obj, im_class);
        } else {
            return this;
        }
    }

    @Override
    public PyObject __call__() {
        return __call__(Py.getThreadState());
    }
    
    @Override
    public PyObject __call__(ThreadState state) {
        PyObject self = checkSelf(null, null);
        if (self == null) {
            return __func__.__call__(state);
        } else {
            return __func__.__call__(state, self);
        }
    }
    
    @Override
    public PyObject __call__(PyObject arg0) {
        return __call__(Py.getThreadState(), arg0);
    }
    
    @Override
    public PyObject __call__(ThreadState state, PyObject arg0) {
        PyObject self = checkSelf(arg0, null);
        if (self == null) {
            return __func__.__call__(state, arg0);
        } else {
            return __func__.__call__(state, self, arg0);
        }
    }
    
    @Override
    public PyObject __call__(PyObject arg0, PyObject arg1) {
        return __call__(Py.getThreadState(), arg0, arg1);
    }
    
    @Override
    public PyObject __call__(ThreadState state, PyObject arg0, PyObject arg1) {
        PyObject self = checkSelf(arg0, null);
        if (self == null) {
            return __func__.__call__(state, arg0, arg1);
        } else {
            return __func__.__call__(state, self, arg0, arg1);
        }
    }
    
    @Override
    public PyObject __call__(PyObject arg0, PyObject arg1, PyObject arg2) {
        return __call__(Py.getThreadState(), arg0, arg1, arg2);
    }
    
    @Override
    public PyObject __call__(ThreadState state, PyObject arg0, PyObject arg1, PyObject arg2) {
        PyObject self = checkSelf(arg0, null);
        if (self == null) {
            return __func__.__call__(state, arg0, arg1, arg2);
        } else {
            return __func__.__call__(state, self, arg0, arg1, arg2);
        }
    }
    
    @Override
    public PyObject __call__(PyObject arg0, PyObject arg1, PyObject arg2, PyObject arg3) {
        return __call__(Py.getThreadState(), arg0, arg1, arg2, arg3);
    }
    
    @Override
    public PyObject __call__(ThreadState state, PyObject arg0, PyObject arg1, PyObject arg2,
                             PyObject arg3) {
        PyObject self = checkSelf(arg0, null);
        if (self == null) {
            return __func__.__call__(state, arg0, arg1, arg2, arg3);
        } else {
            return __func__.__call__(state, self, new PyObject[]{arg0, arg1, arg2, arg3},
                                    Py.NoKeywords);
        }
    }
    
    @Override
    public PyObject __call__(PyObject arg1, PyObject[] args, String[] keywords) {
        return __call__(Py.getThreadState(), arg1, args, keywords);
    }
    
    @Override
    public PyObject __call__(ThreadState state, PyObject arg1, PyObject[] args,
                             String[] keywords) {
        PyObject self = checkSelf(arg1, args);
        if (self == null) {
            return __func__.__call__(state, arg1, args, keywords);
        } else {
            PyObject[] newArgs = new PyObject[args.length + 1];
            System.arraycopy(args, 0, newArgs, 1, args.length);
            newArgs[0] = arg1;
            return __func__.__call__(state, self, newArgs, keywords);
        }
    }
    
    @Override
    public PyObject __call__(PyObject[] args) {
        return __call__(Py.getThreadState(), args);
    }
    
    @Override
    public PyObject __call__(ThreadState state, PyObject[] args) {
        return __call__(state, args, Py.NoKeywords);
    }

    @Override
    public PyObject __call__(PyObject[] args, String[] keywords) {
        return __call__(Py.getThreadState(), args, keywords);
    }
    
    @Override
    public PyObject __call__(ThreadState state, PyObject[] args, String[] keywords) {
        return method___call__(state, args, keywords);
    }

    @ExposedMethod(doc = BuiltinDocs.method___call___doc)
    final PyObject method___call__(ThreadState state, PyObject[] args, String[] keywords) {
        PyObject self = checkSelf(null, args);
        if (self == null) {
            return __func__.__call__(state, args, keywords);
        } else {
            return __func__.__call__(state, self, args, keywords);
        }
    }

    public GuardedInvocation findCallMethod(final CallSiteDescriptor desc, LinkRequest request) {
        if (__func__ instanceof PyFunction) {
            return ((PyFunction) __func__).findCallMethod(desc, request, __self__);
        }

        // FIXME fix dynamically linking reflected Java method
        MethodHandle mh = MH.findVirtual(LOOKUP, getClass(), "__call__", desc.getMethodType().dropParameterTypes(0, 1));
        return new GuardedInvocation(mh, null, new SwitchPoint[0], ClassCastException.class);
    }

    private PyObject checkSelf(PyObject arg, PyObject[] args) {
        PyObject self = __self__;
        if (self != null) {
            return self;
        }
        // Unbound methods must be called with an instance of the
        // class (or a derived class) as first argument
        if (arg != null) {
            self = arg;
        } else if (args != null && args.length >= 1) {
            self = args[0];
        }

        boolean ok = self != null && Py.isInstance(self, im_class);
        if (!ok) {
            // XXX: Need equiv. of PyEval_GetFuncDesc instead of
            // hardcoding "()"
            String msg = String.format("unbound method %s%s must be called with %s instance as "
                            + "first argument (got %s%s instead)",
                    getFuncName(), "()",
                    getClassName(im_class), getInstClassName(self),
                    self == null ? "" : " instance");
            throw Py.TypeError(msg);
        }
        return null;
    }

    @Override
    public int hashCode() {
        int hashCode = __self__ == null ? Py.None.hashCode() : __self__.hashCode();
        return hashCode ^ __func__.hashCode();
    }

    @ExposedGet(name = "__doc__")
    public PyObject getDoc() {
        return __func__.__getattr__("__doc__");
    }

    // FIXME this should work, but it has a dependency on our descriptor mechanism!
//    @ExposedSet(name = "__doc__")
//    public void setDoc() {
//        throw Py.AttributeError("attribute '__doc__' of 'method' objects is not writable");
//    }

    @Override
    public String toString() {
        String className = "?";
        if (im_class != null) {
            className = getClassName(im_class);
        }
        if (__self__ == null) {
            return String.format("<unbound method %s.%s>", className, getFuncName());
        } else {
            return String.format("<bound method %s.%s of %s>", className, getFuncName(),
                                 __self__);
        }
    }

    private String getClassName(PyObject cls) {
        return ((PyType)cls).fastGetName();
    }

    private String getInstClassName(PyObject inst) {
        if (inst == null) {
            return "nothing";
        }
        PyObject classObj = inst.__findattr__("im_class");
        if (classObj == null) {
            classObj = inst.getType();
        }
        return getClassName(classObj);
    }

    private String getFuncName() {
        PyObject funcName = null;
        try {
            funcName = __func__.__findattr__("__name__");
        } catch (PyException pye) {
            // continue
        }
        if (funcName == null) {
            return "?";
        }
        return funcName.toString();
    }

    @Override
    public Object __tojava__(Class<?> c) {
        // Automatically coerce to single method interfaces
        if (__self__ == null) {
            return super.__tojava__(c); // not a bound method, so no special handling
        }
        if (c.isInstance(this) && c != InvocationHandler.class) {
            // for base types, conversion is simple - so don't wrap!
            // InvocationHandler is special, since it's a single method interface
            // that we implement, but if we coerce to it we want the arguments
            return c.cast( this );
        } else if (c.isInterface()) {
            if (c.getDeclaredMethods().length == 1 && c.getInterfaces().length == 0) {
                // Proper single method interface
                return proxy(c);
            } else {
                // Try coerce to interface with multiple overloaded versions of
                // the same method (name)
                String name = null;
                for (Method method : c.getMethods()) {
                    if (method.getDeclaringClass() != Object.class) {
                        if (name == null || name.equals(method.getName())) {
                            name = method.getName();
                        } else {
                            name = null;
                            break;
                        }
                    }
                }
                if (name != null) { // single unique method name
                    return proxy(c);
                }
            }
        }
        return super.__tojava__(c);
    }

    private Object proxy( Class<?> c ) {
        return Proxy.newProxyInstance(c.getClassLoader(), new Class[]{c}, this);
    }

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
        // Handle invocation when invoked through Proxy (as coerced to single method interface)
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke( this, args );
        } else if (args == null || args.length == 0) {
            return __call__().__tojava__(method.getReturnType());
        } else {
            return __call__(Py.javas2pys(args)).__tojava__(method.getReturnType());
        }
    }


    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        int retVal;
        if (im_class != null) {
            retVal = visit.visit(im_class, arg);
            if (retVal != 0) {
                return retVal;
            }
        }
        if (__func__ != null) {
            retVal = visit.visit(__func__, arg);
            if (retVal != 0) {
                return retVal;
            }
        }
        return __self__ == null ? 0 : visit.visit(__self__, arg);
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        return ob != null && (ob == im_class || ob == __func__ || ob == __self__);
    }
}
