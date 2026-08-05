package io.github.xienaoban.biologydictionary.api;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a client-only Biology Dictionary plugin (see {@link EntityPropertyWidgetsPlugin}).
 *
 * <p>NeoForge discovers client plugins by scanning mod bytecode for this annotation, on the client
 * only; Fabric uses the {@code "biologydictionary:client"} entrypoint key instead. The annotated
 * class must implement {@link EntityPropertyWidgetsPlugin} and expose a public no-arg constructor.
 */
@ClientOnly
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BiologyDictionaryClientPlugin {
}
