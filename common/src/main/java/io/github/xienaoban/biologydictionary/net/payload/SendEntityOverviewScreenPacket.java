package io.github.xienaoban.biologydictionary.net.payload;

import io.github.xienaoban.biologydictionary.core.EntityManager;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.gui.screen.BdEntityOverviewScreen;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.net.ClientNetApi;
import io.github.xienaoban.biologydictionary.platform.net.Packet;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;

/**
 * Server asks the client to open the entity overview screen for an entity type.
 */
public record SendEntityOverviewScreenPacket(String entityTypeId) implements Packet {
    public static final Packet.Factory<SendEntityOverviewScreenPacket> FACTORY = SendEntityOverviewScreenPacket::new;

    private SendEntityOverviewScreenPacket(FriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(entityTypeId);
    }

    @ClientOnly
    @Override
    public void clientReceive(ClientNetApi.Context ctx) {
        @ClientOnly final class CO { static void receive(SendEntityOverviewScreenPacket packet) {
            WorldSession ws = WorldSession.get();
            if (ws == null || ClientWorldSession.get() == null) {
                return;
            }
            EntityType<?> entityType = EntityUtils.getEntityType(packet.entityTypeId());
            if (entityType == null) {
                return;
            }
            EntityManager.EntityDictionaryEntry entry = ws.getEntityManager().getEntityEntry(entityType);
            if (entry == null || !BdEntityOverviewScreen.canOpen(entry)) {
                return;
            }
            BdEntityOverviewScreen screen = new BdEntityOverviewScreen(entry);
            ClientUtils.setScreen(screen);
            screen.initOrRequestProperties();
        }}
        CO.receive(this);
    }
}
