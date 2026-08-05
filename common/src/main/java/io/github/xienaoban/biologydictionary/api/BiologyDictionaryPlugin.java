package io.github.xienaoban.biologydictionary.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a Biology Dictionary plugin for loader-side discovery.
 *
 * <p>NeoForge discovers plugins by scanning mod bytecode for this annotation; Fabric uses the
 * {@code "biologydictionary"} entrypoint key instead, so the annotation is optional on Fabric.
 * The annotated class must implement one or more of {@link BiologySkillsPlugin},
 * {@link ExtraEntityPropertiesPlugin}, {@link EntityOrdersPlugin}, {@link DiscoverySourcesPlugin},
 * and expose a public no-arg
 * constructor.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BiologyDictionaryPlugin {
}
