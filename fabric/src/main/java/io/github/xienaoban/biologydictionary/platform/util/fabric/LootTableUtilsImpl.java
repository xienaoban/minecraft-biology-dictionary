package io.github.xienaoban.biologydictionary.platform.util.fabric;

import io.github.xienaoban.biologydictionary.mixin.fabric.LootTableIMixin;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

@SuppressWarnings("unused")
public final class LootTableUtilsImpl {

    public static LootPool[] getPools(LootTable lootTable) {
        return ((LootTableIMixin) lootTable).biologydictionary$getPools();
    }
}
