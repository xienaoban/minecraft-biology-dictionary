package io.github.xienaoban.biologydictionary.platform.client;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.KeyMapping;

public final class KeyMappingRegistry {
    @ExpectPlatform
    public static void registerKeyMapping(KeyMapping mapping) {
        throw new AssertionError();
    }
}
