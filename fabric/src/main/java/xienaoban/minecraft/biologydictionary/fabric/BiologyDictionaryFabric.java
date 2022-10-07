package xienaoban.minecraft.biologydictionary.fabric;

import net.fabricmc.api.ModInitializer;
import xienaoban.minecraft.biologydictionary.BiologyDictionary;

public class BiologyDictionaryFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        if (BiologyDictionary.get() == null) {
            throw new AssertionError();
        }
    }
}