package io.github.xienaoban.biologydictionary.platform.server.forge;

import dev.architectury.registry.CreativeTabRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("unused")
public final class ItemRegistryImpl {

    public static void register(ResourceKey<CreativeModeTab> registryKey, ItemStack itemStack) {
        CreativeTabRegistry.appendStack(registryKey, itemStack);
    }
}
