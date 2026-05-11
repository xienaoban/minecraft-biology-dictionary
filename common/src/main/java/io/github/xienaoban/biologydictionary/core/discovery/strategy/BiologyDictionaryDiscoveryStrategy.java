package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryStrategy;
import io.github.xienaoban.biologydictionary.core.discovery.storage.SavedDataDiscoveryStorage;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Enemy;

import java.util.Map;
import java.util.UUID;

/**
 * Entity is discovered when the player interacts with it via the mod
 * (highlight skill or entity overview screen).
 * Uses MC's SavedData framework for persistence.
 */
public final class BiologyDictionaryDiscoveryStrategy implements DiscoveryStrategy {
    private final SavedDataDiscoveryStorage storage;

    public BiologyDictionaryDiscoveryStrategy(MinecraftServer server) {
        this.storage = server.overworld().getDataStorage().computeIfAbsent(SavedDataDiscoveryStorage.TYPE);
    }

    @Override
    public boolean isDiscovered(ServerPlayer player, EntityType<?> entityType) {
        return storage.isDiscovered(player.getUUID(), entityType);
    }

    public Map<EntityType<?>, DiscoveryRecord> getAllRecords(ServerPlayer player) {
        return storage.getAll(player.getUUID());
    }

    public DiscoveryRecord getRecord(UUID playerUUID, EntityType<?> entityType) {
        return storage.get(playerUUID, entityType);
    }

    @Override
    public boolean onEntityDetailScreenOpened(ServerPlayer player, Entity entity) {
        if (!PlayerUtils.isWithinInteractionRange(player, entity, 10)) {
            return false;
        }
        return tryDiscover(player, entity, DiscoverySource.ENTITY_DETAIL_SCREEN);
    }

    @Override
    public boolean onEntityKilled(ServerPlayer player, Entity entity) {
        return tryDiscover(player, entity, DiscoverySource.KILL);
    }

    @Override
    public boolean onEntityInteracted(ServerPlayer player, Entity entity) {
        if (entity instanceof Enemy) {
            return false;
        }
        return tryDiscover(player, entity, DiscoverySource.INTERACT);
    }

    @Override
    public boolean onPlayerKilledBy(ServerPlayer player, Entity entity) {
        return tryDiscover(player, entity, DiscoverySource.KILLED_BY);
    }

    @Override
    public boolean onEntityObservedWithTelescope(ServerPlayer player, Entity entity) {
        if (!player.isScoping() || !PlayerUtils.isWithinRangeAndUnobstructed(player, entity, ConfigsManager.getServer().getTelescopeDiscoveryRange())) {
            return false;
        }
        return tryDiscover(player, entity, DiscoverySource.TELESCOPE_OBSERVE);
    }

    private boolean tryDiscover(ServerPlayer player, Entity entity, DiscoverySource source) {
        EntityType<?> entityType = EntityUtils.getEntityType(entity);
        if (storage.isDiscovered(player.getUUID(), entityType)) {
            return false;
        }
        DiscoveryRecord record = DiscoveryRecord.discoveredNow(
                player.level().getGameTime(), entity, source);
        if (storage.put(player.getUUID(), entityType, record)) {
            ServerNetManager.sendDiscoveryIncremental(player, entity, entityType, record);
            return true;
        }
        return false;
    }
}
