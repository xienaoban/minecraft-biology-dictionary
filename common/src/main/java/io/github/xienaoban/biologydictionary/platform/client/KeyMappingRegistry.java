package io.github.xienaoban.biologydictionary.platform.client;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.client.KeyMapping;

@ClientOnly
public final class KeyMappingRegistry {
    @ExpectPlatform
    public static void registerKeyMapping(KeyMapping mapping) {
        throw new AssertionError();
    }
}
