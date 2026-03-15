package io.github.xienaoban.biologydictionary.forge;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.ClothConfigScreenProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@OnlyIn(Dist.CLIENT)
@Mod(value = Lang.BIOLOGY_DICTIONARY, dist = Dist.CLIENT)
public class BiologyDictionaryForgeClient {
    public BiologyDictionaryForgeClient(IEventBus modBus, ModContainer container) {
        modBus.addListener(BiologyDictionaryForgeClient::initClient);

        container.registerExtensionPoint(IConfigScreenFactory.class,
                (modContainer, arg) -> ClothConfigScreenProvider.provideScreen(arg));
    }

    private static void initClient(FMLClientSetupEvent event) {
        BiologyDictionaryClient.BDC.forceInitialize();
    }
}
