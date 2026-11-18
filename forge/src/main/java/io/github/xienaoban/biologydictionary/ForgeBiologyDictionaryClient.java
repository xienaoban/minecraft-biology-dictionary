package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.client.KeyMappings;
import io.github.xienaoban.biologydictionary.config.ClothConfigScreenProvider;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.client.ClientEventRegistrar;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@ClientOnly
public final class ForgeBiologyDictionaryClient {
    private ForgeBiologyDictionaryClient() {}

    public static void init(FMLJavaModLoadingContext context) {
        context.getModEventBus().addListener(ForgeBiologyDictionaryClient::registerKeyMappings);
        ModContainer container = context.getContainer();
        container.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (client, parent) -> ClothConfigScreenProvider.provideScreen(parent)));
        ClientEventRegistrar.register();
        BiologyDictionaryClient.forceInitialize();
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyMappings.OPEN_HANDBOOK);
        event.register(KeyMappings.STEAL_INVENTORY);
    }
}
