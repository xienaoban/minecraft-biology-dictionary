package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.core.BiologyDictionaryItem;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import net.minecraft.network.FriendlyByteBuf;

public record RequestBiologyDictionaryItemPacket() implements Packet {
    public static final Packet.Factory<RequestBiologyDictionaryItemPacket> FACTORY =
            buf -> new RequestBiologyDictionaryItemPacket();

    @Override
    public void write(FriendlyByteBuf buf) {}

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        if (PlayerUtils.isCreative(ctx.player())) {
            PlayerUtils.getInventory(ctx.player()).add(BiologyDictionaryItem.createBook());
        }
    }
}
