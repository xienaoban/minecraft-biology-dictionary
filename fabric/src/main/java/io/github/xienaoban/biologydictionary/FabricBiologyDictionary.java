package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.platform.net.ServerNetRegistrar;
import io.github.xienaoban.biologydictionary.platform.server.CommandRegistrar;
import io.github.xienaoban.biologydictionary.platform.server.CreativeTabRegistrar;
import io.github.xienaoban.biologydictionary.platform.server.ServerEventRegistrar;
import net.fabricmc.api.ModInitializer;

public final class FabricBiologyDictionary implements ModInitializer {
	@Override
	public void onInitialize() {
		BiologyDictionary.BD.forceInitialize();
		ServerNetRegistrar.registerCommonPayloads();
		CreativeTabRegistrar.register();
		ServerEventRegistrar.register();
		CommandRegistrar.register();
	}
}
