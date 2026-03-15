package io.github.xienaoban.biologydictionary.platform.client.forge;

import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public final class KeyMappingRegistryImpl {

    public static void registerKeyMapping(KeyMapping mapping) {
        KeyMappingRegistry.register(mapping);
    }
}
