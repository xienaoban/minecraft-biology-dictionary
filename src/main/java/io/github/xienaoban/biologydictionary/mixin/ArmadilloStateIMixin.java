package io.github.xienaoban.biologydictionary.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Armadillo.ArmadilloState.class)
public interface ArmadilloStateIMixin {
    @Accessor("CODEC")
    static Codec<Armadillo.ArmadilloState> getCodec() { throw new AssertionError(); }
}
