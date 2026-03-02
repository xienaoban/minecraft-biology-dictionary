package io.github.xienaoban.biologydictionary.core.property.bundle;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class Bundle<H> {
    private final List<Function<Entity, H>> patterns = new ArrayList<>();
    private final Map<EntityType<?>, List<H>> cache = new ConcurrentHashMap<>();

    void register(Function<Entity, H> pattern) {
        patterns.add(pattern);
    }

    @SafeVarargs
    final void register(EntityType<?> entityType, H... handlers) {
        cache.computeIfAbsent(entityType, ignored -> new ArrayList<>())
                .addAll(Arrays.asList(handlers));
    }

    List<H> getHandlers(Entity entity) {
        return cache.computeIfAbsent(EntityUtils.getEntityType(entity), ignored -> {
            // Cache the handler if the entity matches the pattern.
            List<H> res = new ArrayList<>();
            for (Function<Entity, H> pattern : patterns) {
                H handler = pattern.apply(entity);
                if (handler != null) {
                    res.add(handler);
                }
            }
            return res;
        });
    }
}
