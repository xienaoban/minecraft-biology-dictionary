package io.github.xienaoban.biologydictionary.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as a configuration category.
 * Nested fields will be grouped under this category.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigCategory {
    /**
     * The translation key for the category name.
     */
    String value();
}
