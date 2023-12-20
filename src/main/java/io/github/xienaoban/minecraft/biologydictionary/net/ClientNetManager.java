package io.github.xienaoban.minecraft.biologydictionary.net;

import io.github.xienaoban.minecraft.biologydictionary.platform.net.ClientNetApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary.LOGGER;
import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionaryClient.BDC;

@Environment(EnvType.CLIENT)
public final class ClientNetManager {
    public static void init() {
        registerSendEntityData();
    }

    private static void registerSendEntityData() {
        ClientNetApi.registerReceiver(Channels.SEND_ENTITY_DATA, (client, handler, buf, responseSender) -> {
            boolean notNull = buf.readBoolean();
            if (!notNull) return;
            Entity entity = BDC.getHitEntity();
            if (entity == null || entity.getId() != buf.readInt()) return;
            CompoundTag vanillaNbt = buf.readNbt();
            CompoundTag additionalNbt = buf.readNbt();
            client.execute(() -> {
                LOGGER.info("vanillaNbt = " + vanillaNbt);
                LOGGER.info("additionalNbt = " + additionalNbt);
            });
        });
    }

    public static void requestBookItem() {
        ClientNetApi.send(Channels.REQUEST_BOOK_ITEM, PacketByteBufs.empty());
    }

    public static void requestEntityData(Entity entity) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(entity.getId());
        ClientNetApi.send(Channels.REQUEST_ENTITY_DATA, buf);
    }
}
