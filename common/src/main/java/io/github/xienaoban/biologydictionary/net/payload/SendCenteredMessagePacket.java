package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public record SendCenteredMessagePacket(Component message) implements Packet {
    public static final Packet.Factory<SendCenteredMessagePacket> FACTORY = SendCenteredMessagePacket::new;

    private SendCenteredMessagePacket(FriendlyByteBuf buf) {
        this(ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buf));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buf, message);
    }

    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        final class ClientOnly { static void receive(SendCenteredMessagePacket packet, ClientNetApi.Context ctx) {
            BiologyDictionaryClient.sendCenteredMessage(packet.message());
        }}
        ClientOnly.receive(this, ctx);
    }
}
