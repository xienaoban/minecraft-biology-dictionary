package io.github.xienaoban.biologydictionary;

import net.fabricmc.api.ModInitializer;

public final class FabricBiologyDictionary implements ModInitializer {
	@Override
	public void onInitialize() {
		BiologyDictionary.BD.forceInitialize();
	}
}
