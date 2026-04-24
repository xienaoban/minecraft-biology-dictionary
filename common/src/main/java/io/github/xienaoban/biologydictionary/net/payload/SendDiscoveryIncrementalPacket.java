package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.client.DiscoveryToast;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

/**
 * Server notifies client of a new discovery: S -> C.
 */
public record SendDiscoveryIncrementalPacket(int entityId, EntityType<?> entityType, DiscoveryRecord record) implements Packet {
    public static final Packet.Factory<SendDiscoveryIncrementalPacket> FACTORY = SendDiscoveryIncrementalPacket::new;

    private SendDiscoveryIncrementalPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), readEntityType(buf), DiscoveryRecord.readFromBuf(buf));
    }

    private static EntityType<?> readEntityType(FriendlyByteBuf buf) {
        ResourceLocation id = ResourceLocation.tryParse(buf.readUtf());
        return EntityUtils.getEntityType(id);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeUtf(EntityUtils.getEntityTypeIdName(entityType));
        record.writeToBuf(buf);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        final class W { static void receive(SendDiscoveryIncrementalPacket packet, ClientNetApi.Context ctx) {
            ClientWorldSession session = ClientWorldSession.get();
            if (session == null) { return; }

            Minecraft client = ctx.client();
            LocalPlayer player = ctx.player();
            ClientLevel level = ClientUtils.getClientLevel(client);

            // Update discovery cache
            session.getDiscoveryClientCache().incrementalSync(packet.entityType, packet.record);

            // Show toast
            client.getToasts().addToast(new DiscoveryToast(packet.entityType));

            // Swing if INTERACT
            if (packet.record.source() == DiscoverySource.INTERACT) {
                player.swing(InteractionHand.MAIN_HAND);
            }

            // Add some particles
            Entity target = level != null ? level.getEntity(packet.entityId) : null;
            if (target != null) {
                Vec3 center = target.getBoundingBox().getCenter();
                var rng = level.random;
                for (int i = 0; i < 6; i++) {
                    level.addParticle(ParticleTypes.END_ROD,
                            center.x + (rng.nextDouble() - 0.5) * target.getBbWidth(),
                            center.y + (rng.nextDouble() - 0.5) * target.getBbHeight(),
                            center.z + (rng.nextDouble() - 0.5) * target.getBbWidth(),
                            0, 0.05, 0);
                }
            }
        }}
        W.receive(this, ctx);
    }
}
