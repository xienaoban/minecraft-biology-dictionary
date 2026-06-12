package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.client.KeyMappings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

public final class FabricBiologyDictionaryClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		KeyMappingHelper.registerKeyMapping(KeyMappings.OPEN_HANDBOOK);
		KeyMappingHelper.registerKeyMapping(KeyMappings.DEBUG);
	}
}
