package io.github.xienaoban.minecraft.biologydictionary.common.property;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public final class BlockPosProperty<E extends Entity> extends CodecProperty<E, BlockPos> {
    public BlockPosProperty(String propertyName) {
        super(propertyName, BlockPos.CODEC);
    }
}
