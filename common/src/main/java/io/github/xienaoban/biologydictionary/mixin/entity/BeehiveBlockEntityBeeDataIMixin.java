package io.github.xienaoban.biologydictionary.mixin.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BeehiveBlockEntity.BeeData.class)
public interface BeehiveBlockEntityBeeDataIMixin {
    @Accessor("entityData")
    CompoundTag biologydictionary$getEntityData();

    @Accessor("ticksInHive")
    int biologydictionary$getTicksInHive();

    @Accessor("minOccupationTicks")
    int biologydictionary$getMinOccupationTicks();
}
