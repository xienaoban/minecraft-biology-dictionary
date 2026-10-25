package io.github.xienaoban.biologydictionary.platform.util;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public final class ItemUtils {
    private ItemUtils() {}

    /**
     * Get the spawn egg item of the given entity type.
     * <p>
     * Lives in the platform layer because mod loaders do not agree on how to look up a
     * spawn egg, and callers should not have to care.
     *
     * @return the spawn egg item, or null if the entity type has none
     */
    public static Item getSpawnEggItem(EntityType<?> entityType) {
        return SpawnEggItem.byId(entityType);
    }
}
