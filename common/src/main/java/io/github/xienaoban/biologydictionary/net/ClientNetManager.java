package io.github.xienaoban.biologydictionary.net;

import io.github.xienaoban.biologydictionary.common.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.GeneralSkill;
import io.github.xienaoban.biologydictionary.net.payload.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public final class ClientNetManager {

    public static void init() {
        PacketPayloads.registerBuiltIn(ClientNetApi::register);
    }

    public static void requestBookItem() {
        ClientNetApi.send(new RequestBiologyDictionaryItemPacket());
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

    public static void requestServerConfigs() {
        ClientNetApi.send(new RequestServerConfigsPacket());
    }
}
