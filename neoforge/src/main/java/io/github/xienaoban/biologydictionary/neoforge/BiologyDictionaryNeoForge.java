package io.github.xienaoban.biologydictionary.neoforge;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.Lang;
import net.neoforged.fml.common.Mod;

@Mod(Lang.BIOLOGY_DICTIONARY)
public final class BiologyDictionaryNeoForge {
    public BiologyDictionaryNeoForge() {
        BiologyDictionary.BD.forceInitialize();
    }
}
