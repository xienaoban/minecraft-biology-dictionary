package io.github.xienaoban.biologydictionary.mixin.entity;

import io.github.xienaoban.biologydictionary.core.session.ServerWorldSession;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(method = "killedEntity", at = @At("HEAD"))
    private void biologydictionary$onEntityKilled(ServerLevel serverLevel, LivingEntity livingEntity, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        Player self = (Player) (Object) this;
        if (self instanceof ServerPlayer serverPlayer) {
            ServerWorldSession session = ServerWorldSession.get();
            if (session != null) {
                session.getDiscoveryManager().onEntityKilled(serverPlayer, livingEntity);
            }
        }
    }

    @Inject(method = "interactOn", at = @At("HEAD"))
    private void biologydictionary$onEntityInteracted(Entity entity, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        Player self = (Player) (Object) this;
        if (self instanceof ServerPlayer serverPlayer) {
            ServerWorldSession session = ServerWorldSession.get();
            if (session != null) {
                session.getDiscoveryManager().onEntityInteracted(serverPlayer, entity);
            }
        }
    }
}
