package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.core.EntityManager;
import io.github.xienaoban.biologydictionary.core.EntityOverviewCache;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.gui.screen.BdEntityOverviewScreen;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;

/**
 * Entity overview data and optional open-screen request: S -> C.
 */
public record SendEntityOverviewPacket(String entityTypeId, boolean hasData, CompoundTag vanillaNbt, CompoundTag extraNbt,
                                       boolean openScreen) implements Packet {
    public static final Packet.Factory<SendEntityOverviewPacket> FACTORY = SendEntityOverviewPacket::new;

    private SendEntityOverviewPacket(FriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readBoolean(), buf.readNbt(), buf.readNbt(), buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(entityTypeId);
        buf.writeBoolean(hasData);
        buf.writeNbt(vanillaNbt);
        buf.writeNbt(extraNbt);
        buf.writeBoolean(openScreen);
    }

    @ClientOnly
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        @ClientOnly final class CO { static void receive(SendEntityOverviewPacket packet) {
            WorldSession ws = WorldSession.get();
            if (ws == null || ClientWorldSession.get() == null) {
                return;
            }
            EntityType<?> entityType = EntityUtils.getEntityType(packet.entityTypeId());
            if (entityType == null) {
                return;
            }

            if (packet.hasData()) {
                ws.getEntityOverviewCache().put(entityType,
                        new EntityOverviewCache.CacheEntry(packet.vanillaNbt(), packet.extraNbt()));
                if (ClientUtils.getCurrentScreen() instanceof BdEntityOverviewScreen screen
                        && screen.matchesType(entityType)) {
                    screen.updateProperties(packet.vanillaNbt(), packet.extraNbt());
                }
            }

            if (packet.openScreen()) {
                EntityManager.EntityDictionaryEntry entry = ws.getEntityManager().getEntityEntry(entityType);
                if (entry == null || !BdEntityOverviewScreen.canOpen(entry)) {
                    return;
                }
                BdEntityOverviewScreen screen = new BdEntityOverviewScreen(entry);
                ClientUtils.setScreen(screen);
                screen.initOrRequestProperties();
            }
        }}
        CO.receive(this);
    }
}
