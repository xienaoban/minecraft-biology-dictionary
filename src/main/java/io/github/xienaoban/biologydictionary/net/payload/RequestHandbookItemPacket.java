package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.core.BiologyDictionaryItem;
import net.minecraft.network.FriendlyByteBuf;

public record RequestHandbookItemPacket() implements Packet {
    public static final Packet.Factory<RequestHandbookItemPacket> FACTORY = buf -> new RequestHandbookItemPacket();

    @Override
    public void write(FriendlyByteBuf buf) {}

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        if (PlayerUtils.isCreative(ctx.player())) {
            PlayerUtils.getInventory(ctx.player()).add(BiologyDictionaryItem.createBook());
        }
    }
}
