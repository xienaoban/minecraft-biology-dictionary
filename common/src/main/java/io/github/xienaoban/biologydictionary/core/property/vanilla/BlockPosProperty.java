package io.github.xienaoban.biologydictionary.core.property.vanilla;

import io.github.xienaoban.biologydictionary.core.property.builtin.AbstractProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;

public class BlockPosProperty<E extends Entity> extends AbstractProperty<E, BlockPos> {
    public BlockPosProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        CompoundTag tag = nbt.getCompound(name());
        setVal(tag.isEmpty() ? null : NbtUtils.readBlockPos(tag));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null) {
            nbt.put(name(), NbtUtils.writeBlockPos(getVal()));
        } else {
            nbt.put(name(), new CompoundTag());
        }
    }
}
