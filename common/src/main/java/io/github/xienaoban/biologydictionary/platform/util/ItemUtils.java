package io.github.xienaoban.biologydictionary.platform.util;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;

public final class ItemUtils {
    /**
     * Get spawn egg item for the given entity type.
     * This method is mainly for Forge. In Fabric, it's just {@code SpawnEggItem.byId}.
     *
     * @return the spawn egg item, or null if not found
     */
    @ExpectPlatform
    public static SpawnEggItem getSpawnEggItem(EntityType<?> entityType) {
        throw new AssertionError();
    }
}
