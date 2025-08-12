package io.github.xienaoban.biologydictionary.net;

import io.github.xienaoban.biologydictionary.common.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.net.payloads.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

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

    public static void requestEntityHighlighting(EntityType<?> entityType, float radius) {
        ClientNetApi.send(new RequestEntityHighlightingPacket(entityType, radius));
    }

    public static void requestSpawnEgg(EntityType<?> entityType) {
        ClientNetApi.send(new RequestSpawnEggPacket(entityType));
    }

    public static void requestBeehiveInfo(BlockPos pos) {
        ClientNetApi.send(new RequestBeehiveInfoPacket(pos));
    }

    public static void sendUpdatedEntityProperties(Entity entity, CompoundTag vanillaNbt, CompoundTag extraNbt) {
        ClientNetApi.send(new SendUpdatedEntityPropertiesPacket(entity.getId(), vanillaNbt, extraNbt));
    }
}
