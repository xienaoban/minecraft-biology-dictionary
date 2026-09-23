package io.github.xienaoban.biologydictionary.core;

import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.session.ServerWorldSession;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.net.ServerNetManager;
import io.github.xienaoban.biologydictionary.net.payload.SendEntityOverviewPacket;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

/**
 * Server-side checks and packet construction for the entity overview screen.
 */
public final class ServerEntityOverviewManager {
    private ServerEntityOverviewManager() {}

    public static boolean canOpen(ServerPlayer player, EntityType<?> entityType) {
        ServerWorldSession sws = ServerWorldSession.get();
        return sws != null && (ConfigsManager.getServer().isAllowOverviewForUndiscoveredEntities()
                || PlayerUtils.isCreative(player)
                || sws.getDiscoveryManager().isDiscovered(player, entityType));
    }

    /**
     * Build and send overview data if the player may open it.
     *
     * @return false if the player has no permission to open the overview.
     */
    public static boolean send(ServerPlayer player, EntityType<?> entityType, boolean openScreen) {
        if (!canOpen(player, entityType)) {
            return false;
        }
        WorldSession ws = WorldSession.get();
        if (ws == null) {
            return false;
        }
        EntityOverviewCache.CacheEntry cached = ws.getEntityOverviewCache()
                .getOrCreate(entityType, player.serverLevel());
        ServerNetManager.sendEntityOverview(player, new SendEntityOverviewPacket(
                EntityUtils.getEntityTypeIdName(entityType),
                cached.isValid(), cached.vanillaNbt(), cached.extraNbt(), openScreen));
        return true;
    }

}
