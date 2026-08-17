package io.github.xienaoban.biologydictionary.net;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.net.payload.*;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.Map;

public final class ServerNetManager {

    public static void init() {
        PacketPayloads.registerBuiltIn(ServerNetApi::register);
    }

    public static void sendCenteredMessage(ServerPlayer player, Component message) {
        ServerNetApi.send(player, new SendCenteredMessagePacket(message));
    }

    public static void replyServerConfigs(ServerPlayer player, String serverConfigsYaml) {
        ServerNetApi.send(player, new ReplyServerConfigsPacket(serverConfigsYaml));
    }

    public static void sendDiscoveryIncremental(ServerPlayer player, Entity entity, EntityType<?> entityType, DiscoveryRecord record) {
        ServerNetApi.send(player, new SendDiscoveryIncrementalPacket(EntityUtils.getId(entity), entityType, record));
    }

    public static void replyDictionaryDiscoveryRecords(ServerPlayer player, Map<EntityType<?>, DiscoveryRecord> discoveries) {
        ServerNetApi.send(player, new ReplyBiologyDictionaryDiscoveryFullPacket(discoveries));
    }

    public static void replyHighlightEntitiesSkill(ServerPlayer player, boolean allowed, EntityType<?> entityType, float radius) {
        ServerNetApi.send(player, new ReplyHighlightEntitiesPacket(allowed, entityType, radius));
    }

    public static void replyInventoryStealingScreen(ServerPlayer player, int counter, Entity entity, Container container) {
        ServerNetApi.send(player, new ReplyInventoryStealingScreenPacket(counter, EntityUtils.getId(entity), container.getContainerSize()));
    }
}
