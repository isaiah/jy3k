package org.python.internal.logging;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Logger {
    /**
     * Get the name of the logger.
     * @return logger name
     */
    public String name() default "";
}
