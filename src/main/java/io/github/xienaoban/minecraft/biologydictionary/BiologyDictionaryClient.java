package io.github.xienaoban.minecraft.biologydictionary;

import io.github.xienaoban.minecraft.biologydictionary.client.EntityWidgetManager;
import io.github.xienaoban.minecraft.biologydictionary.client.KeyMappingManager;
import io.github.xienaoban.minecraft.biologydictionary.client.FirstPersonShoulderEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary.LOGGER;

@Environment(EnvType.CLIENT)
public class BiologyDictionaryClient {
    public static final BiologyDictionaryClient BDC = new BiologyDictionaryClient();

    private BiologyDictionaryClient() {
        KeyMappingManager.init();
        FirstPersonShoulderEntityRenderer.init();
        EntityWidgetManager.init();
        LOGGER.info("BiologyDictionary (client) initialized.");
    }

    public void forceInitialize() { /* do nothing but to trigger cinit */ }
}
