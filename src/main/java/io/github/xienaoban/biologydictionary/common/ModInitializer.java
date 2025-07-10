package io.github.xienaoban.biologydictionary.common;

import io.github.xienaoban.biologydictionary.BiologyDictionary;

public class ModInitializer implements net.fabricmc.api.ModInitializer {
    @Override
    public void onInitialize() {
        BiologyDictionary.BD.forceInitialize();
    }
}
