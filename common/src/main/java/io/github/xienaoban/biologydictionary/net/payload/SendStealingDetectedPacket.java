package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.net.ServerNetApi;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public record SendStealingDetectedPacket(int entityId) implements Packet {
	public static final Packet.Factory<SendStealingDetectedPacket> FACTORY = SendStealingDetectedPacket::new;

	private SendStealingDetectedPacket(FriendlyByteBuf buf) {
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
			DamageSource damageSource = level.damageSources().playerAttack(player);
			EntityUtils.hurt(livingEntity, damageSource, 0.0F);

			DamageSource entityDamageSource = level.damageSources().mobAttack(livingEntity);
			EntityUtils.hurt(player, entityDamageSource, 0.01F);
		}
	}
}
