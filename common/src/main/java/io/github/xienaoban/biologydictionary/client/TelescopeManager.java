package io.github.xienaoban.biologydictionary.client;

import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.discovery.ClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

@Environment(EnvType.CLIENT)
public final class TelescopeManager {

    static final int MAX_PROGRESS = 100;

    private Entity lastScopingEntity = null;
    private int discoveryProgress = 0;

    public Entity getScopingEntity() {
        return lastScopingEntity;
    }

    public int getDiscoveryProgress() {
        return discoveryProgress;
    }

    public void tick() {
        ClientWorldSession session = ClientWorldSession.get();
        if (session == null) { return; }

        Minecraft client = ClientUtils.getClient();
        LocalPlayer player = ClientUtils.getClientPlayer(client);
        if (player == null || !player.isScoping()) {
            if (lastScopingEntity != null) {
                lastScopingEntity = null;
                discoveryProgress = 0;
            }
            return;
        }

        Entity target = null;
        double range = ConfigsManager.getServer().getTelescopeRange();
        HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(
                player, entity -> !entity.isSpectator() && entity.isPickable(), range);
        if (hitResult instanceof EntityHitResult entityHit
                && PlayerUtils.isWithinRangeAndUnobstructed(player, entityHit.getEntity(), range)) {
            target = entityHit.getEntity();
        }

        ClientDiscoveryCache cache = session.getDiscoveryClientCache();

        if (target == null) {
            discoveryProgress = Math.max(0, discoveryProgress - 2);
        } else if (target != lastScopingEntity) {
            discoveryProgress = 0;
            lastScopingEntity = target;
        } else {
            // Same target
            EntityType<?> entityType = EntityUtils.getEntityType(target);
            if (cache.isDiscovered(entityType)) {
                discoveryProgress = 0;
            } else {
                double rangeCubed = range * range;
                double distCubed = player.getEyePosition().distanceToSqr(target.getBoundingBox().getCenter());
                int increment = Math.min(Math.max(1, (int)(rangeCubed / distCubed)), 20);
                discoveryProgress = Math.min(MAX_PROGRESS, discoveryProgress + increment);
                if (discoveryProgress == MAX_PROGRESS) {
                    cache.onEntityObservedWithTelescope(player, target);
                }
            }
        }
    }
}
