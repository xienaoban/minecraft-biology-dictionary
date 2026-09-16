package io.github.xienaoban.biologydictionary.mixin.entity;

import com.mojang.serialization.Codec;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.fox.Fox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Fox.class)
public interface FoxIMixin {
    @Accessor("TRUSTED_LIST_CODEC")
    static Codec<List<EntityReference<LivingEntity>>> biologydictionary$getTrustedListCodec() {
        throw new AssertionError();
    }
}
