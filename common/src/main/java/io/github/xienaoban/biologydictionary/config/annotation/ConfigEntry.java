package io.github.xienaoban.biologydictionary.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigEntry {
    double min() default Double.MIN_VALUE;

    double max() default Double.MAX_VALUE;
}
