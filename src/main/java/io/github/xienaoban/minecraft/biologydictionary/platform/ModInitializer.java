package io.github.xienaoban.minecraft.biologydictionary.platform;

import io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary;

public class ModInitializer implements net.fabricmc.api.ModInitializer {
    @Override
    public void onInitialize() {
        BiologyDictionary.BD.forceInitialize();
    }
}
