package io.github.xienaoban.biologydictionary.mixin.entity;

import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public class AnimalMixin {

    /**
     * Inject into spawnChildFromBreeding to make the baby inherit silent status from both parents.
     * This injection happens at RETURN (end) of the method, after all breeding logic is done.
     * We need to use redirect to intercept finalizeSpawnChildFromBreeding call.
     */
    @Inject(method = "finalizeSpawnChildFromBreeding", at = @At("HEAD"))
    private void biologydictionary$inheritSilentFromParents(ServerLevel serverLevel, Animal otherParent, AgeableMob ageableMob, CallbackInfo ci) {
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
