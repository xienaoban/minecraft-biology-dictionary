package io.github.xienaoban.biologydictionary.platform.client.neoforge;

import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public final class KeyMappingRegistryImpl {

    public static void registerKeyMapping(KeyMapping mapping) {
        KeyMappingRegistry.register(mapping);
    }
}
