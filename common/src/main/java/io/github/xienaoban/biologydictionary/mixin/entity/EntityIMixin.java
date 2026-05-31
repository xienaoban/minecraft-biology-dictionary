package io.github.xienaoban.biologydictionary.mixin.entity;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityIMixin {
    @Accessor("FLAG_GLOWING")
    static int biologydictionary$getFlagGlowing() {
        throw new AssertionError();
    }

    @Accessor("wasTouchingWater")
    void biologydictionary$setWasTouchingWater(boolean touchingWater);

    @Invoker("setSharedFlag")
    void biologydictionary$setSharedFlag(int flag, boolean set);
}
