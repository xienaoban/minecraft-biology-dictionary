package io.github.xienaoban.biologydictionary.mixin.loot;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootItem.class)
public interface LootItemIMixin {
    @Accessor("item")
    Item biologydictionary$getItem();
}
