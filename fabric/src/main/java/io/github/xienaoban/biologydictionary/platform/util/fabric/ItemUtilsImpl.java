package io.github.xienaoban.biologydictionary.platform.util.fabric;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;

@SuppressWarnings("unused")
public final class ItemUtilsImpl {

    public static SpawnEggItem getSpawnEggItem(EntityType<?> entityType) {
        // Fabric only has vanilla SpawnEggItem
        return SpawnEggItem.byId(entityType);
    }
}
