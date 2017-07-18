package org.python.internal.lookup;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class MethodHandleFactory {
    private static final MethodHandles.Lookup PUBLIC_LOOKUP = MethodHandles.publicLookup();
    private static final MethodHandles.Lookup LOOKUP        = MethodHandles.lookup();

    private MethodHandleFactory() {
    }

    /**
     * Runtime exception that collects every reason that a method handle lookup operation can go wrong
     */
    @SuppressWarnings("serial")
    public static class LookupException extends RuntimeException {
        /**
         * Constructor
         * @param e causing exception
         */
        public LookupException(final Exception e) {
            super(e);
        }
    }

    private static final MethodHandleFunctionality FUNC = new StandardMethodHandleFunctionality();

    public static MethodHandleFunctionality getFunctionality() {
        return FUNC;
    }

    private static class StandardMethodHandleFunctionality implements MethodHandleFunctionality {

        private StandardMethodHandleFunctionality() {
        }

        @Override
        public MethodHandle findStatic(MethodHandles.Lookup explicitLookup, Class<?> clazz, String name, MethodType type) {
            try {
                return explicitLookup.findStatic(clazz, name, type);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new LookupException(e);
            }
        }

        @Override
        public MethodHandle findConstructor(MethodHandles.Lookup explicitLookup, Class<?> clazz, MethodType type) {
            try {
                return explicitLookup.findConstructor(clazz, type);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new LookupException(e);
            }
        }
    }
}
