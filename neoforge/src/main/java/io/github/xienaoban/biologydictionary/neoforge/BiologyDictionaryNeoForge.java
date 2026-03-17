package io.github.xienaoban.biologydictionary.neoforge;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.Lang;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Lang.BIOLOGY_DICTIONARY)
public final class BiologyDictionaryNeoForge {
    public BiologyDictionaryNeoForge() {
        BiologyDictionary.BD.forceInitialize();
    }
}
