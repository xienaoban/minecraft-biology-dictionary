package xienaoban.minecraft.biologydictionary.forge;

import xienaoban.minecraft.biologydictionary.BiologyDictionary;
import net.minecraftforge.fml.common.Mod;

@Mod(BiologyDictionary.MOD_ID)
public class BiologyDictionaryForge {
    public BiologyDictionaryForge() {
        // [architectury api support]
        // EventBuses.registerModEventBus(BiologyDictionary.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        BiologyDictionary.init();
    }
}