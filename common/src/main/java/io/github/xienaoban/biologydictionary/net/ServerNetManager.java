package io.github.xienaoban.biologydictionary.net;

import io.github.xienaoban.biologydictionary.net.payload.ReplyHighlightEntitiesPacket;
import io.github.xienaoban.biologydictionary.net.payload.ReplyInventoryStealingScreenPacket;
import io.github.xienaoban.biologydictionary.net.payload.ReplyServerConfigsPacket;
import io.github.xienaoban.biologydictionary.net.payload.SendCenteredMessagePacket;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public final class ServerNetManager {

    public static void init() {
        PacketPayloads.registerBuiltIn(ServerNetApi::register);
    }

    public static void sendCenteredMessage(ServerPlayer player, Component message) {
        ServerNetApi.send(player, new SendCenteredMessagePacket(message));
    }

    public static void replyHighlightEntitiesSkill(ServerPlayer player, boolean allowed, EntityType<?> entityType, float radius) {
        ServerNetApi.send(player, new ReplyHighlightEntitiesPacket(allowed, entityType, radius));
    }

    public static void replyInventoryStealingScreen(ServerPlayer player, int counter, Entity entity, Container container) {
        ServerNetApi.send(player, new ReplyInventoryStealingScreenPacket(counter, EntityUtils.getId(entity), container.getContainerSize()));
    }

    public static void replyServerConfigs(ServerPlayer player, String serverConfigsYaml) {
        ServerNetApi.send(player, new ReplyServerConfigsPacket(serverConfigsYaml));
    }
}
