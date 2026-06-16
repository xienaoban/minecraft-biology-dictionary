package io.github.xienaoban.biologydictionary.mixin.entity;

import io.github.xienaoban.biologydictionary.core.session.ServerWorldSession;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "die", at = @At("HEAD"))
    private void biologydictionary$onKilledByEntity(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        // TODO: verify this 26.1.2 injection timing against the old awardStat injection during runtime testing.
        LivingEntity killer = self.getKillCredit();
        if (killer != null) {
            ServerWorldSession sws = ServerWorldSession.get();
            if (sws != null) {
                sws.getDiscoveryManager().onPlayerKilledBy(self, killer);
            }
        }
    }
}
