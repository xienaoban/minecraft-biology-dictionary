package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.client.KeyMappings;
import io.github.xienaoban.biologydictionary.config.ClothConfigScreenProvider;
import io.github.xienaoban.biologydictionary.platform.client.ClientEventRegistrar;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetRegistrar;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

@Mod(value = BiologyDictionary.MOD_ID, dist = Dist.CLIENT)
public final class NeoForgeBiologyDictionaryClient {
	public NeoForgeBiologyDictionaryClient(IEventBus modEventBus, ModContainer container) {
		modEventBus.addListener(NeoForgeBiologyDictionaryClient::registerClientReceivers);
		modEventBus.addListener(NeoForgeBiologyDictionaryClient::registerKeyMappings);
		container.registerExtensionPoint(IConfigScreenFactory.class,
				(modContainer, parent) -> ClothConfigScreenProvider.provideScreen(parent));
		ClientEventRegistrar.register();
	}

	private static void registerClientReceivers(RegisterClientPayloadHandlersEvent event) {
		ClientNetRegistrar.registerClientReceivers(event);
	}

	private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.registerCategory(KeyMappings.CATEGORY);
		event.register(KeyMappings.OPEN_HANDBOOK);
		event.register(KeyMappings.DEBUG);
	}
}
