package io.github.xienaoban.biologydictionary.platform.net;

import io.github.xienaoban.biologydictionary.net.PacketPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

public final class ClientNetRegistrar {
    private ClientNetRegistrar() {}

    public static void registerClientReceivers(RegisterClientPayloadHandlersEvent event) {
        for (PacketPayloads.Entry<?> payload : PacketPayloads.ENTRIES) {
            registerClientReceiver(event, payload);
        }
    }

    private static <T extends Packet> void registerClientReceiver(RegisterClientPayloadHandlersEvent event,
                                                                  PacketPayloads.Entry<T> payload) {
        registerClientReceiver(event, payload.packetClass());
    }

    private static <T extends Packet> void registerClientReceiver(RegisterClientPayloadHandlersEvent event,
                                                                  Class<T> clazz) {
        PacketUtil.registerType(clazz);

        if (PacketUtil.hasClientReceiver(clazz)) {
            CustomPacketPayload.Type<T> type = PacketUtil.getType(clazz);
            event.register(type, (payload, context) -> {
                Minecraft client = Minecraft.getInstance();
                LocalPlayer player = client.player;
                ClientNetApi.Context ctx = new ClientNetApi.Context(client, player);
                payload.clientReceive(ctx);
            });
        }
    }
}
