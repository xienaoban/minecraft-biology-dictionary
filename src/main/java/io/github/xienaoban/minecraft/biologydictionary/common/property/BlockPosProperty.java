package io.github.xienaoban.minecraft.biologydictionary.common.property;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public final class BlockPosProperty<E extends Entity> extends AbstractProperty<E, BlockPos> {
    public BlockPosProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {

        set(nbt.read(name(), BlockPos.CODEC).orElse(null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        nbt.storeNullable(name(), BlockPos.CODEC, get());
    }
}
