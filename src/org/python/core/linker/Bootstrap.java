package org.python.core.linker;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.DynamicLinker;
import jdk.dynalink.DynamicLinkerFactory;
import jdk.dynalink.Operation;
import jdk.dynalink.StandardNamespace;
import jdk.dynalink.StandardOperation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.support.SimpleRelinkableCallSite;
import org.objectweb.asm.MethodVisitor;
import org.python.compiler.Code;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static org.python.util.CodegenUtils.p;
import static org.python.util.CodegenUtils.sig;

/**
 * Bootstrap methods for invokedynamic
 */
public class Bootstrap {
    public static final Call BOOTSTRAP = staticCallNoLookup(Bootstrap.class, "bootstrap", CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class);
    private static final GuardingDynamicLinker pythonLinker = new DynaPythonLinker();

    private static final DynamicLinker dynamicLinker = createDynamicLinker();

    private static DynamicLinker createDynamicLinker() {
         final DynamicLinkerFactory factory = new DynamicLinkerFactory();
         factory.setPrioritizedLinker(pythonLinker);
         return factory.createLinker();
     }

    public static CallSite bootstrap(MethodHandles.Lookup lookup, String name, MethodType type) {
        return dynamicLinker.link(
                new SimpleRelinkableCallSite(
                        new CallSiteDescriptor(lookup, parseOperation(name), type)
                )
        );
    }

    private static Operation parseOperation(String name) {
        return StandardOperation.GET.withNamespace(StandardNamespace.PROPERTY).named(name);
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
