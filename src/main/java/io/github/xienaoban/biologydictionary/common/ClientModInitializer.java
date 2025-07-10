package io.github.xienaoban.biologydictionary.common;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ClientModInitializer implements net.fabricmc.api.ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BiologyDictionaryClient.BDC.forceInitialize();
    }
}
