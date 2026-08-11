package io.github.xienaoban.biologydictionary.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.xienaoban.biologydictionary.api.plugin.BiologyDictionaryClientPlugin;
import io.github.xienaoban.biologydictionary.api.plugin.BiologyDictionaryPlugin;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * Internal lookup for third-party plugins. Each registry asks for plugins of its own interface
 * type; the per-loader bridge supplies the raw discovery lists (Fabric entrypoint / NeoForge
 * annotation scan), and this class caches and filters them by type.
 *
 * <p>This is infrastructure, not part of the third-party API — addons never call it; they implement
 * a plugin interface and are discovered.
 */
public final class PluginLookup {
    @ExpectPlatform
    static Bridge getBridge() {
        throw new AssertionError();
    }

    // Lazily loaded: the Bridge instance exists on both sides, but each cache is populated
    // only when first requested on the side that actually calls the method.
    private static List<Object> commonPlugins;
    private static List<Object> clientPlugins;

    private PluginLookup() {}

    /**
     * Common-side plugins
     * ({@code @BiologyDictionaryPlugin} / the {@code biologydictionary} entrypoint).
     */
    public static <P> List<P> find(Class<P> pluginType) {
        if (commonPlugins == null) {
            commonPlugins = validate(getBridge().discoverCommon(), BiologyDictionaryPlugin.class,
                    BiologyDictionaryClientPlugin.class);
        }
        return filter(commonPlugins, pluginType);
    }

    /**
     * Client-side plugins
     * ({@code @BiologyDictionaryClientPlugin} / the {@code biologydictionary:client} entrypoint).
     */
    @ClientOnly
    public static <P> List<P> findClient(Class<P> pluginType) {
        if (clientPlugins == null) {
            clientPlugins = validate(getBridge().discoverClient(), BiologyDictionaryClientPlugin.class,
                    BiologyDictionaryPlugin.class);
        }
        return filter(clientPlugins, pluginType);
    }

    private static List<Object> validate(List<Object> plugins, Class<? extends Annotation> requiredAnnotation,
            Class<? extends Annotation> forbiddenAnnotation) {
        for (Object plugin : plugins) {
            Class<?> pluginClass = plugin.getClass();
            if (!pluginClass.isAnnotationPresent(requiredAnnotation)
                    || pluginClass.isAnnotationPresent(forbiddenAnnotation)) {
                throw new IllegalStateException("Plugin class " + pluginClass.getName()
                        + " must be annotated with @" + requiredAnnotation.getSimpleName() + " only");
            }
        }
        return plugins;
    }

    private static <P> List<P> filter(List<Object> src, Class<P> type) {
        List<P> out = new ArrayList<>();
        for (Object o : src) {
            if (type.isInstance(o)) { out.add(type.cast(o)); }
        }
        return out;
    }

    /** Platform bridge: raw discovery only. Caching and filtering are handled by {@link PluginLookup}. */
    public interface Bridge {
        List<Object> discoverCommon();
        List<Object> discoverClient();
    }
}
