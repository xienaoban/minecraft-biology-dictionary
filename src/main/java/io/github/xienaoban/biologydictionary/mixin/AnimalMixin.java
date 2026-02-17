package io.github.xienaoban.biologydictionary.mixin;

import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Animal.class)
public class AnimalMixin {

    /**
     * Inject into spawnChildFromBreeding to make the baby inherit silent status from both parents.
     * This injection happens after the baby is created and set to baby state (setBaby(true)),
     * capturing the ageableMob local variable which is the baby.
     */
    @Inject(method = "spawnChildFromBreeding", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Animal;finalizeSpawnChildFromBreeding(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/Animal;Lnet/minecraft/world/entity/AgeableMob;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void inheritSilentFromParents(ServerLevel serverLevel, Animal otherParent, CallbackInfo ci, AgeableMob ageableMob) {
        // Check if the config is enabled
        if (!ConfigsManager.getServer().isInheritSilentFromParents()) {
            return;
        }

        // Check if both parents are silent
        Animal self = (Animal) (Object) this;
        if (!self.isSilent() || !otherParent.isSilent()) {
            return;
        }

        // Check if baby exists and set it to silent
        if (ageableMob != null) {
            ageableMob.setSilent(true);
        }
    }
}
