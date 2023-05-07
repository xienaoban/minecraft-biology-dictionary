package io.github.xienaoban.minecraft.biologydictionary.platform;

import io.github.xienaoban.minecraft.biologydictionary.BiologyDictionaryClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ClientModInitializer implements net.fabricmc.api.ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BiologyDictionaryClient.BDC.forceInitialize();
    }
}
