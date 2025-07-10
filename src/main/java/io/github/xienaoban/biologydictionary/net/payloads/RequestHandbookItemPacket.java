package io.github.xienaoban.biologydictionary.net.payloads;

import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.PacketPayloadMeta;
import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.core.BiologyDictionaryItem;
import net.minecraft.network.FriendlyByteBuf;

public record RequestHandbookItemPacket() implements Packet {
    public static final PacketPayloadMeta<?> META = PacketPayloadMeta.create();

    @Override
    public Type<? extends Packet> type() { return META.type(); }

    @SuppressWarnings("unused")
    public RequestHandbookItemPacket(FriendlyByteBuf buf) { this(); }

    @Override
    public void write(FriendlyByteBuf buf) {}

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        ctx.player().getInventory().add(BiologyDictionaryItem.createBook());
    }
}
