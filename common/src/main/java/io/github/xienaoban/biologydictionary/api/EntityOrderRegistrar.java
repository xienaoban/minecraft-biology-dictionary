package io.github.xienaoban.biologydictionary.api;

import net.minecraft.world.entity.EntityType;

/**
 * Registration handle for the entity display-order registry, passed to {@link EntityOrderPlugin}.
 */
public interface EntityOrderRegistrar {
    void register(EntityType<?> entityType);
}
