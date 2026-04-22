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
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Server notifies client of a new discovery: S -> C.
 */
public record SendDiscoveryIncrementalPacket(EntityType<?> entityType, DiscoveryRecord record) implements Packet {
    public static final Packet.Factory<SendDiscoveryIncrementalPacket> FACTORY = SendDiscoveryIncrementalPacket::new;

    private SendDiscoveryIncrementalPacket(FriendlyByteBuf buf) {
        this(readEntityType(buf), DiscoveryRecord.readFromBuf(buf));
    }

    private static EntityType<?> readEntityType(FriendlyByteBuf buf) {
        Identifier id = Identifier.tryParse(buf.readUtf());
        return EntityUtils.getEntityType(id);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
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
            HitResult hitResult = client.hitResult;

            // Update discovery cache
            session.getDiscoveryClientCache().incrementalSync(packet.entityType, packet.record);

            // Show toast
            client.getToastManager().addToast(new DiscoveryToast(packet.entityType));

            // Swing if INTERACT
            if (packet.record.source() == DiscoverySource.INTERACT) {
                player.swing(InteractionHand.MAIN_HAND);
            }

            // Add some particles
            Entity target = hitResult != null && hitResult.getType() == HitResult.Type.ENTITY
                    ? ((EntityHitResult) hitResult).getEntity() : null;
            if (target != null && level != null) {
                Vec3 center = target.getBoundingBox().getCenter();
                var rng = level.random;
                for (int i = 0; i < 8; i++) {
                    double ox = (rng.nextDouble() - 0.5) * target.getBbWidth();
                    double oy = (rng.nextDouble() - 0.5) * target.getBbHeight();
                    double oz = (rng.nextDouble() - 0.5) * target.getBbWidth();
                    level.addParticle(ParticleTypes.END_ROD,
                            center.x + ox, center.y + oy, center.z + oz, 0, 0.5, 0);
                }
            }
        }}
        W.receive(this, ctx);
    }
}
