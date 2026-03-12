package io.github.xienaoban.biologydictionary.mixin.entity;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityIMixin {
    @Accessor("wasTouchingWater")
    void biologydictionary$setWasTouchingWater(boolean touchingWater);

    @Accessor("FLAG_GLOWING")
    static int biologydictionary$getFlagGlowing() {
        throw new AssertionError();
    }
}
