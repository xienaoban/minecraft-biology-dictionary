package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import net.fabricmc.api.ClientModInitializer;

public final class BiologyDictionaryFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BiologyDictionaryClient.BDC.forceInitialize();
    }
}
