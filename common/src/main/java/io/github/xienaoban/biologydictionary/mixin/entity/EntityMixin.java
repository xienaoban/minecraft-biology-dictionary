package io.github.xienaoban.biologydictionary.mixin.entity;

import io.github.xienaoban.biologydictionary.core.skill.entity.EntitySetPortalCooldownSkill;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow
    private int portalCooldown;

    @Inject(method = "setPortalCooldown()V", at = @At(value = "HEAD"), cancellable = true)
    private void lockSetPortalCooldown(CallbackInfo ci) {
        if (portalCooldown == EntitySetPortalCooldownSkill.ENTITY_PORTAL_COOLDOWN_INFINITY) {
            ci.cancel();
        }
    }

    @Inject(method = "processPortalCooldown()V", at = @At(value = "HEAD"), cancellable = true)
    private void lockTickPortalCooldown(CallbackInfo ci) {
        if (portalCooldown == EntitySetPortalCooldownSkill.ENTITY_PORTAL_COOLDOWN_INFINITY) {
            ci.cancel();
        }
    }
}
