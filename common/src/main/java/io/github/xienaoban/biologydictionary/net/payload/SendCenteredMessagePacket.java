package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
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

    @ClientOnly
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        @ClientOnly final class CO { static void receive(SendCenteredMessagePacket packet, ClientNetApi.Context ctx) {
            BiologyDictionaryClient.sendCenteredMessage(packet.message());
        }}
        CO.receive(this, ctx);
    }
}
