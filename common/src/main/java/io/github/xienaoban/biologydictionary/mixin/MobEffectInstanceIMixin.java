package io.github.xienaoban.biologydictionary.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MobEffectInstance.class)
public interface MobEffectInstanceIMixin {
    @Accessor("duration")
    void biologydictionary$setDuration(int duration);
}
