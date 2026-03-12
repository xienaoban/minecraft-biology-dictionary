package io.github.xienaoban.biologydictionary.neoforge;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.ClothConfigScreenProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@OnlyIn(Dist.CLIENT)
@Mod(value = Lang.BIOLOGY_DICTIONARY, dist = Dist.CLIENT)
public class BiologyDictionaryNeoForgeClient {
    public BiologyDictionaryNeoForgeClient(IEventBus modBus, ModContainer container) {
        modBus.addListener(BiologyDictionaryNeoForgeClient::initClient);

        container.registerExtensionPoint(IConfigScreenFactory.class,
                (modContainer, arg) -> ClothConfigScreenProvider.provideScreen(arg));
    }

    private static void initClient(FMLClientSetupEvent event) {
        BiologyDictionaryClient.BDC.forceInitialize();
    }
}
