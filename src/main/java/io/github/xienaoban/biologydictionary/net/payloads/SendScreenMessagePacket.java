package io.github.xienaoban.biologydictionary.net.payloads;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.PacketPayloadMeta;
import io.github.xienaoban.biologydictionary.common.util.McClientUtils;
import io.github.xienaoban.biologydictionary.gui.screen.AbstractBiologyDictionaryScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public record SendScreenMessagePacket(Component message, int color) implements Packet {
    public static final PacketPayloadMeta<?> META = PacketPayloadMeta.create();

    @Override
    public Type<? extends Packet> type() { return META.type(); }

    @SuppressWarnings("unused")
    public SendScreenMessagePacket(FriendlyByteBuf buf) {
        this(ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buf), buf.readInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buf, message);
        buf.writeInt(color);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        if (McClientUtils.getCurrentScreen() instanceof AbstractBiologyDictionaryScreen screen) {
            screen.sendScreenMessage(message, color);
        } else {
            McClientUtils.showClientTextBoxMessage(Component.translatable(Lang.TEXT_INFO_FROM_THIS_MOD, message).withColor(color));
        }
    }
}
