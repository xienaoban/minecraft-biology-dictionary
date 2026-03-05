package io.github.xienaoban.biologydictionary.neoforge;

import io.github.xienaoban.biologydictionary.Biologydictionary;
import net.neoforged.fml.common.Mod;

@Mod(Biologydictionary.MOD_ID)
public final class BiologydictionaryNeoForge {
    public BiologydictionaryNeoForge() {
        // Run our common setup.
        Biologydictionary.init();
    }
}
