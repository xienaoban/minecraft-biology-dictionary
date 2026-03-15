package io.github.xienaoban.biologydictionary.forge;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.Lang;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Lang.BIOLOGY_DICTIONARY)
public final class BiologyDictionaryForge {
    public BiologyDictionaryForge(IEventBus modBus) {
        modBus.addListener(BiologyDictionaryForge::initCommon);
    }

    private static void initCommon(FMLCommonSetupEvent event) {
        BiologyDictionary.BD.forceInitialize();
    }
}
