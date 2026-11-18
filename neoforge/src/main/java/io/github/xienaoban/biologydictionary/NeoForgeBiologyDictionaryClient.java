package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.client.KeyMappings;
import io.github.xienaoban.biologydictionary.config.ClothConfigScreenProvider;
import io.github.xienaoban.biologydictionary.platform.client.ClientEventRegistrar;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = BiologyDictionary.MOD_ID, dist = Dist.CLIENT)
public final class NeoForgeBiologyDictionaryClient {
    public NeoForgeBiologyDictionaryClient(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(NeoForgeBiologyDictionaryClient::registerKeyMappings);
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (modContainer, parent) -> ClothConfigScreenProvider.provideScreen(parent));
        ClientEventRegistrar.register();
        BiologyDictionaryClient.forceInitialize();
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        // NeoForge 21.1 has no key-mapping category API, so KeyMappings uses a plain translation key.
        event.register(KeyMappings.OPEN_HANDBOOK);
        event.register(KeyMappings.STEAL_INVENTORY);
    }
}
