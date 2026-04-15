package io.github.xienaoban.biologydictionary.forge;

import dev.architectury.platform.forge.EventBuses;
import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.ClothConfigScreenProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(Lang.BIOLOGY_DICTIONARY)
public final class BiologyDictionaryForge {
    public BiologyDictionaryForge() {
        @SuppressWarnings("all")
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        @SuppressWarnings("all")
        ModContainer container = FMLJavaModLoadingContext.get().getContainer();

        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(BiologyDictionary.MOD_ID, modBus);

        BiologyDictionary.BD.forceInitialize();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            BiologyDictionaryClient.BDC.forceInitialize();

            container.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(
                            (mc, screen) -> ClothConfigScreenProvider.provideScreen(screen)
                    )
            );
        }
    }
}
