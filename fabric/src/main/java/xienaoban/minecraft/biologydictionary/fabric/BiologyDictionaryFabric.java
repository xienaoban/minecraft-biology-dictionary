package xienaoban.minecraft.biologydictionary.fabric;

import xienaoban.minecraft.biologydictionary.BiologyDictionary;
import net.fabricmc.api.ModInitializer;

public class BiologyDictionaryFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        BiologyDictionary.init();
    }
}