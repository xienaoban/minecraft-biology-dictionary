package io.github.xienaoban.minecraft.biologydictionary.net;

import io.github.xienaoban.minecraft.biologydictionary.net.payloads.RequestBeehiveInfoPacketPayload;
import io.github.xienaoban.minecraft.biologydictionary.net.payloads.RequestEntityDataPacketPayload;
import io.github.xienaoban.minecraft.biologydictionary.net.payloads.RequestHandbookItemPacketPayload;
import io.github.xienaoban.minecraft.biologydictionary.common.net.ClientNetApi;
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
        ClientNetApi.send(new RequestHandbookItemPacketPayload());
    }

    public static void requestEntityData(Entity entity) {
        ClientNetApi.send(new RequestEntityDataPacketPayload(entity.getId()));
    }

    public static void requestBeehiveInfo(BlockPos pos) {
        ClientNetApi.send(new RequestBeehiveInfoPacketPayload(pos));
    }
}
