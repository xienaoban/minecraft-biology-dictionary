package io.github.xienaoban.biologydictionary.neoforge;

import dev.architectury.networking.NetworkManager;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.PacketUtil;
import io.github.xienaoban.biologydictionary.net.PacketPayloads;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;

@OnlyIn(Dist.DEDICATED_SERVER)
@Mod(value = Lang.BIOLOGY_DICTIONARY, dist = Dist.DEDICATED_SERVER)
public class BiologyDictionaryNeoForgeServer {
    public BiologyDictionaryNeoForgeServer(IEventBus modBus) {
        modBus.addListener(BiologyDictionaryNeoForgeServer::initServer);
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
