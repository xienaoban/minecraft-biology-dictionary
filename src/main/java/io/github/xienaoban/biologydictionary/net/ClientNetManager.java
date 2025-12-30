package io.github.xienaoban.biologydictionary.net;

import io.github.xienaoban.biologydictionary.common.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.net.payload.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public final class ClientNetManager {

    public static void init() {
        PacketPayloads.registerBuiltIn(ClientNetApi::register);
    }

    public static void requestBookItem() {
        ClientNetApi.send(new RequestHandbookItemPacket());
    }

    public static void requestEntityData(Entity entity) {
        ClientNetApi.send(new RequestEntityDataPacket(EntityUtils.getId(entity)));
    }

    public static void requestBeehiveInfo(BlockPos pos) {
        ClientNetApi.send(new RequestBeehiveInfoPacket(pos));
    }

    public static void sendCommonSkill(String skillKey, Tag nbtArgs) {
        ClientNetApi.send(new RequestCommonSkillPacket(skillKey, nbtArgs));
    }

    public static void sendEntityTargetedSkill(String skillKey, Entity entity, Tag nbtArgs) {
        ClientNetApi.send(new RequestEntityTargetedSkillPacket(skillKey, EntityUtils.getId(entity), nbtArgs));
    }

    public static void sendStealingDetected(Entity entity) {
        ClientNetApi.send(new SendStealingDetectedPacket(EntityUtils.getId(entity)));
    }
}
