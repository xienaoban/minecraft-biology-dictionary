package io.github.xienaoban.biologydictionary.api;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * How a player discovered an entity. Identity ({@link #id()}) is fixed at construction;
 * the display name is derived from it as {@code discovery_source.<namespace>.<path>}.
 * Config gate and per-side validation are overridable methods.
 *
 * <p>Only the Biology Dictionary strategy honors {@link #clientCheck} / {@link #serverCheck};
 * the other two strategies are hardcoded. Built-in sources and the registry live in
 * {@code DiscoverySources} (core package).
 */
public abstract class DiscoverySource {

    private final ResourceLocation id;

    protected DiscoverySource(ResourceLocation id) {
        this.id = id;
    }

    /**
     * Serialization identity. Stable, fixed at construction.
     */
    public final ResourceLocation id() {
        return id;
    }

    /**
     * Display name; derived from {@link #id()} as {@code discovery_source.<namespace>.<path>}.
     */
    public Component displayName() {
        return TextUtils.translate("discovery_source." + id.getNamespace() + "." + id.getPath());
    }

    /**
     * Config gate; default permissive.
     */
    public boolean isEnabled() {
        return true;
    }

    /**
     * Client-side validation; default permissive.
     */
    @ClientOnly
    public boolean clientCheck(ClientContext ctx) {
        return true;
    }

    /**
     * Server-side validation; default permissive.
     */
    public boolean serverCheck(ServerContext ctx) {
        return true;
    }

    @ClientOnly
    public record ClientContext(LocalPlayer player, Entity entity) {}

    public record ServerContext(ServerPlayer player, Entity entity) {}
}
