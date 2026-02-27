package io.github.xienaoban.biologydictionary.common.client.neoforge;

import io.github.xienaoban.biologydictionary.common.client.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class KeyMappingRegistryImpl {

    public static void registerKeyMapping(KeyMapping mapping) {
        dev.architectury.registry.client.keymappings.KeyMappingRegistry.register(mapping);
    }
}
