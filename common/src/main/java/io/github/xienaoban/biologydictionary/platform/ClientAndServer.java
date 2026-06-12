package io.github.xienaoban.biologydictionary.platform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as both client and server accessible.
 *
 * <p>Such a method is allowed to call {@link ClientOnly} methods/classes,
 * but still forbidden from referencing Minecraft {@code @Environment(CLIENT)} classes.
 * It is not treated as client-only itself, so non-{@link ClientOnly} code may call it freely.</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface ClientAndServer {
}
