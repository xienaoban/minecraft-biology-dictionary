package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.client.KeyMappings;
import io.github.xienaoban.biologydictionary.platform.client.ClientEventRegistrar;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetRegistrar;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

@Environment(EnvType.CLIENT)
public final class FabricBiologyDictionaryClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientNetRegistrar.registerClientReceivers();
        ClientEventRegistrar.register();
        KeyMappingHelper.registerKeyMapping(KeyMappings.OPEN_HANDBOOK);
        BiologyDictionaryClient.forceInitialize();
    }
}
