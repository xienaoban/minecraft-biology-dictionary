package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import net.fabricmc.api.ModInitializer;

public final class FabricBiologyDictionary implements ModInitializer {
	@Override
	public void onInitialize() {
		BiologyDictionary.BD.forceInitialize();
	}
}
