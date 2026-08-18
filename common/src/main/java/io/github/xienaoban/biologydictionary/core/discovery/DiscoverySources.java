package io.github.xienaoban.biologydictionary.core.discovery;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.api.plugin.DiscoverySourcesPlugin;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.PluginLookup;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Aggregator/registry for {@link DiscoverySource}. Built-in sources register at class init;
 * third-party sources register via {@link DiscoverySourcesPlugin} during {@link #init()}.
 */
public final class DiscoverySources {

    private static final Map<Identifier, DiscoverySource> REGISTRY = new LinkedHashMap<>();

    private DiscoverySources() {}

    public static final DiscoverySource UNKNOWN = register(new DiscoverySource(id("unknown")) {
        @Override public boolean isEnabled() { return false; }
    });

    public static final DiscoverySource ENTITY_DETAIL_SCREEN = register(new DiscoverySource(id("entity_detail_screen")) {
        @Override public boolean isEnabled() { return ConfigsManager.getServer().isDiscoveryByDetailScreen(); }
        @ClientOnly @Override
        public boolean clientCheck(ClientContext ctx) {
            @ClientOnly final class CO { static boolean check(ClientContext ctx) {
                return PlayerUtils.isWithinInteractionRange(ctx.player(), ctx.entity(),
                        ConfigsManager.getServer().getEntityDetailScreenRange());
            }}
            return CO.check(ctx);
        }
        @Override public boolean serverCheck(ServerContext ctx) {
            return PlayerUtils.isWithinInteractionRange(ctx.player(), ctx.entity(),
                    ConfigsManager.getServer().getEntityDetailScreenRange());
        }
    });

    public static final DiscoverySource HIGHLIGHT = register(new DiscoverySource(id("highlight")) {
        @Override public boolean isEnabled() { return ConfigsManager.getServer().isDiscoveryByHighlight(); }
    });

    public static final DiscoverySource TELESCOPE_OBSERVE = register(new DiscoverySource(id("telescope_observe")) {
        @Override public boolean isEnabled() { return ConfigsManager.getServer().isDiscoveryByTelescope(); }
        @ClientOnly @Override
        public boolean clientCheck(ClientContext ctx) {
            @ClientOnly final class CO { static boolean check(ClientContext ctx) {
                return ctx.player().isScoping() && PlayerUtils.isWithinRangeAndUnobstructed(
                        ctx.player(), ctx.entity(), ConfigsManager.getServer().getTelescopeDiscoveryRange());
            }}
            return CO.check(ctx);
        }
        @Override public boolean serverCheck(ServerContext ctx) {
            return ctx.player().isScoping() && PlayerUtils.isWithinRangeAndUnobstructed(
                    ctx.player(), ctx.entity(), ConfigsManager.getServer().getTelescopeDiscoveryRange());
        }
    });

    public static final DiscoverySource INTERACT = register(new DiscoverySource(id("interact")) {
        @Override public boolean isEnabled() { return ConfigsManager.getServer().isDiscoveryByInteract(); }
        @Override public boolean serverCheck(ServerContext ctx) {
            return EntityUtils.isFriendly(ctx.entity());
        }
    });

    public static final DiscoverySource KILL = register(new DiscoverySource(id("kill")) {
        @Override public boolean isEnabled() { return ConfigsManager.getServer().isDiscoveryByKill(); }
    });

    public static final DiscoverySource KILLED_BY = register(new DiscoverySource(id("killed_by")) {
        @Override public boolean isEnabled() { return ConfigsManager.getServer().isDiscoveryByKilledBy(); }
    });

    private static Identifier id(String path) {
        return IdentifierUtils.bd(path);
    }

    public static DiscoverySource byId(Identifier id) {
        return REGISTRY.getOrDefault(id, UNKNOWN);
    }

    /**
     * Lenient parse of a serialized source id. Tolerates legacy uppercase ids (no namespace) by
     * migrating them to the mod's namespace. Unknown ids resolve to {@link #UNKNOWN}.
     */
    public static DiscoverySource parseSource(String id) {
        Identifier identifier = IdentifierUtils.fromStringOrNull(
                id.indexOf(':') < 0 ? BiologyDictionary.MOD_ID + ":" + id.toLowerCase() : id);
        return identifier != null ? byId(identifier) : UNKNOWN;
    }

    /**
     * Read-only snapshot of all registered sources. Only valid after {@link #init()}.
     */
    public static Collection<DiscoverySource> values() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    private static DiscoverySource register(DiscoverySource source) {
        if (REGISTRY.putIfAbsent(source.id(), source) != null) {
            throw new RuntimeException("Duplicate discovery source id: " + source.id());
        }
        return source;
    }

    private static final DiscoverySourcesPlugin.Registrar REGISTRAR = DiscoverySources::register;

    /**
     * Register third-party sources. Built-ins self-register via their static fields at class init;
     * this only dispatches to plugins. Called once from {@link BiologyDictionary} static init.
     */
    public static void init() {
        for (DiscoverySourcesPlugin plugin : PluginLookup.find(DiscoverySourcesPlugin.class)) {
            try {
                plugin.registerDiscoverySources(REGISTRAR);
            } catch (RuntimeException e) {
                throw new IllegalStateException("Failed to register discovery sources from plugin "
                        + plugin.getClass().getName(), e);
            }
        }
    }
}
