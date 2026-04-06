package io.github.xienaoban.biologydictionary.client;

import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache for entity type overview data (reference properties).
 * Stores NBT data for each entity type to avoid repeated server requests.
 */
@Environment(EnvType.CLIENT)
public class EntityTypeOverviewCache {
    private static final Map<String, CacheEntry> CACHE = new ConcurrentHashMap<>();

    public static boolean hasCached(EntityType<?> entityType) {
        return CACHE.containsKey(EntityUtils.getEntityTypeIdName(entityType));
    }

    public static CacheEntry get(EntityType<?> entityType) {
        return CACHE.get(EntityUtils.getEntityTypeIdName(entityType));
    }

    public static void put(String entityTypeId, CompoundTag vanillaNbt, CompoundTag extraNbt) {
        CACHE.put(entityTypeId, new CacheEntry(vanillaNbt, extraNbt));
    }

    public static final class CacheEntry {
        public final CompoundTag vanillaNbt;
        public final CompoundTag extraNbt;

        CacheEntry(CompoundTag vanillaNbt, CompoundTag extraNbt) {
            this.vanillaNbt = vanillaNbt;
            this.extraNbt = extraNbt;
        }
    }
}
