package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryStrategy;
import io.github.xienaoban.biologydictionary.core.discovery.GlobalDiscoveryStats;
import io.github.xienaoban.biologydictionary.core.discovery.storage.SavedDataDiscoveryStorage;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity is discovered when the player interacts with it via the mod
 * (highlight skill or entity overview screen).
 * Uses MC's SavedData framework for persistence. Per-source validation lives on the source.
 *
 * <p>When {@code discoveryGlobalShared} is on, the effective view is the player's own
 * pool plus {@link SavedDataDiscoveryStorage#stats()} presented as global records:
 * anyone's genuine discovery counts for everyone. Sharing is pure view — nothing is
 * persisted for it; other online players are merely notified so their caches update live.
 */
public final class BiologyDictionaryDiscoveryStrategy implements DiscoveryStrategy {
    private final MinecraftServer server;
    private final SavedDataDiscoveryStorage storage;

    public BiologyDictionaryDiscoveryStrategy(MinecraftServer server) {
        this.server = server;
        this.storage = server.getDataStorage().computeIfAbsent(SavedDataDiscoveryStorage.TYPE);
    }

    @Override
    public boolean isDiscovered(ServerPlayer player, EntityType<?> entityType) {
        if (storage.isDiscovered(player.getUUID(), entityType)) {
            return true;
        }
        return shared() && storage.stats().contains(entityType);
    }

    public Map<EntityType<?>, DiscoveryRecord> getAllRecords(ServerPlayer player) {
        Map<EntityType<?>, DiscoveryRecord> result = new HashMap<>(storage.getAll(player.getUUID()));
        if (shared()) {
            for (EntityType<?> entityType : storage.stats().types()) {
                result.putIfAbsent(entityType, globalPresentation(entityType));
            }
        }
        return result;
    }

    @Override
    public DiscoveryRecord getRecord(ServerPlayer player, EntityType<?> entityType) {
        DiscoveryRecord own = storage.get(player.getUUID(), entityType);
        if (own != null || !shared()) {
            return own;
        }
        return globalPresentation(entityType);
    }

    /**
     * The canonical stats entry presented as a global shared record (global=true),
     * or {@code null} if undiscovered. Keeps the original discovery time and source.
     * Global sharing is represented exclusively by the global flag and never
     * participates in the active-sharing chain.
     */
    private DiscoveryRecord globalPresentation(EntityType<?> entityType) {
        GlobalDiscoveryStats.Entry entry = storage.stats().canonical(entityType);
        if (entry == null) {
            return null;
        }
        DiscoveryRecord record = entry.record();
        return new DiscoveryRecord(record.firstDiscoveryTime(), record.firstDiscoveryTick(), record.source(),
                record.dimension(), record.biome(), record.position(), record.weather(), record.entityUUID(),
                record.entityNbt(), entry.discoverer(), entry.discovererName(), List.of(), true);
    }

    @Override
    public boolean onDiscovery(DiscoverySource source, DiscoverySource.ServerContext ctx) {
        return source.serverCheck(ctx) && tryDiscover(ctx.player(), ctx.entity(), source);
    }

    private boolean tryDiscover(ServerPlayer player, Entity entity, DiscoverySource source) {
        EntityType<?> entityType = EntityUtils.getEntityType(entity);
        if (isDiscovered(player, entityType)) {
            return false;
        }
        DiscoveryRecord record = DiscoveryRecord.standard(player.level().getGameTime(), entity, source, player);
        if (!storage.put(player.getUUID(), entityType, record)) {
            return false;
        }
        if (shared()) {
            // The discovery just passed the effective-view check, so the entry put above
            // is the type's only stats entry — the global presentation is this discovery.
            DiscoveryRecord global = globalPresentation(entityType);
            String playerName = player.getGameProfile().name();
            for (ServerPlayer other : server.getPlayerList().getPlayers()) {
                if (!other.getUUID().equals(player.getUUID())) {
                    ServerNetManager.sendSharedDiscoveryIncremental(other, playerName, entityType, global);
                }
            }
        }
        ServerNetManager.sendDiscoveryIncremental(player, entity, entityType, record);
        return true;
    }

    private static boolean shared() {
        return ConfigsManager.getServer().isDiscoveryGlobalShared();
    }
}
