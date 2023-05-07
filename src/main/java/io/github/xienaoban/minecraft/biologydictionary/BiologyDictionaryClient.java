package io.github.xienaoban.minecraft.biologydictionary;

import io.github.xienaoban.minecraft.biologydictionary.client.KeyMappingManager;
import io.github.xienaoban.minecraft.biologydictionary.client.ShoulderEntityRenderer;

import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary.LOGGER;

public class BiologyDictionaryClient {
    public static final BiologyDictionaryClient BDC = new BiologyDictionaryClient();

    private BiologyDictionaryClient() {
        KeyMappingManager.init();
        ShoulderEntityRenderer.init();
        LOGGER.info("BiologyDictionary (client) initialized.");
    }

    public void forceInitialize() { /* do nothing but to trigger cinit */ }
}
