package io.github.xienaoban.biologydictionary.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Entity.class)
public interface EntityIMixin {
    @Accessor("TAG_LIST_CODEC")
    static Codec<List<String>> getTagListCodec() { throw new AssertionError(); }

    @Accessor
    void setWasTouchingWater(boolean touchingWater);
}
