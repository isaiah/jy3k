package org.python.core;

import org.python.antlr.ParseException;

public enum FutureFeature implements Pragma {
    /**
     * Enables nested scopes.
     */
    nested_scopes(CodeFlag.CO_NESTED),
    /**
     * Makes integer / integer division return float.
     */
    division(CodeFlag.CO_FUTURE_DIVISION),
    /**
     * Enables generators.
     */
    generators(CodeFlag.CO_GENERATOR_ALLOWED),
    /**
     * Enables absolute imports.
     */
    absolute_import(CodeFlag.CO_FUTURE_ABSOLUTE_IMPORT),
    /**
     * Enables the with statement.
     */
    with_statement(CodeFlag.CO_FUTURE_WITH_STATEMENT),
    /**
     * Enables the print function.
     */
    print_function(CodeFlag.CO_FUTURE_PRINT_FUNCTION),
    /**
     * Enables unicode literals.
     */
    unicode_literals(CodeFlag.CO_FUTURE_UNICODE_LITERALS),

    /**
     * PEP-479 Change StopIteration handling in inside generators
     */
    generator_stop(CodeFlag.CO_FUTURE_GENERATOR_STOP);

    public static final String MODULE_NAME = "__future__";
    public static final PragmaModule PRAGMA_MODULE = new PragmaModule(
            MODULE_NAME) {

        @Override
        public Pragma getPragma(String name) {
            return getFeature(name);
        }

        @Override
        public Pragma getStarPragma() {
            throw new ParseException("future feature * is not defined");
        }
    };
    private final CodeFlag flag;

    private FutureFeature(CodeFlag flag) {
        this.flag = flag;
    }

    private FutureFeature() {
        this(null);
    }

    public void addTo(PragmaReceiver features) {
        features.add(this);
    }

    public static void addFeature(String featureName, PragmaReceiver features) {
        getFeature(featureName).addTo(features);
    }

    private static FutureFeature getFeature(String featureName) {
        try {
            return valueOf(featureName);
        } catch (IllegalArgumentException ex) {
            throw new ParseException("future feature " + featureName
                    + " is not defined");
        }
    }

    public void setFlag(CompilerFlags cflags) {
        if (flag != null) {
            cflags.setFlag(flag);
        }
    }

}
