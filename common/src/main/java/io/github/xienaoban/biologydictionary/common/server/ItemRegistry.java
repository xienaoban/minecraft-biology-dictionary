package io.github.xienaoban.biologydictionary.common.server;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ItemRegistry {
    public static void register(ResourceKey<CreativeModeTab> registryKey, ItemStack itemStack) {
        ItemGroupEvents.modifyEntriesEvent(registryKey)
                .register(entries -> entries.accept(itemStack));
    }
}
