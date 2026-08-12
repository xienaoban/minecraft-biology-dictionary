package io.github.xienaoban.biologydictionary.client;

import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.EntityManager;
import io.github.xienaoban.biologydictionary.core.discovery.ClientDiscoveryCacheManager;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySources;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

@ClientOnly
public final class TelescopeManager {

    static final int MAX_PROGRESS = 100;
    private static final int COMPLETED_DISPLAY_TICKS = 20; // 1s

    private Entity lastScopingEntity = null;
    private float discoveryProgress = 0;
    private int completedDisplayTicks = 0;

    public Entity getScopingEntity() {
        return lastScopingEntity;
    }

    public int getDiscoveryProgress() {
        return (int) discoveryProgress;
    }

    public boolean isCompletedDisplay() {
        return completedDisplayTicks > 0;
    }

    public void tick() {
        ClientWorldSession cws = ClientWorldSession.get();
        if (cws == null) { return; }

        Configs.ServerConfigs serverConfig = ConfigsManager.getServer();
        if (serverConfig.getDiscoveryStrategy() != Configs.ServerConfigs.DiscoveryStrategyMode.BIOLOGY_DICTIONARY
                || !serverConfig.isDiscoveryByTelescope()) {
            reset();
            return;
        }

        Minecraft client = ClientUtils.getClient();
        LocalPlayer player = ClientUtils.getClientPlayer(client);
        if (player == null || !player.isScoping()) {
            reset();
            return;
        }

        Entity target = null;
        int range = ConfigsManager.getServer().getTelescopeDiscoveryRange();
        HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(
                player, entity -> !entity.isSpectator() && entity.isPickable(), range);
        if (hitResult instanceof EntityHitResult entityHit
                && PlayerUtils.isWithinRangeAndUnobstructed(player, entityHit.getEntity(), range)) {
            target = entityHit.getEntity();
            // Blacklisted entities are treated as no target: no progress, no animation.
            if (EntityManager.isEntityTypeBlacklisted(EntityUtils.getEntityType(target))) {
                target = null;
            }
        }

        // New target interrupts the completed display
        if (completedDisplayTicks > 0) {
            if (target == null) {
                completedDisplayTicks--;
            } else if (target != lastScopingEntity) {
                completedDisplayTicks = 0;
            }
        }

        ClientDiscoveryCacheManager dcm = cws.getDiscoveryCacheManager();

        if (target == null) {
            discoveryProgress = Math.max(0, discoveryProgress - 2);
        } else if (target != lastScopingEntity) {
            discoveryProgress = 0;
            lastScopingEntity = target;
        } else {
            // Same target
            EntityType<?> entityType = EntityUtils.getEntityType(target);
            if (dcm.isDiscovered(entityType)) {
                discoveryProgress = 0;
            } else {
                float distSq = (float) player.getEyePosition().distanceToSqr(target.getBoundingBox().getCenter());
                float t = Math.max(0, 1.0f - (float) Math.sqrt(distSq) / range);
                float increment = 1.0f + (19.0f / 6.0f) * t;
                discoveryProgress = Math.min(MAX_PROGRESS, discoveryProgress + increment);
                if (discoveryProgress >= MAX_PROGRESS) {
                    dcm.onDiscoveryEvent(DiscoverySources.TELESCOPE_OBSERVE, player, target);
                    completedDisplayTicks = COMPLETED_DISPLAY_TICKS;
                }
            }
        }
    }

    private void reset() {
        if (lastScopingEntity != null) {
            lastScopingEntity = null;
            discoveryProgress = 0;
        }
        completedDisplayTicks = 0;
    }
}
