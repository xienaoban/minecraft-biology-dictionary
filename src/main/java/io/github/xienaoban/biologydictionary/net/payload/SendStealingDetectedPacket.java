package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.common.net.Packet;
import io.github.xienaoban.biologydictionary.common.net.PacketPayloadMeta;
import io.github.xienaoban.biologydictionary.common.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public record SendStealingDetectedPacket(int entityId) implements Packet {
    public static final PacketPayloadMeta<?> META = PacketPayloadMeta.create();

    @Override
    public CustomPacketPayload.Type<? extends Packet> type() { return META.type(); }

    @SuppressWarnings("unused")
    public SendStealingDetectedPacket(FriendlyByteBuf buf) {
        this(buf.readInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
    }

    @Override
    public void serverReceive(ServerNetApi.Context ctx) {
        ServerPlayer player = ctx.player();
        ServerLevel level = (ServerLevel) EntityUtils.getLevel(player);

        Entity entity = level.getEntity(entityId);
        if (entity instanceof LivingEntity livingEntity) {
            // Deal 0 damage to the entity to trigger its attack response
            DamageSource damageSource = level.damageSources().playerAttack(player);
            livingEntity.hurtServer(level, damageSource, 0.0f);

            // Also deal 0 damage to the player
            DamageSource entityDamageSource = level.damageSources().mobAttack(livingEntity);
            player.hurtServer(level, entityDamageSource, 0.01f);
        }
    }
}
