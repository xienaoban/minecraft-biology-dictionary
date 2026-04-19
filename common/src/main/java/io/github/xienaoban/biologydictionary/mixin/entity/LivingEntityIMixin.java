package io.github.xienaoban.biologydictionary.mixin.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityIMixin {
    @Invoker("getHurtSound")
    SoundEvent biologydictionary$getHurtSound(DamageSource damageSource);

    @Invoker("getDeathSound")
    SoundEvent biologydictionary$getDeathSound();
}
