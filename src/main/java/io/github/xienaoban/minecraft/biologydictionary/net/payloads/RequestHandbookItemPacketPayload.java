package io.github.xienaoban.minecraft.biologydictionary.net.payloads;

import io.github.xienaoban.minecraft.biologydictionary.core.BiologyDictionaryItem;
import io.github.xienaoban.minecraft.biologydictionary.platform.net.PacketPayload;
import io.github.xienaoban.minecraft.biologydictionary.platform.net.PacketPayloadMeta;
import io.github.xienaoban.minecraft.biologydictionary.platform.net.ServerNetApi;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public record RequestHandbookItemPacketPayload() implements PacketPayload {
    public static final PacketPayloadMeta<?> META = PacketPayloadMeta.create();

    @Override
    public @NotNull Type<? extends PacketPayload> type() { return META.type(); }

    @SuppressWarnings("unused")
    public RequestHandbookItemPacketPayload(FriendlyByteBuf buf) { this(); }

    @Override
    public void write(FriendlyByteBuf buf) {}

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        ctx.player().getInventory().add(BiologyDictionaryItem.createBook());
    }
}
