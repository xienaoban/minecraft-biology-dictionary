package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.EntityProperty;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Request packet for getting entity type reference properties.
 * Client sends this to server to request default/overview data for an entity type.
 */
public record RequestEntityOverviewPacket(String entityTypeId) implements Packet {
    public static final Packet.Factory<RequestEntityOverviewPacket> FACTORY = RequestEntityOverviewPacket::new;

    private RequestEntityOverviewPacket(FriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(entityTypeId);
    }

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        EntityType<?> entityType = EntityUtils.getEntityType(entityTypeId);
        ReplyEntityOverviewPacket toSend;

        if (entityType == null) {
            LOGGER.error("Unknown entity type: {}", entityTypeId);
            toSend = new ReplyEntityOverviewPacket(false, entityTypeId, null, null);
        } else {
            try {
                ServerLevel serverLevel = ctx.player().serverLevel();
                Entity entity = EntityUtils.create(entityType, serverLevel);

                // Initialize goal/ai for mob entities
                if (entity instanceof Mob mob) {
                    mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(entity.blockPosition()),
                                      MobSpawnType.NATURAL, null);
                }

                CompoundTag vanillaNbt = EntityUtils.getNbt(entity);
                CompoundTag extraNbt = new CompoundTag();
                for (EntityProperty<?> p : new EntityProperties<>(entity).getExtras()) {
                    p.getFrom(Misc.cast(entity));
                    p.writeTo(extraNbt);
                }

                toSend = new ReplyEntityOverviewPacket(true, entityTypeId, vanillaNbt, extraNbt);
            } catch (Exception e) {
                LOGGER.error("Failed to create entity overview for type: " + entityTypeId, e);
                toSend = new ReplyEntityOverviewPacket(false, entityTypeId, null, null);
            }
        }

        ServerNetApi.send(ctx.player(), toSend);
    }
}
