package io.github.xienaoban.biologydictionary;

import net.neoforged.fml.common.Mod;

@Mod(BiologyDictionary.MOD_ID)
public final class NeoForgeBiologyDictionary {
	public NeoForgeBiologyDictionary() {
		BiologyDictionary.BD.forceInitialize();
	}
}
