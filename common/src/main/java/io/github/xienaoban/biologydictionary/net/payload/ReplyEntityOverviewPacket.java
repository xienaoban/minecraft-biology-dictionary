package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.client.EntityTypeOverviewCache;
import io.github.xienaoban.biologydictionary.gui.screen.BdEntityOverviewScreen;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Reply packet for entity type reference properties.
 * Server sends this to client with the overview data.
 */
public record ReplyEntityOverviewPacket(boolean notNull, String entityTypeId,
                                         CompoundTag vanillaNbt, CompoundTag extraNbt) implements Packet {
    public static final Packet.Factory<ReplyEntityOverviewPacket> FACTORY = ReplyEntityOverviewPacket::new;

    private ReplyEntityOverviewPacket(FriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readUtf(), buf.readNbt(), buf.readNbt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(notNull);
        buf.writeUtf(entityTypeId);
        buf.writeNbt(vanillaNbt);
        buf.writeNbt(extraNbt);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        final class W {
            static void receive(ReplyEntityOverviewPacket packet, ClientNetApi.Context ctx) {
                if (!packet.notNull()) { return; }

                try {
                    EntityTypeOverviewCache.put(packet.entityTypeId(), packet.vanillaNbt(), packet.extraNbt());

                    // Update current screen if it's an overview screen for this entity type
                    if (ClientUtils.getCurrentScreen() instanceof BdEntityOverviewScreen screen
                            && screen.matchesType(packet.entityTypeId())) {
                        screen.updateProperties(packet.vanillaNbt(), packet.extraNbt());
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to process entity overview reply for type: " + packet.entityTypeId(), e);
                }
            }
        }
        W.receive(this, ctx);
    }
}
