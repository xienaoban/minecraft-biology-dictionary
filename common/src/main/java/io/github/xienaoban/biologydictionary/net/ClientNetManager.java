package io.github.xienaoban.biologydictionary.net;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.GeneralSkill;
import io.github.xienaoban.biologydictionary.net.payload.*;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

@Environment(EnvType.CLIENT)
public final class ClientNetManager {

    public static void init() {
        PacketPayloads.registerBuiltIn(ClientNetApi::register);
    }

    public static void requestServerConfigs() {
        ClientNetApi.send(new RequestServerConfigsPacket());
    }

    public static void requestBookItem() {
        ClientNetApi.send(new RequestBiologyDictionaryItemPacket());
    }

    public static void requestBiologyDictionaryDiscoveryFull() {
        ClientNetApi.send(new RequestBiologyDictionaryDiscoveryFullPacket());
    }

    public static void sendBiologyDictionaryDiscoveryIncremental(EntityType<?> entityType, DiscoveryRecord record) {
        ClientNetApi.send(new SendBiologyDictionaryDiscoveryIncrementalPacket(entityType, record));
    }

    public static void requestEntityOverview(EntityType<?> entityType) {
        ClientNetApi.send(new RequestEntityOverviewPacket(EntityUtils.getEntityTypeIdName(entityType)));
    }

    public static void requestEntityData(Entity entity) {
        ClientNetApi.send(new RequestEntityDataPacket(EntityUtils.getId(entity)));
    }

    public static void requestBeehiveInfo(BlockPos pos) {
        ClientNetApi.send(new RequestBeehiveInfoPacket(pos));
    }

    public static void sendCommonSkill(GeneralSkill skill) {
        ClientNetApi.send(new RequestCommonSkillPacket(skill));
    }

    public static void sendEntityTargetedSkill(Entity entity, EntityTargetedSkill<?> skill) {
        ClientNetApi.send(new RequestEntityTargetedSkillPacket(EntityUtils.getId(entity), skill));
    }

    public static void sendStealingDetected(Entity entity) {
        ClientNetApi.send(new SendStealingDetectedPacket(EntityUtils.getId(entity)));
    }
}
