package io.github.xienaoban.biologydictionary.forge;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BiologyDictionary.MOD_ID)
public final class BiologyDictionaryForge {
    public BiologyDictionaryForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(BiologyDictionary.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        BiologyDictionary.init();
    }
}
