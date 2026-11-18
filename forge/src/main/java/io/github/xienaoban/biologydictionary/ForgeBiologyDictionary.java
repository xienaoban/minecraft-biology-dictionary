package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.platform.net.ServerNetRegistrar;
import io.github.xienaoban.biologydictionary.platform.server.CommandRegistrar;
import io.github.xienaoban.biologydictionary.platform.server.CreativeTabRegistrar;
import io.github.xienaoban.biologydictionary.platform.server.ServerEventRegistrar;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(BiologyDictionary.MOD_ID)
public final class ForgeBiologyDictionary {
    public ForgeBiologyDictionary(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ServerNetRegistrar.register();
        CreativeTabRegistrar.register(modEventBus);
        ServerEventRegistrar.register();
        CommandRegistrar.register();
        BiologyDictionary.forceInitialize();

        // Forge 1.20.1's @Mod has no dist attribute, so the client half is wired from here instead of
        // being a second entry point. The dist check keeps the client class off dedicated servers.
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ForgeBiologyDictionaryClient.init(context);
        }
    }
}
