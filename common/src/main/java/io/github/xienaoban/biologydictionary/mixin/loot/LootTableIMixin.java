package io.github.xienaoban.biologydictionary.mixin.loot;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(LootTable.class)
public interface LootTableIMixin {
    @Accessor("pools")
    List<LootPool> biologydictionary$getPools();
}
