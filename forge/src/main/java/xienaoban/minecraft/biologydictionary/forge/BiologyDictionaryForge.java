package xienaoban.minecraft.biologydictionary.forge;

import xienaoban.minecraft.biologydictionary.BiologyDictionary;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BiologyDictionary.MOD_ID)
public class BiologyDictionaryForge {
    public BiologyDictionaryForge() {
        BiologyDictionary.init();
    }
}