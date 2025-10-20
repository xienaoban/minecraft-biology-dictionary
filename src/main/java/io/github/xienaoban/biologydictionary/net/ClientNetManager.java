package io.github.xienaoban.biologydictionary.net;

import io.github.xienaoban.biologydictionary.common.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.net.payload.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public final class ClientNetManager {
    public static void init() {
        PacketPayloads.LIST.forEach(ClientNetApi::register);
    }

    public static void requestBookItem() {
        ClientNetApi.send(new RequestHandbookItemPacket());
    }

    public static void requestEntityData(Entity entity) {
        ClientNetApi.send(new RequestEntityDataPacket(entity.getId()));
    }

    public static void requestBeehiveInfo(BlockPos pos) {
        ClientNetApi.send(new RequestBeehiveInfoPacket(pos));
    }

    public static boolean sendCommonSkill(String skillKey, Object... args) {
        RequestCommonSkillPacket packet = RequestCommonSkillPacket.of(skillKey, args);
        if (packet == null) { return false; }
        ClientNetApi.send(packet);
        return true;
    }

    public static boolean sendEntityOrientedSkill(String skillKey, Entity entity, Object... args) {
        RequestEntityOrientedSkillPacket packet = RequestEntityOrientedSkillPacket.of(skillKey, entity, args);
        if (packet == null) { return false; }
        ClientNetApi.send(packet);
        return true;
    }
}
