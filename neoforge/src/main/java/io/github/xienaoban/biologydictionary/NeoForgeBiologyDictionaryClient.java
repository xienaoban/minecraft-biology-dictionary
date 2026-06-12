package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.client.KeyMappings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@Mod(value = BiologyDictionary.MOD_ID, dist = Dist.CLIENT)
public final class NeoForgeBiologyDictionaryClient {
	public NeoForgeBiologyDictionaryClient(IEventBus modEventBus) {
		modEventBus.addListener(NeoForgeBiologyDictionaryClient::registerKeyMappings);
	}

	private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.registerCategory(KeyMappings.CATEGORY);
		event.register(KeyMappings.OPEN_HANDBOOK);
		event.register(KeyMappings.DEBUG);
	}
}
