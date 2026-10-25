package io.github.xienaoban.biologydictionary.platform.util.forge;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;

@SuppressWarnings("unused")
public final class ItemUtilsImpl {

    public static Item getSpawnEggItem(EntityType<?> entityType) {
        return ForgeSpawnEggItem.fromEntityType(entityType);
    }
}
