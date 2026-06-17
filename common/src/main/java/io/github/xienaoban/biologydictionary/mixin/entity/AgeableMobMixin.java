package io.github.xienaoban.biologydictionary.mixin.entity;

import net.minecraft.world.entity.AgeableMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AgeableMob.class)
public abstract class AgeableMobMixin {
    @ModifyArg(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/AgeableMob;setAge(I)V", ordinal = 1), index = 0)
    private int biologydictionary$keepBreedCooldownLocked(int age) {
        AgeableMob self = (AgeableMob) (Object) this;
        int lockedAge = AnimalIMixin.biologydictionary$getParentAgeAfterBreeding() + 1;
        if (self.getAge() == lockedAge) {
            return lockedAge;
        }
        return age;
    }
}
