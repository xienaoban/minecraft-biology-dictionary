package xienaoban.minecraft.biologydictionary.client.forge;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import xienaoban.minecraft.biologydictionary.client.KeyMappingManager;

@SuppressWarnings("unused")
public class KeyMappingManagerImpl {
    public static void init() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus mainBus = MinecraftForge.EVENT_BUS;

        modBus.addListener(KeyMappingManagerImpl::registerKeyMappings);
        mainBus.addListener(KeyMappingManagerImpl::registerClientTick);
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyMappingManager.KEY_BOLE_SCREEN);
    }

    public static void registerClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft client = Minecraft.getInstance();
            KeyMappingManager.onClientTickEnd(client);
        }
    }
}
