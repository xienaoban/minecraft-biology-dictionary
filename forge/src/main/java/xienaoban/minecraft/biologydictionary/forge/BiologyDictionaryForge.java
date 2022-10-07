package xienaoban.minecraft.biologydictionary.forge;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import xienaoban.minecraft.biologydictionary.BiologyDictionary;
import xienaoban.minecraft.biologydictionary.BiologyDictionaryClient;

@Mod(BiologyDictionary.MOD_ID)
public class BiologyDictionaryForge {
    public BiologyDictionaryForge() {
        // [architectury api support]
        // EventBuses.registerModEventBus(BiologyDictionary.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::init);
        bus.addListener(this::initClient);
    }

    private void init(FMLCommonSetupEvent event) {
        if (BiologyDictionary.get() == null) {
            throw new AssertionError();
        }
    }

    private void initClient(FMLClientSetupEvent event) {
        if (BiologyDictionaryClient.get() == null) {
            throw new AssertionError();
        }
    }
}