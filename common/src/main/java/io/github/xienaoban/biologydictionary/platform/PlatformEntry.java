package io.github.xienaoban.biologydictionary.platform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a common definition that must be referenced by every platform implementation.
 *
 * <p>This annotation does not define when or how registration happens. Fabric and NeoForge
 * should still register the annotated entry in their own lifecycle hooks. Future verification
 * tasks can scan platform bytecode and fail when an annotated common entry is not referenced.
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface PlatformEntry {
}
