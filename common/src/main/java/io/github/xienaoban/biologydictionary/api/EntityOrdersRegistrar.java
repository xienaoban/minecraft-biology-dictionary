package io.github.xienaoban.biologydictionary.api;

import net.minecraft.world.entity.EntityType;

/**
 * Registration handle for the entity display-order registry, passed to {@link EntityOrdersPlugin}.
 */
public interface EntityOrdersRegistrar {
    void register(EntityType<?> entityType);
}
