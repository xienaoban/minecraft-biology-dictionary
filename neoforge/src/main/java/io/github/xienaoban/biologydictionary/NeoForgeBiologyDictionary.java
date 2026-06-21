package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.platform.net.ServerNetRegistrar;
import io.github.xienaoban.biologydictionary.platform.server.CommandRegistrar;
import io.github.xienaoban.biologydictionary.platform.server.CreativeTabRegistrar;
import io.github.xienaoban.biologydictionary.platform.server.ServerEventRegistrar;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(BiologyDictionary.MOD_ID)
public final class NeoForgeBiologyDictionary {
    public NeoForgeBiologyDictionary(IEventBus modEventBus) {
        modEventBus.addListener(NeoForgeBiologyDictionary::registerPayloads);
        CreativeTabRegistrar.register(modEventBus);
        ServerEventRegistrar.register();
        CommandRegistrar.register();
        BiologyDictionary.BD.forceInitialize();
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        ServerNetRegistrar.registerPayloads(event);
    }
}
