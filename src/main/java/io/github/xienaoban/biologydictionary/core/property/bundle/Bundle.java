package io.github.xienaoban.biologydictionary.core.property.bundle;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class Bundle<EN> {
    private final List<Function<Entity, EN>> patterns = new ArrayList<>();
    private final Map<EntityType<?>, List<EN>> cache = new ConcurrentHashMap<>();

    void register(Function<Entity, EN> pattern) {
        patterns.add(pattern);
    }

    void register(EntityType<?> entityType, EN... entries) {
        cache.computeIfAbsent(entityType, ignored -> new ArrayList<>())
                .addAll(Arrays.asList(entries));
    }

    List<EN> getEntries(Entity entity) {
        return cache.computeIfAbsent(EntityUtils.getEntityType(entity), ignored -> {
            // Cache the entry if the entity matches the pattern.
            List<EN> res = new ArrayList<>();
            for (Function<Entity, EN> pattern : patterns) {
                EN entry = pattern.apply(entity);
                if (entry != null) {
                    res.add(entry);
                }
            }
            return res;
        });
    }
}
