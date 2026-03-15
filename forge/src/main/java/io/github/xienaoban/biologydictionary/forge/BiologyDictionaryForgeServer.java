package io.github.xienaoban.biologydictionary.forge;

import dev.architectury.networking.NetworkManager;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.net.PacketPayloads;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.PacketUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;

@OnlyIn(Dist.DEDICATED_SERVER)
@Mod(value = Lang.BIOLOGY_DICTIONARY, dist = Dist.DEDICATED_SERVER)
public class BiologyDictionaryForgeServer {
    public BiologyDictionaryForgeServer(IEventBus modBus) {
        modBus.addListener(BiologyDictionaryForgeServer::initServer);
    }

    private static void initServer(FMLDedicatedServerSetupEvent event) {
        PacketPayloads.registerBuiltIn(new PacketPayloads.Registrar() {
            @Override
            public <T extends Packet> void register(Class<T> packetClass, Packet.Factory<T> factory) {
                if (PacketUtil.hasClientReceiver(packetClass)) {
                    CustomPacketPayload.Type<T> type = PacketUtil.getType(packetClass);
                    StreamCodec<FriendlyByteBuf, T> codec = PacketUtil.generateCodec(factory);

                    NetworkManager.registerS2CPayloadType(type, codec);
                }
            }
        });
    }
}
