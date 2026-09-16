package io.github.xienaoban.biologydictionary.mixin.entity;

import com.mojang.serialization.Codec;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.Mannequin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mannequin.class)
public interface MannequinIMixin {
    @Accessor("LAYERS_CODEC")
    static Codec<Byte> biologydictionary$getLayersCodec() { throw new AssertionError(); }

    @Accessor("POSE_CODEC")
    static Codec<Pose> biologydictionary$getPoseCodec() { throw new AssertionError(); }
}
