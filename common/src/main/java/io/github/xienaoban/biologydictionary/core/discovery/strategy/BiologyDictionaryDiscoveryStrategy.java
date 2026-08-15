package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryStrategy;
import io.github.xienaoban.biologydictionary.core.discovery.storage.SavedDataDiscoveryStorage;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.Map;

/**
 * Entity is discovered when the player interacts with it via the mod
 * (highlight skill or entity overview screen).
 * Uses MC's SavedData framework for persistence. Per-source validation lives on the source.
 */
public final class BiologyDictionaryDiscoveryStrategy implements DiscoveryStrategy {
    private final SavedDataDiscoveryStorage storage;

    public BiologyDictionaryDiscoveryStrategy(MinecraftServer server) {
        this.storage = server.getDataStorage().computeIfAbsent(SavedDataDiscoveryStorage.TYPE);
    }

    @Override
    public boolean isDiscovered(ServerPlayer player, EntityType<?> entityType) {
        return storage.isDiscovered(player.getUUID(), entityType);
    }

    public Map<EntityType<?>, DiscoveryRecord> getAllRecords(ServerPlayer player) {
        return storage.getAll(player.getUUID());
    }

    @Override
    public DiscoveryRecord getRecord(ServerPlayer player, EntityType<?> entityType) {
        return storage.get(player.getUUID(), entityType);
    }

    @Override
    public boolean onDiscovery(DiscoverySource source, DiscoverySource.ServerContext ctx) {
        return source.serverCheck(ctx) && tryDiscover(ctx.player(), ctx.entity(), source);
    }

    private boolean tryDiscover(ServerPlayer player, Entity entity, DiscoverySource source) {
        EntityType<?> entityType = EntityUtils.getEntityType(entity);
        if (storage.isDiscovered(player.getUUID(), entityType)) {
            return false;
        }
        DiscoveryRecord record = DiscoveryRecord.standard(
                player.level().getGameTime(), entity, source);
        if (storage.put(player.getUUID(), entityType, record)) {
            ServerNetManager.sendDiscoveryIncremental(player, entity, entityType, record);
            return true;
        }
        return false;
    }
}
