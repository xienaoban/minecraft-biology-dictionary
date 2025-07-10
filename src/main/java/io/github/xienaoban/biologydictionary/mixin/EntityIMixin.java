package io.github.xienaoban.biologydictionary.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityIMixin {
    @Accessor
    void setWasTouchingWater(boolean touchingWater);
}
