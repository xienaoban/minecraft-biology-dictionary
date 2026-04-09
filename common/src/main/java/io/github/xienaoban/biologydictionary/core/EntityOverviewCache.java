package io.github.xienaoban.biologydictionary.core;

import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.EntityProperty;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;

import java.util.concurrent.ConcurrentHashMap;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Cache for entity overview data (reference properties).
 * Stores NBT data for each entity type to avoid repeated lookups.
 */
public final class EntityOverviewCache extends ConcurrentHashMap<EntityType<?>, EntityOverviewCache.CacheEntry> {

    public CacheEntry getOrCreate(EntityType<?> entityType, ServerLevel serverLevel) {
        try {
            Entity entity = EntityUtils.create(entityType, serverLevel);

            // Initialize goal/ai for mob entities
            // @see net.minecraft.world.entity.EntityType.create(net.minecraft.server.level.ServerLevel, java.util.function.Consumer<T>, net.minecraft.core.BlockPos, net.minecraft.world.entity.MobSpawnType, boolean, boolean)
            if (entity instanceof Mob mob) {
                mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(entity.blockPosition()),
                        MobSpawnType.NATURAL, null);
            }

            CompoundTag vanillaNbt = EntityUtils.getNbt(entity);
            CompoundTag extraNbt = new CompoundTag();
            for (EntityProperty<?> p : new EntityProperties<>(entity).getExtras()) {
                p.getFrom(Misc.cast(entity));
                p.writeTo(extraNbt);
            }

            return new CacheEntry(vanillaNbt, extraNbt);
        } catch (Exception e) {
            LOGGER.error("Failed to create entity overview for type: {}", EntityUtils.getEntityTypeIdName(entityType), e);
            return new CacheEntry(null, null);
        }
    }



    public record CacheEntry(CompoundTag vanillaNbt, CompoundTag extraNbt) {}
}
