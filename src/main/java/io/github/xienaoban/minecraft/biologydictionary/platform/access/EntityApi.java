package io.github.xienaoban.minecraft.biologydictionary.platform.access;

import io.github.xienaoban.minecraft.biologydictionary.platform.mixin.EntityIMixin;
import net.minecraft.world.entity.Entity;

public final class EntityApi {
    public static void setInWater(Entity entity, boolean inWater) {
        ((EntityIMixin) entity).setWasTouchingWater(inWater);
    }
}
