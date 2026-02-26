package io.github.xienaoban.biologydictionary.common.server;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ItemRegistry {
    @ExpectPlatform
    public static void register(ResourceKey<CreativeModeTab> registryKey, ItemStack itemStack) {
        throw new AssertionError();
    }
}