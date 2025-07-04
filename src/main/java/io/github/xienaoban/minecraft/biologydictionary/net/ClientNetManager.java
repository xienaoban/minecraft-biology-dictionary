package io.github.xienaoban.minecraft.biologydictionary.net;

import io.github.xienaoban.minecraft.biologydictionary.common.net.ClientNetApi;
import io.github.xienaoban.minecraft.biologydictionary.net.payloads.RequestBeehiveInfoPacket;
import io.github.xienaoban.minecraft.biologydictionary.net.payloads.RequestEntityDataPacket;
import io.github.xienaoban.minecraft.biologydictionary.net.payloads.RequestHandbookItemPacket;
import io.github.xienaoban.minecraft.biologydictionary.net.payloads.SendUpdatedEntityPropertiesPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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

    public static void sendUpdatedEntityProperties(Entity entity, CompoundTag vanillaNbt, CompoundTag extraNbt) {
        ClientNetApi.send(new SendUpdatedEntityPropertiesPacket(entity.getId(), vanillaNbt, extraNbt));
    }
}
