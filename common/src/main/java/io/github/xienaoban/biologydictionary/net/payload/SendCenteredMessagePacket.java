package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public record SendCenteredMessagePacket(Component message) implements Packet {
    public static final Packet.Factory<SendCenteredMessagePacket> FACTORY = SendCenteredMessagePacket::new;

    private SendCenteredMessagePacket(FriendlyByteBuf buf) {
        this(buf.readComponent());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeComponent(message);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        final class C { static void receive(SendCenteredMessagePacket packet, ClientNetApi.Context ctx) {
            BiologyDictionaryClient.sendCenteredMessage(packet.message());
        }}
        C.receive(this, ctx);
    }
}
