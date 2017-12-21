package org.python.core.linker;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.DynamicLinker;
import jdk.dynalink.DynamicLinkerFactory;
import jdk.dynalink.Operation;
import jdk.dynalink.StandardOperation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.support.SimpleRelinkableCallSite;
import org.objectweb.asm.MethodVisitor;
import org.python.compiler.Code;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static jdk.dynalink.StandardNamespace.ELEMENT;
import static jdk.dynalink.StandardNamespace.METHOD;
import static jdk.dynalink.StandardNamespace.PROPERTY;
import static jdk.dynalink.StandardOperation.GET;
import static jdk.dynalink.StandardOperation.SET;
import static org.python.util.CodegenUtils.p;
import static org.python.util.CodegenUtils.sig;

/**
 * Bootstrap methods for invokedynamic
 */
public class Bootstrap {
    public static final Call BOOTSTRAP = staticCallNoLookup(Bootstrap.class, "bootstrap", CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class, int.class);
    private static final GuardingDynamicLinker DYNA_PYTHON_LINKER = new DynaPythonLinker();
    private static final GuardingDynamicLinker PYOBJ_LINKER = new PyObjectLinker();

    private static final DynamicLinker dynamicLinker = createDynamicLinker();

    /** Property getter operation {@code obj.prop} */
    public static final int GET_PROPERTY        = 0;
    /** Element getter operation {@code obj[index]} */
    public static final int GET_ELEMENT         = 1;
    /** Property getter operation, subsequently invoked {@code obj.prop()} */
    public static final int GET_METHOD_PROPERTY = 2;
    /** Element getter operation, subsequently invoked {@code obj[index]()} */
    public static final int GET_METHOD_ELEMENT  = 3;
    /** Property setter operation {@code obj.prop = value} */
    public static final int SET_PROPERTY        = 4;
    /** Element setter operation {@code obj[index] = value} */
    public static final int SET_ELEMENT         = 5;
    /** Call operation {@code fn(args...)} */
    public static final int CALL                = 6;
    private static final int OPERATION_MASK = 6;

    // Correspond to the operation indices above.
    private static final Operation[] OPERATIONS = new Operation[] {
        GET.withNamespace(PROPERTY),
        GET.withNamespace(ELEMENT),
        GET.withNamespaces(METHOD, PROPERTY, ELEMENT),
        GET.withNamespaces(METHOD, ELEMENT, PROPERTY),
        SET.withNamespaces(PROPERTY, ELEMENT),
        SET.withNamespaces(ELEMENT, PROPERTY),
        StandardOperation.CALL,
    };


    private static DynamicLinker createDynamicLinker() {
         final DynamicLinkerFactory factory = new DynamicLinkerFactory();
         factory.setPrioritizedLinkers(DYNA_PYTHON_LINKER, PYOBJ_LINKER);
         return factory.createLinker();
     }

    public static CallSite bootstrap(MethodHandles.Lookup lookup, String name, MethodType type, int flags) {
        return dynamicLinker.link(
                new SimpleRelinkableCallSite(
                        new CallSiteDescriptor(lookup, parseOperation(name, flags), type)
                )
        );
    }

    private static Operation parseOperation(String name, int flags) {
        return OPERATIONS[flags].named(name);
    }

    public static Call staticCallNoLookup(final Class<?> clazz, final String name, final Class<?> rtype, final Class<?> ...ptypes) {
        return staticCallNoLookup(p(clazz), name, sig(rtype, ptypes));
    }

    public static Call staticCallNoLookup(final String className, final String name, final String descriptor) {
        return new Call(null, className, name, descriptor) {
            @Override
            protected void invoke(Code code) {
                code.invokestatic(className, name, descriptor);
            }
        };
    }

    public static abstract class Access {

        protected final MethodHandle methodHandle;
        protected final String className;
        protected final String name;
        protected final String descriptor;

        protected Access(MethodHandle methodHandle, String className, String name, String descriptor) {
            this.methodHandle = methodHandle;
            this.className = className;
            this.name = name;
            this.descriptor = descriptor;
        }

        public MethodHandle getMethodHandle() {
            return methodHandle;
        }

        public String getClassName() {
            return className;
        }

        public String getName() {
            return name;
        }

        public String getDescriptor() {
            return descriptor;
        }

    }

    public static abstract class FieldAccess extends Access {

        protected FieldAccess(String className, String name, String descriptor) {
            super(null, className, name, descriptor);
        }

        protected abstract MethodVisitor get(Code code);
        protected abstract void put(Code code);
    }

    public static abstract class Call extends Access {

        protected Call(MethodHandle methodHandle, String className, String name, String descriptor) {
            super(methodHandle, className, name, descriptor);
        }

        protected abstract void invoke(Code code);
    }
}
