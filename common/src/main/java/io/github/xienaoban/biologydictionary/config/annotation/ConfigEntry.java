package io.github.xienaoban.biologydictionary.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as a configuration entry.
 * The field name is automatically used as the translation key.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigEntry {
    /**
     * Minimum value for numeric types.
     */
    double min() default Double.MIN_VALUE;

    /**
     * Maximum value for numeric types.
     */
    double max() default Double.MAX_VALUE;
}
