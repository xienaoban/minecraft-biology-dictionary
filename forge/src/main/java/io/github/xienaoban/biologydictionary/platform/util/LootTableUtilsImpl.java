package io.github.xienaoban.biologydictionary.platform.util;

import io.github.xienaoban.biologydictionary.mixin.forge.LootTableIMixin;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

public final class LootTableUtilsImpl implements LootTableUtils.PlatformBridge {
    @Override
    public LootPool[] getPools(LootTable lootTable) {
        return ((LootTableIMixin) lootTable).biologydictionary$getPools().toArray(new LootPool[0]);
    }
}
