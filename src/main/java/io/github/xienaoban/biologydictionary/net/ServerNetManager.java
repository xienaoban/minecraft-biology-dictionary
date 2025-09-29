package io.github.xienaoban.biologydictionary.net;

import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.net.payload.SendCenteredMessagePacket;
import io.github.xienaoban.biologydictionary.net.payload.ReplyHighlightEntitiesPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

public final class ServerNetManager {
    public static void init() {
        PacketPayloads.LIST.forEach(ServerNetApi::register);
    }

    public static void sendCenteredMessage(ServerPlayer player, Component message) {
        ServerNetApi.send(player, new SendCenteredMessagePacket(message));
    }

    public static void replyHighlightEntitiesSkill(ServerPlayer player, boolean allowed, EntityType<?> entityType, float radius) {
        ServerNetApi.send(player, new ReplyHighlightEntitiesPacket(allowed, entityType, radius));
    }
}
