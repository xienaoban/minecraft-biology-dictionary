package io.github.xienaoban.biologydictionary.platform.util.fabric;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

@SuppressWarnings("unused")
public final class ItemUtilsImpl {

    public static Item getSpawnEggItem(EntityType<?> entityType) {
        // Fabric only has the vanilla spawn egg item
        return SpawnEggItem.byId(entityType);
    }
}
