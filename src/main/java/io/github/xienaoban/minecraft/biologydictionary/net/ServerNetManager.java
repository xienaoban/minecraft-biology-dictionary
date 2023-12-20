package io.github.xienaoban.minecraft.biologydictionary.net;

import io.github.xienaoban.minecraft.biologydictionary.core.BiologyDictionaryItem;
import io.github.xienaoban.minecraft.biologydictionary.core.EntityPropertyWidgetRegistryManager;
import io.github.xienaoban.minecraft.biologydictionary.platform.access.PlayerApi;
import io.github.xienaoban.minecraft.biologydictionary.platform.net.ServerNetApi;
import io.github.xienaoban.minecraft.biologydictionary.util.MiscUtil;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class ServerNetManager {
    public static void init() {
        registerRequestBookItem();
        registerRequestEntityData();
    }

    private static void registerRequestBookItem() {
        ServerNetApi.registerReceiver(Channels.REQUEST_BOOK_ITEM, (server, player, handler, buf, responseSender) -> {
            if (PlayerApi.isSurvival(player)) return;
            server.execute(() -> {
                player.getInventory().add(BiologyDictionaryItem.createBook());
            });
        });
    }

    private static void registerRequestEntityData() {
        ServerNetApi.registerReceiver(Channels.REQUEST_ENTITY_DATA, (server, player, handler, buf, responseSender) -> {
            int entityId = buf.readInt();
            server.execute(() -> {
                Entity entity = player.getCommandSenderWorld().getEntity(entityId);
                sendEntityData(server, player, entity);
            });
        });
    }

    public static void sendEntityData(MinecraftServer server, ServerPlayer player, Entity entity) {
        server.execute(() -> {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(entity != null);

            if (entity != null) {
                buf.writeInt(entity.getId());

                // Write vanilla NBT data.
                CompoundTag vanillaNbt = entity.saveWithoutId(new CompoundTag());
                entity.saveWithoutId(vanillaNbt);
                buf.writeNbt(vanillaNbt);

                // Write data that not in vanilla NBT.
                CompoundTag additionalNbt = new CompoundTag();
                for (var clazz : MiscUtil.topDown(entity)) {
                    for (var registry : EntityPropertyWidgetRegistryManager.getInstance().getRegistries(clazz)) {
                        for (var handler : registry.getEntityDataBufHandlers().values()) {
                            handler.write(additionalNbt, MiscUtil.cast(entity));
                        }
                    }
                }
                buf.writeNbt(additionalNbt);
            }

            ServerNetApi.send(player, Channels.SEND_ENTITY_DATA, buf);
        });
    }
}
