package xienaoban.minecraft.biologydictionary.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import xienaoban.minecraft.biologydictionary.BiologyDictionaryClient;

@Environment(EnvType.CLIENT)
public class BiologyDictionaryClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (BiologyDictionaryClient.get() == null) {
            throw new AssertionError();
        }
    }
}
