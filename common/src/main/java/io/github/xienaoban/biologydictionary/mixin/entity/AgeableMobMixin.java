package io.github.xienaoban.biologydictionary.mixin.entity;

import net.minecraft.world.entity.AgeableMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AgeableMob.class)
public abstract class AgeableMobMixin {
    @Shadow
    protected int forcedAge;

    @Shadow
    public abstract void setAge(int ageToSet);

    @Inject(method = "setAge(I)V", at = @At(value = "HEAD"), cancellable = true)
    private void biologydictionary$setAgeToForcedAge(int ageToSet, CallbackInfo ci) {
        // Do not grow up / breed if forcedAge is set.
        // Make forcedAge acts just like that in 1.21.6.
        if (ageToSet == 0 && forcedAge != 0) {
            setAge(forcedAge);
            ci.cancel();
        }
    }
}
