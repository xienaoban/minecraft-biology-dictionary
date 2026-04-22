package io.github.xienaoban.biologydictionary.client;

import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

@Environment(EnvType.CLIENT)
public final class TelescopeManager {

    static final double RANGE = 100.0;
    private Entity lastScopingEntity = null;

    public void tick() {
        ClientWorldSession session = ClientWorldSession.get();
        if (session == null) {
            return;
        }
        Minecraft client = ClientUtils.getClient();
        LocalPlayer player = ClientUtils.getClientPlayer(client);
        if (player == null || !player.isScoping()) {
            return;
        }
        Entity target = null;
        HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(
                player, entity -> !entity.isSpectator() && entity.isPickable(), RANGE);
        if (hitResult instanceof EntityHitResult entityHit
                && PlayerUtils.isWithinRangeAndUnobstructed(player, entityHit.getEntity(), RANGE)) {
            target = entityHit.getEntity();
        }
        if (target != lastScopingEntity && target != null) {
            session.getDiscoveryClientCache().onEntityObservedWithTelescope(player, target);
        }
        lastScopingEntity = target;
    }
}
