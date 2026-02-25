package io.github.xienaoban.biologydictionary.neoforge;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.Lang;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Lang.BIOLOGY_DICTIONARY)
public final class BiologyDictionaryNeoForge {
    public BiologyDictionaryNeoForge(IEventBus modBus) {
        modBus.addListener(BiologyDictionaryNeoForge::initCommon);
        modBus.addListener(BiologyDictionaryNeoForge::initClient);
    }

    private static void initCommon(FMLCommonSetupEvent event) {
        BiologyDictionary.BD.forceInitialize();
    }

    private static void initClient(FMLClientSetupEvent event) {
        BiologyDictionaryClient.BDC.forceInitialize();
    }
}
