package io.github.xienaoban.biologydictionary.platform.util.forge;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;

@SuppressWarnings("unused")
public final class ItemUtilsImpl {

    public static SpawnEggItem getSpawnEggItem(EntityType<?> entityType) {
        return ForgeSpawnEggItem.fromEntityType(entityType);
    }
}
