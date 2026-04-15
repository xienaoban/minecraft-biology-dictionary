package io.github.xienaoban.biologydictionary.core.property;

import io.github.xienaoban.biologydictionary.platform.util.Misc;
import net.minecraft.world.entity.EntityType;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Caches expensive static per-entity-type computations.
 * Lives in {@link io.github.xienaoban.biologydictionary.core.session.WorldSession}.
 *
 * <p>Structure: {@code Map<EntityType<?>, Map<Class<?>, Object>>}.
 * The inner key is a class used to identify the cache entry type
 * (e.g. {@code EntityLootTableProperty.class}, {@code MobTemptProperty.class}).</p>
 */
public final class StaticEntityPropertyCache {

    private final ConcurrentHashMap<EntityType<?>, ConcurrentHashMap<Class<?>, Object>> map = new ConcurrentHashMap<>();

    public <T> T get(EntityType<?> entityType, Class<?> key) {
        var inner = map.get(entityType);
        if (inner == null) return null;
        return Misc.cast(inner.get(key));
    }

    public <T> T getOrCompute(EntityType<?> entityType, Class<?> key, Supplier<T> computer) {
        var inner = map.computeIfAbsent(entityType, k -> new ConcurrentHashMap<>());
        return Misc.cast(inner.computeIfAbsent(key, k -> computer.get()));
    }

    public void put(EntityType<?> entityType, Class<?> key, Object value) {
        if (value == null) return;
        map.computeIfAbsent(entityType, k -> new ConcurrentHashMap<>()).put(key, value);
    }
}
