package io.github.xienaoban.biologydictionary.platform.util;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

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
    @ExpectPlatform
    public static Item getSpawnEggItem(EntityType<?> entityType) {
        throw new AssertionError();
    }
}
